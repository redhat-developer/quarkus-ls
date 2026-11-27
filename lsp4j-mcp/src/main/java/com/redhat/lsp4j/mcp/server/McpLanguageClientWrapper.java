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
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.services.LanguageClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Generic wrapper for LanguageClient that intercepts publishDiagnostics
 * to cache diagnostics for MCP tools.
 *
 * Uses dynamic proxy to avoid implementing all interface methods manually.
 * Works with any LanguageClient implementation.
 *
 * @param <C> The specific LanguageClient type to wrap
 */
public class McpLanguageClientWrapper {

    /**
     * Create a wrapper for a LanguageClient that intercepts publishDiagnostics.
     *
     * @param <C> The specific LanguageClient type
     * @param clientClass The LanguageClient class
     * @param delegate The LanguageClient instance to wrap
     * @param cache The MCP cache for storing diagnostics
     * @return A wrapped LanguageClient instance
     */
    @SuppressWarnings("unchecked")
    public static <C extends LanguageClient> C wrap(Class<C> clientClass, C delegate, McpCache cache) {
        return (C) Proxy.newProxyInstance(
            clientClass.getClassLoader(),
            new Class<?>[]{clientClass},
            new McpInvocationHandler<>(delegate, cache)
        );
    }

    private static class McpInvocationHandler<C extends LanguageClient> implements InvocationHandler {
        private final C delegate;
        private final McpCache cache;

        McpInvocationHandler(C delegate, McpCache cache) {
            this.delegate = delegate;
            this.cache = cache;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Intercept publishDiagnostics
            if (method.getName().equals("publishDiagnostics") && args != null && args.length == 1) {
                PublishDiagnosticsParams diagnostics = (PublishDiagnosticsParams) args[0];
                cache.putDiagnostics(diagnostics.getUri(), diagnostics.getDiagnostics());
            }

            // Delegate to the real client
            return method.invoke(delegate, args);
        }
    }
}
