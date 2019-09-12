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
import static com.redhat.quarkus.services.QuarkusAssert.getDefaultQuarkusProjectInfo;
import static com.redhat.quarkus.services.QuarkusAssert.testDiagnosticsFor;

import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.QuarkusValidationTypeSettings;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

/**
 * Test with diagnostics in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesDiagnosticsTest {

	@Test
	public void validateUnknownProperties() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size=20\n" + //
				"quarkus.datasource.min-size=\n" + //
				"unknown.property=X\n" + // <-- error
				"quarkus.log.category.XXXXXXXXXXXXX.min-level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".min-level=DEBUG\n" + // no error
																							// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																							// is a key
				"\n" + //
				"";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(),
				d(8, 0, 16, "Unknown property 'unknown.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(10, 0, 57, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level'",
						DiagnosticSeverity.Warning, ValidationType.unknown));
	};

	@Test
	public void validateUnknownPropertyMissingEquals() throws BadLocationException {
		String value = "unknown.property\n" + //
				"quarkus.datasource.min-size=\n" + //
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".min-level=DEBUG\n" + // no error
																							// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																							// is a key
				"\n" + //
				"";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(),
				d(0, 0, 16, "Missing equals sign after 'unknown.property'", DiagnosticSeverity.Error, ValidationType.syntax),
				d(0, 0, 16, "Unknown property 'unknown.property'", DiagnosticSeverity.Warning, ValidationType.unknown));
	};


	@Test
	public void validateUnknownPropertiesAsError() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size=20\n" + //
				"quarkus.datasource.min-size=\n" + //
				"unknown.property=X\n" + // <-- error
				"quarkus.log.category.XXXXXXXXXXXXX.min-level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".min-level=DEBUG\n" + // no error
																							// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																							// is a key
				"\n" + //
				"";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		QuarkusValidationTypeSettings unknown = new QuarkusValidationTypeSettings();
		unknown.setSeverity("error");
		settings.setUnknown(unknown);

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(8, 0, 16, "Unknown property 'unknown.property'", DiagnosticSeverity.Error, ValidationType.unknown), //
				d(10, 0, 57, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level'",
						DiagnosticSeverity.Error, ValidationType.unknown));
	};

	@Test
	public void validateUnknownPropertiesExcluded() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size=20\n" + //
				"quarkus.datasource.min-size=\n" + //
				"unknown.property=X\n" + // <-- error
				"quarkus.log.category.XXXXXXXXXXXXX.min-level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".min-level=DEBUG\n" + // no error
																							// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																							// is a key
				"\n" + //
				"";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		QuarkusValidationTypeSettings unknown = new QuarkusValidationTypeSettings();
		unknown.setSeverity("error");
		unknown.setExcluded(new String[] { "unknown.property" });
		settings.setUnknown(unknown);

		testDiagnosticsFor(value, 1, getDefaultQuarkusProjectInfo(), settings,
				d(10, 0, 57, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.min-level'",
						DiagnosticSeverity.Error, ValidationType.unknown));
	};


	@Test
	public void validateSyntaxMissingEquals() throws BadLocationException {
		String value = "quarkus.http.cors true\n" + // <-- error
				"quarkus.application.name=\"name\"\n" + //
				"quarkus.datasource.username"; // <-- error

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 0, 17, "Missing equals sign after 'quarkus.http.cors'", DiagnosticSeverity.Error, ValidationType.syntax), //
				d(2, 0, 27, "Missing equals sign after 'quarkus.datasource.username'",
						DiagnosticSeverity.Error, ValidationType.syntax));
	};

	@Test
	public void validateSyntaxMissingEqualsComment() throws BadLocationException {
		String value = "quarkus.http.cors=true\n" + //
				"quarkus.application.name # ====";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(1, 0, 24, "Missing equals sign after 'quarkus.application.name'", DiagnosticSeverity.Error, ValidationType.syntax));
	};

	@Test
	public void validateDuplicateProperty() throws BadLocationException {
		String value = "quarkus.http.port=8080\n" + // <-- warning
				"quarkus.http.cors=false\n" + //
				"quarkus.http.host=0.0.0.0\n" + //
				"quarkus.http.port=8080\n" + // <-- warning
				"quarkus.ssl.native=true";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(3, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate));
	};

	@Test
	public void validateDuplicateProperties() throws BadLocationException {
		String value = "quarkus.http.port=8080\n" + // <-- warning
				"quarkus.http.cors=false\n" + //
				"quarkus.http.host=0.0.0.0\n" + //
				"quarkus.http.port=8080\n" + // <-- warning
				"quarkus.ssl.native=true\n" + //
				"quarkus.http.port=8080\n" + // <-- warning
				"quarkus.http.cors.headers=\"test\"";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(3, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(5, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate));
	};

	@Test
	public void validateDifferentDuplicateProperties() throws BadLocationException {

		String value = "quarkus.http.port=8080\n" + // <-- warning
				"quarkus.ssl.native=false\n" + // <-- warning
				"quarkus.http.port=8080\n" + // <-- warning
				"quarkus.http.host=0.0.0.0\n" + //
				"quarkus.http.port=8080\n" + // <-- warning
				"quarkus.ssl.native=true\n" + // <-- warning
				"quarkus.http.cors.headers=\"test\"";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(1, 0, 18, "Duplicate property 'quarkus.ssl.native'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(5, 0, 18, "Duplicate property 'quarkus.ssl.native'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(2, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate),
				d(4, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning, ValidationType.duplicate));
	};
}
