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
package com.redhat.qute.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.ls.commons.snippets.SnippetRegistryProvider;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteInlayHintSettings;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.settings.SharedSettings;

/**
 * The Qute language service.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteLanguageService implements SnippetRegistryProvider<Snippet> {

	private final QuteCodeLens codelens;
	private final QuteCodeActions codeActions;
	private final QuteCompletions completions;
	private final QuteHover hover;
	private final QuteHighlighting highlighting;
	private final QuteDefinition definition;
	private final QuteDocumentLink documentLink;
	private final QuteSymbolsProvider symbolsProvider;
	private final QuteDiagnostics diagnostics;
	private final QuteLinkedEditing linkedEditing;
	private final QuteReference reference;
	private final QuteInlayHint inlayHint;

	private SnippetRegistry<Snippet> coreTagSnippetRegistry;

	public QuteLanguageService(JavaDataModelCache javaCache) {
		this.completions = new QuteCompletions(javaCache, this);
		this.codelens = new QuteCodeLens(javaCache);
		this.codeActions = new QuteCodeActions();
		this.hover = new QuteHover(javaCache, this);
		this.highlighting = new QuteHighlighting();
		this.definition = new QuteDefinition(javaCache);
		this.documentLink = new QuteDocumentLink();
		this.symbolsProvider = new QuteSymbolsProvider();
		this.diagnostics = new QuteDiagnostics(javaCache);
		this.reference = new QuteReference();
		this.linkedEditing = new QuteLinkedEditing();
		this.inlayHint = new QuteInlayHint(javaCache);
	}

	/**
	 * Returns completion list for the given position
	 * 
	 * @param template             the Qute template
	 * @param position             the position where completion was triggered
	 * @param completionSettings   the completion settings.
	 * @param formattingSettings   the formatting settings.
	 * @param nativeImagesSettings the native image settings.
	 * @param cancelChecker        the cancel checker
	 * @return completion list for the given position
	 */
	public CompletableFuture<CompletionList> doComplete(Template template, Position position,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			QuteNativeSettings nativeImagesSettings, CancelChecker cancelChecker) {
		return completions.doComplete(template, position, completionSettings, formattingSettings, nativeImagesSettings,
				cancelChecker);
	}

	public CompletableFuture<List<? extends CodeLens>> getCodeLens(Template template, SharedSettings settings,
			CancelChecker cancelChecker) {
		return codelens.getCodelens(template, settings, cancelChecker);
	}

	public CompletableFuture<Hover> doHover(Template template, Position position, SharedSettings sharedSettings,
			CancelChecker cancelChecker) {
		return hover.doHover(template, position, sharedSettings, cancelChecker);
	}

	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context, Range range,
			SharedSettings sharedSettings) {
		return codeActions.doCodeActions(template, context, range, sharedSettings);
	}

	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(template, position, cancelChecker);
	}

	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(template, position, cancelChecker);
	}

	public List<DocumentLink> findDocumentLinks(Template template) {
		return documentLink.findDocumentLinks(template);
	}

	public List<DocumentSymbol> findDocumentSymbols(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(template, cancelChecker);
	}

	public List<SymbolInformation> findSymbolInformations(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(template, cancelChecker);
	}

	/**
	 * Validate the given Qute <code>template</code>.
	 * 
	 * @param template             the Qute template.
	 * @param validationSettings   the validation settings.
	 * @param nativeImagesSettings the native image settings.
	 * @param cancelChecker        the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(Template template, QuteValidationSettings validationSettings,
			QuteNativeSettings nativeImagesSettings, ResolvingJavaTypeContext resolvingJavaTypeFutures,
			CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(template, validationSettings, nativeImagesSettings, resolvingJavaTypeFutures,
				cancelChecker);
	}

	public List<? extends Location> findReferences(Template template, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		return reference.findReferences(template, position, context, cancelChecker);
	}

	public LinkedEditingRanges findLinkedEditingRanges(Template template, Position position,
			CancelChecker cancelChecker) {
		return linkedEditing.findLinkedEditingRanges(template, position, cancelChecker);
	}

	public CompletableFuture<List<InlayHint>> getInlayHint(Template template, Range range,
			QuteInlayHintSettings inlayHintSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			CancelChecker cancelChecker) {
		return inlayHint.getInlayHint(template, range, inlayHintSettings, resolvingJavaTypeContext, cancelChecker);
	}

	/**
	 * Returns the core tag (ex : #for, #if, etc) snippet registry.
	 * 
	 * @return the core tag (ex : #for, #if, etc) snippet registry.
	 */
	@Override
	public SnippetRegistry<Snippet> getSnippetRegistry() {
		if (coreTagSnippetRegistry == null) {
			loadCoreTagSnippetRegistry();
		}
		return coreTagSnippetRegistry;
	}

	private synchronized void loadCoreTagSnippetRegistry() {
		if (coreTagSnippetRegistry != null) {
			return;
		}
		coreTagSnippetRegistry = new SnippetRegistry<Snippet>();
	}

}
