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

	private transient ResolvedJavaTypeInfo resolvedType;

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
	 * Determines whether the Javadoc must be loaded for this Java element.
	 * <p>
	 * The Javadoc should be loaded only if:
	 * <ul>
	 * <li>the Java element type has not been resolved yet</li>
	 * <li>and no documentation is currently available</li>
	 * </ul>
	 * </p>
	 *
	 * @return {@code true} if the Javadoc must be loaded, {@code false} otherwise.
	 */
	public boolean shouldLoadDocumentation() {
		if (isTypeResolved()) {
			return false;
		}
		return getDocumentation() == null;
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
			switch (c) {
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

	/**
	 * Indicates whether the Java element type has been resolved.
	 *
	 * @return {@code true} if the Java element type is resolved, {@code false}
	 *         otherwise.
	 */
	public final boolean isTypeResolved() {
		return resolvedType != null;
	}

	/**
	 * Returns the resolved Java type information for this element.
	 *
	 * @return the resolved Java type information, or {@code null} if the type has
	 *         not been resolved yet.
	 */
	public final ResolvedJavaTypeInfo getResolvedType() {
		return resolvedType;
	}

	/**
	 * Sets the resolved Java type information for this element.
	 * <p>
	 * Once the type is resolved, the element is considered fully typed and
	 * additional metadata (such as documentation) does not need to be loaded lazily
	 * anymore.
	 * </p>
	 *
	 * @param resolvedType the resolved Java type information to associate with this
	 *                     element, or {@code null} to clear the resolved state
	 */
	public void setResolvedType(ResolvedJavaTypeInfo resolvedType) {
		this.resolvedType = resolvedType;
	}
}
