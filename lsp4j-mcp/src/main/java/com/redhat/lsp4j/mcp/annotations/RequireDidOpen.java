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
package com.redhat.lsp4j.mcp.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a tool requires the file to be opened before execution.
 *
 * The MCP framework will automatically:
 * 1. Check if the file (specified by 'uri' parameter) is already opened
 * 2. If not, simulate didOpen with file content from disk
 * 3. Execute the tool method
 * 4. If file was not originally opened, cleanup with didClose
 *
 * The tool method must have a parameter named 'uri' of type String.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireDidOpen {

    /**
     * Name of the parameter containing the file URI.
     * Default is "uri".
     */
    String uriParam() default "uri";

    /**
     * Language ID to use when opening the file (e.g., "qute", "java").
     * Default is "qute".
     */
    String languageId() default "qute";
}
