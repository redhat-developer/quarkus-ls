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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.commons.jaxrs.JaxRsMethodKind;
import com.redhat.qute.commons.jaxrs.RestParam;

/**
 * Java method information.
 *
 * @author Angelo ZERR
 *
 */
public class JavaMethodInfo extends JavaMemberInfo {

	private static final String GET_PREFIX = "get";

	private static final String IS_PREFIX = "is";

	private JaxRsMethodKind jaxRsMethodKind;

	private Map<String, RestParam> restParameters;

	private transient String methodName;

	private String returnType;

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
	 * Returns the JAX RS method kind and null otherwise.
	 * 
	 * @return the JAX RS method kind and null otherwise
	 */
	public JaxRsMethodKind getJaxRsMethodKind() {
		return jaxRsMethodKind;
	}

	/**
	 * Set the JAX RS method kind.
	 * 
	 * @param jaxRsMethodKind the JAX RS method kind
	 */
	public void setJaxRsMethodKind(JaxRsMethodKind jaxRsMethodKind) {
		this.jaxRsMethodKind = jaxRsMethodKind;
	}

	/**
	 * Set the Rest parameters information.
	 * 
	 * @param restParameters the Rest parameters information.
	 */
	public void setRestParameters(Map<String, RestParam> restParameters) {
		this.restParameters = restParameters;
	}

	/**
	 * Returns the Rest parameters information.
	 * 
	 * @return the Rest parameters information.
	 */
	public Collection<RestParam> getRestParameters() {
		return restParameters != null ? restParameters.values() : Collections.emptyList();
	}

