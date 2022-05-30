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

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Java method parameter or type parameter (generic) information.
 *
 * @author Angelo ZERR
 *
 */
public class JavaParameterInfo extends JavaElementInfo {

	private static final String VARARGS = "...";

	private final String name;

	private final String type;

	private transient JavaTypeInfo javaType;

	private String varArgType;

	public JavaParameterInfo(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the parameter name.
	 *
	 * @return the parameter name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the Java type parameter as String.
	 *
	 * @return the Java type parameter as String.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the Java type parameter as String.
	 *
	 * @return the Java type parameter as String.
	 */
	public String getVarArgType() {
		if (varArgType == null && isVarargs()) {
			varArgType = type.substring(0, type.length() - 3);
		}
		return varArgType;
	}

	/**
	 * Returns true if the Java type is a varargs (ex : java.lang.String...) and
	 * false otherwise.
	 * 
	 * @return true if the Java type is a varargs (ex : java.lang.String...) and
	 *         false otherwise.
	 */
	public boolean isVarargs() {
		return type.endsWith(VARARGS);
	}

	/**
	 * Returns the Java type parameter.
	 *
	 * @return the Java type parameter.
	 */
	public JavaTypeInfo getJavaType() {
		if (javaType == null) {
			javaType = new JavaTypeInfo();
			javaType.setSignature(getType());
		}
		return javaType;
	}

	/**
	 * Returns the Java parameter signature with a simple type.
	 *
	 * Example:
	 *
	 * <code>
	 * query : String
	 * </code>
	 *
	 * @return the Java method signature with simple type.
	 */
	public String getSimpleParameter() {
		StringBuilder paramBuilder = new StringBuilder();
		paramBuilder.append(name);
		paramBuilder.append(" : ");
		paramBuilder.append(getJavaElementSimpleType());
		return paramBuilder.toString();
	}

	@Override
	public String getJavaElementSimpleType() {
		if (isVarargs()) {
			String type = getVarArgType();
			return getSimpleType(type) + VARARGS;
		}
		return super.getJavaElementSimpleType();
	}

	@Override
	public JavaElementKind getJavaElementKind() {
		return JavaElementKind.PARAMETER;
	}

	@Override
	public String getJavaElementType() {
		return getType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("type", this.getType());
		b.add("varargs", this.isVarargs());
		b.add("signature", this.getSignature());
		return b.toString();
	}

}
