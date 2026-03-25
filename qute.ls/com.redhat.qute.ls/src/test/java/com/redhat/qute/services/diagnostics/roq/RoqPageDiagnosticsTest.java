/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.roq;

import static com.redhat.qute.QuteAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test diagnostics with Roq Quarkus extension and page data model.
 *
 * @author Angelo ZERR
 *
 */
public class RoqPageDiagnosticsTest {

	@Test
	public void pageData() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Page page}\r\n" + //
				"{#for item in page.data.toc}\r\n" + //
				"    {#for subsection in item.subsections} \r\n" + //
				"    {/}    \r\n" + //
				"{/}";
		testDiagnosticsFor(template, //
				d(1, 24, 1, 27, QuteErrorCode.IterationError,
						"Iteration error: {page.data.toc} resolved to [java.lang.Object] which is not iterable.", //
						"qute", DiagnosticSeverity.Error));
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, null, expected);
	}

}
