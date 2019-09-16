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
				d(0, 0, 16, "Missing equals sign after 'unknown.property'", DiagnosticSeverity.Error,
						ValidationType.syntax),
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
				d(0, 0, 17, "Missing equals sign after 'quarkus.http.cors'", DiagnosticSeverity.Error,
						ValidationType.syntax), //
				d(2, 0, 27, "Missing equals sign after 'quarkus.datasource.username'", DiagnosticSeverity.Error,
						ValidationType.syntax));
	};

	@Test
	public void validateSyntaxMissingEqualsComment() throws BadLocationException {
		String value = "quarkus.http.cors=true\n" + //
				"quarkus.application.name # ====";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(1, 0, 24, "Missing equals sign after 'quarkus.application.name'", DiagnosticSeverity.Error,
						ValidationType.syntax));
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
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(3, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
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
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(3, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(5, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
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
				d(1, 0, 18, "Duplicate property 'quarkus.ssl.native'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(5, 0, 18, "Duplicate property 'quarkus.ssl.native'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(0, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(2, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(4, 0, 17, "Duplicate property 'quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
	};

	@Test
	public void validateDuplicatePropertyDifferentProfile() throws BadLocationException {

		String value = "quarkus.http.port=8080\n" + //
				"%dev.quarkus.http.port=9090\n" + //
				"%prod.quarkus.http.port=9090\n" + //
				"quarkus.ssl.native=true";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);
	};

	@Test
	public void validateDuplicatePropertySameProfile() throws BadLocationException {

		String value = "quarkus.http.port=8080\n" + //
				"%dev.quarkus.http.port=9090\n" + // <-- warning
				"%dev.quarkus.http.port=9090"; // <-- warning

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(1, 0, 22, "Duplicate property '%dev.quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(2, 0, 22, "Duplicate property '%dev.quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
	};

	@Test
	public void validateEnumValueNoError() throws BadLocationException {

		String value = "quarkus.log.console.async.overflow=DISCARD\n" + //
				"quarkus.log.file.async.overflow = BLOCK ";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);
	};

	@Test
	public void validateEnumValueError() throws BadLocationException {

		String value = "quarkus.log.console.async.overflow=error\n" + // <-- error
				"quarkus.log.file.async.overflow = error"; // <-- error

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 35, 40, "Invalid enum value: 'error' is invalid for type org.jboss.logmanager.handlers.AsyncHandler$OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value),
				d(1, 34, 39, "Invalid enum value: 'error' is invalid for type org.jboss.logmanager.handlers.AsyncHandler$OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value));
	};

	@Test
	public void validateIntValueNoError() throws BadLocationException {

		String value = "quarkus.http.port=09010\n" + //
				"quarkus.http.io-threads = 9\n" + //
				"quarkus.http.test-ssl-port=43444\n" + //
				"quarkus.thread-pool.core-threads  =  ";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);
	};

	@Test
	public void validateIntValueError() throws BadLocationException {

		String value = "quarkus.http.port=4.3\n" + // <-- int
				"quarkus.http.io-threads = hello\n" + // <-- java.util.OptionalInt expected
				"quarkus.http.test-ssl-port=DISCARD\n" + // <-- int
				"quarkus.thread-pool.core-threads=false\n" + // <-- int
				"quarkus.hibernate-orm.jdbc.statement-batch-size= error"; // <-- java.util.Optional<java.lang.Integer>

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 18, 21, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 26, 31, "Type mismatch: java.util.OptionalInt expected", DiagnosticSeverity.Error, ValidationType.value),
				d(2, 27, 34, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(3, 33, 38, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(4, 49, 54, "Type mismatch: java.util.Optional<java.lang.Integer> expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateBooleanNoError() throws BadLocationException {

		String value = "quarkus.http.cors  =   \n" + //
				"quarkus.arc.auto-inject-fields=false\n" + //
				"quarkus.ssl.native = true\n" + //
				"quarkus.keycloak.policy-enforcer.lazy-load-paths=false";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);
	}

	@Test
	public void validateBooleanError() throws BadLocationException {

		String value = "quarkus.http.cors  =   DISCARD\n" + // <-- boolean
				"quarkus.arc.auto-inject-fields=1.76\n" + // <-- boolean
				"quarkus.ssl.native = hello\n" + // <-- java.util.Optional<java.lang.Boolean>
				"quarkus.keycloak.policy-enforcer.lazy-load-paths=abc"; // <-- java.lang.Boolean

		QuarkusValidationSettings settings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 23, 30, "Type mismatch: boolean expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 31, 35, "Type mismatch: boolean expected", DiagnosticSeverity.Error, ValidationType.value),
				d(2, 21, 26, "Type mismatch: java.util.Optional<java.lang.Boolean> expected", DiagnosticSeverity.Error, ValidationType.value),
				d(3, 49, 52, "Type mismatch: java.lang.Boolean expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateFloatNoError() throws BadLocationException {

		String value = "quarkus.thread-pool.growth-resistance=0";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=0.4343";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=340.434";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=340.434f";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);
	}

	@Test
	public void validateFloatError() throws BadLocationException {

		String value ="quarkus.thread-pool.growth-resistance=abc0.4343";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 38, 47, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.thread-pool.growth-resistance=DISCARD";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 38, 45, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.thread-pool.growth-resistance=hello";
		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings,
				d(0, 38, 43, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateBuildTimeInjectValues() throws BadLocationException {

		String value = "quarkus.http.cors = ${value.one}\n" + //
			"quarkus.http.port=${value_two}\n" + //
			"quarkus.ssl.native=    ${value-three}";

		QuarkusValidationSettings settings = new QuarkusValidationSettings();

		testDiagnosticsFor(value, getDefaultQuarkusProjectInfo(), settings);

		
	}


}
