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
 * Position with MCP annotations for automatic schema generation.
 * Note: We don't extend LSP4J Position because Gson needs direct field access for deserialization.
 */
public class Position {

    private int line;
    private int character;

    public Position() {
    }

    public Position(int line, int character) {
        this.line = line;
        this.character = character;
    }

    @ToolArg(description = "Line number (0-based)")
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @ToolArg(description = "Character offset in line (0-based)")
    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }
}
