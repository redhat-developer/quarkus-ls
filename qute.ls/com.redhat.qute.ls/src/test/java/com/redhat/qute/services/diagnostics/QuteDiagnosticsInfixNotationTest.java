/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test with infix notation.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInfixNotationTest {

	@Test
	public void inValidMethodForInfix() throws Exception {
		// Test with codePointCount(beginIndex : int,endIndex : int) : int
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo codePointCount 1}";
		testDiagnosticsFor(template, //
				d(1, 5, 1, 19, QuteErrorCode.InvalidMethodInfixNotation,
						"The method `codePointCount` cannot be used with infix notation, because it does not have exactly `1` parameter.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void validMethodForInfixWithOneSignatures() throws Exception {
		// Test with charAt(index : int) : char
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo charAt 1}";
		testDiagnosticsFor(template);

		template = "{@java.lang.String foo}\r\n" + //
				"{foo charAt}"; //
		testDiagnosticsFor(template, //
				d(1, 5, 1, 11, QuteErrorCode.InfixNotationParameterRequired,
						"A parameter for the infix notation method `charAt` is required.", DiagnosticSeverity.Error));

		template = "{@java.lang.String foo}\r\n" + //
				"{foo charAt '1'}"; //
		testDiagnosticsFor(template, //
				d(1, 5, 1, 11, QuteErrorCode.InvalidMethodParameter,
						"The method `charAt(int)` in the type `String` is not applicable for the arguments `(String)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void validMethodForInfixWithSeveralSignatures() throws Exception {
		// Tests with getBytes which have several signature
		// - getBytes() : byte[]
		// " getBytes(charsetName : java.lang.String) : byte[]

		String template = "{@java.lang.String foo}\r\n" + //
				"{foo getBytes '1'}"; //
		testDiagnosticsFor(template);

		template = "{@java.lang.String foo}\r\n" + //
				"{foo getBytes}"; //
		testDiagnosticsFor(template, //
				d(1, 5, 1, 13, QuteErrorCode.InfixNotationParameterRequired,
						"A parameter for the infix notation method `getBytes` is required.", DiagnosticSeverity.Error));

		template = "{@java.lang.String foo}\r\n" + //
				"{foo getBytes()}";
		testDiagnosticsFor(template, //
				d(1, 5, 1, 15, QuteErrorCode.InfixNotationParameterRequired,
						"A parameter for the infix notation method `getBytes()` is required.", DiagnosticSeverity.Error));

		template = "{@java.lang.String foo}\r\n" + //
				"{foo getBytes 1}"; //
		testDiagnosticsFor(template, //
				d(1, 5, 1, 13, QuteErrorCode.InvalidMethodParameter,
						"The method `getBytes(String)` in the type `String` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void undefinedMethodParameter() throws Exception {
		// Infix notation
		String template = "{mom or dad}"; //
		Diagnostic d1 = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, "`mom` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d1.setData(DiagnosticDataFactory.createUndefinedObjectData("mom", false));

		Diagnostic d2 = d(0, 8, 0, 11, QuteErrorCode.UndefinedObject, "`dad` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d2.setData(DiagnosticDataFactory.createUndefinedObjectData("dad", false));

		testDiagnosticsFor(template, //
				d1, d2);

		// NO Infix notation
		template = "{mom.or(dad)}"; //
		d1 = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, "`mom` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d1.setData(DiagnosticDataFactory.createUndefinedObjectData("mom", false));

		d2 = d(0, 8, 0, 11, QuteErrorCode.UndefinedObject, "`dad` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d2.setData(DiagnosticDataFactory.createUndefinedObjectData("dad", false));

		testDiagnosticsFor(template, //
				d1, d2);
	}
	
	@Test
	public void operators() throws Exception {
		// Even if there are spaces, the operators ||, are not considered as infix notation
		// see https://quarkus.io/guides/qute-reference#built-in-resolvers
		String template = "{mom || dad}"; //
		Diagnostic d1 = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, "`mom` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d1.setData(DiagnosticDataFactory.createUndefinedObjectData("mom", false));

		Diagnostic d2 = d(0, 8, 0, 11, QuteErrorCode.UndefinedObject, "`dad` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d2.setData(DiagnosticDataFactory.createUndefinedObjectData("dad", false));

		testDiagnosticsFor(template, //
				d1, d2);
	}
	
	@Test
	public void elvisOperator() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo ?: foo : 'word'}"; //
		testDiagnosticsFor(template);
		
		template = "{@java.lang.String foo}\r\n" + //
				"{foo :? foo : }"; //
		testDiagnosticsFor(template);
	}
	
	@Test
	public void twoParts() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo 'word'}"; //
		testDiagnosticsFor(template, //
				d(1, 5, 1, 11, QuteErrorCode.InfixNotationParameterRequired,
				"A parameter for the infix notation method `'word'` is required.", DiagnosticSeverity.Error));
	}
}
