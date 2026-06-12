/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.lsp4j.mcp.tools.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.lsp4j.mcp.annotations.Inject;
import com.redhat.lsp4j.mcp.annotations.RequireDidOpen;
import com.redhat.lsp4j.mcp.annotations.Tool;
import com.redhat.lsp4j.mcp.annotations.ToolArg;
import com.redhat.lsp4j.mcp.cache.McpCache;
import com.redhat.lsp4j.mcp.tools.McpTool;
import com.redhat.lsp4j.mcp.tools.Position;
import com.redhat.lsp4j.mcp.tools.TextDocumentIdentifier;

/**
 * Generic LSP tool to get code actions at a position. Works with any
 * LSP4J-based language server.
 */
public class LspGetCodeActionsTool implements McpTool {

	/**
	 * Request object for code action tool.
	 */
	public static class CodeActionRequest {

		private TextDocumentIdentifier textDocument;
		private Position position;

		public CodeActionRequest() {
		}

		public CodeActionRequest(TextDocumentIdentifier textDocument, Position position) {
			this.textDocument = textDocument;
			this.position = position;
		}

		@ToolArg(description = "Text document identifier")
		public TextDocumentIdentifier getTextDocument() {
			return textDocument;
		}

		public void setTextDocument(TextDocumentIdentifier textDocument) {
			this.textDocument = textDocument;
		}

		@ToolArg(description = "Position in the file")
		public Position getPosition() {
			return position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}
	}

	@Inject
	private McpCache cache;

	@Inject
	private LanguageServer languageServer;

	@Tool(description = "Get code actions at a given position in a file")
	@RequireDidOpen(uriParam = "arg0.textDocument.uri")
	public List<Either<Command, CodeAction>> getCodeActions(CodeActionRequest request) throws Exception {
		Position position = request.getPosition();
		String uri = request.getTextDocument().getUri();

		// Get diagnostics at this position
		List<Diagnostic> allDiagnostics = cache.getDiagnostics(uri);
		List<Diagnostic> diagnosticsAtPosition = filterDiagnosticsAtPosition(allDiagnostics, position);

		// Build CodeActionParams
		CodeActionParams params = new CodeActionParams();
		params.setTextDocument(new org.eclipse.lsp4j.TextDocumentIdentifier(uri));
		org.eclipse.lsp4j.Position lspPosition = new org.eclipse.lsp4j.Position(position.getLine(), position.getCharacter());
		params.setRange(new Range(lspPosition, lspPosition));

		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(diagnosticsAtPosition);
		params.setContext(context);

		// Call textDocument/codeAction
		return languageServer.getTextDocumentService().codeAction(params).get(5, TimeUnit.SECONDS);
	}

	private List<Diagnostic> filterDiagnosticsAtPosition(List<Diagnostic> diagnostics, Position position) {
		if (diagnostics == null) {
			return new ArrayList<>();
		}

		return diagnostics.stream().filter(d -> isPositionInRange(position, d.getRange())).collect(Collectors.toList());
	}

	private boolean isPositionInRange(Position position, Range range) {
		if (range == null) {
			return false;
		}

		org.eclipse.lsp4j.Position start = range.getStart();
		org.eclipse.lsp4j.Position end = range.getEnd();

		// Check if position is after or at start
		if (position.getLine() < start.getLine()) {
			return false;
		}
		if (position.getLine() == start.getLine() && position.getCharacter() < start.getCharacter()) {
			return false;
		}

		// Check if position is before or at end
		if (position.getLine() > end.getLine()) {
			return false;
		}
		if (position.getLine() == end.getLine() && position.getCharacter() > end.getCharacter()) {
			return false;
		}

		return true;
	}
}
