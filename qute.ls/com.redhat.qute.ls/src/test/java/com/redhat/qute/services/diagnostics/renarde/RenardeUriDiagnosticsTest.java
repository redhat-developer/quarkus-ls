/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.renarde;

import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test with Renarde uri/uriabs.
 *
 * @author Angelo ZERR
 *
 */
public class RenardeUriDiagnosticsTest {

	@Test
	public void badController() throws Exception {
		testDiagnosticsFor("{uri:XXXX.confirm()}", //
				d(0, 5, 0, 9, QuteErrorCode.UndefinedObject,
						"`XXXX` cannot be resolved to an object.",
						DiagnosticSeverity.Warning));
	}

	@Test
	public void confirm() throws Exception {
		testDiagnosticsFor("{uri:Login.confirm('ok')}");
		testDiagnosticsFor("{uri:Login.confirm()}", //
				d(0, 11, 0, 18, QuteErrorCode.InvalidMethodParameter,
						"The method `confirm(String)` in the type `Login` is not applicable for the arguments `()`.",
						DiagnosticSeverity.Error));
		testDiagnosticsFor("{uri:Login.confirm(0)}", //
				d(0, 11, 0, 18, QuteErrorCode.InvalidMethodParameter,
						"The method `confirm(String)` in the type `Login` is not applicable for the arguments `(Integer)`.",
						DiagnosticSeverity.Error));
		testDiagnosticsFor("{uri:Login.confirm('ok',0)}", //
				d(0, 11, 0, 18, QuteErrorCode.InvalidMethodParameter,
						"The method `confirm(String)` in the type `Login` is not applicable for the arguments `(String, Integer)`.",
						DiagnosticSeverity.Error));

	}

	@Test
	public void manualLogin() throws Exception {
		testDiagnosticsFor("{uri:Login.manualLogin()}");
		testDiagnosticsFor("{uri:Login.manualLogin('nok')}", //
				d(0, 11, 0, 22, QuteErrorCode.InvalidMethodParameter,
						"The method `manualLogin(String, String, WebAuthnLoginResponse, RoutingContext)` in the type `Login` is not applicable for the arguments `(String)`.",
						DiagnosticSeverity.Error));

	}

	@Test
	public void timeoutGame() throws Exception {
		testDiagnosticsFor("{uri:Login.timeoutGame()}");
		testDiagnosticsFor("{uri:Login.timeoutGame}");
		testDiagnosticsFor("{uri:Login.timeoutGame('nok')}", //
				d(0, 11, 0, 22, QuteErrorCode.InvalidMethodParameter,
						"The method `timeoutGame()` in the type `Login` is not applicable for the arguments `(String)`.",
						DiagnosticSeverity.Error));

	}
}
