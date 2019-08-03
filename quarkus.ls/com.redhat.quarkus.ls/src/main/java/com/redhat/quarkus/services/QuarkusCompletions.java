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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
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

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		String text = document.getText();
		int offset;
		try {
			offset = document.offsetAt(position);
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
			collectPropertyKeySuggestions(document, token, projectInfo, completionSettings, list);
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
	private static void collectPropertyKeySuggestions(TextDocument document, PropertiesToken token,
			QuarkusProjectInfo projectInfo, QuarkusCompletionSettings completionSettings, CompletionList list) {

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		boolean markdownSupprted = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);

		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {

			CompletionItem item = new CompletionItem(property.getPropertyName());
			item.setKind(CompletionItemKind.Property);
			if (snippetsSupported) {
				String defaultValue = property.getDefaultValue();
				StringBuilder insertText = new StringBuilder();
				insertText.append(property.getPropertyName());
				insertText.append(' '); // TODO: this space should be configured in format settings
				insertText.append('=');
				insertText.append(' '); // TODO: this space should be configured in format settings
				if (defaultValue != null) {
					SnippetsBuilder.placeholders(0, defaultValue, insertText);
				} else {
					SnippetsBuilder.tabstops(0, insertText);
				}
				item.setInsertText(insertText.toString());
				item.setInsertTextFormat(InsertTextFormat.Snippet);
			}
			item.setDocumentation(DocumentationUtils.getDocumentation(property, markdownSupprted));
			list.getItems().add(item);
		}
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
