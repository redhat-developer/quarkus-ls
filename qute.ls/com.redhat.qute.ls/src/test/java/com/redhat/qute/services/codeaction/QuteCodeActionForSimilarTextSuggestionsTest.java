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
import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
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
	public void similarTextSuggestionQuickFixForUndefinedNamespace() throws Exception {
		String template = "{inje:model}";
		Diagnostic d = d(0, 1, 0, 5, //
				QuteErrorCode.UndefinedNamespace, //
				"No namespace resolver found for: `inje`.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 1, 0, 5, "inject")), //
				ca(d, c("Ignore `UndefinedNamespace` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedNamespace.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void noSimilarTextSuggestionQuickFixForUndefinedNamespace() throws Exception {
		String template = "{foo:bar}";
		Diagnostic d = d(0, 1, 0, 4, //
				QuteErrorCode.UndefinedNamespace, //
				"No namespace resolver found for: `foo`.", //
				DiagnosticSeverity.Warning);

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, c("Ignore `UndefinedNamespace` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedNamespace.severity", //
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

}
