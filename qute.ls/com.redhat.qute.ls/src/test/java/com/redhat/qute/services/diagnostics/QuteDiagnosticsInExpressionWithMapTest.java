/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
 * Test with expressions and Map.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithMapTest {

	@Test
	public void entrySetWithMap() {
		String template = "{@java.util.Map<java.lang.String,org.acme.Item> map}\r\n" + //
				"\r\n" + //
				"{#for item in map.entrySet()}\r\n" + //
				"	{item.getKey()}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);

		template = "{@java.util.Map<java.lang.String,org.acme.Item> map}\r\n" + //
				"\r\n" + //
				"{#for item in map.entrySet()}\r\n" + //
				"	{item.nameXXX}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(3, 7, 3, 14, QuteErrorCode.UnknownProperty,
						"`nameXXX` cannot be resolved or is not a field of `java.util.Map$Entry<java.lang.String,org.acme.Item>` Java type.",
						new JavaBaseTypeOfPartData("java.util.Map$Entry<java.lang.String,org.acme.Item>"),
						DiagnosticSeverity.Error));
	}

}
