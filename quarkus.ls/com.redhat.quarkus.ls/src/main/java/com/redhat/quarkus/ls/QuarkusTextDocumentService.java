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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.ls.commons.ModelTextDocument;
import com.redhat.quarkus.ls.commons.ModelTextDocuments;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.services.QuarkusLanguageService;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.SharedSettings;

/**
 * Quarkus text document service.
 *
 */
public class QuarkusTextDocumentService implements TextDocumentService {

	private final ModelTextDocuments<PropertiesModel> documents;

	private final QuarkusProjectInfoCache projectInfoCache;

	private final QuarkusLanguageServer quarkusLanguageServer;

	private final SharedSettings sharedSettings;

	public QuarkusTextDocumentService(QuarkusLanguageServer quarkusLanguageServer) {
		this.quarkusLanguageServer = quarkusLanguageServer;
		this.documents = new ModelTextDocuments<PropertiesModel>((document, cancelChecker) -> {
			return PropertiesModel.parse(document);
		});
		this.projectInfoCache = new QuarkusProjectInfoCache(quarkusLanguageServer);
		this.sharedSettings = new SharedSettings();
	}

	/**
	 * Update shared settings from the client capabilities.
	 * 
	 * @param capabilities the client capabilities
	 */
	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			sharedSettings.getCompletionSettings().setCapabilities(textDocumentClientCapabilities.getCompletion());
			sharedSettings.getHoverSettings().setCapabilities(textDocumentClientCapabilities.getHover());
		}
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocument document = documents.onDidOpenTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		TextDocument document = documents.onDidChangeTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		quarkusLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		// Get Quarkus project information which stores all available Quarkus
		// properties
		QuarkusProjectInfoParams projectInfoParams = createProjectInfoParams(params.getTextDocument(), null);
		return projectInfoCache.getQuarkusProjectInfo(projectInfoParams).thenComposeAsync(projectInfo -> {
			if (!projectInfo.isQuarkusProject()) {
				return CompletableFuture.completedFuture(Either.forRight(new CompletionList()));
			}
			// then get the Properties model document
			return getPropertiesModel(params.getTextDocument(), (cancelChecker, document) -> {
				// then return completion by using the Quarkus project information and the
				// Properties model document
				CompletionList list = getQuarkusLanguageService().doComplete(document, params.getPosition(),
						projectInfo, sharedSettings.getCompletionSettings(), null);
				return Either.forRight(list);
			});
		});
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams params) {
		// Get Quarkus project information which stores all available Quarkus
		// properties
		QuarkusProjectInfoParams projectInfoParams = createProjectInfoParams(params.getTextDocument(), null);
		return projectInfoCache.getQuarkusProjectInfo(projectInfoParams).thenComposeAsync(projectInfo -> {
			if (!projectInfo.isQuarkusProject()) {
				return null;
			}
			// then get the Properties model document
			return getPropertiesModel(params.getTextDocument(), (cancelChecker, document) -> {
				// then return hover by using the Quarkus project information and the
				// Properties model document
				return getQuarkusLanguageService().doHover(document, params.getPosition(), projectInfo,
						sharedSettings.getHoverSettings());
			});
		});
	}

	private static QuarkusProjectInfoParams createProjectInfoParams(TextDocumentIdentifier id,
			List<String> documentationFormat) {
		return new QuarkusProjectInfoParams(id.getUri(), documentationFormat);
	}

	private QuarkusLanguageService getQuarkusLanguageService() {
		return quarkusLanguageServer.getQuarkusLanguageService();
	}

	private void triggerValidationFor(TextDocument document) {
		// TODO: implement validation for application.properties
	}

	/**
	 * Returns the text document from the given uri.
	 * 
	 * @param uri the uri
	 * @return the text document from the given uri.
	 */
	public ModelTextDocument<PropertiesModel> getDocument(String uri) {
		return documents.get(uri);
	}

	/**
	 * Returns the properties model for a given uri in a future and then apply the
	 * given function.
	 * 
	 * @param <R>
	 * @param documentIdentifier the document indetifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link PropertiesModel} and returns the
	 *                           to be computed value
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getPropertiesModel(TextDocumentIdentifier documentIdentifier,
			BiFunction<CancelChecker, PropertiesModel, R> code) {
		return computeModelAsync(getDocument(documentIdentifier.getUri()).getModel(), code);
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
}