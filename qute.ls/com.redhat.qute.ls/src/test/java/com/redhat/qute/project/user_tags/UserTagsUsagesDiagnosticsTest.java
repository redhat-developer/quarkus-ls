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
package com.redhat.qute.project.user_tags;

import static com.redhat.qute.QuteAssert.assertDiagnostics;
import static com.redhat.qute.QuteAssert.d;
import static com.redhat.qute.project.MockQuteLanguageServer.findPublishDiagnostics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.services.diagnostics.JavaBaseTypeOfPartData;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test diagnostics on user tag parameters that infer their type based on how
 * the user tag is used.
 */
public class UserTagsUsagesDiagnosticsTest {

	@Test
	public void diagnostics() throws Exception {
		RenardeProjectQuteLanguageServer server = new RenardeProjectQuteLanguageServer();

		// Open user tag -> name parameter has no type --> no error with byteValue
		server.didOpenWithContent("tags/input.html", "<input name=\"{name.byteValue}\" />");
		// Diagnostics published on closed document
		Collection<PublishDiagnosticsParams> publishDiagnostics = server.getPublishDiagnostics();
		assertEquals(1, publishDiagnostics.size());
		assertTrue(publishDiagnostics.iterator().next().getDiagnostics().isEmpty());

		// Open a template which call user tag with String type -> name has String
		// type
		server.didOpenWithContent("main.html", "{#input name=\"FOO\" /}");
		// Diagnostics published on opened document
		publishDiagnostics = server.getPublishDiagnostics();
		assertEquals(1, publishDiagnostics.size());
		assertTrue(publishDiagnostics.iterator().next().getDiagnostics().isEmpty());

		// use tags has no error with byteValue since main.html is not saved.
		assertTrue(server.getPublishDiagnostics().isEmpty());

		// save main.html retrigger validation for all documents
		server.didSaveFile("main.html");

		publishDiagnostics = server.getPublishDiagnostics();
		assertEquals(2, publishDiagnostics.size());

		PublishDiagnosticsParams inputDiagnostics = findPublishDiagnostics(publishDiagnostics, "input.html");
		assertEquals(1, inputDiagnostics.getDiagnostics().size());
		assertDiagnostics(inputDiagnostics.getDiagnostics(), d(0, 19, 0, 28, //
				QuteErrorCode.UnknownProperty, //
				"`byteValue` cannot be resolved or is not a field of `java.lang.String` Java type.", //
				new JavaBaseTypeOfPartData("java.lang.String"), //
				DiagnosticSeverity.Error));
	}

}
