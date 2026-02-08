package com.redhat.qute.project.extensions.roq.frontmatter;

import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getDocumentation;
import static com.redhat.qute.services.commands.QuteClientCommandConstants.COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Command;
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
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider.FrontMatterProperty;
import com.redhat.qute.services.QuteCompletions;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.settings.QuteCommandCapabilities;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.StringUtils;

public class YamlFrontMatterCompletion {

	private static final Collection<String> BOOLEAN_VALUES = List.of("false", "true");

	public CompletableFuture<CompletionList> doComplete(YamlDocument document, Template template, int offset,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			QuteCommandCapabilities commandCapabilities, CancelChecker cancelChecker) {
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
			return completionOnPropertyKey(null, offset, existingKeys, document, true, completionSettings,
					commandCapabilities);
		}
		case YamlMapping: {
			Set<String> existingKeys = getExistingKeys(null, document);
			return completionOnPropertyKey(null, offset, existingKeys, document, true, completionSettings,
					commandCapabilities);
		}
		case YamlProperty: {
			YamlProperty property = (YamlProperty) yamlNode;
			char prev = document.getText().charAt(offset - 1);
			if (prev == '\n') {
				Set<String> existingKeys = getExistingKeys(null, document);
				return completionOnPropertyKey(null, offset, existingKeys, document, true, completionSettings,
						commandCapabilities);
			} else if (property.isInKey(offset)) {
				// completion on yaml property key
				Set<String> existingKeys = getExistingKeys(property, document);
				return completionOnPropertyKey(property.getKey(), offset, existingKeys, document,
						property.getColonOffset() == -1, completionSettings, commandCapabilities);
			} else {
				// completion on yaml property value
				if (YamlFrontMatterConfig.isPropertyConfig(property, YamlFrontMatterConfig.LAYOUT_PROPERTY)) {
					// completion on layout value
					return completionOnLayout(property.getValue(), offset, document, template, completionSettings);
				} else if (YamlFrontMatterConfig.isPropertyConfig(property, YamlFrontMatterConfig.PAGINATE_PROPERTY)) {
					// completion on paginate value
					return completionOnPaginate(property.getValue(), offset, document, template, completionSettings);
				}
			}
		}
		case YamlScalar: {
			Set<String> existingKeys = getExistingKeys(yamlNode, document);
			return completionOnPropertyKey(yamlNode, offset, existingKeys, document, true, completionSettings,
					commandCapabilities);
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

	private CompletableFuture<CompletionList> completionOnPropertyKey(YamlNode property, int offset,
			Set<String> existingKeys, YamlDocument document, boolean generateValue,
			QuteCompletionSettings completionSettings, QuteCommandCapabilities commandCapabilities) {
		List<CompletionItem> completionItems = new ArrayList<>();
		CompletionList list = new CompletionList();
		list.setItems(completionItems);

		Range range = createRange(property, offset, document);
		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		boolean hasMarkdown = completionSettings.canSupportMarkupKind(MarkupKind.MARKDOWN);
		boolean canSupportTriggerSuggest = commandCapabilities
				.isCommandSupported(COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST);
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
			Command command = new Command();
			command.setCommand(QuteClientCommandConstants.COMMAND_EDITOR_ACTION_TRIGGET_SUGGEST);
			item.setCommand(command);
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
		return "layout".equals(frontMatterProperty.getName()) || "paginate".equals(frontMatterProperty.getName());
	}

	// Completion on layout value

	private CompletableFuture<CompletionList> completionOnLayout(YamlNode yamlNode, int offset, YamlDocument document,
			Template template, QuteCompletionSettings completionSettings) {
		// Completion on layout: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(yamlNode, offset, document);
			boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();

			Path filePath = FileUtils.createPath(template.getUri());
			roq.collectLayouts(filePath, layout -> {
				CompletionItem item = createCompletionFile(layout, range, snippetsSupported);
				completionItems.add(item);
			});

		}
		list.setItems(new ArrayList<>(completionItems));
		return CompletableFuture.completedFuture(list);
	}

	// Completion on layout value

	private CompletableFuture<CompletionList> completionOnPaginate(YamlNode yamlNode, int offset, YamlDocument document,
			Template template, QuteCompletionSettings completionSettings) {
		// Completion on paginate: |
		Set<CompletionItem> completionItems = new HashSet<>();
		CompletionList list = new CompletionList();

		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Range range = createRange(yamlNode, offset, document);
			boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
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

	private static CompletionItem createCompletionFile(Path file, Range range, boolean snippetsSupported) {
		String fileName = DataModelProject.getUriWithoutExtension(file.getFileName().toString());
		String label = fileName;
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(label);
		item.setKind(CompletionItemKind.File);
		TextEdit textEdit = new TextEdit();
		textEdit.setRange(range);

		item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		textEdit.setNewText(fileName);
		item.setTextEdit(Either.forLeft(textEdit));
		return item;
	}

}
