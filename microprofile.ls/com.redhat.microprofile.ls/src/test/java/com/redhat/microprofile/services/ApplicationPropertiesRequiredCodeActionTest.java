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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.settings.MicroProfileValidationSettings;
import com.redhat.microprofile.settings.MicroProfileValidationTypeSettings;

/**
 * Test with the required code actions in 'application.properties' file.
 * 
 *
 */
public class ApplicationPropertiesRequiredCodeActionTest {

	private static MicroProfileProjectInfo projectInfo;
	private static MicroProfileValidationSettings validationSettings;

	@BeforeClass
	public static void setUp() throws Exception {
		projectInfo = new MicroProfileProjectInfo();
		validationSettings = new MicroProfileValidationSettings();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.required.property");
		p1.setRequired(true);
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.second.required.property");
		p2.setRequired(true);
		properties.add(p2);
		ItemMetadata p3 = new ItemMetadata();
		p3.setName("quarkus.third.required.property");
		p3.setRequired(true);
		properties.add(p3);
		ItemMetadata p4 = new ItemMetadata();
		p4.setName("quarkus.optional.property");
		p4.setRequired(false);
		properties.add(p4);
		projectInfo.setProperties(properties);

		MicroProfileValidationTypeSettings error = new MicroProfileValidationTypeSettings();
		error.setSeverity("warning");
		validationSettings.setRequired(error);
	}

	@Test
	public void testOneMissingPropertyCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.second.required.property=hello\n" + //
				"quarkus.third.required.property=hello";

		Diagnostic d = d(0, 0, 2, 37, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		testDiagnosticsFor(value, projectInfo, validationSettings, d);
		testCodeActionsFor(value, d, projectInfo,
				ca("Add all missing required properties?", te(2, 37, 2, 37, "\nquarkus.required.property="), d));
	}

	@Test
	public void testOneMissingPropertyCodeActionDelimeter() throws BadLocationException {

		String value = "quarkus.optional.property=hello\r\n" + //
				"quarkus.second.required.property=hello\r\n" + //
				"quarkus.third.required.property=hello";

		Diagnostic d = d(0, 0, 2, 37, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		testDiagnosticsFor(value, projectInfo, validationSettings, d);
		testCodeActionsFor(value, d, projectInfo,
				ca("Add all missing required properties?", te(2, 37, 2, 37, "\r\nquarkus.required.property="), d));
	}

	@Test
	public void testMissingPropertyCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.third.required.property=hello";

		Diagnostic d1 = d(0, 0, 1, 37, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d2 = d(0, 0, 1, 37, "Missing required property 'quarkus.second.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo, ca("Add all missing required properties?",
				te(1, 37, 1, 37, "\nquarkus.required.property=\nquarkus.second.required.property="), d1, d2));
	}

	@Test
	public void testMissingPropertyTrailingWhitespaceCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.third.required.property=hello\n      \n     \n     ";

		Diagnostic d1 = d(0, 0, 4, 5, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d2 = d(0, 0, 4, 5, "Missing required property 'quarkus.second.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo, ca("Add all missing required properties?",
				te(1, 37, 1, 37, "\nquarkus.required.property=\nquarkus.second.required.property="), d1, d2));
	}

	@Test
	public void testMissingPropertyWhitepspaceCodeAction() throws BadLocationException {

		String value = "     \n      \n     \n      \n     \n     ";

		Diagnostic d1 = d(0, 0, 5, 5, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d2 = d(0, 0, 5, 5, "Missing required property 'quarkus.second.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d3 = d(0, 0, 5, 5, "Missing required property 'quarkus.third.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);
		d.add(d3);
		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2, d3);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo, ca("Add all missing required properties?", te(0,
				0, 0, 0,
				"quarkus.required.property=\nquarkus.second.required.property=\nquarkus.third.required.property="), d1,
				d2, d3));
	}

	@Test
	public void testMissingPropertyEmptyFileCodeAction() throws BadLocationException {

		String value = "";

		Diagnostic d1 = d(0, 0, 0, 0, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d2 = d(0, 0, 0, 0, "Missing required property 'quarkus.second.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d3 = d(0, 0, 0, 0, "Missing required property 'quarkus.third.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);
		d.add(d3);

		String lineSeparator = System.lineSeparator();

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2, d3);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo,
				ca("Add all missing required properties?", te(0, 0, 0, 0, "quarkus.required.property=" + //
						lineSeparator + //
						"quarkus.second.required.property=" + //
						lineSeparator + //
						"quarkus.third.required.property="), d1, d2, d3));
	}

	@Test
	public void testMissingPropertySpacesCodeAction() throws BadLocationException {

		String value = "";

		Diagnostic d1 = d(0, 0, 0, 0, "Missing required property 'quarkus.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d2 = d(0, 0, 0, 0, "Missing required property 'quarkus.second.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);
		Diagnostic d3 = d(0, 0, 0, 0, "Missing required property 'quarkus.third.required.property'",
				DiagnosticSeverity.Warning, ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);
		d.add(d3);

		MicroProfileFormattingSettings quarkusFormattingSettings = new MicroProfileFormattingSettings();
		quarkusFormattingSettings.setSurroundEqualsWithSpaces(true);

		String lineSeparator = System.lineSeparator();

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2, d3);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo, quarkusFormattingSettings,
				ca("Add all missing required properties?", te(0, 0, 0, 0, "quarkus.required.property = " + //
						lineSeparator + //
						"quarkus.second.required.property = " + //
						lineSeparator + //
						"quarkus.third.required.property = "), d1, d2, d3));
	}
}
