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
package com.redhat.qute.project;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Arrays;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;

/**
 * This class provides the capability to track the deleted/created files from
 * src/main/resources/templates folder of a Qute project and generates
 * the LSP {@link DidChangeWatchedFilesParams} to update closed document cache.
 * 
 * This class is used only when the LSP client cannot support
 * {@link DidChangeWatchedFilesParams}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteProjectFilesWatcher extends WatchDir {

	private QuteProject project;
	private final Thread thread;

	public QuteProjectFilesWatcher(QuteProject project) throws IOException {
		super(project.getTemplateBaseDir(), true);
		this.project = project;
		thread = new Thread(this);
		thread.setName("Watch Qute templates for '" + project.getUri() + "'");
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	protected void notifyListeners(WatchEvent<?> event, Path child) {
		if (Files.isDirectory(child)) {
			return;
		}
		FileEvent fileEvent = null;
		String fileUri = child.toUri().toASCIIString();
		if (event.kind() == ENTRY_CREATE) {
			fileEvent = new FileEvent(fileUri, FileChangeType.Created);
		} else if (event.kind() == ENTRY_MODIFY) {
			fileEvent = new FileEvent(fileUri, FileChangeType.Changed);
		} else if (event.kind() == ENTRY_DELETE) {
			fileEvent = new FileEvent(fileUri, FileChangeType.Deleted);
		}
		if (fileEvent != null) {
			DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(fileEvent));
			project.getProjectRegistry().didChangeWatchedFiles(params);
		}
	}

	@Override
	public void stop() {
		super.stop();
		thread.interrupt();
	}
}
