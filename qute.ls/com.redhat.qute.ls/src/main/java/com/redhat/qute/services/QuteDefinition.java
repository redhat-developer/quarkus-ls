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

import static com.redhat.qute.parser.template.Section.isWhenSection;

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

import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.NamespacePart;
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
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.WhenSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.tags.ObjectPartCollector;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagParameter;
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
					return findDefinitionFromParameterDeclaration(offset, (ParameterDeclaration) node, template,
							cancelChecker);
				case Expression:
					return findDefinitionFromExpression(offset, (Expression) node, template, cancelChecker);
				case ExpressionPart:
					Part part = (Part) node;
					return findDefinitionFromPart(part, template, cancelChecker);
				case Parameter:
					return findDefinitionFromParameter(offset, (Parameter) node, template, cancelChecker);
				default:
					// no definitions
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
									List<Parameter> parameters = project
											.findInsertTagParameter(includeSection.getReferencedTemplateId(), tagName);
									if (parameters != null) {
										for (Parameter index : parameters) {
											String linkedTemplateUri = index.getOwnerTemplate().getUri();
											Range linkedTargetRange = QutePositionUtility.selectParameterName(index);
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
			ParameterDeclaration parameterDeclaration, Template template, CancelChecker cancelChecker) {
		QuteProject project = template.getProject();
		if (project != null && parameterDeclaration.isInJavaTypeName(offset)) {
			RangeOffset range = parameterDeclaration.getJavaTypeNameRange(offset);
			if (range != null) {
				String className = template.getText(range);
				QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(className, project.getUri());
				return findJavaDefinition(params, project, cancelChecker,
						() -> QutePositionUtility.createRange(range, template));
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findJavaDefinition(QuteJavaDefinitionParams params,
			QuteProject project, CancelChecker cancelChecker, Supplier<Range> originSelectionRangeProvider) {
		return project.getProjectRegistry().getJavaDefinition(params) //
				.thenApply(location -> {
					cancelChecker.checkCanceled();
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

		String namespace = part.getNamespace();
		if (namespace != null) {
			// {data:ite|m}
			// {inject:bea|n}
			// {config:booleanP|roperty(propertyName)}
			return findDefinitionFromPartWithNamespace(namespace, part, template, cancelChecker);
		}

		switch (part.getPartKind()) {
			case Object:
				return findDefinitionFromObjectPart((ObjectPart) part, template, cancelChecker);
			case Property:
				return findDefinitionFromPropertyPart((PropertyPart) part, template, cancelChecker);
			case Method:
				return findDefinitionFromPropertyPart((MethodPart) part, template, cancelChecker);
			default:
				return NO_DEFINITION;
		}
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPartWithNamespace(String namespace,
			Part part, Template template, CancelChecker cancelChecker) {
		QuteProject project = template.getProject();
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			// {data:it|em}
			// check if it is defined in parameter declaration
			ParameterDeclaration parameterDeclaration = template.findInParameterDeclarationByAlias(part.getPartName());
			if (parameterDeclaration != null) {
				String targetUri = template.getUri();
				Range targetRange = QutePositionUtility.selectAlias(parameterDeclaration);
				Range originSelectionRange = QutePositionUtility.createRange(part);
				LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange, originSelectionRange);
				return CompletableFuture.completedFuture(Arrays.asList(locationLink));
			}
			// check if it is defined with @CheckedTemplate
			if (project != null) {
				return findDefinitionFromParameterDataModel(part, template, project, cancelChecker);
			}
			return NO_DEFINITION;
		}
		if (project != null) {
			// {inject:bea|n}
			// {config:booleanP|roperty(propertyName)}
			return project.findJavaElementWithNamespace(namespace, part.getPartName()) //
					.thenCompose(member -> {
						cancelChecker.checkCanceled();
						return findDefinitionFromJavaMember(member, part, project, cancelChecker);
					});
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromObjectPart(ObjectPart part,
			Template template, CancelChecker cancelChecker) {
		Parameter parameter = part.getOwnerParameter();
		if (parameter != null && CaseSection.isCaseSection(parameter.getOwnerSection())) {
			return findDefinitionFromParameterCaseSection(parameter, (CaseSection) parameter.getOwnerSection(),
					template, cancelChecker);
		}
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

		QuteProject project = template.getProject();
		if (project != null) {
			// Try to find in parameter data model
			return findDefinitionFromParameterDataModel(part, template, project, cancelChecker);
		}
		return NO_DEFINITION;
	}

	/**
	 * Find parameter definition for Enum in #when section.
	 *
	 * @param parameter
	 * @param caseSection
	 * @param template
	 * @param cancelChecker
	 *
	 * @return parameter definition for Enum in #when section.
	 */
	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameterCaseSection(Parameter parameter,
			CaseSection caseSection, Template template, CancelChecker cancelChecker) {
		Section whenSection = caseSection.getParentSection();
		if (!isWhenSection(whenSection)) {
			return NO_DEFINITION;
		}
		if (((CaseSection) caseSection).isCaseOperator(parameter) && caseSection.getParameters().size() > 1) {
			return NO_DEFINITION;
		}

		// Ex: {#when Machine.status}
		// {#is ON }
		// {#is OF|F}
		// {/when}
		QuteProject project = template.getProject();
		if (project == null) {
			return NO_DEFINITION;
		}

		Parameter value = ((WhenSection) whenSection).getValueParameter();
		if (value != null) {
			ResolvedJavaTypeInfo whenJavaType = project.resolveJavaTypeSync(value);
			if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(whenJavaType) && whenJavaType.isEnum()) {
				JavaMemberInfo member = project.findMember(whenJavaType, parameter.getName());
				if (member != null) {
					QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(member.getSourceType(),
							project.getUri());
					params.setSourceField(member.getName());
					return findJavaDefinition(params, project, cancelChecker,
							() -> QutePositionUtility.selectParameterName(parameter));
				}
			}
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameterDataModel(Part part,
			Template template, QuteProject project, CancelChecker cancelChecker) {
		return template.getParameterDataModel(part.getPartName()) //
				.thenCompose(parameter -> {
					cancelChecker.checkCanceled();
					// Try to find in parameter model
					if (parameter != null) {
						if (parameter.getSourceType() == null) {
							return NO_DEFINITION;
						}
						QuteJavaDefinitionParams params = parameter.toJavaDefinitionParams(project.getUri());
						return findJavaDefinition(params, project, cancelChecker,
								() -> QutePositionUtility.createRange(part));
					}
					// try to find in global variables
					return project.findGlobalVariableJavaElement(part.getPartName()) //
							.thenCompose(member -> {
								cancelChecker.checkCanceled();
								return findDefinitionFromJavaMember(member, part, project, cancelChecker);
							});
				});
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part, Template template,
			CancelChecker cancelChecker) {
		QuteProject project = template.getProject();
		if (project != null) {
			Parts parts = part.getParent();
			Part previousPart = parts.getPreviousPart(part);
			return project.resolveJavaType(previousPart) //
					.thenCompose(previousResolvedType -> {
						cancelChecker.checkCanceled();
						if (previousResolvedType != null) {
							return findDefinitionFromPropertyPart(part, project, previousResolvedType,
									cancelChecker);
						}
						return NO_DEFINITION;
					});
		}
		return NO_DEFINITION;
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromPropertyPart(Part part,
			QuteProject project,
			ResolvedJavaTypeInfo previousResolvedType, CancelChecker cancelChecker) {
		// The Java class type from the previous part has been resolved, resolve the
		// property
		JavaMemberInfo member = project.findMember(previousResolvedType, part.getPartName());
		return findDefinitionFromJavaMember(member, part, project, cancelChecker);
	}

	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromJavaMember(JavaElementInfo member,
			Part part, QuteProject project, CancelChecker cancelChecker) {
		if (member == null) {
			return NO_DEFINITION;
		}

		String sourceType = member.getJavaElementKind() == JavaElementKind.TYPE ? member.getName()
				: ((JavaMemberInfo) member).getSourceType();
		if (sourceType == null) {
			return NO_DEFINITION;
		}

		QuteJavaDefinitionParams params = new QuteJavaDefinitionParams(sourceType, project.getUri());
		if (member != null) {
			switch (member.getJavaElementKind()) {
				case FIELD:
					params.setSourceField(member.getName());
					break;
				case METHOD:
					params.setSourceMethod(member.getName());
					break;
				default:
			}
		}
		return findJavaDefinition(params, project, cancelChecker, () -> QutePositionUtility.createRange(part));
	}

	/**
	 * Find definition from parameter name of user tag section.
	 * 
	 * @param offset        the offset where definition is done.
	 * @param parameter     the parameter where definition is done.
	 * @param template      the template.
	 * @param cancelChecker the cancel checker.
	 * 
	 * @return the defintion of the parameter name and no definition otherwise.
	 */
	private CompletableFuture<List<? extends LocationLink>> findDefinitionFromParameter(int offset, Parameter parameter,
			Template template, CancelChecker cancelChecker) {
		if (!parameter.isInName(offset)) {
			return NO_DEFINITION;
		}
		Section section = parameter.getOwnerSection();
		if (section == null || section.getSectionKind() != SectionKind.CUSTOM) {
			return NO_DEFINITION;
		}
		QuteProject project = template.getProject();
		if (project == null) {
			return NO_DEFINITION;
		}
		UserTag userTag = project.findUserTag(section.getTag());
		if (userTag == null) {
			return NO_DEFINITION;
		}
		UserTagParameter userTagParameter = userTag.findParameter(parameter.getName());
		if (userTagParameter == null) {
			return NO_DEFINITION;
		}
		// Ex: {#input na|me="" /}
		// will open the src/main/resources/templates/tags/input.html and select where
		// {name} is declared
		ObjectPartCollector collector = new ObjectPartCollector(parameter.getName());
		userTag.getTemplate().accept(collector);
		List<LocationLink> locations = new ArrayList<>();
		for (ObjectPart part : collector.getObjectParts()) {
			String targetUri = part.getOwnerTemplate().getUri();
			Range targetRange = QutePositionUtility.createRange(part);
			Range originSelectionRange = QutePositionUtility.selectParameterName(parameter);
			LocationLink locationLink = new LocationLink(targetUri, targetRange, targetRange, originSelectionRange);
			locations.add(locationLink);
		}
		return CompletableFuture.completedFuture(locations);
	}
}