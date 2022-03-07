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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

	private transient JavaTypeInfo javaReturnType;

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
	@Override
	public String getSignature() {
		return super.getSignature();
	}

	/**
	 * Returns true if it's a virtual method and false otherwise.
	 * 
	 * @return true if it's a virtual method and false otherwise.
	 */
	public boolean isVirtual() {
		return false;
	}

	/**
	 * Returns the Java method signature with simple names.
	 *
	 * Example:
	 *
	 * <code>
	 * find(query : String, params : Map<String,Object>) : PanacheQuery<T>
	 * </code>
	 *
	 * @return the Java method signature.
	 */
	public String getSimpleSignature() {
		StringBuilder simpleSignatureBuilder = new StringBuilder();
		simpleSignatureBuilder.append(getMethodName());
		simpleSignatureBuilder.append("(");
		List<JavaParameterInfo> parameters = getParameters();
		StringJoiner commaJoiner = new StringJoiner(", ");
		for (JavaParameterInfo parameter : parameters) {
			commaJoiner.add(parameter.getSimpleParameter());
		}
		simpleSignatureBuilder.append(commaJoiner.toString());
		simpleSignatureBuilder.append(") : ");
		simpleSignatureBuilder.append(super.getSimpleType(getReturnType()));
		return simpleSignatureBuilder.toString();
	}

	/**
	 * Returns the method name.
	 * 
	 * @return the method name.
	 */
	public String getMethodName() {
		return getName();
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
	 * Returns the Java return type.
	 *
	 * @return the Java type parameter.
	 */
	public JavaTypeInfo getJavaReturnType() {
		if (javaReturnType == null) {
			javaReturnType = new JavaTypeInfo();
			javaReturnType.setSignature(getReturnType());
		}
		return javaReturnType;
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

	/**
	 * Returns the resolved type if return type has generic (ex : T,
	 * java.util.List<T>) by using the given java type argument.
	 * 
	 * @param baseType Java type of the base object.
	 * 
	 * @return the resolved type if return type has generic (ex : T,
	 *         java.util.List<T>) by using the given java type argument.
	 */
	@Override
	public String resolveJavaElementType(ResolvedJavaTypeInfo baseType) {
		return resolveReturnType(baseType, getJavaTypeInfo());
	}

	protected String resolveReturnType(ResolvedJavaTypeInfo baseType, JavaTypeInfo baseDeclType) {
		if (getReturnType() == null) {
			// void method
			return null;
		}

		if (baseType == null || baseDeclType == null) {
			// prevent from NPE
			return getReturnType();
		}

		JavaTypeInfo returnType = getJavaReturnType();
		if (!returnType.isGenericType()) {
			// return type has no generic:
			// - java.util.List<java.lang.String>
			// - java.lang.String
			return returnType.getSignature();
		}

		// return type has generic which must be resolved:
		// - java.util.List<T>
		// - T

		// Resolve each generic name
		// T = java.lang.String
		Map<String, String> resolvedGenericNames = new HashMap<>();
		List<JavaParameterInfo> genericsParameterType = baseDeclType.getTypeParameters();
		if (genericsParameterType.isEmpty()) {
			// ex : T
			resolvedGenericNames.put(baseDeclType.getName(), baseType.getSignature());
			if (baseType.isArray()) {
				// ex : T[]
				resolvedGenericNames.put(baseDeclType.getName().replace("[]", ""), baseType.getIterableOf());
			}
		} else {
			// ex : java.util .List<T>
			for (int i = 0; i < genericsParameterType.size(); i++) {
				JavaParameterInfo argParameter = genericsParameterType.get(i);
				JavaParameterInfo javaParameter = baseType.getTypeParameters().get(i);
				resolvedGenericNames.put(argParameter.getType(), javaParameter.getType());
			}
		}

		// resolve return type
		// java.lang.Iterable<T> -> java.lang.Iterable<org.acme.Item>
		List<JavaParameterInfo> returnTypeParameters = returnType.getTypeParameters();
		if (returnTypeParameters.isEmpty()) {
			// ex : T for the method get(index : int) T
			// returns the resolved type of T (ex : java.lang.String)
			return resolvedGenericNames.get(returnType.getName());
		}

		// ex : java.util.List<T> for the method take(index : int) java.util.List<T>
		// returns the resolved type of T (ex : java.util.List<java.lang.String>)
		StringBuilder resolved = new StringBuilder(returnType.getName());
		resolved.append('<');
		for (int j = 0; j < returnTypeParameters.size(); j++) {
			JavaParameterInfo p = returnTypeParameters.get(j);
			if (j > 0) {
				resolved.append(',');
			}
			resolved.append(resolvedGenericNames.get(p.getType()));
		}
		resolved.append('>');

		return resolved.toString();
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
