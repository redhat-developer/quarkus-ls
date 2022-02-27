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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Test for diagnostics in parameter declaration.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInParameterDeclarationTest {

	@Test
	public void kwownJavaClass() throws Exception {
		String template = "{@org.acme.Item item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownJavaClass() throws Exception {
		String template = "{@org.acme.ItemXXX item}";
		testDiagnosticsFor(template, //
				d(0, 2, 0, 18, QuteErrorCode.UnknownType, "`org.acme.ItemXXX` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void kwownJavaClassInTypeParameter() throws Exception {
		String template = "{@java.util.List<org.acme.Item> item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownJavaClassInTypeParameter() throws Exception {
		String template = "{@java.util.List<org.acme.ItemXXX> item}";
		testDiagnosticsFor(template, //
				d(0, 17, 0, 33, QuteErrorCode.UnknownType, "`org.acme.ItemXXX` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}
	
	@Test
	public void kwownJavaClassInSecondTypeParameter() throws Exception {
		String template = "{@java.util.Map<java.lang.String,org.acme.Item> item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void unkwownJavaClassInSecondTypeParameter() throws Exception {
		String template = "{@java.util.Map<java.lang.String,org.acme.ItemXXX> item}";
		testDiagnosticsFor(template, //
				d(0, 33, 0, 49, QuteErrorCode.UnknownType, "`org.acme.ItemXXX` cannot be resolved to a type.",
						DiagnosticSeverity.Error));
	}
}
