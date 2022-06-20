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
package com.redhat.qute.services.completions;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.snippets.AbstractQuteSnippetContext;
import com.redhat.qute.utils.QutePositionUtility;

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
	 * @param list              completion list to update.
	 */
	public void collectSnippetSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			CompletionList list) {
		Node node = completionRequest.getNode();
		int offset = completionRequest.getOffset();
		Template template = node.getOwnerTemplate();
		String text = template.getText();
		int endExpr = offset;
		// compute the from for search expression according to the node
		int fromSearchExpr = getExprLimitStart(node, endExpr);
		// compute the start expression
		int startExpr = getExprStart(text, fromSearchExpr, endExpr);
		try {
			Range replaceRange = getReplaceRange(startExpr, endExpr, offset, template);
			String lineDelimiter = template.lineDelimiter(replaceRange.getStart().getLine());
			List<CompletionItem> snippets = getSnippetRegistry().getCompletionItems(replaceRange, lineDelimiter,
					completionRequest.canSupportMarkupKind(MarkupKind.MARKDOWN),
					completionRequest.isCompletionSnippetsSupported(), (context, model) -> {
						if (context instanceof AbstractQuteSnippetContext) {
							return (((AbstractQuteSnippetContext) context).isMatch(completionRequest, model));
						}
						return false;
					}, (suffix) -> {
						// Search the suffix from the right of completion offset.
						for (int i = endExpr; i < text.length(); i++) {
							char ch = text.charAt(i);
							if (Character.isWhitespace(ch)) {
								// whitespace, continue to eat character
								continue;
							} else {
								// the current character is not a whitespace, search the suffix index
								Integer eatIndex = getSuffixIndex(text, suffix, i);
								if (eatIndex != null) {
									try {
										return template.positionAt(eatIndex);
									} catch (BadLocationException e) {
										return null;
									}
								}
								return null;
							}
						}
						return null;
					}, suffixToFind, prefixFilter);
			for (CompletionItem completionItem : snippets) {
				list.getItems().add(completionItem);
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteCompletions, collectSnippetSuggestions position error", e);
		}
	}

	private static Integer getSuffixIndex(String text, String suffix, final int initOffset) {
		int offset = initOffset;
		char ch = text.charAt(offset);
		// Try to search the first character which matches the suffix
		Integer suffixIndex = null;
		for (int j = 0; j < suffix.length(); j++) {
			if (suffix.charAt(j) == ch) {
				suffixIndex = j;
				break;
			}
		}
		if (suffixIndex != null) {
			// There is one of character of the suffix
			offset++;
			if (suffixIndex == suffix.length()) {
				// the suffix index is the last character of the suffix
				return offset;
			}
			// Try to eat the most characters of the suffix
			for (; offset < text.length(); offset++) {
				suffixIndex++;
				if (suffixIndex == suffix.length()) {
					// the suffix index is the last character of the suffix
					return offset;
				}
				ch = text.charAt(offset);
				if (suffix.charAt(suffixIndex) != ch) {
					return offset;
				}
			}
			return offset;
		}
		return null;
	}

	/**
	 * Returns the limit start offset of the expression according to the current
	 * node.
	 *
	 * @param currentNode the node.
	 * @param offset      the offset.
	 * @return the limit start offset of the expression according to the current
	 *         node.
	 */
	private static int getExprLimitStart(Node currentNode, int offset) {
		if (currentNode == null) {
			// should never occurs
			return 0;
		}
		// if (currentNode.isText()) {
		return currentNode.getStart();
		// }
	}

	private static Range getReplaceRange(int replaceStart, int replaceEnd, int offset, Template template)
			throws BadLocationException {
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		return QutePositionUtility.createRange(replaceStart, replaceEnd, template);
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

	private static int getExprStart(String value, int from, int to) {
		if (to == 0) {
			return to;
		}
		int index = to - 1;
		while (index > 0) {
			if (Character.isWhitespace(value.charAt(index))) {
				return index + 1;
			}
			if (index <= from) {
				return from;
			}
			index--;
		}
		return index;
	}

}
