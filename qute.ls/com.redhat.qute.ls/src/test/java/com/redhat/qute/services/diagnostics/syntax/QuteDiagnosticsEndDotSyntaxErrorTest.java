/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.syntax;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;

/**
 * Syntax error with end dot.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsEndDotSyntaxErrorTest {

	@Test
	public void objectPartEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.}";
		testDiagnosticsFor(template, d(1, 6, 1, 7, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void objectPartInInfixNotationEndsWithDot() {
		String template = "{@java.lang.String name}\r\n" + //
				"{name. ?: \"Qute\"}";
		testDiagnosticsFor(template, d(1, 5, 1, 6, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void objectPartInInfixNotationEndsWithDot2() {
		String template = "{@java.lang.String name}\r\n" + //
				"{name ?: name.}";
		testDiagnosticsFor(template, d(1, 13, 1, 14, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void objectPartInMethodParamEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{@int index}\r\n" + //
				"{items.get(index.)}";
		testDiagnosticsFor(template, d(2, 16, 2, 17, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void propertyPartEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size.}";
		testDiagnosticsFor(template, d(1, 11, 1, 12, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void propertyPartInMethodParamEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.get(items.size.)}";
		testDiagnosticsFor(template, d(1, 21, 1, 22, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}

	@Test
	public void methodPartEndsWithDot() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.size().}";
		testDiagnosticsFor(template, d(1, 13, 1, 14, QuteSyntaxErrorCode.UNEXPECTED_TOKEN, //
				"Syntax error: `Unexpected '.' token`.", DiagnosticSeverity.Error));
	}
}
