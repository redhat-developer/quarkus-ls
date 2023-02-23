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

import com.redhat.qute.project.MockQuteLanguageServer;

/**
 * Diagnostics tests with closed/opened Qute template in a given project without
 * LSP DidChangeWatchedFiles capability.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDiagnosticsInProjectWithoutLSPDidChangeWatchedFilesTest extends AbstractQuteDiagnosticsInProjectTest {

	public QuteDiagnosticsInProjectWithoutLSPDidChangeWatchedFilesTest() {
		super(false);
	}
	
	@Override
	protected void deleteFile(Path filePath, MockQuteLanguageServer server) throws Exception {
		Files.delete(filePath);
	}

	@Override
	protected void createFile(Path filePath, MockQuteLanguageServer server) throws Exception {
		Files.createFile(filePath);
	}

	@Override
	protected void changeFile(Path filePath, String content, MockQuteLanguageServer server) throws Exception {
		try (Writer writer = Files.newBufferedWriter(filePath)) {
			writer.append(content);
			writer.flush();
		} catch (IOException e) {

		} finally {

		}

	}
}
