/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConverterKind;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.SnippetsBuilder;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyKey;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.values.ValuesRulesManager;
import org.eclipse.lsp4mp.settings.MicroProfileCompletionSettings;
import org.eclipse.lsp4mp.settings.MicroProfileFormattingSettings;
import org.eclipse.lsp4mp.snippets.LanguageId;
import org.eclipse.lsp4mp.snippets.SnippetContextForProperties;
import org.eclipse.lsp4mp.utils.DocumentationUtils;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils;
import org.eclipse.lsp4mp.utils.MicroProfilePropertiesUtils.FormattedPropertyResult;

/**
 * The MicroProfile completions
 * 
 * @author Angelo ZERR
 *
 */
class MicroProfileCompletions {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileCompletions.class.getName());
	private TextDocumentSnippetRegistry snippetRegistry;

	/**
	 * Returns completion list for the given position
	 * 
	 * @param document           the properties model document
	 * @param position           the position where completion was triggered
	 * @param projectInfo        the MicroProfile project information
	 * @param valuesRulesManager manager for values rules
	 * @param completionSettings the completion settings
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(PropertiesModel document, Position position, MicroProfileProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, MicroProfileCompletionSettings completionSettings,
			MicroProfileFormattingSettings formattingSettings, CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		int offset = -1;
		Node node = null;
		try {
			offset = document.offsetAt(position);
			node = document.findNodeAt(offset);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileCompletions, position error", e);
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
					formattingSettings, list);
			// Collect completion items with snippet
			collectSnippetSuggestions(offset, node, document, projectInfo, completionSettings, getSnippetRegistry(),
					list);
			break;
		}
		return list;
	}

	/**
	 * Collect property keys.
	 * 
	 * @param offset             the offset where completion was invoked
	 * @param node               the property key node
	 * @param projectInfo        the MicroProfile project information
	 * @param valuesRulesManager the values rules manager
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyKeySuggestions(int offset, Node node, PropertiesModel model,
			MicroProfileProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
			MicroProfileCompletionSettings completionSettings, MicroProfileFormattingSettings formattingSettings,
			CompletionList list) {

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		boolean markdownSupported = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);

		Range range = null;
		try {
			range = model.getDocument().lineRangeAt(offset);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileCompletion#collectPropertyKeySuggestions, position error", e);
			return;
		}

		String profile = null;
		if (node != null && node.getNodeType() == NodeType.PROPERTY_KEY) {
			PropertyKey key = (PropertyKey) node;
			if (key.isBeforeProfile(offset)) {
				collectProfileSuggestions(offset, key, model, markdownSupported, list);
				return;
			}
			profile = key.getProfile();
		}

		Set<String> existingProperties = getExistingProperties(model);

		// Completion on MicroProfile properties
		for (ItemMetadata property : projectInfo.getProperties()) {
			if (property == null) {
				continue;
			}
			String propertyName = property.getName();
			if (profile != null) {
				propertyName = "%" + profile + "." + propertyName;
			}
			if (existingProperties.contains(propertyName) && node.getNodeType() == NodeType.PROPERTY_KEY
					&& !((PropertyKey) node).getPropertyNameWithProfile().equals(propertyName)) {
				// don't add completion items for properties that already exist
				// unless current node has a key equal to current property name
				continue;
			}

			String name = property.getName();
			CompletionItem item = new CompletionItem(name);
			item.setKind(CompletionItemKind.Property);

			String defaultValue = property.getDefaultValue();
			Collection<ValueHint> enums = MicroProfilePropertiesUtils.getEnums(property, projectInfo, model,
					valuesRulesManager);

			StringBuilder insertText = new StringBuilder();
			if (profile != null) {
				insertText.append('%');
				insertText.append(profile);
				insertText.append('.');
			}
			FormattedPropertyResult formattedProperty = getPropertyName(name, snippetsSupported);
			insertText.append(formattedProperty.getPropertyName());

			String filterText = insertText.toString();
			item.setFilterText(filterText);

			if (formattingSettings.isSurroundEqualsWithSpaces()) {
				insertText.append(' ');
			}
			insertText.append('=');
			if (formattingSettings.isSurroundEqualsWithSpaces()) {
				insertText.append(' ');
			}

			if (enums != null && enums.size() > 0) {
				// Enumerations
				if (snippetsSupported) {
					// Because of LSP limitation, we cannot use default value with choice.
					SnippetsBuilder.choice(formattedProperty.getParameterCount() + 1,
							enums.stream().map(valueHint -> valueHint.getPreferredValue(property.getConverterKinds()))
									.collect(Collectors.toList()),
							insertText);
				} else {
					// Plaintext: use default value or the first enum if no default value.
					String defaultEnumValue = defaultValue != null ? defaultValue : enums.iterator().next().getValue();
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
	 * Collect Quarkus profiles
	 * 
	 * @param offset            the offset where completion was invoked
	 * @param key               the property key
	 * @param model             the properties model
	 * @param markdownSupported boolean determining if markdown is supported
	 * @param list              the completion list
	 */
	private static void collectProfileSuggestions(int offset, PropertyKey key, PropertiesModel model,
			boolean markdownSupported, CompletionList list) {

		Range range = null;
		Position currPosition = null;
		boolean addPeriod = false;
		String line = null;
		TextDocument textDocument = model.getDocument();
		try {
			range = textDocument.lineRangeAt(offset);
			currPosition = textDocument.positionAt(offset);
			line = textDocument.lineText(currPosition.getLine());
			addPeriod = currPosition.getCharacter() < line.length() && line.charAt(currPosition.getCharacter()) != '.';
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileCompletion#collectPropertyKeySuggestions, position error", e);
			return;
		}

		// Collect all existing profiles declared in application.properties
		Set<String> profiles = model.getChildren().stream().filter(n -> n.getNodeType() == NodeType.PROPERTY).map(n -> {
			Property property = (Property) n;
			return property.getProfile();
		}).filter(Objects::nonNull).filter(not(String::isEmpty)).distinct().collect(Collectors.toSet());
		// merge existings profiles with default profiles.
		profiles.addAll(QuarkusModel.getDefaultProfileNames());
		// Completion on profiles
		for (String p : profiles) {
			if (p.equals(key.getProfile())) {
				continue;
			}

			CompletionItem item = new CompletionItem(p);
			item.setKind(CompletionItemKind.Struct);

			String insertText = new StringBuilder("%").append(p).append(addPeriod ? "." : "").toString();
			range.setEnd(currPosition);
			TextEdit textEdit = new TextEdit(range, insertText);
			item.setTextEdit(textEdit);
			item.setInsertTextFormat(InsertTextFormat.PlainText);
			item.setFilterText(insertText);
			addDocumentationIfDefaultProfile(item, markdownSupported);
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

		for (ValueHint profile : QuarkusModel.DEFAULT_PROFILES.getValues()) {
			if (profile.getValue().equals(item.getLabel())) {
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
		return MicroProfilePropertiesUtils.formatPropertyForCompletion(propertyName);
	}

	/**
	 * Collect property values.
	 * 
	 * @param node               the property value node
	 * @param projectInfo        the MicroProfile project information
	 * @param completionSettings the completion settings
	 * @param list               the completion list to fill
	 */
	private static void collectPropertyValueSuggestions(Node node, PropertiesModel model,
			MicroProfileProjectInfo projectInfo, ValuesRulesManager valuesRulesManager,
			MicroProfileCompletionSettings completionSettings, CompletionList list) {

		Node parent = node.getParent();
		if (parent != null && parent.getNodeType() != Node.NodeType.PROPERTY) {
			return;
		}
		Property property = (Property) parent;
		String propertyName = property.getPropertyName();

		ItemMetadata item = MicroProfilePropertiesUtils.getProperty(propertyName, projectInfo);
		if (item != null) {
			Collection<ValueHint> enums = MicroProfilePropertiesUtils.getEnums(item, projectInfo, model,
					valuesRulesManager);
			if (enums != null && !enums.isEmpty()) {
				boolean markdownSupported = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);
				for (ValueHint e : enums) {
					list.getItems()
							.add(getValueCompletionItem(e, item.getConverterKinds(), node, model, markdownSupported));
				}
			}
		}
	}

	/**
	 * Returns the <code>CompletionItem</code> which offers completion for value
	 * completion for <code>value</code> at the start offset of <code>node</code>.
	 * 
	 * @param converterKinds
	 * 
	 * @param value             the value for completion
	 * @param docs              the documentation for completion
	 * @param node              the node where its start offset is where value
	 *                          completion occurs
	 * @param model             the property model
	 * @param markdownSupported true if markdown is supported and false otherwise.
	 * @return the value completion item
	 */
	private static CompletionItem getValueCompletionItem(ValueHint item, List<ConverterKind> converterKinds, Node node,
			PropertiesModel model, boolean markdownSupported) {
		String value = item.getPreferredValue(converterKinds);
		CompletionItem completionItem = new CompletionItem(value);
		completionItem.setKind(CompletionItemKind.Value);

		Range range = null;
		try {
			TextDocument doc = model.getDocument();
			int startOffset;
			if (node.getNodeType() == NodeType.ASSIGN) {
				startOffset = node.getEnd();
			} else {
				startOffset = node.getStart();
			}
			range = doc.lineRangeAt(startOffset);
			range.setStart(doc.positionAt(startOffset));
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In MicroProfileCompletion#getEnumCompletionItem, position error", e);
		}

		TextEdit textEdit = new TextEdit(range, value);
		completionItem.setTextEdit(textEdit);
		completionItem.setDocumentation(DocumentationUtils.getDocumentation(item, markdownSupported));

		return completionItem;
	}

	private static void collectSnippetSuggestions(int completionOffset, Node node, PropertiesModel document,
			MicroProfileProjectInfo projectInfo, MicroProfileCompletionSettings completionSettings,
			TextDocumentSnippetRegistry snippetRegistry, CompletionList list) {
		boolean markdownSupported = completionSettings.isDocumentationFormatSupported(MarkupKind.MARKDOWN);
		snippetRegistry.getCompletionItems(document.getDocument(), completionOffset, markdownSupported, context -> {
			if (context instanceof SnippetContextForProperties) {
				SnippetContextForProperties contextProperties = (SnippetContextForProperties) context;
				return contextProperties.isMatch(projectInfo);
			}
			return false;
		}).forEach(item -> {
			list.getItems().add(item);
		});
	}

	private TextDocumentSnippetRegistry getSnippetRegistry() {
		if (snippetRegistry == null) {
			snippetRegistry = new TextDocumentSnippetRegistry(LanguageId.properties.name()) {

				@Override
				protected String getExpr(TextDocument document, int completionOffset) {
					String expr = super.getExpr(document, completionOffset);
					if (expr.length() > 0 && expr.charAt(0) == '%') {
						return null;
					}
					return expr;
				}
			};
		}
		return snippetRegistry;
	}

}