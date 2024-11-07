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
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;

/**
 * Qute diagnostics with user tags.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithUserTagTest {

	@Test
	public void bundleStyle() {
		// test with default value declared in bundleStyle.html user tag
		// {#let name?="main.css"}
		// In this case:

		// - name is optional
		String template = "{#bundleStyle /}";
		testDiagnosticsFor(template);
		// - name can be overridden
		template = "{#bundleStyle name='foo.css'/}";
		testDiagnosticsFor(template);
	}

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
	public void ga4() {
		String template = "{#ga4 /}";
		testDiagnosticsFor(template);
	}
	
	@Test
	public void definedAllParameterNamesWithoutAssignment() {
		String template = "{@java.lang.String name}\n" + //
				"{@java.lang.String id}\n" + //
				"{#input name value='' id /}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedParameterName() {
		String template = "{#input name='' XXXX='' /}";
		Diagnostic d = d(0, 16, 0, 20, QuteErrorCode.UndefinedParameter,
				"No parameter `XXXX` found for `input` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void ignoreUndefinedParameterNameWhenArgs() throws Exception {
		// tagWithArgs.html contains an expression which uses _args
		// in this case, we ignore the QuteErrorCode.UndefinedParameter error.
		String template = "{#tagWithArgs name='' XXXX='' /}";

		// There is no error with name (just an error with foo parameter which is
		// required)
		Diagnostic d = d(0, 1, 0, 13, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `foo` of `tagWithArgs` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 29, 0, 30, //
						" foo=\"foo\"")));
	}

	@Test
	public void duplicateNameParameter() {
		String template = "{#input name='' name='' /}";
		Diagnostic d = d(0, 16, 0, 20, QuteErrorCode.DuplicateParameter,
				"Duplicate parameter `name` of `input` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void missingRequiredParameterName() throws Exception {
		String template = "{#input /}";
		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `name` of `input` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 7, 0, 8, //
						" name=\"name\"")));
	}

	@Test
	public void missingRequiredItParameterName() {
		String template = "{#form /}";
		Diagnostic d = d(0, 1, 0, 6, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `it` of `form` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void validItParameter() {
		String template = "{#form uri:Login.confirm('ok') /}";
		testDiagnosticsFor(template);
	}

	@Test
	public void duplicateItParameter() {
		String template = "{#form uri:Login.confirm('ok') uri:Login.confirm('ok') /}";
		Diagnostic d = d(0, 31, 0, 54, QuteErrorCode.DuplicateParameter,
				"Duplicate parameter `it` of `form` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void duplicateItParameter2() {
		String template = "{@java.lang.String login}\n" + //
				"{@java.lang.String login}\n" + //
				"{#form login confirm /}";
		Diagnostic d = d(2, 13, 2, 20, QuteErrorCode.DuplicateParameter,
				"Duplicate parameter `it` of `form` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void missingRequiredParameterNameMultiple() throws Exception {
		String template = "{#inputRequired /}";
		Diagnostic d = d(0, 1, 0, 15, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `name`, `class` of `inputRequired` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 15, 0, 16, //
						" name=\"name\" class=\"class\"")));
	}

	@Test
	public void missingRequiredParameterNameMultipleWithExisting() throws Exception {
		String template = "{#inputRequired name=\"name\"/}";
		Diagnostic d = d(0, 1, 0, 15, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `class` of `inputRequired` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 27, 0, 27, //
						" class=\"class\"")));
	}

	// https://github.com/redhat-developer/quarkus-ls/issues/841
	@Test
	public void missingRequiredParameterNestedContent() throws Exception {
		String template = "{#formElement}\r\n" + //
				"  Hello!\r\n" + //
				"{/}";
		Diagnostic d = d(0, 1, 0, 13, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `name`, `label` of `formElement` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 13, 0, 13, //
						" name=\"name\" label=\"label\"")));
	}

	@Test
	public void missingRequiredParameterIt() throws Exception {
		String template = "{#myTag required=\"required\"}\r\n" + //
				"{/}";
		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `it` of `myTag` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 27, 0, 27, //
						" \"\"")));
	}

	// https://github.com/redhat-developer/quarkus-ls/issues/856
	@Test
	public void missingRequiredParameterWithOptionalParamter() throws Exception {
		String template = "{#myTag required=\"required\" optional=\"optional\"}\r\n" + //
				"{/}";
		Diagnostic d = d(0, 1, 0, 7, QuteErrorCode.MissingRequiredParameter,
				"Missing required parameter(s) `it` of `myTag` user tag.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 47, 0, 47, //
						" \"\"")));
	}

	@Test
	public void noMatchBetweenStartEndSection() throws Exception {
		String template = "{#form }\r\n"
				+ "	\r\n"
				+ "	{/for}";
		Diagnostic d1 = d(2, 2, 2, 6, QuteSyntaxErrorCode.SECTION_END_DOES_NOT_MATCH_START,
				"Parser error: section end tag [for] does not match the start tag [form]",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d1, //
				d(0, 1, 0, 6, QuteErrorCode.MissingRequiredParameter,
						"Missing required parameter(s) `it` of `form` user tag.",
						DiagnosticSeverity.Warning));
		testCodeActionsFor(template, d1, //
				ca(d1, te(2, 3, 2, 6, //
						"form")));
	}
}
