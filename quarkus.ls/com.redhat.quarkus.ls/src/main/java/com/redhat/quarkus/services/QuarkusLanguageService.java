/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.QuarkusHoverSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;

/**
 * The Quarkus language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusLanguageService {

	private final QuarkusCompletions completions;
	private final QuarkusHover hover;
	private final QuarkusDiagnostics diagnostics;
	private final QuarkusCodeActions codeActions;

	public QuarkusLanguageService() {
		this.completions = new QuarkusCompletions();
		this.hover = new QuarkusHover();
		this.diagnostics = new QuarkusDiagnostics();
		this.codeActions = new QuarkusCodeActions();
	}

	public CompletionList doComplete(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusCompletionSettings completionSettings, CancelChecker cancelChecker) {
		return completions.doComplete(document, position, projectInfo, completionSettings, cancelChecker);
	}

	/**
	 * Returns Hover object for the currently hovered token
	 * 
	 * @param document      the document
	 * @param position      the hover position
	 * @param projectInfo   the Quarkus project information
	 * @param hoverSettings the hover settings
	 * @return Hover object for the currently hovered token
	 */
	public Hover doHover(TextDocument document, Position position, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings) {
		return hover.doHover(document, position, projectInfo, hoverSettings);
	}

	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, TextDocument document) {
		return codeActions.doCodeActions(context, range, document);
	}

	public List<Diagnostic> doDiagnostics(TextDocument document, QuarkusValidationSettings validationSettings) {
		return diagnostics.doDiagnostics(document, validationSettings);
	}

	public CompletableFuture<Path> publishDiagnostics(TextDocument document, 
			Consumer<PublishDiagnosticsParams> publishDiagnostics,
			QuarkusValidationSettings validationSettings) {
		
		String uri = document.getUri();
		List<Diagnostic> diagnostics = this.doDiagnostics(document, validationSettings);
		publishDiagnostics.accept(new PublishDiagnosticsParams(uri, diagnostics));
		return null;
	}
}
