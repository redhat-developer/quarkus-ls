/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.commons.QuteJavaDiagnosticsParams;
import com.redhat.qute.commons.QuteJavaDocumentLinkParams;
import com.redhat.qute.ls.AbstractTextDocumentService;
import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.ls.commons.TextDocuments;
import com.redhat.qute.ls.commons.ValidatorDelayer;
import com.redhat.qute.settings.SharedSettings;

public class JavaFileTextDocumentService extends AbstractTextDocumentService {

	private final TextDocuments<TextDocument> textDocuments;
	private final ValidatorDelayer<TextDocument> validatorDelayer;

	public JavaFileTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		super(quteLanguageServer, sharedSettings);
		textDocuments = new TextDocuments<>();
		validatorDelayer = new ValidatorDelayer<>((textDocument) -> {
			triggerValidationFor(textDocument);
		});
	}

	// ------------------------------ did* for Java file -------------------------

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocument textDocument = textDocuments.onDidOpenTextDocument(params);
		validate(textDocument, false);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		TextDocument textDocument = textDocuments.onDidChangeTextDocument(params);
		validate(textDocument, true);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		TextDocument textDocument = textDocuments.onDidCloseTextDocument(params);
		quteLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(textDocument.getUri(), new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {

	}

	// ------------------------------ Code Lens ------------------------------

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		if (!sharedSettings.getCodeLensSettings().isEnabled()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		QuteJavaCodeLensParams javaParams = new QuteJavaCodeLensParams(params.getTextDocument().getUri());
		return quteLanguageServer.getLanguageClient().getJavaCodelens(javaParams);
	}

	// ------------------------------ Document link ------------------------------

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		QuteJavaDocumentLinkParams javaParams = new QuteJavaDocumentLinkParams(params.getTextDocument().getUri());
		return quteLanguageServer.getLanguageClient().getJavaDocumentLink(javaParams);
	}

	// ------------------------------ Diagnostics ------------------------------

	private void validate(TextDocument textDocument, boolean delay) {
		if (delay) {
			validatorDelayer.validateWithDelay(textDocument);
		} else {
			triggerValidationFor(textDocument);
		}
	}

	private void triggerValidationFor(TextDocument textDocument) {
		triggerValidationFor(Arrays.asList(textDocument));
	}

	/**
	 * Validate all given Java files uris.
	 *
	 * @param textDocuments Java files uris to validate.
	 */
	private void triggerValidationFor(List<TextDocument> textDocuments) {
		if (textDocuments.isEmpty()) {
			return;
		}
		List<String> uris = textDocuments.stream().map(textDocument -> textDocument.getUri())
				.collect(Collectors.toList());
		QuteJavaDiagnosticsParams params = new QuteJavaDiagnosticsParams(uris);
		quteLanguageServer.getLanguageClient().getJavaDiagnostics(params) //
				.thenApply(diagnostics -> {
					if (diagnostics == null) {
						return null;
					}
					for (PublishDiagnosticsParams diagnostic : diagnostics) {
						quteLanguageServer.getLanguageClient().publishDiagnostics(diagnostic);
					}
					return null;
				});
	}

	@Override
	public FileType getFileType() {
		return FileType.JAVA;
	}
}
