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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test code action for similar text suggestions.
 *
 */
public class QuteCodeActionForSimilarTextSuggestionsTest {

	@Test
	public void similarTextSuggestionQuickFixForUndefinedObject() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{stri}";

		Diagnostic d = d(1, 1, 1, 5, //
				QuteErrorCode.UndefinedObject, //
				"`stri` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("stri", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String stri}\r\n")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)), //
				ca(d, te(1, 5, 1, 5, "??")), //
				ca(d, te(1, 1, 1, 5, "string")));
	}

	@Test
	public void similarTextSuggestionCenterQuickFixForUndefinedObject() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{trin}";

		Diagnostic d = d(1, 1, 1, 5, //
				QuteErrorCode.UndefinedObject, //
				"`trin` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("trin", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String trin}\r\n")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)), //
				ca(d, te(1, 5, 1, 5, "??")), //
				ca(d, te(1, 1, 1, 5, "string")));
	}
}