	/**
	 * Returns the rest parameter information for the given parameter name and null
	 * otherwise.
	 * 
	 * @param name the parameter name.
	 * 
	 * @return the rest parameter information for the given parameter name and null
	 *         otherwise.
	 */
	public RestParam getRestParameter(String name) {
		return restParameters != null ? restParameters.get(name) : null;
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

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Returns true if the method is a void method and false otherwise.
	 * 
	 * @return true if the method is a void method and false otherwise.
	 */
	public boolean isVoidMethod() {
		return "void".equals(getReturnType());
	}

	/**
	 * Returns the Java return type.
	 *
	 * @return the Java type parameter.
	 */
	public JavaTypeInfo getJavaReturnType() {
		if (getReturnType() == null) {
			return null;
		}
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
		if (GET_PREFIX.equals(methodName) || IS_PREFIX.equals(methodName)) {
			return NO_VALUE;
		}
		int index = -1;
		if (methodName.startsWith(GET_PREFIX)) {
			index = 3;
		} else if (methodName.startsWith(IS_PREFIX)) {
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

	/**
	 * Returns the applicable parameters of the method.
	 * 
	 * <p>
	 * By default it returns the same list that getParameters(). This method is
	 * override for method value resolver to returns the applicable parameters.
	 * </p>
	 * 
	 * @return the applicable parameters of the method.
	 */
	public List<JavaParameterInfo> getApplicableParameters() {
		return getParameters();
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
			if (genericsParameterType.size() == baseType.getTypeParameters().size()) {
				for (int i = 0; i < genericsParameterType.size(); i++) {
					JavaParameterInfo argParameter = genericsParameterType.get(i);
					JavaParameterInfo javaParameter = baseType.getTypeParameters().get(i);
					resolvedGenericNames.put(argParameter.getType(), javaParameter.getType());
				}
			} else if (genericsParameterType.size() == 1 && baseType.getIterableOf() != null) {
				// ex: baseTypeDecl = java.lang.Iterable<T>
				// baseType= io.quarkiverse.roq.frontmatter.runtime.model.RoqCollection
				// where RoqCollection extends
				// java.util.ArrayList<io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage>
				// which is an
				// iterableOf=io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage
				resolvedGenericNames.put(genericsParameterType.get(0).getType(), baseType.getIterableOf());
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

	/**
	 * Returns a new method with the generic type information applied to the
	 * signature.
	 * 
	 * <code>
	 *    get(key : K) : V
	 * </code>
	 * 
	 * becomes:
	 * 
	 * <code>
	 *    get(key : java.lang.String) : java.lang.Integer
	 * </code>
	 * 
	 * where 'K' is filled in the generic Map with the 'java.lang.String' value and
	 * 'V' is filled in the generic Map with the 'java.lang.Integer' value.
	 * 
	 * @param method     the Java method.
	 * @param genericMap the generic Map
	 * 
	 * @return a new method with the generic type information applied to the
	 *         signature.
	 */
	public static JavaMethodInfo applyGenericTypeInvocation(JavaMethodInfo method, Map<String, String> genericMap) {
		JavaMethodInfo newMethod = new JavaMethodInfo();
		StringBuilder newSignature = new StringBuilder(method.getMethodName());
		newSignature.append("(");
		List<JavaParameterInfo> parameters = method.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			JavaParameterInfo parameter = parameters.get(i);
			if (i > 0) {
				newSignature.append(", ");
			}
			newSignature.append(parameter.getName());
			newSignature.append(" : ");
			JavaTypeInfo.applyGenericTypeInvocation(parameter.getJavaType(), genericMap, newSignature);
		}
		newSignature.append(")");
		JavaTypeInfo returnType = method.getJavaReturnType();
		if (returnType != null) {
			newSignature.append(" : ");
			JavaTypeInfo.applyGenericTypeInvocation(returnType, genericMap, newSignature);
		}
		newMethod.setSignature(newSignature.toString());
		return newMethod;
	}

	/**
	 * Resolves the return type of this method by inferring generic type variables
	 * from the provided resolved argument types.
	 *
	 * Examples: - get(arg : T) : T + [org.acme.Item] → org.acme.Item - get(arg :
	 * List<T>) : T + [java.util.List<org.acme.Item>] → org.acme.Item - find(clazz :
	 * Class<T>) : T + [java.lang.Class<org.acme.Item>] → org.acme.Item - put(key :
	 * K, val : V) : Map<K,V>+ [java.lang.String, org.acme.Item] →
	 * java.util.Map<java.lang.String,org.acme.Item>
	 *
	 * @param argumentTypes the resolved argument types passed at call site
	 * @return the resolved return type, null if void, raw return type if resolution
	 *         fails
	 */
	public String resolveReturnType(List<ResolvedJavaTypeInfo> argumentTypes, JavaTypeResolver resolver) {
		String rawReturn = getReturnType();
		if (rawReturn == null || isVoidMethod()) {
			// void or no return type → returned as-is
			return rawReturn;
		}

		JavaTypeInfo returnType = getJavaReturnType();
		if (!returnType.isGenericType()) {
			// void, int, java.lang.String, java.util.List<java.lang.String> → returned
			// as-is
			return rawReturn;
		}

		// Build the generic map by matching declared parameters against provided
		// arguments
		Map<String, String> genericMap = new HashMap<>();
		List<JavaParameterInfo> declaredParams = getParameters();
		for (int i = 0; i < declaredParams.size() && i < argumentTypes.size(); i++) {
			JavaTypeInfo declared = declaredParams.get(i).getJavaType();
			ResolvedJavaTypeInfo argument = argumentTypes.get(i);
			inferGenericMap(declared, argument, resolver, genericMap);
		}

		// Fill unresolved generic variables with java.lang.Object
		for (JavaParameterInfo returnTypeParam : returnType.getTypeParameters()) {
			genericMap.putIfAbsent(returnTypeParam.getType(), "java.lang.Object");
		}
		// Handle the case where return type itself is a single generic variable e.g. T
		if (returnType.isSingleGenericType() && !genericMap.containsKey(returnType.getName())) {
			genericMap.putIfAbsent(returnType.getName(), "java.lang.Object");
		}

		if (genericMap.isEmpty()) {
			return rawReturn;
		}

		// Apply the resolved generic map on the return type
		return JavaTypeInfo.applyGenericTypeInvocation(returnType, genericMap);
	}

	/**
	 * Attempts to infer generic mappings by searching through the extended types hierarchy.
	 *
	 * @param declared   the declared parameter type
	 * @param argument   the argument type (which has no type parameters itself)
	 * @param resolver   the type resolver
	 * @param genericMap the map to populate
	 * @return true if a match was found and inference succeeded
	 */
	private static boolean inferFromExtendedTypes(JavaTypeInfo declared, ResolvedJavaTypeInfo argument,
			JavaTypeResolver resolver, Map<String, String> genericMap) {
		List<String> extendedTypes = argument.getExtendedTypes();
		if (extendedTypes == null || extendedTypes.isEmpty()) {
			return false;
		}

		String declaredBaseName = declared.getName();

		for (String extendedType : extendedTypes) {
			// Resolve the extended type
			ResolvedJavaTypeInfo extendedTypeInfo = resolver.resolveJavaTypeSync(extendedType);
			if (extendedTypeInfo == null) {
				continue;
			}

			// Check if this extended type matches the declared type by base name
			// Extract base name without generics: java.util.ArrayList<Item> -> java.util.ArrayList
			JavaTypeInfo extendedTypeAsJavaType = new JavaTypeInfo();
			extendedTypeAsJavaType.setSignature(extendedType);
			String extendedBaseName = extendedTypeAsJavaType.getName();
			if (extendedBaseName.equals(declaredBaseName)) {
				// Direct match! Infer from this extended type
				inferGenericMap(declared, extendedTypeInfo, resolver, genericMap);
				return true;
			} else {
				// No direct match, search recursively in this extended type's hierarchy
				if (inferFromExtendedTypes(declared, extendedTypeInfo, resolver, genericMap)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Recursively infers generic variable mappings by comparing a declared
	 * (possibly generic) parameter type against a concrete resolved argument type.
	 *
	 * Examples: declared=T, argument=org.acme.Item → {T: org.acme.Item}
	 * declared=List<T>, argument=java.util.List<org.acme.Item>→ {T: org.acme.Item}
	 * declared=Class<T>, argument=java.lang.Class<org.acme.Item>→{T: org.acme.Item}
	 *
	 * @param declared   the declared parameter type (may contain generic variables)
	 * @param argument   the concrete resolved type provided at call site
	 * @param resolver
	 * @param genericMap the map to populate with resolved generic variables
	 */
	private static void inferGenericMap(JavaTypeInfo declared, ResolvedJavaTypeInfo argument, JavaTypeResolver resolver,
			Map<String, String> genericMap) {
		if (argument == null) {
			return;
		}
		if (declared.isSingleGenericType()) {
			// declared = T → T: org.acme.Item
			genericMap.put(declared.getName(), argument.getSignature());
			if (declared.isArray() && argument.isArray()) {
				genericMap.put(declared.getIterableOf(), argument.getIterableOf());
			}
			return;
		}

		List<JavaParameterInfo> argumentTypeParams = argument.getTypeParameters();
		if (argumentTypeParams.isEmpty()) {
			// args has no generic, search in extended types
			// Example: argument=org.acme.Items (no type params)
			// with extendedTypes=[java.util.List<org.acme.Item>]
			// declared=java.util.List<T>
			// We need to find the matching extended type and infer from it
			List<String> extendedTypes = argument.getExtendedTypes();
			if (extendedTypes != null && !extendedTypes.isEmpty() && resolver != null) {
				// Try to find a matching extended type in the hierarchy
				if (inferFromExtendedTypes(declared, argument, resolver, genericMap)) {
					return;
				}
			}
		}

		// declared has type parameters: e.g. List<T>, Map<K,V>, Class<T>
		// recurse positionally: List<T> vs List<org.acme.Item> → T=org.acme.Item
		List<JavaParameterInfo> declaredTypeParams = declared.getTypeParameters();

		for (int i = 0; i < declaredTypeParams.size() && i < argumentTypeParams.size(); i++) {
			JavaTypeInfo declaredInner = declaredTypeParams.get(i).getJavaType();
			// wrap the argument inner type in a lightweight ResolvedJavaTypeInfo
			// getTypeParameters() works from signature alone (see JavaTypeInfo)
			ResolvedJavaTypeInfo argumentInner = new ResolvedJavaTypeInfo();
			argumentInner.setSignature(argumentTypeParams.get(i).getType());
			inferGenericMap(declaredInner, argumentInner, resolver, genericMap);
		}
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
