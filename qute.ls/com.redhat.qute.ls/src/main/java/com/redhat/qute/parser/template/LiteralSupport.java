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

	// Java primitive types
	private static final String BOOLEAN_PRIMITIVE_TYPE = "boolean";
	private static final String BYTE_PRIMITIVE_TYPE = "byte";
	private static final String CHAR_PRIMITIVE_TYPE = "char";
	private static final String DOUBLE_PRIMITIVE_TYPE = "double";
	private static final String FLOAT_PRIMITIVE_TYPE = "float";
	private static final String INT_PRIMITIVE_TYPE = "int";
	private static final String LONG_PRIMITIVE_TYPE = "long";

	// Java Object types
	private static final String NULL_TYPE = "null";
	private static final String STRING_TYPE = "java.lang.String";
	private static final String BOOLEAN_TYPE = "java.lang.Boolean";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String BYTE_TYPE = "java.lang.Byte";
	private static final String CHARACTER_TYPE = "java.lang.Character";
	private static final String DOUBLE_TYPE = "java.lang.Double";
	private static final String FLOAT_TYPE = "java.lang.Float";
	private static final String INTEGER_TYPE = "java.lang.Integer";
	private static final String LONG_TYPE = "java.lang.Long";

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

	/**
	 * Returns the Java object primitive type of the given primitive type and null
	 * otherwise.
	 * 
	 * @param primitiveType the primitive type
	 * 
	 * @return the Java object primitive type of the given primitive type and null
	 *         otherwise.
	 */
	public static String getPrimitiveObjectType(String primitiveType) {
		switch (primitiveType) {
		case BOOLEAN_PRIMITIVE_TYPE:
			return BOOLEAN_TYPE;
		case BYTE_PRIMITIVE_TYPE:
			return BYTE_TYPE;
		case CHAR_PRIMITIVE_TYPE:
			return CHARACTER_TYPE;
		case DOUBLE_PRIMITIVE_TYPE:
			return DOUBLE_TYPE;
		case FLOAT_PRIMITIVE_TYPE:
			return FLOAT_TYPE;
		case INT_PRIMITIVE_TYPE:
			return INTEGER_TYPE;
		case LONG_PRIMITIVE_TYPE:
			return LONG_TYPE;
		}
		return null;
	}

	/**
	 * Returns true if the given literal is 'null'and false otherwise.
	 * 
	 * @param javaType the Java type.
	 * 
	 * @return true if the given literal is 'null'and false otherwise.
	 */
	public static boolean isNull(String javaType) {
		return NULL_TYPE.equals(javaType);
	}

}
