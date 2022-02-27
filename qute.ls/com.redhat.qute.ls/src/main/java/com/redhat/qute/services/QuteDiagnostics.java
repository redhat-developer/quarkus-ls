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

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.expression.PropertyPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.ParameterDeclaration;
import com.redhat.qute.parser.template.ParameterDeclaration.JavaTypeRangeOffset;
import com.redhat.qute.parser.template.RangeOffset;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.IncludeSection;
import com.redhat.qute.parser.template.sections.LoopSection;
import com.redhat.qute.project.JavaMemberResult;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.project.indexing.QuteIndex;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
import com.redhat.qute.services.diagnostics.QuteDiagnosticsForSyntax;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.services.diagnostics.ResolvingJavaTypeContext;
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

	private static final ResolvedJavaTypeInfo RESOLVING_JAVA_TYPE = new ResolvedJavaTypeInfo();

	private final JavaDataModelCache javaCache;

	private final QuteDiagnosticsForSyntax diagnosticsForSyntax;

	private class ResolutionContext extends HashMap<String, ResolvedJavaTypeInfo> {

		private static final long serialVersionUID = 1L;

		private final ResolutionContext parent;

		private ResolvedJavaTypeInfo withObject;

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

		public JavaMemberInfo findMemberWithObject(String property, String projectUri) {
			if (withObject != null) {
				if (withObject.isIterable() && projectUri != null) {
					String iterableType = withObject.getIterableType();
					ResolvedJavaTypeInfo resolvedIterableType = javaCache.resolveJavaType(iterableType, projectUri)
							.getNow(null);
					if (resolvedIterableType != null) {
						JavaMemberInfo member = javaCache.findMember(resolvedIterableType, property);
						if (member != null) {
							return member;
						}
					}
				} else {
					JavaMemberInfo member = javaCache.findMember(withObject, property);
					if (member != null) {
						return member;
					}
				}
			}
			// Search in parent context
			ResolutionContext parent = this.parent;
			while (parent != null) {
				JavaMemberInfo member = parent.findMemberWithObject(property, projectUri);
				if (member != null) {
					return member;
				}
				parent = parent.getParent();
			}
			return null;
		}

	}

	public QuteDiagnostics(JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
		this.diagnosticsForSyntax = new QuteDiagnosticsForSyntax();
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 *
	 * @param template           the Qute template.
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, CancelChecker cancelChecker) {
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
		try {
			diagnosticsForSyntax.validateWithRealQuteParser(template, diagnostics);
			validateDataModel(template, template, resolvingJavaTypeContext, new ResolutionContext(), diagnostics);
		} catch (CancellationException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while validating '" + template.getUri() + "'.", e);
		}
		cancelChecker.checkCanceled();
		return diagnostics;
	}

	private void validateDataModel(Node parent, Template template, ResolvingJavaTypeContext resolvingJavaTypeContext,
			ResolutionContext currentContext, List<Diagnostic> diagnostics) {
		ResolutionContext previousContext = currentContext;
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			switch (node.getKind()) {
			case ParameterDeclaration: {
				ParameterDeclaration parameter = (ParameterDeclaration) node;
				String javaTypeToResolve = parameter.getJavaType();
				if (!StringUtils.isEmpty(javaTypeToResolve)) {
					String projectUri = template.getProjectUri();
					if (projectUri != null) {
						List<JavaTypeRangeOffset> classNameRanges = parameter.getJavaTypeNameRanges();
						for (RangeOffset classNameRange : classNameRanges) {
							String className = template.getText(classNameRange);
							ResolvedJavaTypeInfo resolvedJavaType = resolveJavaType(className, projectUri,
									resolvingJavaTypeContext);
							if (resolvedJavaType == null) {
								// Java type doesn't exist
								Range range = QutePositionUtility.createRange(classNameRange, template);
								Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
										QuteErrorCode.UnknownType, className);
								diagnostics.add(diagnostic);
							} else if (!isResolvingJavaType(resolvedJavaType)) {
								currentContext.put(javaTypeToResolve, resolvedJavaType);
							}
						}
					}
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
				for (Parameter parameter : parameters) {
					Expression expression = parameter.getJavaTypeExpression();
					if (expression != null) {
						ResolvedJavaTypeInfo result = validateExpression(expression, section, template, previousContext,
								resolvingJavaTypeContext, diagnostics);
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
						default:
						}
					}
				}
				switch (section.getSectionKind()) {
				case INCLUDE:
					validateIncludeSection((IncludeSection) section, diagnostics);
					break;
				default:
					validateSectionTag(section, template, resolvingJavaTypeContext, diagnostics);
				}
				break;
			}
			case Expression: {
				validateExpression((Expression) node, null, template, previousContext, resolvingJavaTypeContext,
						diagnostics);
				break;
			}
			default:
			}
			validateDataModel(node, template, resolvingJavaTypeContext, currentContext, diagnostics);
		}
	}

	private static void validateSectionTag(Section section, Template template,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		String tagName = section.getTag();
		if (StringUtils.isEmpty(tagName)) {
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
					return;
				}

				// Check if section tag is a parameter from an include section
				Node parent = section.getParent();
				while (parent != null) {
					if (parent.getKind() == NodeKind.Section) {
						Section parentSection = (Section) parent;
						if (parentSection.getSectionKind() == SectionKind.INCLUDE) {
							IncludeSection includeSection = (IncludeSection) parentSection;
							List<QuteIndex> indexes = project
									.findInsertTagParameter(includeSection.getReferencedTemplateId(), tagName);
							if (indexes != null) {
								return;
							}
						}
					}
					parent = parent.getParent();
				}

				Range range = QutePositionUtility.selectStartTagName(section);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
						QuteErrorCode.UndefinedSectionTag, tagName);
				// Create data information helpful for code action
				diagnostic.setData(DiagnosticDataFactory.createUndefinedSectionTagData(tagName));
				diagnostics.add(diagnostic);
			}
		}
	}

	private static boolean canChangeContext(Section section) {
		SectionKind sectionKind = section.getSectionKind();
		return sectionKind == SectionKind.EACH || sectionKind == SectionKind.FOR || sectionKind == SectionKind.LET
				|| sectionKind == SectionKind.SET || sectionKind == SectionKind.WITH;
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
		} else {
			// #include doesn't define a template id
			// ex: {#include}
			Range range = QutePositionUtility.selectStartTagName(includeSection);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.TemplateNotDefined);
			diagnostics.add(diagnostic);
		}
	}

	private ResolvedJavaTypeInfo validateExpression(Expression expression, Section ownerSection, Template template,
			ResolutionContext resolutionContext, ResolvingJavaTypeContext resolvingJavaTypeContext,
			List<Diagnostic> diagnostics) {
		try {
			String projectUri = template.getProjectUri();
			String literalJavaType = expression.getLiteralJavaType();
			if (literalJavaType != null) {
				// The expression is a literal:
				// - {'abcd'} : string literal
				// - {true} : boolean literal
				// - {null} : null literal
				// - {123} : integer literal
				ResolvedJavaTypeInfo resolvedLiteralType = javaCache.resolveJavaType(literalJavaType, projectUri)
						.getNow(null);
				if (resolvedLiteralType == null) {
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
					resolvedJavaType = validateExpressionParts(parts, ownerSection, template, projectUri,
							resolutionContext, resolvingJavaTypeContext, diagnostics);
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
			String projectUri, ResolutionContext resolutionContext, ResolvingJavaTypeContext resolvingJavaTypeContext,
			List<Diagnostic> diagnostics) {
		ResolvedJavaTypeInfo resolvedJavaType = null;
		String namespace = null;
		for (int i = 0; i < parts.getChildCount(); i++) {
			Part current = ((Part) parts.getChild(i));

			if (current.isLast()) {
				// It's the last part, check if it is not ended with '.'
				int end = current.getEnd();
				String text = template.getText();
				if (end < text.length()) {
					char c = text.charAt(end);
					if (c == '.') {
						Range range = QutePositionUtility.createRange(end, end + 1, template);
						Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
								QuteErrorCode.SyntaxError, "Unexpected '.' token.");
						diagnostics.add(diagnostic);
					}
				}
			}

			switch (current.getPartKind()) {

			case Namespace: {
				NamespacePart namespacePart = (NamespacePart) current;
				namespace = validateNamespace(namespacePart, projectUri, resolvingJavaTypeContext, diagnostics);
				if (namespace == null) {
					// Invalid namespace
					return null;
				}
				break;
			}

			case Object: {
				ObjectPart objectPart = (ObjectPart) current;
				resolvedJavaType = validateObjectPart(namespace, objectPart, ownerSection, template, projectUri,
						resolutionContext, diagnostics, resolvingJavaTypeContext);
				if (resolvedJavaType == null) {
					// The Java type of the object part cannot be resolved, stop the validation of
					// property, method.
					return null;
				}
				break;
			}

			case Method:
			case Property: {
				// java.util.List<org.acme.Item>
				ResolvedJavaTypeInfo iter = resolvedJavaType;
				if (resolvedJavaType != null && resolvedJavaType.isIterable() && !resolvedJavaType.isArray()) {
					// Expression uses iterable type
					// {@java.util.List<org.acme.Item items>
					// {items.size()}
					// Property, method to validate must be done for iterable type (ex :
					// java.util.List
					String iterableType = resolvedJavaType.getIterableType();
					resolvedJavaType = resolveJavaType(iterableType, projectUri, resolvingJavaTypeContext);
					if (resolvedJavaType == null || isResolvingJavaType(resolvedJavaType)) {
						// The java type doesn't exists or it is resolving, stop the validation
						return null;
					}
				}

				resolvedJavaType = validateMemberPart(current, ownerSection, template, projectUri, resolutionContext,
						resolvedJavaType, iter, diagnostics, resolvingJavaTypeContext);
				if (resolvedJavaType == null) {
					// The Java type of the previous part cannot be resolved, stop the validation of
					// followings property, method.
					return null;
				}
				break;
			}
			}
		}
		return resolvedJavaType;
	}

	private ResolvedJavaTypeInfo resolveJavaType(String javaType, String projectUri,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		CompletableFuture<ResolvedJavaTypeInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(javaType,
				projectUri);
		ResolvedJavaTypeInfo resolvedJavaType = resolvingJavaTypeFuture.getNow(RESOLVING_JAVA_TYPE);
		if (isResolvingJavaType(resolvedJavaType)) {
			LOGGER.log(Level.INFO, QuteErrorCode.ResolvingJavaType.getMessage(javaType));
			resolvingJavaTypeContext.add(resolvingJavaTypeFuture);
			return RESOLVING_JAVA_TYPE;
		}
		return resolvedJavaType;
	}

	private boolean isResolvingJavaType(ResolvedJavaTypeInfo resolvedJavaClass) {
		return RESOLVING_JAVA_TYPE.equals(resolvedJavaClass);
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
	private String validateNamespace(NamespacePart namespacePart, String projectUri,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		String namespace = namespacePart.getPartName();
		if (NamespacePart.DATA_NAMESPACE.equals(namespace)) {
			return namespace;
		}
		if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
			// The data model is not loaded, ignore the error of undefined namespace
			return null;
		}

		if (projectUri != null) {
			if (!javaCache.hasNamespace(namespace, projectUri)) {
				Range range = QutePositionUtility.createRange(namespacePart);
				Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
						QuteErrorCode.UndefinedNamespace, namespacePart.getPartName());
				diagnostics.add(diagnostic);
				return null;
			}
		}
		return namespace;
	}

	private ResolvedJavaTypeInfo validateObjectPart(String namespace, ObjectPart objectPart, Section ownerSection,
			Template template, String projectUri, ResolutionContext resolutionContext, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		JavaMemberInfo javaMember = resolutionContext.findMemberWithObject(objectPart.getPartName(), projectUri);
		if (javaMember != null) {
			ResolvedJavaTypeInfo resolvedJavaType = resolveJavaType(javaMember.getJavaElementType(), projectUri,
					resolvingJavaTypeContext);
			if (isResolvingJavaType(resolvedJavaType)) {
				return null;
			}
			return resolvedJavaType;
		}

		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			if (!resolvingJavaTypeContext.isDataModelTemplateResolved()) {
				// The data model is not loaded, ignore the error of undefined variable
				return null;
			}

			if (UserTagUtils.isUserTag(template)) {
				// Ignore undefined variable diagnostic for user tag
				return null;
			}

			// ex : {item} --> undefined variable
			Range range = QutePositionUtility.createRange(objectPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning, QuteErrorCode.UndefinedVariable,
					objectPart.getPartName());
			// Create data information helpful for code action
			diagnostic.setData(DiagnosticDataFactory.createUndefinedVariableData(objectPart.getPartName(),
					ownerSection != null && ownerSection.isIterable()));
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
						ResolvedJavaTypeInfo alias = javaCache.resolveJavaType(lastPart, projectUri)
								.getNow(RESOLVING_JAVA_TYPE);
						if (isResolvingJavaType(alias)) {
							return null;
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
		return validateJavaTypePart(objectPart, ownerSection, projectUri, diagnostics, resolvingJavaTypeContext,
				javaTypeToResolve);
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
			String projectUri, ResolutionContext resolutionContext, ResolvedJavaTypeInfo baseType,
			ResolvedJavaTypeInfo iterableOfType, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		if (part.getPartKind() == PartKind.Method) {
			// Validate method part
			// ex : {foo.method(1, 2)}
			return validateMethodPart((MethodPart) part, ownerSection, template, projectUri, resolutionContext,
					baseType, iterableOfType, diagnostics, resolvingJavaTypeContext);
		}
		// Validate property part
		// ex : {foo.property}
		return validatePropertyPart((PropertyPart) part, ownerSection, template, projectUri, resolutionContext,
				baseType, iterableOfType, diagnostics, resolvingJavaTypeContext);
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
	 * @param diagnostics              the diagnostic list to fill.
	 * @param resolvingJavaTypeContext the resolving Java type context.
	 * 
	 * @return the Java type returned by the member part and null otherwise.
	 */
	private ResolvedJavaTypeInfo validatePropertyPart(PropertyPart part, Section ownerSection, Template template,
			String projectUri, ResolutionContext resolutionContext, ResolvedJavaTypeInfo resolvedJavaType,
			ResolvedJavaTypeInfo iterableOfType, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {
		JavaMemberInfo javaMember = javaCache.findMember(part, resolvedJavaType, projectUri);
		if (javaMember == null) {
			// ex : {@org.acme.Item item}
			// "{item.XXXX}
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnknownProperty,
					part.getPartName(), resolvedJavaType.getSignature());
			diagnostics.add(diagnostic);
			return null;
		}
		String memberType = javaMember.resolveJavaElementType(iterableOfType);
		return validateJavaTypePart(part, ownerSection, projectUri, diagnostics, resolvingJavaTypeContext, memberType);
	}

	/**
	 * Validate the given method part.
	 * 
	 * @param part                     the method part to validate.
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
	private ResolvedJavaTypeInfo validateMethodPart(MethodPart methodPart, Section ownerSection, Template template,
			String projectUri, ResolutionContext resolutionContext, ResolvedJavaTypeInfo resolvedJavaType,
			ResolvedJavaTypeInfo iter, List<Diagnostic> diagnostics,
			ResolvingJavaTypeContext resolvingJavaTypeContext) {

		// Validate parameters of the method part
		boolean undefinedType = false;
		List<ResolvedJavaTypeInfo> parameterTypes = new ArrayList<>();
		for (Parameter parameter : methodPart.getParameters()) {
			ResolvedJavaTypeInfo result = null;
			Expression expression = parameter.getJavaTypeExpression();
			if (expression != null) {
				result = validateExpression(expression, ownerSection, template, resolutionContext,
						resolvingJavaTypeContext, diagnostics);
			}
			if (result == null) {
				undefinedType = true;
			}
			parameterTypes.add(result);
		}
		if (undefinedType) {
			// One of parameter cannot be resolved as type, teh validation is stopped
			return null;
		}

		// All parameters are resolved, validate the existing of method name according
		// to the computed parameter types

		String methodName = methodPart.getPartName();
		String namespace = methodPart.getNamespace();
		JavaMemberResult result = javaCache.findMethod(resolvedJavaType, namespace, methodName, parameterTypes,
				projectUri);
		JavaMethodInfo method = (JavaMethodInfo) result.getMember();
		if (method == null) {
			QuteErrorCode errorCode = QuteErrorCode.UnknownMethod;
			String arg = null;
			if (namespace != null) {
				// ex :{config.getXXXX()}
				errorCode = QuteErrorCode.UnkwownNamespaceResolverMethod;
				arg = namespace;
			} else {
				// ex : {@org.acme.Item item}
				// {item.getXXXX()}
				arg = resolvedJavaType.getSignature();
				InvalidMethodReason reason = javaCache.getInvalidMethodReason(methodName, resolvedJavaType, projectUri);
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
			}
			Range range = QutePositionUtility.createRange(methodPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, errorCode, methodName, arg);
			diagnostics.add(diagnostic);
			return null;
		}

		boolean matchVirtualMethod = result.isMatchVirtualMethod();
		if (!matchVirtualMethod) {

			Range range = QutePositionUtility.createRange(methodPart);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error,
					QuteErrorCode.InvalidVirtualMethod, //
					method.getName(), method.getSimpleSourceType(), //
					resolvedJavaType.getJavaElementSimpleType());
			diagnostics.add(diagnostic);
			return null;
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

		String memberType = method.resolveJavaElementType(iter);
		return validateJavaTypePart(methodPart, ownerSection, projectUri, diagnostics, resolvingJavaTypeContext,
				memberType);
	}

	private ResolvedJavaTypeInfo validateJavaTypePart(Part part, Section ownerSection, String projectUri,
			List<Diagnostic> diagnostics, ResolvingJavaTypeContext resolvingJavaTypeContext, String javaTypeToResolve) {
		if (StringUtils.isEmpty(javaTypeToResolve)) {
			Range range = QutePositionUtility.createRange(part);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.UnknownType,
					part.getPartName());
			diagnostics.add(diagnostic);
			return null;
		}

		if (projectUri == null) {
			return null;
		}

		CompletableFuture<ResolvedJavaTypeInfo> resolvingJavaTypeFuture = null;
		if (part.getPartKind() == PartKind.Object) {
			// Object part case.
			// - if expression is included inside a loop section (#for, etc), we need to get
			// the iterable of. If javaTypeToResolve= 'java.util.List<org.acme.Item>',we
			// must get 'org.acme.Item'.
			// - otherwise resolve the given java type to resolve
			resolvingJavaTypeFuture = javaCache.resolveJavaType(part, projectUri, false);
		} else {
			// Other part kind (property, method), resolve the given java type to resolve
			resolvingJavaTypeFuture = javaCache.resolveJavaType(javaTypeToResolve, projectUri);
		}
		ResolvedJavaTypeInfo resolvedJavaType = resolvingJavaTypeFuture.getNow(RESOLVING_JAVA_TYPE);
		if (isResolvingJavaType(resolvedJavaType)) {
			// Java type must be loaded.
			LOGGER.log(Level.INFO, QuteErrorCode.ResolvingJavaType.getMessage(javaTypeToResolve));
			resolvingJavaTypeContext.add(resolvingJavaTypeFuture);
			return null;
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
}
