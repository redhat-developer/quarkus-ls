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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with #let section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithLetSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"\r\n" + //
				"{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {negDouble}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#set name=item.name age=10 long=10L negDouble=-10D isActive=true simpleQuote='abcd' doubleQuote=\"efgh\"}\r\n"
				+ //
				"  {true}\r\n" + //
				"  {name}\r\n" + //
				"  {negDouble}\r\n" + //
				"  {isActive}\r\n" + //
				"  {simpleQuote}\r\n" + //
				"  {doubleQuote}\r\n" + //
				"{/set}\r\n" + //
				"";
		Diagnostic d = d(0, 11, 0, 15, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d, //
				d(2, 3, 2, 7, QuteErrorCode.UnknownType, "`name` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d, te(0, 15, 0, 15, "??")));

	}

	@Test
	public void autoClose() throws Exception {
		String template = "{#let name='value' }\r\n" + //
				" {name}\r\n" + //
				"{/let}";
		testDiagnosticsFor(template);

		template = "{#let name='value' /}\r\n" + //
				" {name}\r\n" + //
				"{/let}";
		Diagnostic d = d(1, 2, 1, 6, QuteErrorCode.UndefinedObject, "`name` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, //
				d(2, 1, 2, 5, QuteSyntaxErrorCode.SECTION_START_NOT_FOUND,
						"Parser error: section start tag not found for {/let}", DiagnosticSeverity.Error), //
				d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String name}\r\n")),
				ca(d, te(1, 6, 1, 6, "??")));

	}

	@Test
	public void notAsObjectPartName() throws Exception {
		String template = "{@int !value}\r\n" + //
				"{#let name=!value /}";
		testDiagnosticsFor(template);
	}
}
