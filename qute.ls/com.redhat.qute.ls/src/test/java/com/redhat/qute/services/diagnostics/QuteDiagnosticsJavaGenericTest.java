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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Test with Java generic diagnostic.
 *
 */
public class QuteDiagnosticsJavaGenericTest {

	@Test
	public void generic() throws Exception {
		String template = "{@java.util.List<java.lang.String> list1}\n" + //
				"{@java.util.List<java.lang.String> list2}\n" + //
				"\n" + //
				"{list1.addAll(list2)}\n" + //
				"{list1.containsAll(list2)}";
		testDiagnosticsFor(template);
	}

}
