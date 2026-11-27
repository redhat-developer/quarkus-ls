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
package com.redhat.lsp4j.mcp.server;

import com.redhat.lsp4j.mcp.cache.McpCache;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Example showing the simplified MCP server integration API.
 *
 * This is NOT a real test - just documentation of the new API.
 */
public class McpLanguageServerWrapperUsageExample {

    /**
     * Example 1: Fixed port mode (9339)
     */
    public static void exampleFixedPort(LanguageServer myLanguageServer) {
        // Wrap the language server with MCP support on fixed port 9339
        LanguageServer wrappedServer = McpLanguageServerWrapper.wrap(
            LanguageServer.class,
            myLanguageServer,
            9339
        );

        // MCP server will start automatically in initialized() notification
        // MCP server will stop automatically in shutdown()

        // Use wrappedServer in your LSP launcher...
    }

    /**
     * Example 2: Settings-based mode (reads from workspace/configuration)
     */
    public static void exampleSettingsBased(LanguageServer myLanguageServer) {
        // Wrap the language server with MCP support based on settings
        LanguageServer wrappedServer = McpLanguageServerWrapper.wrap(
            LanguageServer.class,
            myLanguageServer,
            "qute.mcp.port"
        );

        // MCP server behavior:
        // - In initialized(): calls workspace/configuration to read "qute.mcp.port"
        // - If port is valid (> 0): starts MCP server
        // - If port is null/invalid: MCP server disabled
        // - On didChangeConfiguration: re-reads setting and restarts/stops server as needed
        // - On shutdown(): stops MCP server

        // Use wrappedServer in your LSP launcher...
    }

    /**
     * Example 3: No MCP support (backward compatibility)
     */
    public static void exampleNoMcp(LanguageServer myLanguageServer) {
        // Wrap without MCP config - only TextDocumentService wrapping for cache
        LanguageServer wrappedServer = McpLanguageServerWrapper.wrap(
            LanguageServer.class,
            myLanguageServer
        );

        // No MCP server started
        // Use wrappedServer in your LSP launcher...
    }

    /**
     * Full example with Language Server
     */
    public static void main(String[] args) {
        // Create your language server instance
        MyLanguageServer server = new MyLanguageServer();

        // Wrap with MCP support (settings-based)
        LanguageServer wrappedServer = McpLanguageServerWrapper.wrap(
            LanguageServer.class,
            server,
            "myls.mcp.port"
        );

        // Standard LSP launcher code (simplified)
        /*
        Launcher<LanguageClient> launcher = createServerLauncher(
            wrappedServer,
            System.in,
            System.out,
            Executors.newCachedThreadPool()
        );

        server.setClient(launcher.getRemoteProxy());
        launcher.startListening();
        */

        // That's it! No manual MCP server start/stop needed.
        // The wrapper handles everything automatically:
        // - Client wrapping (for publishDiagnostics interception)
        // - MCP cache management
        // - MCP server lifecycle (start/stop/restart)
    }

    // Dummy class for example
    static class MyLanguageServer implements LanguageServer {
        @Override
        public org.eclipse.lsp4j.services.TextDocumentService getTextDocumentService() {
            return null;
        }

        @Override
        public org.eclipse.lsp4j.services.WorkspaceService getWorkspaceService() {
            return null;
        }

        @Override
        public java.util.concurrent.CompletableFuture<org.eclipse.lsp4j.InitializeResult> initialize(
                org.eclipse.lsp4j.InitializeParams params) {
            return null;
        }

        @Override
        public java.util.concurrent.CompletableFuture<Object> shutdown() {
            return null;
        }

        @Override
        public void exit() {
        }
    }
}
