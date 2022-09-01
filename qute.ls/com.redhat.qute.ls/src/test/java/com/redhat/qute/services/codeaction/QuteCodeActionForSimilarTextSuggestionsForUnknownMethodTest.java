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
package com.redhat.qute.services.codeaction;

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test code action for similar text suggestions for
 * {@link QuteErrorCode#UnknownMethod}.
 *
 */
public class QuteCodeActionForSimilarTextSuggestionsForUnknownMethodTest {

	@Test
	public void similarTextSuggestionQuickFixForUndefinedMethod() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{string.charA()}";

		Diagnostic d = d(1, 8, 1, 13, //
				QuteErrorCode.UnknownMethod, //
				"`charA` cannot be resolved or is not a method of `java.lang.String` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("java.lang.String"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 8, 1, 13, "charAt")));
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedMethodWithResolver() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{string.orEmp()}";

		Diagnostic d = d(1, 8, 1, 13, //
				QuteErrorCode.UnknownMethod, //
				"`orEmp` cannot be resolved or is not a method of `java.lang.String` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("java.lang.String"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 8, 1, 13, "orEmpty")));
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedMethodFromExtendedType() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.conver()}";

		Diagnostic d = d(1, 6, 1, 12, //
				QuteErrorCode.UnknownMethod, //
				"`conver` cannot be resolved or is not a method of `org.acme.Item` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.Item"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 6, 1, 12, "convert")));
	}

	@Test
	public void noSimilarTextSuggestionQuickFixForUndefinedMethod() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{string.noMethod()}";

		Diagnostic d = d(1, 8, 1, 16, //
				QuteErrorCode.UnknownMethod, //
				"`noMethod` cannot be resolved or is not a method of `java.lang.String` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("java.lang.String"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d);
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedMethodWithNamespace() throws Exception {
		String template = "{cdi:bean.charA()}";

		Diagnostic d = d(0, 10, 0, 15, //
				QuteErrorCode.UnknownMethod, //
				"`charA` cannot be resolved or is not a method of `java.lang.String` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("java.lang.String"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 10, 0, 15, "charAt")));
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedMethodFromClassWithCyclicInheritance() throws Exception {
		String template = "{@org.acme.qute.cyclic.ClassA classA}\r\n" + //
				"{classA.conver()}";

		Diagnostic d = d(1, 8, 1, 14, //
				QuteErrorCode.UnknownMethod, //
				"`conver` cannot be resolved or is not a method of `org.acme.qute.cyclic.ClassA` Java type.", //
				DiagnosticSeverity.Error);
		d.setData(new JavaBaseTypeOfPartData("org.acme.qute.cyclic.ClassA"));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 8, 1, 14, "convert")));
	}

}
