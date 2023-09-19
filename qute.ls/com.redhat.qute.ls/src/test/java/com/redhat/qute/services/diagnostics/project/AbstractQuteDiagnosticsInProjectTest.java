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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.junit.jupiter.api.Test;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.project.MockQuteLanguageServer;
import com.redhat.qute.project.QuteQuickStartProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.utils.FileUtils;
import com.redhat.qute.utils.IOUtils;

/**
 * Abstract class for testing diagnostics with closed/opened Qute template in a
 * given project.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractQuteDiagnosticsInProjectTest {

	private static class QuteQuickStartProjectLanguageServer extends MockQuteLanguageServer {

		private final Path templatesPath;

		public QuteQuickStartProjectLanguageServer() {
			templatesPath = FileUtils
					.createPath("src/test/resources/projects/qute-quickstart/src/main/resources/templates");
		}

		@Override
		public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
			ProjectInfo projectInfo = createQuickStartProject();
			return CompletableFuture.completedFuture(projectInfo);
		}

		@Override
		public CompletableFuture<Collection<ProjectInfo>> getProjects() {
			Collection<ProjectInfo> projects = Arrays.asList(createQuickStartProject());
			return CompletableFuture.completedFuture(projects);
		}

		private ProjectInfo createQuickStartProject() {
			ProjectInfo projectInfo = new ProjectInfo(QuteQuickStartProject.PROJECT_URI,
					Collections.emptyList(),
					FileUtils.toUri(templatesPath));
			return projectInfo;
		};

	}

	private final boolean didChangeWatchedFilesSupported;

	public AbstractQuteDiagnosticsInProjectTest(boolean didChangeWatchedFilesSupported) {
		this.didChangeWatchedFilesSupported = didChangeWatchedFilesSupported;
	}

	@Test
	public void ValidateClosedAndOpenedTemplates() throws Exception {

		QuteQuickStartProjectLanguageServer server = new QuteQuickStartProjectLanguageServer();
		server.getProjectRegistry().setDidChangeWatchedFilesSupported(didChangeWatchedFilesSupported);
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
		server.getPublishDiagnostics().clear();

		// Open detail.html
		Path filePath = server.templatesPath.resolve("detail.html");
		String fileUri = FileUtils.toUri(filePath);
		String template = IOUtils.getContent(filePath);
		server.didOpen(fileUri, template);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(),
				d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
						"Template not found: `bad`.", //
						DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Error));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

	private void onDeleteFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		server.getPublishDiagnostics().clear();

		Path detailErrorPath = server.templatesPath.resolve("detail_error.html");
		deleteFile(detailErrorPath, server);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertEquals(3, diagnostics.size());

		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

	}

	private void onCreateFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		server.getPublishDiagnostics().clear();

		Path detailErrorPath = server.templatesPath.resolve("detail_error.html");
		createFile(detailErrorPath, server);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertTrue(diagnostics.size() >= 3);

		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		detailDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());
	}

	private void onChangeFile(QuteQuickStartProjectLanguageServer server) throws Exception {
		server.getPublishDiagnostics().clear();

		String template = "{#include bad} \r\n"
				+ "  {#title}My Title{/title} \r\n"
				+ "  <div> \r\n"
				+ "    My body.\r\n"
				+ "  </div>\r\n"
				+ "{/include}";

		Path detailErrorPath = server.templatesPath.resolve("detail_error.html");
		changeFile(detailErrorPath, template, server);

		waitForDiagnostics();

		List<PublishDiagnosticsParams> diagnostics = server.getPublishDiagnostics();
		assertTrue(diagnostics.size() >= 3);

		// detail.html
		PublishDiagnosticsParams detailDiagnostics = findPublishDiagnostics(diagnostics, "detail.html");
		assertTrue(detailDiagnostics.getDiagnostics().isEmpty());

		// detail_error.html
		PublishDiagnosticsParams detailErrorDiagnostics = findPublishDiagnostics(diagnostics, "detail_error.html");
		assertEquals(2, detailErrorDiagnostics.getDiagnostics().size());
		assertDiagnostics(detailErrorDiagnostics.getDiagnostics(),
				d(0, 10, 0, 13, QuteErrorCode.TemplateNotFound, //
						"Template not found: `bad`.", //
						DiagnosticSeverity.Error), //
				d(1, 3, 1, 9, QuteErrorCode.UndefinedSectionTag, //
						"No section helper found for `title`.", //
						DiagnosticSeverity.Error));

		// base.html
		PublishDiagnosticsParams baseDiagnostics = findPublishDiagnostics(diagnostics, "base.html");
		assertTrue(baseDiagnostics.getDiagnostics().isEmpty());
	}

	private void waitForDiagnostics() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}

	private static PublishDiagnosticsParams findPublishDiagnostics(List<PublishDiagnosticsParams> diagnostics,
			String fileName) {
		for (PublishDiagnosticsParams diagnostic : diagnostics) {
			if (diagnostic.getUri().endsWith(fileName)) {
				return diagnostic;
			}
		}
		return null;
	}

	protected abstract void deleteFile(Path filePath, MockQuteLanguageServer server) throws Exception;

	protected abstract void createFile(Path filePath, MockQuteLanguageServer server) throws Exception;

	protected abstract void changeFile(Path filePath, String content, MockQuteLanguageServer server) throws Exception;
}
