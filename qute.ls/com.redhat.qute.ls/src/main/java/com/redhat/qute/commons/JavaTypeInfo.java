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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

	public static final List<String> PRIMITIVE_TYPES = Arrays.asList("boolean", "byte", "char", "double", "float",
			"int", "long");

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
	public void setJavaTypeKind(JavaTypeKind kind) {
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
	
	public Map<String, InvalidMethodReason> getInvalidMethods() {
		return invalidMethods;
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
		int end = signature.lastIndexOf('>');
		// ex : java.util.Map<K,V> will return an array of K,V
		StringBuilder paramName = new StringBuilder();
		boolean ignore = false;
		for (int i = start + 1; i < end; i++) {
			char c = signature.charAt(i);
			// ex query :
			switch (c) {
			case '<':
				ignore = true;
				paramName.append(c);
				break;
			case '>':
				ignore = false;
				paramName.append(c);
				break;
			case ',':
				if (ignore) {
					paramName.append(c);
				} else {
					parameters.add(new JavaParameterInfo(null, paramName.toString()));
					paramName.setLength(0);
				}
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

	/**
	 * Returns true if the given type is a generic type and false otherwise.
	 *
	 * @param type the Java type.
	 *
	 * @return true if the given type is a generic type and false otherwise.
	 */
	private static boolean isTypeParameterName(String type) {
		int index = type.indexOf('[');
		if (index != -1) {
			// ex : byte[] -> byte
			return isTypeParameterName(type.substring(0, index));
		}
		if (type.indexOf('.') == -1 && !PRIMITIVE_TYPES.contains(type)) {
			// ex : T
			return true;
		}
		// int, org.acme.Item
		return false;
	}

	/**
	 * Returns true if the Java type is an array (ex : java.lang.String[]) and false
	 * otherwise.
	 *
	 * @return true if the Java type is an array (ex : java.lang.String[]) and false
	 *         otherwise.
	 */
	public boolean isArray() {
		return getName().endsWith("[]");
	}

	/**
	 * Returns the result of apply generic type invocation for the given
	 * <code>javaType</code> with the given <code>genericMap</code>.
	 * 
	 * @param javaType   the Java type.
	 * @param genericMap the generic Map.
	 * 
	 * @return the result of apply generic type invocation for the given
	 *         <code>javaType</code> with the given <code>genericMap</code>.
	 */
	public static String applyGenericTypeInvocation(JavaTypeInfo javaType, Map<String, String> genericMap) {
		StringBuilder result = new StringBuilder();
		applyGenericTypeInvocation(javaType, genericMap, result);
		return result.toString();
	}

	/**
	 * Apply generic type invocation for the JavaType with the given given
	 * <code>genericMap</code>.
	 * 
	 * @param javaType   the Java type.
	 * @param genericMap the generic Map.
	 * @param result     the result.
	 */
	public static void applyGenericTypeInvocation(JavaTypeInfo javaType, Map<String, String> genericMap,
			StringBuilder result) {
		List<JavaParameterInfo> parameters = javaType.getTypeParameters();
		if (!parameters.isEmpty()) {
			// - org.acme.A<B,C,D>
			// - java.util.Collection<E>
			result.append(javaType.getName());
			result.append("<");
			for (int i = 0; i < parameters.size(); i++) {
				if (i > 0) {
					result.append(",");
				}
				JavaParameterInfo parameter = parameters.get(i);
				String type = parameter.getType();
				applyGenericTypeInvocation(type, genericMap, result);
			}
			result.append(">");
		} else {
			// - int
			// - T
			applyGenericTypeInvocation(javaType.getName(), genericMap, result);
		}
	}

	/**
	 * Returns the result of apply generic type invocation for the given
	 * <code>javaType</code> with the given <code>genericMap</code>.
	 * 
	 * @param javaType   the Java type.
	 * @param genericMap the generic Map.
	 * 
	 * @return the result of apply generic type invocation for the given
	 *         <code>javaType</code> with the given <code>genericMap</code>.
	 */
	public static String applyGenericTypeInvocation(String javaType, Map<String, String> genericMap) {
		StringBuilder result = new StringBuilder();
		applyGenericTypeInvocation(javaType, genericMap, result);
		return result.toString();
	}

	/**
	 * Apply generic type invocation for the given <code>javaType</code> with the
	 * given given <code>genericMap</code>.
	 * 
	 * @param javaType   the Java type.
	 * @param genericMap the generic Map.
	 * @param result     the result.
	 */
	public static void applyGenericTypeInvocation(String javaType, Map<String, String> genericMap,
			StringBuilder result) {
		String paramType = genericMap.getOrDefault(javaType, javaType);
		if (containsGenericParameter(paramType)) {
			JavaTypeInfo subType = new JavaTypeInfo();
			subType.setSignature(paramType);
			JavaTypeInfo.applyGenericTypeInvocation(subType, genericMap, result);
		} else {
			result.append(paramType);
		}
	}

	/**
	 * Returns true if the given <code>javaType</code> defines some generic type
	 * parameters (ex : java.util.List<E>) and false otherwise.
	 * 
	 * @param javaType the Java type.
	 * 
	 * @return true if the given <code>javaType</code> defines some generic type
	 *         parameters (ex : java.util.List<E>) and false otherwise.
	 */
	private static boolean containsGenericParameter(String javaType) {
		return javaType != null && javaType.indexOf('<') != -1;
	}

	/**
	 * Returns a Map which contains the generic name of the Java type (ex : <T>) and
	 * the concrete type <code>javaTypeName</code> (ex : <java.lang.String>).
	 * 
	 * <code>
	 *  ResolvedJavaTypeInfo map = new ResolvedJavaTypeInfo();
	 *  map.setSignature("java.util.Map<K,V>"); 
	 *  
	 *  // Here the createGenericMap will return a map like:
	 *  // -  K = java.lang.String
	 *  // -  V = java.lang.Integer
	 *  map.createGenericMap(java.util.Map<java.lang.String,java.lang.Integer>);
	 * </code>
	 * 
	 * @param javaType the concrete java type (ex :
	 *                 Map<java.lang.String,org.acme.Item>).
	 * 
	 * @return
	 */
	public Map<String, String> createGenericMap(String javaType) {
		if (javaType.indexOf('<') == -1) {
			return null;
		}
		JavaTypeInfo typeInfo = new JavaTypeInfo();
		typeInfo.setSignature(javaType);
		Map<String, String> generics = new LinkedHashMap<>();
		List<JavaParameterInfo> typeParameters = typeInfo.getTypeParameters();
		int size = Math.min(typeParameters.size(), getTypeParameters().size());
		for (int i = 0; i < size; i++) {
			generics.put(getTypeParameters().get(i).getType(), typeParameters.get(i).getType());
		}
		return generics;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("signature", this.getSignature());
		b.add("array", this.isArray());
		b.add("genericType", this.isGenericType());
		return b.toString();
	}

}
