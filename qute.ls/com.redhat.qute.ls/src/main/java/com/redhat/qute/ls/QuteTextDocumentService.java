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
package com.redhat.qute.ls;

import java.util.List;
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
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
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
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.qute.ls.java.JavaFileTextDocumentService;
import com.redhat.qute.ls.template.TemplateFileTextDocumentService;
import com.redhat.qute.services.codeactions.CodeActionUnresolvedData;
import com.redhat.qute.services.completions.CompletionItemUnresolvedData;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.JSONUtility;

/**
 * LSP text document service for Qute template file.
 *
 */
public class QuteTextDocumentService implements TextDocumentService {

	private final SharedSettings sharedSettings;

	private final JavaFileTextDocumentService javaFileTextDocumentService;

	private final TemplateFileTextDocumentService templateFileTextDocumentService;

	public QuteTextDocumentService(QuteLanguageServer quteLanguageServer) {
		this.sharedSettings = quteLanguageServer.getSharedSettings();
		this.javaFileTextDocumentService = new JavaFileTextDocumentService(quteLanguageServer, sharedSettings);
		this.templateFileTextDocumentService = new TemplateFileTextDocumentService(quteLanguageServer, sharedSettings);
	}

	/**
	 * Update shared settings from the client capabilities.
	 *
	 * @param capabilities the client capabilities
	 */
	public void updateClientCapabilities(ClientCapabilities capabilities,
			ExtendedClientCapabilities extendedClientCapabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			sharedSettings.getCompletionSettings().setCapabilities(textDocumentClientCapabilities.getCompletion());
			sharedSettings.getCodeActionSettings().setCapabilities(textDocumentClientCapabilities.getCodeAction());
		}
		templateFileTextDocumentService.updateClientCapabilities(capabilities, extendedClientCapabilities);
		javaFileTextDocumentService.updateClientCapabilities(capabilities);
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
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		CompletionItemUnresolvedData data = JSONUtility.toModel(unresolved.getData(),
				CompletionItemUnresolvedData.class);
		if (data != null) {
			AbstractTextDocumentService service = getTextDocumentService(data.getTextDocumentUri());
			if (service != null) {
				return service.resolveCompletionItem(unresolved, data);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.hover(params);
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
			DefinitionParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.definition(params);
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

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.documentHighlight(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.documentLink(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.references(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.rename(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<LinkedEditingRanges> linkedEditingRange(LinkedEditingRangeParams params) {
		TextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.linkedEditingRange(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		AbstractTextDocumentService service = getTextDocumentService(params.getTextDocument());
		if (service != null) {
			return service.inlayHint(params);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction codeAction) {
		/*
		 * {
		 * resolverKind: ...,
		 * textDocumentUri: ...,
		 * resolverData: {
		 * ...
		 * }
		 * }
		 */
		CodeActionUnresolvedData data = JSONUtility.toModel(codeAction.getData(), CodeActionUnresolvedData.class);
		AbstractTextDocumentService service = getTextDocumentService(data.getTextDocumentUri());
		if (service != null) {
			return service.resolveCodeAction(codeAction);
		}
		return CompletableFuture.completedFuture(null);
	}

	private AbstractTextDocumentService getTextDocumentService(TextDocumentIdentifier document) {
		return getTextDocumentService(document.getUri());
	}

	private TextDocumentService getTextDocumentService(TextDocumentItem document) {
		return getTextDocumentService(document.getUri());
	}

	private AbstractTextDocumentService getTextDocumentService(String uri) {
		String fileExtension = getFileExtension(uri);
		if ("java".equals(fileExtension) || "class".equals(fileExtension)) {
			return javaFileTextDocumentService;
		}
		return templateFileTextDocumentService;
	}

	private static String getFileExtension(String uri) {
		if (uri == null) {
			return null;
		}
		int index = uri.lastIndexOf('.');
		return index != -1 ? uri.substring(index + 1, uri.length()) : null;
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		templateFileTextDocumentService.dataModelChanged(event);
	}

	public void validationSettingsChanged() {
		templateFileTextDocumentService.validationSettingsChanged();
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		templateFileTextDocumentService.didChangeWatchedFiles(params);
	}
}