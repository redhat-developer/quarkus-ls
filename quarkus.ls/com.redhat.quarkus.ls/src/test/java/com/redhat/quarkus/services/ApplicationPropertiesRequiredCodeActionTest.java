/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import static com.redhat.quarkus.services.QuarkusAssert.ca;
import static com.redhat.quarkus.services.QuarkusAssert.d;
import static com.redhat.quarkus.services.QuarkusAssert.te;
import static com.redhat.quarkus.services.QuarkusAssert.testCodeActionsFor;
import static com.redhat.quarkus.services.QuarkusAssert.testDiagnosticsFor;

import java.util.ArrayList;
import java.util.List;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.QuarkusValidationTypeSettings;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test with the required code actions in 'application.properties' file.
 * 
 *
 */
public class ApplicationPropertiesRequiredCodeActionTest {

	private static QuarkusProjectInfo projectInfo;
	private static QuarkusValidationSettings validationSettings;

	@BeforeClass
	public static void setUp() throws Exception {
		projectInfo = new QuarkusProjectInfo();
		validationSettings = new QuarkusValidationSettings();
		List<ExtendedConfigDescriptionBuildItem> properties = new ArrayList<ExtendedConfigDescriptionBuildItem>();
		ExtendedConfigDescriptionBuildItem p1 = new ExtendedConfigDescriptionBuildItem();
		p1.setPropertyName("quarkus.required.property");
		p1.setRequired(true);
		properties.add(p1);
		ExtendedConfigDescriptionBuildItem p2 = new ExtendedConfigDescriptionBuildItem();
		p2.setPropertyName("quarkus.second.required.property");
		p2.setRequired(true);
		properties.add(p2);
		ExtendedConfigDescriptionBuildItem p3 = new ExtendedConfigDescriptionBuildItem();
		p3.setPropertyName("quarkus.third.required.property");
		p3.setRequired(true);
		properties.add(p3);
		ExtendedConfigDescriptionBuildItem p4 = new ExtendedConfigDescriptionBuildItem();
		p4.setPropertyName("quarkus.optional.property");
		p4.setRequired(false);
		properties.add(p4);
		projectInfo.setProperties(properties);

		QuarkusValidationTypeSettings error = new QuarkusValidationTypeSettings();
		error.setSeverity("warning");
		validationSettings.setRequired(error);
	}

	@Test
	public void testOneMissingPropertyCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.second.required.property=hello\n" + //
			"quarkus.third.required.property=hello";

		Diagnostic d = d(0, 0, 2, 37, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		testDiagnosticsFor(value, projectInfo, validationSettings, d);
		testCodeActionsFor(value, d, projectInfo,
				ca("Add all missing required properties?", te(2, 37, 2, 37, "\nquarkus.required.property="), d));
	}

	@Test
	public void testOneMissingPropertyCodeActionDelimeter() throws BadLocationException {

		String value = "quarkus.optional.property=hello\r\n" + //
			"quarkus.second.required.property=hello\r\n" + //
			"quarkus.third.required.property=hello";

		Diagnostic d = d(0, 0, 2, 37, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		testDiagnosticsFor(value, projectInfo, validationSettings, d);
		testCodeActionsFor(value, d, projectInfo,
				ca("Add all missing required properties?", te(2, 37, 2, 37, "\r\nquarkus.required.property="), d));
	}

	@Test
	public void testMissingPropertyCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.third.required.property=hello";

		Diagnostic d1 = d(0, 0, 1, 37, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d2 = d(0, 0, 1, 37, "Missing required property 'quarkus.second.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo,
				ca("Add all missing required properties?", te(1, 37, 1, 37, "\nquarkus.required.property=\nquarkus.second.required.property="), d1, d2));
	}

	@Test
	public void testMissingPropertyTrailingWhitespaceCodeAction() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.third.required.property=hello\n      \n     \n     ";

		Diagnostic d1 = d(0, 0, 4, 5, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d2 = d(0, 0, 4, 5, "Missing required property 'quarkus.second.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);

		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo,
				ca("Add all missing required properties?", te(1, 37, 1, 37, "\nquarkus.required.property=\nquarkus.second.required.property="), d1, d2));
	}

	@Test
	public void testMissingPropertyWhitepspaceCodeAction() throws BadLocationException {

		String value = "     \n      \n     \n      \n     \n     ";


		Diagnostic d1 = d(0, 0, 5, 5, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d2 = d(0, 0, 5, 5, "Missing required property 'quarkus.second.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d3 = d(0, 0, 5, 5, "Missing required property 'quarkus.third.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);
		d.add(d3);
		testDiagnosticsFor(value, projectInfo, validationSettings, d1, d2, d3);
		testCodeActionsFor(value, d, d.get(0).getRange(), projectInfo,
				ca("Add all missing required properties?", te(0, 0, 0, 0, "quarkus.required.property=\nquarkus.second.required.property=\nquarkus.third.required.property="), d1, d2, d3));
	}

	@Test
	public void testMissingPropertyEmptyFileCodeAction() throws BadLocationException {

		String value = "";

		Diagnostic d1 = d(0, 0, 0, 0, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d2 = d(0, 0, 0, 0, "Missing required property 'quarkus.second.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d3 = d(0, 0, 0, 0, "Missing required property 'quarkus.third.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

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

		Diagnostic d1 = d(0, 0, 0, 0, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d2 = d(0, 0, 0, 0, "Missing required property 'quarkus.second.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);
		Diagnostic d3 = d(0, 0, 0, 0, "Missing required property 'quarkus.third.required.property'", DiagnosticSeverity.Warning,
				ValidationType.required);

		List<Diagnostic> d = new ArrayList<>();
		d.add(d1);
		d.add(d2);
		d.add(d3);

		QuarkusFormattingSettings quarkusFormattingSettings = new QuarkusFormattingSettings();
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
