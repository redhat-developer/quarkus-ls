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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lsp4j.mcp.GSonUtils;
import com.redhat.lsp4j.mcp.annotations.Inject;
import com.redhat.lsp4j.mcp.annotations.RequireDidOpen;
import com.redhat.lsp4j.mcp.annotations.Tool;
import com.redhat.lsp4j.mcp.annotations.ToolArg;
import com.redhat.lsp4j.mcp.cache.McpCache;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Registry for MCP tools with dependency injection and automatic registration.
 */
public class McpToolRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpToolRegistry.class);

    private final Map<Class<?>, Object> dependencies = new HashMap<>();
    private final List<LspMcpServer.McpToolRegistration> toolRegistrations = new ArrayList<>();

    /**
     * Register a dependency for injection.
     */
    public void register(Class<?> type, Object instance) {
        dependencies.put(type, instance);
    }

    /**
     * Inject dependencies into a tool class instance and scan its @Tool methods.
     */
    public void scanAndRegister(Object toolInstance) {
        // Inject dependencies
        injectDependencies(toolInstance);

        // Scan @Tool methods
        for (Method method : toolInstance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Tool.class)) {
                registerTool(toolInstance, method);
            }
        }
    }

    /**
     * Get all registered tools.
     */
    public List<LspMcpServer.McpToolRegistration> getToolRegistrations() {
        return toolRegistrations;
    }

    private void injectDependencies(Object instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = dependencies.get(field.getType());
                if (dependency == null) {
                    throw new IllegalStateException("No dependency registered for type: " + field.getType());
                }
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency: " + field.getName(), e);
                }
            }
        }
    }

    private void registerTool(Object toolInstance, Method method) {
        Tool toolAnnotation = method.getAnnotation(Tool.class);

        // Determine tool name
        String toolName = toolAnnotation.name();
        if (toolName.isEmpty()) {
            toolName = camelToSnake(method.getName());
        }

        String description = toolAnnotation.description();

        // Generate input schema
        Map<String, Object> inputSchema = generateInputSchema(method);

        // Create handler
        McpToolHandler handler = createHandler(toolInstance, method);

        LOGGER.info("Registered tool: {} from method {}.{}", toolName,
                    toolInstance.getClass().getSimpleName(), method.getName());

        toolRegistrations.add(new LspMcpServer.McpToolRegistration(toolName, description, inputSchema, handler));
    }

    public Map<String, Object> generateInputSchema(Method method) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (Parameter param : method.getParameters()) {
            // Get parameter name from @ToolArg annotation if present, otherwise use Java name (arg0, arg1, etc.)
            String paramName = param.getName();
            ToolArg paramAnnotation = param.getAnnotation(ToolArg.class);
            if (paramAnnotation != null && !paramAnnotation.name().isEmpty()) {
                paramName = paramAnnotation.name();
            }

            // Check if parameter type has @ToolArg on its methods (like Position)
            if (hasToolArgAnnotations(param.getType())) {
                // Nested object - recurse into its methods
                Map<String, Object> nestedProps = new LinkedHashMap<>();
                List<String> nestedRequired = new ArrayList<>();
                for (Method getter : param.getType().getDeclaredMethods()) {
                    if (getter.isAnnotationPresent(ToolArg.class)) {
                        ToolArg argAnnotation = getter.getAnnotation(ToolArg.class);
                        String fieldName = getterToFieldName(getter.getName());
                        nestedProps.put(fieldName, Map.of(
                            "type", javaTypeToJsonType(getter.getReturnType()),
                            "description", argAnnotation.description()
                        ));
                        nestedRequired.add(fieldName);
                    }
                }
                properties.put(paramName, Map.of(
                    "type", "object",
                    "properties", nestedProps,
                    "required", nestedRequired
                ));
                required.add(paramName);
            } else {
                // Simple parameter
                ToolArg argAnnotation = param.getAnnotation(ToolArg.class);
                String description = argAnnotation != null ? argAnnotation.description() : "";

                properties.put(paramName, Map.of(
                    "type", javaTypeToJsonType(param.getType()),
                    "description", description
                ));

                required.add(paramName);
            }
        }

        return Map.of(
            "type", "object",
            "properties", properties,
            "required", required
        );
    }

    private boolean hasToolArgAnnotations(Class<?> type) {
        for (Method method : type.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ToolArg.class)) {
                return true;
            }
        }
        return false;
    }

    private String getterToFieldName(String getterName) {
        if (getterName.startsWith("get") && getterName.length() > 3) {
            return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
        }
        return getterName;
    }

    private String javaTypeToJsonType(Class<?> type) {
        if (type == String.class) return "string";
        if (type == int.class || type == Integer.class) return "integer";
        if (type == long.class || type == Long.class) return "integer";
        if (type == double.class || type == Double.class) return "number";
        if (type == float.class || type == Float.class) return "number";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        return "object";
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private McpToolHandler createHandler(Object toolInstance, Method method) {
        RequireDidOpen requireDidOpen = method.getAnnotation(RequireDidOpen.class);

        if (requireDidOpen != null) {
            return new DidOpenInterceptorHandler(toolInstance, method, requireDidOpen);
        } else {
            return new SimpleMethodHandler(toolInstance, method);
        }
    }

    /**
     * Simple handler that just invokes the method.
     */
    private static class SimpleMethodHandler implements McpToolHandler {
        private final Object toolInstance;
        private final Method method;

        SimpleMethodHandler(Object toolInstance, Method method) {
            this.toolInstance = toolInstance;
            this.method = method;
        }

        @Override
        public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
            try {
                Map<String, Object> args = request.arguments();
                Object[] methodArgs = extractMethodArgs(method, args);

                Object result = method.invoke(toolInstance, methodArgs);

                String json = GSonUtils.toJson(result);
                return McpSchema.CallToolResult.builder()
                    .content(List.of(new McpSchema.TextContent(json)))
                    .isError(false)
                    .build();

            } catch (Exception e) {
                LOGGER.error("Error executing tool", e);
                return errorResult(e.getMessage());
            }
        }

        private Object[] extractMethodArgs(Method method, Map<String, Object> args) {
            Parameter[] params = method.getParameters();
            Object[] methodArgs = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                // Use same logic as generateInputSchema to get parameter name
                String paramName = param.getName();
                ToolArg paramAnnotation = param.getAnnotation(ToolArg.class);
                if (paramAnnotation != null && !paramAnnotation.name().isEmpty()) {
                    paramName = paramAnnotation.name();
                }
                Object value = args.get(paramName);

                // If value is null but param is not a primitive, try to construct from args
                if (value == null && !param.getType().isPrimitive()) {
                    // Try to deserialize the whole args map into the parameter type
                    // This handles cases where MCP client sends flat structure but we expect nested objects
                    try {
                        value = GSonUtils.getGson().fromJson(
                            GSonUtils.getGson().toJson(args),
                            param.getType()
                        );
                    } catch (Exception e) {
                        // If that fails, leave it null
                    }
                } else if (value instanceof Map && !param.getType().equals(Map.class)) {
                    // Handle nested objects like Position when explicitly provided
                    value = GSonUtils.getGson().fromJson(
                        GSonUtils.getGson().toJson(value),
                        param.getType()
                    );
                }

                methodArgs[i] = value;
            }

            return methodArgs;
        }

        private McpSchema.CallToolResult errorResult(String message) {
            Map<String, String> error = Map.of("error", message);
            return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(GSonUtils.toJson(error))))
                .isError(true)
                .build();
        }
    }

    /**
     * Handler that wraps method invocation with didOpen/didClose logic.
     */
    private class DidOpenInterceptorHandler implements McpToolHandler {
        private final Object toolInstance;
        private final Method method;
        private final RequireDidOpen annotation;

        DidOpenInterceptorHandler(Object toolInstance, Method method, RequireDidOpen annotation) {
            this.toolInstance = toolInstance;
            this.method = method;
            this.annotation = annotation;
        }

        @Override
        public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
            try {
                Map<String, Object> args = request.arguments();
                String uri = extractUri(args);

                McpCache cache = (McpCache) dependencies.get(McpCache.class);
                LanguageServer languageServer = (LanguageServer) dependencies.get(LanguageServer.class);

                boolean wasOpened = cache.isOpened(uri);

                // If file not opened, simulate didOpen
                if (!wasOpened) {
                    LOGGER.info("File not opened, simulating didOpen: {}", uri);

                    String content;
                    try {
                        String path = uri.replace("file:///", "").replace("file://", "");
                        content = Files.readString(Paths.get(path));
                    } catch (Exception e) {
                        return errorResult("Failed to read file: " + e.getMessage());
                    }

                    DidOpenTextDocumentParams didOpenParams = new DidOpenTextDocumentParams();
                    TextDocumentItem textDocument = new TextDocumentItem();
                    textDocument.setUri(uri);
                    textDocument.setLanguageId(annotation.languageId());
                    textDocument.setVersion(1);
                    textDocument.setText(content);
                    didOpenParams.setTextDocument(textDocument);

                    languageServer.getTextDocumentService().didOpen(didOpenParams);

                    // Wait for publishDiagnostics to be called
                    // TODO: Use a proper notification mechanism instead of sleep
                    Thread.sleep(500);
                }

                // Execute the actual tool method
                Object[] methodArgs = extractMethodArgs(method, args);
                Object result = method.invoke(toolInstance, methodArgs);

                // Cleanup if needed
                if (!wasOpened) {
                    LOGGER.info("Cleaning up with didClose: {}", uri);
                    DidCloseTextDocumentParams didCloseParams = new DidCloseTextDocumentParams();
                    TextDocumentIdentifier textDocument = new TextDocumentIdentifier(uri);
                    didCloseParams.setTextDocument(textDocument);
                    languageServer.getTextDocumentService().didClose(didCloseParams);
                }

                String json = GSonUtils.toJson(result);
                return McpSchema.CallToolResult.builder()
                    .content(List.of(new McpSchema.TextContent(json)))
                    .isError(false)
                    .build();

            } catch (Exception e) {
                LOGGER.error("Error executing tool with didOpen interception", e);
                return errorResult(e.getMessage());
            }
        }

        private String extractUri(Map<String, Object> args) {
            String uriParamName = annotation.uriParam();

            // Support nested paths like "textDocument.uri"
            String[] parts = uriParamName.split("\\.");
            Object current = args;

            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    throw new IllegalArgumentException("Cannot navigate to " + uriParamName);
                }

                if (current == null) {
                    throw new IllegalArgumentException("Missing required uri parameter: " + uriParamName);
                }
            }

            return current.toString();
        }

        public Object[] extractMethodArgs(Method method, Map<String, Object> args) {
            Parameter[] params = method.getParameters();
            Object[] methodArgs = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                // Use same logic as generateInputSchema to get parameter name
                String paramName = param.getName();
                ToolArg paramAnnotation = param.getAnnotation(ToolArg.class);
                if (paramAnnotation != null && !paramAnnotation.name().isEmpty()) {
                    paramName = paramAnnotation.name();
                }
                Object value = args.get(paramName);

                // If value is null but param is not a primitive, try to construct from args
                if (value == null && !param.getType().isPrimitive()) {
                    // Try to deserialize the whole args map into the parameter type
                    // This handles cases where MCP client sends flat structure but we expect nested objects
                    try {
                        value = GSonUtils.getGson().fromJson(
                            GSonUtils.getGson().toJson(args),
                            param.getType()
                        );
                    } catch (Exception e) {
                        // If that fails, leave it null
                    }
                } else if (value instanceof Map && !param.getType().equals(Map.class)) {
                    // Handle nested objects like Position when explicitly provided
                    value = GSonUtils.getGson().fromJson(
                        GSonUtils.getGson().toJson(value),
                        param.getType()
                    );
                }

                methodArgs[i] = value;
            }

            return methodArgs;
        }

        private McpSchema.CallToolResult errorResult(String message) {
            Map<String, String> error = Map.of("error", message);
            return McpSchema.CallToolResult.builder()
                .content(List.of(new McpSchema.TextContent(GSonUtils.toJson(error))))
                .isError(true)
                .build();
        }
    }
}
