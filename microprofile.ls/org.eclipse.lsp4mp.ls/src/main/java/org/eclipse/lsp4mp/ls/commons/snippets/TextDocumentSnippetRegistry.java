/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.commons.snippets;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;

/**
 * Snippet registry which works with {@link TextDocument}.
 * 
 * @author Angelo ZERR
 *
 */
public class TextDocumentSnippetRegistry extends SnippetRegistry {

	private static final Logger LOGGER = Logger.getLogger(TextDocumentSnippetRegistry.class.getName());

	public TextDocumentSnippetRegistry() {
		super();
	}

	public TextDocumentSnippetRegistry(String languageId) {
		super(languageId);
	}

	/**
	 * Returns the snippet completion items for the given completion offset and
	 * context filter.
	 * 
	 * @param document           the text document.
	 * @param completionOffset   the completion offset.
	 * @param canSupportMarkdown true if markdown is supported to generate
	 *                           documentation and false otherwise.
	 * @param contextFilter      the context filter.
	 * @return the snippet completion items for the given completion offset and
	 *         context filter.
	 */
	public List<CompletionItem> getCompletionItems(TextDocument document, int completionOffset,
			boolean canSupportMarkdown, Predicate<ISnippetContext<?>> contextFilter) {
		try {
			String lineDelimiter = getLineDelimiter(document, completionOffset);
			Range range = getReplaceRange(document, completionOffset);
			return super.getCompletionItem(range, lineDelimiter, canSupportMarkdown, contextFilter);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while computing snippet completion items", e);
			return Collections.emptyList();
		}
	}

	private static String getLineDelimiter(TextDocument document, int completionOffset) throws BadLocationException {
		int lineNumber = document.positionAt(completionOffset).getLine();
		return document.lineDelimiter(lineNumber);
	}

	public Range getReplaceRange(TextDocument document, int completionOffset) throws BadLocationException {
		String expr = getExpr(document, completionOffset);
		if (expr == null) {
			return null;
		}
		int startOffset = completionOffset - expr.length();
		int endOffset = completionOffset;
		return getReplaceRange(startOffset, endOffset, document);
	}

	protected String getExpr(TextDocument document, int completionOffset) {
		return findExprBeforeAt(document.getText(), completionOffset);
	}

	protected Range getReplaceRange(int replaceStart, int replaceEnd, TextDocument document)
			throws BadLocationException {
		return new Range(document.positionAt(replaceStart), document.positionAt(replaceEnd));
	}
}
