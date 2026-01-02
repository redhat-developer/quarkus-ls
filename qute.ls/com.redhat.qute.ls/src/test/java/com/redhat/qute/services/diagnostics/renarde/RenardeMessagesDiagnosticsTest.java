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
package com.redhat.qute.services.diagnostics.renarde;

import static com.redhat.qute.QuteAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.renarde.RenardeErrorCode;
import com.redhat.qute.project.renarde.RenardeProject;

/**
 * Test diagnostics with Renarde Quarkus extension.
 *
 * @author Angelo ZERR
 *
 */
public class RenardeMessagesDiagnosticsTest {

	@Test
	public void validMessageKey() throws Exception {
		String template = "{m:main.login}";
		testDiagnosticsFor(template);

		template = "{m:todos.message.added('foo')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void invalidMessageKey() throws Exception {
		String template = "{m:main.loginXXX}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 16, RenardeErrorCode.RenardeMessages, "Unknown message 'main.loginXXX'.",
						DiagnosticSeverity.Warning));

		template = "{m:todos.message.addedXXX('foo')}";
		testDiagnosticsFor(template, //
				d(0, 1, 0, 32, RenardeErrorCode.RenardeMessages, "Unknown message 'todos.message.addedXXX'.",
						DiagnosticSeverity.Warning));
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RenardeProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, null, expected);
	}

}
