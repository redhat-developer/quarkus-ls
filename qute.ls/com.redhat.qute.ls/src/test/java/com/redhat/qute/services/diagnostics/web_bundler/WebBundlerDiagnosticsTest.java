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
package com.redhat.qute.services.diagnostics.web_bundler;

import static com.redhat.qute.QuteAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.web_bundler.WebBundlerProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test with web bundler.
 *
 * @author Angelo ZERR
 *
 */
public class WebBundlerDiagnosticsTest {

	@Test
	public void userTagFromComponents() throws Exception {
		String template = "{#permissionDialog\r\n" + //
				"    title = \"\"\r\n" + //
				"    selectedCode = \"\"\r\n" + //
				"}\r\n" + //
				"    {#invalid-tag /}\r\n" + // <-- error
				"    {#trigger}\r\n" + // <-- no error: trigger comes from {#insert trigger} declared in
										// permissionDialog.html
				"        <button type=\"button\">Edit</button>\r\n" + //
				"    {/trigger}\r\n" + //
				"{/permissionDialog}";
		testDiagnosticsFor(template, //
				d(4, 5, 4, 17, QuteErrorCode.UndefinedSectionTag, "No section helper found for `invalid-tag`.", //
						"qute", DiagnosticSeverity.Warning));
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, WebBundlerProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, null, expected);
	}

}
