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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.ParameterDeclaration.JavaTypeRangeOffset;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.QuteCompletions;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute completion for classes in parameter declaration.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForParameterDeclaration {

	private final JavaDataModelCache javaCache;

	public QuteCompletionsForParameterDeclaration(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<CompletionList> doCollectJavaClassesSuggestions(ParameterDeclaration parameterDeclaration,
			Template template, int offset, QuteCompletionSettings completionSettings, CancelChecker cancelChecker) {
		String projectUri = template.getProjectUri();
		if (projectUri != null) {
			if (parameterDeclaration.isInJavaTypeName(offset)) {
				// Completion for java types
				JavaTypeRangeOffset rangeOffset = parameterDeclaration.getJavaTypeNameRange(offset);
				if (rangeOffset != null) {
					boolean hasAlias = parameterDeclaration.hasAlias();
					boolean closed = parameterDeclaration.isClosed();
					String patternTypeName = template.getText(rangeOffset.getStart(), offset);
					return collectJavaClassesSuggestions(patternTypeName, hasAlias, closed, rangeOffset, projectUri, template,
							completionSettings, cancelChecker);
				}
			}
		}
		return QuteCompletions.EMPTY_FUTURE_COMPLETION;
	}

	private CompletableFuture<CompletionList> collectJavaClassesSuggestions(String pattern, boolean hasAlias,
			boolean closed, JavaTypeRangeOffset rangeOffset, String projectUri, Template template,
			QuteCompletionSettings completionSettings, CancelChecker cancelChecker) {
		QuteJavaTypesParams params = new QuteJavaTypesParams(pattern, projectUri);
		return javaCache.getJavaTypes(params) //
				.thenApply(result -> {
					cancelChecker.checkCanceled();
					if (result == null) {
						return null;
					}
					CompletionList list = new CompletionList();
					list.setItems(new ArrayList<>());
					Range range = QutePositionUtility.createRange(rangeOffset, template);

					for (JavaTypeInfo typeInfo : result) {
						String fullClassName = typeInfo.getName();
						CompletionItem item = new CompletionItem();
						item.setLabel(typeInfo.getSignature());
						TextEdit textEdit = new TextEdit();
						textEdit.setRange(range);

						StringBuilder insertText = new StringBuilder(fullClassName);
						if (typeInfo.getJavaTypeKind() == JavaTypeKind.Package) {
							item.setKind(CompletionItemKind.Module);
							if (rangeOffset.isInGeneric() && !rangeOffset.isGenericClosed()) {
								insertText.append('>');
							}
						} else {
							item.setKind(typeInfo.getJavaTypeKind() == JavaTypeKind.Interface ? CompletionItemKind.Interface
									: CompletionItemKind.Class);

							int snippetIndex = 1;
							// Generate class
							int index = fullClassName.lastIndexOf('.');
							String typeName = index != -1 ? fullClassName.substring(index + 1, fullClassName.length())
									: fullClassName;
							List<JavaParameterInfo> parameters = typeInfo.getTypeParameters();
							if (!parameters.isEmpty()) {
								insertText.append('<');
								for (int i = 0; i < parameters.size(); i++) {
									if (i > 0) {
										insertText.append(',');
									}
									String parameterType = parameters.get(i).getType();
									if (completionSettings.isCompletionSnippetsSupported()) {
										SnippetsBuilder.placeholders(snippetIndex++, parameterType, insertText);
									} else {
										insertText.append(parameterType);
									}
								}
								insertText.append('>');
							} else if (rangeOffset.isInGeneric() && !rangeOffset.isGenericClosed()) {
								insertText.append('>');
							}

							if (!hasAlias) {
								insertText.append(' ');
							}

							// Generate alias
							String alias = String.valueOf(typeName.charAt(0)).toLowerCase()
									+ typeName.substring(1, typeName.length());
							if (completionSettings.isCompletionSnippetsSupported()) {
								item.setInsertTextFormat(InsertTextFormat.Snippet);
								if (!hasAlias) {
									SnippetsBuilder.placeholders(snippetIndex++, alias, insertText);
									if (!closed) {
										insertText.append("}");
									}
								}
								SnippetsBuilder.tabstops(0, insertText); // $0
							} else {
								item.setInsertTextFormat(InsertTextFormat.PlainText);
								if (!hasAlias) {
									insertText.append(alias);
									if (!closed) {
										insertText.append("}");
									}
								}
							}
						}
						textEdit.setNewText(insertText.toString());
						item.setTextEdit(Either.forLeft(textEdit));
						list.getItems().add(item);
					}
					return list;
				});
	}

}
