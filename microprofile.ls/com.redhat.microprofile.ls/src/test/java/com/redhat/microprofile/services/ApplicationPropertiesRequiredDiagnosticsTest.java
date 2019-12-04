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

import static com.redhat.microprofile.services.MicroProfileAssert.d;
import static com.redhat.microprofile.services.MicroProfileAssert.testDiagnosticsFor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.settings.MicroProfileValidationSettings;
import com.redhat.microprofile.settings.MicroProfileValidationTypeSettings;

/**
 * Test with the required diagnostic in 'application.properties' file.
 * 
 *
 */
public class ApplicationPropertiesRequiredDiagnosticsTest {

	private static MicroProfileProjectInfo projectInfo;
	private static MicroProfileValidationSettings settings;

	@BeforeClass
	public static void setUp() throws Exception {
		projectInfo = new MicroProfileProjectInfo();
		settings = new MicroProfileValidationSettings();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.required.property");
		p1.setRequired(true);
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.optional.property");
		p2.setRequired(false);
		properties.add(p2);
		ItemMetadata p3 = new ItemMetadata();
		p3.setName("quarkus.second.optional.property");
		p3.setRequired(false);
		properties.add(p3);
		projectInfo.setProperties(properties);

		MicroProfileValidationTypeSettings error = new MicroProfileValidationTypeSettings();
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
				d(0, 0, 1, 38, "Missing required property 'quarkus.required.property'", DiagnosticSeverity.Error,
						ValidationType.required));
	}

	@Test
	public void testRequiredMissingValue() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.required.property=   \n" + //
				"quarkus.second.optional.property=hello";

		testDiagnosticsFor(value, projectInfo, settings,
				d(1, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error,
						ValidationType.requiredValue));
	}

	@Test
	public void testRequiredBothMissingValueDuplicates() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.required.property=   \n" + //
				"quarkus.required.property=   \n" + //
				"quarkus.second.optional.property=hello";

		testDiagnosticsFor(value, projectInfo, settings,
				d(1, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(2, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(1, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error,
						ValidationType.requiredValue),
				d(2, 0, 29, "Missing required property value for 'quarkus.required.property'", DiagnosticSeverity.Error,
						ValidationType.requiredValue));
	}

	@Test
	public void testRequiredOneMissingValueDuplicates() throws BadLocationException {

		String value = "quarkus.optional.property=hello\n" + //
				"quarkus.required.property=value\n" + //
				"quarkus.required.property=   \n" + //
				"quarkus.second.optional.property=hello";

		testDiagnosticsFor(value, projectInfo, settings,
				d(1, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(2, 0, 25, "Duplicate property 'quarkus.required.property'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
	}

}
