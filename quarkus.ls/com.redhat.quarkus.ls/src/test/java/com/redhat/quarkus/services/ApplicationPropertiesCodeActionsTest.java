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

import static com.redhat.quarkus.services.QuarkusAssert.ca;
import static com.redhat.quarkus.services.QuarkusAssert.d;
import static com.redhat.quarkus.services.QuarkusAssert.te;
import static com.redhat.quarkus.services.QuarkusAssert.testCodeActionsFor;
import static com.redhat.quarkus.services.QuarkusAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.Test;

import com.redhat.quarkus.ls.commons.BadLocationException;

/**
 * Test with code actions in 'application.properties' file.
 * 
 * @author Angelo ZERR
 *
 */
public class ApplicationPropertiesCodeActionsTest {

	@Test
	public void codeActionsForUnknownProperties() throws BadLocationException {
		String value = "# quarkus.datasource.url=jdbc:postgresql:quarkus_test\n" + //
				"quarkus.application.nme=X\n" + // <-- error
				"\n" + //
				"";
		Diagnostic d = d(1, 0, 23, "Unknown property 'quarkus.application.nme'", DiagnosticSeverity.Warning,
				ValidationType.unknown);
		testDiagnosticsFor(value, d);
		testCodeActionsFor(value, d,
				ca("Did you mean 'quarkus.application.name' ?", te(1, 0, 1, 23, "quarkus.application.name"), d));
	};

}
