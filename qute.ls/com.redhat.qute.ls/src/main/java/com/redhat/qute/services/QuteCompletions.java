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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.completions.CompletionItemUnresolvedData;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.completions.QuteCompletionsForExpression;
import com.redhat.qute.services.completions.QuteCompletionsForParameterDeclaration;
import com.redhat.qute.services.completions.tags.QuteCompletionForTagSection;
import com.redhat.qute.services.completions.tags.QuteCompletionsForSnippets;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.SharedSettings;

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

	private final QuteCompletionsForSnippets<Snippet> completionsForSnippets;

	private final QuteCompletionForTagSection completionForTagSection;

	public QuteCompletions(JavaDataModelCache javaCache, SnippetRegistryProvider<Snippet> snippetRegistryProvider) {
		this.completionsForParameterDeclaration = new QuteCompletionsForParameterDeclaration(javaCache);
		this.completionsForSnippets = new QuteCompletionsForSnippets<Snippet>(snippetRegistryProvider);
		this.completionForTagSection = new QuteCompletionForTagSection(completionsForSnippets);
		this.completionForExpression = new QuteCompletionsForExpression(completionForTagSection, javaCache);
	}

	/**
	 * Returns completion list for the given position
	 *
	 * @param template             the Qute template
	 * @param position             the position where completion was triggered
	 * @param completionSettings   the completion settings.
	 * @param formattingSettings   the formatting settings.
	 * @param nativeImagesSettings the native image settings.
	 * @param cancelChecker        the cancel checker
	 * @return completion list for the given position
	 */
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			QuteNativeSettings nativeImagesSettings, CancelChecker cancelChecker) {
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
			return completionForExpression.doCompleteExpression(completionRequest, expression, nodeExpression, template,
					offset, completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Text) {
			// The completion is triggered in text node (before node)
			Section parent = node.getParentSection();
			if (parent != null && (parent.isInEndTagName(offset))) {
				// The completion is triggered inside end tag
				return EMPTY_FUTURE_COMPLETION;
			}
			// The completion is triggered in text node

			// Check if completion is triggered after a start bracket character and if it's
			// a valid expression
			int nbBrackets = 0;
			int bracketOffset = offset - 1;
			char previousChar = text.charAt(bracketOffset);
			if (previousChar == '#') {
				// {#
				bracketOffset--;
			}
			while (bracketOffset >= 0 && text.charAt(bracketOffset) == '{') {
				bracketOffset--;
				nbBrackets++;
			}
			if (nbBrackets > 0) {
				// The completion is triggered after bracket character
				// {| --> valid expression
				// {{| --> invalid expression
				// {{{| --> valid expression

				// or after an hash
				// {#| --> valid section
				// {{#| --> invalid section
				// {{{#| --> valid section

				if (nbBrackets % 2 != 0) {
					// The completion is triggered in text node after bracket '{' character
					return completionForExpression.doCompleteExpression(completionRequest, null, node, template, offset,
							completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
				}
				return EMPTY_FUTURE_COMPLETION;
			}
		} else if (node.getKind() == NodeKind.ParameterDeclaration) {
			return completionsForParameterDeclaration.doCollectJavaClassesSuggestions((ParameterDeclaration) node,
					template, offset, completionSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Section) {
			// {#|}
			return completionForTagSection.doCompleteTagSection(completionRequest, completionSettings,
					formattingSettings, cancelChecker);
		} else if (node.getKind() == NodeKind.Parameter) {
			Parameter parameter = (Parameter) node;
			if (isCompletionAllowed(parameter, offset)) {
				// {# let name=|
				return completionForExpression.doCompleteExpression(completionRequest, null, null, template, offset,
						completionSettings, formattingSettings, nativeImagesSettings, cancelChecker);
			}
		}
		return collectSnippetSuggestions(completionRequest);
	}

	/**
	 * Returns true if completion is allowed at the current offset and section.
	 *
	 * @param parameter the parameter.
	 * @param offset    the offset.
	 *
	 * @return true if completion is allowed at the current offset and section.
	 */
	public boolean isCompletionAllowed(Parameter parameter, int offset) {
		if (Section.isCaseSection(parameter.getOwnerSection())) {
			// {#case O|FF}
			return true;
		}
		if (parameter.isAfterAssign(offset)) {
			// {#let name=va|lue}
			return true;
		}
		return false;
	}

	private CompletableFuture<CompletionList> collectSnippetSuggestions(CompletionRequest completionRequest) {
		Template template = completionRequest.getTemplate();
		QuteProject project = template.getProject();
		Set<CompletionItem> completionItems = new HashSet<>();
		if (project != null) {
			project.collectUserTagSuggestions(completionRequest, "", null, completionItems);
		}
		completionsForSnippets.collectSnippetSuggestions(completionRequest, "", null, completionItems);
		CompletionList list = new CompletionList();
		list.setItems(completionItems.stream().collect(Collectors.toList()));
		return CompletableFuture.completedFuture(list);
	}

	public CompletionItem resolveCompletionItem(CompletionItem unresolved,
			CompletionItemUnresolvedData data, Template template,
			SharedSettings sharedSettings, CancelChecker cancelChecker) {
		switch (data.getResolverKind()) {
			case UpdateOrphanEndTagSection:
				return completionsForSnippets.resolveCompletionItem(unresolved, data, template, sharedSettings,
						cancelChecker);
		}
		return null;
	}

}
