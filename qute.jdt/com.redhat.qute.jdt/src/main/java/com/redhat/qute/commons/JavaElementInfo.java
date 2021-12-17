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
package com.redhat.qute.commons;

import java.util.StringJoiner;

/**
 * Base class for Java element (Java types, methods, fields, parameters)
 *
 * @author Angelo ZERR
 *
 */
public abstract class JavaElementInfo {

	protected static final String NO_VALUE = "~";

	private String signature;

	private String documentation;

	/**
	 * Returns the Java element signature.
	 *
	 * @return the Java element signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Set the Java element signature.
	 *
	 * @param signature the Java element signature.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Returns the Java element documentation and null otherwise.
	 *
	 * @return the Java element documentation and null otherwise.
	 */
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * Set the Java element documentation.
	 *
	 * @param documentation the Java element documentation.
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	/**
	 * Returns the Java element name.
	 *
	 * @return the Java element name.
	 */
	public abstract String getName();

	/**
	 * Returns the Java element kind (type, method, field).
	 *
	 * @return the Java element kind (type, method, field).
	 */
	public abstract JavaElementKind getJavaElementKind();

	/**
	 * Returns the Java element type.
	 *
	 * @return the Java element type.
	 */
	public abstract String getJavaElementType();

	/**
	 * Returns the simple (without packages) element type and null otherwise.
	 *
	 * @return the simple (without packages) element type and null otherwise.
	 */
	public String getJavaElementSimpleType() {
		String type = getJavaElementType();
		return getSimpleType(type);
	}

	/**
	 * Returns the simple type of the given type. Example:
	 *
	 * <p>
	 * java.util.List<org.acme.Item>
	 * </p>
	 *
	 * becomes
	 *
	 * <p>
	 * List<Item>
	 * </p>
	 *
	 * @param type the Java type.
	 *
	 * @return the simple type of the given type.
	 */
	public static String getSimpleType(String type) {
		if (type == null) {
			return null;
		}
		int startBracketIndex = type.indexOf('<');
		if (startBracketIndex != -1) {
			int endBracketIndex = type.indexOf('>', startBracketIndex);
			// Main type
			StringBuilder simpleType = new StringBuilder(
					getSimpleTypeWithoutGeneric(type.substring(0, startBracketIndex)));
			// Generic type
			simpleType.append('<');
			String[] generics = type.substring(startBracketIndex + 1, endBracketIndex).split(",");
			StringJoiner commaJoiner = new StringJoiner(",");
			for (String generic : generics) {
				commaJoiner.add(getSimpleTypeWithoutGeneric(generic));
			}
			simpleType.append(commaJoiner.toString());
			simpleType.append('>');

			return simpleType.toString();
		}
		return getSimpleTypeWithoutGeneric(type);
	}

	private static String getSimpleTypeWithoutGeneric(String type) {
		int index = type.lastIndexOf('.');
		return index != -1 ? type.substring(index + 1, type.length()) : type;
	}

}
