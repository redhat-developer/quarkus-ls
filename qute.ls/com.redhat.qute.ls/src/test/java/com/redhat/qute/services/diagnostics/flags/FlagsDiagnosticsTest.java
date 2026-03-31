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
package com.redhat.qute.services.diagnostics.flags;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.flags.FlagsProject;

/**
 * Test with Flags flag: namespace.
 *
 * @author Angelo ZERR
 *
 */
public class FlagsDiagnosticsTest {

	@Test
	public void flags() throws Exception {
		testDiagnosticsFor("{flag:enabled('')}");
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, "test.qute", null, FlagsProject.PROJECT_URI, null, false, expected);
	}
}
