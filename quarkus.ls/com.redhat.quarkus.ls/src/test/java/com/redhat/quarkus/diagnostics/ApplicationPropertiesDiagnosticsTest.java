/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.diagnostics;

import static com.redhat.quarkus.services.QuarkusAssert.d;
import static com.redhat.quarkus.services.QuarkusAssert.te;
import static com.redhat.quarkus.services.QuarkusAssert.ca;
import static com.redhat.quarkus.services.QuarkusAssert.testDiagnosticsFor;
import static com.redhat.quarkus.services.QuarkusAssert.testCodeActionsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.Test;

/**
 * Test diagnostics in 'application.properties' file.
 *
 */
public class ApplicationPropertiesDiagnosticsTest {

	@Test
	public void testMissingEqualsSignOnlyKey() {
		String content = "quarkus.application.name";

		Diagnostic d = d(0, 0, 0, content.length(), "missing_equals_after_key");
		testDiagnosticsFor(content, d);
		testCodeActionsFor(content, d, ca(d, te(0, content.length(), 0, content.length(), " = ")));
	}

	@Test
	public void testMissingEqualsSignComment() {
		String content = "quarkus.application.name # ====";
		Diagnostic d = d(0, 0, 0, content.length(), "missing_equals_after_key");
		testDiagnosticsFor(content, d);
	}

	@Test
	public void testMissingEqualsSignOnlyKeyMultipleLines() {
		String key0 = "quarkus.application.name";
		String key1 = "quarkus.application.version";
		String key2 = "quarkus.datasource.background-validation-interval";
		String key3 = "quarkus.datasource.idle-removal-interval";

		String content =
		key0 + System.lineSeparator() + 
		key1 + System.lineSeparator() + 
		key2 + System.lineSeparator() + 
		key3;

		Diagnostic d0 = d(0, 0, 0, key0.length(), "missing_equals_after_key");
		Diagnostic d1 = d(1, 0, 1, key1.length(), "missing_equals_after_key");
		Diagnostic d2 = d(2, 0, 2, key2.length(), "missing_equals_after_key");
		Diagnostic d3 = d(3, 0, 3, key3.length(), "missing_equals_after_key");

		testDiagnosticsFor(content, d0, d1, d2, d3);
		testCodeActionsFor(content, d0, ca(d0, te(0, key0.length(), 0, key0.length(), " = ")));
		testCodeActionsFor(content, d1, ca(d1, te(1, key1.length(), 1, key1.length(), " = ")));
		testCodeActionsFor(content, d2, ca(d2, te(2, key2.length(), 2, key2.length(), " = ")));
		testCodeActionsFor(content, d3, ca(d3, te(3, key3.length(), 3, key3.length(), " = ")));
	}

	@Test
	public void testNoMissingEqualsSignEmptyFile() {
		String content = "";
		testDiagnosticsFor(content);
	}

	@Test
	public void testNoMissingEqualsSignComment() {
		String content = "# quarkus.application.name";
		testDiagnosticsFor(content);
	}


}