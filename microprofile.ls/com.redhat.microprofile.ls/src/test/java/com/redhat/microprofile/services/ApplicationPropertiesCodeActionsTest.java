/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import static com.redhat.microprofile.services.MicroProfileAssert.ca;
import static com.redhat.microprofile.services.MicroProfileAssert.d;
import static com.redhat.microprofile.services.MicroProfileAssert.te;
import static com.redhat.microprofile.services.MicroProfileAssert.testCodeActionsFor;
import static com.redhat.microprofile.services.MicroProfileAssert.testDiagnosticsFor;

import java.util.Arrays;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.client.CommandKind;
import com.redhat.microprofile.ls.commons.client.ConfigurationItemEdit;
import com.redhat.microprofile.ls.commons.client.ConfigurationItemEditType;
import com.redhat.microprofile.services.ValidationType;

/**
 * Test with code actions in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesCodeActionsTest {

	@Test
	public void codeActionsForUnknownProperties() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.application.nme=X\n" + // <-- error
				"\n" + //
				"";
		Diagnostic d = d(1, 0, 23, "Unknown property 'quarkus.application.nme'", DiagnosticSeverity.Warning,
				ValidationType.unknown);

		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit("quarkus.tools.validation.unknown.excluded",
				ConfigurationItemEditType.add, "quarkus.application.nme");

		Command command = new Command("Add quarkus.application.nme to unknown excluded array",
				CommandKind.COMMAND_CONFIGURATION_UPDATE, Arrays.asList(configItemEdit));

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'quarkus.application.name' ?", te(1, 0, 1, 23, "quarkus.application.name"), d),
				ca("Exclude 'quarkus.application.nme' from unknown property validation?", command, d));
	};

	@Test
	public void codeActionsForUnknownLogLevelValue() throws BadLocationException {
		String value = "quarkus.log.level=WARNIN";
		Diagnostic d = d(0, 18, 24, "Invalid enum value: 'WARNIN' is invalid for type java.util.Optional<java.util.logging.Level>", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'WARNING'?", te(0, 18, 0, 24, "WARNING"), d));
	};

	@Test
	public void codeActionsForUnknownLogLevelStartsWith() throws BadLocationException {
		String value = "quarkus.log.level=F";
		Diagnostic d = d(0, 18, 19, "Invalid enum value: 'F' is invalid for type java.util.Optional<java.util.logging.Level>", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'FINE'?", te(0, 18, 0, 19, "FINE"), d),
				ca("Did you mean 'FINER'?", te(0, 18, 0, 19, "FINER"), d),
				ca("Did you mean 'FINEST'?", te(0, 18, 0, 19, "FINEST"), d));
	};

	@Test
	public void codeActionsForUnknownLogLevelValueMappedProperty() throws BadLocationException {
		String value = "quarkus.log.category.\"org.acme\".level=WARNIN";
		Diagnostic d = d(0, 38, 44, "Invalid enum value: 'WARNIN' is invalid for type java.lang.String", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'WARNING'?", te(0, 38, 0, 44, "WARNING"), d));
	};

	@Test
	public void codeActionsForUnknownEnum() throws BadLocationException {
		String value = "quarkus.log.syslog.async.overflow=BLACK";
		Diagnostic d = d(0, 34, 39, "Invalid enum value: 'BLACK' is invalid for type org.jboss.logmanager.handlers.AsyncHandler$OverflowAction", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'BLOCK'?", te(0, 34, 0, 39, "BLOCK"), d));
	};

	@Test
	public void codeActionsForUnknownEnumStartsWith() throws BadLocationException {
		String value = "quarkus.log.syslog.async.overflow=B";
		Diagnostic d = d(0, 34, 35, "Invalid enum value: 'B' is invalid for type org.jboss.logmanager.handlers.AsyncHandler$OverflowAction", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'BLOCK'?", te(0, 34, 0, 35, "BLOCK"), d));
	};

	@Test
	public void codeActionsForUnknownBoolean() throws BadLocationException {
		String value = "quarkus.http.cors=fals";
		Diagnostic d = d(0, 18, 22, "Type mismatch: boolean expected", DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'false'?", te(0, 18, 0, 22, "false"), d));
	};

	@Test
	public void codeActionsForReplaceUnknown() throws BadLocationException {
		String value = "quarkus.log.syslog.async.overflow=unknown-value";
		Diagnostic d = d(0, 34, 47,
				"Invalid enum value: 'unknown-value' is invalid for type org.jboss.logmanager.handlers.AsyncHandler$OverflowAction",
				DiagnosticSeverity.Error,
				ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Replace with 'BLOCK'?", te(0, 34, 0, 47, "BLOCK"), d),
				ca("Replace with 'DISCARD'?", te(0, 34, 0, 47, "DISCARD"), d));
	};
}
