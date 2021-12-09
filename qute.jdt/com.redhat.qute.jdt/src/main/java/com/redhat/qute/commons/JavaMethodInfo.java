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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Java method information.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaMethodInfo extends JavaMemberInfo {

	private transient String methodName;

	private transient String returnType;

	private transient String getterName;

	private transient List<JavaParameterInfo> parameters;

	/**
	 * Returns the Java method signature.
	 * 
	 * Example:
	 * 
	 * <code>
	 * find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>
	 * </code>
	 * 
	 * @return the Java method signature.
	 */
	public String getSignature() {
		return super.getSignature();
	}

	/**
	 * Returns the method name.
	 * 
	 * Example:
	 * 
	 * <code>
	 *  find
	 *  </code>
	 * 
	 * from the given signature:
	 * 
	 * <code>
	 * find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>
	 * </code>
	 * 
	 * @return the method name.
	 */
	@Override
	public String getName() {
		if (methodName != null) {
			return methodName;
		}
		// The method name is not computed, compute it from signature
		String signature = getSignature();
		int index = signature != null ? signature.indexOf('(') : -1;
		if (index != -1) {
			methodName = signature.substring(0, index);
		}
		return methodName;
	}
	
	/**
	 * Returns the method return Java type and null otherwise.
	 * 
	 * Example:
	 * 
	 * <code>
	 *  io.quarkus.hibernate.orm.panache.PanacheQuery<T>
	 *  </code>
	 * 
	 * from the given signature:
	 * 
	 * <code>
	 * find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>
	 * </code>
	 * 
	 * @return the method return Java type and null otherwise.
	 */
	public String getReturnType() {
		if (returnType == null) {
			// Compute return type from the signature
			String signature = getSignature();
			int index = signature.lastIndexOf(':');
			returnType = index != -1 ? signature.substring(index + 1, signature.length()).trim() : NO_VALUE;
		}
		return NO_VALUE.equals(returnType) ? null : returnType;
	}

	/**
	 * Returns the getter name of the method name.
	 * 
	 * @return the getter name of the method name.
	 */
	public String getGetterName() {
		if (getterName == null) {
			getterName = computeGetterName();
		}
		return NO_VALUE.equals(getterName) ? null : getterName;
	}

	private String computeGetterName() {
		if (hasParameters()) {
			return NO_VALUE;
		}
		String methodName = getName();
		int index = -1;
		if (methodName.startsWith("get")) {
			index = 3;
		} else if (methodName.startsWith("is")) {
			index = 2;
		}
		if (index == -1) {
			return NO_VALUE;
		}
		return (methodName.charAt(index) + "").toLowerCase() + methodName.substring(index + 1, methodName.length());
	}

	/**
	 * Returns true if the method have parameters and false otherwise.
	 * 
	 * @return true if the method have parameters and false otherwise.
	 */
	public boolean hasParameters() {
		String signature = getSignature();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		return end - start > 1;
	}

	/**
	 * Returns the Java method parameter at the given index and null otherwise.
	 * 
	 * @param index the method parameter index
	 * 
	 * @return the Java method parameter at the given index and null otherwise.
	 */
	public JavaParameterInfo getParameterAt(int index) {
		List<JavaParameterInfo> parameters = getParameters();
		return parameters.size() > index ? parameters.get(index) : null;
	}

	/**
	 * Returns the method parameters.
	 * 
	 * @return the method parameters.
	 */
	public List<JavaParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = parseParameters(getSignature());
		}
		return parameters;
	}

	private static List<JavaParameterInfo> parseParameters(String signature) {
		List<JavaParameterInfo> parameters = new ArrayList<>();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		// query : java.lang.String, params :
		// java.util.Map<java.lang.String,java.lang.Object>
		boolean paramTypeParsing = false;
		StringBuilder paramName = new StringBuilder();
		StringBuilder paramType = new StringBuilder();
		int daemon = 0;
		for (int i = start + 1; i < end; i++) {
			char c = signature.charAt(i);
			if (!paramTypeParsing) {
				// ex query :
				switch (c) {
				case ' ':
					// ignore space
					break;
				case ':':
					paramTypeParsing = true;
					break;
				default:
					paramName.append(c);
				}
			} else {
				// ex java.lang.String,
				switch (c) {
				case ' ':
					// ignore space
					break;
				case '<':
					daemon++;
					paramType.append(c);
					break;
				case '>':
					daemon--;
					paramType.append(c);
					break;
				case ',':
					if (daemon == 0) {
						parameters.add(new JavaParameterInfo(paramName.toString(), paramType.toString()));
						paramName.setLength(0);
						paramType.setLength(0);
						paramTypeParsing = false;
						daemon = 0;
					} else {
						paramType.append(c);
					}
					break;
				default:
					paramType.append(c);
				}
			}
		}
		if (paramName.length() > 0) {
			parameters.add(new JavaParameterInfo(paramName.toString(), paramType.toString()));
		}
		return parameters;
	}

	@Override
	public JavaElementKind getJavaElementKind() {
		return JavaElementKind.METHOD;
	}

	@Override
	public String getJavaElementType() {
		return getReturnType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("returnType", this.getReturnType());
		b.add("signature", this.getSignature());
		return b.toString();
	}
}