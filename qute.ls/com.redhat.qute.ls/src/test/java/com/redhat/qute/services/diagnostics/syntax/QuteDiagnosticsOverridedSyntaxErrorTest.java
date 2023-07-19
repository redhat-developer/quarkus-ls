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

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.validator.QuteSyntaxErrorCode;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Syntax error which improves the error of the real Qute parser.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsOverridedSyntaxErrorTest {

	@Test
	public void UNTERMINATED_SECTION_with_let() throws Exception {
		String template = "{#let name='foo'}";
		testDiagnosticsFor(template);
	}

	@Test
	public void UNTERMINATED_SECTION_with_let_insideIf() throws Exception {
		String template = "{#if true}\r\n" + //
				"	{#let sandwich='salami'}\r\n" + //
				"{#else}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void UNTERMINATED_SECTION_with_include() throws Exception {
		String template = "{#include template}";
		Diagnostic d = d(0, 10, 0, 18, QuteErrorCode.TemplateNotFound,
				"Template not found: `template`.",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void UNTERMINATED_SECTION_with_insert() throws Exception {
		String template = "{#insert}";
		Diagnostic d = d(0, 1, 0, 8, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
				"Parser error: unterminated section [insert] detected",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
	}

	@Test
	public void UNTERMINATED_SECTION() throws Exception {
		String template = "{@java.util.List items}\r\n"
				+ "{#each items}\r\n" // <-- error
				+ "	\r\n"
				+ "{#each items}\r\n"
				+ "	\r\n"
				+ "{/each}\r\n";
		Diagnostic d = d(1, 1, 1, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
				"Parser error: unterminated section [each] detected",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(5, 7, 5, 7, //
						"\r\n{/each}")));
	}

	@Test
	public void severalUNTERMINATED_SECTION() throws Exception {
		String template = "{@java.util.List items}\r\n"
				+ "{#each items}\r\n" // <-- error
				+ "	\r\n"
				+ "{#each items}\r\n"
				+ "	\r\n"
				+ "{/each}\r\n"
				+ "{#each items}\r\n"; // <-- error
		Diagnostic d1 = d(1, 1, 1, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
				"Parser error: unterminated section [each] detected",
				DiagnosticSeverity.Error);
		Diagnostic d2 = d(6, 1, 6, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
				"Parser error: unterminated section [each] detected",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d1, d2);
		testCodeActionsFor(template, d1, //
				ca(d1, te(5, 7, 5, 7, //
						"\r\n{/each}")));
		testCodeActionsFor(template, d2, //
				ca(d2, te(6, 13, 6, 13, //
						"\r\n{/each}")));
	}

	@Test
	public void nestedUNTERMINATED_SECTION_with_content() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n"
				+ "{#if true}\r\n"
				+ "	\r\n"
				+ "  {#each items}\r\n" // <-- error
				+ "    {it.name}\r\n"
				+ "	\r\n"
				+ "{/if}\r\n";
		Diagnostic d = d(3, 3, 3, 8, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
				"Parser error: unterminated section [each] detected",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(4, 13, 4, 13, //
						"\r\n  {/each}")));
	}

	@Test
	public void SECTION_END_DOES_NOT_MATCH_START() throws Exception {
		String template = "{#for }\r\n"
				+ "	\r\n"
				+ "	{/each}"; // <-- error
		Diagnostic d = d(2, 2, 2, 7, QuteSyntaxErrorCode.SECTION_END_DOES_NOT_MATCH_START,
				"Parser error: section end tag [each] does not match the start tag [for]",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(2, 3, 2, 7, //
						"for")));
	}

	@Test
	public void NO_SECTION_NAME() {
		String template = "{#}"; // <-- error
		testDiagnosticsFor(template,
				d(0, 1, 0, 2, QuteSyntaxErrorCode.NO_SECTION_NAME,
						"Parser error: no section name declared for {#}",
						DiagnosticSeverity.Error));
	}

	@Test
	public void SECTION_START_NOT_FOUND() {
		String template = "{#if true}\r\n"
				+ "	Bye...\r\n"
				+ "{/if} \r\n"
				+ "	Hello \r\n"
				+ "{/if}"; // <-- error
		testDiagnosticsFor(template,
				d(4, 1, 4, 4, QuteSyntaxErrorCode.SECTION_START_NOT_FOUND,
						"Parser error: section start tag not found for {/if}",
						DiagnosticSeverity.Error));
	}

	@Test
	public void SECTION_START_NOT_FOUND_emptyEndSection() {
		String template = "	{#if asdf??}\r\n"
				+ "\r\n"
				+ "	{/}\r\n"
				+ "\r\n"
				+ "	{/}\r\n" // <-- error
				+ "\r\n"
				+ "	{/}\r\n" // <-- error
				+ "\r\n"
				+ "	{/}\r\n" // <-- error
				+ "";
		testDiagnosticsFor(template,
				d(4, 2, 4, 3, QuteSyntaxErrorCode.SECTION_START_NOT_FOUND,
						"Parser error: section start tag not found for {/}",
						DiagnosticSeverity.Error),
				d(6, 2, 6, 3, QuteSyntaxErrorCode.SECTION_START_NOT_FOUND,
						"Parser error: section start tag not found for {/}",
						DiagnosticSeverity.Error),
				d(8, 2, 8, 3, QuteSyntaxErrorCode.SECTION_START_NOT_FOUND,
						"Parser error: section start tag not found for {/}",
						DiagnosticSeverity.Error));
	}

	@Test
	public void SECTION_BLOCK_END_DOES_NOT_MATCH_START() throws Exception {
		String template = "{#if true}\r\n"
				+ "	Hello\r\n"
				+ "{#else}\r\n"
				+ "Hi\r\n"
				+ "{/elsa}\r\n" // <-- error
				+ "{/if}";
		Diagnostic d = d(4, 1, 4, 6, QuteSyntaxErrorCode.SECTION_BLOCK_END_DOES_NOT_MATCH_START,
				"Parser error: section block end tag [elsa] does not match the start tag [else]",
				DiagnosticSeverity.Error);
		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(4, 2, 4, 6, "if")));
	}

	@Test
	public void SECTION_BLOCK_END_DOES_NOT_MATCH_START_no_error() throws Exception {
		String template = "{#if true}\r\n"
				+ "	Hello\r\n"
				+ "{#else}\r\n"
				+ "Hi\r\n"
				+ "{/else}\r\n" // should not report error, need to fix in future PR
				+ "{/if}";
		testDiagnosticsFor(template, //
				d(4, 1, 4, 6, QuteSyntaxErrorCode.SECTION_BLOCK_END_DOES_NOT_MATCH_START,
						"Parser error: section block end tag [else] does not match the start tag [else]",
						DiagnosticSeverity.Error));
	}

	@Test
	public void validIfElseSyntax() {
		String template = "{#if false || true}OK{#else}OK{/if}";
		testDiagnosticsFor(template);
	}

}
