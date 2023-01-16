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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Test with #fragment section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithFragmentSectionTest {

	@Test
	public void fragment() throws Exception {
		String template = "{#fragment id=items}\r\n" + //
				"	    \r\n" + //
				"{/fragment}";
		testDiagnosticsFor(template);
	}
}
