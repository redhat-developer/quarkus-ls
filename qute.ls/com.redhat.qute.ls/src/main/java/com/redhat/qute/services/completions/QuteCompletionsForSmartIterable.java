/*******************************************************************************
* Copyright (c) 2025 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import static com.redhat.qute.ls.commons.snippets.SnippetRegistry.updateInsertTextMode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.services.QuteCompletableFutures;
import com.redhat.qute.utils.Pluralizer;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;

/**
 * Provides smart completions for iterable variables in Qute templates.
 * <p>
 * This class analyzes a Qute template and its data model to collect iterable
 * variables, and generates completion items for the editor that insert
 * <strong>Qute sections</strong> like <code>#for</code> and <code>#each</code>.
 * </p>
 *
 * <p>
 * Behavior details:
 * </p>
 * <ul>
 * <li>For <code>#for</code> sections, the generated item name attempts to be
 * the singular form of the iterable variable name. If singularization fails, it
 * falls back to using the type of the iterable's elements (the "iterableOf"
 * type).</li>
 * <li>For <code>#each</code> sections, the default item name is
 * <code>it</code>.</li>
 * <li>The generated completion respects line indentation, line delimiters,
 * snippet placeholders, and tab stops if supported by the editor.</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * // Suppose the template has an iterable variable "users"
 * {#for user in users}  // singularized from "users"
 *     {user}            // placeholder for the current item
 * {/for}
 *
 * // Using #each
 * {#each users}
 *     {it}               // default item name
 * {/each}
 * </pre>
 */
