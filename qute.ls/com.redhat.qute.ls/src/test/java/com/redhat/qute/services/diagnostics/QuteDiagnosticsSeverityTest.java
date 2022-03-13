/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.settings.QuteValidationSettings.Severity;
import com.redhat.qute.settings.QuteValidationTypeSettings;

/**
 * Test with diagnostic severity setting
 *
 */
public class QuteDiagnosticsSeverityTest {

	@Test
	public void undefinedObjectSeverityDefault() throws Exception {
		String template = "{foo}";
		QuteValidationSettings validationSettings = new QuteValidationSettings();
		Diagnostic d = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, //
				"`foo` cannot be resolved to an object.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("foo", false));
		testDiagnosticsFor(template, validationSettings, d);
	}

	@Test
	public void undefinedObjectSeverityIgnore() throws Exception {
		String template = "{foo}";
		QuteValidationSettings validationSettings = new QuteValidationSettings();
		QuteValidationTypeSettings undefinedObject = new QuteValidationTypeSettings();
		undefinedObject.setSeverity(Severity.ignore.name());
		validationSettings.setUndefinedObject(undefinedObject);
		testDiagnosticsFor(template, validationSettings);
	}

	@Test
	public void undefinedObjectSeverityWarning() throws Exception {
		String template = "{foo}";
		QuteValidationSettings validationSettings = new QuteValidationSettings();
		QuteValidationTypeSettings undefinedObject = new QuteValidationTypeSettings();
		undefinedObject.setSeverity(Severity.warning.name());
		validationSettings.setUndefinedObject(undefinedObject);
		Diagnostic d = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, //
				"`foo` cannot be resolved to an object.", DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("foo", false));
		testDiagnosticsFor(template, validationSettings, d);
	}

	@Test
	public void undefinedObjectSeverityError() throws Exception {
		String template = "{foo}";
		QuteValidationSettings validationSettings = new QuteValidationSettings();
		QuteValidationTypeSettings undefinedObject = new QuteValidationTypeSettings();
		undefinedObject.setSeverity(Severity.error.name());
		validationSettings.setUndefinedObject(undefinedObject);
		Diagnostic d = d(0, 1, 0, 4, QuteErrorCode.UndefinedObject, //
				"`foo` cannot be resolved to an object.", DiagnosticSeverity.Error);
		d.setData(DiagnosticDataFactory.createUndefinedObjectData("foo", false));
		testDiagnosticsFor(template, validationSettings, d);
	}

}
