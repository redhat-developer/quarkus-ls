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
package com.redhat.qute.services.diagnostics.project;

import static com.redhat.qute.QuteAssert.assertDiagnostics;
import static com.redhat.qute.QuteAssert.d;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.project.MockProjectQuteLanguageServer;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

import static com.redhat.qute.project.MockQuteLanguageServer.findPublishDiagnostics;

/**
 * Diagnostics tests with closed/opened Qute template in a given project with
 * LSP DidChangeWatchedFiles capability.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInProjectWithLSPDidChangeWatchedFilesTest {

	private static class QuteQuickStartProjectLanguageServer extends MockProjectQuteLanguageServer {

		public QuteQuickStartProjectLanguageServer() {
			super(QuteQuickStartProject.PROJECT_URI);
		}
	}

	@Test
	public void ValidateClosedAndOpenedTemplates() throws Exception {

		QuteQuickStartProjectLanguageServer server = new QuteQuickStartProjectLanguageServer();
		server.getProjectRegistry().setDidChangeWatchedFilesSupported(true);

		// 1) On load: load detail.html
		onLoadFilesTest(server);

		// 2) On delete: delete detail_error.html
		onDeleteFile(server);

		// 3) On create: create empty detail_error.html
		onCreateFile(server);

		// 4) On change: update detail_error.html
		onChangeFile(server);
	}

	private void onLoadFilesTest(QuteQuickStartProjectLanguageServer server) throws IOException {

		// Open detail.html
		server.didOpenFle("detail.html");

		Collection<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(), d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
				"Template not found: `bad`.", //
				DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Warning));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

	private void onDeleteFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		server.deleteFile("detail_error.html");

		Collection<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

	}

	private void onCreateFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		server.createFile("detail_error.html");

		Collection<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertTrue(diagnostics.size() >= 3);

		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());
	}

	private void onChangeFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		String template = "{#include bad} \r\n" + //
				"  {#title}My Title{/title} \r\n" + //
				"  <div> \r\n" + //
				"    My body.\r\n" + //
				"  </div>\r\n" + //
				"{/include}";

		server.changeFile("detail_error.html", template);

		Collection<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(), d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
				"Template not found: `bad`.", //
				DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Warning));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

}
