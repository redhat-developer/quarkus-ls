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

import com.redhat.lsp4j.mcp.annotations.ToolArg;

/**
 * TextDocumentIdentifier with MCP annotations for automatic schema generation.
 * Note: We don't extend LSP4J TextDocumentIdentifier because Gson needs direct field access.
 */
public class TextDocumentIdentifier {

    private String uri;

    public TextDocumentIdentifier() {
    }

    public TextDocumentIdentifier(String uri) {
        this.uri = uri;
    }

    @ToolArg(description = "File URI (e.g., file:///path/to/file)")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
