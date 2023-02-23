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

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;

import com.redhat.qute.project.MockQuteLanguageServer;

/**
 * Diagnostics tests with closed/opened Qute template in a given project with
 * LSP DidChangeWatchedFiles capability.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInProjectWithLSPDidChangeWatchedFilesTest extends AbstractQuteDiagnosticsInProjectTest {

	public QuteDiagnosticsInProjectWithLSPDidChangeWatchedFilesTest() {
		super(true);
	}

	@Override
	protected void deleteFile(Path filePath, MockQuteLanguageServer server) throws Exception {
		Files.delete(filePath);
		FileEvent deleteEvent = new FileEvent(filePath.toUri().toASCIIString(), FileChangeType.Deleted);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(deleteEvent));
		server.didChangeWatchedFiles(params);
	}

	@Override
	protected void createFile(Path filePath, MockQuteLanguageServer server) throws Exception {
		Files.createFile(filePath);
		String uri = filePath.toUri().toASCIIString();

		FileEvent createEvent = new FileEvent(uri, FileChangeType.Created);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(createEvent));
		server.didChangeWatchedFiles(params);

		FileEvent changedEvent = new FileEvent(uri, FileChangeType.Changed);
		params = new DidChangeWatchedFilesParams(Arrays.asList(changedEvent));
		server.didChangeWatchedFiles(params);
	}

	@Override
	protected void changeFile(Path filePath, String content, MockQuteLanguageServer server) throws Exception {
		try (Writer writer = Files.newBufferedWriter(filePath)) {
			writer.append(content);
			writer.flush();
		} catch (IOException e) {

		} finally {

		}
		FileEvent createEvent = new FileEvent(filePath.toUri().toASCIIString(), FileChangeType.Changed);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(createEvent));
		server.didChangeWatchedFiles(params);
	}
}
