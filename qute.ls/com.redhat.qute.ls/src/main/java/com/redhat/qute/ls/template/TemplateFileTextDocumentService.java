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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.LinkedEditingRangeParams;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.ls.AbstractTextDocumentService;
import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.diagnostics.ResolvingJavaTypeContext;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * LSP text document service for Qute template file.
 *
 */
public class TemplateFileTextDocumentService extends AbstractTextDocumentService {

	private final QuteTextDocuments documents;

	public TemplateFileTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		super(quteLanguageServer, sharedSettings);
		this.documents = new QuteTextDocuments((document, cancelChecker) -> {
			return TemplateParser.parse(document, () -> cancelChecker.checkCanceled());
		}, quteLanguageServer, quteLanguageServer.getProjectRegistry());
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) documents.onDidOpenTextDocument(params);
		document.getProjectInfoFuture() //
				.thenAccept(projectInfo -> {
					if (projectInfo != null) {
						// At this step we get informations about the Java project (used to collect Java
						// classes available for the given Qute template)
						// We retrigger the validation to validate data model.
						triggerValidationFor(document);
					}
				});
		triggerValidationFor(document);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) documents.onDidChangeTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		quteLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return computeModelAsync2(getDocument(params.getTextDocument().getUri()).getModel(),
				(cancelChecker, template) -> {
					return getQuteLanguageService()
							.doComplete(template, params.getPosition(), sharedSettings.getCompletionSettings(),
									sharedSettings.getFormattingSettings(), cancelChecker) //
							.thenApply(list -> {
								return Either.forRight(list);
							});

				});

	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		if (!sharedSettings.getCodeLensSettings().isEnabled()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		return computeModelAsync2(getDocument(params.getTextDocument().getUri()).getModel(),
				(cancelChecker, template) -> {
					return getQuteLanguageService().getCodeLens(template, sharedSettings, cancelChecker);
				});
	}

	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return computeModelAsync2(getDocument(params.getTextDocument().getUri()).getModel(),
				(cancelChecker, template) -> {
					return getQuteLanguageService()
							.doCodeActions(template, params.getContext(), params.getRange(), sharedSettings) //
							.thenApply(codeActions -> {
								return codeActions.stream() //
										.map(ca -> {
											Either<Command, CodeAction> e = Either.forRight(ca);
											return e;
										}) //
										.collect(Collectors.toList());
							});
				});
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		return computeModelAsync2(getDocument(params.getTextDocument().getUri()).getModel(),
				(cancelChecker, template) -> {
					return getQuteLanguageService().doHover(template, params.getPosition(), sharedSettings,
							cancelChecker);
				});
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		return getTemplate(params.getTextDocument(), (cancelChecker, template) -> {
			return getQuteLanguageService().findDocumentHighlights(template, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			DefinitionParams params) {
		return computeModelAsync2(getDocument(params.getTextDocument().getUri()).getModel(),
				(cancelChecker, template) -> {
					return getQuteLanguageService() //
							.findDefinition(template, params.getPosition(), cancelChecker) //
							.thenApply(definitions -> {
								if (super.isDefinitionLinkSupport()) {
									return Either.forRight(definitions);
								}
								List<? extends Location> locations = definitions //
										.stream() //
										.map(locationLink -> QutePositionUtility.toLocation(locationLink)) //
										.collect(Collectors.toList());
								return Either.forLeft(locations);
							});

				});
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return getTemplate(params.getTextDocument(), (cancelChecker, template) -> {
			return getQuteLanguageService().findDocumentLinks(template);
		});
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return getTemplate(params.getTextDocument(), (cancelChecker, template) -> {
			if (super.isHierarchicalDocumentSymbolSupport()) {
				return getQuteLanguageService().findDocumentSymbols(template, cancelChecker) //
						.stream() //
						.map(s -> {
							Either<SymbolInformation, DocumentSymbol> e = Either.forRight(s);
							return e;
						}) //
						.collect(Collectors.toList());
			}
			return getQuteLanguageService().findSymbolInformations(template, cancelChecker) //
					.stream() //
					.map(s -> {
						Either<SymbolInformation, DocumentSymbol> e = Either.forLeft(s);
						return e;
					}) //
					.collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return getTemplate(params.getTextDocument(), (cancelChecker, template) -> {
			return getQuteLanguageService().findReferences(template, params.getPosition(), params.getContext(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<LinkedEditingRanges> linkedEditingRange(LinkedEditingRangeParams params) {
		return getTemplate(params.getTextDocument(), (cancelChecker, template) -> {
			return getQuteLanguageService().findLinkedEditingRanges(template, params.getPosition(), cancelChecker);
		});
	}

	private QuteLanguageService getQuteLanguageService() {
		return quteLanguageServer.getQuarkusLanguageService();
	}

	private void triggerValidationFor(QuteTextDocument document) {
		document.getModel() //
				.thenApply((template) -> {

					// The template is parsed, check if the document has changed since the
					// parsing
					template.checkCanceled();

					// Collect diagnostics
					ResolvingJavaTypeContext resolvingJavaTypeContext = new ResolvingJavaTypeContext(template);
					List<Diagnostic> diagnostics = getQuteLanguageService().doDiagnostics(template,
							getSharedSettings().getValidationSettings(template.getUri()),
							resolvingJavaTypeContext, () -> template.checkCanceled());

					// Diagnostics has been collected, before diagnostics publishing, check if the
					// document has changed since diagnostics collect.
					template.checkCanceled();

					// Publish diagnostics
					quteLanguageServer.getLanguageClient()
							.publishDiagnostics(new PublishDiagnosticsParams(template.getUri(), diagnostics));

					if (!resolvingJavaTypeContext.isEmpty()) {
						// Some Java types was not loaded, wait for that all Java types are resolved to
						// retrigger the validation.
						CompletableFuture<Void> allFutures = CompletableFuture.allOf(resolvingJavaTypeContext
								.toArray(new CompletableFuture[resolvingJavaTypeContext.size()]));
						allFutures.thenAccept(Void -> {
							triggerValidationFor(document);
						});
					}

					return null;
				});
	}

	/**
	 * Returns the text document from the given uri.
	 *
	 * @param uri the uri
	 * @return the text document from the given uri.
	 */
	public QuteTextDocument getDocument(String uri) {
		return (QuteTextDocument) documents.get(uri);
	}

	/**
	 * Returns the properties model for a given uri in a future and then apply the
	 * given function.
	 *
	 * @param <R>
	 * @param documentIdentifier the document identifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link Template} and returns the to be
	 *                           computed value
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getTemplate(TextDocumentIdentifier documentIdentifier,
			BiFunction<CancelChecker, Template, R> code) {
		return getTemplate(getDocument(documentIdentifier.getUri()), code);
	}

	/**
	 * Returns the properties model for a given uri in a future and then apply the
	 * given function.
	 *
	 * @param <R>
	 * @param documentIdentifier the document identifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link Template} and returns the to be
	 *                           computed value
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getTemplate(QuteTextDocument document,
			BiFunction<CancelChecker, Template, R> code) {
		return computeModelAsync(document.getModel(), code);
	}

	private static <R, M> CompletableFuture<R> computeModelAsync(CompletableFuture<M> loadModel,
			BiFunction<CancelChecker, M, R> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenCombineAsync(loadModel, code);
		CancelChecker cancelIndicator = () -> {
			if (result.isCancelled())
				throw new CancellationException();
		};
		start.complete(cancelIndicator);
		return result;
	}

	private static <R, M> CompletableFuture<R> computeModelAsync2(CompletableFuture<M> loadModel,
			BiFunction<CancelChecker, M, CompletableFuture<R>> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenCombineAsync(loadModel, code)//
				.thenCompose(a -> {
					return a;
				});
		CancelChecker cancelIndicator = () -> {
			if (result.isCancelled())
				throw new CancellationException();
		};
		start.complete(cancelIndicator);
		return result;
	}

	public void validationSettingsChanged() {
		// trigger validation for all opened Qute template files
		documents.all().stream().forEach(document -> {
			triggerValidationFor((QuteTextDocument) document);
		});
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		// trigger validation for all opened Qute template files
		documents.all().stream().forEach(document -> {
			triggerValidationFor((QuteTextDocument) document);
		});
	}

}