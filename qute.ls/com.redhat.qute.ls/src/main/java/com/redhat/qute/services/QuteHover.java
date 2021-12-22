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

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.SectionMetadata;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;

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

	public QuteHover(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<Hover> doHover(Template template, Position position, SharedSettings settings,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
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
		case ParameterDeclaration:
			ParameterDeclaration parameterDeclaration = (ParameterDeclaration) node;
			return doHoverForParameterDeclaration(parameterDeclaration, template, hoverRequest, cancelChecker);
		case ExpressionPart:
			Part part = (Part) node;
			return doHoverForExpressionPart(part, template, hoverRequest, cancelChecker);
		case Parameter:
			Parameter parameter = (Parameter) node;
			return doHoverForParameter(parameter, template, hoverRequest);
		default:
			return NO_HOVER;
		}
	}

	private CompletableFuture<Hover> doHoverForParameterDeclaration(ParameterDeclaration parameterDeclaration,
			Template template, HoverRequest hoverRequest, CancelChecker cancelChecker) {
		RangeOffset classNameRange = parameterDeclaration.getJavaTypeNameRange(hoverRequest.getOffset());
		if (classNameRange != null) {
			if (classNameRange != null) {
				String className = template.getText(classNameRange);
				return javaCache.resolveJavaType(className, template.getProjectUri()) //
						.thenApply(resolvedType -> {
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
			CancelChecker cancelChecker) {
		String projectUri = template.getProjectUri();
		switch (part.getPartKind()) {
		case Object:
			return doHoverForObjectPart(part, projectUri, hoverRequest);
		case Method:
		case Property:
			return doHoverForPropertyPart(part, projectUri, hoverRequest);
		default:
			return NO_HOVER;
		}
	}

	private CompletableFuture<Hover> doHoverForObjectPart(Part part, String projectUri, HoverRequest hoverRequest) {
		// Check if part is a literal (ex: true, null, 123, 'abc', etc)
		Expression expression = part.getParent().getParent();
		String literalJavaType = expression.getLiteralJavaType();
		if (literalJavaType != null) {
			return javaCache.resolveJavaType(literalJavaType, projectUri) //
					.thenApply(resolvedJavaType -> {
						if (resolvedJavaType != null) {
							boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
							MarkupContent content = DocumentationUtils.getDocumentation(resolvedJavaType, hasMarkdown);
							Range range = QutePositionUtility.createRange(part);
							return new Hover(content, range);
						}
						return null;
					});
		}
		return javaCache.resolveJavaType(part, projectUri) //
				.thenApply(resolvedJavaType -> {
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

	private static SectionMetadata getMetadata(Part part) {
		Section section = part.getParentSection();
		if (section == null) {
			return null;
		}
		return (SectionMetadata) section.getMetadata(part.getPartName());
	}

	private CompletableFuture<Hover> doHoverForPropertyPart(Part part, String projectUri, HoverRequest hoverRequest) {
		Parts parts = part.getParent();
		Part previousPart = parts.getPreviousPart(part);
		return javaCache.resolveJavaType(previousPart, projectUri) //
				.thenCompose(resolvedJavaType -> {
					if (resolvedJavaType != null) {
						if (resolvedJavaType.isIterable()) {
							// Expression uses iterable type
							// {@java.util.List<org.acme.Item items>
							// {items.si|ze()}
							// Property, method to find as hover must be done for iterable type (ex :
							// java.util.List>
							String iterableType = resolvedJavaType.getIterableType();
							CompletableFuture<ResolvedJavaTypeInfo> iterableResolvedTypeFuture = javaCache
									.resolveJavaType(iterableType, projectUri);
							return iterableResolvedTypeFuture.thenApply((iterableResolvedType) -> {
								return doHoverForPropertyPart(part, projectUri, iterableResolvedType, resolvedJavaType,
										hoverRequest);
							});
						}

						Hover hover = doHoverForPropertyPart(part, projectUri, resolvedJavaType, null, hoverRequest);
						return CompletableFuture.completedFuture(hover);
					}
					return NO_HOVER;
				});
	}

	private Hover doHoverForPropertyPart(Part part, String projectUri, ResolvedJavaTypeInfo resolvedType,
			ResolvedJavaTypeInfo iterableOfResolvedType, HoverRequest hoverRequest) {
		// The Java class type from the previous part had been resolved, resolve the
		// property
		JavaMemberInfo member = javaCache.findMember(part, resolvedType, projectUri);
		if (member == null) {
			return null;
		}
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		MarkupContent content = DocumentationUtils.getDocumentation(member, iterableOfResolvedType, hasMarkdown);
		Range range = QutePositionUtility.createRange(part);
		return new Hover(content, range);
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
