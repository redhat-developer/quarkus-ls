/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;
import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;
import com.redhat.microprofile.ls.commons.client.CommandKind;
import com.redhat.microprofile.settings.MicroProfileCodeLensSettings;
import com.redhat.microprofile.settings.SharedSettings;
import com.redhat.microprofile.utils.DocumentationUtils;

/**
 * LSP text document service for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentService extends AbstractTextDocumentService {

	private final MicroProfileLanguageServer quarkusLanguageServer;
	private final SharedSettings sharedSettings;

	public JavaTextDocumentService(MicroProfileLanguageServer quarkusLanguageServer, SharedSettings sharedSettings) {
		this.quarkusLanguageServer = quarkusLanguageServer;
		this.sharedSettings = sharedSettings;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {

	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {

	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {

	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		boolean urlCodeLensEnabled = sharedSettings.getCodeLensSettings().isUrlCodeLensEnabled();
		;
		if (!urlCodeLensEnabled) {
			// Don't consume JDT LS extension if all code lens are disabled.
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		MicroProfileJavaCodeLensParams javaParams = new MicroProfileJavaCodeLensParams(
				params.getTextDocument().getUri());
		if (sharedSettings.getCommandCapabilities().isCommandSupported(CommandKind.COMMAND_OPEN_URI)) {
			javaParams.setOpenURICommand(CommandKind.COMMAND_OPEN_URI);
		}
		javaParams.setCheckServerAvailable(true);
		javaParams.setUrlCodeLensEnabled(urlCodeLensEnabled);
		// javaParams.setLocalServerPort(8080); // TODO : manage this server port from
		// the settings
		return quarkusLanguageServer.getLanguageClient().getJavaCodelens(javaParams);
	}

	public void updateCodeLensSettings(MicroProfileCodeLensSettings newCodeLens) {
		sharedSettings.getCodeLensSettings().setUrlCodeLensEnabled(newCodeLens.isUrlCodeLensEnabled());
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
		MicroProfileJavaHoverParams javaParams = new MicroProfileJavaHoverParams(params.getTextDocument().getUri(),
				params.getPosition());
		return quarkusLanguageServer.getLanguageClient().getJavaHover(javaParams).thenApply(info -> {

			if (info == null) {
				return null;
			}

			boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
			Hover h = DocumentationUtils.doHover(info, markdownSupported);
			return h;
		});
	}
}
