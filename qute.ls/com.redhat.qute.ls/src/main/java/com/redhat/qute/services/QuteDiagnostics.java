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
import static com.redhat.qute.parser.template.Section.isWhenSection;
import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;
import static com.redhat.qute.utils.UserTagUtils.IT_OBJECT_PART_NAME;
import static com.redhat.qute.utils.UserTagUtils.NESTED_CONTENT_OBJECT_PART_NAME;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.jaxrs.JaxRsParamKind;
import com.redhat.qute.commons.jaxrs.RestParam;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.CaseOperator;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.LiteralSupport;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Operator;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.ParameterDeclaration.JavaTypeRangeOffset;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CaseSection;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.JavaMemberResult;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagParameter;
import com.redhat.qute.services.diagnostics.CollectHtmlInputNamesVisitor;
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteDiagnosticsForSyntax;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.JavaTypeFilter.JavaMemberAccessibility;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.utils.QutePositionUtility;
import com.redhat.qute.utils.StringUtils;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Qute diagnostics support.
 *
 */
class QuteDiagnostics {

	private static final Logger LOGGER = Logger.getLogger(QuteDiagnostics.class.getName());

	private static final String FORM_SECTION_TAG = "form";

	private final QuteDiagnosticsForSyntax diagnosticsForSyntax;

	private final QuteProjectRegistry projectRegistry;

	private class ResolutionContext extends HashMap<String, ResolvedJavaTypeInfo> {

		private static final long serialVersionUID = 1L;

		private final ResolutionContext parent;

		private ResolvedJavaTypeInfo withObject;

		private ResolvedJavaTypeInfo whenObject;

		public ResolutionContext() {
			this(null);
		}

		public ResolutionContext(ResolutionContext parent) {
			this.parent = parent;
		}

		public ResolutionContext getParent() {
			return parent;
		}

		public void setWithObject(ResolvedJavaTypeInfo withObject) {
			this.withObject = withObject;
		}

		public void setWhenObject(ResolvedJavaTypeInfo whenObject) {
			this.whenObject = whenObject;
		}

		public JavaMemberInfo findMemberWithObject(String property, QuteProject project) {
			if (withObject != null) {
				JavaMemberInfo member = project.findMember(withObject, property);
				if (member != null) {
					return member;
				}
			}
			// Search in parent context
			ResolutionContext parent = this.parent;
			while (parent != null) {
				JavaMemberInfo member = parent.findMemberWithObject(property, project);
				if (member != null) {
					return member;
				}
				parent = parent.getParent();
			}
			return null;
		}

	}

	public QuteDiagnostics(QuteProjectRegistry projectRegistry) {
		this.projectRegistry = projectRegistry;
		this.diagnosticsForSyntax = new QuteDiagnosticsForSyntax();
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 *
	 * @param template             the Qute template.
	 * @param validationSettings   the validation settings.
	 * @param nativeImagesSettings the native images settings.
	 * @param cancelChecker        the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, QuteValidationSettings validationSettings,
			QuteNativeSettings nativeImagesSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			CancelChecker cancelChecker) {
		cancelChecker.checkCanceled();
		if (validationSettings == null) {
			validationSettings = QuteValidationSettings.DEFAULT;
		}
		if (!validationSettings.canValidate(template.getUri())) {
			// the validation is disabled for this template
			return Collections.emptyList();
		}

		if (!resolvingJavaTypeContext.isProjectResolved()) {
			LOGGER.log(Level.INFO, "Resolving project for the template '" + template.getUri() + "'.");
		} else if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
			LOGGER.log(Level.INFO, "Resolving data model (@CheckedTemplate, Template field) for the template '"
					+ template.getUri() + "'.");
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		// Validate Qute syntax
		try {
			diagnosticsForSyntax.validateWithRealQuteParser(template, diagnostics);
		} catch (CancellationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while validating Qute syntax'" + template.getUri() + "'.", e);
		}
		// Validate data model
		try {
			validateDataModel(template, template, validationSettings, nativeImagesSettings, resolvingJavaTypeContext,
					new ResolutionContext(), diagnostics);
		} catch (CancellationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while validating Qute data model'" + template.getUri() + "'.", e);
		}
		cancelChecker.checkCanceled();
		return diagnostics;
	}

