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

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test diagnostics and code actions with #form section
 *
 */
public class QuteDiagnosticsWithFormSectionTest {

	@Test
	public void missingInputs() throws Exception {
		String template = "{#form uri:Login.manualLogin() id=\"login\"}\r\n" + //
				"{/form}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.MissingExpectedInput,
				"Missing expected input(s): `userName`.", DiagnosticSeverity.Warning);
		d.setData(new JavaBaseTypeOfPartData("rest.Login"));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 42, 1, 0, //
						"\r\n  <input name=\"userName\" >\r\n")), //
				ca(d, te(0, 42, 1, 0, //
						"\r\n  <input name=\"password\" >\r\n" + //
								"  <input name=\"userName\" >\r\n")));
	}

	@Test
	public void missingInputsWithIndent() throws Exception {
		String template = "  {#form uri:Login.manualLogin() id=\"login\"}\r\n" + //
				"  {/form}";
		Diagnostic d = d(0, 3, 0, 8, QuteErrorCode.MissingExpectedInput,
				"Missing expected input(s): `userName`.", DiagnosticSeverity.Warning);
		d.setData(new JavaBaseTypeOfPartData("rest.Login"));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 44, 1, 2, //
						"\r\n    <input name=\"userName\" >\r\n  ")), //
				ca(d, te(0, 44, 1, 2, //
						"\r\n    <input name=\"password\" >\r\n" + //
								"    <input name=\"userName\" >\r\n  ")));
	}

	@Test
	public void missingInputsWithTab() throws Exception {
		String template = "{#form uri:Login.manualLogin() id=\"login\"}\r\n" + //
				"\t<input name=\"password\" >\r\n" + //
				"{/form}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.MissingExpectedInput,
				"Missing expected input(s): `userName`.", DiagnosticSeverity.Warning);
		d.setData(new JavaBaseTypeOfPartData("rest.Login"));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 42, 2, 0, //
						"\r\n\t<input name=\"userName\" >\r\n")), //
				ca(d, te(0, 42, 2, 0, //
						"\r\n\t<input name=\"password\" >\r\n" + //
								"\t<input name=\"userName\" >\r\n")));
	}

	@Test
	public void missingInputsWithOptional() throws Exception {
		String template = "{#form uri:Login.manualLogin() id=\"login\"}\r\n" + //
				"  <input name=\"password\" >\r\n" + //
				"{/form}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.MissingExpectedInput,
				"Missing expected input(s): `userName`.", DiagnosticSeverity.Warning);
		d.setData(new JavaBaseTypeOfPartData("rest.Login"));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 42, 2, 0, //
						"\r\n  <input name=\"userName\" >\r\n")), //
				ca(d, te(0, 42, 2, 0, //
						"\r\n  <input name=\"password\" >\r\n" + //
								"  <input name=\"userName\" >\r\n")));
	}

	@Test
	public void noMissingRequiredInputs() throws Exception {
		String template = "{#form uri:Login.manualLogin() id=\"login\"}" + //
				"  <input name=\"userName\" >" + //
				"{/form}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noMissingRequiredInputsComplete() throws Exception {
		String template = "{#form uri:Login.complete()}\r\n" + //
				"{/form}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.MissingExpectedInput,
				"Missing expected input(s): `firstName`, `lastName`, `userName`.", DiagnosticSeverity.Warning);
		d.setData(new JavaBaseTypeOfPartData("rest.Login"));
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 28, 1, 0, //
						"\r\n  <input name=\"firstName\" >\r\n" + //
								"  <input name=\"lastName\" >\r\n" + //
								"  <input name=\"userName\" >\r\n")), //
				ca(d, te(0, 28, 1, 0, //
						"\r\n  <input name=\"confirmationCode\" >\r\n" + //
								"  <input name=\"firstName\" >\r\n" + //
								"  <input name=\"lastName\" >\r\n" + //
								"  <input name=\"password\" >\r\n" + //
								"  <input name=\"password2\" >\r\n" + //
								"  <input name=\"userName\" >\r\n")));
	}

}
