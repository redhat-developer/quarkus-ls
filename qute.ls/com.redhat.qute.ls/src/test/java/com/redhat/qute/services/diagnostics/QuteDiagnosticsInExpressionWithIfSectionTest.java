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
import static com.redhat.qute.QuteAssert.te;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;

/**
 * Test with #if section
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInExpressionWithIfSectionTest {

	@Test
	public void definedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#if item.name != '' && item > 0}\r\n" + //
				"{/if}";
		testDiagnosticsFor(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{#if item.name != '' && item > 0}\r\n" + //
				"{/if}";

		Diagnostic d1 = d(0, 5, 0, 9, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d1.setData(DiagnosticDataFactory.createUndefinedObjectData("item", false));

		Diagnostic d2 = d(0, 24, 0, 28, QuteErrorCode.UndefinedObject, "`item` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d2.setData(DiagnosticDataFactory.createUndefinedObjectData("item", false));

		testDiagnosticsFor(template, d1, d2);
		testCodeActionsFor(template, d1, //
				ca(d1, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d1, c("Ignore `qute.validation.undefinedObject.severity` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d1)));
		testCodeActionsFor(template, d2, //
				ca(d2, te(0, 0, 0, 0, "{@java.lang.String item}\r\n")), //
				ca(d2, c("Ignore `qute.validation.undefinedObject.severity` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d2)));
	}

}
