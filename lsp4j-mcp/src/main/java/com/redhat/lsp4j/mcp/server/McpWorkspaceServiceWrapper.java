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

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Wrapper for WorkspaceService that intercepts didChangeConfiguration
 * to trigger MCP server restart if port changes.
 */
public class McpWorkspaceServiceWrapper {

    /**
     * Callback when didChangeConfiguration is received.
     */
    @FunctionalInterface
    public interface ConfigurationChangeListener {
        void onConfigurationChanged(DidChangeConfigurationParams params);
    }

    @SuppressWarnings("unchecked")
    public static <S extends WorkspaceService> S wrap(
            Class<S> serviceClass,
            S delegate,
            ConfigurationChangeListener listener) {

        return (S) Proxy.newProxyInstance(
            serviceClass.getClassLoader(),
            new Class<?>[]{serviceClass},
            new WorkspaceInvocationHandler<>(delegate, listener)
        );
    }

    private static class WorkspaceInvocationHandler<S extends WorkspaceService> implements InvocationHandler {
        private final S delegate;
        private final ConfigurationChangeListener listener;

        WorkspaceInvocationHandler(S delegate, ConfigurationChangeListener listener) {
            this.delegate = delegate;
            this.listener = listener;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Intercept didChangeConfiguration
            if (method.getName().equals("didChangeConfiguration")
                && args != null && args.length > 0
                && args[0] instanceof DidChangeConfigurationParams) {

                // Call delegate first
                Object result = method.invoke(delegate, args);

                // Notify listener
                if (listener != null) {
                    listener.onConfigurationChanged((DidChangeConfigurationParams) args[0]);
                }

                return result;
            }

            // Delegate all other methods
            return method.invoke(delegate, args);
        }
    }
}
