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

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Generic MCP (Model Context Protocol) Server for LSP4J-based language servers.
 *
 * This server exposes language server capabilities as MCP tools via HTTP/SSE,
 * allowing AI assistants like Claude Code or Bob to interact with the language server.
 *
 * Architecture:
 * <pre>
 * AI Client (Claude Code, Bob)
 *   ↕ HTTP/SSE (MCP protocol)
 * LspMcpServer (Undertow on configurable port)
 *   ↕ Java API
 * Language Server (via MCP tools)
 * </pre>
 *
 * Usage:
 * <pre>
 * LspMcpServer mcpServer = LspMcpServer.builder()
 *     .serverInfo("qute-ls", "1.0.0")
 *     .port(9339)
 *     .registerTool(new GetDiagnosticsTool(cache, languageServer))
 *     .registerTool(new QuteDataModelTool(languageServer))
 *     .build();
 *
 * mcpServer.start();
 * </pre>
 */
public class LspMcpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LspMcpServer.class);

    private static final String DEFAULT_SSE_ENDPOINT = "/sse";
    private static final String DEFAULT_MESSAGE_ENDPOINT = "/mcp/message";
    private static final int DEFAULT_PORT = 9339;

    private final String serverName;
    private final String serverVersion;
    private final int port;
    private final List<McpToolRegistration> tools;

    private Undertow undertowServer;
    private McpSyncServer mcpServer;
    private HttpServletSseServerTransportProvider transportProvider;
    private boolean started = false;

    private LspMcpServer(Builder builder) {
        this.serverName = builder.serverName;
        this.serverVersion = builder.serverVersion;
        this.port = builder.port;
        this.tools = new ArrayList<>(builder.tools);
    }

    /**
     * Start the MCP server.
     *
     * The server will listen on http://localhost:{port} with:
     * - /sse for SSE connections
     * - /mcp/message for client messages
     *
     * @throws IllegalStateException if server is already started
     */
    public void start() {
        if (started) {
            throw new IllegalStateException("MCP server already started");
        }

        LOGGER.info("Starting MCP Server: {} v{}", serverName, serverVersion);

        try {
            // Create Jackson3 JSON mapper
            JsonMapper jackson3Mapper = JsonMapper.builder().build();
            JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(jackson3Mapper);

            // Create HTTP/SSE transport provider (this is a Servlet)
            transportProvider = HttpServletSseServerTransportProvider.builder()
                    .jsonMapper(mcpJsonMapper)
                    .messageEndpoint(DEFAULT_MESSAGE_ENDPOINT)
                    .sseEndpoint(DEFAULT_SSE_ENDPOINT)
                    .build();

            // Build MCP server with capabilities and tools
            var serverBuilder = io.modelcontextprotocol.server.McpServer.sync(transportProvider)
                    .serverInfo(serverName, serverVersion)
                    .jsonMapper(mcpJsonMapper)
                    .jsonSchemaValidator(new DefaultJsonSchemaValidator())
                    .capabilities(McpSchema.ServerCapabilities.builder()
                            .tools(true)
                            .build());

            // Register all tools
            LOGGER.info("Registering {} MCP tools", tools.size());
            for (McpToolRegistration toolReg : tools) {
                LOGGER.info("Registering tool: {} - {}", toolReg.getName(), toolReg.getDescription());

                serverBuilder.toolCall(
                        McpSchema.Tool.builder()
                                .name(toolReg.getName())
                                .description(toolReg.getDescription())
                                .inputSchema(toolReg.getInputSchema())
                                .build(),
                        (exchange, request) -> {
                            try {
                                return toolReg.getHandler().execute(exchange, request);
                            } catch (Exception e) {
                                LOGGER.error("Error executing tool: " + toolReg.getName(), e);
                                return McpSchema.CallToolResult.builder()
                                        .content(List.of(new McpSchema.TextContent(
                                                "Error: " + e.getMessage()
                                        )))
                                        .isError(true)
                                        .build();
                            }
                        }
                );
                LOGGER.info("Registered MCP tool: {} - {}", toolReg.getName(), toolReg.getDescription());
            }

            mcpServer = serverBuilder.build();

            // Configure Undertow servlet deployment
            // Use singleton instance to maintain SSE sessions
            final HttpServletSseServerTransportProvider singletonServlet = transportProvider;

            DeploymentInfo servletBuilder = Servlets.deployment()
                    .setClassLoader(LspMcpServer.class.getClassLoader())
                    .setContextPath("/")
                    .setDeploymentName("lsp4j-mcp")
                    .addServlets(
                            Servlets.servlet("mcpServlet", HttpServletSseServerTransportProvider.class,
                                            new InstanceFactory<HttpServletSseServerTransportProvider>() {
                                                @Override
                                                public InstanceHandle<HttpServletSseServerTransportProvider> createInstance() {
                                                    return new InstanceHandle<HttpServletSseServerTransportProvider>() {
                                                        @Override
                                                        public HttpServletSseServerTransportProvider getInstance() {
                                                            return singletonServlet;
                                                        }

                                                        @Override
                                                        public void release() {
                                                            // Never release - we manage lifecycle
                                                        }
                                                    };
                                                }
                                            })
                                    .addMapping("/*")
                                    .setAsyncSupported(true)
                    );

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            // Create and start Undertow server
            undertowServer = Undertow.builder()
                    .addHttpListener(port, "localhost")
                    .setHandler(manager.start())
                    .build();

            undertowServer.start();

            started = true;
            LOGGER.info("MCP Server started successfully");
            LOGGER.info("  SSE endpoint: http://localhost:{}{}", port, DEFAULT_SSE_ENDPOINT);
            LOGGER.info("  Message endpoint: http://localhost:{}{}", port, DEFAULT_MESSAGE_ENDPOINT);

        } catch (ServletException e) {
            LOGGER.error("Failed to deploy MCP servlet", e);
            throw new RuntimeException("Failed to start MCP server", e);
        } catch (Exception e) {
            LOGGER.error("Failed to start MCP server", e);
            throw new RuntimeException("Failed to start MCP server", e);
        }
    }

    /**
     * Stop the MCP server.
     */
    public void stop() {
        if (!started) {
            return;
        }

        LOGGER.info("Stopping MCP Server");
        try {
            if (undertowServer != null) {
                undertowServer.stop();
            }
            if (transportProvider != null) {
                transportProvider.close();
            }
            if (mcpServer != null) {
                mcpServer.close();
            }
            started = false;
            LOGGER.info("MCP Server stopped");
        } catch (Exception e) {
            LOGGER.error("Error stopping MCP server", e);
        }
    }

    /**
     * Check if the server is running.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Create a new builder for LspMcpServer.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for LspMcpServer.
     */
    public static class Builder {
        private String serverName = "lsp4j-mcp-server";
        private String serverVersion = "1.0.0";
        private int port = DEFAULT_PORT;
        private final McpToolRegistry registry = new McpToolRegistry();
        private final List<McpToolRegistration> tools = new ArrayList<>();

        /**
         * Set the server name and version.
         */
        public Builder serverInfo(String name, String version) {
            this.serverName = name;
            this.serverVersion = version;
            return this;
        }

        /**
         * Set the port to listen on.
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Register a dependency for injection into tool providers.
         */
        public Builder registerDependency(Class<?> type, Object instance) {
            registry.register(type, instance);
            return this;
        }

        /**
         * Register an MCP tool manually (legacy support).
         * @deprecated Use annotation-based tools with SPI instead
         */
        @Deprecated
        public Builder registerTool(String name, String description, Map<String, Object> inputSchema, McpToolHandler handler) {
            this.tools.add(new McpToolRegistration(name, description, inputSchema, handler));
            return this;
        }

        /**
         * Build the LspMcpServer.
         */
        public LspMcpServer build() {
            // Auto-discover tool classes via SPI
            loadToolsFromSPI();

            // Add discovered tools to the tools list
            tools.addAll(registry.getToolRegistrations());

            return new LspMcpServer(this);
        }

        private void loadToolsFromSPI() {
            // Use ServiceLoader to discover MCP tools
            ServiceLoader<com.redhat.lsp4j.mcp.tools.McpTool> loader =
                ServiceLoader.load(com.redhat.lsp4j.mcp.tools.McpTool.class);

            for (com.redhat.lsp4j.mcp.tools.McpTool tool : loader) {
                LOGGER.info("Discovered MCP tool: {}", tool.getClass().getName());
                registry.scanAndRegister(tool);
            }
        }
    }

    /**
     * Tool registration.
     */
    public static class McpToolRegistration {
        private final String name;
        private final String description;
        private final Map<String, Object> inputSchema;
        private final McpToolHandler handler;

        McpToolRegistration(String name, String description, Map<String, Object> inputSchema, McpToolHandler handler) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
            this.handler = handler;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getInputSchema() {
            return inputSchema;
        }

        public McpToolHandler getHandler() {
            return handler;
        }
    }
}
