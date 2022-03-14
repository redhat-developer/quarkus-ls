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

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.client.CommandCapabilities;
import com.redhat.qute.ls.commons.client.CommandKindCapabilities;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.diagnostics.DiagnosticDataFactory;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;

/**
 * Test code action with settings.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionWithSettingsTest {

	@Test
	public void withoutDisableValidationQuickFix() throws Exception {
		String template = "{item}";

		Diagnostic d = d(0, 1, 0, 5, //
				QuteErrorCode.UndefinedObject, //
				"`item` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("item", false));

		testDiagnosticsFor(template, d);
		testCodeActionsFor(template, d, //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}" + //
						System.lineSeparator())), //
				ca(d, c("Ignore `qute.validation.undefinedObject.severity` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	@Test
	public void withDisableValidationQuickFix() throws Exception {
		String template = "{item}";

		Diagnostic d = d(0, 1, 0, 5, //
				QuteErrorCode.UndefinedObject, //
				"`item` cannot be resolved to an object.", //
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("item", false));

		testDiagnosticsFor(template, d);

		SharedSettings settings = createSharedSettings(QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE);
		testCodeActionsFor(template, d, //
				settings, //
				ca(d, c("Disable Qute validation for the `qute-quickstart` project.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.enabled", //
						"test.qute", //
						ConfigurationItemEditType.update, false, //
						d)), //
				ca(d, c("Exclude this file from validation.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.excluded", //
						"test.qute", //
						ConfigurationItemEditType.add, "test.qute", //
						d)), //
				ca(d, te(0, 0, 0, 0, "{@java.lang.String item}" + //
						System.lineSeparator())), //
				ca(d, c("Ignore `qute.validation.undefinedObject.severity` problem.", //
						QuteClientCommandConstants.COMMAND_CONFIGURATION_UPDATE, //
						"qute.validation.undefinedObject.severity", //
						"test.qute", //
						ConfigurationItemEditType.update, "ignore", //
						d)));
	}

	private static SharedSettings createSharedSettings(String... commandIds) {
		SharedSettings settings = new SharedSettings();
		CommandCapabilities commandCapabilities = new CommandCapabilities();
		CommandKindCapabilities kinds = new CommandKindCapabilities(
				commandIds != null ? Arrays.asList(commandIds) : Collections.emptyList());
		commandCapabilities.setCommandKind(kinds);
		settings.getCommandCapabilities().setCapabilities(commandCapabilities);
		return settings;
	}
}
