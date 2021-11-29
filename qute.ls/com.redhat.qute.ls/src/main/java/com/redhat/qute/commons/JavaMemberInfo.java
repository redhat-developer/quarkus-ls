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

	private transient ResolvedJavaTypeInfo resolvedType;

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
		return resolvedType != null ? resolvedType.getSignature() : null;
	}

}
