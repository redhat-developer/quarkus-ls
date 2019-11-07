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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;

import com.redhat.quarkus.commons.QuarkusJavaCodeLensParams;
import com.redhat.quarkus.ls.commons.client.CommandKind;
import com.redhat.quarkus.settings.QuarkusCodeLensSettings;
import com.redhat.quarkus.settings.SharedSettings;

/**
 * LSP text document service for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentService extends AbstractTextDocumentService {

	private final QuarkusLanguageServer quarkusLanguageServer;
	private final SharedSettings sharedSettings;

	public JavaTextDocumentService(QuarkusLanguageServer quarkusLanguageServer, SharedSettings sharedSettings) {
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
		QuarkusJavaCodeLensParams javaParams = new QuarkusJavaCodeLensParams(params.getTextDocument().getUri());
		if (sharedSettings.getCommandCapabilities().isCommandSupported(CommandKind.COMMAND_OPEN_URI)) {
			javaParams.setOpenURICommand(CommandKind.COMMAND_OPEN_URI);
		}
		javaParams.setCheckServerAvailable(true);
		javaParams.setUrlCodeLensEnabled(urlCodeLensEnabled);
		// javaParams.setLocalServerPort(8080); // TODO : manage this server port from
		// the settings
		return quarkusLanguageServer.getLanguageClient().quarkusJavaCodelens(javaParams);
	}

	public void updateCodeLensSettings(QuarkusCodeLensSettings newCodeLens) {
		sharedSettings.getCodeLensSettings().setUrlCodeLensEnabled(newCodeLens.isUrlCodeLensEnabled());
	}

}
