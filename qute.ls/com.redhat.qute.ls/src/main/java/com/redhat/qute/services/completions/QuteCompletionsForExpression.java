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

import static com.redhat.qute.services.QuteCompletions.EMPTY_COMPLETION;
import static com.redhat.qute.services.QuteCompletions.EMPTY_FUTURE_COMPLETION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.ls.commons.SnippetsBuilder;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.parser.template.sections.WithSection;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;

/**
 * Qute completion inside Qute expression.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForExpression {

	private final JavaDataModelCache javaCache;

	public QuteCompletionsForExpression(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	/**
	 * Returns the completion result of the given offset inside the given
	 * expression.
	 *
	 * @param expression         the expression where the completion has been
	 *                           triggered and null otherwise.
	 * @param nodeExpression     the node expression where the completion has been
	 *                           triggered and null otherwise.
	 * @param template           the owner template.
	 * @param offset             the offset where the completion has been triggered.
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 *
	 * @param cancelChecker      the cancel checker.
	 *
	 * @return the completion result as future.
	 */
	public CompletableFuture<CompletionList> doCompleteExpression(Expression expression, Node nodeExpression,
			Template template, int offset, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, CancelChecker cancelChecker) {
		if (nodeExpression == null) {
			// ex : { | }
			return doCompleteExpressionForObjectPart(expression, null, offset, template, completionSettings,
					formattingSettings);
		} else if (expression == null) {
			// ex : {|
			return doCompleteExpressionForObjectPart(null, nodeExpression, offset, template, completionSettings,
					formattingSettings);
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionPart) {
			Part part = (Part) nodeExpression;
			switch (part.getPartKind()) {
			case Object:
				// ex : { ite|m }
				return doCompleteExpressionForObjectPart(expression, part, offset, template, completionSettings,
						formattingSettings);
			case Property:
			case Method:
				// ex : { item.n| }
				// ex : { item.n|ame }
				// ex : { item.getN|ame() }
				Parts parts = part.getParent();
				return doCompleteExpressionForMemberPart(part, parts, template, completionSettings, formattingSettings);
			default:
				break;
			}
			return EMPTY_FUTURE_COMPLETION;
		}

		if (nodeExpression.getKind() == NodeKind.ExpressionParts) {
			char previous = template.getText().charAt(offset - 1);
			switch (previous) {
			case ':': {
				// ex : { data:| }
				// ex : { data:|name }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForObjectPart(expression, part, offset, template, completionSettings,
						formattingSettings);
			}
			case '.': {
				// ex : { item.| }
				// ex : { item.|name }
				// ex : { item.|getName() }
				Parts parts = (Parts) nodeExpression;
				Part part = parts.getPartAt(offset + 1);
				return doCompleteExpressionForMemberPart(part, parts, template, completionSettings, formattingSettings);
			}
			}
		}
		return EMPTY_FUTURE_COMPLETION;
	}

	/**
	 * Returns the completion result for Java fields, methods of the Java type
	 * class, interface of the given part <code>part</code>
	 *
	 * @param part               the part.
	 * @param parts              the owner parts.
	 * @param template           the owner template.
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 *
	 * @return the completion list.
	 */
	private CompletableFuture<CompletionList> doCompleteExpressionForMemberPart(Part part, Parts parts,
			Template template, QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings) {
		int start = part != null ? part.getStart() : parts.getEnd();
		int end = part != null ? part.getEnd() : parts.getEnd();
		String projectUri = template.getProjectUri();
		Part previousPart = parts.getPreviousPart(part);
		return javaCache.resolveJavaType(previousPart, projectUri) //
				.thenCompose(resolvedType -> {
					if (resolvedType == null) {
						return EMPTY_FUTURE_COMPLETION;
					}
					if (resolvedType.isIterable()) {
						// Completion for member of the iterable element of the given Java class
						// iterable
						// ex : completion for 'org.acme.Item' iterable element of the
						// 'java.util.List<org.acme.Item>' Java class iterable
						return javaCache.resolveJavaType(resolvedType.getIterableType(), projectUri) //
								.thenApply(resolvedIterableType -> {
									if (resolvedIterableType == null) {
										return EMPTY_COMPLETION;
									}
									return doCompleteForJavaTypeMembers(resolvedIterableType, start, end, template,
											completionSettings, formattingSettings);
								});
					}
					// Completion for member of the given Java class
					// ex : org.acme.Item
					CompletionList list = doCompleteForJavaTypeMembers(resolvedType, start, end, template,
							completionSettings, formattingSettings);
					return CompletableFuture.completedFuture(list);
				});

	}

	/**
	 * Returns the completion result for Java fields, methods of the given Java type
	 * class, interface <code>resolvedType</code>
	 *
	 * @param resolvedType       the Java class, interface.
	 * @param start              the part start index to replace.
	 * @param end                the part end index to replace.
	 * @param template           the owner Qute template.
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 *
	 * @return the completion list.
	 */
	private CompletionList doCompleteForJavaTypeMembers(ResolvedJavaTypeInfo resolvedType, int start, int end,
			Template template, QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings) {
		CompletionList list = new CompletionList();
		list.setItems(new ArrayList<>());
		Range range = QutePositionUtility.createRange(start, end, template);
		String projectUri = template.getProjectUri();

		Set<String> existingProperties = new HashSet<>();
		// Completion for Java fields
		fillCompletionFields(resolvedType, range, projectUri, existingProperties, list);

		// Completion for Java methods
		Set<String> existingMethodSignatures = new HashSet<>();
		fillCompletionMethods(resolvedType, range, projectUri, completionSettings, formattingSettings,
				existingProperties, existingMethodSignatures, list);

		// Completion for virtual methods (from value resolvers)

		// Static value resolvers (orEmpty, etc)
		List<ValueResolver> resolvers = javaCache.getResolversFor(resolvedType);
		for (ValueResolver method : resolvers) {
			fillCompletionMethod(method, range, completionSettings, formattingSettings, list);
		}

		// Dynamic value resolvers (from @TemplateExtension)
		resolvers = javaCache.getTemplateExtensionResolvers(projectUri);
		for (ValueResolver method : resolvers) {
			fillCompletionMethod(method, range, completionSettings, formattingSettings, list);
		}
		return list;
	}

	/**
	 * Fill completion list <code>list</code> with the methods of the given Java
	 * type <code>resolvedType</code>.
	 *
	 * @param resolvedType       the Java type class, interface.
	 * @param range              the range.
	 * @param projectUri         the project Uri
	 * @param existingProperties the existing properties and method, field
	 *                           signature.
	 * @param list               the completion list.
	 */
	private void fillCompletionFields(ResolvedJavaTypeInfo resolvedType, Range range, String projectUri,
			Set<String> existingProperties, CompletionList list) {
		// Fill completion with Java type fields.
		for (JavaFieldInfo field : resolvedType.getFields()) {
			String fieldName = field.getName();
			// It is necessary to check if the name of the given field has already been
			// added in the completion
			// list to avoid duplication of input fields.
			// Ex: to avoid duplicate of foo fields in this case
			// class A extends B {String foo;}
			// class B {String foo;}
			if (!existingProperties.contains(fieldName)) {
				CompletionItem item = new CompletionItem();
				item.setLabel(field.getSimpleSignature());
				item.setFilterText(fieldName);
				item.setKind(CompletionItemKind.Field);
				TextEdit textEdit = new TextEdit();
				textEdit.setRange(range);
				textEdit.setNewText(fieldName);
				item.setTextEdit(Either.forLeft(textEdit));
				list.getItems().add(item);
				existingProperties.add(fieldName);
			}
		}
		// Fill completion with extended Java type fields.
		List<String> extendedTypes = resolvedType.getExtendedTypes();
		if (extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				ResolvedJavaTypeInfo resolvedExtendedType = javaCache.resolveJavaType(extendedType, projectUri)
						.getNow(null);
				if (resolvedExtendedType != null) {
					fillCompletionFields(resolvedExtendedType, range, projectUri, existingProperties, list);
				}
			}
		}
	}

	/**
	 * Fill completion list <code>list</code> with the methods of the given Java
	 * type <code>resolvedType</code>.
	 *
	 * @param resolvedType       the Java type class, interface.
	 * @param range              the range.
	 * @param projectUri         the project Uri
	 * @param completionSettings the completion settings
	 * @param formattingSettings the formatting settings.
	 * @param existingProperties the existing properties and method, field
	 *                           signature.
	 * @param list               the completion list.
	 */
	private void fillCompletionMethods(ResolvedJavaTypeInfo resolvedType, Range range, String projectUri,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<String> existingProperties, Set<String> existingMethodSignatures, CompletionList list) {
		for (JavaMethodInfo method : resolvedType.getMethods()) {
			String methodSignature = method.getSignature();
			if (!existingMethodSignatures.contains(methodSignature)) {
				existingMethodSignatures.add(methodSignature);
				String property = method.getGetterName();
				if (property != null && !existingProperties.contains(property)) {
					// It's a getter method, create a completion item for simple property (value)
					// from the method name (getValue)
					CompletionItem item = new CompletionItem();
					item.setLabel(property + " : " + method.getJavaElementSimpleType());
					item.setFilterText(property);
					item.setKind(CompletionItemKind.Property);
					TextEdit textEdit = new TextEdit();
					textEdit.setRange(range);
					textEdit.setNewText(property);
					item.setTextEdit(Either.forLeft(textEdit));
					list.getItems().add(item);
				}

				// Completion for method name (getValue)
				fillCompletionMethod(method, range, completionSettings, formattingSettings, list);
			}

		}
		List<String> extendedTypes = resolvedType.getExtendedTypes();
		if (extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				ResolvedJavaTypeInfo resolvedExtendedType = javaCache.resolveJavaType(extendedType, projectUri)
						.getNow(null);
				if (resolvedExtendedType != null) {
					fillCompletionMethods(resolvedExtendedType, range, projectUri, completionSettings,
							formattingSettings, existingProperties, existingMethodSignatures, list);
				}
			}
		}
	}

	private static CompletionItem fillCompletionMethod(JavaMethodInfo method, Range range,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings, CompletionList list) {
		String methodSignature = method.getSimpleSignature();
		CompletionItem item = new CompletionItem();
		item.setLabel(methodSignature);
		item.setFilterText(method.getName());
		item.setKind(CompletionItemKind.Method);
		item.setInsertTextFormat(completionSettings.isCompletionSnippetsSupported() ? InsertTextFormat.Snippet
				: InsertTextFormat.PlainText);
		TextEdit textEdit = new TextEdit();
		textEdit.setRange(range);
		textEdit.setNewText(createMethodSnippet(method, completionSettings, formattingSettings));
		item.setTextEdit(Either.forLeft(textEdit));
		list.getItems().add(item);
		return item;
	}

	private static String createMethodSnippet(JavaMethodInfo method, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings) {
		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		String methodName = method.getName();
		StringBuilder snippet = new StringBuilder(methodName);
		if (method.hasParameters()) {
			snippet.append("(");
			for (int i = 0; i < method.getParameters().size(); i++) {
				if (i > 0) {
					snippet.append(", ");
				}
				JavaParameterInfo parameter = method.getParameterAt(i);
				if (snippetsSupported) {
					SnippetsBuilder.placeholders(i + 1, parameter.getName(), snippet);
				} else {
					snippet.append(parameter.getName());
				}
			}
			snippet.append(")");
			if (snippetsSupported) {
				SnippetsBuilder.tabstops(0, snippet);
			}
		}
		return snippet.toString();
	}

	private CompletableFuture<CompletionList> doCompleteExpressionForObjectPart(Expression expression, Node part,
			int offset, Template template, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings) {
		// Completion for root object
		int partStart = part != null && part.getKind() != NodeKind.Text ? part.getStart() : offset;
		int partEnd = part != null && part.getKind() != NodeKind.Text ? part.getEnd() : offset;
		Range range = QutePositionUtility.createRange(partStart, partEnd, template);
		CompletionList list = new CompletionList();

		// Collect alias declared from parameter declaration
		doCompleteExpressionForObjectPartWithParameterAlias(template, range, list);
		// Collect parameters from CheckedTemplate method parameters
		doCompleteExpressionForObjectPartWithCheckedTemplate(template, range, list);
		// Collect declared model inside section, let, etc
		Set<String> existingVars = new HashSet<>();
		doCompleteExpressionForObjectPartWithParentNodes(part, expression != null ? expression : part, range, offset,
				template.getProjectUri(), existingVars, completionSettings, formattingSettings, list);
		// Namespace parts
		doCompleteExpressionForNamespacePart(template, completionSettings, formattingSettings, range, list);

		return CompletableFuture.completedFuture(list);
	}

	private void doCompleteExpressionForNamespacePart(Template template, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, Range range, CompletionList list) {
		List<ValueResolver> namespaceResolvers = javaCache.getNamespaceResolvers(template.getProjectUri());
		for (ValueResolver method : namespaceResolvers) {
			CompletionItem item = fillCompletionMethod(method, range, completionSettings, formattingSettings, list);
			item.setKind(CompletionItemKind.Function);
			// Display namespace resolvers (ex : config:getConfigProperty(...)) after
			// declared variables
			item.setSortText("Zb" + item.getLabel());
		}
	}

	private void doCompleteExpressionForObjectPartWithParentNodes(Node part, Node node, Range range, int offset,
			String projectUri, Set<String> existingVars, QuteCompletionSettings completionSettings,
			QuteFormattingSettings formattingSettings, CompletionList list) {
		Section section = node != null ? node.getParentSection() : null;
		if (section == null) {
			return;
		}
		if (section.getKind() == NodeKind.Section) {
			boolean collect = true;
			if (section.getSectionKind() == SectionKind.FOR || section.getSectionKind() == SectionKind.EACH) {
				LoopSection iterableSection = ((LoopSection) section);
				if (iterableSection.isInElseBlock(offset)) {
					// Completion is triggered after a #else inside a #for, we don't provide
					// completion for metadata or aliases
					collect = false;
				}
			}

			if (collect) {

				// 1) Completion for metadata section
				List<SectionMetadata> metadatas = section.getMetadata();
				for (SectionMetadata metadata : metadatas) {
					String name = metadata.getName();
					if (!existingVars.contains(name)) {
						existingVars.add(name);
						CompletionItem item = new CompletionItem();
						item.setLabel(name);
						item.setKind(CompletionItemKind.Keyword);
						// Display metadata section (ex : count for #each) after declared variables
						item.setSortText("Za" + name);
						TextEdit textEdit = new TextEdit(range, name);
						item.setTextEdit(Either.forLeft(textEdit));
						item.setDetail(metadata.getDescription());
						list.getItems().add(item);
					}
				}

				// 2) Completion for aliases section
				switch (section.getSectionKind()) {
				case EACH:
				case FOR:
					LoopSection iterableSection = ((LoopSection) section);
					// Completion for iterable section like #each, #for
					String alias = iterableSection.getAlias();
					if (!StringUtils.isEmpty(alias)) {
						if (!existingVars.contains(alias)) {
							existingVars.add(alias);
							CompletionItem item = new CompletionItem();
							item.setLabel(alias);
							item.setKind(CompletionItemKind.Reference);
							TextEdit textEdit = new TextEdit(range, alias);
							item.setTextEdit(Either.forLeft(textEdit));
							list.getItems().add(item);
						}
					}
					break;
				case LET:
				case SET:
					// completion for parameters coming from #let, #set
					List<Parameter> parameters = section.getParameters();
					if (parameters != null) {
						for (Parameter parameter : parameters) {
							String parameterName = parameter.getName();
							if (!existingVars.contains(parameterName)) {
								existingVars.add(parameterName);
								CompletionItem item = new CompletionItem();
								item.setLabel(parameterName);
								item.setKind(CompletionItemKind.Reference);
								TextEdit textEdit = new TextEdit(range, parameterName);
								item.setTextEdit(Either.forLeft(textEdit));
								list.getItems().add(item);
							}
						}
					}
					break;
				case WITH:
					// Completion for properties/methods of with object from #with
					Parameter object = ((WithSection) section).getObjectParameter();
					if (object != null) {
						ResolvedJavaTypeInfo withJavaTypeInfo = javaCache.resolveJavaType(object, projectUri)
								.getNow(null);
						if (withJavaTypeInfo != null) {
							fillCompletionFields(withJavaTypeInfo, range, projectUri, existingVars, list);
							fillCompletionMethods(withJavaTypeInfo, range, projectUri, completionSettings,
									formattingSettings, existingVars, new HashSet<>(), list);
						}
					}
					break;
				default:
				}
			}
		}
		doCompleteExpressionForObjectPartWithParentNodes(part, section, range, offset, projectUri, existingVars,
				completionSettings, formattingSettings, list);
	}

	private void doCompleteExpressionForObjectPartWithParameterAlias(Template template, Range range,
			CompletionList list) {
		List<String> aliases = template.getChildren().stream() //
				.filter(n -> n.getKind() == NodeKind.ParameterDeclaration) //
				.map(n -> ((ParameterDeclaration) n).getAlias()) //
				.filter(alias -> alias != null) //
				.collect(Collectors.toList());
		for (String alias : aliases) {
			CompletionItem item = new CompletionItem();
			item.setLabel(alias);
			item.setKind(CompletionItemKind.Reference);
			TextEdit textEdit = new TextEdit(range, alias);
			item.setTextEdit(Either.forLeft(textEdit));
			list.getItems().add(item);
		}
	}

	private void doCompleteExpressionForObjectPartWithCheckedTemplate(Template template, Range range,
			CompletionList list) {
		ExtendedDataModelTemplate dataModel = javaCache.getDataModelTemplate(template).getNow(null);
		if (dataModel == null || dataModel.getParameters() == null) {
			return;
		}
		for (ExtendedDataModelParameter parameter : dataModel.getParameters()) {
			CompletionItem item = new CompletionItem();
			item.setLabel(parameter.getKey());
			item.setKind(CompletionItemKind.Reference);
			TextEdit textEdit = new TextEdit(range, parameter.getKey());
			item.setTextEdit(Either.forLeft(textEdit));
			list.getItems().add(item);
		}
	}
}
