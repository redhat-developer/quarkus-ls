/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.ls.commons.TextDocuments;
import com.redhat.quarkus.services.QuarkusLanguageService;

/**
 * Quarkus text document service.
 *
 */
public class QuarkusTextDocumentService implements TextDocumentService {

	private final TextDocuments<TextDocument> documents;

	private final QuarkusProjectInfoCache projectInfoCache;

	private final QuarkusLanguageServer quarkusLanguageServer;

	public QuarkusTextDocumentService(QuarkusLanguageServer quarkusLanguageServer) {
		this.quarkusLanguageServer = quarkusLanguageServer;
		this.documents = new TextDocuments<TextDocument>();
		this.projectInfoCache = new QuarkusProjectInfoCache(quarkusLanguageServer);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocument document = documents.onDidOpenTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		TextDocument document = documents.onDidChangeTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		quarkusLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		String uri = params.getTextDocument().getUri();
		return projectInfoCache.getQuarkusProjectInfo(uri).thenApplyAsync(projectInfo -> {
			if (!projectInfo.isQuarkusProject()) {
				return Either.forRight(new CompletionList());
			}
			TextDocument document = documents.get(uri);
			CompletionList list = getQuarkusLanguageService().doComplete(document, params.getPosition(), projectInfo,
					null);
			return Either.forRight(list);
		});
	}

	private QuarkusLanguageService getQuarkusLanguageService() {
		return quarkusLanguageServer.getQuarkusLanguageService();
	}

	private void triggerValidationFor(TextDocument document) {
		// TODO: implement validation for application.properties
	}

}
