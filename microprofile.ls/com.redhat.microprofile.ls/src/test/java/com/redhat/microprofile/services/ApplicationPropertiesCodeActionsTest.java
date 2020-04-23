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

import org.eclipse.lsp4j.CodeAction;
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

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'quarkus.application.name' ?", te(1, 0, 1, 23, "quarkus.application.name"), d),
				caAddToExcluded("quarkus.application.nme", d), caAddToExcluded("quarkus.application.*", d));
	};

	@Test
	public void codeActionsForUnknownPropertiesParentKey() throws BadLocationException {
		String value = "abcdefghij.group=myUser\n" + //
				"abcdefghij.registry=http://my.docker-registry.net\n" + //
				"abcdefghij.labels[0].key=foo\n" + //
				"abcdefghij.labels[0].value=bar\n" + //
				"abcdefghij.readiness-probe.initial-delay-seconds=20\n" + //
				"abcdefghij.readiness-probe.period-seconds=45";

		Diagnostic d1 = d(0, 0, 16, "Unknown property 'abcdefghij.group'", DiagnosticSeverity.Warning,
				ValidationType.unknown);
		Diagnostic d2 = d(1, 0, 19, "Unknown property 'abcdefghij.registry'", DiagnosticSeverity.Warning,
				ValidationType.unknown);
		Diagnostic d3 = d(2, 0, 24, "Unknown property 'abcdefghij.labels[0].key'", DiagnosticSeverity.Warning,
				ValidationType.unknown);
		Diagnostic d4 = d(3, 0, 26, "Unknown property 'abcdefghij.labels[0].value'", DiagnosticSeverity.Warning,
				ValidationType.unknown);
		Diagnostic d5 = d(4, 0, 48, "Unknown property 'abcdefghij.readiness-probe.initial-delay-seconds'",
				DiagnosticSeverity.Warning, ValidationType.unknown);
		Diagnostic d6 = d(5, 0, 41, "Unknown property 'abcdefghij.readiness-probe.period-seconds'",
				DiagnosticSeverity.Warning, ValidationType.unknown);

		testDiagnosticsFor(value, d1, d2, d3, d4, d5, d6);
		testCodeActionsFor(value, d1, caAddToExcluded("abcdefghij.group", d1), caAddToExcluded("abcdefghij.*", d1));

		testCodeActionsFor(value, d2, caAddToExcluded("abcdefghij.registry", d2), caAddToExcluded("abcdefghij.*", d2));

		testCodeActionsFor(value, d3, caAddToExcluded("abcdefghij.labels[0].key", d3),
				caAddToExcluded("abcdefghij.labels[0].*", d3), caAddToExcluded("abcdefghij.*", d3));

		testCodeActionsFor(value, d4, caAddToExcluded("abcdefghij.labels[0].value", d4),
				caAddToExcluded("abcdefghij.labels[0].*", d4), caAddToExcluded("abcdefghij.*", d4));

		testCodeActionsFor(value, d5, caAddToExcluded("abcdefghij.readiness-probe.initial-delay-seconds", d5),
				caAddToExcluded("abcdefghij.readiness-probe.*", d5), caAddToExcluded("abcdefghij.*", d5));

		testCodeActionsFor(value, d6, caAddToExcluded("abcdefghij.readiness-probe.period-seconds", d6),
				caAddToExcluded("abcdefghij.readiness-probe.*", d6), caAddToExcluded("abcdefghij.*", d6));
	};

	@Test
	public void codeActionsForUnknownPropertiesParentKey2() throws BadLocationException {

		String value = "a.b.c.d=123\n" + //
				"a.c.d=123";

		Diagnostic d1 = d(0, 0, 7, "Unknown property 'a.b.c.d'", DiagnosticSeverity.Warning, ValidationType.unknown);
		Diagnostic d2 = d(1, 0, 5, "Unknown property 'a.c.d'", DiagnosticSeverity.Warning, ValidationType.unknown);

		testDiagnosticsFor(value, d1, d2);

		testCodeActionsFor(value, d1, caAddToExcluded("a.b.c.d", d1), caAddToExcluded("a.b.c.*", d1),
				caAddToExcluded("a.b.*", d1), caAddToExcluded("a.*", d1));

		testCodeActionsFor(value, d2, caAddToExcluded("a.c.d", d2), caAddToExcluded("a.c.*", d2),
				caAddToExcluded("a.*", d2));
	};

	@Test
	public void codeActionsForUnknownPropertiesParentKey3() throws BadLocationException {

		String value = "quarkus.a.b.c.d=123";

		Diagnostic d = d(0, 0, 15, "Unknown property 'quarkus.a.b.c.d'", DiagnosticSeverity.Warning,
				ValidationType.unknown);

		testDiagnosticsFor(value, d);

		testCodeActionsFor(value, d, caAddToExcluded("quarkus.a.b.c.d", d), caAddToExcluded("quarkus.a.b.c.*", d),
				caAddToExcluded("quarkus.a.b.*", d), caAddToExcluded("quarkus.a.*", d));
	};

	@Test
	public void codeActionsForUnknownPropertiesParentKey4() throws BadLocationException {

		String value = "a.b.c.d=123";

		Diagnostic d1 = d(0, 0, 7, "Unknown property 'a.b.c.d'", DiagnosticSeverity.Warning, ValidationType.unknown);

		testDiagnosticsFor(value, d1);

		testCodeActionsFor(value, d1, caAddToExcluded("a.b.c.d", d1), caAddToExcluded("a.b.c.*", d1),
				caAddToExcluded("a.b.*", d1), caAddToExcluded("a.*", d1));
	};

	@Test
	public void codeActionsForUnknownLogLevelValue() throws BadLocationException {
		String value = "quarkus.log.level=WARNIN";
		Diagnostic d = d(0, 18, 24,
				"Invalid enum value: 'WARNIN' is invalid for type java.util.logging.Level",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'WARNING'?", te(0, 18, 0, 24, "WARNING"), d));
	};

	@Test
	public void codeActionsForUnknownLogLevelStartsWith() throws BadLocationException {
		String value = "quarkus.log.level=F";
		Diagnostic d = d(0, 18, 19,
				"Invalid enum value: 'F' is invalid for type java.util.logging.Level",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'FINE'?", te(0, 18, 0, 19, "FINE"), d),
				ca("Did you mean 'FINER'?", te(0, 18, 0, 19, "FINER"), d),
				ca("Did you mean 'FINEST'?", te(0, 18, 0, 19, "FINEST"), d));
	};

	@Test
	public void codeActionsForUnknownLogLevelValueMappedProperty() throws BadLocationException {
		String value = "quarkus.log.category.\"org.acme\".level=WARNIN";
		Diagnostic d = d(0, 38, 44, "Invalid enum value: 'WARNIN' is invalid for type io.quarkus.runtime.logging.InheritableLevel",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'WARNING'?", te(0, 38, 0, 44, "WARNING"), d));
	};

	@Test
	public void codeActionsForUnknownEnum() throws BadLocationException {
		String value = "quarkus.log.syslog.async.overflow=BLACK";
		Diagnostic d = d(0, 34, 39,
				"Invalid enum value: 'BLACK' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'BLOCK'?", te(0, 34, 0, 39, "BLOCK"), d));
	};

	@Test
	public void codeActionsForUnknownEnumStartsWith() throws BadLocationException {
		// verbatim
		String value = "quarkus.log.syslog.async.overflow=B";
		Diagnostic d = d(0, 34, 35,
				"Invalid enum value: 'B' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'BLOCK'?", te(0, 34, 0, 35, "BLOCK"), d));

		// kebab_case
		value = "quarkus.log.syslog.async.overflow=b";
		d = d(0, 34, 35,
				"Invalid enum value: 'b' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'block'?", te(0, 34, 0, 35, "block"), d));
	};

	@Test
	public void codeActionsForUnknownBoolean() throws BadLocationException {
		String value = "quarkus.http.cors=fals";
		Diagnostic d = d(0, 18, 22, "Type mismatch: boolean expected", DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Did you mean 'false'?", te(0, 18, 0, 22, "false"), d));
	};

	@Test
	public void codeActionsForReplaceUnknown() throws BadLocationException {
		String value = "quarkus.log.syslog.async.overflow=unknown-value";
		Diagnostic d = d(0, 34, 47,
				"Invalid enum value: 'unknown-value' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value);

		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d, ca("Replace with 'block'?", te(0, 34, 0, 47, "block"), d),
				ca("Replace with 'discard'?", te(0, 34, 0, 47, "discard"), d));

	}

	/**
	 * Returns a code action for <code>diagnostic</code> that causes
	 * <code>item</code> to be added to
	 * <code>quarkus.tools.validation.unknown.excluded</code> client configuration
	 * 
	 * @param item       the item to add to the client configuration array
	 * @param diagnostic the diagnostic for the <code>CodeAction</code>
	 * @return a code action that causes <code>item</code> to be added to
	 *         <code>quarkus.tools.validation.unknown.excluded</code> client
	 *         configuration
	 */
	private CodeAction caAddToExcluded(String item, Diagnostic diagnostic) {
		ConfigurationItemEdit configItemEdit = new ConfigurationItemEdit("quarkus.tools.validation.unknown.excluded",
				ConfigurationItemEditType.add, item);

		Command command = new Command("Add " + item + " to unknown excluded array",
				CommandKind.COMMAND_CONFIGURATION_UPDATE, Arrays.asList(configItemEdit));

		return ca("Exclude '" + item + "' from unknown property validation?", command, diagnostic);
	}
}