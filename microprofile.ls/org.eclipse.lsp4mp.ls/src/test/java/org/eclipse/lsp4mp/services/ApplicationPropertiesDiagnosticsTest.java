/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import static org.eclipse.lsp4mp.services.MicroProfileAssert.d;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.getDefaultMicroProfileProjectInfo;
import static org.eclipse.lsp4mp.services.MicroProfileAssert.testDiagnosticsFor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.JavaVersion;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConverterKind;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.services.ValidationType;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;
import org.eclipse.lsp4mp.settings.MicroProfileValidationTypeSettings;
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
				"quarkus.log.category.XXXXXXXXXXXXX.level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".level=DEBUG\n" + // no error
																						// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																						// is a key
				"\n" + //
				"";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(),
				d(8, 0, 16, "Unknown property 'unknown.property'", DiagnosticSeverity.Warning, ValidationType.unknown), //
				d(10, 0, 53, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level'",
						DiagnosticSeverity.Warning, ValidationType.unknown));
	};

	@Test
	public void validateUnknownPropertyMissingEquals() throws BadLocationException {
		String value = "unknown.property\n" + //
				"quarkus.datasource.min-size=\n" + //
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".level=DEBUG\n" + // no error
																						// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																						// is a key
				"\n" + //
				"";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(),
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
				"quarkus.log.category.XXXXXXXXXXXXX.level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".level=DEBUG\n" + // no error
																						// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																						// is a key
				"\n" + //
				"";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		MicroProfileValidationTypeSettings unknown = new MicroProfileValidationTypeSettings();
		unknown.setSeverity("error");
		settings.setUnknown(unknown);

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(8, 0, 16, "Unknown property 'unknown.property'", DiagnosticSeverity.Error, ValidationType.unknown), //
				d(10, 0, 53, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level'",
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
				"quarkus.log.category.XXXXXXXXXXXXX.level=DEBUG\n" + // no error 'XXXXXXXXXXXXX' is a key
				"quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level=DEBUG\n" + // <-- error
				"quarkus.log.category.\"XXXXXXXXXXXXX.YYYYYYYYYYYY\".level=DEBUG\n" + // no error
																						// 'XXXXXXXXXXXXX.YYYYYYYYYYYY'
																						// is a key
				"\n" + //
				"";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		MicroProfileValidationTypeSettings unknown = new MicroProfileValidationTypeSettings();
		unknown.setSeverity("error");
		unknown.setExcluded(new String[] { "unknown.property" });
		settings.setUnknown(unknown);

		testDiagnosticsFor(value, 1, getDefaultMicroProfileProjectInfo(), settings,
				d(10, 0, 53, "Unknown property 'quarkus.log.category.XXXXXXXXXXXXX.YYYYYYYYYYYY.level'",
						DiagnosticSeverity.Error, ValidationType.unknown));
	};

	@Test
	public void validateUnknownPropertiesExcludedWithPattern() throws BadLocationException {
		String value = "com.mycompany.remoteServices.MyServiceClient/mp-rest/url=url\n" + //
				"com.mycompany.remoteServices.MyServiceClient/mp-rest/uri=uri\n" + //
				"com.mycompany.foo=bar";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		MicroProfileValidationTypeSettings unknown = new MicroProfileValidationTypeSettings();
		unknown.setSeverity("error");
		settings.setUnknown(unknown);

		// */mp-rest/url pattern --> only
		// com.mycompany.remoteServices.MyServiceClient/mp-rest/url is ignored
		unknown.setExcluded(new String[] { "*/mp-rest/url" });
		testDiagnosticsFor(value, 2, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 56, "Unknown property 'com.mycompany.remoteServices.MyServiceClient/mp-rest/uri'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(2, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		// */mp-rest/* pattern --> all errors containing path 'mp-rest' are ignored
		unknown.setExcluded(new String[] { "*/mp-rest/*" });
		testDiagnosticsFor(value, 1, getDefaultMicroProfileProjectInfo(), settings,
				d(2, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		value = "com.mycompany.remoteServices.MyServiceClient/mp-rest/url/foo=url\n" + //
				"com.mycompany.remoteServices.MyServiceClient/mp-rest/uri/bar=uri\n" + //
				"com.mycompany.remoteServices.MyOtherClient/mp-rest/url/foo=url\n" + //
				"com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar=uri\n" + //
				"com.mycompany.foo=bar";

		// com.mycompany.* pattern --> all errors are ignored
		unknown.setExcluded(new String[] { "com.mycompany.*" });
		testDiagnosticsFor(value, 0, getDefaultMicroProfileProjectInfo(), settings);

		// com.mycompany.remoteServices.MyServiceClient/**/ --> all 'MyServiceClient' errors are ignored
		unknown.setExcluded(new String[] { "com.mycompany.remoteServices.MyServiceClient/**/" });
		testDiagnosticsFor(value, 3, getDefaultMicroProfileProjectInfo(), settings,
				d(2, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/url/foo'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(3, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(4, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));
		
		// com.mycompany.remoteServices.MyServiceClient/**/foo --> all errors
		// for 'MyServiceClient' properties ending with path 'foo' are ignored
		unknown.setExcluded(new String[] { "com.mycompany.remoteServices.MyServiceClient/**/foo" });
		testDiagnosticsFor(value, 4, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 60, "Unknown property 'com.mycompany.remoteServices.MyServiceClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(2, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/url/foo'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(3, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(4, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		// com.mycompany.*/**/foo --> all errors for properties
		// ending with path 'foo' are ignored
		unknown.setExcluded(new String[] { "com.mycompany.*/**/foo" });
		testDiagnosticsFor(value, 3, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 60, "Unknown property 'com.mycompany.remoteServices.MyServiceClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(3, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(4, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		// com*MyService*/**/foo --> all errors for 'MyService' properties
		// ending with path 'foo' are ignored
		unknown.setExcluded(new String[] { "com*MyService*/**/foo" });
		testDiagnosticsFor(value, 4, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 60, "Unknown property 'com.mycompany.remoteServices.MyServiceClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(2, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/url/foo'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(3, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(4, 0, 17, "Unknown property 'com.mycompany.foo'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		// *foo --> all errors ending with 'foo' are ignored
		unknown.setExcluded(new String[] { "*foo" });
		testDiagnosticsFor(value, 2, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 60, "Unknown property 'com.mycompany.remoteServices.MyServiceClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown),
				d(3, 0, 58, "Unknown property 'com.mycompany.remoteServices.MyOtherClient/mp-rest/uri/bar'",
						DiagnosticSeverity.Error, ValidationType.unknown));

		// * pattern --> all errors are ignored
		unknown.setExcluded(new String[] { "*" });
		testDiagnosticsFor(value, 0, getDefaultMicroProfileProjectInfo(), settings);
	};

	@Test
	public void validateSyntaxMissingEquals() throws BadLocationException {
		String value = "quarkus.http.cors true\n" + // <-- error
				"quarkus.application.name=\"name\"\n" + //
				"quarkus.datasource.username"; // <-- error

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 0, 17, "Missing equals sign after 'quarkus.http.cors'", DiagnosticSeverity.Error,
						ValidationType.syntax), //
				d(2, 0, 27, "Missing equals sign after 'quarkus.datasource.username'", DiagnosticSeverity.Error,
						ValidationType.syntax));
	};

	@Test
	public void validateSyntaxMissingEqualsComment() throws BadLocationException {
		String value = "quarkus.http.cors=true\n" + //
				"quarkus.application.name # ====";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
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

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
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

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
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

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
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

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	};

	@Test
	public void validateDuplicatePropertySameProfile() throws BadLocationException {

		String value = "quarkus.http.port=8080\n" + //
				"%dev.quarkus.http.port=9090\n" + // <-- warning
				"%dev.quarkus.http.port=9090"; // <-- warning

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(1, 0, 22, "Duplicate property '%dev.quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate),
				d(2, 0, 22, "Duplicate property '%dev.quarkus.http.port'", DiagnosticSeverity.Warning,
						ValidationType.duplicate));
	};

	@Test
	public void validateEnumValueNoError() throws BadLocationException {

		String value = "quarkus.log.console.async.overflow=DISCARD\n" + //
				"quarkus.log.file.async.overflow = BLOCK ";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	};

	@Test
	public void validateEnumValueError() throws BadLocationException {

		String value = "quarkus.log.console.async.overflow=error\n" + // <-- error
				"quarkus.log.file.async.overflow = error"; // <-- error

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings, d(0, 35, 40,
				"Invalid enum value: 'error' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
				DiagnosticSeverity.Error, ValidationType.value),
				d(1, 34, 39,
						"Invalid enum value: 'error' is invalid for type org.jboss.logmanager.handlers.AsyncHandler.OverflowAction",
						DiagnosticSeverity.Error, ValidationType.value));
	};

	@Test
	public void validateIntValueNoError() throws BadLocationException {

		String value = "quarkus.http.port=09010\n" + //
				"quarkus.http.io-threads = 9\n" + //
				"quarkus.http.test-ssl-port=43444\n" + //
				"quarkus.thread-pool.core-threads  =  ";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	};

	@Test
	public void validateIntValueError() throws BadLocationException {

		String value = "quarkus.http.port=4.3\n" + // <-- int
				"quarkus.http.io-threads = hello\n" + // <-- java.util.OptionalInt expected
				"quarkus.http.test-ssl-port=DISCARD\n" + // <-- int
				"quarkus.thread-pool.core-threads=false\n" + // <-- int
				"quarkus.hibernate-orm.jdbc.statement-batch-size= error"; // <-- java.util.Optional<java.lang.Integer>

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 18, 21, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 26, 31, "Type mismatch: java.util.OptionalInt expected", DiagnosticSeverity.Error,
						ValidationType.value),
				d(2, 27, 34, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(3, 33, 38, "Type mismatch: int expected", DiagnosticSeverity.Error, ValidationType.value),
				d(4, 49, 54, "Type mismatch: java.util.Optional<java.lang.Integer> expected", DiagnosticSeverity.Error,
						ValidationType.value));
	}

	@Test
	public void validateBooleanNoError() throws BadLocationException {

		String value = "quarkus.http.cors  =   \n" + //
				"quarkus.arc.auto-inject-fields=false\n" + //
				"quarkus.ssl.native = true\n" + //
				"MP_Fault_Tolerance_Metrics_Enabled=false";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	}

	@Test
	public void validateBooleanError() throws BadLocationException {

		String value = "quarkus.http.cors  =   DISCARD\n" + // <-- boolean
				"quarkus.arc.auto-inject-fields=1.76\n" + // <-- boolean
				"quarkus.ssl.native = hello\n" + // <-- java.util.Optional<java.lang.Boolean>
				"MP_Fault_Tolerance_Metrics_Enabled=abc"; // <-- java.lang.Boolean

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 23, 30, "Type mismatch: boolean expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 31, 35, "Type mismatch: boolean expected", DiagnosticSeverity.Error, ValidationType.value),
				d(2, 21, 26, "Type mismatch: java.util.Optional<java.lang.Boolean> expected", DiagnosticSeverity.Error,
						ValidationType.value),
				d(3, 35, 38, "Type mismatch: java.lang.Boolean expected", DiagnosticSeverity.Error,
						ValidationType.value));
	}

	@Test
	public void validateFloatNoError() throws BadLocationException {

		String value = "quarkus.thread-pool.growth-resistance=0";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=0.4343";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=340.434";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=340.434f";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "quarkus.thread-pool.growth-resistance=";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	}

	@Test
	public void validateFloatError() throws BadLocationException {

		String value = "quarkus.thread-pool.growth-resistance=abc0.4343";

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 38, 47, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.thread-pool.growth-resistance=DISCARD";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 38, 45, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.thread-pool.growth-resistance=hello";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 38, 43, "Type mismatch: float expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateBigDecimalError() throws BadLocationException {
		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.BigDecimal");
		p1.setType("java.math.BigDecimal");
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.Optional.BigDecimal");
		p2.setType("java.util.Optional<java.math.BigDecimal>");
		properties.add(p2);
		projectInfo.setProperties(properties);

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		
		String value = "quarkus.BigDecimal=12\n" + //
				"quarkus.Optional.BigDecimal=12";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigDecimal=-19\n" + //
				"quarkus.Optional.BigDecimal=-19";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigDecimal=3.14159\n" + //
				"quarkus.Optional.BigDecimal=3.14159";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigDecimal=314.159e-2\n" + //
				"quarkus.Optional.BigDecimal=314.159e-2";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigDecimal=hello world\n" + //
				"quarkus.Optional.BigDecimal=hello world";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 30, "Type mismatch: java.math.BigDecimal expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 39, "Type mismatch: java.util.Optional<java.math.BigDecimal> expected", DiagnosticSeverity.Error, ValidationType.value));
		
		value = "quarkus.BigDecimal=true\n" + //
				"quarkus.Optional.BigDecimal=true";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 23, "Type mismatch: java.math.BigDecimal expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 32, "Type mismatch: java.util.Optional<java.math.BigDecimal> expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateBigIntegerError() throws BadLocationException {
		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		List<ItemMetadata> properties = new ArrayList<ItemMetadata>();
		ItemMetadata p1 = new ItemMetadata();
		p1.setName("quarkus.BigInteger");
		p1.setType("java.math.BigInteger");
		properties.add(p1);
		ItemMetadata p2 = new ItemMetadata();
		p2.setName("quarkus.Optional.BigInteger");
		p2.setType("java.util.Optional<java.math.BigInteger>");
		properties.add(p2);
		projectInfo.setProperties(properties);

		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		
		String value = "quarkus.BigInteger=12\n" + //
				"quarkus.Optional.BigInteger=12";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigInteger=-19\n" + //
				"quarkus.Optional.BigInteger=-19";
		testDiagnosticsFor(value, projectInfo, settings);

		value = "quarkus.BigInteger=hello world\n" + //
				"quarkus.Optional.BigInteger=hello world";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 30, "Type mismatch: java.math.BigInteger expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 39, "Type mismatch: java.util.Optional<java.math.BigInteger> expected", DiagnosticSeverity.Error, ValidationType.value));
		
		value = "quarkus.BigInteger=true\n" + //
				"quarkus.Optional.BigInteger=true";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 23, "Type mismatch: java.math.BigInteger expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 32, "Type mismatch: java.util.Optional<java.math.BigInteger> expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.BigInteger=3.14159\n" + //
				"quarkus.Optional.BigInteger=3.14159";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 26, "Type mismatch: java.math.BigInteger expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 35, "Type mismatch: java.util.Optional<java.math.BigInteger> expected", DiagnosticSeverity.Error, ValidationType.value));

		value = "quarkus.BigInteger=314.159e-2\n" + //
				"quarkus.Optional.BigInteger=314.159e-2";
		testDiagnosticsFor(value, projectInfo, settings,
				d(0, 19, 29, "Type mismatch: java.math.BigInteger expected", DiagnosticSeverity.Error, ValidationType.value),
				d(1, 28, 38, "Type mismatch: java.util.Optional<java.math.BigInteger> expected", DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateBuildTimeInjectValues() throws BadLocationException {
		String value = "quarkus.http.cors = ${value.one}\n" + //
				"quarkus.http.port=${value_two}\n" + //
				"quarkus.ssl.native=    ${value-three}";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	}

	@Test
	public void validateValueForLevelBasedOnRule() throws BadLocationException {
		// quarkus.log.file.level has 'java.util.logging.Level' which has no
		// enumeration
		// to fix it, quarkus-values-rules.json defines the Level enumerations
		String value = "quarkus.log.file.level=XXX ";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings,
				d(0, 23, 27, "Invalid enum value: 'XXX' is invalid for type java.util.logging.Level",
						DiagnosticSeverity.Error, ValidationType.value));
	}

	@Test
	public void validateValueForTransactionIsolationLevelEnumKebabCase() {
		String value = "quarkus.datasource.transaction-isolation-level = READ_UNCOMMITTED";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	}

	@Test
	public void validateValueForTransactionIsolationLevelEnumVerbatim() {
		String value = "quarkus.datasource.transaction-isolation-level = read-uncommitted";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);
	}

	@Test
	public void validateAccordingConverterKinds() {
		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		projectInfo.setProperties(new ArrayList<>());
		projectInfo.setHints(new ArrayList<>());
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		ItemMetadata p1 = new ItemMetadata();
		p1.setName("property.converters.none");
		p1.setType("MyEnumType");
		projectInfo.getProperties().add(p1);

		ItemMetadata p2 = new ItemMetadata();
		p2.setName("property.converters.verbatim");
		p2.setConverterKinds(Arrays.asList(ConverterKind.VERBATIM));
		p2.setType("MyEnumType");
		projectInfo.getProperties().add(p2);

		ItemMetadata p3 = new ItemMetadata();
		p3.setName("property.converters.kebab_case");
		p3.setConverterKinds(Arrays.asList(ConverterKind.KEBAB_CASE));
		p3.setType("MyEnumType");
		projectInfo.getProperties().add(p3);

		ItemMetadata p4 = new ItemMetadata();
		p4.setName("property.converters.both");
		p4.setConverterKinds(Arrays.asList(ConverterKind.KEBAB_CASE, ConverterKind.VERBATIM));
		p4.setType("MyEnumType");
		projectInfo.getProperties().add(p4);

		ItemHint hint = new ItemHint();
		hint.setName("MyEnumType");
		hint.setValues(new ArrayList<>());
		ValueHint valueHint = new ValueHint();
		valueHint.setValue("READ_UNCOMMITTED");
		hint.getValues().add(valueHint);
		projectInfo.getHints().add(hint);

		// No converter
		String value = "property.converters.none = READ_UNCOMMITTED";
		testDiagnosticsFor(value, projectInfo, settings);
		value = "property.converters.none = read-uncommitted";
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 27, 43, "Invalid enum value: 'read-uncommitted' is invalid for type MyEnumType",
						DiagnosticSeverity.Error, ValidationType.value));

		// verbatim converter
		value = "property.converters.verbatim = READ_UNCOMMITTED";
		testDiagnosticsFor(value, projectInfo, settings);
		value = "property.converters.verbatim = read-uncommitted";
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 31, 47, "Invalid enum value: 'read-uncommitted' is invalid for type MyEnumType",
						DiagnosticSeverity.Error, ValidationType.value));

		// kebab_case converter
		value = "property.converters.kebab_case = read-uncommitted";
		testDiagnosticsFor(value, projectInfo, settings);
		value = "property.converters.kebab_case = READ_UNCOMMITTED";
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 33, 49, "Invalid enum value: 'READ_UNCOMMITTED' is invalid for type MyEnumType",
						DiagnosticSeverity.Error, ValidationType.value));

		// both converters
		value = "property.converters.both = read-uncommitted";
		testDiagnosticsFor(value, projectInfo, settings);
		value = "property.converters.both = READ_UNCOMMITTED";
		testDiagnosticsFor(value, projectInfo, settings);

	}

	@Test
	public void validateRegexPatternValue() {
		MicroProfileProjectInfo projectInfo = new MicroProfileProjectInfo();
		projectInfo.setProperties(new ArrayList<>());
		projectInfo.setHints(new ArrayList<>());
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();

		ItemMetadata p1 = new ItemMetadata();
		p1.setName("mp.opentracing.server.skip-pattern");
		p1.setRequired(false);
		p1.setType("java.util.Optional<java.util.regex.Pattern>");
		projectInfo.getProperties().add(p1);

		String value = "mp.opentracing.server.skip-pattern=/foo|/bar.*";
		testDiagnosticsFor(value, projectInfo, settings);
		
		String ls = System.lineSeparator();
		value = "mp.opentracing.server.skip-pattern=(";

		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 35, 36, "Unclosed group near index 1" + ls + "(" + ls + "",
						DiagnosticSeverity.Error, ValidationType.value));

		value = "mp.opentracing.server.skip-pattern=[";
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 35, 36, "Unclosed character class near index 0" + ls + "[" + ls + "^" + ls + "",
						DiagnosticSeverity.Error, ValidationType.value));
		
		value = "mp.opentracing.server.skip-pattern=\\";
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 35, 36, "Unexpected internal error near index 1" + ls + "\\" + ls + "",
						DiagnosticSeverity.Error, ValidationType.value));

		value = "mp.opentracing.server.skip-pattern={";
		
		StringBuilder message = new StringBuilder("Illegal repetition");
		if (JavaVersion.CURRENT > 12) {
			message.append(" near index 1");
		}
		message.append(ls).append("{").append(ls);
		
		testDiagnosticsFor(value, projectInfo, settings, //
				d(0, 35, 36, message.toString(),
						DiagnosticSeverity.Error, ValidationType.value));			
	}

	@Test
	public void validateMultilineKey() {
		String value = "quarkus.\\\n" +
		"application.\\\n" +
		"name=name";
		MicroProfileValidationSettings settings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "quarkus.\\\r\n" +
		"application.\\\r\n" +
		"name=name";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings);

		value = "qu.\\\n" +
		"application.\\\n" +
		"name=name";
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), settings, //
				d(0, 0, 2, 4, "Unknown property 'qu.application.name'",
						DiagnosticSeverity.Warning, ValidationType.unknown));
	}
}
