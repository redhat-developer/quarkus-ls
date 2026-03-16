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

import static com.redhat.qute.QuteAssert.assertDiagnostics;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.project.MockQuteLanguageServer.findPublishDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.roq.RoqProjectQuteLanguageServer;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test diagnostics with Roq Quarkus extension and layout.
 *
 * @author Angelo ZERR
 *
 */
public class RoqLayoutDiagnosticsTest {

	@Test
	public void layout() throws Exception {

		// template/layouts/default.hml defines {#insert head /}
		String template = "---\r\n" + //
				"layout: default\r\n" + //
				"---\r\n" + //
				"{#head /}\r\n" + // <-- head coming from default.html
				"{#headXXXX /}"; // <-- UndefinedSectionTag

		RoqProjectQuteLanguageServer server = new RoqProjectQuteLanguageServer();
		// Open user tag -> name parameter has no type
		server.didOpenWithContent("input.html", template);

		Collection<PublishDiagnosticsParams> publishDiagnostics = server.getPublishDiagnostics();
		PublishDiagnosticsParams inputDiagnostics = findPublishDiagnostics(publishDiagnostics, "input.html");
		assertEquals(1, inputDiagnostics.getDiagnostics().size());
		assertDiagnostics(inputDiagnostics.getDiagnostics(), d(4, 1, 4, 10, //
				QuteErrorCode.UndefinedSectionTag, //
				"No section helper found for `headXXXX`.", //
				DiagnosticSeverity.Warning));
	}

}
