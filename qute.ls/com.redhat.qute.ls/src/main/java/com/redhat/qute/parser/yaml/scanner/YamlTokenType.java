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
package com.redhat.qute.parser.yaml.scanner;

/**
 * YAML token types.
 */
public enum YamlTokenType {

	// Content tokens
    Content,
    Whitespace,
    Newline,
    
    // Key-Value tokens
    Key,
    Colon,
    Value,
    
    // Scalar value types
    ScalarString,
    ScalarNumber,
    ScalarBoolean,
    ScalarNull,
    
    // String tokens
    StartString,
    String,
    EndString,
    
    // List tokens
    Dash,
    
    // Flow collection tokens (JSON-style)
    ArrayOpen,      // [
    ArrayClose,     // ]
    ObjectOpen,     // {
    ObjectClose,    // }
    Comma,
    
    // Comment tokens
    StartComment,
    Comment,
    EndComment,
    
    // Other
    Unknown,
    EOS;
}
