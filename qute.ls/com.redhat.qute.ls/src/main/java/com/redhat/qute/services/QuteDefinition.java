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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.project.indexing.QuteIndex;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.services.definition.DefinitionRequest;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.QuteSearchUtils;

/**
 * Qute definition support.
 *
 */
class QuteDefinition {

	private static final Logger LOGGER = Logger.getLogger(QuteDefinition.class.getName());

	private static CompletableFuture<List<? extends LocationLink>> NO_DEFINITION = CompletableFuture
			.completedFuture(Collections.emptyList());

	private final JavaDataModelCache javaCache;

	public QuteDefinition(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
	}

	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		try {
			DefinitionRequest definitionRequest = new DefinitionRequest(template, position);
			Node node = definitionRequest.getNode();
			if (node == null) {
				return NO_DEFINITION;
			}
			int offset = definitionRequest.getOffset();
			switch (node.getKind()) {
			case Section:
				// - Start end tag definition
				// - Java data model definition
				return findDefinitionFromSection(offset, (Section) node, template, cancelChecker);
			case ParameterDeclaration:
				// Return Java class definition
				return findDefinitionFromParameterDeclaration(offset, (ParameterDeclaration) node, template);
			case Expression:
				return findDefinitionFromExpression(offset, (Expression) node, template, cancelChecker);
			case ExpressionPart:
				Part part = (Part) node;
				return findDefinitionFromPart(part, template, cancelChecker);
			default:
				// none definitions
				return NO_DEFINITION;
			}

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Failed creating DefinitionRequest", e);
			return NO_DEFINITION;
		}

	}

	/**
	 * Find start end tag definition.
	 * 
	 * @param offset
	 * 
	 * @param document
	 * @param template
	 * 
	 * @param request  the definition request
	 * @return
	 * @throws BadLocationException
	 */
	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromSection(int offset, Section sectionTag,
			Template template, CancelChecker cancelChecker) throws BadLocationException {
		List<LocationLink> locations = new ArrayList<>();
		// Try start / end tag definition to jump from start|end tag to end|start tag
		if (!findDefinitionFromStartEndTagSection(offset, sectionTag, template, locations)) {
			// Try Java data model definition
			if (sectionTag.isInParameters(offset)) {
				Parameter parameter = sectionTag.getParameterAtOffset(offset);
				if (parameter != null) {
					Expression expression = parameter.getJavaTypeExpression();
					if (expression != null) {
						return findDefinitionFromExpression(offset, expression, template, cancelChecker);
					}
				}
			}
		}
		return CompletableFuture.completedFuture(locations);
	}

	private static boolean findDefinitionFromStartEndTagSection(int offset, Section section, Template template,
			List<LocationLink> locations) {
		if (section.isInStartTagName(offset)) {
			int locationsLength = locations.size();
			if (section.getSectionKind() == SectionKind.CUSTOM) {
				QuteProject project = template.getProject();
				if (project != null) {
					String tagName = section.getTag();
					UserTag userTag = project.findUserTag(tagName);
					if (userTag != null) {
						// 1. Jump to custom user tag file inside src/main/resources/templates/tags
						String userTagUri = userTag.getUri();
						Range targetRange = new Range(new Position(0, 0), new Position(0, 0));
						Range originRange = QutePositionUtility.selectStartTagName(section);
						locations.add(new LocationLink(userTagUri, targetRange, targetRange, originRange));
					} else {
						// 2. Jump to custom tag declared in the the {#insert custom-tag of the included
						// Qute template (by using {#include base).
						Range originRange = null;
						Node parent = section.getParent();
						while (parent != null) {
							if (parent.getKind() == NodeKind.Section) {
								Section parentSection = (Section) parent;
								if (parentSection.getSectionKind() == SectionKind.INCLUDE) {
									IncludeSection includeSection = (IncludeSection) parentSection;
									List<QuteIndex> indexes = project
											.findInsertTagParameter(includeSection.getReferencedTemplateId(), tagName);
									if (indexes != null) {
										for (QuteIndex index : indexes) {
											String linkedTemplateUri = index.getTemplatePath().toUri().toString();
											Range linkedTargetRange = index.getRange();
											if (originRange == null) {
												originRange = QutePositionUtility.selectStartTagName(section);
											}
											locations.add(new LocationLink(linkedTemplateUri, linkedTargetRange,
													linkedTargetRange, originRange));
										}
									}
								}
							}
							parent = parent.getParent();
						}
					}
				}
			}

			if (section.hasEndTag() && locationsLength == locations.size()) {
				// 3. Jump to end tag section
				Range originRange = QutePositionUtility.selectStartTagName(section);
				Range targetRange = QutePositionUtility.selectEndTagName(section);
				locations.add(new LocationLink(template.getUri(), targetRange, targetRange, originRange));
			}

			return true;
		} else if (section.isInEndTagName(offset)) {
			if (section.hasStartTag()) {
				// Jump to start tag section
				Range originRange = QutePositionUtility.selectEndTagName(section);
				Range targetRange = QutePositionUtility.selectStartTagName(section);
				locations.add(new LocationLink(template.getUri(), targetRange, targetRange, originRange));
			}
			return true;
		}
		return false;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameterDeclaration(int offset,
			ParameterDeclaration parameterDeclaration, Template template) {
		String projectUri = template.getProjectUri();
		if (projectUri != null && parameterDeclaration.isInJavaTypeName(offset)) {
			RangeOffset range = parameterDeclaration.getJavaTypeNameRange(offset);
			if (range != null) {
				String className = template.getText(range);
				QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(className, projectUri);
				return findJavaDefinition(params, () -> QutePositionUtility.createRange(range, template));
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findJavaDefinition(QuteJavaDefinitionParams params,
			Supplier<Range> originSelectionRangeProvider) {
		return javaCache.getJavaDefinition(params) //
				.thenApply(location -> {
					if (location != null) {
						String targetUri = location.getUri();
						Range targetRange = location.getRange();
						Range originSelectionRange = originSelectionRangeProvider.get();
						LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange,
								originSelectionRange);
						return Arrays.asList(locationLink);
					}
					return Collections.emptyList();
				});
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromExpression(int offset,
			Expression expression, Template template, CancelChecker cancelChecker) {
		Node expressionNode = expression.findNodeExpressionAt(offset);
		if (expressionNode != null && expressionNode.getKind() == NodeKind.ExpressionPart) {
			Part part = (Part) expressionNode;
			return findDefinitionFromPart(part, template, cancelChecker);
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPart(Part part, Template template,
			CancelChecker cancelChecker) {
		switch (part.getPartKind()) {
		case Object:
			return findDefinitionFromObjectPart((ObjectPart) part, template, cancelChecker);
		case Property:
			return findDefinitionFromPropertyPart((PropertyPart) part, template);
		case Method:
			return findDefinitionFromPropertyPart((MethodPart) part, template);
		default:
			return NO_DEFINITION;
		}
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromObjectPart(ObjectPart part,
			Template template, CancelChecker cancelChecker) {
		List<LocationLink> locations = new ArrayList<>();

		QuteSearchUtils.searchDeclaredObject(part, (node, range) -> {
			String targetUri = node.getOwnerTemplate().getUri();
			Range targetRange = range;
			Range originSelectionRange = QutePositionUtility.createRange(part);
			LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange, originSelectionRange);
			locations.add(locationLink);
		}, false, cancelChecker);

		if (!locations.isEmpty()) {
			return CompletableFuture.completedFuture(locations);
		}

		String projectUri = template.getProjectUri();
		if (projectUri != null) {
			ExtendedDataModelParameter parameter = template.getParameterDataModel(part.getPartName()).getNow(null);
			if (parameter != null) {
				QuteJavaDefinitionParams params = parameter.toJavaDefinitionParams(projectUri);
				return findJavaDefinition(params, () -> QutePositionUtility.createRange(part));
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part,
			Template template) {
		String projectUri = template.getProjectUri();
		if (projectUri != null) {
			Parts parts = part.getParent();
			Part previousPart = parts.getPreviousPart(part);
			return javaCache.resolveJavaType(previousPart, projectUri) //
					.thenCompose(previousResolvedType -> {
						if (previousResolvedType != null) {
							if (previousResolvedType.isIterable()) {
								// Expression uses iterable type
								// {@java.util.List<org.acme.Item items>
								// {items.si|ze()}
								// Property, method to find as definition must be done for iterable type (ex :
								// java.util.List>
								String iterableType = previousResolvedType.getIterableType();
								CompletableFuture<ResolvedJavaTypeInfo> iterableResolvedTypeFuture = javaCache
										.resolveJavaType(iterableType, projectUri);
								return iterableResolvedTypeFuture.thenCompose((iterableResolvedType) -> {
									return findDefinitionFromPropertyPart(part, projectUri, iterableResolvedType);
								});
							}
							return findDefinitionFromPropertyPart(part, projectUri, previousResolvedType);
						}
						return NO_DEFINITION;
					});
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part, String projectUri,
			ResolvedJavaTypeInfo previousResolvedType) {
		// The Java class type from the previous part has been resolved, resolve the
		// property
		JavaMemberInfo member = javaCache.findMember(part, previousResolvedType, projectUri);
		String sourceType = member != null ? member.getSourceType() : null;
		if (sourceType == null) {
			return NO_DEFINITION;
		}

		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, projectUri);
		if (member != null && member.getJavaElementKind() == JavaElementKind.METHOD) {
			// Try to find a method definition
			params.setSourceMethod(member.getName());
		} else {
			// Try to find a field definition
			String property = part.getPartName();
			params.setSourceField(property);
		}
		return findJavaDefinition(params, () -> QutePositionUtility.createRange(part));
	}

}
