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
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with #if section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithIfSectionTest {

	@Test
	public void definedObject() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if item.name != '' && item > 0}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void definedObjectInBracket() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if (item.name != '' && item > 0) && item > 0}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedObject() throws Exception {
		String template = "{#if item.name != '' && item > 0}\r\n" + //
				"{/if}";

		Diagnostic d1 = d(0, 5, 0, 9, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		Diagnostic d2 = d(0, 24, 0, 28, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d1, d2);
		testCodeActionsFor(template, d1, //
				ca(d1, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d1, te(0, 9, 0, 9, "??")), //
				ca(d1, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d1)));
		testCodeActionsFor(template, d2, //
				ca(d2, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d2, te(0, 28, 0, 28, "??")), //
				ca(d2, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d2)));
	}

	@Test
	public void invalidOperator() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if item.name XX '' && item > 0}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template, //
				d(1, 15, 1, 17, QuteErrorCode.InvalidOperator,
						"Invalid `XX` operator for section `#if`. Allowed operators are `[!,gt,>,ge,>=,lt,<,le,<=,eq,==,is,ne,!=,&&,and,||,or]`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void doubleEqualsOperator() throws Exception {
		String template = "{@java.lang.String one}\r\n" + //
				"{#if one == one}OK{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void conditionWithMethod() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{#if (foo.or(false) || false || true) && (true)}OK{/if}\r\n" + //
				"{#if foo.or(false) || false}OK{#else}NOK{/if}\r\n" + //
				"{#if false || (foo.or(false) || (false || true))}OK{#else}NOK{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void notOperatorWithObjectPart() {
		String template = "{#if !true}NOK{#else}OK{/if}";
		testDiagnosticsFor(template);

		template = "{#if !foo}NOK{#else}OK{/if}";
		testDiagnosticsFor(template, d(0, 6, 0, 9, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning));
	}
	
	@Test
	public void onlyNotOperator() {
		String template = "{#if !}{/if}";
		Diagnostic d = d(0, 0, 0, 0, QuteErrorCode.SyntaxError, "Unsupported param type: NOT",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void noOptionalParameterInIfBlock() {
		String template = "{#if foo}\r\n" + //
				"	{foo}\r\n" + //
				"{/if}";

		Diagnostic d1 = d(0, 5, 0, 8, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		Diagnostic d2 = d(1, 2, 1, 5, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d1, d2);
	}

	@Test
	public void optionalParameterInIfBlock() {
		String template = "{#if foo??}\r\n" + //
				"	{foo}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void optionalParameterInIfBlockWithNull() {
		String template = "{#let bar=null}\r\n" + //
				"    {#if bar??}\r\n" + //
				"        {bar}\r\n" + //
				"    {/if}\r\n" + //
				"{/let}";
		testDiagnosticsFor(template);
	}

}
