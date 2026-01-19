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

import static com.redhat.qute.services.QuteCompletableFutures.isValidJavaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaElementInfo;
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
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.extensions.HoverParticipant;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.services.extensions.HoverExtensionProvider;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Qute hover support.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHover {

	private static final Logger LOGGER = Logger.getLogger(QuteHover.class.getName());

	public static final String MARKDOWN_SEPARATOR = "___";

	private static CompletableFuture<Hover> NO_HOVER = CompletableFuture.completedFuture(null);

	private final SnippetRegistryProvider<Snippet> snippetRegistryProvider;

	public QuteHover(SnippetRegistryProvider<Snippet> snippetRegistryProvider) {
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
		QuteProject project = template.getProject();
		if (project == null) {
			return NO_HOVER;
		}
		RangeOffset classNameRange = parameterDeclaration.getJavaTypeNameRange(hoverRequest.getOffset());
		if (classNameRange != null) {
			if (classNameRange != null) {
				String className = template.getText(classNameRange);
				return project.resolveJavaType(className) //
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
		QuteProject project = template.getProject();
		String namespace = part.getNamespace();
		if (namespace != null) {
			// {inject:bea|n}
			return doHoverForExpressionPartWithNamespace(namespace, part, template, project, hoverRequest,
					cancelChecker);
		}
		switch (part.getPartKind()) {
		case Namespace:
			return doHoverForNamespacePart(part, project, hoverRequest, cancelChecker);
		case Object:
			return doHoverForObjectPart(part, project, hoverRequest, cancelChecker);
		case Method:
			return doHoverForMethodInvocation((MethodPart) part, project, hoverRequest, nativeSettings, cancelChecker);
		case Property:
			return doHoverForPropertyPart(part, project, hoverRequest, cancelChecker);
		default:
			return NO_HOVER;
		}
	}

	public CompletableFuture<Hover> doHoverForExpressionPartWithNamespace(String namespace, Part part,
			Template template, QuteProject project, HoverRequest hoverRequest, CancelChecker cancelChecker) {
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			// {data:it|em}
			// check if it is defined in parameter declaration
			ParameterDeclaration parameterDeclaration = template.findInParameterDeclarationByAlias(part.getPartName());
			if (parameterDeclaration != null) {
				return doHoverForJavaType(part, parameterDeclaration.getJavaType(), project, hoverRequest,
						cancelChecker);
			}
			// check if it is defined with @CheckedTemplate
			if (project != null) {
				return template.getParameterDataModel(part.getPartName()) //
						.thenCompose(parameter -> {
							cancelChecker.checkCanceled();
							if (parameter != null) {
								return doHoverForJavaType(part, parameter.getJavaType(), project, hoverRequest,
										cancelChecker);
							}
							return NO_HOVER;
						});
			}
			return NO_HOVER;
		}
		if (project != null) {
			// {inject:be|an}
			return project.findJavaElementWithNamespace(namespace, part.getPartName()) //
					.thenApply(javaElement -> {
						if (javaElement == null) {
							// ex: {m:applica|tion.index.subtitle}
							List<Hover> hovers = new ArrayList<Hover>();
							for (HoverParticipant hoverParticipant : project.getExtensions()) {
								if (hoverParticipant.isEnabled()) {
									hoverParticipant.doHover(part, hovers, cancelChecker);
								}
							}
							return mergeHover(hovers, null);
						}
						if (javaElement instanceof HoverExtensionProvider) {
							return ((HoverExtensionProvider) javaElement).getHover(part, hoverRequest);
						} else if (javaElement instanceof JavaTypeInfo) {
							return doHoverForJavaType(part, (JavaTypeInfo) javaElement, hoverRequest);
						} else if (javaElement instanceof JavaMemberInfo) {
							return doHoverForMember(part, (JavaMemberInfo) javaElement, hoverRequest);
						}
						return null;

					});
		}
		return NO_HOVER;
	}

	private static Hover doHoverForMember(Part part, JavaMemberInfo member, HoverRequest hoverRequest) {
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		MarkupContent content = DocumentationUtils.getDocumentation(member, null, hasMarkdown);
		Range range = QutePositionUtility.createRange(part);
		return new Hover(content, range);
	}

	private CompletableFuture<Hover> doHoverForNamespacePart(Part part, QuteProject project, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		String namespace = part.getPartName();
		return project.getNamespaceResolverInfo(namespace) //
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

	private CompletableFuture<Hover> doHoverForObjectPart(Part part, QuteProject project, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		if (UserTagUtils.isUserTag(hoverRequest.getTemplate())) {
			// It's an user tag
			SectionMetadata specialKey = UserTagUtils.getSpecialKey(part.getPartName());
			if (specialKey != null) {
				if (UserTagUtils.IT_OBJECT_PART_NAME.equals(specialKey.getName())) {
					return project.resolveJavaType(part) //
							.thenApply(resolvedJavaType -> {
								// Try to show the proper type of 'it'
								cancelChecker.checkCanceled();
								String itType = isValidJavaType(resolvedJavaType)
										? JavaElementInfo.getSimpleType(resolvedJavaType.getName())
										: null;
								SectionMetadata javaTypeInfo = itType != null
										? new SectionMetadata(specialKey.getName(), itType, specialKey.getDescription())
										: specialKey;
								boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
								MarkupContent content = DocumentationUtils.getDocumentation(javaTypeInfo, hasMarkdown);
								Range range = QutePositionUtility.createRange(part);
								return new Hover(content, range);
							});

				}
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

		if (project == null) {
			return NO_HOVER;
		}

		// Check if part is a literal (ex: true, null, 123, 'abc', etc)
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			return doHoverForJavaType(part, literalJavaType, project, hoverRequest, cancelChecker);
		}
		return project.resolveJavaType(part) //
				.thenApply(resolvedJavaType -> {
					cancelChecker.checkCanceled();

					if (isValidJavaType(resolvedJavaType)) {
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

	public CompletableFuture<Hover> doHoverForJavaType(Part part, String javaType, QuteProject project,
			HoverRequest hoverRequest, CancelChecker cancelChecker) {
		return project.resolveJavaType(javaType) //
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

	private CompletableFuture<Hover> doHoverForMethodInvocation(MethodPart part, QuteProject project,
			HoverRequest hoverRequest, QuteNativeSettings nativeSettings, CancelChecker cancelChecker) {
		if (project == null) {
			return NO_HOVER;
		}
		Parts parts = part.getParent();
		Part previousPart = parts.getPreviousPart(part);
		return project.resolveJavaType(previousPart) //
				.thenCompose(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					if (resolvedJavaType != null) {
						ResolvedJavaTypeInfo iterableOfResolvedType = resolvedJavaType.isArray() ? null
								: resolvedJavaType;
						return doHoverForMethodInvocation(part, project, resolvedJavaType, iterableOfResolvedType,
								hoverRequest, nativeSettings, cancelChecker);
					}
					return NO_HOVER;
				});
	}

	private CompletableFuture<Hover> doHoverForMethodInvocation(MethodPart part, QuteProject project,
			ResolvedJavaTypeInfo resolvedType, ResolvedJavaTypeInfo iterableOfResolvedType, HoverRequest hoverRequest,
			QuteNativeSettings nativeSettings, CancelChecker cancelChecker) {
		// The Java class type from the previous part had been resolved,
		// resolve the method

		final ResolvedJavaTypeInfo[] parameterTypes = new ResolvedJavaTypeInfo[part.getParameters().size()];
		final CompletableFuture<Void>[] paramResolveFutures = new CompletableFuture[part.getParameters().size()];

		for (int i = 0; i < part.getParameters().size(); i++) {
			final int index = i;
			CompletableFuture<Void> paramResolveFuture = project.resolveJavaType(part.getParameters().get(i)) //
					.thenAccept(resolvedJavaType -> parameterTypes[index] = resolvedJavaType);
			paramResolveFutures[i] = paramResolveFuture;
		}

		return CompletableFuture.allOf(paramResolveFutures) //
				.thenCompose((unused) -> {
					cancelChecker.checkCanceled();
					boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
					JavaMemberResult memberResult = project.findMethod(resolvedType, null, part.getPartName(),
							Arrays.asList(parameterTypes), nativeSettings.isEnabled());
					if (memberResult == null || !memberResult.isMatchParameters() || memberResult.getMember() == null) {
						// check if it's a value resolver
						MethodValueResolver member = project.findValueResolver(resolvedType, part.getPartName());
						if (member == null) {
							return NO_HOVER;
						}
						MarkupContent content = DocumentationUtils.getDocumentation(member, iterableOfResolvedType,
								hasMarkdown);
						Range range = QutePositionUtility.createRange(part);
						return CompletableFuture.completedFuture(new Hover(content, range));
					}

					if (memberResult.getMember().shouldLoadDocumentation() && resolvedType != null) {
						CompletableFuture<Void> javadocFetchFuture = project
								.getJavadoc(memberResult.getMember(), resolvedType, hasMarkdown) //
								.thenAccept(documentation -> memberResult.getMember()
										.setDocumentation(documentation == null ? "" : documentation));
						return javadocFetchFuture.thenApply(alsoUnused -> {
							MarkupContent content = DocumentationUtils.getDocumentation(memberResult.getMember(),
									iterableOfResolvedType, hasMarkdown);
							Range range = QutePositionUtility.createRange(part);
							return new Hover(content, range);
						});
					}
					MarkupContent content = DocumentationUtils.getDocumentation(memberResult.getMember(),
							iterableOfResolvedType, hasMarkdown);
					Range range = QutePositionUtility.createRange(part);
					return CompletableFuture.completedFuture(new Hover(content, range));
				});
	}

	private CompletableFuture<Hover> doHoverForPropertyPart(Part part, QuteProject project, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		Part previousPart = parts.getPreviousPart(part);
		return project.resolveJavaType(previousPart) //
				.thenCompose(resolvedJavaType -> {
					cancelChecker.checkCanceled();
					if (isValidJavaType(resolvedJavaType)) {
						// Hover for Java type
						ResolvedJavaTypeInfo iterableOfResolvedType = resolvedJavaType.isArray() ? null
								: resolvedJavaType;
						return doHoverForPropertyPart(part, project, resolvedJavaType, iterableOfResolvedType,
								hoverRequest);
					}
					// ex: {m:applica|tion.index.subtitle}
					List<Hover> hovers = new ArrayList<Hover>();
					for (HoverParticipant hoverParticipant : project.getExtensions()) {
						if (hoverParticipant.isEnabled()) {
							hoverParticipant.doHover(part, hovers, cancelChecker);
						}
					}
					return CompletableFuture.completedFuture(mergeHover(hovers, null));
				});
	}

	private CompletableFuture<Hover> doHoverForPropertyPart(Part part, QuteProject project,
			ResolvedJavaTypeInfo resolvedType, ResolvedJavaTypeInfo iterableOfResolvedType, HoverRequest hoverRequest) {
		// The Java class type from the previous part had been resolved, resolve the
		// property
		if (project == null) {
			return NO_HOVER;
		}
		JavaMemberInfo member = project.findMember(resolvedType, part.getPartName());
		if (member == null) {
			return NO_HOVER;
		}
		if (member instanceof HoverExtensionProvider) {
			// Custom Hover
			Hover customHover = ((HoverExtensionProvider) member).getHover(part, hoverRequest);
			return CompletableFuture.completedFuture(customHover);
		}
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		if (member.shouldLoadDocumentation()) {
			CompletableFuture<Void> fetchDocsFuture = project.getJavadoc(member, resolvedType, hasMarkdown) //
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
		QuteProject project = template.getProject();
		if (project == null) {
			return NO_HOVER;
		}
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
						Part iterablePart = iterableParameter.getJavaTypeExpression().getLastPart();
						return project.resolveJavaType(iterablePart) //
								.thenCompose(resolvedJavaType -> {
									if (resolvedJavaType != null && resolvedJavaType.isIterable()) {
										CompletableFuture<ResolvedJavaTypeInfo> future = resolvedJavaType
												.isTypeResolved()
														? CompletableFuture
																.completedFuture(resolvedJavaType.getResolvedType())
														: project.resolveJavaType(resolvedJavaType.getIterableOf());
										return future //
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

					String literalJavaType = expression.getLiteralJavaType();
					if (literalJavaType != null) {
						return project.resolveJavaType(literalJavaType) //
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
					return project.resolveJavaType(parameter) //
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

	/**
	 * Returns the aggregated LSP hover from the given hover list.
	 * 
	 * @param hovers       the hover list.
	 * @param defaultRange the default range according to the hovered DOM node.
	 * @return the aggregated LSP hover from the given hover list.
	 */
	private static Hover mergeHover(List<Hover> hovers, Range defaultRange) {
		if (hovers.isEmpty()) {
			// no hover
			return null;
		}
		if (hovers.size() == 1) {
			// One hover
			Hover hover = hovers.get(0);
			if (hover.getRange() == null) {
				hover.setRange(defaultRange);
			}
			return hover;
		}
		// Several hovers.

		// Get list of markup content
		List<MarkupContent> contents = hovers.stream() //
				.filter(hover -> hover.getContents() != null && hover.getContents().isRight()
						&& hover.getContents().getRight() != null) //
				.map(hover -> hover.getContents().getRight()).collect(Collectors.toList());

		// Find the best hover range
		Range range = defaultRange;
		for (Hover hover : hovers) {
			if (hover.getRange() != null) {
				if (range == null) {
					range = hover.getRange();
				} else {
					// TODO : compute the best range
				}
			}
		}
		return createHover(contents, range);
	}

	/**
	 * Create the hover from the given markup content list and range.
	 * 
	 * @param values       the list of documentation values
	 * @param defaultRange the default range.
	 * @return the hover from the given markup content list and range.
	 */
	public static Hover createHover(List<MarkupContent> values, Range defaultRange) {
		if (values.isEmpty()) {
			return null;
		}
		if (values.size() == 1) {
			return new Hover(values.get(0), defaultRange);
		}
		// Markup kind
		boolean hasMarkdown = values.stream() //
				.anyMatch(contents -> MarkupKind.MARKDOWN.equals(contents.getKind()));
		String markupKind = hasMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT;
		// Contents
		String content = createContent(values, markupKind);
		// Range
		Range range = defaultRange;
		return new Hover(new MarkupContent(markupKind, content), range);
	}

	/**
	 * Create the content.
	 * 
	 * @param values     the list of documentation values
	 * @param markupKind the markup kind.
	 * @return the content.
	 */
	private static String createContent(List<MarkupContent> values, String markupKind) {
		StringBuilder content = new StringBuilder();
		for (MarkupContent value : values) {
			if (!StringUtils.isEmpty(value.getValue())) {
				if (content.length() > 0) {
					if (markupKind.equals(MarkupKind.MARKDOWN)) {
						content.append(System.lineSeparator());
						content.append(System.lineSeparator());
						content.append(MARKDOWN_SEPARATOR);
					}
					content.append(System.lineSeparator());
					content.append(System.lineSeparator());
				}
				content.append(value.getValue());
			}
		}
		return content.toString();
	}
}