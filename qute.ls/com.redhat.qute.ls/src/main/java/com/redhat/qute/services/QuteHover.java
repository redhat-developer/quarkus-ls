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

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.CaseOperator;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.JavaMemberResult;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Qute hover support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHover {

	private static final Logger LOGGER = Logger.getLogger(QuteHover.class.getName());

	private static CompletableFuture<Hover> NO_HOVER = CompletableFuture.completedFuture(null);

	private final JavaDataModelCache javaCache;

	private final SnippetRegistryProvider<Snippet> snippetRegistryProvider;

	public QuteHover(JavaDataModelCache javaCache, SnippetRegistryProvider<Snippet> snippetRegistryProvider) {
		this.javaCache = javaCache;
		this.snippetRegistryProvider = snippetRegistryProvider;
	}

	public CompletableFuture<Hover> doHover(Template template, Position position, SharedSettings settings,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		QuteNativeSettings nativeSettings = settings.getNativeSettings();
		HoverRequest hoverRequest = null;
		try {
			hoverRequest = new HoverRequest(template, position, settings);
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating HoverRequest", e);
			return NO_HOVER;
		}
		Node node = hoverRequest.getNode();
		if (node == null) {
			return NO_HOVER;
		}
		switch (node.getKind()) {
		case Section:
			// - Start end tag definition
			Section section = (Section) node;
			return doHoverForSection(section, template, hoverRequest, cancelChecker);
		case ParameterDeclaration:
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			return doHoverForParameterDeclaration(parameterDeclaration, template, hoverRequest, cancelChecker);
		case ExpressionPart:
			Part part = (Part) node;
			return doHoverForExpressionPart(part, template, hoverRequest, nativeSettings, cancelChecker);
		case Parameter:
			Parameter parameter = (Parameter) node;
			return doHoverForParameter(parameter, template, hoverRequest);
		default:
			return NO_HOVER;
		}
	}

	private CompletableFuture<Hover> doHoverForSection(Section section, Template template, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		if (section.hasTag()) {
			// the section defines a tag (e : {#for
			String tagName = section.getTag();
			if (section.getSectionKind() == SectionKind.CUSTOM) {
				// custom tag: search information about user tag
				QuteProject project = template.getProject();
				if (project != null) {
					UserTag userTag = project.findUserTag(tagName);
					if (userTag != null) {
						Range range = createSectionTagRange(section, hoverRequest);
						if (range != null) {
							boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
							MarkupContent content = DocumentationUtils.getDocumentation(userTag, hasMarkdown);
							Hover hover = new Hover(content, range);
							return CompletableFuture.completedFuture(hover);
						}
					}
				}
			} else {
				// core tag like #for, #if, etc: display document hover for the section
				Optional<Snippet> snippetSection = snippetRegistryProvider.getSnippetRegistry() //
						.getSnippets() //
						.stream()//
						.filter(snippet -> tagName.equals(snippet.getLabel())) //
						.findFirst();
				if (snippetSection.isPresent()) {
					Snippet snippet = snippetSection.get();
					Range range = createSectionTagRange(section, hoverRequest);
					if (range != null) {
						boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
						MarkupContent content = DocumentationUtils.getDocumentation(snippet, hasMarkdown);
						Hover hover = new Hover(content, range);
						return CompletableFuture.completedFuture(hover);
					}
				}
			}
		}
		return NO_HOVER;
	}

	private static Range createSectionTagRange(Section section, HoverRequest hoverRequest) {
		int offset = hoverRequest.getOffset();
		Range range = null;
		if (section.isInStartTagName(offset)) {
			range = QutePositionUtility.selectStartTagName(section);
		} else if (section.isInEndTagName(offset)) {
			range = QutePositionUtility.selectEndTagName(section);
		}
		return range;
	}

	private CompletableFuture<Hover> doHoverForParameterDeclaration(ParameterDeclaration parameterDeclaration,
			Template template, HoverRequest hoverRequest, CancelChecker cancelChecker) {
		RangeOffset classNameRange = parameterDeclaration.getJavaTypeNameRange(hoverRequest.getOffset());
		if (classNameRange != null) {
			if (classNameRange != null) {
				String className = template.getText(classNameRange);
				return javaCache.resolveJavaType(className, template.getProjectUri()) //
						.thenApply(resolvedType -> {
							cancelChecker.checkCanceled();
							if (resolvedType != null) {
								boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
								MarkupContent content = DocumentationUtils.getDocumentation(resolvedType, hasMarkdown);
								Range range = QutePositionUtility.createRange(classNameRange, template);
								return new Hover(content, range);
							}
							return null;
						});
			}
		}
		return NO_HOVER;
	}

	private CompletableFuture<Hover> doHoverForExpressionPart(Part part, Template template, HoverRequest hoverRequest,
			QuteNativeSettings nativeSettings, CancelChecker cancelChecker) {
		String projectUri = template.getProjectUri();
		String namespace = part.getNamespace();
		if (namespace != null) {
			// {inject:bea|n}
			return doHoverForExpressionPartWithNamespace(namespace, part, template, projectUri, hoverRequest,
					cancelChecker);
		}
		switch (part.getPartKind()) {
		case Namespace:
			return doHoverForNamespacePart(part, projectUri, hoverRequest, cancelChecker);
		case Object:
			return doHoverForObjectPart(part, projectUri, hoverRequest, cancelChecker);
		case Method:
			return doHoverForMethodInvocation((MethodPart)part, projectUri, hoverRequest, nativeSettings, cancelChecker);
		case Property:
			return doHoverForPropertyPart(part, projectUri, hoverRequest, cancelChecker);
		default:
			return NO_HOVER;
		}
	}

	public CompletableFuture<Hover> doHoverForExpressionPartWithNamespace(String namespace, Part part,
			Template template, String projectUri, HoverRequest hoverRequest, CancelChecker cancelChecker) {
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			// {data:it|em}
			// check if it is defined in parameter declaration
			ParameterDeclaration parameterDeclaration = template.findInParameterDeclarationByAlias(part.getPartName());
			if (parameterDeclaration != null) {
				return doHoverForJavaType(part, parameterDeclaration.getJavaType(), projectUri, hoverRequest,
						cancelChecker);
			}
			// check if it is defined with @CheckedTemplate
			if (projectUri != null) {
				return template.getParameterDataModel(part.getPartName()) //
						.thenCompose(parameter -> {
							cancelChecker.checkCanceled();
							if (parameter != null) {
								return doHoverForJavaType(part, parameter.getJavaType(), projectUri, hoverRequest,
										cancelChecker);
							}
							return NO_HOVER;
						});
			}
			return NO_HOVER;
		}
		if (projectUri != null) {
			// {inject:be|an}
			return javaCache.findJavaElementWithNamespace(namespace, part.getPartName(), projectUri) //
					.thenApply(javaElement -> {
						if (javaElement == null) {
							return null;
						}
						if (javaElement instanceof JavaTypeInfo) {
							return doHoverForJavaType(part, (JavaTypeInfo) javaElement, hoverRequest);
						}
						JavaMemberInfo member = (JavaMemberInfo) javaElement;
						boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
						MarkupContent content = DocumentationUtils.getDocumentation(member, null, hasMarkdown);
						Range range = QutePositionUtility.createRange(part);
						return new Hover(content, range);

					});
		}
		return NO_HOVER;
	}

	private CompletableFuture<Hover> doHoverForNamespacePart(Part part, String projectUri, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		String namespace = part.getPartName();
		return javaCache.getNamespaceResolverInfo(namespace, projectUri) //
				.thenCompose(namespaceInfo -> {
					cancelChecker.checkCanceled();
					if (namespaceInfo != null) {
						boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
						MarkupContent content = DocumentationUtils.getDocumentation(namespace, namespaceInfo,
								hasMarkdown);
						Range range = QutePositionUtility.createRange(part);
						Hover hover = new Hover(content, range);
						return CompletableFuture.completedFuture(hover);
					}
					return NO_HOVER;
				});
	}

	private CompletableFuture<Hover> doHoverForObjectPart(Part part, String projectUri, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		if (UserTagUtils.isUserTag(hoverRequest.getTemplate())) {
			// It's an user tag
			SectionMetadata specialKey = UserTagUtils.getSpecialKey(part.getPartName());
			if (specialKey != null) {
				// its a special key for user tag ({it} or {nested-content), display the special
				// key documentation.
				boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
				MarkupContent content = DocumentationUtils.getDocumentation(specialKey, hasMarkdown);
				Range range = QutePositionUtility.createRange(part);
				Hover hover = new Hover(content, range);
				return CompletableFuture.completedFuture(hover);
			}
		}

		Expression expression = part.getParent().getParent();
		if (CaseSection.isCaseSection(expression.getOwnerSection())) {
			// It's an operator in the #case section
			// Ex: {#case <|= 10}
			CaseSection caseSection = (CaseSection) expression.getOwnerSection();
			Parameter operatorParam = caseSection.getParameterAtOffset(part.getStart());
			if (caseSection.isCaseOperator(operatorParam)) {
				CaseOperator operator = caseSection.getCaseOperator();
				boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
				MarkupContent content = DocumentationUtils.getDocumentation(operator, hasMarkdown);
				Range range = QutePositionUtility.createRange(operatorParam);
				Hover hover = new Hover(content, range);
				return CompletableFuture.completedFuture(hover);
			}
		}

		// Check if part is a literal (ex: true, null, 123, 'abc', etc)
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			return doHoverForJavaType(part, literalJavaType, projectUri, hoverRequest, cancelChecker);
		}
		return javaCache.resolveJavaType(part, projectUri) //
				.thenApply(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					if (resolvedJavaType != null) {
						SectionMetadata metadata = getMetadata(part);
						boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
						MarkupContent content = DocumentationUtils.getDocumentation(resolvedJavaType,
								metadata != null ? metadata.getDescription() : null, hasMarkdown);
						Range range = QutePositionUtility.createRange(part);
						return new Hover(content, range);
					}
					return null;
				});
	}

	public CompletableFuture<Hover> doHoverForJavaType(Part part, String javaType, String projectUri,
			HoverRequest hoverRequest, CancelChecker cancelChecker) {
		return javaCache.resolveJavaType(javaType, projectUri) //
				.thenApply(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					return doHoverForJavaType(part, resolvedJavaType, hoverRequest);
				});
	}

	private static Hover doHoverForJavaType(Part part, JavaTypeInfo javaType, HoverRequest hoverRequest) {
		if (javaType != null) {
			boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
			MarkupContent content = DocumentationUtils.getDocumentation(javaType, hasMarkdown);
			Range range = QutePositionUtility.createRange(part);
			return new Hover(content, range);
		}
		return null;
	}

	private static SectionMetadata getMetadata(Part part) {
		Section section = part.getParentSection();
		if (section == null) {
			return null;
		}
		return (SectionMetadata) section.getMetadata(part.getPartName());
	}

	private CompletableFuture<Hover> doHoverForMethodInvocation(MethodPart part, String projectUri,
			HoverRequest hoverRequest, QuteNativeSettings nativeSettings, CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		Part previousPart = parts.getPreviousPart(part);
		return javaCache.resolveJavaType(previousPart, projectUri) //
				.thenCompose(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					if (resolvedJavaType != null) {
						ResolvedJavaTypeInfo iterableOfResolvedType = resolvedJavaType.isArray() ? null : resolvedJavaType;
						return doHoverForMethodInvocation(part, projectUri, resolvedJavaType, iterableOfResolvedType, hoverRequest, nativeSettings, cancelChecker);
					}
					return NO_HOVER;
				});
	}

	private CompletableFuture<Hover> doHoverForMethodInvocation(MethodPart part, String projectUri,
			ResolvedJavaTypeInfo resolvedType, ResolvedJavaTypeInfo iterableOfResolvedType, HoverRequest hoverRequest,
			QuteNativeSettings nativeSettings, CancelChecker cancelChecker) {
		// The Java class type from the previous part had been resolved,
		// resolve the method

		final ResolvedJavaTypeInfo[] parameterTypes = new ResolvedJavaTypeInfo[part.getParameters().size()];
		final CompletableFuture<Void>[] paramResolveFutures = new CompletableFuture[part.getParameters().size()];

		for (int i = 0; i < part.getParameters().size(); i++) {
			final int index = i;
			CompletableFuture<Void> paramResolveFuture = javaCache
					.resolveJavaType(part.getParameters().get(i), projectUri) //
					.thenAccept(resolvedJavaType -> parameterTypes[index] = resolvedJavaType);
			paramResolveFutures[i] = paramResolveFuture;
		}

		return CompletableFuture.allOf(paramResolveFutures) //
				.thenCompose((unused) -> {
					cancelChecker.checkCanceled();
					boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
					JavaMemberResult memberResult = javaCache.findMethod(resolvedType, part.getPartName(),
							Arrays.asList(parameterTypes), nativeSettings.isEnabled(), projectUri);

					if (memberResult == null || !memberResult.isMatchParameters() || memberResult.getMember() == null) {
						// check if it's a value resolver
						MethodValueResolver member = javaCache.findValueResolver(resolvedType, part.getPartName(),
								projectUri);
						if (member == null) {
							return NO_HOVER;
						}
						MarkupContent content = DocumentationUtils.getDocumentation(member, iterableOfResolvedType,
								hasMarkdown);
						Range range = QutePositionUtility.createRange(part);
						return CompletableFuture.completedFuture(new Hover(content, range));
					}

					if (memberResult.getMember().getDocumentation() == null && resolvedType != null) {
						CompletableFuture<Void> javadocFetchFuture = javaCache
								.getJavadoc(memberResult.getMember(), resolvedType, projectUri, hasMarkdown) //
								.thenAccept(documentation -> memberResult.getMember()
										.setDocumentation(documentation == null ? "" : documentation));
						return javadocFetchFuture.thenApply(alsoUnused -> {
							MarkupContent content = DocumentationUtils.getDocumentation(memberResult.getMember(),
									iterableOfResolvedType,
									hasMarkdown);
							Range range = QutePositionUtility.createRange(part);
							return new Hover(content, range);
						});
					}
					MarkupContent content = DocumentationUtils.getDocumentation(memberResult.getMember(),
							iterableOfResolvedType,
							hasMarkdown);
					Range range = QutePositionUtility.createRange(part);
					return CompletableFuture.completedFuture(new Hover(content, range));
				});
	}

	private CompletableFuture<Hover> doHoverForPropertyPart(Part part, String projectUri, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		Part previousPart = parts.getPreviousPart(part);
		return javaCache.resolveJavaType(previousPart, projectUri) //
				.thenCompose(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					if (resolvedJavaType != null) {
						ResolvedJavaTypeInfo iterableOfResolvedType = resolvedJavaType.isArray() ? null : resolvedJavaType;
						return doHoverForPropertyPart(part, projectUri, resolvedJavaType, iterableOfResolvedType, hoverRequest);
					}
					return NO_HOVER;
				});
	}

	private CompletableFuture<Hover> doHoverForPropertyPart(Part part, String projectUri,
			ResolvedJavaTypeInfo resolvedType,
			ResolvedJavaTypeInfo iterableOfResolvedType, HoverRequest hoverRequest) {
		// The Java class type from the previous part had been resolved, resolve the
		// property
		JavaMemberInfo member = javaCache.findMember(part, resolvedType, projectUri);
		if (member == null) {
			return NO_HOVER;
		}
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		if (member.getDocumentation() == null) {
			CompletableFuture<Void> fetchDocsFuture = javaCache
					.getJavadoc(member, resolvedType, projectUri, hasMarkdown) //
					.thenAccept(documentation -> member.setDocumentation(documentation == null ? "" : documentation));
			return fetchDocsFuture.thenApply((unusedNull) -> {
				MarkupContent content = DocumentationUtils.getDocumentation(member, iterableOfResolvedType,
						hasMarkdown);
				Range range = QutePositionUtility.createRange(part);
				return new Hover(content, range);
			});
		}
		MarkupContent content = DocumentationUtils.getDocumentation(member, iterableOfResolvedType, hasMarkdown);
		Range range = QutePositionUtility.createRange(part);
		return CompletableFuture.completedFuture(new Hover(content, range));
	}

	private CompletableFuture<Hover> doHoverForParameter(Parameter parameter, Template template,
			HoverRequest hoverRequest) {
		int offset = hoverRequest.getOffset();
		if (parameter.isInName(offset)) {
			// A parameter name is hovered
			if (parameter.getOwnerSection() != null && (parameter.getOwnerSection().getSectionKind() == SectionKind.FOR
					|| parameter.getOwnerSection().getSectionKind() == SectionKind.EACH)) {
				// a parameter from #for section is hovered
				LoopSection iterableSection = (LoopSection) parameter.getOwnerSection();
				if (iterableSection.isInAlias(offset)) {
					Parameter iterableParameter = iterableSection.getIterableParameter();
					if (iterableParameter != null) {
						boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
						String projectUri = template.getProjectUri();
						Part iterablePart = iterableParameter.getJavaTypeExpression().getLastPart();
						return javaCache.resolveJavaType(iterablePart, projectUri) //
								.thenCompose(resolvedJavaType -> {
									if (resolvedJavaType != null && resolvedJavaType.isIterable()) {
										return javaCache.resolveJavaType(resolvedJavaType.getIterableOf(), projectUri) //
												.thenApply(resolvedIterableOf -> {
													MarkupContent content = DocumentationUtils
															.getDocumentation(resolvedIterableOf, hasMarkdown);
													Range range = QutePositionUtility.createRange(parameter);
													return new Hover(content, range);
												});
									}
									return NO_HOVER;
								});
					}
				}
			} else {
				// Other parameter

				// Check if part is a literal (ex: true, null, 123, 'abc', etc)
				Expression expression = parameter.getJavaTypeExpression();
				if (expression != null) {
					String projectUri = template.getProjectUri();
					String literalJavaType = expression.getLiteralJavaType();
					if (literalJavaType != null) {
						return javaCache.resolveJavaType(literalJavaType, projectUri) //
								.thenApply(resolvedJavaType -> {
									if (resolvedJavaType != null) {
										boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
										MarkupContent content = DocumentationUtils.getDocumentation(resolvedJavaType,
												hasMarkdown);
										Range range = QutePositionUtility.createRange(parameter);
										return new Hover(content, range);
									}
									return null;
								});
					}
					return javaCache.resolveJavaType(parameter, projectUri) //
							.thenApply(resolvedJavaType -> {
								if (resolvedJavaType != null) {
									boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
									MarkupContent content = DocumentationUtils.getDocumentation(resolvedJavaType,
											hasMarkdown);
									Range range = QutePositionUtility.createRange(parameter);
									return new Hover(content, range);
								}
								return null;
							});
				}
			}
		}
		return NO_HOVER;
	}
}