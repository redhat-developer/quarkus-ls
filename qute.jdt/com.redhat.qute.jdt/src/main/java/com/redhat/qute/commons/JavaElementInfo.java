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
		if (type == null || type.indexOf('.') == -1) {
			// ex: List
			return type;
		}
		int lastIndex = 0;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < type.length(); i++) {
			char c = type.charAt(i);
			switch(c) {
			case '.':
				lastIndex = i;
				break;
			case '<':
			case ',':
				result.append(type.substring(lastIndex + 1, i + 1));
				lastIndex = i;
				break;
			}			
		}
		if (lastIndex != type.length()) {
			result.append(type.substring(lastIndex + 1, type.length()));
		}
		return result.toString();
	}

}