public class QuteCompletionsForSmartIterable {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletionsForSmartIterable.class.getName());

	/**
	 * Holds the name of an iterable variable and its resolved Java type
	 * information. Used for generating smart completions in Qute templates.
	 */
	private class IterableVariableInfo {
		/** The name of the iterable variable in the template */
		public final String name;
		/** The resolved Java type information of the iterable */
		public final ResolvedJavaTypeInfo javaTypeInfo;

		public IterableVariableInfo(String name, ResolvedJavaTypeInfo javaTypeInfo) {
			this.name = name;
			this.javaTypeInfo = javaTypeInfo;
		}
	}

	/**
	 * Collects smart completion items for iterable variables at a given position in
	 * a Qute template and adds them to the provided set of completion items.
	 *
	 * <p>
	 * The method analyzes parameter declarations and the data model of the template
	 * to detect iterable variables (arrays or collections) and generates completion
	 * items for both <code>#for</code> and <code>#each</code> sections.
	 * </p>
	 *
	 * @param completionRequest the completion request containing the template,
	 *                          offset, and editor capabilities
	 * @param completionItems   the set of completion items to populate
	 */
	public void collectIterableSuggestions(CompletionRequest completionRequest, Set<CompletionItem> completionItems) {
		try {
			Template template = completionRequest.getTemplate();
			int offset = completionRequest.getOffset();

			// Collect iterable names
			Set<IterableVariableInfo> iterableInfos = collectIterableInfos(offset, template);
			if (iterableInfos.isEmpty()) {
				return;
			}

			Range replaceRange = QutePositionUtility.selectJavaNameIdentifier(offset, template);

			int lineNumber = replaceRange.getStart().getLine();
			String lineDelimiter;
			String whitespacesIndent = null;
			if (!completionRequest.isInsertTextModeAdjustIndentationSupported()) {
				LineIndentInfo indentInfo = template.lineIndentInfo(lineNumber);
				lineDelimiter = indentInfo.getLineDelimiter();
				whitespacesIndent = indentInfo.getWhitespacesIndent();
			} else {
				lineDelimiter = template.lineDelimiter(lineNumber);
			}
			InsertTextMode defaultInsertTextMode = completionRequest.getDefaultInsertTextMode();

			for (IterableVariableInfo iterableInfo : iterableInfos) {
				populateCompletionItem("each", iterableInfo, replaceRange, completionRequest, whitespacesIndent,
						lineDelimiter, defaultInsertTextMode, completionItems);
				populateCompletionItem("for", iterableInfo, replaceRange, completionRequest, whitespacesIndent,
						lineDelimiter, defaultInsertTextMode, completionItems);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteCompletions, collectIterableSuggestions position error", e);
		}
	}

	/**
	 * Populates a CompletionItem for the given iterable variable and section type,
	 * constructs the insertion text (including snippet placeholders if supported),
	 * and adds it to the provided set of completion items.
	 *
	 * <p>
	 * For <code>#for</code> sections, the item name is generated by trying to
	 * singularize the iterable variable name. If singularization fails, the element
	 * type of the iterable ("iterableOf") is used. For <code>#each</code>, the item
	 * name defaults to <code>it</code>.
	 * </p>
	 *
	 * @param sectionTag            the section type, either "for" or "each"
	 * @param iterableInfo          information about the iterable variable
	 * @param replaceRange          the text range in the editor to replace with the
	 *                              generated section
	 * @param completionRequest     the completion request context
	 * @param whitespacesIndent     the indentation to apply for inserted lines, may
	 *                              be null
	 * @param lineDelimiter         the line delimiter to use (e.g., "\n" or "\r\n")
	 * @param defaultInsertTextMode the default insert text mode (adjusts snippet
	 *                              behavior)
	 * @param completionItems       the set of completion items to populate
	 */
	private void populateCompletionItem(String sectionTag, IterableVariableInfo iterableInfo, Range replaceRange,
			CompletionRequest completionRequest, String whitespacesIndent, String lineDelimiter,
			InsertTextMode defaultInsertTextMode, Set<CompletionItem> completionItems) {

		boolean isForTag = "for".equals(sectionTag);
		String itemName = generateItemName(iterableInfo);

		// Build the insertion text
		StringBuilder insertTextBuilder = new StringBuilder();
		appendSectionStart(insertTextBuilder, sectionTag, iterableInfo.name, isForTag, itemName, completionRequest);
		appendContentBlock(insertTextBuilder, isForTag, itemName, whitespacesIndent, lineDelimiter, completionRequest);
		appendSectionEnd(insertTextBuilder, sectionTag, lineDelimiter);

		// Create and configure completion item
		CompletionItem item = new CompletionItem();
		item.setKind(CompletionItemKind.Snippet);
		item.setLabel("#" + sectionTag + " " + iterableInfo.name);
		item.setFilterText(iterableInfo.name);
		item.setInsertTextFormat(completionRequest.isCompletionSnippetsSupported() ? InsertTextFormat.Snippet
				: InsertTextFormat.PlainText);
		updateInsertTextMode(item, whitespacesIndent, defaultInsertTextMode);
		item.setTextEdit(Either.forLeft(new TextEdit(replaceRange, insertTextBuilder.toString())));

		completionItems.add(item);
	}

	/**
	 * Appends the starting part of a Qute section to the insert text.
	 *
	 * <p>
	 * Example for a "for" section:
	 * </p>
	 * 
	 * <pre>
	 * {#for user in users}
	 * </pre>
	 *
	 * @param sb           the string builder for the insert text
	 * @param sectionTag   the section type ("for" or "each")
	 * @param iterableName the name of the iterable variable
	 * @param isForTag     whether this is a "for" section
	 * @param itemName     the singular item name for the current element
	 * @param request      the completion request context
	 */
	private void appendSectionStart(StringBuilder sb, String sectionTag, String iterableName, boolean isForTag,
			String itemName, CompletionRequest request) {
		sb.append("{#").append(sectionTag).append(" ");
		if (isForTag && request.isCompletionSnippetsSupported()) {
			SnippetsBuilder.placeholders(1, itemName, sb);
			sb.append(" in ");
		} else if (isForTag) {
			sb.append(itemName).append(" in ");
		}
		sb.append(iterableName).append("}");
	}

	/**
	 * Appends the content block of a Qute section, including indentation, tab
	 * stops, and the placeholder for the current item.
	 *
	 * <p>
	 * Example for a "for" section with snippet support:
	 * </p>
	 * 
	 * <pre>
	 * { user } // user is the singular item name
	 * </pre>
	 *
	 * <p>
	 * For #each, the placeholder will be {it}.
	 * </p>
	 *
	 * @param sb                the string builder for the insert text
	 * @param isForTag          whether this is a "for" section
	 * @param itemName          the singular item name for the current element
	 * @param whitespacesIndent the indentation to apply, may be null
	 * @param lineDelimiter     the line delimiter to use
	 * @param request           the completion request context
	 */
	private void appendContentBlock(StringBuilder sb, boolean isForTag, String itemName, String whitespacesIndent,
			String lineDelimiter, CompletionRequest request) {
		sb.append(lineDelimiter);
		if (whitespacesIndent != null) {
			sb.append(whitespacesIndent);
		}
		sb.append("\t{");
		if (isForTag && request.isCompletionSnippetsSupported()) {
			SnippetsBuilder.placeholders(1, itemName, sb);
		} else {
			sb.append(isForTag ? itemName : "it");
		}
		sb.append("}");
		if (request.isCompletionSnippetsSupported()) {
			SnippetsBuilder.tabstops(0, sb);
		}
	}

	/**
	 * Appends the closing part of a Qute section to the insert text.
	 *
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * {/for} or {/each}
	 * </pre>
	 *
	 * @param sb            the string builder for the insert text
	 * @param sectionTag    the section type ("for" or "each")
	 * @param lineDelimiter the line delimiter to use
	 */
	private void appendSectionEnd(StringBuilder sb, String sectionTag, String lineDelimiter) {
		sb.append(lineDelimiter).append("{/").append(sectionTag).append("}");
	}

	/**
	 * Collects iterable variables from template parameters and the data model.
	 *
	 * <p>
	 * Only variables that are arrays or implement Iterable are considered.
	 * </p>
	 *
	 * @param offset   the cursor position in the template (not used directly for
	 *                 filtering)
	 * @param template the Qute template being analyzed
	 * @return a set of IterableVariableInfo objects representing iterable variables
	 */
	private Set<IterableVariableInfo> collectIterableInfos(int offset, Template template) {
		Set<IterableVariableInfo> iterableInfos = new HashSet<>();
		QuteProject project = template.getProject();

		// Parameter declarations
		for (Node child : template.getChildren()) {
			if (child.getKind() == NodeKind.ParameterDeclaration) {
				ParameterDeclaration parameterDeclaration = (ParameterDeclaration) child;
				String alias = parameterDeclaration.getAlias();
				if (!StringUtils.isEmpty(alias)) {
					ResolvedJavaTypeInfo javaTypeInfo = project.resolveJavaTypeSync(parameterDeclaration.getJavaType());
					addIterableInfo(alias, javaTypeInfo, iterableInfos);
				}
			}
		}

		// Data model from CheckedTemplate
		ExtendedDataModelTemplate dataModel = project.getDataModelTemplate(template).getNow(null);
		if (dataModel != null) {
			List<ExtendedDataModelParameter> parameters = dataModel.getParameters();
			if (parameters != null && !parameters.isEmpty()) {
				for (ExtendedDataModelParameter parameter : parameters) {
					ResolvedJavaTypeInfo javaTypeInfo = project.resolveJavaTypeSync(parameter.getSourceType());
					addIterableInfo(parameter.getKey(), javaTypeInfo, iterableInfos);
				}
			}
		}

		return iterableInfos;
	}

	/**
	 * Adds an iterable variable to the set if its Java type is an array or
	 * implements Iterable.
	 *
	 * @param iterableName     the name of the iterable variable
	 * @param resolvedJavaType the resolved Java type information
	 * @param iterableInfos    the set to populate with valid iterable variables
	 */
	private void addIterableInfo(String iterableName, ResolvedJavaTypeInfo resolvedJavaType,
			Set<IterableVariableInfo> iterableInfos) {
		if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedJavaType)
				&& (resolvedJavaType.isArray() || resolvedJavaType.isIterable())) {
			iterableInfos.add(new IterableVariableInfo(iterableName, resolvedJavaType));
		}
	}

	/**
	 * Generates the item name for the current element in a Qute section.
	 *
	 * <p>
	 * For #for, attempts to singularize the iterable name; if that fails, uses the
	 * element type of the iterable. For #each, returns "it".
	 * </p>
	 *
	 * @param iterableInfo the iterable variable information
	 * @return the generated item name
	 */
	private static String generateItemName(IterableVariableInfo iterableInfo) {
		String iterableName = iterableInfo.name;
		String item = Pluralizer.PLURALIZER.singular(iterableName);
		if (isValidItemName(item, iterableName)) {
			return item;
		}
		item = generateItemNameFromJavaType(iterableInfo);
		if (isValidItemName(item, iterableName)) {
			return item;
		}
		return "item";
	}

	private static String generateItemNameFromJavaType(IterableVariableInfo iterableInfo) {
		String iterableOf = iterableInfo.javaTypeInfo.getIterableOf();
		if (!StringUtils.isEmpty(iterableOf)) {
			int index = iterableOf.lastIndexOf('.');
			if (index != -1) {
				String name = iterableOf.substring(index + 1);
				if (!StringUtils.isEmpty(name)) {
					char c = Character.toLowerCase(name.charAt(0));
					if (name.length() == 1) {
						return String.valueOf(c);
					}
					return String.valueOf(c) + name.substring(1);
				}
			}
			return iterableOf;
		}
		return null;
	}

	private static boolean isValidItemName(String name, String iterableName) {
		return name != null && !name.isEmpty() && !name.equals(iterableName);
	}

}
