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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Qute diagnostics with user tags.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithUserTagTest {

	@Test
	public void definedRequiredParameterName() {
		String template = "{#input name='' /}";
		testDiagnosticsFor(template);
	}

	@Test
	public void definedAllParameterNames() {
		String template = "{#input name='' value='' id='' /}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedParameterName() {
		String template = "{#input name='' XXXX='' /}";
		Diagnostic d = d(0, 16, 0, 20, QuteErrorCode.UndefinedUserTagParameter,
				"No parameter `XXXX` found for `input` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void missingRequiredParameterName() {
		String template = "{#input /}";
		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.RequiredUserTagParameter,
				"Missing required parameter `name` of `input` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void missingRequiredItParameterName() {
		String template = "{#form /}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.RequiredUserTagParameter,
				"Missing required parameter `it` of `form` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void validItParameter() {
		String template = "{#form uri:Login.confirm('ok') /}";
		testDiagnosticsFor(template);
	}

	@Test
	public void tooManyItParameter() {
		String template = "{#form uri:Login.confirm('ok') uri:Login.confirm('ok') /}";
		Diagnostic d = d(0, 31, 0, 54, QuteErrorCode.TooManyUserTagItParameter,
				"Too many `it` parameter declared in `form` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}
}
