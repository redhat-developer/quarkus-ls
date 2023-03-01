/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions.tags;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.snippets.AbstractQuteSnippetContext;

public class QuteCompletionsForSnippets<T extends Snippet> {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletionsForSnippets.class.getName());

	private SnippetRegistry<T> snippetRegistry;

	private final boolean loadDefault;

	private final SnippetRegistryProvider<T> snippetRegistryProvider;

	public QuteCompletionsForSnippets(SnippetRegistryProvider<T> snippetRegistryProvider) {
		this(snippetRegistryProvider, false);
	}

	public QuteCompletionsForSnippets() {
		this(true);
	}

	public QuteCompletionsForSnippets(boolean loadDefault) {
		this(null, loadDefault);
	}

	private QuteCompletionsForSnippets(SnippetRegistryProvider<T> snippetRegistryProvider, boolean loadDefault) {
		this.loadDefault = loadDefault;
		this.snippetRegistryProvider = snippetRegistryProvider;
	}

	/**
	 * Collect snippets suggestions.
	 *
	 * @param completionRequest completion request.
	 * @param prefixFilter      prefix filter.
	 * @param suffixToFind      suffix to found to eat it when completion snippet is
	 *                          applied.
	 * @param completionItems   set of completion items to update
	 */
	public void collectSnippetSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			Set<CompletionItem> completionItems) {
		Node node = completionRequest.getNode();
		int offset = completionRequest.getOffset();
		Template template = completionRequest.getTemplate();

		Section section = getCoveredStartSection(node, offset);
		if (section != null && section.hasEndTag() && !section.hasEmptyEndTag()) {
			// - {#f|or }{/for}
			// No completion for section tags
			return;
		}

		try {
			QuteSnippetContentProvider contentProvider = new QuteSnippetContentProvider(node, section, offset, template);
			Range replaceRange = contentProvider.getReplaceRange();
			int lineNumber = replaceRange.getStart().getLine();
			String lineDelimiter = null;
			String whitespacesIndent = null;
			if (!completionRequest.isInsertTextModeAdjustIndentationSupported()) {
				LineIndentInfo indentInfo = template.lineIndentInfo(lineNumber);
				lineDelimiter = indentInfo.getLineDelimiter();
				whitespacesIndent = indentInfo.getWhitespacesIndent();
			} else {
				lineDelimiter = template.lineDelimiter(lineNumber);
			}

			InsertTextMode defaultInsertTextMode = completionRequest.getDefaultInsertTextMode();
			List<CompletionItem> snippets = getSnippetRegistry().getCompletionItems(replaceRange, lineDelimiter,
					whitespacesIndent, defaultInsertTextMode,
					completionRequest.canSupportMarkupKind(MarkupKind.MARKDOWN),
					completionRequest.isCompletionSnippetsSupported(), (context, model) -> {
						if (context instanceof AbstractQuteSnippetContext) {
							return (((AbstractQuteSnippetContext) context).isMatch(completionRequest, model));
						}
						return false;
					}, contentProvider, suffixToFind, prefixFilter,
					contentProvider);

			completionItems.addAll(snippets);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteCompletions, collectSnippetSuggestions position error", e);
		}
	}

	/**
	 * Return the section where completion is triggered in start tag and null
	 * otherwise (ex: {#f|or}.
	 * 
	 * @param node   the node.
	 * @param offset the offset.
	 * 
	 * @return the section where completion is triggered in start tag and null
	 *         otherwise.
	 */
	private static Section getCoveredStartSection(Node node, int offset) {
		if (node == null || node.getKind() != NodeKind.Section) {
			return null;
		}
		Section section = (Section) node;
		if (section.isInStartTagName(offset)) {
			// Completion is triggered inside a start tag
			// - {#f|or }
			// - {#| }
			return section;
		}
		return null;
	}

	protected SnippetRegistry<T> getSnippetRegistry() {
		if (snippetRegistryProvider != null) {
			return snippetRegistryProvider.getSnippetRegistry();
		}
		if (snippetRegistry == null) {
			snippetRegistry = new SnippetRegistry<T>(null, loadDefault);
		}
		return snippetRegistry;
	}
}