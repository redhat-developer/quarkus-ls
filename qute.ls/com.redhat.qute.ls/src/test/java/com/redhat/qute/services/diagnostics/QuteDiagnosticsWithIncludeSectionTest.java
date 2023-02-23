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

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test with #include section
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithIncludeSectionTest {

	@Test
	public void templateNotDefined() throws Exception {
		String template = "{#include /}";
		testDiagnosticsFor(template, //
				// error coming from the real Qute parser
				d(0, 11, 0, 11, QuteErrorCode.SyntaxError,
						"Parser error: mandatory section parameters not declared for {#include /}: [template]",
						DiagnosticSeverity.Error), //
				// error coming from Qute LS parser
				d(0, 1, 0, 9, QuteErrorCode.TemplateNotDefined, "Template id must be defined as parameter.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void templateNotFound() throws Exception {
		String template = "{#include XXXX /}";
		testDiagnosticsFor(template, //
				d(0, 10, 0, 14, QuteErrorCode.TemplateNotFound, "Template not found: `XXXX`.",
						DiagnosticSeverity.Error));

		template = "{#include XXXX }{/include}";
		testDiagnosticsFor(template, //
				d(0, 10, 0, 14, QuteErrorCode.TemplateNotFound, "Template not found: `XXXX`.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void templateFound() throws Exception {
		String template = "{#include base.html }\r\n" +
				"	{#body}ABCD{/body}\r\n" +
				"{/include}";
		testDiagnosticsFor(template);
	}

	@Test
	public void templateFoundWithShortSyntax() throws Exception {
		String template = "{#include base }\r\n" +
				"	{#body}ABCD{/body}\r\n" +
				"{/include}";
		testDiagnosticsFor(template);
	}
}
