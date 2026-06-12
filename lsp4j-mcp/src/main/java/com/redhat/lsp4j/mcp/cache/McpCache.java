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
package com.redhat.lsp4j.mcp.cache;

import org.eclipse.lsp4j.Diagnostic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for MCP server that tracks:
 * - Which files are currently opened in the IDE (via didOpen/didClose)
 * - Diagnostics for all files (via publishDiagnostics)
 *
 * This cache allows MCP tools to:
 * 1. Know if a file is already opened (to avoid unnecessary didOpen/didClose)
 * 2. Retrieve diagnostics without re-parsing
 */
public class McpCache {

    /**
     * Set of URIs for files currently opened in the IDE.
     * Updated by didOpen/didClose interception.
     */
    private final Set<String> openedFiles = Collections.synchronizedSet(new HashSet<>());

    /**
     * Map of URI -> Diagnostics.
     * Updated by publishDiagnostics interception.
     * Thread-safe because publishDiagnostics can be called asynchronously.
     */
    private final Map<String, List<Diagnostic>> diagnostics = new ConcurrentHashMap<>();

    /**
     * Called when a file is opened in the IDE.
     * Intercepted from TextDocumentService.didOpen().
     *
     * @param uri the file URI
     */
    public void onDidOpen(String uri) {
        openedFiles.add(uri);
    }

    /**
     * Called when a file is closed in the IDE.
     * Intercepted from TextDocumentService.didClose().
     *
     * @param uri the file URI
     */
    public void onDidClose(String uri) {
        openedFiles.remove(uri);
    }

    /**
     * Called when diagnostics are published by the language server.
     * Intercepted from LanguageClient.publishDiagnostics().
     *
     * @param uri         the file URI
     * @param diagnostics the diagnostics for this file
     */
    public void putDiagnostics(String uri, List<Diagnostic> diagnostics) {
        this.diagnostics.put(uri, new ArrayList<>(diagnostics));
    }

    /**
     * Check if a file is currently opened in the IDE.
     *
     * @param uri the file URI
     * @return true if the file is opened, false otherwise
     */
    public boolean isOpened(String uri) {
        return openedFiles.contains(uri);
    }

    /**
     * Get cached diagnostics for a file.
     *
     * @param uri the file URI
     * @return the diagnostics, or empty list if not in cache
     */
    public List<Diagnostic> getDiagnostics(String uri) {
        return diagnostics.getOrDefault(uri, Collections.emptyList());
    }

    /**
     * Remove diagnostics from cache.
     * Useful for cleanup after temporary didOpen/didClose.
     *
     * @param uri the file URI
     */
    public void removeDiagnostics(String uri) {
        diagnostics.remove(uri);
    }

    /**
     * Clear all cache (for testing or reset).
     */
    public void clear() {
        openedFiles.clear();
        diagnostics.clear();
    }
}
