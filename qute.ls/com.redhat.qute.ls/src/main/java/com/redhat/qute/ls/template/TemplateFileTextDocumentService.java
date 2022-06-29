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
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
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
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.LinkedEditingRangeParams;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.ls.AbstractTextDocumentService;
import com.redhat.qute.ls.QuteLanguageServer;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.ValidatorDelayer;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * LSP text document service for Qute template file.
 *
 */
public class TemplateFileTextDocumentService extends AbstractTextDocumentService {

	private final QuteTextDocuments documents;
	private ValidatorDelayer<ModelTextDocument<Template>> validatorDelayer;

	public TemplateFileTextDocumentService(QuteLanguageServer quteLanguageServer, SharedSettings sharedSettings) {
		super(quteLanguageServer, sharedSettings);
		this.documents = new QuteTextDocuments((document, cancelChecker) -> {
			return TemplateParser.parse(document, () -> cancelChecker.checkCanceled());
		}, quteLanguageServer, quteLanguageServer.getProjectRegistry());
		this.validatorDelayer = new ValidatorDelayer<ModelTextDocument<Template>>((template) -> {
			validate((QuteTextDocument) template);
		});
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
						triggerValidationFor(document, false);
					}
				});
		triggerValidationFor(document, false);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		QuteTextDocument document = (QuteTextDocument) documents.onDidChangeTextDocument(params);
		triggerValidationFor(document, true);
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
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					return getQuteLanguageService()
							.doComplete(template, params.getPosition(), sharedSettings.getCompletionSettings(),
									sharedSettings.getFormattingSettings(), sharedSettings.getNativeSettings(),
									cancelChecker) //
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
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					return getQuteLanguageService().getCodeLens(template, sharedSettings, cancelChecker);
				});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					// Cancel checker is not passed to doCodeActions, since code actions don't yet
					// need to interact with JDT/editor
					return getQuteLanguageService()
							.doCodeActions(template, params.getContext(), params.getRange(), sharedSettings) //
							.thenApply(codeActions -> {
								cancelChecker.checkCanceled();
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
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					return getQuteLanguageService().doHover(template, params.getPosition(), sharedSettings,
							cancelChecker);
				});
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().findDocumentHighlights(template, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			DefinitionParams params) {
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					return getQuteLanguageService() //
							.findDefinition(template, params.getPosition(), cancelChecker) //
							.thenApply(definitions -> {
								cancelChecker.checkCanceled();
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
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().findDocumentLinks(template);
		});
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
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
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().findReferences(template, params.getPosition(), params.getContext(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().doRename(template, params.getPosition(), params.getNewName(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<LinkedEditingRanges> linkedEditingRange(LinkedEditingRangeParams params) {
		return getTemplate(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().findLinkedEditingRanges(template, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		if (!sharedSettings.getInlayHintSettings().isEnabled()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		return getTemplateCompose(params.getTextDocument(),
				(template, cancelChecker) -> {
					// Collect inlay hints
					ResolvingJavaTypeContext resolvingJavaTypeContext = new ResolvingJavaTypeContext(template,
							quteLanguageServer.getDataModelCache());
					CompletableFuture<List<InlayHint>> hints = getQuteLanguageService().getInlayHint(template,
							params.getRange(), sharedSettings.getInlayHintSettings(), resolvingJavaTypeContext,
							cancelChecker);
					if (!resolvingJavaTypeContext.isEmpty()) {
						// Some Java types was not loaded, wait for that all Java types are resolved to
						// retrigger the inlay hints.
						CompletableFuture<Void> allFutures = CompletableFuture.allOf(resolvingJavaTypeContext
								.toArray(new CompletableFuture[resolvingJavaTypeContext.size()]));
						return allFutures.thenCompose(Void -> {
							cancelChecker.checkCanceled();
							// All Java type are resolved, recompute the inlay hints.
							return inlayHint(params);
						});
					}

					return hints;
				});
	}

	private QuteLanguageService getQuteLanguageService() {
		return quteLanguageServer.getQuarkusLanguageService();
	}

	private void triggerValidationFor(QuteTextDocument document, boolean delayed) {
		if (delayed) {
			validatorDelayer.validateWithDelay(document);
		} else {
			CompletableFuture.runAsync(() -> {
				validate(document);
			});
		}
	}

	private void validate(QuteTextDocument document) {
		var template = document.getModel();

		// Collect diagnostics
		ResolvingJavaTypeContext resolvingJavaTypeContext = new ResolvingJavaTypeContext(template,
				quteLanguageServer.getDataModelCache());
		List<Diagnostic> diagnostics = getQuteLanguageService().doDiagnostics(template,
				getSharedSettings().getValidationSettings(template.getUri()),
				getSharedSettings().getNativeSettings(), resolvingJavaTypeContext,
				() -> template.checkCanceled());

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
				triggerValidationFor(document, false);
			});
		}
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
	 * Parses the given Qute template file then passes the model to
	 * the given function, then returns the result of the given function.
	 *
	 * @param <R>                The type of the result computed by the bifunction
	 * @param documentIdentifier the document identifier.
	 * @param code               a bifunction that accepts the parsed
	 *                           {@link Template} and
	 *                           a {@link CancelChecker} and returns the value to be
	 *                           computed
	 * @see {@link TemplateFileTextDocumentService#getTemplateCompose}
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getTemplate(TextDocumentIdentifier documentIdentifier,
			BiFunction<Template, CancelChecker, R> code) {
		return documents.computeModelAsync(documentIdentifier, code);
	}

	/**
	 * Parses the given Qute template file then passes the model to
	 * the given function, then returns the result of the given function.
	 *
	 * Version of {@link TemplateFileTextDocumentService#getTemplate} that returns a
	 * future of the value instead of the value itself
	 *
	 * @param <R>                The type of the result computed by the bifunction
	 * @param documentIdentifier the document identifier
	 * @param code               a bifunction that accepts the parsed
	 *                           {@link Template} and
	 *                           a {@link CancelChecker} and returns the value to be
	 *                           computed as a future
	 * @see {@link TemplateFileTextDocumentService#getTemplate}
	 * @return the result of the passed bifunction as a future
	 */
	public <R> CompletableFuture<R> getTemplateCompose(TextDocumentIdentifier documentIdentifier,
			BiFunction<Template, CancelChecker, CompletableFuture<R>> code) {
		return documents.computeModelAsyncCompose(documentIdentifier, code);
	}

	public void validationSettingsChanged() {
		// trigger validation for all opened Qute template files
		documents.all().stream().forEach(document -> {
			triggerValidationFor((QuteTextDocument) document, false);
		});
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		// trigger validation for all opened Qute template files
		documents.all().stream().forEach(document -> {
			triggerValidationFor((QuteTextDocument) document, false);
		});
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		// trigger validation for all opened Qute template files
		documents.all().stream().forEach(document -> {
			triggerValidationFor((QuteTextDocument) document, true);
		});
	}

}