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
package com.redhat.qute.services.diagnostics.multiple;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.multiple.QuteProjectA;
import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Test with user tag section and project dependencies.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithUserTagSectionTest {

	@Test
	public void validateUserTagAInProjectA() throws Exception {
		// Use tag-a which is defined in project A
		String template = "{#tag-a /}";
		testDiagnosticsFor(template, QuteProjectA.PROJECT_URI);
	}

	@Test
	public void validateUserTagAInProjectB() throws Exception {
		// Use tag-a which is defined in project B which depends from project A
		String template = "{#tag-a /}";
		testDiagnosticsFor(template, QuteProjectB.PROJECT_URI);
	}

	private static void testDiagnosticsFor(String value, String projectUri, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, projectUri, null, false, null, expected);
	}
}
