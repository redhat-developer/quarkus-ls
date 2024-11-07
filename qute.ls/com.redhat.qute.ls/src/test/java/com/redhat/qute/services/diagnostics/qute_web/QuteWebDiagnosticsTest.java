/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.qute_web;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.qute_web.QuteWebProject;

/**
 * Test diagnostics with Qute Web Quarkus extension.
 *
 * @author Angelo ZERR
 *
 */
public class QuteWebDiagnosticsTest {

	@Test
	public void noError() throws Exception {
		String template = "{#let name=http:param('name', 'Qute')}\r\n" + //
				"<!DOCTYPE html>\r\n" + //
				"<html>\r\n" + //
				"<body>\r\n" + //
				"<h1>Hello {name}</h1>\r\n" + //
				"</body>\r\n" + //
				"</html>";
		testDiagnosticsFor(template);
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, QuteWebProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, true, null, expected);
	}

}
