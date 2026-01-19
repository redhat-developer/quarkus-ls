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

public class YamlConstants {
    public static final int _MIN = "-".codePointAt(0);      // dash for list items
    public static final int _DDT = ":".codePointAt(0);      // key/value separator
    public static final int _DQO = "\"".codePointAt(0);     // double quote
    public static final int _SQO = "\'".codePointAt(0);     // single quote
    public static final int _WSP = " ".codePointAt(0);      // space
    public static final int _TAB = "\t".codePointAt(0);
    public static final int _NWL = "\n".codePointAt(0);     // newline
    public static final int _CAR = "\r".codePointAt(0);     // carriage return
    public static final int _LFD = "\f".codePointAt(0);     // form feed
    public static final int _PIPE = "|".codePointAt(0);     // multi-line scalar
    public static final int _GT = ">".codePointAt(0);       // folded scalar
    public static final int _HASHTAG = "#".codePointAt(0);  // comment
}
