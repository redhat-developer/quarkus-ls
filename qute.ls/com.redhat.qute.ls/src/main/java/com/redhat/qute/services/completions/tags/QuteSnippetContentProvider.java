/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
import java.util.Map;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.DefaultSnippetContentProvider;
import com.redhat.qute.ls.commons.snippets.ISuffixPositionProvider;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Snippet content provider to manage Qute sections. The default snippet
 * generates the full snippet content like:
 * 
 * <code>
 * 	{#if ${1:condition}}
    	$0"
	{/if}
 * </code>
 * 
 * The customized provider can manage the generation of the first snippet body
 * line according some usecase.
 * 
 * Given this template:
 * 
 * <code>
 * 	{#|}
	{/}
 * </code>
 * 
 * When completion is triggered on |, the apply completion should generate only
 * 
 * <code>
 * {#if ${1:condition}}
 * </code>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSnippetContentProvider extends DefaultSnippetContentProvider implements ISuffixPositionProvider {

	private boolean hasParameters;
	private Range replaceRange;
	private Position startTagClosePosition;
	private boolean generateOnlyFirstPartOfSection;

	public QuteSnippetContentProvider(Node node, Section section, int offset, Template template)
			throws BadLocationException {
		if (section != null) {
			// {#f|or} --> the replaceRange should be |{#for|}

			// Compute replace range with the start tag name by including curly (ex :
			// '{#for')
			replaceRange = QutePositionUtility.selectStartTagName(section, true /* include curly */);

			// Is section has parameters?
			hasParameters = !section.getParameters().isEmpty();

			// Position of start section closed
			if (!hasParameters && section.isStartTagClosed()) {
				startTagClosePosition = template.positionAt(section.getStartTagCloseOffset() + 1);
			}
			generateOnlyFirstPartOfSection = section.hasEndTag();
			if (generateOnlyFirstPartOfSection && section.hasEmptyEndTag()) {
				// {#for}{/}
				// Check if there is a parent section which is not cloded, in this case the {/}
				// is mapped with this parent section and
				// we consider that the current section is not closed to generate the full
				// content of the snippet.

				// Given this template:
				// {#for}{#ea|ch}{/}
				// In this case Template AST link the second 'each' with {/} and
				// generateOnlyFirstPartOfSection is set to true.
				// But in this usecase the #each parent section #for is not closed, we force
				// generateOnlyFirstPartOfSection to false
				// to generate the full content of #each
				Section parentSection = section.getParentSection();
				while (parentSection != null) {
					if (!parentSection.hasEndTag()) {
						generateOnlyFirstPartOfSection = false;
						break;
					}
					parentSection = parentSection.getParentSection();
				}
			}
		}

		if (replaceRange == null) {
			// f|or --> the replaceRange should be |for|
			String text = template.getText();
			int endExpr = offset;
			// compute the from for search expression according to the node
			int fromSearchExpr = getExprLimitStart(node, endExpr);
			// compute the start expression
			int startExpr = getExprStart(text, fromSearchExpr, endExpr);
			replaceRange = getReplaceRange(startExpr, endExpr, offset, template);
		}
	}

	@Override
	public Position findSuffixPosition(String suffix) {
		return startTagClosePosition;
	}

	@Override
	public String getInsertText(Snippet snippet, Map<String, String> model, boolean replace, String lineDelimiter,
			String whitespacesIndent) {
		List<String> body = snippet.getBody();
		if (!generateOnlyFirstPartOfSection || body.size() <= 1) {
			// {#for
			// for
			// --> generate the full content of the snippet.
			return super.getInsertText(snippet, model, replace, lineDelimiter, whitespacesIndent);
		}
		// Generate the first line content of the snippet
		if (hasParameters) {
			// Generate {#for
			String sectionTag = snippet.getLabel();
			return ("{#" + sectionTag);
		} else {
			// Generate {#for ${1:item} in ${2:items}}
			String firstLine = body.get(0);
			return super.merge(firstLine, model, replace);
		}
	}

	public Range getReplaceRange() {
		return replaceRange;
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
		return currentNode.getStart();
	}

	private static Range getReplaceRange(int replaceStart, int replaceEnd, int offset, Template template)
			throws BadLocationException {
		if (replaceStart > offset) {
			replaceStart = offset;
		}
		return QutePositionUtility.createRange(replaceStart, replaceEnd, template);
	}
}
