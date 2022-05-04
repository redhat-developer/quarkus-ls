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
package com.redhat.qute.services.diagnostics;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.ls.commons.CodeActionFactory;

public enum QuteErrorCode implements IQuteErrorCode {

	// Error code for resolving Java type
	ResolvingJavaType("Resolving Java type `{0}`."), //

	// Error code for namespaces
	UndefinedNamespace("No namespace resolver found for: `{0}`."), //

	// Error code for operator
	InvalidOperator("Invalid `{0}` operator for section `#{1}`. Allowed operators are `{2}`."), //

	// Error code for object part
	UndefinedObject("`{0}` cannot be resolved to an object."), //
	UnknownType("`{0}` cannot be resolved to a type."), //

	// Error codes for property part
	UnknownProperty("`{0}` cannot be resolved or is not a field of `{1}` Java type."), //
	PropertyNotSupportedInNativeMode("Property `{0}` of `{1}` Java type cannot be used in native image mode."), //
	InheritedPropertyNotSupportedInNativeMode(
			"Inherited property `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`."), //
	ForbiddenByRegisterForReflectionFields(
			"The field `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(fields = false)`."), //
	PropertyIgnoredByTemplateData(
			"The property `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignore = {2})`."), //

	// Error code for method part
	UnknownMethod("`{0}` cannot be resolved or is not a method of `{1}` Java type."), //
	UnknownNamespaceResolverMethod("`{0}` cannot be resolved or is not a method of `{1}` namespace resolver."), //
	InvalidMethodVoid("Invalid `{0}` method of `{1}` : void return is not allowed."), //
	InvalidMethodFromObject("Invalid `{0}` method of `{1}` : method from `java.lang.Object` is not allowed."), //
	InvalidMethodStatic("Invalid `{0}` method of `{1}` : static method is not allowed."), //
	InvalidMethodParameter("The method `{0}` in the type `{1}` is not applicable for the arguments `{2}`."), //
	InvalidVirtualMethod(
			"The virtual method `{0}` in the type `{1}` is not applicable for the base object `{2}` type."), //
	InfixNotationParameterRequired("A parameter for the infix notation method `{0}` is required."), //
	InvalidMethodInfixNotation(
			"The method `{0}` cannot be used with infix notation, because it does not have exactly `1` parameter."), //
	MethodNotSupportedInNativeMode("Method `{0}` of `{1}` Java type cannot be used in native image mode."), //
	InheritedMethodNotSupportedInNativeMode(
			"Inherited method `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignoreSuperclasses = true)`."), //
	ForbiddenByRegisterForReflectionMethods(
			"The method `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@RegisterForReflection(methods = false)`."), //
	ForbiddenByTemplateDataProperties(
			"The method `{0}` of `{1}` Java type cannot be used in native image mode because it has `{2}` parameters and Java type is annotated with `@TemplateData(properties = true)`."), //
	MethodIgnoredByTemplateData(
			"The method `{0}` of `{1}` Java type cannot be used in native image mode because Java type is annotated with `@TemplateData(ignore = {2})`."), //

	// Error code for #for / #each section
	IterationError("Iteration error: '{'{0}'}' resolved to [{1}] which is not iterable."),

	// Error code for #include section
	TemplateNotFound("Template not found: `{0}`."), //
	TemplateNotDefined("Template id must be defined as parameter."),

	UndefinedSectionTag("No section helper found for `{0}`."), //

	SyntaxError("Syntax error: `{0}`.");

	private final String rawMessage;

	QuteErrorCode(String rawMessage) {
		this.rawMessage = rawMessage;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String toString() {
		return getCode();
	}

	@Override
	public String getRawMessage() {
		return rawMessage;
	}

	public boolean isQuteErrorCode(Either<String, Integer> code) {
		return CodeActionFactory.isDiagnosticCode(code, name());
	}

	public static QuteErrorCode getErrorCode(Either<String, Integer> diagnosticCode) {
		if (diagnosticCode == null || diagnosticCode.isRight()) {
			return null;
		}
		String code = diagnosticCode.getLeft();
		try {
			return valueOf(code);
		} catch (Exception e) {
			return null;
		}
	}
}
