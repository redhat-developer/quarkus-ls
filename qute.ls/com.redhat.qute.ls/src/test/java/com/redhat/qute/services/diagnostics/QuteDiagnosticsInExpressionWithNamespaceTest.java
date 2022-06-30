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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with expressions.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithNamespaceTest {

	@Test
	public void undefineNamespace() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{dataXXX:item}";
		Diagnostic d = d(1, 1, 1, 8, QuteErrorCode.UndefinedNamespace, "No namespace resolver found for: `dataXXX`.",
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
	public void dataNamespace() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{data:item}";
		testDiagnosticsFor(template);
	}

	@Test
	public void validInjectFieldNamespace() {
		String template = "{inject:bean}";
		testDiagnosticsFor(template);

		template = "{inject:bean.isEmpty()}";
		testDiagnosticsFor(template);
	}

	@Test
	public void validInjectMethodNamespace() {
		String template = "{config:property('qute')}";
		testDiagnosticsFor(template);

		template = "{config:propertyXXXX('qute')}";
		testDiagnosticsFor(template,
				d(0, 8, 0, 20, QuteErrorCode.UnknownNamespaceResolverMethod,
						"`propertyXXXX` cannot be resolved or is not a method of `config` namespace resolver.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void anyMatchName() {
		// config:*
		String template = "{config:foo}";
		testDiagnosticsFor(template);
	}

	@Test
	public void invalidInjectNamespaceInFirstPart() {
		String template = "{inject:foo}";
		Diagnostic d = d(0, 8, 0, 11, QuteErrorCode.UndefinedObject, "`foo` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("foo", false));
		testDiagnosticsFor(template, d);

		template = "{inject:foo()}";
		testDiagnosticsFor(template,
				d(0, 8, 0, 11, QuteErrorCode.UnknownNamespaceResolverMethod,
						"`foo` cannot be resolved or is not a method of `inject` namespace resolver.",
						DiagnosticSeverity.Error));
	}

	@Test
	public void invalidInjectNamespaceInMemberPart() {
		String template = "{inject:bean.XXXX}";
		testDiagnosticsFor(template,
				d(0, 13, 0, 17, QuteErrorCode.UnknownProperty,
						"`XXXX` cannot be resolved or is not a field of `java.lang.String` Java type.",
						new UnknownPropertyData("java.lang.String", "XXXX"),
						DiagnosticSeverity.Error));

		template = "{inject:bean.XXXX()}";
		testDiagnosticsFor(template,
				d(0, 13, 0, 17, QuteErrorCode.UnknownMethod,
						"`XXXX` cannot be resolved or is not a method of `java.lang.String` Java type.",
						DiagnosticSeverity.Error));

	}

	@Test
	public void cdi() {
		String template = "{cdi:bean}";
		testDiagnosticsFor(template);

		template = "{cdi:bean.isEmpty()}";
		testDiagnosticsFor(template);

	}

	@Test
	public void badNamespace() throws Exception {
		String template = "{X:}";
		Diagnostic d = d(0, 1, 0, 2, QuteErrorCode.UndefinedNamespace, "No namespace resolver found for: `X`.",
				DiagnosticSeverity.Warning);
		testDiagnosticsFor(template,
				d(0, 0, 0, 3, QuteErrorCode.SyntaxError, "Parser error on line 1: empty expression found {X:}",
						DiagnosticSeverity.Error), //
				d);

		template = "{X:bean}";
		testDiagnosticsFor(template, d(0, 1, 0, 2, QuteErrorCode.UndefinedNamespace,
				"No namespace resolver found for: `X`.", DiagnosticSeverity.Warning));
		testCodeActionsFor(template, d, //
				ca(d, c("Ignore `UndefinedNamespace` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedNamespace.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}
}
