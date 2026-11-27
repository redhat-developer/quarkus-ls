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

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

import com.redhat.lsp4j.mcp.annotations.Inject;
import com.redhat.lsp4j.mcp.annotations.RequireDidOpen;
import com.redhat.lsp4j.mcp.annotations.Tool;
import com.redhat.lsp4j.mcp.annotations.ToolArg;
import com.redhat.lsp4j.mcp.cache.McpCache;
import com.redhat.lsp4j.mcp.tools.McpTool;
import com.redhat.lsp4j.mcp.tools.TextDocumentIdentifier;

/**
 * Generic LSP tool to get diagnostics for a file.
 * Works with any LSP4J-based language server.
 */
public class LspGetDiagnosticsTool implements McpTool {

    @Inject
    private McpCache cache;

    @Tool(description = "Get diagnostics for a file")
    @RequireDidOpen(uriParam = "textDocument.uri")
    public List<Diagnostic> getDiagnostics(
            @ToolArg(name = "textDocument", description = "Text document identifier") TextDocumentIdentifier textDocument) {
        return cache.getDiagnostics(textDocument.getUri());
    }
}
