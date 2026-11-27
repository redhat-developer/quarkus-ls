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
package com.redhat.lsp4j.mcp;

import java.util.HashMap;

import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;

import com.google.gson.Gson;

/**
 * Utilities for JSON serialization of LSP4J objects.
 */
public class GSonUtils {

    private static final Gson LSP4J_GSON = new MessageJsonHandler(new HashMap<>()).getGson();

    private GSonUtils() {
    }

    /**
     * Returns a Gson instance configured with all LSP4J type adapters.
     * This includes adapters for Position, Range, Diagnostic, CodeAction, etc.
     *
     * @return a Gson instance configured for LSP4J objects
     */
    public static Gson getGson() {
        return LSP4J_GSON;
    }

    /**
     * Converts an object to JSON using LSP4J's Gson configuration.
     *
     * @param obj the object to serialize
     * @return JSON string
     */
    public static String toJson(Object obj) {
        return LSP4J_GSON.toJson(obj);
    }
}
