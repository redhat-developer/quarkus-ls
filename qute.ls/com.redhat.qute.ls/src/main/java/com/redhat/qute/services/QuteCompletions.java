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
package com.redhat.qute.services;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.completions.QuteCompletionsForExpression;
import com.redhat.qute.services.completions.QuteCompletionsForParameterDeclaration;
import com.redhat.qute.services.completions.QuteCompletionsForSnippets;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

/**
 * The Qute completions
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletions {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletions.class.getName());

	public static final CompletionList EMPTY_COMPLETION = new CompletionList();

	public static final CompletableFuture<CompletionList> EMPTY_FUTURE_COMPLETION = CompletableFuture
			.completedFuture(EMPTY_COMPLETION);

	private final QuteCompletionsForParameterDeclaration completionsForParameterDeclaration;

	private final QuteCompletionsForExpression completionForExpression;

	private final QuteCompletionsForSnippets completionsForSnippets;

	public QuteCompletions(JavaDataModelCache javaCache) {
		this.completionsForParameterDeclaration = new QuteCompletionsForParameterDeclaration(javaCache);
		this.completionForExpression = new QuteCompletionsForExpression(javaCache);
		this.completionsForSnippets = new QuteCompletionsForSnippets();
	}

	/**
	 * Returns completion list for the given position
	 *
	 * @param template           the Qute template
	 * @param position           the position where completion was triggered
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		CompletionRequest completionRequest = null;
		try {
			completionRequest = new CompletionRequest(template, position, completionSettings, formattingSettings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of CompletionRequest failed", e);
			return EMPTY_FUTURE_COMPLETION;
		}
		Node node = completionRequest.getNode();
		if (node == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		String text = template.getText();
		int offset = completionRequest.getOffset();

		if (node.getKind() == NodeKind.Expression || node.getKind() == NodeKind.ExpressionParts
				|| node.getKind() == NodeKind.ExpressionPart) {
			Expression expression = null;
			Node nodeExpression = null;
			if (node.getKind() == NodeKind.Expression) {
				expression = (Expression) node;
			} else if (node.getKind() == NodeKind.ExpressionParts) {
				nodeExpression = node;
				expression = ((Parts) node).getParent();
			} else if (node.getKind() == NodeKind.ExpressionPart) {
				nodeExpression = node;
				expression = ((Part) node).getParent().getParent();
			}
			return completionForExpression.doCompleteExpression(expression, nodeExpression, template, offset,
					completionSettings, formattingSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Text) {
			// The completion is triggered in text node (before node)
			Section parent = node.getParentSection();
			if (parent != null && (parent.isInEndTag(offset))) {
				// The completion is triggered inside end tag
				return EMPTY_FUTURE_COMPLETION;
			}
			// The completion is triggered in text node

			// Check if completion is triggered after a start bracket character and if it's
			// a valid expression
			int nbBrackets = 0;
			int bracketOffset = offset - 1;
			while (bracketOffset >= 0 && text.charAt(bracketOffset) == '{') {
				bracketOffset--;
				nbBrackets++;
			}
			if (nbBrackets > 0) {
				// The completion is triggered after bracket character
				// {| --> valid expression
				// {{| --> invalid expression
				// {{{| --> valid expression
				if (nbBrackets % 2 != 0) {
					// The completion is triggered in text node after bracket '{' character
					return completionForExpression.doCompleteExpression(null, node, template, offset,
							completionSettings, formattingSettings, cancelChecker);
				}
				return EMPTY_FUTURE_COMPLETION;
			}
		} else if (node.getKind() == NodeKind.ParameterDeclaration) {
			return completionsForParameterDeclaration.doCollectJavaClassesSuggestions((ParameterDeclaration) node,
					template, offset, completionSettings);
		}

		return collectSnippetSuggestions(completionRequest);
	}

	private CompletableFuture<CompletionList> collectSnippetSuggestions(CompletionRequest completionRequest) {
		CompletionList list = new CompletionList();
		completionsForSnippets.collectSnippetSuggestions(completionRequest, list);
		return CompletableFuture.completedFuture(list);
	}

}