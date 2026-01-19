/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.roq;

import static com.redhat.qute.QuteAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test diagnostics with Roq Quarkus extension and data files.
 *
 * @author Angelo ZERR
 *
 */
public class RoqDataYamlDiagnosticsTest {

	@Test
	public void validInjectDataFile() throws Exception {
		// data/books.yaml
		String template = "{inject:books}";
		testDiagnosticsFor(template);
		template = "{inject:books.list}";
		testDiagnosticsFor(template);

		// data/sandwiches.yaml
		template = "{inject:sandwiches}";
		testDiagnosticsFor(template);
		template = "{inject:sandwiches.sandwiches}";
		testDiagnosticsFor(template);
	}

	@Test
	public void invalidInjectDataFile() throws Exception {
		String template = "{inject:booksXXX}";
		testDiagnosticsFor(template, //
				d(0, 8, 0, 16, QuteErrorCode.UndefinedObject, "`booksXXX` cannot be resolved to an object.", //
						"qute", DiagnosticSeverity.Warning));
		template = "{inject:books.listXXX}";
		testDiagnosticsFor(template, //
				d(0, 14, 0, 21, QuteErrorCode.UnknownProperty,
						"`listXXX` cannot be resolved or is not a field of `null` Java type.", //
						"qute", DiagnosticSeverity.Error));
	}

	@Test
	public void validInjectDataFileInForSection() throws Exception {
		String template = "{#for b in inject:books.list}\r\n" + //
				"    {b.title}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void nestedFields() throws Exception {
		String template = "{#for item in inject:sandwiches.sandwiches}\r\n" + //
				"    <h2>{item.name}</h2>\r\n" + //
				"    <ul>\r\n" + //
				"    {#for ingredient in item.ingredients}\r\n" + //
				"        <li>{ingredient}</li>\r\n" + //
				"    {/for}\r\n" + //
				"    </ul>\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void invalidInjectDataFileInForSection() throws Exception {
		String template = "{#for b in inject:books.list}\r\n" + //
				"    {b.titleXXX}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template, //
				d(1, 7, 1, 15, QuteErrorCode.UnknownProperty,
						"`titleXXX` cannot be resolved or is not a field of `java.lang.Object` Java type.", //
						"qute", DiagnosticSeverity.Error));
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, null, expected);
	}
	
	

}
