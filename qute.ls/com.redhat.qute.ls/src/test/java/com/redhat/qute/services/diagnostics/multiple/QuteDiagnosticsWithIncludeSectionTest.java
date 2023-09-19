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
package com.redhat.qute.services.diagnostics.multiple;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Test with #include section and project dependencies.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsWithIncludeSectionTest {

	@Test
	public void templateFromProjectBFoundInProjectA() throws Exception {
		String template = "{#include root.html }\r\n" +
				"{/include}";
		testDiagnosticsFor(template, QuteProjectB.PROJECT_URI);
	}

	@Test
	public void templateFromProjectBFoundInProjectAdWithShortSyntax() throws Exception {
		String template = "{#include root }\r\n" +
				"{/include}";
		testDiagnosticsFor(template, QuteProjectB.PROJECT_URI);
	}

	public static void testDiagnosticsFor(String value, String projectUri, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, projectUri, null, false,
				null, expected);
	}
}
