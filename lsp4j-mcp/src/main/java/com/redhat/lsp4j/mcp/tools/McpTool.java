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
package com.redhat.lsp4j.mcp.tools;

/**
 * Marker interface for MCP tools discovered via SPI.
 *
 * Classes implementing this interface will be automatically discovered
 * and their @Tool annotated methods will be registered as MCP tools.
 *
 * This interface has no methods - it's purely for ServiceLoader discovery.
 */
public interface McpTool {
}
