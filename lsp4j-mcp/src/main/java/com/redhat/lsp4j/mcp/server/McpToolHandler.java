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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Functional interface for MCP tool handlers.
 */
@FunctionalInterface
public interface McpToolHandler {

    /**
     * Execute the tool with the given request.
     *
     * @param exchange the MCP server exchange
     * @param request  the tool call request
     * @return the tool result
     */
    McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request);
}
