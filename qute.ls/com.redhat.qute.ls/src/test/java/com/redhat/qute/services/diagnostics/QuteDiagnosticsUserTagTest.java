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

import static com.redhat.qute.QuteAssert.ca;
import static com.redhat.qute.QuteAssert.createFile;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.getFileUri;
import static com.redhat.qute.QuteAssert.teOp;
import static com.redhat.qute.QuteAssert.testCodeActionsFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * User tag diagnostics.
 *
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsUserTagTest {

	@Test
	public void undefinedVariableInTemplate() {
		String template = "{name}";
		Diagnostic d = d(0, 1, 0, 5, QuteErrorCode.UndefinedObject, "`name` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("name", false));
		testDiagnosticsFor(template, //
				"src/main/resources/templates/user.html", //
				"user", //
				d);
	}

	@Test
	public void undefinedVariableInUserTagTemplate() {
		String template = "{name}";
		testDiagnosticsFor(template, //
				"src/main/resources/templates/tags/user.html", //
				"tags/user");
	}

	@Test
	public void undefinedVariableInUserTagCall() {
		String template = "{#user name=name /}";
		Diagnostic d = d(0, 12, 0, 16, QuteErrorCode.UndefinedObject, "`name` cannot be resolved to an object.",
				DiagnosticSeverity.Warning);
		d.setData(DiagnosticDataFactory.createUndefinedVariableData("name", false));
		testDiagnosticsFor(template, d);
	}

	@Test
	public void stringParameterInUserTagCall() {
		String template = "{#user name=\"User Name\" /}";
		testDiagnosticsFor(template);
	}
	
	@Test
	public void undefinedSectionTag() throws Exception {
		String template = "{#undefined /}";

		Diagnostic d = d(0, 1, 0, 11, QuteErrorCode.UndefinedSectionTag, "No section helper found for `undefined`.",
				DiagnosticSeverity.Error);
		d.setData(DiagnosticDataFactory.createUndefinedSectionTagData("undefined"));
		testDiagnosticsFor(template, //
				d);

		String userTagUri = getFileUri("/tags/undefined.html");
		testCodeActionsFor(template, d, //
				ca(d, //
						createFile(userTagUri, false), //
						teOp(userTagUri, 0, 0, 0, 0, //
								"")));
	}

}
