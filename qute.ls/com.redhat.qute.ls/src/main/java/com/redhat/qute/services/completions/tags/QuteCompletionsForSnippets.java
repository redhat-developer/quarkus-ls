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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.LineIndentInfo;
import com.redhat.qute.ls.commons.snippets.DefaultSnippetContentProvider;
import com.redhat.qute.ls.commons.snippets.ISnippetContentProvider;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.completions.CompletionItemResolverKind;
import com.redhat.qute.services.completions.CompletionItemUnresolvedData;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.snippets.AbstractQuteSnippetContext;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.JSONUtility;
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
	 * @param completionItems   set of completion items to update
	 */
	public void collectSnippetSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			Set<CompletionItem> completionItems) {
		Node node = completionRequest.getNode();
		int offset = completionRequest.getOffset();
		Range replaceRange = null;
		boolean hasOrphanEndTagLocal = false;
		Position startTagClosePositionLocal = null;
		Template template = completionRequest.getTemplate();
		boolean hasParametersLocal = false;

		try {
			if (node != null && node.getKind() == NodeKind.Section) {
				Section section = (Section) node;
				if (section.isInStartTagName(offset)) {
					// Completion is triggered inside a start tag
					// - {#f|or }
					// - {#| }

					// Compute replace range with the start tag name by including curly (ex :
					// '{#for')
					replaceRange = QutePositionUtility.selectStartTagName(section, true /* include curly */);

					// Is section has parameters?
					hasParametersLocal = !section.getParameters().isEmpty();

					// Position of start section closed
					if (!hasParametersLocal && section.isStartTagClosed()) {
						startTagClosePositionLocal = template.positionAt(section.getStartTagCloseOffset() + 1);
					}

					// Get the orphan end section to update with the tag name coming from the
					// completion item
					// - {#for }
					// - {/} // <-- update the end tag section here with additional text edit
					hasOrphanEndTagLocal = section.getOrphanEndSection(offset, section.getTag(), true) != null;
				}
			}
			boolean hasOrphanEndTag = hasOrphanEndTagLocal;
			boolean hasParameters = hasParametersLocal;
			Position startTagClosePosition = startTagClosePositionLocal;
			String text = template.getText();

			int endExpr = offset;
			if (replaceRange == null) {
				// compute the from for search expression according to the node
				int fromSearchExpr = getExprLimitStart(node, endExpr);
				// compute the start expression
				int startExpr = getExprStart(text, fromSearchExpr, endExpr);
				replaceRange = getReplaceRange(startExpr, endExpr, offset, template);
			}
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
					}, (suffix) -> {
						if (startTagClosePosition != null) {
							return startTagClosePosition;
						}
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
					}, suffixToFind, prefixFilter,
					new ISnippetContentProvider() {

						@Override
						public String getInsertText(Snippet snippet, Map<String, String> model, boolean replace,
								String lineDelimiter,
								String whitespacesIndent, CompletionItem item) {
							List<String> body = snippet.getBody();
							if (body != null) {
								if (!hasOrphanEndTag || body.size() <= 1) {
									return DefaultSnippetContentProvider.INSTANCE.getInsertText(snippet, model, replace,
											lineDelimiter, whitespacesIndent, item);
								}

								String firstLine = body.get(0);
								String currentTag = getTag(firstLine);
								if (currentTag == null) {
									return DefaultSnippetContentProvider.INSTANCE.getInsertText(snippet, model, replace,
											lineDelimiter, whitespacesIndent, item);
								}

								item.setData(new CompletionItemUnresolvedData(template.getUri(),
										CompletionItemResolverKind.UpdateOrphanEndTagSection,
										new UpdateOrphanEndTagSectionData(offset, currentTag)));

								if (hasParameters) {
									return ("{#" + currentTag);
								} else {
									return DefaultSnippetContentProvider.merge(firstLine, model, replace);
								}
							}
							return "";
						}
					});

			completionItems.addAll(snippets);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "In QuteCompletions, collectSnippetSuggestions position error", e);
		}
	}

	private static String getTag(String bodyLine) {
		int start = bodyLine.indexOf("{#");
		if (start != -1) {
			int end = bodyLine.indexOf(" ");
			return bodyLine.substring(start + 2, end);
		}
		return null;
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

	public CompletionItem resolveCompletionItem(CompletionItem unresolved,
			CompletionItemUnresolvedData data, Template template, SharedSettings sharedSettings,
			CancelChecker cancelChecker) {
		UpdateOrphanEndTagSectionData updateEndTagData = JSONUtility.toModel(data.getResolverData(),
				UpdateOrphanEndTagSectionData.class);
		int offset = updateEndTagData.getOffset();
		String tag = updateEndTagData.getTag();
		Section section = (Section) template.findNodeAt(offset);

		Section orphanEndSection = section.getOrphanEndSection(offset, section.getTag(), true);
		if (orphanEndSection == null) {
			return null;
		}

		Range orphanEndRange = QutePositionUtility.selectEndTagName(orphanEndSection, false /* exclude the '/' */);
		unresolved.setAdditionalTextEdits(Collections.singletonList(new TextEdit(orphanEndRange, tag)));
		return unresolved;
	}

}
