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
 * Syntax error which improves the error of the real Qute parser.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsOverridedSyntaxErrorTest {

	@Test
	public void UNTERMINATED_SECTION() {
		String template = "{@java.util.List items}\r\n"
				+ "{#each items}\r\n" // <-- error
				+ "	\r\n"
				+ "{#each items}\r\n"
				+ "	\r\n"
				+ "{/each}\r\n";
		testDiagnosticsFor(template, //
				d(1, 1, 1, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
						"Parser error: unterminated section [each] detected",
						DiagnosticSeverity.Error));
	}

	@Test
	public void severalUNTERMINATED_SECTION() {
		String template = "{@java.util.List items}\r\n"
				+ "{#each items}\r\n" // <-- error
				+ "	\r\n"
				+ "{#each items}\r\n"
				+ "	\r\n"
				+ "{/each}\r\n"
				+ "{#each items}\r\n"; // <-- error
		testDiagnosticsFor(template, //
				d(1, 1, 1, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
						"Parser error: unterminated section [each] detected",
						DiagnosticSeverity.Error),
				d(6, 1, 6, 6, QuteSyntaxErrorCode.UNTERMINATED_SECTION,
						"Parser error: unterminated section [each] detected",
						DiagnosticSeverity.Error));
	}

	@Test
	public void SECTION_END_DOES_NOT_MATCH_START() {
		String template = "{#for }\r\n"
				+ "	\r\n"
				+ "	{/each}"; // <-- error
		testDiagnosticsFor(template,
				d(2, 2, 2, 7, QuteSyntaxErrorCode.SECTION_END_DOES_NOT_MATCH_START,
						"Parser error: section end tag [each] does not match the start tag [for]",
						DiagnosticSeverity.Error));
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
	public void SECTION_BLOCK_END_DOES_NOT_MATCH_START() {
		String template = "{#if true}\r\n"
				+ "	Hello\r\n"
				+ "{#else}\r\n"
				+ "Hi\r\n"
				+ "{/elsa}\r\n" // <-- error
				+ "{/if}";
		testDiagnosticsFor(template,
				d(4, 1, 4, 6, QuteSyntaxErrorCode.SECTION_BLOCK_END_DOES_NOT_MATCH_START,
						"Parser error: section block end tag [elsa] does not match the start tag [else]",
						DiagnosticSeverity.Error));
	}

	@Test
	public void validIfElseSyntax() {
		String template = "{#if false || true}OK{#else}OK{/if}";
		testDiagnosticsFor(template);
	}

}
