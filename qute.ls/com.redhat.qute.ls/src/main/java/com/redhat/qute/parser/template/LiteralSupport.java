/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.template;

import java.util.regex.Pattern;

public class LiteralSupport {

	private static final Pattern INTEGER_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,10}");
	private static final Pattern LONG_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,19}(L|l)");
	private static final Pattern DOUBLE_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(d|D)");
	private static final Pattern FLOAT_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(f|F)");

	public static String getLiteralJavaType(String literal) {
		if (literal == null || literal.isEmpty()) {
			return null;
		}
		if (isStringLiteralSeparator(literal.charAt(0))) {
			return "java.lang.String";
		} else if (literal.equals("true")) {
			return "java.lang.Boolean";
		} else if (literal.equals("false")) {
			return "java.lang.Boolean";
		} else if (literal.equals("null")) {
			return "null";
		} else {
			char firstChar = literal.charAt(0);
			if (Character.isDigit(firstChar) || firstChar == '-' || firstChar == '+') {
				if (INTEGER_LITERAL_PATTERN.matcher(literal).matches()) {
					return "int";
				} else if (LONG_LITERAL_PATTERN.matcher(literal).matches()) {
					return "long";
				} else if (DOUBLE_LITERAL_PATTERN.matcher(literal).matches()) {
					return "double";
				} else if (FLOAT_LITERAL_PATTERN.matcher(literal).matches()) {
					return "float";
				}
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the char is a string literal separator,
	 * <code>false</code> otherwise.
	 * 
	 * @param character
	 * @return <code>true</code> if the char is a string literal separator,
	 *         <code>false</code> otherwise.
	 */
	private static boolean isStringLiteralSeparator(char character) {
		return character == '"' || character == '\'';
	}
}
