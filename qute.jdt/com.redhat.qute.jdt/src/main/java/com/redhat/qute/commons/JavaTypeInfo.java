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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Java type information for:
 * 
 * <ul>
 * <li>class name</li>
 * <li>interface name</li>
 * <li>package name</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTypeInfo extends JavaElementInfo {

	private JavaTypeKind typeKind;

	private List<JavaParameterInfo> parameters;

	private String name;

	private Map<String /* invalid method name */, InvalidMethodReason> invalidMethods;

	/**
	 * Returns the fully qualified name of the Java type with type parameters.
	 * 
	 * Example:
	 * 
	 * <code>
	 * 	java.util.List<E>
	 * </code>
	 * 
	 * @return the fully qualified name of the Java type with type parameters.
	 */
	@Override
	public String getSignature() {
		return super.getSignature();
	}

	/**
	 * Returns the fully qualified name of the Java type without type parameters.
	 * 
	 * Example:
	 * 
	 * <code>
	 * 	java.util.List
	 * </code>
	 * 
	 * @return the fully qualified name of the Java type without type parameters.
	 */
	@Override
	public String getName() {
		getTypeParameters();
		return name;
	}

	@Override
	public String getJavaElementType() {
		return getSignature();
	}

	@Override
	public JavaElementKind getJavaElementKind() {
		return JavaElementKind.TYPE;
	}

	/**
	 * Returns the Java type kind (class, interface, package)..
	 * 
	 * @return the Java type kind (class, interface, package)..
	 */
	public JavaTypeKind getJavaTypeKind() {
		return typeKind;
	}

	/**
	 * Set the Java type kind (class, interface, package).
	 * 
	 * @param kind the Java type kind (class, interface, package).
	 */
	public void setKind(JavaTypeKind kind) {
		this.typeKind = kind;
	}

	/**
	 * Returns the reason of the invalid method of the given method name and null
	 * otherwise.
	 * 
	 * @param methodName the method name to check.
	 * 
	 * @return the reason of the invalid method of the given method name and null
	 *         otherwise.
	 */
	public InvalidMethodReason getInvalidMethodReason(String methodName) {
		return invalidMethods != null ? invalidMethods.get(methodName) : null;
	}

	/**
	 * Set the invalid method reason for the given method name.
	 * 
	 * @param methodName the method name.
	 * 
	 * @param reason     the invalid method reason.
	 */
	public void setInvalidMethod(String methodName, InvalidMethodReason reason) {
		if (this.invalidMethods == null) {
			setInvalidMethods(new HashMap<>());
		}
		this.invalidMethods.put(methodName, reason);
	}

	/**
	 * Set the invalid methods map.
	 * 
	 * @param invalidMethods the invalid methods map.
	 */
	public void setInvalidMethods(Map<String /* method name */, InvalidMethodReason> invalidMethods) {
		this.invalidMethods = invalidMethods;
	}

	/**
	 * Returns the java type parameters.
	 * 
	 * Example:
	 * 
	 * <ul>
	 * <li>E for java.util.List<E></li>
	 * <li>[K,V] for java.util.Map<K,V></li>
	 * </ul>
	 * 
	 * 
	 * @return the java type parameters.
	 */
	public List<JavaParameterInfo> getTypeParameters() {
		if (parameters == null) {
			String signature = super.getSignature();
			int index = signature.indexOf('<');
			if (index == -1) {
				name = signature;
				parameters = Collections.emptyList();
			} else {
				name = signature.substring(0, index);
				parameters = parseTypeParameters(signature, index);
			}
		}
		return parameters;
	}

	private static List<JavaParameterInfo> parseTypeParameters(String signature, int start) {
		List<JavaParameterInfo> parameters = new ArrayList<>();
		int end = signature.indexOf('>', start - 1);
		// ex : java.util.Map<K,V> will return an array of K,V
		StringBuilder paramName = new StringBuilder();
		for (int i = start + 1; i < end; i++) {
			char c = signature.charAt(i);
			// ex query :
			switch (c) {
			case ',':
				parameters.add(new JavaParameterInfo(null, paramName.toString()));
				paramName.setLength(0);
				break;
			default:
				paramName.append(c);
			}
		}
		if (paramName.length() > 0) {
			parameters.add(new JavaParameterInfo(null, paramName.toString()));
		}
		return parameters;
	}

	/**
	 * Returns true if the java type is a generic type and false otherwise.
	 * 
	 * Returns true for :
	 * 
	 * <ul>
	 * <li>T</li>
	 * <li>java.util.List<T></li>
	 * </ul>
	 * 
	 * @return true if the java type is a generic type and false otherwise.
	 */
	public boolean isGenericType() {
		if (isSingleGenericType()) {
			return true;
		}

		// ex : java.util.List<org.acme.Item>
		// ex : java.util.List<T>
		for (JavaParameterInfo parameter : getTypeParameters()) {
			if (isTypeParameterName(parameter.getType())) {
				// ex : T
				return true;
			}
		}
		return false;
	}

	public boolean isSingleGenericType() {
		if (getTypeParameters().isEmpty()) {
			// ex : java.lang.String
			// ex : T
			if (isTypeParameterName(getName())) {
				// ex : T
				return true;
			}			
		}
		return false;
	}

	private static boolean isTypeParameterName(String type) {
		return type.indexOf('.') == -1 && !"boolean".equals(type) && !"byte".equals(type) && !"double".equals(type)
				&& !"float".equals(type) && !"int".equals(type) && !"long".equals(type);
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("signature", this.getSignature());
		b.add("genericType", this.isGenericType());
		return b.toString();
	}

}
