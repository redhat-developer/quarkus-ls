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

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

/**
 * Syntax error from the real Qute parser.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsSyntaxError {

	@Test
	public void emptyParameterDeclaration() {
		String template = "{@}";
		testDiagnosticsFor(template, //
				d(0, 2, 0, 2, QuteErrorCode.SyntaxError, "Parser error on line 1: invalid parameter declaration {@}",
						DiagnosticSeverity.Error));
	}
}
