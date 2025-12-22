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

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.ls.QuteTextDocumentService;

/**
 * Mock Qute Language server which helps to track show messages, actionable
 * notification and commands.
 *
 */
public class MockQuteLanguageServer extends QuteLanguageServer {

	public MockQuteLanguageServer() {
		super.setClient(new MockQuteLanguageClient());
		super.getProjectRegistry().setDidChangeWatchedFilesSupported(true);
	}

	@Override
	protected QuteProjectRegistry createProjectRegistry() {
		return new MockQuteProjectRegistry(this, this, this, this, this, this, this, this, //
				() -> null);
	}

	public Set<PublishDiagnosticsParams> getPublishDiagnostics() {
		return getLanguageClient().getPublishDiagnostics();
	}

	@Override
	public MockQuteLanguageClient getLanguageClient() {
		return (MockQuteLanguageClient) super.getLanguageClient();
	}

	public TextDocumentIdentifier didOpen(String fileUri, String fileContent) {
		TextDocumentIdentifier quteIdentifier = new TextDocumentIdentifier(fileUri);
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(
				new TextDocumentItem(quteIdentifier.getUri(), "qute", 1, fileContent));
		QuteTextDocumentService textDocumentService = (QuteTextDocumentService) super.getTextDocumentService();
		textDocumentService.didOpen(params);
		return quteIdentifier;
	}

	public void didChange(String fileUri, String text, int line, int startCharacter, int endCharacter) {
		DidChangeTextDocumentParams params = new DidChangeTextDocumentParams();
		params.setTextDocument(new VersionedTextDocumentIdentifier(fileUri, 0));
		TextDocumentContentChangeEvent event = new TextDocumentContentChangeEvent();
		event.setText(text);
		event.setRange(new Range(new Position(line, startCharacter), new Position(line, endCharacter)));
		event.setRangeLength(endCharacter - startCharacter);
		params.setContentChanges(List.of(event));
		getTextDocumentService().didChange(params);
	}

	public Hover hover(String fileUri, int line, int character) {
		HoverParams params = new HoverParams();
		params.setTextDocument(new TextDocumentIdentifier(fileUri));
		params.setPosition(new Position(line, character));
		try {
			return getTextDocumentService().hover(params).get();
		} catch (Exception e) {
			return null;
		}
	}

	public void deleteFile(Path filePath) throws Exception {
		Files.delete(filePath);
		String uri = FileUtils.toUri(filePath);
		FileEvent deleteEvent = new FileEvent(uri, FileChangeType.Deleted);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(deleteEvent));
		super.didChangeWatchedFiles(params);
	}

	public void createFile(Path filePath) throws Exception {
		Files.createFile(filePath);
		String uri = FileUtils.toUri(filePath);

		FileEvent createEvent = new FileEvent(uri, FileChangeType.Created);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(createEvent));
		super.didChangeWatchedFiles(params);

		FileEvent changedEvent = new FileEvent(uri, FileChangeType.Changed);
		params = new DidChangeWatchedFilesParams(Arrays.asList(changedEvent));
		super.didChangeWatchedFiles(params);
	}

	public void changeFile(Path filePath, String content) throws Exception {
		try (Writer writer = Files.newBufferedWriter(filePath)) {
			writer.append(content);
			writer.flush();
		} catch (IOException e) {

		} finally {

		}
		String uri = FileUtils.toUri(filePath);
		FileEvent createEvent = new FileEvent(uri, FileChangeType.Changed);
		DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(Arrays.asList(createEvent));
		super.didChangeWatchedFiles(params);
	}

}
