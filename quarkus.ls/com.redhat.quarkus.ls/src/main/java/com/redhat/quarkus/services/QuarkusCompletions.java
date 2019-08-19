/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.SnippetsBuilder;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.utils.DocumentationUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils.PropertiesToken;

/**
 * The Quarkus completions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuarkusCompletions.class.getName());
	private static final Collection<String> BOOLEAN_ENUMS = Collections
			.unmodifiableCollection(Arrays.asList("false", "true"));

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		String text = document.getText();
		String lineText = null;
		int offset;
		try {
			offset = document.offsetAt(position);
			lineText = document.lineText(position.getLine());
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCompletion, position error", e);
			return list;
		}
		int startLineOffset = offset - position.getCharacter();
		PropertiesToken token = PropertiesScannerUtils.getTokenAt(text, startLineOffset, offset);
		switch (token.getType()) {
		case COMMENTS:
			// no completions
			break;
		case KEY:
			// completion on property key
			collectPropertyKeySuggestions(document, position.getLine(), lineText.length(), projectInfo,
					completionSettings, list);
			break;
		case VALUE:
			// completion on property value
			collectPropertyValueSuggestions(document, token, projectInfo, completionSettings, list);
			break;
		}
		return list;
	}

	/**
	 * Collect property keys.
	 * 
	 * @param document           the document
	 * @param token              the token
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyKeySuggestions(TextDocument document, int line, int endCharacter,
			QuarkusProjectInfo projectInfo, QuarkusCompletionSettings completionSettings, CompletionList list) {

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		boolean markdownSupprted = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);

		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {

			CompletionItem item = new CompletionItem(property.getPropertyName());
			item.setKind(CompletionItemKind.Property);

			String defaultValue = property.getDefaultValue();
			Collection<String> enums = getEnums(property);

			StringBuilder insertText = new StringBuilder();
			insertText.append(getPropertyName(property.getPropertyName(), snippetsSupported));
			insertText.append(' '); // TODO: this space should be configured in format settings
			insertText.append('=');
			insertText.append(' '); // TODO: this space should be configured in format settings

			if (enums != null && enums.size() > 0) {
				// Enumerations
				if (snippetsSupported) {
					// Because of LSP limitation, we cannot use default value with choice.
					SnippetsBuilder.choice(1, enums, insertText);
				} else {
					// Plaintext: use default value or the first enum if no default value.
					String defaultEnumValue = defaultValue != null ? defaultValue : enums.iterator().next();
					insertText.append(defaultEnumValue);
				}
			} else if (defaultValue != null) {
				// Default value
				if (snippetsSupported) {
					SnippetsBuilder.placeholders(0, defaultValue, insertText);
				} else {
					insertText.append(defaultValue);
				}
			} else {
				if (snippetsSupported) {
					SnippetsBuilder.tabstops(0, insertText);
				}
			}

			Range range = new Range(new Position(line, 0), new Position(line, endCharacter));
			TextEdit textEdit = new TextEdit(range, insertText.toString());
			item.setTextEdit(textEdit);

			item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);
			item.setDocumentation(DocumentationUtils.getDocumentation(property, markdownSupprted));
			list.getItems().add(item);
		}
	}

	private static String getPropertyName(String propertyName, boolean snippetsSupported) {
		int index = propertyName.indexOf("{*}");
		if (index != -1) {
			int i = 1;
			String current = propertyName;
			StringBuilder newName = new StringBuilder();
			while (index != -1) {
				newName.append(current.substring(0, index));
				current = current.substring(index + 3, current.length());
				newName.append("\"");
				if (snippetsSupported) {
					newName.append("${");
					newName.append(i++);
					newName.append(":key}");
				}
				newName.append("\"");
				index = current.indexOf("{*}");
			}
			newName.append(current);
			return newName.toString();
		}
		return propertyName;
	}

	/**
	 * Returns the enums values according the property type.
	 * 
	 * @param property the Quarkus property
	 * @return the enums values according the property type
	 */
	private static Collection<String> getEnums(ExtendedConfigDescriptionBuildItem property) {
		if (property.getEnums() != null) {
			return property.getEnums();
		}
		if (property.isBooleanType()) {
			return BOOLEAN_ENUMS;
		}
		return null;
	}

	/**
	 * Collect property values.
	 * 
	 * @param document           the document
	 * @param token              the token
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyValueSuggestions(TextDocument document, PropertiesToken token,
			QuarkusProjectInfo projectInfo, QuarkusCompletionSettings completionSettings, CompletionList list) {

	}
}
