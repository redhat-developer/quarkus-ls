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

	private static final String NO_VALUE = "~";

	private String signature;

	private String returnType;

	private String getterName;

	private List<JavaMethodParameterInfo> parameters;

	/**
	 * Returns the Java method signature.
	 * 
	 * @return
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Set the Java method signature.
	 * 
	 * @param signature the Java method signature.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

	/**
	 * Returns the method return Java type and null otherwise.
	 * 
	 * @return the method return Java type and null otherwise.
	 */
	public String getReturnType() {
		if (returnType == null) {
			String signature = getSignature();
			int index = signature.lastIndexOf(':');
			returnType = index != -1 ? signature.substring(index + 1, signature.length()).trim() : NO_VALUE;
		}
		return NO_VALUE.equals(returnType) ? null : returnType;
	}

	@Override
	public String getName() {
		String name = super.getName();
		if (name != null) {
			return name;
		}
		String signature = getSignature();
		int index = signature != null ? signature.indexOf('(') : -1;
		if (index != -1) {
			super.setName(signature.substring(0, index));
		}
		return super.getName();
	}

	/**
	 * Returns the getter name.
	 * 
	 * @return the getter name.
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
	 * Returns the Java parameter at the given index and null otherwise.
	 * 
	 * @param index parameter index
	 * @return the Java parameter at the given index and null otherwise.
	 */
	public JavaMethodParameterInfo getParameterAt(int index) {
		List<JavaMethodParameterInfo> parameters = getParameters();
		return parameters.size() > index ? parameters.get(index) : null;
	}

	/**
	 * Returns the method parameters.
	 * 
	 * @return the method parameters.
	 */
	public List<JavaMethodParameterInfo> getParameters() {
		if (parameters == null) {
			parameters = parseParameters(signature);
		}
		return parameters;
	}

	private static List<JavaMethodParameterInfo> parseParameters(String signature) {
		List<JavaMethodParameterInfo> parameters = new ArrayList<>();
		int start = signature.indexOf('(');
		int end = signature.indexOf(')', start - 1);
		String content = signature.substring(start + 1, end);
		// query : java.lang.String, params :
		// java.util.Map<java.lang.String,java.lang.Object>
		boolean paramTypeParsing = false;
		StringBuilder paramName = new StringBuilder();
		StringBuilder paramType = new StringBuilder();
		int daemon = 0;
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
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
						parameters.add(new JavaMethodParameterInfo(paramName.toString(), paramType.toString()));
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
			parameters.add(new JavaMethodParameterInfo(paramName.toString(), paramType.toString()));
		}
		return parameters;
	}

	@Override
	public JavaMemberKind getKind() {
		return JavaMemberKind.METHOD;
	}

	@Override
	public String getMemberType() {
		return getReturnType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("signature", this.signature);
		return b.toString();
	}
}
