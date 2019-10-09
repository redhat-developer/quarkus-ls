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

import static com.redhat.quarkus.services.QuarkusAssert.d;
import static com.redhat.quarkus.services.QuarkusAssert.testDiagnosticsFor;

import java.util.ArrayList;
import java.util.List;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.QuarkusValidationTypeSettings;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test with the required diagnostic in 'application.properties' file.
 * 
 *
 */
public class ApplicationPropertiesRequiredDiagnosticsTest {

	private static QuarkusProjectInfo projectInfo;
	private static QuarkusValidationSettings settings;

	@BeforeClass
	public static void setUp() throws Exception {
		projectInfo = new QuarkusProjectInfo();
		settings = new QuarkusValidationSettings();
		List<ExtendedConfigDescriptionBuildItem> properties = new ArrayList<ExtendedConfigDescriptionBuildItem>();
		ExtendedConfigDescriptionBuildItem p1 = new ExtendedConfigDescriptionBuildItem();
		p1.setPropertyName("quarkus.required.property");
		p1.setRequired(true);
		properties.add(p1);
		ExtendedConfigDescriptionBuildItem p2 = new ExtendedConfigDescriptionBuildItem();
		p2.setPropertyName("quarkus.optional.property");
		p2.setRequired(false);
		properties.add(p2);
		ExtendedConfigDescriptionBuildItem p3 = new ExtendedConfigDescriptionBuildItem();
		p3.setPropertyName("quarkus.second.optional.property");
		p3.setRequired(false);
		properties.add(p3);
		projectInfo.setProperties(properties);

		QuarkusValidationTypeSettings error = new QuarkusValidationTypeSettings();
		error.setSeverity("error");
		settings.setRequired(error);
	}

	@Test
	public void testNoErrorRequiredExists() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.required.property=hello";
		
		testDiagnosticsFor(value, projectInfo, settings);
	}

	@Test
	public void testMissingRequired() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.second.optional.property=hello";
		
		testDiagnosticsFor(value, projectInfo, settings,
		d(0, 0, 1, 38, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Error, ValidationType.required));
	}

	@Test
	public void testRequiredMissingValue() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.required.property=   \n" + //
			"quarkus.second.optional.property=hello";
		
		testDiagnosticsFor(value, projectInfo, settings,
		d(1, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error, ValidationType.required));
	}

	@Test
	public void testRequiredBothMissingValueDuplicates() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.required.property=   \n" + //
			"quarkus.required.property=   \n" + //
			"quarkus.second.optional.property=hello";
		
		testDiagnosticsFor(value, projectInfo, settings,
		d(1, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning, ValidationType.duplicate),
		d(2, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning, ValidationType.duplicate),
		d(1, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error, ValidationType.required),
		d(2, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error, ValidationType.required));
	}

	@Test
	public void testRequiredOneMissingValueDuplicates() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
			"quarkus.required.property=value\n" + //
			"quarkus.required.property=   \n" + //
			"quarkus.second.optional.property=hello";
		
		testDiagnosticsFor(value, projectInfo, settings,
		d(1, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning, ValidationType.duplicate),
		d(2, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning, ValidationType.duplicate));
	}


}
