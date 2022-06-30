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
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteTemplateGenerateMissingJavaMember;
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

	private final QuteCodeActions codeActions;
	private final QuteCodeLens codeLens;
	private final QuteCompletions completions;
	private final QuteDefinition definition;
	private final QuteDiagnostics diagnostics;
	private final QuteDocumentLink documentLink;
	private final QuteHighlighting highlighting;
	private final QuteHover hover;
	private final QuteInlayHint inlayHint;
	private final QuteLinkedEditing linkedEditing;
	private final QuteReference reference;
	private final QuteRename rename;
	private final QuteSymbolsProvider symbolsProvider;

	private SnippetRegistry<Snippet> coreTagSnippetRegistry;

	public QuteLanguageService(JavaDataModelCache javaCache) {
		this.codeActions = new QuteCodeActions();
		this.codeLens = new QuteCodeLens(javaCache);
		this.completions = new QuteCompletions(javaCache, this);
		this.definition = new QuteDefinition(javaCache);
		this.diagnostics = new QuteDiagnostics(javaCache);
		this.documentLink = new QuteDocumentLink();
		this.highlighting = new QuteHighlighting();
		this.hover = new QuteHover(javaCache, this);
		this.inlayHint = new QuteInlayHint(javaCache);
		this.linkedEditing = new QuteLinkedEditing();
		this.reference = new QuteReference();
		this.rename = new QuteRename();
		this.symbolsProvider = new QuteSymbolsProvider();
	}

	/**
	 * Create CodeAction(s) in the given Qute <code>template</code>.
	 *
	 * @param template       the Qute template.
	 * @param context        the Code Action context.
	 * @param resolver       the generate missing java member resolver
	 * @param sharedSettings the Qute shared settings.
	 * @return the CodeAction(s) to be added to the Qute template.
	 */
	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context,
			QuteTemplateGenerateMissingJavaMember resolver, Range range, SharedSettings sharedSettings) {
		return codeActions.doCodeActions(template, context, range, resolver, sharedSettings);
	}

	/**
	 * Create CodeLens' in the given Qute <code>template</code>.
	 *
	 * @param template       the Qute template.
	 * @param sharedSettings the Qute shared settings.
	 * @return the CodeLens' to be added to the Qute template.
	 */
	public CompletableFuture<List<? extends CodeLens>> getCodeLens(Template template, SharedSettings settings,
			CancelChecker cancelChecker) {
		return codeLens.getCodelens(template, settings, cancelChecker);
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

	/**
	 * Resolve definition in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param postion       the position sent in the request.
	 * @param cancelChecker the cancel checker.
	 * @return link(s) to the location of the definition.
	 */
	public CompletableFuture<List<? extends LocationLink>> findDefinition(Template template, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(template, position, cancelChecker);
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

	/**
	 * Document links in the given Qute <code>template</code>.
	 *
	 * @param template the Qute template.
	 * @return link range(s) of the document(s).
	 */
	public List<DocumentLink> findDocumentLinks(Template template) {
		return documentLink.findDocumentLinks(template);
	}

	/**
	 * Document highlights in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param postion       the position sent in the request.
	 * @param cancelChecker the cancel checker.
	 * @return highlight range(s) of the document(s).
	 */
	public List<DocumentHighlight> findDocumentHighlights(Template template, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(template, position, cancelChecker);
	}

	/**
	 * Hover display(s) in the given Qute <code>template</code>.
	 *
	 * @param template       the Qute template.
	 * @param postion        the position sent in the request.
	 * @param sharedSettings the Qute shared settings.
	 * @param cancelChecker  the cancel checker.
	 * @return hover response for the hovered member.
	 */
	public CompletableFuture<Hover> doHover(Template template, Position position, SharedSettings sharedSettings,
			CancelChecker cancelChecker) {
		return hover.doHover(template, position, sharedSettings, cancelChecker);
	}

	/**
	 * Inlay hint display(s) in the given Qute <code>template</code>.
	 *
	 * @param template                 the Qute template.
	 * @param range                    the position sent in the request.
	 * @param inlayHintSettings        the Qute inlay hint settings.
	 * @param resolvingJavaTypeContext context for resolved java type in hint.
	 * @param cancelChecker            the cancel checker.
	 * @return hover response for the hovered member.
	 */
	public CompletableFuture<List<InlayHint>> getInlayHint(Template template, Range range,
			QuteInlayHintSettings inlayHintSettings, ResolvingJavaTypeContext resolvingJavaTypeContext,
			CancelChecker cancelChecker) {
		return inlayHint.getInlayHint(template, range, inlayHintSettings, resolvingJavaTypeContext, cancelChecker);
	}

	/**
	 * Linked editing range(s) in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param postion       the position sent in the request.
	 * @param cancelChecker the cancel checker.
	 * @return linked editing range(s) of the symbol.
	 */
	public LinkedEditingRanges findLinkedEditingRanges(Template template, Position position,
			CancelChecker cancelChecker) {
		return linkedEditing.findLinkedEditingRanges(template, position, cancelChecker);
	}

	/**
	 * Reference location(s) in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param postion       the position sent in the request.
	 * @param context       the reference context for symbol references.
	 * @param cancelChecker the cancel checker.
	 * @return linked editing range(s) of the symbol.
	 */
	public List<? extends Location> findReferences(Template template, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		return reference.findReferences(template, position, context, cancelChecker);
	}

	/**
	 * Rename instances in the given Qute <code>template</code>.
	 *
	 * @param template           the Qute template.
	 * @param validationSettings the validation settings.
	 * @param newText            the new text to replace the referenced instances.
	 * @param cancelChecker      the cancel checker.
	 * @return the rename edits to be made to the Qute template.
	 */
	public WorkspaceEdit doRename(Template template, Position position, String newText, CancelChecker cancelChecker) {
		return rename.doRename(template, position, newText, cancelChecker);
	}

	/**
	 * Document symbol(s) in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param cancelChecker the cancel checker.
	 * @return document symbol(s) in the template.
	 */
	public List<DocumentSymbol> findDocumentSymbols(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(template, cancelChecker);
	}

	/**
	 * Document symbol information in the given Qute <code>template</code>.
	 *
	 * @param template      the Qute template.
	 * @param cancelChecker the cancel checker.
	 * @return a list of document symbol information in the template.
	 */
	public List<SymbolInformation> findSymbolInformations(Template template, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(template, cancelChecker);
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
