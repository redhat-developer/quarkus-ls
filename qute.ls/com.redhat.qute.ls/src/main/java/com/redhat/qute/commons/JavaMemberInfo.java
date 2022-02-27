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
public abstract class JavaMemberInfo extends JavaElementInfo {

	protected static final String NO_VALUE = "~";

	private transient JavaTypeInfo javaType;

	/**
	 * Returns the owner Java type and null otherwise.
	 * 
	 * @return the owner Java type and null otherwise.
	 */
	public JavaTypeInfo getJavaTypeInfo() {
		return javaType;
	}

	/**
	 * Set the owner Java type.
	 * 
	 * @param javaType the owner Java type.
	 */
	public void setJavaType(JavaTypeInfo javaType) {
		this.javaType = javaType;
	}

	/**
	 * Returns the java source type and null otherwise.
	 * 
	 * @return the java source type and null otherwise.
	 */
	public String getSourceType() {
		JavaTypeInfo javaType = getJavaTypeInfo();
		return javaType != null ? javaType.getName() : null;
	}

	/**
	 * Returns the java source simple type and null otherwise.
	 * 
	 * @return the java source simple type and null otherwise.
	 */
	public String getSimpleSourceType() {
		return getSimpleType(getSourceType());
	}

	/**
	 * Resolve the element type which could use generic by using the given argument
	 * java type.
	 * 
	 * @param argType the argument Java type.
	 * 
	 * @return the element type which could use generic by using the given argument
	 *         java type.
	 */
	public String resolveJavaElementType(ResolvedJavaTypeInfo argType) {
		return getJavaElementType();
	}

}
