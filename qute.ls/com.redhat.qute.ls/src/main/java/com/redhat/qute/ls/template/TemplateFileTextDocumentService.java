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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;
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

import com.redhat.qute.commons.QuteTelemetryConstants;
import com.redhat.qute.commons.TelemetryEvent;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.ls.AbstractTextDocumentService;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteTemplateProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.ValidatorDelayer;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.documents.QuteOpenedTextDocument;
import com.redhat.qute.project.documents.QuteOpenedTextDocuments;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * LSP text document service for Qute template file.
 *
 */
public class TemplateFileTextDocumentService extends AbstractTextDocumentService
		implements QuteTemplateProvider, TemplateValidator {

	private final QuteLanguageService quteLanguageService;

	private final QuteOpenedTextDocuments openedDocuments;
	private final QuteProjectRegistry projectRegistry;
	private ValidatorDelayer<ModelTextDocument<Template>> validatorDelayer;
	private boolean hasOpenedAQuteDocument;

	public TemplateFileTextDocumentService(QuteLanguageService quteLanguageService,
			QuteProjectInfoProvider projectInfoProvider, Supplier<QuteLanguageClientAPI> languageClientProvider,
			SharedSettings sharedSettings) {
		super(languageClientProvider, sharedSettings);
		this.quteLanguageService = quteLanguageService;
		this.projectRegistry = quteLanguageService.getProjectRegistry();
		this.openedDocuments = new QuteOpenedTextDocuments((document, cancelChecker) -> {
			return TemplateParser.parse(document, () -> cancelChecker.checkCanceled());
		}, projectInfoProvider, projectRegistry);
		this.validatorDelayer = new ValidatorDelayer<ModelTextDocument<Template>>((template) -> {
			triggerValidationFor((QuteTextDocument) template);
		});

	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		QuteOpenedTextDocument document = (QuteOpenedTextDocument) openedDocuments.onDidOpenTextDocument(params);
		if (!hasOpenedAQuteDocument) {
			hasOpenedAQuteDocument = true;
			QuteLanguageClientAPI languageClient = getLanguageClient();
			if (languageClient != null) {
				languageClient.telemetryEvent(new TelemetryEvent(QuteTelemetryConstants.FILE_OPENED, new HashMap<>()));
			}
		}
		// The qute template is opened, trigger the validation
		QuteProject project = document.getProject();
		if (project != null) {
			// The project has been already loaded, trigger the validation
			triggerValidationFor(document);
		} else {
			document.getProjectInfoFuture() //
					.thenAccept(projectInfo -> {
						if (projectInfo != null) {
							// At this step we get informations about the Java project (used to collect Java
							// classes available for the given Qute template)
							// We retrigger the validation to validate data model.
							triggerValidationFor(document);
						}
					});
		}
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		QuteOpenedTextDocument document = (QuteOpenedTextDocument) openedDocuments.onDidChangeTextDocument(params);
		// The qute template has changed, trigger the validation
		if (projectRegistry.isAsyncValidation()) {
			validatorDelayer.validateWithDelay(document);
		} else {
			triggerValidationFor(document);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		openedDocuments.onDidCloseTextDocument(params);
		// Since closed document is managed, we don't publish empty diagnostics, because
		// closed document can report errors.
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		openedDocuments.onDidSaveTextDocument(params);
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService()
					.doComplete(template, params.getPosition(), sharedSettings.getCompletionSettings(),
							sharedSettings.getFormattingSettings(), sharedSettings.getNativeSettings(), cancelChecker) //
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
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().getCodeLens(template, sharedSettings, cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		QuteLanguageClientAPI languageClient = getLanguageClient();
		if (languageClient == null || validatorDelayer.isRevalidating(params.getTextDocument().getUri())) {
			return CompletableFuture.completedFuture((List<Either<Command, CodeAction>>) Collections.EMPTY_LIST);
		}
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			// Cancel checker is not passed to doCodeActions, since code actions don't yet
			// need to interact with JDT/editor
			return getQuteLanguageService()
					.doCodeActions(template, params.getContext(), languageClient, params.getRange(), sharedSettings) //
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
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().doHover(template, params.getPosition(), sharedSettings, cancelChecker);
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
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
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
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			return getQuteLanguageService().findDocumentLinks(template, cancelChecker);
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
		return getTemplateCompose(params.getTextDocument(), (template, cancelChecker) -> {
			// Collect inlay hints
			ResolvingJavaTypeContext resolvingJavaTypeContext = new ResolvingJavaTypeContext(template);
			CompletableFuture<List<InlayHint>> hints = getQuteLanguageService().getInlayHint(template,
					params.getRange(), sharedSettings, resolvingJavaTypeContext, cancelChecker);
			if (!resolvingJavaTypeContext.isEmpty()) {
				// Some Java types was not loaded, wait for that all Java types are resolved to
				// retrigger the inlay hints.
				CompletableFuture<Void> allFutures = CompletableFuture.allOf(
						resolvingJavaTypeContext.toArray(new CompletableFuture[resolvingJavaTypeContext.size()]));
				return allFutures.thenCompose(Void -> {
					cancelChecker.checkCanceled();
					// All Java type are resolved, recompute the inlay hints.
					return inlayHint(params);
				});
			}

			return hints;
		});
	}

	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction codeAction) {
		QuteLanguageClientAPI languageClient = getLanguageClient();
		if (languageClient == null) {
			return CompletableFuture.completedFuture(codeAction);
		}
		return getQuteLanguageService().resolveCodeAction(codeAction, languageClient);
	}

	private QuteLanguageService getQuteLanguageService() {
		return quteLanguageService;
	}

	/**
	 * Returns the text document from the given uri.
	 *
	 * @param uri the uri
	 * @return the text document from the given uri.
	 */
	public QuteOpenedTextDocument getDocument(String uri) {
		return (QuteOpenedTextDocument) openedDocuments.get(uri);
	}

	/**
	 * Parses the given Qute template file then passes the model to the given
	 * function, then returns the result of the given function.
	 *
	 * @param <R>                The type of the result computed by the bifunction
	 * @param documentIdentifier the document identifier.
	 * @param code               a bifunction that accepts the parsed
	 *                           {@link Template} and a {@link CancelChecker} and
	 *                           returns the value to be computed
	 * @see {@link TemplateFileTextDocumentService#getTemplateCompose}
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getTemplate(TextDocumentIdentifier documentIdentifier,
			BiFunction<Template, CancelChecker, R> code) {
		return openedDocuments.computeModelAsync(documentIdentifier, code);
	}

	/**
	 * Parses the given Qute template file then passes the model to the given
	 * function, then returns the result of the given function.
	 *
	 * Version of {@link TemplateFileTextDocumentService#getTemplate} that returns a
	 * future of the value instead of the value itself
	 *
	 * @param <R>                The type of the result computed by the bifunction
	 * @param documentIdentifier the document identifier
	 * @param code               a bifunction that accepts the parsed
	 *                           {@link Template} and a {@link CancelChecker} and
	 *                           returns the value to be computed as a future
	 * @see {@link TemplateFileTextDocumentService#getTemplate}
	 * @return the result of the passed bifunction as a future
	 */
	public <R> CompletableFuture<R> getTemplateCompose(TextDocumentIdentifier documentIdentifier,
			BiFunction<Template, CancelChecker, CompletableFuture<R>> code) {
		return openedDocuments.computeModelAsyncCompose(documentIdentifier, code);
	}

	public void validationSettingsChanged() {
		validateAllTemplates();
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		validateAllTemplates();
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		projectRegistry.didChangeWatchedFiles(params);
	}

	// ----------------------- validation

	@Override
	public void triggerValidationFor(QuteTextDocument document) {
		var template = document.getTemplate();

		// Collect diagnostics
		ResolvingJavaTypeContext resolvingJavaTypeContext = new ResolvingJavaTypeContext(template);
		List<Diagnostic> diagnostics = getQuteLanguageService().doDiagnostics(template,
				getSharedSettings().getValidationSettings(template.getUri()), getSharedSettings().getNativeSettings(),
				resolvingJavaTypeContext, () -> template.checkCanceled());

		// Diagnostics has been collected, before diagnostics publishing, check if the
		// document has changed since diagnostics collect.
		template.checkCanceled();

		// Publish diagnostics for the given template
		QuteLanguageClientAPI languageClient = getLanguageClient();
		if (languageClient != null) {
			languageClient //
					.publishDiagnostics(new PublishDiagnosticsParams(template.getUri(), diagnostics));
		}

		if (!resolvingJavaTypeContext.isEmpty()) {
			// Some Java types was not loaded, wait for that all Java types are resolved to
			// retrigger the validation.
			CompletableFuture<Void> allFutures = CompletableFuture
					.allOf(resolvingJavaTypeContext.toArray(new CompletableFuture[resolvingJavaTypeContext.size()]));
			allFutures.thenAccept(Void -> {
				triggerValidationFor(document);
			});
		}
	}

	@Override
	public void clearDiagnosticsFor(String fileUri) {
		QuteLanguageClientAPI languageClient = getLanguageClient();
		if (languageClient != null) {
			languageClient //
					.publishDiagnostics(new PublishDiagnosticsParams(fileUri, Collections.emptyList()));
		}
	}

	private void validateAllTemplates() {
		Collection<QuteProject> projects = getProjectRegistry().getProjects();
		if (projects.isEmpty()) {
			// trigger validation for all opened Qute template files
			openedDocuments.all().stream().forEach(document -> {
				triggerValidationFor((QuteOpenedTextDocument) document);
			});
		} else {
			triggerValidationFor(projects);
		}
	}

	@Override
	public void triggerValidationFor(Collection<QuteProject> projects) {
		for (QuteProject project : projects) {
			openedDocuments.all().stream().forEach(document -> {
				QuteProject documentProject = document.getModel().getProject();
				if (project.equals(documentProject)) {
					triggerValidationFor((QuteOpenedTextDocument) document);
				}
			});
			project.validateClosedTemplates(null);
		}
	}

	private QuteProjectRegistry getProjectRegistry() {
		return projectRegistry;
	}

	@Override
	public FileType getFileType() {
		return FileType.TEMPLATE;
	}

	@Override
	public Template getTemplate(String uri) {
		QuteOpenedTextDocument document = getDocument(uri);
		return document != null ? document.getModel() : null;
	}

	public void dispose() {
		validatorDelayer.dispose();
	}

}
