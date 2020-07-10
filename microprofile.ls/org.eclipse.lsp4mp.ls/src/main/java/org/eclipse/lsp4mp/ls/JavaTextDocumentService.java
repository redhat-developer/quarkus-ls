/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeActionParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaCodeLensParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;
import org.eclipse.lsp4mp.commons.MicroProfileJavaHoverParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;
import org.eclipse.lsp4mp.ls.JavaTextDocuments.JavaTextDocument;
import org.eclipse.lsp4mp.ls.commons.BadLocationException;
import org.eclipse.lsp4mp.ls.commons.TextDocument;
import org.eclipse.lsp4mp.ls.commons.client.CommandKind;
import org.eclipse.lsp4mp.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.eclipse.lsp4mp.settings.MicroProfileCodeLensSettings;
import org.eclipse.lsp4mp.settings.SharedSettings;
import org.eclipse.lsp4mp.snippets.SnippetContextForJava;

/**
 * LSP text document service for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentService extends AbstractTextDocumentService {

	private static final Logger LOGGER = Logger.getLogger(JavaTextDocumentService.class.getName());

	private final MicroProfileLanguageServer microprofileLanguageServer;
	private final SharedSettings sharedSettings;

	private final JavaTextDocuments documents;

	private TextDocumentSnippetRegistry snippetRegistry;

	public JavaTextDocumentService(MicroProfileLanguageServer microprofileLanguageServer,
			SharedSettings sharedSettings) {
		this.microprofileLanguageServer = microprofileLanguageServer;
		this.sharedSettings = sharedSettings;
		this.documents = new JavaTextDocuments(microprofileLanguageServer);
	}

	// ------------------------------ did* for Java file -------------------------

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		triggerValidationFor(documents.onDidOpenTextDocument(params));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		triggerValidationFor(documents.onDidChangeTextDocument(params));
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		String uri = params.getTextDocument().getUri();
		microprofileLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		// validate all opened java files which belong to a MicroProfile project
		triggerValidationForAll(null);
	}

	// ------------------------------ Completion ------------------------------

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		JavaTextDocument document = documents.get(params.getTextDocument().getUri());
		return document.executeIfInMicroProfileProject((projectInfo) -> {
			return CompletableFutures.computeAsync(cancel -> {
				try {
					// Returns java snippets
					int completionOffset = document.offsetAt(params.getPosition());
					boolean canSupportMarkdown = true;
					CompletionList list = new CompletionList();
					list.setItems(new ArrayList<>());
					documents.getSnippetRegistry()
							.getCompletionItems(document, completionOffset, canSupportMarkdown, context -> {
								if (context != null && context instanceof SnippetContextForJava) {
									return ((SnippetContextForJava) context).isMatch(projectInfo);
								}
								return true;
							}).forEach(item -> {
								list.getItems().add(item);
							});
					return Either.forRight(list);
				} catch (BadLocationException e) {
					LOGGER.log(Level.SEVERE, "Error while getting java completions", e);
					return Either.forRight(null);
				}
			});
		}, Either.forLeft(Collections.emptyList()));
	}

	// ------------------------------ Code Lens ------------------------------

	public void updateCodeLensSettings(MicroProfileCodeLensSettings newCodeLens) {
		sharedSettings.getCodeLensSettings().setUrlCodeLensEnabled(newCodeLens.isUrlCodeLensEnabled());
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		boolean urlCodeLensEnabled = sharedSettings.getCodeLensSettings().isUrlCodeLensEnabled();
		if (!urlCodeLensEnabled) {
			// Don't consume JDT LS extension if all code lens are disabled.
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		JavaTextDocument document = documents.get(params.getTextDocument().getUri());
		return document.executeIfInMicroProfileProject((projectInfo) -> {
			MicroProfileJavaCodeLensParams javaParams = new MicroProfileJavaCodeLensParams(
					params.getTextDocument().getUri());
			if (sharedSettings.getCommandCapabilities().isCommandSupported(CommandKind.COMMAND_OPEN_URI)) {
				javaParams.setOpenURICommand(CommandKind.COMMAND_OPEN_URI);
			}
			javaParams.setCheckServerAvailable(true);
			javaParams.setUrlCodeLensEnabled(urlCodeLensEnabled);
			// javaParams.setLocalServerPort(8080); // TODO : manage this server port from
			// the settings
			return microprofileLanguageServer.getLanguageClient().getJavaCodelens(javaParams);
		}, Collections.emptyList());
	}

	// ------------------------------ Code Action ------------------------------

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		JavaTextDocument document = documents.get(params.getTextDocument().getUri());
		return document.executeIfInMicroProfileProject((projectInfo) -> {
			MicroProfileJavaCodeActionParams javaParams = new MicroProfileJavaCodeActionParams();
			javaParams.setTextDocument(params.getTextDocument());
			javaParams.setRange(params.getRange());
			javaParams.setContext(params.getContext());
			javaParams.setResourceOperationSupported(microprofileLanguageServer.getCapabilityManager()
					.getClientCapabilities().isResourceOperationSupported());
			return microprofileLanguageServer.getLanguageClient().getJavaCodeAction(javaParams) //
					.thenApply(codeActions -> {
						return codeActions.stream() //
								.map(ca -> {
									Either<Command, CodeAction> e = Either.forRight(ca);
									return e;
								}) //
								.collect(Collectors.toList());
					});
		}, Collections.emptyList());
	}

	// ------------------------------ Hover ------------------------------

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		JavaTextDocument document = documents.get(params.getTextDocument().getUri());
		return document.executeIfInMicroProfileProject((projectinfo) -> {
			boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
			DocumentFormat documentFormat = markdownSupported ? DocumentFormat.Markdown : DocumentFormat.PlainText;
			MicroProfileJavaHoverParams javaParams = new MicroProfileJavaHoverParams(params.getTextDocument().getUri(),
					params.getPosition(), documentFormat);
			return microprofileLanguageServer.getLanguageClient().getJavaHover(javaParams);
		}, null);
	}

	// ------------------------------ Diagnostics ------------------------------

	/**
	 * Validate the given opened Java file.
	 * 
	 * @param document the opened Java file.
	 */
	private void triggerValidationFor(JavaTextDocument document) {
		document.executeIfInMicroProfileProject((projectinfo) -> {
			String uri = document.getUri();
			triggerValidationFor(Arrays.asList(uri));
			return null;
		}, null);
	}

	/**
	 * Validate all opened Java files which belong to a MicroProfile project.
	 * 
	 * @param projectURIs list of project URIs filter and null otherwise.
	 */
	private void triggerValidationForAll(Set<String> projectURIs) {
		triggerValidationFor(documents.all().stream() //
				.filter(document -> projectURIs == null || projectURIs.contains(document.getProjectURI())) //
				.filter(JavaTextDocument::isInMicroProfileProject) //
				.map(TextDocument::getUri) //
				.collect(Collectors.toList()));
	}

	/**
	 * Validate all given Java files uris.
	 * 
	 * @param uris Java files uris to validate.
	 */
	private void triggerValidationFor(List<String> uris) {
		if (uris.isEmpty()) {
			return;
		}
		MicroProfileJavaDiagnosticsParams javaParams = new MicroProfileJavaDiagnosticsParams(uris);
		boolean markdownSupported = sharedSettings.getHoverSettings().isContentFormatSupported(MarkupKind.MARKDOWN);
		if (markdownSupported) {
			javaParams.setDocumentFormat(DocumentFormat.Markdown);
		}
		microprofileLanguageServer.getLanguageClient().getJavaDiagnostics(javaParams) //
				.thenApply(diagnostics -> {
					if (diagnostics == null) {
						return null;
					}
					for (PublishDiagnosticsParams diagnostic : diagnostics) {
						microprofileLanguageServer.getLanguageClient().publishDiagnostics(diagnostic);
					}
					return null;
				});
	}

	public void propertiesChanged(MicroProfilePropertiesChangeEvent event) {
		if (documents.propertiesChanged(event)) {
			triggerValidationForAll(event.getProjectURIs());
		}
	}

}
