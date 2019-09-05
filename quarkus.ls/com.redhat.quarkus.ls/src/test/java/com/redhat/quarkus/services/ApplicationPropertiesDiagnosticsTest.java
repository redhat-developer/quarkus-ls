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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.QuarkusValidationTypeSettings;

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
				"quarkus.datasource.max-size\n" + //
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
	public void validateUnknownPropertiesAsError() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.datasource.driver=org.postgresql.Driver\n" + //
				"quarkus.datasource.username=quarkus_test\n" + //
				"quarkus.datasource.password=quarkus_test\n" + //
				"\n" + //
				"       \n" + //
				"quarkus.datasource.max-size\n" + //
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
				"quarkus.datasource.max-size\n" + //
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

}