	private void validateDataModel(Node parent, Template template, QuteValidationSettings validationSettings,
			QuteNativeSettings nativeImagesSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			ResolutionContext currentContext, List<Diagnostic> diagnostics) {
		QuteProject project = template.getProject();
		String projectUri = project != null ? project.getUri() : null;
		JavaTypeFilter filter = projectRegistry.getJavaTypeFilter(projectUri, nativeImagesSettings);
		ResolutionContext previousContext = currentContext;
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
				case ParameterDeclaration: {
					if (project != null) {
						ParameterDeclaration parameter = (ParameterDeclaration) node;
						validateParameterDeclaration(parameter, template, project, resolvingJavaTypeContext,
								currentContext,
								diagnostics);
					}
					break;
				}
				case Section: {
					Section section = (Section) node;
					if (canChangeContext(section)) {
						currentContext = new ResolutionContext(currentContext);
					}
					List<Parameter> parameters = section.getParameters();
					// validate expression parameters
					boolean checkValidOperator = section.getSectionKind() == SectionKind.IF;
					boolean shouldBeAnOperator = false;
					for (Parameter parameter : parameters) {
						if (shouldBeAnOperator) {
							// #if section, the current parameter name must be an operator
							String operatorName = parameter.getName();
							if (!section.isValidOperator(operatorName)) {
								Range range = QutePositionUtility.createRange(parameter);
								Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
										QuteErrorCode.InvalidOperator, operatorName, section.getTag(),
										section.getAllowedOperators() //
												.stream() //
												.map(Operator::getName) //
												.collect(Collectors.joining(",", "[", "]")));
								diagnostics.add(diagnostic);
							}
						} else {
							Expression expression = parameter.getJavaTypeExpression();
							if (expression != null) {
								// Validate object, property, method parts from the expression
								ResolvedJavaTypeInfo result = validateExpression(expression, section, template,
										validationSettings, filter, previousContext, resolvingJavaTypeContext,
										diagnostics);
								switch (section.getSectionKind()) {
									case FOR:
									case EACH:
										String alias = ((LoopSection) section).getAlias();
										currentContext.put(alias, result);
										break;
									case WITH:
										currentContext.setWithObject(result);
										break;
									case LET:
									case SET:
										currentContext.put(parameter.getName(), result);
										break;
									case SWITCH:
									case WHEN:
										currentContext.setWhenObject(result);
										break;
									default:
								}
							}
						}
						shouldBeAnOperator = checkValidOperator && !shouldBeAnOperator;
					}
					switch (section.getSectionKind()) {
						case INCLUDE:
							validateIncludeSection((IncludeSection) section, diagnostics);
							break;
						case CASE:
						case IS:
							// Ex: {#switch person.name} --> person.name is of type String
							// {#case 'John'} --> Ensure type of part ('John') inside #case section matches
							// #switch
							// {#case 123} --> Report unexpected type here, expected String but was Integer
							// {/switch}
							Section parentSection = section.getParentSection();
							if (!isWhenSection(parentSection)) {
								Range range = QutePositionUtility.selectStartTagName(section);
								Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
										QuteErrorCode.InvalidParentInCaseSection, section.getTag());
								diagnostics.add(diagnostic);
							} else {
								ResolvedJavaTypeInfo whenJavaType = currentContext.whenObject;
								if (whenJavaType != null) {
									validateCaseSectionParameters((CaseSection) section, parentSection, template,
											whenJavaType, project, diagnostics);
								}
							}
							break;
						default:
							validateSectionTag(section, template, resolvingJavaTypeContext, diagnostics);
					}
					break;
				}
				case Expression: {
					validateExpression((Expression) node, null, template, validationSettings, filter, previousContext,
							resolvingJavaTypeContext, diagnostics);
					break;
				}
				default:
			}
			validateDataModel(node, template, validationSettings, nativeImagesSettings, resolvingJavaTypeContext,
					currentContext, diagnostics);
		}
	}

	/**
	 * Validate parameter declaration.
	 *
	 * @param parameter                the parameter declaration.
	 * @param template                 the owner Qute template.
	 * @param projectUri               the project Uri.
	 * @param resolvingJavaTypeContext the resoving Java type context.
	 * @param currentContext           the resolution context.
	 * @param diagnostics              the diagnostics to update.
	 */
	private void validateParameterDeclaration(ParameterDeclaration parameter, Template template, QuteProject project,
			ResolvingJavaTypeContext resolvingJavaTypeContext, ResolutionContext currentContext,
			List<Diagnostic> diagnostics) {
		String javaTypeToResolve = parameter.getJavaType();
		if (!StringUtils.isEmpty(javaTypeToResolve)) {

			List<JavaTypeRangeOffset> classNameRanges = parameter.getJavaTypeNameRanges();
			for (RangeOffset classNameRange : classNameRanges) {
				String className = template.getText(classNameRange);
				ResolvedJavaTypeInfo resolvedJavaType = resolveJavaType(className, project,
						resolvingJavaTypeContext);
				if (resolvedJavaType == null) {
					// ex : {@org.acme.XXXXX item}
					// Java type 'org.acme.XXXXX' doesn't exist
					Range range = QutePositionUtility.createRange(classNameRange, template);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.UnknownType, className);
					diagnostics.add(diagnostic);
				} else if (!QuteCompletableFutures.isResolvingJavaType(resolvedJavaType)) {
					currentContext.put(javaTypeToResolve, resolvedJavaType);
				}
			}
		}
	}

	private static void validateSectionTag(Section section, Template template,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		String tagName = section.getTag();
		if (StringUtils.isEmpty(tagName) || section.isOrphanEndTag()) {
			// {#
			// {/elsa}
			// we ignore this error here, because the Qute validator syntax already takes
			// care of this error.
			return;
		}
		SectionKind sectionKind = section.getSectionKind();
		if (sectionKind == SectionKind.CUSTOM) {
			if (!resolvingJavaTypeContext.isBinaryUserTagResolved()) {
				// Don't validate custom tag, if the binary user tags are not loaded.
				return;
			}

			QuteProject project = template.getProject();
			if (project != null) {

				// Check if section tag is an user tag
				UserTag userTag = project.findUserTag(tagName);
				if (userTag != null) {
					// Validate parameters of user tag

					List<String> existingParameters = new ArrayList<>();
					// Check if the parameter exists
					for (Parameter parameter : section.getParameters()) {
						String paramName = !parameter.hasValueAssigned() ? IT_OBJECT_PART_NAME : parameter.getName();
						if (existingParameters.contains(paramName)) {
							// It exists several parameters with the same name
							Range range = QutePositionUtility.selectParameterName(parameter);
							Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
									QuteErrorCode.DuplicateParameter, paramName, tagName);
							diagnostics.add(diagnostic);
						} else {
							existingParameters.add(paramName);
							// Check if the declared parameter is defined in the user tag
							UserTagParameter userTagParameter = userTag.findParameter(paramName);
							if (userTagParameter == null) {
								Range range = QutePositionUtility.selectParameterName(parameter);
								Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
										QuteErrorCode.UndefinedParameter, paramName, tagName);
								diagnostics.add(diagnostic);
							}
						}
					}

					// Check if all required parameters are declared
					List<String> missingRequiredParameters = new ArrayList<>();
					for (UserTagParameter parameter : userTag.getParameters()) {
						String paramName = parameter.getName();
						if (!NESTED_CONTENT_OBJECT_PART_NAME.equals(paramName)) {
							if (parameter.isRequired() && !existingParameters.contains(paramName)) {
								missingRequiredParameters.add(paramName);
							}
						}
					}
					if (!missingRequiredParameters.isEmpty()) {
						String diagnosticMessageArg = missingRequiredParameters //
								.stream() //
								.collect(Collectors.joining("`, `", "`", "`"));
						Range range = QutePositionUtility.selectStartTagName(section);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
								QuteErrorCode.MissingRequiredParameter, diagnosticMessageArg, tagName);
						diagnostics.add(diagnostic);
					}
					return;
				}

				// Check if section tag is a parameter from an include section
				Node parent = section.getParent();
				while (parent != null) {
					if (parent.getKind() == NodeKind.Section) {
						Section parentSection = (Section) parent;
						if (parentSection.getSectionKind() == SectionKind.INCLUDE) {
							IncludeSection includeSection = (IncludeSection) parentSection;
							List<Parameter> parameters = project
									.findInsertTagParameter(includeSection.getReferencedTemplateId(), tagName);
							if (parameters != null) {
								// The parameter exists
								return;
							}
						}
					}
					parent = parent.getParent();
				}

				Range range = QutePositionUtility.selectStartTagName(section);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.UndefinedSectionTag, tagName);
				diagnostics.add(diagnostic);
			}
		}

	}

	private static boolean canChangeContext(Section section) {
		SectionKind sectionKind = section.getSectionKind();
		return sectionKind == SectionKind.EACH || sectionKind == SectionKind.FOR || sectionKind == SectionKind.LET
				|| sectionKind == SectionKind.SET || sectionKind == SectionKind.WITH
				|| sectionKind == SectionKind.SWITCH || sectionKind == SectionKind.WHEN;
	}

	/**
	 * Validate #include section.
	 *
	 * @param includeSection the include section
	 * @param diagnostics    the diagnostics to fill.
	 */
	private static void validateIncludeSection(IncludeSection includeSection, List<Diagnostic> diagnostics) {
		Parameter templateParameter = includeSection.getTemplateParameter();
		if (templateParameter != null) {
			// Validate template id, only if project exists.
			QuteProject project = includeSection.getOwnerTemplate().getProject();
			if (project != null) {
				// include defines a template to include
				// ex : {#include base}
				Path templateFile = includeSection.getReferencedTemplateFile();
				if (templateFile == null || Files.notExists(templateFile)) {
					// It doesn't exists a file named base, base.qute.html, base.html, etc
					Range range = QutePositionUtility.createRange(templateParameter);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.TemplateNotFound, templateParameter.getValue());
					diagnostics.add(diagnostic);
				}
			}
		} else {
			// #include doesn't define a template id
			// ex: {#include}
			Range range = QutePositionUtility.selectStartTagName(includeSection);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.TemplateNotDefined);
			diagnostics.add(diagnostic);
		}
	}

	/**
	 * Validate Renarde #form section.
	 *
	 * @param section           the form section
	 * @param javaMemberInfo    the information on the method referenced in the form
	 *                          section.
	 * @param baseTypeSignature the base type of the method referenced in the form
	 *                          section.
	 * @param diagnostics       the diagnostics.
	 */
	private static void validateRenardeFormSectionParameters(Section section, JavaMemberInfo javaMemberInfo,
			String baseTypeSignature, List<Diagnostic> diagnostics) {
		if (javaMemberInfo == null) {
			return;
		}
		JavaMethodInfo method = (JavaMethodInfo) javaMemberInfo;

		Collection<RestParam> restParams = method.getRestParameters();
		if (restParams.isEmpty()) {
			return;
		}
		// Collect the @RestForm parameters
		List<RestParam> formParams = restParams.stream()
				.filter(p -> p.getParameterKind() == JaxRsParamKind.FORM)
				.collect(Collectors.toList());
		if (formParams.isEmpty()) {
			return;
		}

		// Collect all html input elements from the text nodes inside #form section
		CollectHtmlInputNamesVisitor visitor = new CollectHtmlInputNamesVisitor();
		section.accept(visitor);
		List<String> existingInputNames = visitor.getHtmlInputNames();

		// Validate @RestForm parameters
		List<String> missingRequiredInputNames = new ArrayList<>();
		for (RestParam param : formParams) {
			if (param.isRequired()) {
				if (existingInputNames == null || !existingInputNames.contains(param.getName())) {
					missingRequiredInputNames.add(param.getName());
				}
			}
		}
		if (!missingRequiredInputNames.isEmpty()) {
			String diagnosticMessageArg;
			diagnosticMessageArg = missingRequiredInputNames //
					.stream() //
					.collect(Collectors.joining("`, `", "`", "`"));
			Range range = QutePositionUtility.selectStartTagName(section);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
					QuteErrorCode.MissingExpectedInput, diagnosticMessageArg);
			diagnostic.setData(new JavaBaseTypeOfPartData(baseTypeSignature));
			diagnostics.add(diagnostic);
		}
	}

	private ResolvedJavaTypeInfo validateExpression(Expression expression, Section ownerSection, Template template,
			QuteValidationSettings validationSettings, JavaTypeFilter filter, ResolutionContext resolutionContext,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		try {
			QuteProject project = template.getProject();
			String literalJavaType = expression.getLiteralJavaType();
			if (literalJavaType != null) {
				// The expression is a literal:
				// - {'abcd'} : string literal
				// - {true} : boolean literal
				// - {null} : null literal
				// - {123} : integer literal
				ResolvedJavaTypeInfo resolvedLiteralType = project != null
						? project.resolveJavaTypeSync(literalJavaType)
						: null;
				if (QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedLiteralType)) {
					return null;
				}
				return validateIterable(expression.getLastPart(), ownerSection, resolvedLiteralType,
						resolvedLiteralType.getName(), diagnostics);
			}
			// The expression reference Java data model (ex : {item})
			ResolvedJavaTypeInfo resolvedJavaType = null;
			List<Node> expressionChildren = expression.getExpressionContent();
			for (Node expressionChild : expressionChildren) {
				if (expressionChild.getKind() == NodeKind.ExpressionParts) {
					Parts parts = (Parts) expressionChild;
					resolvedJavaType = validateExpressionParts(parts, ownerSection, template, project,
							validationSettings, filter, resolutionContext, resolvingJavaTypeContext, diagnostics);
				}
			}
			return resolvedJavaType;
		} catch (CancellationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while validating expression '" + expression.getContent() + "' in '"
					+ template.getUri() + "'.", e);
			return null;
		}
	}

	private ResolvedJavaTypeInfo validateExpressionParts(Parts parts, Section ownerSection, Template template,
			QuteProject project, QuteValidationSettings validationSettings, JavaTypeFilter filter,
			ResolutionContext resolutionContext, ResolvingJavaTypeContext resolvingJavaTypeContext,
			List<Diagnostic> diagnostics) {
		// In native mode we need to collect all Java type of the expression to use it
		// for filter Java type, fields and methods.
		ResolvedJavaTypeInfo baseType = null;
		String namespace = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = parts.getChild(i);

			switch (current.getPartKind()) {

				case Namespace: {
					NamespacePart namespacePart = (NamespacePart) current;
					namespace = validateNamespace(namespacePart, project, validationSettings,
							resolvingJavaTypeContext,
							diagnostics);
					if (namespace == null) {
						// Invalid namespace
						return null;
					}
					break;
				}

				case Object: {
					ObjectPart objectPart = (ObjectPart) current;
					baseType = validateObjectPart(namespace, objectPart, ownerSection, template, project,
							validationSettings, resolutionContext, diagnostics, resolvingJavaTypeContext);
					if (QuteCompletableFutures.isResolvingJavaType(baseType)) {
						// The java type is resolving, stop the validation
						return QuteCompletableFutures.RESOLVING_JAVA_TYPE;
					}
					break;
				}

				case Method:
				case Property: {
					// java.util.List<org.acme.Item>
					if (QuteCompletableFutures.isResolvingJavaType(baseType)) {
						// The java type is resolving, stop the validation
						return QuteCompletableFutures.RESOLVING_JAVA_TYPE;
					}
					baseType = validateMemberPart(current, ownerSection, template, project, validationSettings,
							filter,
							resolutionContext, baseType, baseType, diagnostics, resolvingJavaTypeContext);
					break;
				}
			}
		}
		return baseType;
	}

	private ResolvedJavaTypeInfo resolveJavaType(String javaType, QuteProject project,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		return resolvingJavaTypeContext.resolveJavaType(javaType, project);
	}

	/**
	 * Validate namespace part.
	 *
	 * @param namespacePart            the namespace part to validate.
	 * @param projectUri               the project Uri.
	 * @param resolvingJavaTypeContext
	 * @param diagnostics              the diagnostics list to fill.
	 * @return the namespace of the part if it is an defined namespace and null
	 *         otherwise.
	 */
	private String validateNamespace(NamespacePart namespacePart, QuteProject project,
			QuteValidationSettings validationSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			List<Diagnostic> diagnostics) {
		String namespace = namespacePart.getPartName();
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			return namespace;
		}
		if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
			// The data model is not loaded, ignore the error of undefined namespace
			return null;
		}

		if (project != null) {

			if (!project.hasNamespace(namespace)) {
				DiagnosticSeverity severity = validationSettings.getUndefinedNamespace().getDiagnosticSeverity();
				if (severity == null) {
					return null;
				}
				Range range = QutePositionUtility.createRange(namespacePart);
				Diagnostic diagnostic = createDiagnostic(range, severity, QuteErrorCode.UndefinedNamespace,
						namespacePart.getPartName());
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return namespace;
	}

	private ResolvedJavaTypeInfo validateObjectPart(String namespace, ObjectPart objectPart, Section ownerSection,
			Template template, QuteProject project, QuteValidationSettings validationSettings,
			ResolutionContext resolutionContext, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		if (project == null) {
			return null;
		}

		JavaMemberInfo javaMember = resolutionContext.findMemberWithObject(objectPart.getPartName(), project);
		if (javaMember != null) {
			ResolvedJavaTypeInfo resolvedJavaType = resolveJavaType(javaMember.getJavaElementType(), project,
					resolvingJavaTypeContext);
			// returns the java type or the resolving java type
			return resolvedJavaType;
		}

		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			if (objectPart.isOptional()) {
				// The object part is optional (ex : foo??)
				return null;
			}

			String partName = objectPart.getPartName();
			if (partName.isEmpty()) {
				// This case comes from when only ! is used in #if
				// ex : {#if !}
				// In this case ! is not considered as an object part.
				return null;
			}

			String literalJavaType = LiteralSupport.getLiteralJavaType(partName);
			if (literalJavaType != null) {
				// The object part is a literal type (ex : true)
				return null;
			}

			if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
				// The data model is not loaded, ignore the error of undefined object
				return null;
			}

			if (UserTagUtils.isUserTag(template)) {
				// Ignore undefined object diagnostic for user tag
				return null;
			}

			if (Section.isCaseSection(ownerSection)) {
				// Skip validation for case section, is done later
				return null;
			}

			// ex : {item} --> undefined object
			DiagnosticSeverity severity = validationSettings.getUndefinedObject().getDiagnosticSeverity();
			if (severity == null) {
				return null;
			}
			Range range = QutePositionUtility.createRange(objectPart);
			Diagnostic diagnostic = createDiagnostic(range, severity, QuteErrorCode.UndefinedObject,
					objectPart.getPartName());
			diagnostics.add(diagnostic);
			return null;
		}

		String javaTypeToResolve = javaTypeInfo.getJavaType();
		if (javaTypeToResolve == null) {
			// case of (#for item as data.items) where data.items expression must be
			// evaluated
			Expression expression = javaTypeInfo.getJavaTypeExpression();
			if (expression != null) {
				String literalJavaType = expression.getLiteralJavaType();
				if (literalJavaType != null) {
					javaTypeToResolve = literalJavaType;
				} else {
					Part lastPart = expression.getLastPart();
					if (lastPart != null) {
						ResolvedJavaTypeInfo alias = project.resolveJavaType(lastPart)
								.getNow(QuteCompletableFutures.RESOLVING_JAVA_TYPE);
						if (QuteCompletableFutures.isResolvingJavaType(alias)) {
							// The java type is resolving, stop the validation
							return QuteCompletableFutures.RESOLVING_JAVA_TYPE;
						}
						if (alias == null) {
							if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
								// The data model is not loaded, don't validate the item of items.
								return null;
							}
						} else {
							javaTypeToResolve = alias.getSignature();
						}
					}
				}
			}
		}
		return validateJavaTypePart(objectPart, ownerSection, project, diagnostics, resolvingJavaTypeContext,
				javaTypeToResolve, javaTypeInfo.getJavaTypeOwnerNode());
	}

	/**
	 * Validate the given property, method part.
	 *
	 * @param part                     the property, method part to validate.
	 * @param ownerSection             the owner section and null otherwise.
	 * @param template                 the template.
	 * @param projectUri               the project Uri.
	 * @param resolutionContext        the resolution context.
	 * @param baseType                 the base object type.
	 * @param iterableOfType           the iterable of type.
	 * @param diagnostics              the diagnostic list to fill.
	 * @param resolvingJavaTypeContext the resolving Java type context.
	 *
	 * @return the Java type returned by the member part and null otherwise.
	 */
	private ResolvedJavaTypeInfo validateMemberPart(Part part, Section ownerSection, Template template,
			QuteProject project, QuteValidationSettings validationSettings, JavaTypeFilter filter,
			ResolutionContext resolutionContext, ResolvedJavaTypeInfo baseType, ResolvedJavaTypeInfo iterableOfType,
			List<Diagnostic> diagnostics, ResolvingJavaTypeContext resolvingJavaTypeContext) {
		if (part.getPartKind() == PartKind.Method) {
			// Validate method part
			// ex : {foo.method(1, 2)}
			return validateMethodPart((MethodPart) part, ownerSection, template, project, validationSettings, filter,
					resolutionContext, baseType, iterableOfType, diagnostics, resolvingJavaTypeContext);
		}
		// Validate property part
		// ex : {foo.property}
		return validatePropertyPart((PropertyPart) part, ownerSection, template, project, resolutionContext,
				baseType, iterableOfType, filter, diagnostics, resolvingJavaTypeContext);
	}

	/**
	 * Validate the given property part.
	 *
	 * @param part                     the property part to validate.
	 * @param ownerSection             the owner section and null otherwise.
	 * @param template                 the template.
	 * @param projectUri               the project Uri.
	 * @param resolutionContext        the resolution context.
	 * @param baseType                 the base object type.
	 * @param iterableOfType           the iterable of type.
	 * @param nativeImagesSettings
	 * @param diagnostics              the diagnostic list to fill.
	 * @param resolvingJavaTypeContext the resolving Java type context.
	 *
	 * @return the Java type returned by the member part and null otherwise.
	 */
	private ResolvedJavaTypeInfo validatePropertyPart(PropertyPart part, Section ownerSection, Template template,
			QuteProject project, ResolutionContext resolutionContext, ResolvedJavaTypeInfo baseType,
			ResolvedJavaTypeInfo iterableOfType, JavaTypeFilter filter, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		if (!isValidJavaType(baseType) || project == null) {
			return null;
		}
		JavaMemberResult result = project.findProperty(part, baseType, filter.isInNativeMode());
		JavaMemberInfo javaMember = result.getMember();
		if (javaMember == null) {
			// ex : {@org.acme.Item item}
			// "{item.XXXX}
			Range range = QutePositionUtility.createRange(part);
			String signature = baseType.getSignature();
			String property = part.getPartName();
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnknownProperty,
					property, signature);
			diagnostic.setData(new JavaBaseTypeOfPartData(signature));
			diagnostics.add(diagnostic);
			return null;
		} else if (canValidateMemberInNativeMode(filter, javaMember)) {
			// The property (field, method) has been retrieved, check if it is a valid in
			// native mode
			JavaTypeAccessibiltyRule javaTypeAccessibility = filter.getJavaTypeAccessibility(baseType,
					resolvingJavaTypeContext.getJavaTypesSupportedInNativeMode());
			if (!JavaTypeAccessibiltyRule.ALLOWED_WITHOUT_RESTRICTION.equals(javaTypeAccessibility)) {
				if (javaTypeAccessibility == null) {
					Range range = QutePositionUtility.createRange(part);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.PropertyNotSupportedInNativeMode, part.getPartName(),
							baseType.getSignature());
					diagnostics.add(diagnostic);
					return null;
				}
				if (!filter.isSuperClassAllowed(javaMember, baseType, javaTypeAccessibility)) {
					// @TemplateData(ignoreSuperclasses = true)
					Range range = QutePositionUtility.createRange(part);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.InheritedPropertyNotSupportedInNativeMode, part.getPartName(),
							baseType.getSignature());
					diagnostics.add(diagnostic);
					return null;
				}

				JavaMemberAccessibility javaMemberAccessibility = filter.getJavaMemberAccessibility(javaMember,
						javaTypeAccessibility);
				switch (javaMemberAccessibility.getKind()) {
					case FORBIDDEN_BY_TEMPLATE_DATA_IGNORE: {
						Range range = QutePositionUtility.createRange(part);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
								QuteErrorCode.MethodIgnoredByTemplateData, part.getPartName(), baseType.getSignature(),
								javaMemberAccessibility.getIgnore());
						diagnostics.add(diagnostic);
						return null;
					}

					case FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES: {
						JavaMethodInfo method = (JavaMethodInfo) javaMember;
						Range range = QutePositionUtility.createRange(part);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
								QuteErrorCode.ForbiddenByTemplateDataProperties, method.getName(),
								baseType.getSignature(),
								method.getParameters().size());
						diagnostics.add(diagnostic);
						return null;
					}

					case FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS:
					case FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS: {
						Range range = QutePositionUtility.createRange(part);
						QuteErrorCode errorCode = javaMember.getJavaElementKind() == JavaElementKind.METHOD
								? QuteErrorCode.ForbiddenByRegisterForReflectionMethods
								: QuteErrorCode.ForbiddenByRegisterForReflectionFields;
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, errorCode,
								part.getPartName(), baseType.getSignature());
						diagnostics.add(diagnostic);
						return null;
					}

					default:
				}
			}
		}

		String memberType = javaMember.resolveJavaElementType(iterableOfType);
		return validateJavaTypePart(part, ownerSection, project, diagnostics, resolvingJavaTypeContext, memberType,
				null);
	}

	/**
	 * Validate the given method part.
	 *
	 * @param methodPart               the method part to validate.
	 * @param ownerSection             the owner section and null otherwise.
	 * @param template                 the template.
	 * @param project                  the project Uri.
	 * @param validationSettings       the validation settings.
	 * @param the                      Java type filter.
	 * @param resolutionContext        the resolution context.
	 * @param baseType                 the base object type.
	 * @param iterableOfType           the iterable of type.
	 * @param diagnostics              the diagnostic list to fill.
	 * @param resolvingJavaTypeContext the resolving Java type context.
	 *
	 * @return the Java type returned by the member part and null otherwise.
	 */
	private ResolvedJavaTypeInfo validateMethodPart(MethodPart methodPart, Section ownerSection, Template template,
			QuteProject project, QuteValidationSettings validationSettings, JavaTypeFilter filter,
			ResolutionContext resolutionContext, ResolvedJavaTypeInfo baseType, ResolvedJavaTypeInfo iterableOfType,
			List<Diagnostic> diagnostics, ResolvingJavaTypeContext resolvingJavaTypeContext) {
		if (methodPart.isInfixNotation()) {
			// ex : foo or '1'
			// The method part used with infix notation must have one parameter
			if (methodPart.getParameters().isEmpty()) {
				Range range = QutePositionUtility.createRange(methodPart);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.InfixNotationParameterRequired, methodPart.getPartName());
				diagnostics.add(diagnostic);
				return null;
			}
		}

		// Validate parameters of the method part
		boolean undefinedType = false;
		List<ResolvedJavaTypeInfo> parameterTypes = new ArrayList<>();
		for (Parameter parameter : methodPart.getParameters()) {
			ResolvedJavaTypeInfo result = null;
			Expression expression = parameter.getJavaTypeExpression();
			if (expression != null) {
				result = validateExpression(expression, ownerSection, template, validationSettings, filter,
						resolutionContext, resolvingJavaTypeContext, diagnostics);
			}
			if (result == null || QuteCompletableFutures.RESOLVING_JAVA_TYPE == result) {
				// The resolved type cannot be resolved or it is resolving.
				undefinedType = true;
			}
			parameterTypes.add(result);
		}
		if (undefinedType) {
			// One of parameter cannot be resolved as type, the validation is stopped
			return null;
		}

		if (methodPart.isOperator()) {
			return baseType;
		}

		if (project == null) {
			return null;
		}
		// All parameters are resolved, validate the existing of method name according
		// to the computed parameter types
		String methodName = methodPart.getPartName();
		String namespace = methodPart.getNamespace();
		JavaMemberResult result = project.findMethod(baseType, namespace, methodName, parameterTypes,
				filter.isInNativeMode());
		JavaMethodInfo method = (JavaMethodInfo) result.getMember();
		if (method == null) {
			String signature = null;
			QuteErrorCode errorCode = QuteErrorCode.UnknownMethod;
			String arg = null;
			if (namespace != null) {
				// ex :{config.getXXXX()}
				errorCode = QuteErrorCode.UnknownNamespaceResolverMethod;
				arg = namespace;
			} else {
				// ex : {@org.acme.Item item}
				// {item.getXXXX()}
				if (baseType != null) {
					arg = baseType.getSignature();
					InvalidMethodReason reason = project.getInvalidMethodReason(methodName, baseType);
					if (reason != null) {
						switch (reason) {
							case VoidReturn:
								errorCode = QuteErrorCode.InvalidMethodVoid;
								break;
							case Static:
								errorCode = QuteErrorCode.InvalidMethodStatic;
								break;
							case FromObject:
								errorCode = QuteErrorCode.InvalidMethodFromObject;
								break;
							default:
						}
					}
					signature = baseType.getSignature();
				}
			}
			Range range = QutePositionUtility.createRange(methodPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, errorCode, methodName, arg);
			if (signature != null) {
				diagnostic.setData(new JavaBaseTypeOfPartData(signature));
			}
			diagnostics.add(diagnostic);
			return null;
		} else if (canValidateMemberInNativeMode(filter, method)) {
			// The (non virtual) method has been retrieved, check if it is a valid in native
			// mode

			JavaTypeAccessibiltyRule javaTypeAccessibility = filter.getJavaTypeAccessibility(baseType,
					resolvingJavaTypeContext.getJavaTypesSupportedInNativeMode());
			if (!JavaTypeAccessibiltyRule.ALLOWED_WITHOUT_RESTRICTION.equals(javaTypeAccessibility)) {

				if (javaTypeAccessibility == null) {
					Range range = QutePositionUtility.createRange(methodPart);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.MethodNotSupportedInNativeMode, method.getName(), baseType.getSignature());
					diagnostics.add(diagnostic);
					return null;
				}
				if (!filter.isSuperClassAllowed(method, baseType, javaTypeAccessibility)) {
					Range range = QutePositionUtility.createRange(methodPart);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.InheritedMethodNotSupportedInNativeMode, method.getName(),
							baseType.getSignature());
					diagnostics.add(diagnostic);
					return null;
				}
				JavaMemberAccessibility javaMemberAccessibility = filter.getJavaMemberAccessibility(method,
						javaTypeAccessibility);
				switch (javaMemberAccessibility.getKind()) {

					case FORBIDDEN_BY_TEMPLATE_DATA_IGNORE: {
						Range range = QutePositionUtility.createRange(methodPart);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
								QuteErrorCode.MethodIgnoredByTemplateData, method.getName(), baseType.getSignature(),
								javaMemberAccessibility.getIgnore());
						diagnostics.add(diagnostic);
						return null;
					}

					case FORBIDDEN_BY_TEMPLATE_DATA_PROPERTIES: {
						Range range = QutePositionUtility.createRange(methodPart);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
								QuteErrorCode.ForbiddenByTemplateDataProperties, method.getName(),
								baseType.getSignature(),
								method.getParameters().size());
						diagnostics.add(diagnostic);
						return null;
					}

					case FORBIDDEN_BY_REGISTER_FOR_REFLECTION_FIELDS:
					case FORBIDDEN_BY_REGISTER_FOR_REFLECTION_METHODS: {
						Range range = QutePositionUtility.createRange(methodPart);
						QuteErrorCode errorCode = method.getJavaElementKind() == JavaElementKind.METHOD
								? QuteErrorCode.ForbiddenByRegisterForReflectionMethods
								: QuteErrorCode.ForbiddenByRegisterForReflectionFields;
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, errorCode,
								method.getName(), baseType.getSignature());
						diagnostics.add(diagnostic);
						return null;
					}
					default:
				}
			}
		}

		boolean matchVirtualMethod = result.isMatchVirtualMethod();
		if (!matchVirtualMethod) {

			Range range = QutePositionUtility.createRange(methodPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
					QuteErrorCode.InvalidVirtualMethod, //
					method.getName(), method.getSimpleSourceType(), //
					baseType.getJavaElementSimpleType());
			diagnostics.add(diagnostic);
			return null;
		}

		if (methodPart.isInfixNotation()) {
			int nbParameters = method.getParameters().size() - (method.isVirtual() ? 1 : 0);
			if (nbParameters != 1) {
				// infix notation,
				// ex: String#codePointCount cannot be used with infix notation
				// String#codePointCount(beginIndex : int,endIndex : int) : int
				Range range = QutePositionUtility.createRange(methodPart);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.InvalidMethodInfixNotation, methodName);
				diagnostics.add(diagnostic);
				return null;
			}
		}

		boolean matchParameters = result.isMatchParameters();
		if (!matchParameters) {

			// The method codePointAt(int) in the type String is not applicable for the
			// arguments ()
			StringBuilder expectedSignature = new StringBuilder("(");
			boolean ignoreParameter = method.isVirtual();
			for (JavaParameterInfo parameter : method.getParameters()) {
				if (!ignoreParameter) {
					if (expectedSignature.length() > 1) {
						expectedSignature.append(", ");
					}
					expectedSignature.append(parameter.getJavaElementSimpleType());
				}
				ignoreParameter = false;
			}
			expectedSignature.append(")");
			expectedSignature.insert(0, method.getName());

			StringBuilder actualSignature = new StringBuilder("(");
			for (ResolvedJavaTypeInfo parameterType : parameterTypes) {
				if (actualSignature.length() > 1) {
					actualSignature.append(", ");
				}
				actualSignature
						.append(parameterType != null ? parameterType.getJavaElementSimpleType() : parameterType);
			}
			actualSignature.append(")");

			Range range = QutePositionUtility.createRange(methodPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
					QuteErrorCode.InvalidMethodParameter, //
					expectedSignature.toString() /* codePointAt(int) */, //
					method.getSimpleSourceType() /* String */, //
					actualSignature.toString() /* "()" */);
			diagnostics.add(diagnostic);
			return null;
		}

		if (method.isVoidMethod()) {
			return null;
		}
		if (ownerSection != null && FORM_SECTION_TAG.equals(ownerSection.getTag())) {
			validateRenardeFormSectionParameters(ownerSection, result.getMember(), baseType.getSignature(),
					diagnostics);
		}
		String memberType = method.resolveJavaElementType(iterableOfType);
		return validateJavaTypePart(methodPart, ownerSection, project, diagnostics, resolvingJavaTypeContext,
				memberType, null);
	}

	private static boolean canValidateMemberInNativeMode(JavaTypeFilter filter, JavaMemberInfo member) {
		if (member.getJavaElementKind() == JavaElementKind.METHOD) {
			JavaMethodInfo method = (JavaMethodInfo) member;
			if (method.isVirtual()) {
				return false;
			}
		}
		return filter.isInNativeMode();
	}

	private ResolvedJavaTypeInfo validateJavaTypePart(Part part, Section ownerSection, QuteProject project,
			List<Diagnostic> diagnostics, ResolvingJavaTypeContext resolvingJavaTypeContext, String javaTypeToResolve,
			Node referencedNode) {
		if (StringUtils.isEmpty(javaTypeToResolve)) {
			if (referencedNode != null && referencedNode.getKind() == NodeKind.Parameter
					&& ((Parameter) (referencedNode)).isOptional()) {
				// {foo} inside an #if block references foo as optional {#if foo??}
				return null;
			}
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnknownType,
					part.getPartName());
			diagnostics.add(diagnostic);
			return null;
		}

		if (LiteralSupport.isNull(javaTypeToResolve)) {
			return null;
		}

		if (project == null) {
			return null;
		}

		CompletableFuture<ResolvedJavaTypeInfo> resolvingJavaTypeFuture = null;
		if (part.getPartKind() == PartKind.Object) {
			// Object part case.
			// - if expression is included inside a loop section (#for, etc), we need to get
			// the iterable of. If javaTypeToResolve= 'java.util.List<org.acme.Item>',we
			// must get 'org.acme.Item'.
			// - otherwise resolve the given java type to resolve
			resolvingJavaTypeFuture = project.resolveJavaType(part);
		} else {
			// Other part kind (property, method), resolve the given java type to resolve
			resolvingJavaTypeFuture = project.resolveJavaType(javaTypeToResolve, true);
		}
		ResolvedJavaTypeInfo resolvedJavaType = resolvingJavaTypeFuture
				.getNow(QuteCompletableFutures.RESOLVING_JAVA_TYPE);
		if (QuteCompletableFutures.isResolvingJavaType(resolvedJavaType)) {
			// Java type must be loaded.
			LOGGER.log(Level.INFO, QuteErrorCode.ResolvingJavaType.getMessage(javaTypeToResolve));
			resolvingJavaTypeContext.add(resolvingJavaTypeFuture);
			return QuteCompletableFutures.RESOLVING_JAVA_TYPE;
		}

		if (resolvedJavaType == null) {
			// Java type doesn't exist
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnknownType,
					javaTypeToResolve);
			diagnostics.add(diagnostic);
			return null;
		}

		return validateIterable(part, ownerSection, resolvedJavaType, javaTypeToResolve, diagnostics);
	}

	private ResolvedJavaTypeInfo validateIterable(Part part, Section ownerSection,
			ResolvedJavaTypeInfo resolvedJavaType, String javaTypeToResolve, List<Diagnostic> diagnostics) {
		if (part != null && part.isLast() && ownerSection != null && ownerSection.isIterable()) {
			// The expression is declared inside an iterable section like #for, #each.
			// Ex: {#for item in items}
			if (!resolvedJavaType.isIterable() && !resolvedJavaType.isInteger()) {
				// The Java class is not an iterable class like
				// - java.util.List
				// - object array
				// - integer
				String expression = part.getParent().getContent();
				Range range = QutePositionUtility.createRange(part);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.IterationError,
						expression, javaTypeToResolve);
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return resolvedJavaType;
	}

	/**
	 * Validate the part in a case or in section.
	 *
	 * @param caseSection the case or is section.
	 * @param whenSection the parent switch or when section.
	 * @param whenType    the type of the switch or when section that is expected in
	 *                    the case or in section.
	 * @param projectUri  the project Uri.
	 * @param diagnostics the diagnostic list.
	 */
	private void validateCaseSectionParameters(CaseSection caseSection, Section whenSection, Template template,
			ResolvedJavaTypeInfo whenType, QuteProject project, List<Diagnostic> diagnostics) {
		String expression = whenSection.getExpressionContent();
		List<Parameter> parameters = caseSection.getParameters();
		String whenTypeName = whenType.getSignature();
		if (parameters.isEmpty()) {
			// Ex: {#case} --> no parameters or operators
			Range range = QutePositionUtility.createRange(caseSection.getStartTagNameOpenOffset(),
					caseSection.getStartTagNameCloseOffset(), template);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.MissingParameter,
					caseSection.getTag());
			diagnostics.add(diagnostic);
			return;
		}
		Parameter firstParameter = parameters.get(0);
		// Iterate through children and ensure the type of the #case or #is parts are
		// the same as the parent section
		for (int i = 0; i < parameters.size(); i++) {
			Parameter parameter = parameters.get(i);
			String parameterName = parameter.getName();
			CaseOperator caseOperator = caseSection.getCaseOperator();
			if (caseOperator == null) {
				if (i > 0) {
					// Ex: {#is ON OFF} --> no operator implies only 1 parameter is allowed
					Range range = QutePositionUtility.createRange(firstParameter);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.InvalidOperator, firstParameter.getName(), caseSection.getTag(),
							caseSection.getAllowedOperators() //
									.stream() //
									.map(Operator::getName) //
									.collect(Collectors.joining(", ", "[", "]")));
					diagnostics.add(diagnostic);
					return;
				}
			} else {
				if (i == 0) {
					continue;
				} else if (i > 1 && !caseOperator.isMulti()) {
					// Ex: {#is ne ON OFF} --> operator 'ne' only allows 1 parameter
					Range range = QutePositionUtility.createRange(parameter);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.UnexpectedParameter, parameterName, firstParameter.getName(),
							caseSection.getTag());
					diagnostics.add(diagnostic);
					continue;
				}
			}
			String caseJavaType = LiteralSupport.getLiteralJavaType(parameter.getValue());
			if (caseJavaType == null && project.findMember(whenType, parameterName) == null) {
				if (i == 0 && parameters.size() > 1) {
					// The first parameter is invalid: either invalid Enum value or unknown operator
					// Ex: {#is XXX ON}
					Range range = QutePositionUtility.createRange(parameter);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.InvalidOperator, parameterName, caseSection.getTag(),
							caseSection.getAllowedOperators() //
									.stream() //
									.map(Operator::getName) //
									.collect(Collectors.joining(", ", "[", "]")));
					diagnostics.add(diagnostic);
					return;
				}
				// Case of Enum or invalid value since it is not a literal java type
				// {#when machine.status} --> machine.status is has values ON and OFF
				// {#is ON} --> valid enum value
				// {#is in BAD_ENUM_VAUE} --> Error: unexpected enum value
				// {/when}
				Range range = QutePositionUtility.createRange(parameter);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.UnexpectedValueInCaseSection, parameterName, expression, whenTypeName);
				diagnostics.add(diagnostic);
			} else if (caseJavaType != null && !whenTypeName.equals(caseJavaType)) {
				if (i == 0 && parameters.size() > 1) {
					// The first parameter is invalid: either unexpected type or unknown operator
					// Ex: {#is XXX 'John'}
					Range range = QutePositionUtility.createRange(parameter);
					Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
							QuteErrorCode.InvalidOperator, parameterName, caseSection.getTag(),
							caseSection.getAllowedOperators() //
									.stream() //
									.map(Operator::getName) //
									.collect(Collectors.joining(", ", "[", "]")));
					diagnostics.add(diagnostic);
					return;
				}
				// Case where the java type of expression of child is different from parent
				// Ex: {#switch person.name} --> person.name is of type String
				// {#case 'John'} --> valid expression type of String
				// {#case 123} --> Error: unexpected type, expected String but was Integer
				// {/switch}
				Range range = QutePositionUtility.createRange(parameter);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.UnexpectedMemberTypeInCaseSection, caseJavaType, expression, whenTypeName);
				diagnostics.add(diagnostic);
			}
		}
	}
}
