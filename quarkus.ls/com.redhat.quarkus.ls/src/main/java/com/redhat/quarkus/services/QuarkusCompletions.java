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
import com.redhat.quarkus.commons.PropertyInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.SnippetsBuilder;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.utils.DocumentationUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;

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

	/**
	 * Returns completion list for the given position
	 * 
	 * @param document           the properties model document
	 * @param position           the position where completion was triggereds
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		int offset = -1;
		Node node = null;
		try {
			offset = document.offsetAt(position);
			node = document.findNodeAt(offset);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCompletion, position error", e);
			return list;
		}
		if (node == null) {
			return list;
		}

		switch (node.getNodeType()) {
		case COMMENTS:
			// no completions
			break;
		case ASSIGN:
		case PROPERTY_VALUE:
			// completion on property value
			collectPropertyValueSuggestions(node, document, projectInfo, completionSettings, list);
			break;
		default:
			// completion on property key
			collectPropertyKeySuggestions(offset, document, projectInfo, completionSettings, list);
			break;
		}
		return list;
	}

	/**
	 * Collect property keys.
	 * 
	 * @param offset             the property key node
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyKeySuggestions(int offset, PropertiesModel model, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CompletionList list) {

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		boolean markdownSupported = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);

		Range range = null;
		try {
			range = model.getDocument().lineRangeAt(offset);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCompletion#collectPropertyKeySuggestions, position error", e);
			return;
		}

		// TODO : get the profile of the property where completion was triggered
		String profil = null;
		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {

			CompletionItem item = new CompletionItem(property.getPropertyName());
			item.setKind(CompletionItemKind.Property);

			String defaultValue = property.getDefaultValue();
			Collection<String> enums = getEnums(property);

			StringBuilder insertText = new StringBuilder();
			insertText.append(getPropertyName(property.getPropertyName(), snippetsSupported));
			insertText.append('='); // TODO: spaces around the equals sign should be configured in format settings

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

			TextEdit textEdit = new TextEdit(range, insertText.toString());
			item.setTextEdit(textEdit);

			item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);
			item.setDocumentation(DocumentationUtils.getDocumentation(property, profil, markdownSupported));
			list.getItems().add(item);
		}
	}

	/**
	 * Returns the property name to insert when completion is applied.
	 * 
	 * @param propertyName      the property name
	 * @param snippetsSupported true if snippet is supported and false otherwise.
	 * @return the property name to insert when completion is applied.
	 */
	private static String getPropertyName(String propertyName, boolean snippetsSupported) {
		if (!snippetsSupported) {
			return propertyName;
		}
		return QuarkusPropertiesUtils.formatPropertyForCompletion(propertyName);
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
	 * @param node               the property value node
	 * @param projectInfo        the Quarkus project information
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyValueSuggestions(Node node, PropertiesModel model,
			QuarkusProjectInfo projectInfo, QuarkusCompletionSettings completionSettings, CompletionList list) {

		Node parent = node.getParent();
		if (parent != null && parent.getNodeType() != Node.NodeType.PROPERTY) {
			return;
		}

		PropertyKey key = getPropertyKeyFromProperty((Property) parent);

		if (key == null) {
			return;
		}

		String keyText = key.getText().trim();

		PropertyInfo propertyInfo = QuarkusPropertiesUtils.getProperty(keyText, projectInfo);
		if (propertyInfo != null && propertyInfo.getProperty() != null) {
			Collection<String> enums = getEnums(propertyInfo.getProperty());
			if (enums != null && !enums.isEmpty()) {
				for (String e : enums) {
					list.getItems().add(getValueCompletionItem(e, node, model));
				}
			}
		}
	}

	/**
	 * Returns the <code>PropertyKey</code> object associated with
	 * <code>property</code>.
	 * 
	 * @param property the property to find the <code>PropertyKey</code> for
	 * @return the <code>PropertyKey</code> object associated with
	 *         <code>property</code>
	 */
	private static PropertyKey getPropertyKeyFromProperty(Property property) {
		Node key = property.getKey();

		if (key == null) {
			return null;
		}

		return (PropertyKey) key;
	}

	/**
	 * Returns the <code>CompletionItem</code> which offers completion for value
	 * completion for <code>value</code> at the start offset of <code>node</code>.
	 * 
	 * @param value the value for completion
	 * @param node  the node where its start offset is where value completion occurs
	 * @param model the property model
	 * @return the value completion item
	 */
	private static CompletionItem getValueCompletionItem(String value, Node node, PropertiesModel model) {
		CompletionItem completionItem = new CompletionItem(value);
		completionItem.setKind(CompletionItemKind.Value);

		Range range = null;
		try {
			TextDocument doc = model.getDocument();
			int startOffset = node.getStart();
			range = doc.lineRangeAt(startOffset);
			range.setStart(doc.positionAt(startOffset));
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuarkusCompletion#getEnumCompletionItem, position error", e);
		}

		TextEdit textEdit = new TextEdit(range, value);
		completionItem.setTextEdit(textEdit);
		return completionItem;
	}
}