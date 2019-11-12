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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.EnumItem;
import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.SnippetsBuilder;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.Node;
import com.redhat.quarkus.model.Node.NodeType;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.Property;
import com.redhat.quarkus.model.PropertyKey;
import com.redhat.quarkus.model.values.ValuesRulesManager;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.utils.DocumentationUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils;
import com.redhat.quarkus.utils.QuarkusPropertiesUtils.FormattedPropertyResult;

/**
 * The Quarkus completions
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuarkusCompletions.class.getName());

	/**
	 * Returns completion list for the given position
	 * 
	 * @param document           the properties model document
	 * @param position           the position where completion was triggered
	 * @param projectInfo        the Quarkus project information
	 * @param valuesRulesManager manager for values rules
	 * @param completionSettings the completion settings
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(PropertiesModel document, Position position, QuarkusProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, QuarkusCompletionSettings completionSettings,
			CancelChecker cancelChecker) {
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
			collectPropertyValueSuggestions(node, document, projectInfo, valuesRulesManager, completionSettings, list);
			break;
		default:
			// completion on property key
			collectPropertyKeySuggestions(offset, node, document, projectInfo, valuesRulesManager, completionSettings,
					list);
			break;
		}
		return list;
	}

	/**
	 * Collect property keys.
	 * 
	 * @param offset             the property key node
	 * @param node
	 * @param projectInfo        the Quarkus project information
	 * @param valuesRulesManager
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyKeySuggestions(int offset, Node node, PropertiesModel model,
			QuarkusProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
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

		String profile = null;
		if (node != null && node.getNodeType() == NodeType.PROPERTY_KEY) {
			PropertyKey key = (PropertyKey) node;
			if (key.isBeforeProfile(offset)) {
				// Collect all existing profiles declared in application.properties
				Set<String> profiles = model.getChildren().stream().filter(n -> n.getNodeType() == NodeType.PROPERTY)
						.map(n -> {
							Property property = (Property) n;
							return property.getProfile();
						}).filter(Objects::nonNull).filter(not(String::isEmpty)).distinct().collect(Collectors.toSet());
				// merge existings profiles with default profiles.
				profiles.addAll(QuarkusModel.getDefaultProfileNames());
				// Completion on profiles
				for (String p : profiles) {
					CompletionItem item = new CompletionItem(p);
					item.setKind(CompletionItemKind.Struct);
					String insertText = new StringBuilder("%").append(p).toString();

					TextEdit textEdit = new TextEdit(range, insertText);
					item.setTextEdit(textEdit);
					item.setInsertTextFormat(InsertTextFormat.PlainText);
					item.setFilterText(insertText);
					addDocumentationIfDefaultProfile(item, markdownSupported);
					list.getItems().add(item);
				}
				return;
			}
			profile = key.getProfile();
		}

		Set<String> existingProperties = getExistingProperties(model);

		// Completion on Quarkus properties
		for (ExtendedConfigDescriptionBuildItem property : projectInfo.getProperties()) {
			if (property == null) {
				continue;
			}
			String propertyName = property.getPropertyName();
			if (profile != null) {
				propertyName = "%" + profile + "." + propertyName;
			}
			if (existingProperties.contains(propertyName) && node.getNodeType() == NodeType.PROPERTY_KEY
					&& !((PropertyKey) node).getPropertyNameWithProfile().equals(propertyName)) {
				// don't add completion items for properties that already exist
				// unless current node has a key equal to current property name
				continue;
			}

			CompletionItem item = new CompletionItem(property.getPropertyName());
			item.setKind(CompletionItemKind.Property);

			String defaultValue = property.getDefaultValue();
			Collection<EnumItem> enums = QuarkusPropertiesUtils.getEnums(property, model, valuesRulesManager);

			StringBuilder insertText = new StringBuilder();
			if (profile != null) {
				insertText.append('%');
				insertText.append(profile);
				insertText.append('.');
			}
			FormattedPropertyResult formattedProperty = getPropertyName(property.getPropertyName(), snippetsSupported);
			insertText.append(formattedProperty.getPropertyName());

			String filterText = insertText.toString();
			item.setFilterText(filterText);

			insertText.append('='); // TODO: spaces around the equals sign should be configured in format settings

			if (enums != null && enums.size() > 0) {
				// Enumerations
				if (snippetsSupported) {
					// Because of LSP limitation, we cannot use default value with choice.
					SnippetsBuilder.choice(formattedProperty.getMappedParameterCount() + 1,
							enums.stream().map(EnumItem::getName).collect(Collectors.toList()), insertText);
				} else {
					// Plaintext: use default value or the first enum if no default value.
					String defaultEnumValue = defaultValue != null ? defaultValue : enums.iterator().next().getName();
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
			item.setDocumentation(DocumentationUtils.getDocumentation(property, profile, markdownSupported));
			list.getItems().add(item);
		}
	}

	/**
	 * Adds documentation to <code>item</code> if <code>item</code> represents a
	 * default profile
	 * 
	 * @param item
	 * @param markdown
	 */
	private static void addDocumentationIfDefaultProfile(CompletionItem item, boolean markdown) {

		for (EnumItem profile : QuarkusModel.DEFAULT_PROFILES) {
			if (profile.getName().equals(item.getLabel())) {
				item.setDocumentation(DocumentationUtils.getDocumentation(profile, markdown));
				break;
			}
		}
	}

	private static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	/**
	 * Returns a set of property names for the properties in <code>model</code>.
	 * 
	 * @param model the <code>PropertiesModel</code> to get property names from
	 * @return set of property names for the properties in <code>model</code>
	 */
	private static Set<String> getExistingProperties(PropertiesModel model) {
		Set<String> set = new HashSet<String>();
		for (Node child : model.getChildren()) {
			if (child.getNodeType() == NodeType.PROPERTY) {
				String name = ((Property) child).getPropertyNameWithProfile();
				if (name != null && !name.isEmpty()) {
					set.add(name);
				}
			}
		}
		return set;
	}

	/**
	 * Returns the property name to insert when completion is applied.
	 * 
	 * @param propertyName      the property name
	 * @param snippetsSupported true if snippet is supported and false otherwise.
	 * @return the property name to insert when completion is applied.
	 */
	private static FormattedPropertyResult getPropertyName(String propertyName, boolean snippetsSupported) {
		if (!snippetsSupported) {
			return new FormattedPropertyResult(propertyName, 0);
		}
		return QuarkusPropertiesUtils.formatPropertyForCompletion(propertyName);
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
			QuarkusProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
			QuarkusCompletionSettings completionSettings, CompletionList list) {

		Node parent = node.getParent();
		if (parent != null && parent.getNodeType() != Node.NodeType.PROPERTY) {
			return;
		}
		Property property = (Property) parent;
		String propertyName = property.getPropertyName();

		ExtendedConfigDescriptionBuildItem item = QuarkusPropertiesUtils.getProperty(propertyName, projectInfo);
		if (item != null) {
			Collection<EnumItem> enums = QuarkusPropertiesUtils.getEnums(item, model, valuesRulesManager);
			if (enums != null && !enums.isEmpty()) {
				boolean markdownSupported = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);
				for (EnumItem e : enums) {
					list.getItems().add(getValueCompletionItem(e, node, model, markdownSupported));
				}
			}
		}
	}

	/**
	 * Returns the <code>CompletionItem</code> which offers completion for value
	 * completion for <code>value</code> at the start offset of <code>node</code>.
	 * 
	 * @param value             the value for completion
	 * @param docs              the documentation for completion
	 * @param node              the node where its start offset is where value
	 *                          completion occurs
	 * @param model             the property model
	 * @param markdownSupported true if markdown is supported and false otherwise.
	 * @return the value completion item
	 */
	private static CompletionItem getValueCompletionItem(EnumItem item, Node node, PropertiesModel model,
			boolean markdownSupported) {
		String value = item.getName();
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
		completionItem.setDocumentation(DocumentationUtils.getDocumentation(item, markdownSupported));

		return completionItem;
	}
}