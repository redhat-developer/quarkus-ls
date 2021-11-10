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

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.scanner.Scanner;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.scanner.ScannerState;
import com.redhat.qute.parser.template.scanner.TemplateScanner;
import com.redhat.qute.parser.template.scanner.TokenType;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.completions.QuteCompletionsForExpression;
import com.redhat.qute.services.completions.QuteCompletionsForSnippets;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.QutePositionUtility;

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

	private final QuteCompletionsForExpression completionForExpression;

	private final QuteCompletionsForSnippets completionsForSnippets;

	private final JavaDataModelCache javaCache;

	public QuteCompletions(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
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
		CompletionList list = new CompletionList();
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
		} else if (node.getKind() == NodeKind.Text && text.charAt(offset - 1) == '{') {
			return completionForExpression.doCompleteExpression(null, node, template, offset, completionSettings,
					formattingSettings, cancelChecker);
		}

		Scanner<TokenType, ScannerState> scanner = TemplateScanner.createScanner(text, node.getStart());

		TokenType token = scanner.scan();
		while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
			cancelChecker.checkCanceled();
			switch (token) {
			case StartParameterDeclaration:
				if (scanner.getTokenEnd() == offset) {
					// {@|
					int start = offset;
					int end = offset;
					return collectJavaClassesSuggestions(start, end, template, completionSettings);
				}
				break;
			case ParameterDeclaration:
				if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
					// {@I|te
					int start = scanner.getTokenOffset();
					int end = scanner.getTokenEnd();
					return collectJavaClassesSuggestions(start, end, template, completionSettings);
				}
				break;
			default:
			}

			token = scanner.scan();
		}

		completionsForSnippets.collectSnippetSuggestions(completionRequest, list);
		return CompletableFuture.completedFuture(list);
	}

	private CompletableFuture<CompletionList> collectJavaClassesSuggestions(int start, int end, Template template,
			QuteCompletionSettings completionSettings) {
		String projectUri = template.getProjectUri();
		if (projectUri == null) {
			return EMPTY_FUTURE_COMPLETION;
		}
		String pattern = template.getText(start, end);
		QuteJavaTypesParams params = new QuteJavaTypesParams(pattern, projectUri);
		return javaCache.getJavaClasses(params) //
				.thenApply(result -> {
					if (result == null) {
						return null;
					}
					CompletionList list = new CompletionList();
					list.setItems(new ArrayList<>());

					for (JavaTypeInfo javaClassInfo : result) {
						String fullClassName = javaClassInfo.getClassName();
						CompletionItem item = new CompletionItem();
						item.setLabel(fullClassName);
						TextEdit textEdit = new TextEdit();
						Range range = QutePositionUtility.createRange(start, end, template);
						textEdit.setRange(range);

						String parameterDeclaration = fullClassName;
						if (javaClassInfo.isPackage()) {
							item.setKind(CompletionItemKind.Module);
						} else {
							item.setKind(CompletionItemKind.Class);
							int index = fullClassName.lastIndexOf('.');
							String className = index != -1 ? fullClassName.substring(index + 1, fullClassName.length())
									: fullClassName;
							String alias = String.valueOf(className.charAt(0)).toLowerCase()
									+ className.substring(1, className.length());

							StringBuilder insertText = new StringBuilder(fullClassName);
							insertText.append(' ');
							if (completionSettings.isCompletionSnippetsSupported()) {
								item.setInsertTextFormat(InsertTextFormat.Snippet);
								insertText.append("${1:");
								insertText.append(alias);
								insertText.append("}$0");
							} else {
								item.setInsertTextFormat(InsertTextFormat.PlainText);
								insertText.append(alias);
							}
							parameterDeclaration = insertText.toString();

						}
						textEdit.setNewText(parameterDeclaration);
						item.setTextEdit(Either.forLeft(textEdit));
						list.getItems().add(item);
					}
					return list;
				});
	}

}