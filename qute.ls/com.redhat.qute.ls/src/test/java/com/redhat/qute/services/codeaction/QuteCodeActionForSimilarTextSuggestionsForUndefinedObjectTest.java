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
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test code action for similar text suggestions for
 * {@link QuteErrorCode#UndefinedObject}.
 *
 */
public class QuteCodeActionForSimilarTextSuggestionsForUndefinedObjectTest {

	@Test
	public void similarTextSuggestionQuickFixForUndefinedObject() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{stri}";

		Diagnostic d = d(1, 1, 1, 5, //
				QuteErrorCode.UndefinedObject, //
				"`stri` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 1, 1, 5, "string")), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String stri}\r\n")), //
				ca(d, te(1, 5, 1, 5, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void similarTextSuggestionCenterQuickFixForUndefinedObject() throws Exception {
		String template = "{@java.lang.String string}\r\n" + //
				"{trin}";

		Diagnostic d = d(1, 1, 1, 5, //
				QuteErrorCode.UndefinedObject, //
				"`trin` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(1, 1, 1, 5, "string")), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String trin}\r\n")), //
				ca(d, te(1, 5, 1, 5, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void notSmilarTextSuggestionQuickFixForUndefinedObject() throws Exception {
		String template = "{abc}";

		Diagnostic d = d(0, 1, 0, 4, //
				QuteErrorCode.UndefinedObject, //
				"`abc` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String abc}" + //
						System.lineSeparator())), //
				ca(d, te(0, 4, 0, 4, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void smilarTextSuggestionQuickFixForUndefinedObjectInMethodPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{@java.lang.Integer index}\r\n" + //
				"{#let name=items.get(inde)}\r\n" + //
				"	\r\n" + //
				"{/let}";

		Diagnostic d = d(2, 21, 2, 25, //
				QuteErrorCode.UndefinedObject, //
				"`inde` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(2, 21, 2, 25, "index")), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String inde}\r\n")), //
				ca(d, te(2, 25, 2, 25, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedObjectUsingLoopSectionMetadataAlias() throws Exception {
		String template = "{@java.util.List<org.acme.Item> array}\r\n" + //
				"{#for item in array}\r\n" + //
				"	{ite}\r\n" + //
				"{/for}}";
		Diagnostic d = d(2, 2, 2, 5, //
				QuteErrorCode.UndefinedObject, //
				"`ite` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(2, 2, 2, 5, "item")), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String ite}\r\n")), //
				ca(d, te(2, 5, 2, 5, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void similarTextSuggestionQuickFixForUndefinedObjectUsingLoopSectionMetadataAliasAndParameter()
			throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"	{ite}\r\n" + //
				"{/for}}";
		Diagnostic d = d(2, 2, 2, 5, //
				QuteErrorCode.UndefinedObject, //
				"`ite` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(2, 2, 2, 5, "item")), //
				ca(d, te(2, 2, 2, 5, "items")), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String ite}\r\n")), //
				ca(d, te(2, 5, 2, 5, "??")), //
				ca(d, c("Ignore `UndefinedObject` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

}
