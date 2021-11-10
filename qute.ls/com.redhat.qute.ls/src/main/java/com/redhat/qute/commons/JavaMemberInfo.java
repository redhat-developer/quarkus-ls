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
 * Abstract class for Java member (method, field).
 * 
 * @author Angelo ZERR
 *
 */
public abstract class JavaMemberInfo {

	public static enum JavaMemberKind {
		FIELD, METHOD;
	}

	private String name;

	private String description;

	private transient ResolvedJavaTypeInfo resolvedType;

	/**
	 * Returns the member name.
	 * 
	 * @return the member name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the member name.
	 * 
	 * @param name the member name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the member description and null otherwise.
	 * 
	 * @return the member description and null otherwise.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the member description.
	 * 
	 * @param description the member description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the resolved type and null otherwise.
	 * 
	 * @return the resolved type and null otherwise.
	 */
	public ResolvedJavaTypeInfo getResolvedType() {
		return resolvedType;
	}

	/**
	 * Set the resolved type.
	 * 
	 * @param resolvedType the resolved type
	 */
	public void setResolvedType(ResolvedJavaTypeInfo resolvedType) {
		this.resolvedType = resolvedType;
	}

	/**
	 * Returns the java source type and null otherwise.
	 * 
	 * @return the java source type and null otherwise.
	 */
	public String getSourceType() {
		return resolvedType != null ? resolvedType.getClassName() : null;
	}

	/**
	 * Returns the simple (without packages) member type and null otherwise.
	 * 
	 * @return the simple (without packages) member type and null otherwise.
	 */
	public String getMemberSimpleType() {
		String type = getMemberType();
		if (type == null) {
			return null;
		}

		int startBracketIndex = type.indexOf('<');
		if (startBracketIndex != -1) {
			int endBracketIndex = type.indexOf('>', startBracketIndex);
			String generic = getSimpleType(type.substring(startBracketIndex + 1, endBracketIndex));
			String mainType = getSimpleType(type.substring(0, startBracketIndex));
			return mainType + '<' + generic + '>';
		}
		return getSimpleType(type);
	}

	private static String getSimpleType(String type) {
		int index = type.lastIndexOf('.');
		return index != -1 ? type.substring(index + 1, type.length()) : type;
	}

	/**
	 * Returns the Java member kind.
	 * 
	 * @return the Java member kind.
	 */
	public abstract JavaMemberKind getKind();

	public abstract String getMemberType();

}
