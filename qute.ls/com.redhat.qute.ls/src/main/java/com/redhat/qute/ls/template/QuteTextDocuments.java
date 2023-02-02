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
package com.redhat.qute.ls.template;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.ModelTextDocuments;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProjectRegistry;

public class QuteTextDocuments extends ModelTextDocuments<Template> {

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteProjectRegistry projectRegistry;

	public QuteTextDocuments(BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry) {
		super(parse);
		this.projectInfoProvider = projectInfoProvider;
		this.projectRegistry = projectRegistry;
	}

	@Override
	public QuteTextDocument createDocument(TextDocumentItem document) {
		QuteTextDocument doc = new QuteTextDocument(document, parse, projectInfoProvider, projectRegistry);
		doc.setIncremental(isIncremental());
		return doc;
	}

	@Override
	public ModelTextDocument<Template> onDidOpenTextDocument(DidOpenTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) super.onDidOpenTextDocument(params);
		projectRegistry.onDidOpenTextDocument(document);
		return document;
	}

	@Override
	public ModelTextDocument<Template> onDidCloseTextDocument(DidCloseTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) super.onDidCloseTextDocument(params);
		projectRegistry.onDidCloseTextDocument(document);
		return document;
	}

	public ModelTextDocument<Template> onDidSaveTextDocument(DidSaveTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) super.onDidSaveTextDocument(params);
		projectRegistry.onDidSaveTextDocument(document);
		return document;
	}
}
