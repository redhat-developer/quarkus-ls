/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.roq.frontmatter;

import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getDocumentation;
import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getImageDocumentation;
import static com.redhat.qute.services.commands.QuteClientCommandConstants.COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider;
import com.redhat.qute.services.QuteCompletions;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.utils.StringUtils;

/**
 * Yaml frontmatter completion support.
 */
public class YamlFrontMatterCompletion {

	private static final Collection<String> BOOLEAN_VALUES = List.of("false", "true");

	public CompletableFuture<CompletionList> doComplete(CompletionRequest completionRequest, YamlDocument document,
			Template template, int offset, CancelChecker cancelChecker) {
		YamlNode yamlNode = document.findNodeBefore(offset);
		if (yamlNode.getKind() != YamlNodeKind.YamlProperty && yamlNode.getEnd() < offset) {
			if (yamlNode.getParent() != null && NodeBase.isIncluded(yamlNode.getParent(), offset)) {
				yamlNode = yamlNode.getParent();
			}
		}
		if (!ensureDocumentIsParent(yamlNode)) {
			return QuteCompletions.EMPTY_FUTURE_COMPLETION;
		}
		switch (yamlNode.getKind()) {
		case YamlDocument: {
			// ex: |
			Set<String> existingKeys = getExistingKeys(null, document);
			return completionOnPropertyKey(completionRequest, null, offset, existingKeys, document, true);
		}
		case YamlMapping: {
			Set<String> existingKeys = getExistingKeys(null, document);
			return completionOnPropertyKey(completionRequest, null, offset, existingKeys, document, true);
		}
		case YamlProperty: {
			YamlProperty property = (YamlProperty) yamlNode;
			char prev = document.getText().charAt(offset - 1);
			if (prev == '\n') {
				Set<String> existingKeys = getExistingKeys(null, document);
				return completionOnPropertyKey(completionRequest, null, offset, existingKeys, document, true);
			} else if (property.isInKey(offset)) {
				// completion on yaml property key
				Set<String> existingKeys = getExistingKeys(property, document);
				return completionOnPropertyKey(completionRequest, property.getKey(), offset, existingKeys, document,
						property.getColonOffset() == -1);
			} else {
				// completion on yaml property value
				if (property.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)) {
					// completion on layout value
					return completionOnLayout(completionRequest, property.getValue(), offset, document, template);
				} else if (property.isProperty(FrontMatterProperty.THEME_LAYOUT_PROPERTY)) {
					// completion on theme-layout value
					return completionOnThemeLayout(completionRequest, property.getValue(), offset, document, template);
				} else if (property.isProperty(FrontMatterProperty.PAGINATE_PROPERTY)) {
					// completion on paginate value
					return completionOnPaginate(completionRequest, property.getValue(), offset, document, template);
				} else if (property.isProperty(FrontMatterProperty.IMAGE_PROPERTY)) {
					// completion on image value
					return completionOnImage(completionRequest, property.getValue(), offset, document, template);
				}
			}
		}
		case YamlScalar: {
			Set<String> existingKeys = getExistingKeys(yamlNode, document);
			return completionOnPropertyKey(completionRequest, yamlNode, offset, existingKeys, document, true);
		}
		default:
			return QuteCompletions.EMPTY_FUTURE_COMPLETION;
		}

	}

	private static Set<String> getExistingKeys(YamlNode current, YamlDocument document) {
		Set<String> existingKeys = new HashSet<>();
		if (document.getChildCount() > 0) {
			YamlNode first = document.getChild(0);
			if (first.getKind() == YamlNodeKind.YamlMapping) {
				for (YamlNode child : first.getChildren()) {
					if (child.getKind() == YamlNodeKind.YamlProperty) {
						YamlProperty property = (YamlProperty) child;
						if (current == null || !current.equals(property)) {
							YamlScalar key = property.getKey();
							if (key != null) {
								existingKeys.add(key.getValue());
							}
						}
					}
				}
			}
		}
		return existingKeys;
	}

	private boolean ensureDocumentIsParent(YamlNode yamlNode) {
		switch (yamlNode.getKind()) {
		case YamlDocument:
			return true;
		case YamlMapping:
		case YamlScalar:
			return (yamlNode.getParent() != null && yamlNode.getParent().getKind() == YamlNodeKind.YamlDocument
					|| yamlNode.getParent() != null && yamlNode.getParent().getParent() != null
							&& yamlNode.getParent().getParent().getKind() == YamlNodeKind.YamlDocument);
		case YamlProperty:
			return yamlNode.getParent() != null && yamlNode.getParent().getParent() != null
					&& yamlNode.getParent().getParent().getKind() == YamlNodeKind.YamlDocument;
		default:
			return false;
		}
	}

	// Completion on property key

	private CompletableFuture<CompletionList> completionOnPropertyKey(CompletionRequest completionRequest,
			YamlNode property, int offset, Set<String> existingKeys, YamlDocument document, boolean generateValue) {
		List<CompletionItem> completionItems = new ArrayList<>();
		CompletionList list = new CompletionList();
		list.setItems(completionItems);

		Range range = createRange(property, offset, document);
		boolean snippetsSupported = completionRequest.isCompletionSnippetsSupported();
		boolean hasMarkdown = completionRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		boolean canSupportTriggerSuggest = completionRequest.isCommandSupported(COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST);
		for (FrontMatterProperty propertyConfig : YamlFrontMatterSchemaProvider.getInstance().getProperties()) {
			if (!existingKeys.contains(propertyConfig.getName())) {
				fillCompletion(propertyConfig, range, snippetsSupported, hasMarkdown, generateValue,
						canSupportTriggerSuggest, completionItems);
			}
		}

		return CompletableFuture.completedFuture(list);
	}

	private static CompletionItem fillCompletion(FrontMatterProperty frontMatterProperty, Range range,
			boolean snippetsSupported, boolean hasMarkdown, boolean generateValue, boolean canSupportTriggerSuggest,
			List<CompletionItem> completionItems) {
		String label = frontMatterProperty.getName();
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(label);
		item.setKind(CompletionItemKind.Property);

		// TextEdit
		StringBuilder insertText = new StringBuilder(frontMatterProperty.getName());
		if (generateValue) {
			insertText.append(": ");

			// Value
			String defaultValue = frontMatterProperty.getDefaultValue();
			// Boolean value
			if (frontMatterProperty.isBoolean()) {
				if (snippetsSupported) {
					// Boolean choice
					SnippetsBuilder.choice(1, BOOLEAN_VALUES, insertText);
					SnippetsBuilder.tabstops(0, insertText);
				} else {
					insertText.append(defaultValue != null ? defaultValue : "false");
				}
			} else {
				if (!StringUtils.isEmpty(defaultValue)) {
					insertText.append(defaultValue);
				}
				if (snippetsSupported) {
					SnippetsBuilder.tabstops(0, insertText);
				}
			}

		}

		if (canSupportTriggerSuggest && isRetriggerCompletion(frontMatterProperty)) {
			// Retrigger completion when
			// - layout: property is inserted to open completion for available layouts.
			// - paginate: property is inserted to open completion for available collection.
			item.setCommand(QuteClientCommandConstants.COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST_COMMAND);
		}

		TextEdit textEdit = new TextEdit();
		textEdit.setRange(range);
		textEdit.setNewText(insertText.toString());
		item.setTextEdit(Either.forLeft(textEdit));
		item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		// Documentation
		MarkupContent documentation = getDocumentation(frontMatterProperty, hasMarkdown);
		item.setDocumentation(documentation);
		CompletionItemLabelDetails detail = new CompletionItemLabelDetails();
		detail.setDescription(frontMatterProperty.getType());
		item.setLabelDetails(detail);

		completionItems.add(item);
		return item;
	}

	private static boolean isRetriggerCompletion(FrontMatterProperty frontMatterProperty) {
		return frontMatterProperty.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)
				|| frontMatterProperty.isProperty(FrontMatterProperty.THEME_LAYOUT_PROPERTY)
				|| frontMatterProperty.isProperty(FrontMatterProperty.PAGINATE_PROPERTY)
				|| frontMatterProperty.isProperty(FrontMatterProperty.IMAGE_PROPERTY);
	}

	// Completion on layout value

	private CompletableFuture<CompletionList> completionOnLayout(CompletionRequest completionRequest, YamlNode yamlNode,
			int offset, YamlDocument document, Template template) {
		// Completion on layout: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		Set<String> existingIds = new HashSet<>();
		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(yamlNode, offset, document);
			boolean snippetsSupported = completionRequest.isCompletionSnippetsSupported();

			Path filePath = FileUtils.createPath(template.getUri());
			roq.collectLayouts(filePath, (folder, layout, templateId, binary, origin) -> {
				if (templateId == null) {
					templateId = folder.relativize(layout).toString().replace('\\', '/');
					templateId = DataModelProject.getUriWithoutExtension(templateId);
				}
				if (!existingIds.contains(templateId)) {
					existingIds.add(templateId);
					CompletionItem item = createCompletionFile(templateId, range, snippetsSupported);
					if (origin != null) {
						CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
						labelDetails.setDescription(origin);
						item.setLabelDetails(labelDetails);
					}
					completionItems.add(item);
				}
			});
		}
		list.setItems(new ArrayList<>(completionItems));
		return CompletableFuture.completedFuture(list);
	}

	// Completion on theme-layout value

	private CompletableFuture<CompletionList> completionOnThemeLayout(CompletionRequest completionRequest,
			YamlNode yamlNode, int offset, YamlDocument document, Template template) {
		// Completion on layout: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(yamlNode, offset, document);
			boolean snippetsSupported = completionRequest.isCompletionSnippetsSupported();
			roq.collectThemeLayouts((folder, layout, templateId, binary, origin) -> {
				if (templateId == null) {
					templateId = folder.relativize(layout).toString().replace('\\', '/');
					templateId = DataModelProject.getUriWithoutExtension(templateId);
				}
				CompletionItem item = createCompletionFile(templateId, range, snippetsSupported);
				if (origin != null) {
					CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
					labelDetails.setDescription(origin);
					item.setLabelDetails(labelDetails);
				}
				completionItems.add(item);
			});
		}
		list.setItems(new ArrayList<>(completionItems));
		return CompletableFuture.completedFuture(list);
	}

	// Completion on paginate value

	private CompletableFuture<CompletionList> completionOnPaginate(CompletionRequest completionRequest,
			YamlNode yamlNode, int offset, YamlDocument document, Template template) {
		// Completion on paginate: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(yamlNode, offset, document);
			boolean snippetsSupported = completionRequest.isCompletionSnippetsSupported();
			for (String collectionName : roq.getConfiguredCollections()) {
				CompletionItem item = new CompletionItem();
				item.setLabel(collectionName);
				item.setFilterText(collectionName);
				item.setKind(CompletionItemKind.Module);
				TextEdit textEdit = new TextEdit();
				textEdit.setRange(range);
				item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);
				textEdit.setNewText(collectionName);
				item.setTextEdit(Either.forLeft(textEdit));
				completionItems.add(item);
			}
		}
		list.setItems(new ArrayList<>(completionItems));
		return CompletableFuture.completedFuture(list);
	}

	private Range createRange(YamlNode yamlNode, int offset, YamlDocument document) {
		Range range = null;
		if (yamlNode != null) {
			range = YamlPositionUtility.createRange(yamlNode);
		} else {
			range = YamlPositionUtility.createRange(offset, offset, document);
		}
		return range;
	}

	private static CompletionItem createCompletionFile(String filePath, Range range, boolean snippetsSupported) {

		String label = filePath;
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(label);
		item.setKind(CompletionItemKind.File);
		TextEdit textEdit = new TextEdit();
		textEdit.setRange(range);

		item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		textEdit.setNewText(filePath);
		item.setTextEdit(Either.forLeft(textEdit));
		return item;
	}

	// Completion on image value

	private CompletableFuture<CompletionList> completionOnImage(CompletionRequest completionRequest,
			YamlNode propertyValue, int offset, YamlDocument document, Template template) {
		// Completion on image: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(propertyValue, offset, document);
			boolean snippetsSupported = completionRequest.isCompletionSnippetsSupported();
			boolean hasMarkdown = completionRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);

			Path templatePath = FileUtils.createPath(template.getUri());
			roq.collectImages(templatePath, (folder, image, templateId, binary, origin) -> {
				String imagePath = folder.relativize(image).toString().replace('\\', '/');
				if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
					String path = ((YamlScalar) propertyValue).getValue();
					if (path.length() > 0 && path.charAt(0) == '/') {
						imagePath = '/' + imagePath;
					}
				}
				CompletionItem item = createCompletionFile(imagePath, range, snippetsSupported);

				// Documentation
				MarkupContent documentation = getImageDocumentation(new TemplatePath(image, templateId), hasMarkdown);
				item.setDocumentation(documentation);

				completionItems.add(item);
			});
		}
		list.setItems(new ArrayList<>(completionItems));
		return CompletableFuture.completedFuture(list);
	}
}
