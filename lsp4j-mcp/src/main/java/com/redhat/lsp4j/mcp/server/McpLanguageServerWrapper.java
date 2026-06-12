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

import com.google.gson.JsonElement;
import com.redhat.lsp4j.mcp.cache.McpCache;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generic wrapper for LanguageServer that:
 * 1. Wraps TextDocumentService to intercept didOpen/didClose for MCP caching
 * 2. Automatically starts MCP server based on configuration
 *
 * Uses dynamic proxy to avoid implementing all interface methods manually.
 * Works with any LanguageServer implementation.
 *
 * @param <S> The specific LanguageServer type to wrap
 */
public class McpLanguageServerWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpLanguageServerWrapper.class);

    /**
     * Create a wrapper for a LanguageServer with MCP support.
     *
     * Simple usage - reads MCP port from settings:
     * <pre>
     * LanguageServer wrappedServer = McpLanguageServerWrapper.wrap(
     *     LanguageServer.class,
     *     myServer,
     *     "myls.mcp.port"  // Setting path to read port from
     * );
     * </pre>
     *
     * @param <S> The specific LanguageServer type
     * @param serverClass The LanguageServer class
     * @param delegate The LanguageServer instance to wrap
     * @param mcpPortSettingPath Setting path to read MCP port from (e.g., "qute.mcp.port")
     * @return A wrapped LanguageServer instance
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> S wrap(
            Class<S> serverClass,
            S delegate,
            String mcpPortSettingPath) {

        McpCache cache = new McpCache();
        McpServerConfig config = McpServerConfig.withSettingPath(mcpPortSettingPath);

        McpTextDocumentServiceWrapper wrappedTextDocService = new McpTextDocumentServiceWrapper(
            delegate.getTextDocumentService(),
            cache
        );

        return (S) Proxy.newProxyInstance(
            serverClass.getClassLoader(),
            new Class<?>[]{serverClass},
            new McpInvocationHandler<>(delegate, wrappedTextDocService, cache, config)
        );
    }

    /**
     * Create a wrapper for a LanguageServer with MCP support using a fixed port.
     *
     * @param <S> The specific LanguageServer type
     * @param serverClass The LanguageServer class
     * @param delegate The LanguageServer instance to wrap
     * @param port Fixed port for MCP server
     * @return A wrapped LanguageServer instance
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> S wrap(
            Class<S> serverClass,
            S delegate,
            int port) {

        McpCache cache = new McpCache();
        McpServerConfig config = McpServerConfig.withPort(port);

        McpTextDocumentServiceWrapper wrappedTextDocService = new McpTextDocumentServiceWrapper(
            delegate.getTextDocumentService(),
            cache
        );

        return (S) Proxy.newProxyInstance(
            serverClass.getClassLoader(),
            new Class<?>[]{serverClass},
            new McpInvocationHandler<>(delegate, wrappedTextDocService, cache, config)
        );
    }

    /**
     * Advanced: Create a wrapper with explicit cache and config (for custom scenarios).
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> S wrap(
            Class<S> serverClass,
            S delegate,
            McpCache cache,
            McpServerConfig mcpConfig) {

        McpTextDocumentServiceWrapper wrappedTextDocService = new McpTextDocumentServiceWrapper(
            delegate.getTextDocumentService(),
            cache
        );

        return (S) Proxy.newProxyInstance(
            serverClass.getClassLoader(),
            new Class<?>[]{serverClass},
            new McpInvocationHandler<>(delegate, wrappedTextDocService, cache, mcpConfig)
        );
    }

    /**
     * Create a wrapper for a LanguageServer without MCP support (only TextDocument wrapping).
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> S wrap(Class<S> serverClass, S delegate) {
        McpCache cache = new McpCache();
        return wrap(serverClass, delegate, cache, null);
    }

    /**
     * Helper for wrapping both Language Server and Language Client with MCP support.
     *
     * Usage:
     * <pre>
     * McpWrapper wrapper = McpLanguageServerWrapper.create(MyServerAPI.class, myServer, "my.mcp.port");
     * MyServerAPI wrappedServer = wrapper.getWrappedServer();
     * // ... create launcher ...
     * MyClientAPI wrappedClient = wrapper.wrapClient(MyClientAPI.class, launcher.getRemoteProxy());
     * myServer.setClient(wrappedClient);
     * </pre>
     */
    public static class McpWrapper<S extends LanguageServer> {
        private final S wrappedServer;
        private final McpInvocationHandler<?> handler;

        McpWrapper(S wrappedServer, McpInvocationHandler<?> handler) {
            this.wrappedServer = wrappedServer;
            this.handler = handler;
        }

        /**
         * Get the wrapped Language Server to pass to the LSP launcher.
         */
        public S getWrappedServer() {
            return wrappedServer;
        }

        /**
         * Wrap the Language Client from the launcher.
         * Call this after creating the launcher, before calling server.setClient().
         *
         * @param clientClass The client interface class (e.g., QuteLanguageClientAPI.class)
         * @param client The client from launcher.getRemoteProxy()
         * @return The wrapped client to pass to server.setClient()
         */
        public <C extends LanguageClient> C wrapClient(Class<C> clientClass, C client) {
            return handler.wrapAndSetClient(clientClass, client);
        }
    }

    /**
     * Create an MCP wrapper for a Language Server.
     *
     * Use this when setClient() is not in the LanguageServer interface.
     * This is the recommended approach for most Language Servers.
     *
     * @param serverClass The Language Server interface class
     * @param delegate The Language Server implementation
     * @param mcpPortSettingPath The LSP setting path to read the MCP port from (e.g., "qute.mcp.port")
     * @return An McpWrapper to get the wrapped server and wrap the client
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> McpWrapper<S> create(
            Class<S> serverClass,
            S delegate,
            String mcpPortSettingPath) {

        McpCache cache = new McpCache();
        McpServerConfig config = McpServerConfig.withSettingPath(mcpPortSettingPath);

        McpTextDocumentServiceWrapper wrappedTextDocService = new McpTextDocumentServiceWrapper(
            delegate.getTextDocumentService(),
            cache
        );

        McpInvocationHandler<S> handler = new McpInvocationHandler<>(delegate, wrappedTextDocService, cache, config);

        S wrappedServer = (S) Proxy.newProxyInstance(
            serverClass.getClassLoader(),
            new Class<?>[]{serverClass},
            handler
        );

        return new McpWrapper<>(wrappedServer, handler);
    }

    /**
     * Create an MCP wrapper for a Language Server with a fixed port.
     */
    @SuppressWarnings("unchecked")
    public static <S extends LanguageServer> McpWrapper<S> create(
            Class<S> serverClass,
            S delegate,
            int port) {

        McpCache cache = new McpCache();
        McpServerConfig config = McpServerConfig.withPort(port);

        McpTextDocumentServiceWrapper wrappedTextDocService = new McpTextDocumentServiceWrapper(
            delegate.getTextDocumentService(),
            cache
        );

        McpInvocationHandler<S> handler = new McpInvocationHandler<>(delegate, wrappedTextDocService, cache, config);

        S wrappedServer = (S) Proxy.newProxyInstance(
            serverClass.getClassLoader(),
            new Class<?>[]{serverClass},
            handler
        );

        return new McpWrapper<>(wrappedServer, handler);
    }

    private static class McpInvocationHandler<S extends LanguageServer> implements InvocationHandler {
        private final S delegate;
        private final McpTextDocumentServiceWrapper wrappedTextDocService;
        private WorkspaceService wrappedWorkspaceService;
        private final McpCache cache;
        private final McpServerConfig mcpConfig;
        private LspMcpServer mcpServer;
        private LanguageClient client;
        private String serverName = "lsp-mcp-server";
        private String serverVersion = "1.0.0";
        private Integer currentPort;
        private boolean isInitialized = false;

        McpInvocationHandler(S delegate, McpTextDocumentServiceWrapper wrappedTextDocService,
                           McpCache cache, McpServerConfig mcpConfig) {
            this.delegate = delegate;
            this.wrappedTextDocService = wrappedTextDocService;
            this.cache = cache;
            this.mcpConfig = mcpConfig;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Intercept getTextDocumentService
            if (method.getName().equals("getTextDocumentService") &&
                TextDocumentService.class.isAssignableFrom(method.getReturnType())) {
                return wrappedTextDocService;
            }

            // Intercept getWorkspaceService
            if (method.getName().equals("getWorkspaceService") &&
                WorkspaceService.class.isAssignableFrom(method.getReturnType())) {

                // Lazy wrap workspace service
                if (wrappedWorkspaceService == null && mcpConfig != null && mcpConfig.getSettingPath() != null) {
                    WorkspaceService originalWorkspaceService = delegate.getWorkspaceService();
                    wrappedWorkspaceService = McpWorkspaceServiceWrapper.wrap(
                        WorkspaceService.class,
                        originalWorkspaceService,
                        params -> {
                            // On didChangeConfiguration, re-check MCP port
                            if (client != null) {
                                requestMcpPortFromSettings(mcpConfig.getSettingPath());
                            }
                        }
                    );
                }

                return wrappedWorkspaceService != null ? wrappedWorkspaceService : delegate.getWorkspaceService();
            }

            // Intercept initialize to capture server info
            if (method.getName().equals("initialize") && args != null && args.length > 0
                && args[0] instanceof InitializeParams) {
                CompletableFuture<InitializeResult> result =
                    (CompletableFuture<InitializeResult>) method.invoke(delegate, args);

                // Extract server name/version if available
                InitializeParams params = (InitializeParams) args[0];
                if (params.getProcessId() != null) {
                    // Could extract from initialization options if needed
                }

                return result;
            }

            // Intercept initialized notification
            if (method.getName().equals("initialized") && mcpConfig != null) {
                // Call original initialized
                Object result = method.invoke(delegate, args);

                // Mark as initialized
                isInitialized = true;

                // Try to start MCP server (needs both initialized + client)
                tryStartMcpServer();

                return result;
            }

            // Intercept setClient to wrap the client and capture it
            if (method.getName().equals("setClient") && args != null && args.length > 0
                && args[0] instanceof LanguageClient) {
                LanguageClient originalClient = (LanguageClient) args[0];

                // Wrap the client to intercept publishDiagnostics
                LanguageClient wrappedClient = McpLanguageClientWrapper.wrap(
                    LanguageClient.class,
                    originalClient,
                    cache
                );

                this.client = wrappedClient;

                // Call setClient with wrapped client
                Object result = method.invoke(delegate, wrappedClient);

                // Try to start MCP server (needs both initialized + client)
                if (mcpConfig != null) {
                    tryStartMcpServer();
                }

                return result;
            }

            // Intercept shutdown to stop MCP server
            if (method.getName().equals("shutdown")) {
                stopMcpServer();
                return method.invoke(delegate, args);
            }

            // Delegate all other methods
            return method.invoke(delegate, args);
        }

        @SuppressWarnings("unchecked")
        <C extends LanguageClient> C wrapAndSetClient(Class<C> clientClass, C originalClient) {
            // Wrap the client to intercept publishDiagnostics
            C wrappedClient = McpLanguageClientWrapper.wrap(
                clientClass,
                originalClient,
                cache
            );

            this.client = wrappedClient;

            // Try to start MCP server (needs both initialized + client)
            if (mcpConfig != null) {
                tryStartMcpServer();
            }

            return wrappedClient;
        }

        private void tryStartMcpServer() {
            // Need both initialized flag AND client to start
            if (!isInitialized || client == null) {
                LOGGER.debug("Cannot start MCP server yet: isInitialized={}, client={}", isInitialized, client != null);
                return;
            }

            // Already started?
            if (mcpServer != null && mcpServer.isStarted()) {
                return;
            }

            // Start based on config
            if (mcpConfig.getPort() != null) {
                // Fixed port mode
                startMcpServer(mcpConfig.getPort());
            } else if (mcpConfig.getSettingPath() != null) {
                // Settings-based mode - request configuration
                requestMcpPortFromSettings(mcpConfig.getSettingPath());
            }
        }

        private void requestMcpPortFromSettings(String settingPath) {
            if (client == null) {
                LOGGER.warn("Cannot request MCP port: client not set");
                return;
            }

            ConfigurationParams params = new ConfigurationParams();
            ConfigurationItem item = new ConfigurationItem();
            item.setSection(settingPath);
            params.setItems(Collections.singletonList(item));

            client.configuration(params).thenAccept(configs -> {
                if (configs != null && !configs.isEmpty()) {
                    Object config = configs.get(0);
                    Integer newPort = extractPort(config);

                    // Check if port changed
                    if (newPort != null && newPort > 0) {
                        if (currentPort == null || !currentPort.equals(newPort)) {
                            LOGGER.info("MCP port changed from {} to {} (settings: {})",
                                currentPort, newPort, settingPath);
                            // Stop old server if running
                            stopMcpServer();
                            // Start with new port
                            startMcpServer(newPort);
                            currentPort = newPort;
                        }
                    } else {
                        // Port removed or invalid - stop server
                        if (currentPort != null) {
                            LOGGER.info("MCP server disabled (port removed from settings {})", settingPath);
                            stopMcpServer();
                            currentPort = null;
                        }
                    }
                }
            }).exceptionally(e -> {
                LOGGER.error("Failed to request MCP configuration from settings: " + settingPath, e);
                return null;
            });
        }

        private Integer extractPort(Object config) {
            if (config instanceof Number) {
                return ((Number) config).intValue();
            } else if (config instanceof JsonElement) {
                JsonElement element = (JsonElement) config;
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                    return element.getAsInt();
                }
            }
            return null;
        }

        private void startMcpServer(int port) {
            if (mcpServer != null && mcpServer.isStarted()) {
                LOGGER.warn("MCP server already started on port {}", currentPort);
                return;
            }

            try {
                LOGGER.info("Starting MCP server on port {}", port);
                mcpServer = LspMcpServer.builder()
                    .serverInfo(serverName, serverVersion)
                    .port(port)
                    .registerDependency(org.eclipse.lsp4j.services.LanguageServer.class, delegate)
                    .registerDependency(com.redhat.lsp4j.mcp.cache.McpCache.class, cache)
                    .build();

                mcpServer.start();
                currentPort = port;
                LOGGER.info("MCP server started successfully on port {}", port);
            } catch (Exception e) {
                LOGGER.error("Failed to start MCP server on port " + port, e);
                currentPort = null;
            }
        }

        private void stopMcpServer() {
            if (mcpServer != null && mcpServer.isStarted()) {
                LOGGER.info("Stopping MCP server on port {}", currentPort);
                try {
                    mcpServer.stop();
                    mcpServer = null;
                    currentPort = null;
                } catch (Exception e) {
                    LOGGER.error("Error stopping MCP server", e);
                }
            }
        }
    }

    /**
     * MCP server configuration.
     */
    public static class McpServerConfig {
        private final Integer port;
        private final String settingPath;

        private McpServerConfig(Integer port, String settingPath) {
            this.port = port;
            this.settingPath = settingPath;
        }

        /**
         * Create config with fixed port.
         */
        public static McpServerConfig withPort(int port) {
            return new McpServerConfig(port, null);
        }

        /**
         * Create config with settings path.
         */
        public static McpServerConfig withSettingPath(String settingPath) {
            return new McpServerConfig(null, settingPath);
        }

        public Integer getPort() {
            return port;
        }

        public String getSettingPath() {
            return settingPath;
        }
    }
}
