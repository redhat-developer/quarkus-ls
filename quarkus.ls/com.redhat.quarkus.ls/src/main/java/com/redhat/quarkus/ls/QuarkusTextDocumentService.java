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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;
import com.redhat.quarkus.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.quarkus.settings.QuarkusCodeLensSettings;
import com.redhat.quarkus.settings.QuarkusFormattingSettings;
import com.redhat.quarkus.settings.QuarkusSymbolSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.SharedSettings;

/**
 * Quarkus text document service.
 *
 */
public class QuarkusTextDocumentService implements TextDocumentService {

	private final Map<String, TextDocumentService> textDocumentServicesMap;
	private final ApplicationPropertiesTextDocumentService applicationPropertiesTextDocumentService;
	private final JavaTextDocumentService javaTextDocumentService;
	private SharedSettings sharedSettings;

	public QuarkusTextDocumentService(QuarkusLanguageServer quarkusLanguageServer) {
		textDocumentServicesMap = new HashMap<>();
		this.sharedSettings = new SharedSettings();
		applicationPropertiesTextDocumentService = new ApplicationPropertiesTextDocumentService(quarkusLanguageServer,
				sharedSettings);
		javaTextDocumentService = new JavaTextDocumentService(quarkusLanguageServer, sharedSettings);
		textDocumentServicesMap.put("properties", applicationPropertiesTextDocumentService);
		textDocumentServicesMap.put("java", javaTextDocumentService);
	}

	/**
	 * Update shared settings from the client capabilities.
	 * 
	 * @param capabilities               the client capabilities
	 * @param extendedClientCapabilities the extended client capabilities
	 */
	public void updateClientCapabilities(ClientCapabilities capabilities,
			ExtendedClientCapabilities extendedClientCapabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			sharedSettings.getCompletionSettings().setCapabilities(textDocumentClientCapabilities.getCompletion());
			sharedSettings.getHoverSettings().setCapabilities(textDocumentClientCapabilities.getHover());
		}
		if (extendedClientCapabilities != null) {
			sharedSettings.getCommandCapabilities().setCapabilities(extendedClientCapabilities.getCommands());
		}
		applicationPropertiesTextDocumentService.updateClientCapabilities(capabilities);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			service.didOpen(params);
		}
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			service.didChange(params);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			service.didClose(params);
		}
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			service.didSave(params);
		}
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
		TextDocumentService service = getTextDocumentService(position.getTextDocument());
		if (service != null) {
			return service.completion(position);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		TextDocumentService service = getTextDocumentService(position.getTextDocument());
		if (service != null) {
			return service.hover(position);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.documentSymbol(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			TextDocumentPositionParams position) {
		TextDocumentService service = getTextDocumentService(position.getTextDocument());
		if (service != null) {
			return service.definition(position);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.formatting(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.rangeFormatting(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.codeAction(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.codeLens(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	public void quarkusPropertiesChanged(QuarkusPropertiesChangeEvent event) {
		applicationPropertiesTextDocumentService.quarkusPropertiesChanged(event);
	}

	public void updateSymbolSettings(QuarkusSymbolSettings newSettings) {
		applicationPropertiesTextDocumentService.updateSymbolSettings(newSettings);
	}

	public void updateValidationSettings(QuarkusValidationSettings newValidation) {
		applicationPropertiesTextDocumentService.updateValidationSettings(newValidation);
	}

	public void updateFormattingSettings(QuarkusFormattingSettings newFormatting) {
		applicationPropertiesTextDocumentService.updateFormattingSettings(newFormatting);
	}

	public void updateCodeLensSettings(QuarkusCodeLensSettings newCodeLens) {
		javaTextDocumentService.updateCodeLensSettings(newCodeLens);
	}

	private TextDocumentService getTextDocumentService(TextDocumentIdentifier document) {
		String fileExtension = getFileExtension(document);
		return fileExtension != null ? textDocumentServicesMap.get(fileExtension) : null;
	}

	private TextDocumentService getTextDocumentService(TextDocumentItem document) {
		String fileExtension = getFileExtension(document);
		return fileExtension != null ? textDocumentServicesMap.get(fileExtension) : null;
	}

	private static String getFileExtension(TextDocumentIdentifier document) {
		return getFileExtension(document.getUri());
	}

	private static String getFileExtension(TextDocumentItem document) {
		return getFileExtension(document.getUri());
	}

	private static String getFileExtension(String uri) {
		int index = uri != null ? uri.lastIndexOf('.') : -1;
		return index != -1 ? uri.substring(index + 1, uri.length()) : null;
	}

}