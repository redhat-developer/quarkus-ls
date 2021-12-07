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
	UndefinedNamespace("No namespace resolver found for: `{0}`"), //

	// Error code for object, property, method parts
	UndefinedVariable("`{0}` cannot be resolved to a variable."), //
	UnkwownType("`{0}` cannot be resolved to a type."), //
	UnkwownMethod("`{0}` cannot be resolved or is not a method of `{1}` Java type."), //
	InvalidMethodVoid("Invalid `{0}` method of `{1}` : void return is not allowed."), //
	InvalidMethodFromObject("Invalid `{0}` method of `{1}` : method from `java.lang.Object` is not allowed."), //
	InvalidMethodStatic("Invalid `{0}` method of `{1}` : static method is not allowed."), //
	
	UnkwownProperty("`{0}` cannot be resolved or is not a field of `{1}` Java type."), //

	// Error code for #for / #each section
	NotInstanceOfIterable("`{0}` is not an instance of `java.lang.Iterable`."),

	// Error code for #include section
	TemplateNotFound("Template not found: `{0}`."), //
	TemplateNotDefined("Template id must be defined as parameter."),

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
}
