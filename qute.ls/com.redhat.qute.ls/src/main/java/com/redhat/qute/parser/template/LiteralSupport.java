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

/**
 * Qute literal support.
 * 
 * @author Angelo ZERR
 *
 */
public class LiteralSupport {

	// Java types
	private static final String STRING_TYPE = "java.lang.String";
	private static final String BOOLEAN_TYPE = "java.lang.Boolean";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String FLOAT_TYPE = "float";
	private static final String DOUBLE_TYPE = "double";
	private static final String LONG_TYPE = "long";
	private static final String INTEGER_TYPE = "int";
	private static final String NULL_TYPE = "null";

	// Numeric pattern

	private static final Pattern INTEGER_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,10}");
	private static final Pattern LONG_LITERAL_PATTERN = Pattern.compile("[-+]?\\d{1,19}(L|l)");
	private static final Pattern DOUBLE_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(d|D)");
	private static final Pattern FLOAT_LITERAL_PATTERN = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+(f|F)");

	public static String getLiteralJavaType(String literal) {
		if (literal == null || literal.isEmpty()) {
			return null;
		}
		if (isStringLiteralSeparator(literal.charAt(0))) {
			return STRING_TYPE;
		} else if (literal.equals(TRUE)) {
			return BOOLEAN_TYPE;
		} else if (literal.equals(FALSE)) {
			return BOOLEAN_TYPE;
		} else if (literal.equals(NULL_TYPE)) {
			return NULL_TYPE;
		} else {
			char firstChar = literal.charAt(0);
			if (Character.isDigit(firstChar) || firstChar == '-' || firstChar == '+') {
				if (INTEGER_LITERAL_PATTERN.matcher(literal).matches()) {
					return INTEGER_TYPE;
				} else if (LONG_LITERAL_PATTERN.matcher(literal).matches()) {
					return LONG_TYPE;
				} else if (DOUBLE_LITERAL_PATTERN.matcher(literal).matches()) {
					return DOUBLE_TYPE;
				} else if (FLOAT_LITERAL_PATTERN.matcher(literal).matches()) {
					return FLOAT_TYPE;
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
