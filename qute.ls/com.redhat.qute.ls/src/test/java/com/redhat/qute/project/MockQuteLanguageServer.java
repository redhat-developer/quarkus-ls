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

import java.util.List;

import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

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
	}

	public List<PublishDiagnosticsParams> getPublishDiagnostics() {
		return getLanguageClient().getPublishDiagnostics();
	}

	@Override
	public MockQuteLanguageClient getLanguageClient() {
		return (MockQuteLanguageClient) super.getLanguageClient();
	}

	public TextDocumentIdentifier didOpen(String fileURI, String template) {
		TextDocumentIdentifier quteIdentifier = new TextDocumentIdentifier(fileURI);
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams(
				new TextDocumentItem(quteIdentifier.getUri(), "qute", 1, template));
	    QuteTextDocumentService textDocumentService = (QuteTextDocumentService) super.getTextDocumentService();
		textDocumentService.didOpen(params);
		return quteIdentifier;
	}
}
