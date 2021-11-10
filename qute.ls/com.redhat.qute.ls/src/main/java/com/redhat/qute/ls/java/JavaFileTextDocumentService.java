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
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import com.redhat.qute.settings.SharedSettings;

public class JavaFileTextDocumentService extends AbstractTextDocumentService {

	public JavaFileTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		super(quteLanguageServer, sharedSettings);
	}

	// ------------------------------ did* for Java file -------------------------

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		triggerValidationFor(params.getTextDocument().getUri());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		triggerValidationFor(params.getTextDocument().getUri());
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		String uri = params.getTextDocument().getUri();
		quteLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	public void updateClientCapabilities(ClientCapabilities capabilities) {

	}

	// ------------------------------ Code Lens ------------------------------

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
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

	private void triggerValidationFor(String uri) {
		triggerValidationFor(Arrays.asList(uri));
	}

	/**
	 * Validate all given Java files uris.
	 *
	 * @param uris Java files uris to validate.
	 */
	private void triggerValidationFor(List<String> uris) {
		if (uris.isEmpty()) {
			return;
		}
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
}
