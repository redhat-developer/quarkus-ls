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

import java.util.Collections;
import java.util.List;

/**
 * Resolved Java type information.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvedJavaTypeInfo extends JavaTypeInfo {

	private List<String> extendedTypes;

	private List<JavaFieldInfo> fields;

	private List<JavaMethodInfo> methods;

	private String iterableType;

	private String iterableOf;

	private Boolean isIterable;

	/**
	 * Returns list of extended types.
	 * 
	 * @return list of extended types.
	 */
	public List<String> getExtendedTypes() {
		return extendedTypes;
	}

	/**
	 * Set list of extended types.
	 * 
	 * @param extendedTypes list of extended types.
	 */
	public void setExtendedTypes(List<String> extendedTypes) {
		this.extendedTypes = extendedTypes;
	}

	/**
	 * Returns member fields.
	 * 
	 * @return member fields.
	 */
	public List<JavaFieldInfo> getFields() {
		return fields != null ? fields : Collections.emptyList();
	}

	/**
	 * Set member fields.
	 * 
	 * @param fields member fields.
	 */
	public void setFields(List<JavaFieldInfo> fields) {
		this.fields = fields;
	}

	/**
	 * Return member methods.
	 * 
	 * @return member methods.
	 */
	public List<JavaMethodInfo> getMethods() {
		return methods != null ? methods : Collections.emptyList();
	}

	/**
	 * Set member methods.
	 * 
	 * @param methods member methods.
	 */
	public void setMethods(List<JavaMethodInfo> methods) {
		this.methods = methods;
	}

	/**
	 * Returns iterable type and null otherwise.
	 * 
	 * @return iterable type and null otherwise.
	 */
	public String getIterableType() {
		return iterableType;
	}

	/**
	 * Set iterable type.
	 * 
	 * @param iterableType iterable type.
	 */
	public void setIterableType(String iterableType) {
		this.iterableType = iterableType;
	}

	/**
	 * Returns iterable of and null otherwise.
	 * 
	 * @return iterable of and null otherwise.
	 */
	public void setIterableOf(String iterableOf) {
		this.iterableOf = iterableOf;
	}

	/**
	 * Returns iterable of.
	 * 
	 * @return iterable of.
	 */
	public String getIterableOf() {
		return iterableOf;
	}

	/**
	 * Returns true if the Java type is iterable (ex :
	 * java.util.List<org.acme.item>) and false otherwise.
	 * 
	 * @return true if the Java type is iterable and false otherwise.
	 */
	public boolean isIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		isIterable = computeIsIterable();
		return isIterable.booleanValue();
	}

	private synchronized boolean computeIsIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		if (iterableOf != null) {
			return true;
		}
		boolean iterable = getSignature().equals("java.lang.Iterable");
		if (!iterable && extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				if ("Iterable".equals(extendedType) || extendedType.equals("java.lang.Iterable")) {
					iterable = true;
					break;
				}
			}
		}

		if (iterable) {
			this.iterableOf = "java.lang.Object";
			this.iterableType = getSignature();
		}
		return iterable;
	}

	/**
	 * Returns the member retrieved by the given property and null otherwise.
	 * 
	 * @param property the property
	 * @return the member retrieved by the given property and null otherwise.
	 */
	public JavaMemberInfo findMember(String property) {
		JavaFieldInfo fieldInfo = findField(property);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(property);
	}

	/**
	 * Returns the member field retrieved by the given name and null otherwise.
	 * 
	 * @param fieldName the field name
	 * 
	 * @return the member field retrieved by the given property and null otherwise.
	 */
	public JavaFieldInfo findField(String fieldName) {
		if (fields == null || fields.isEmpty() || isEmpty(fieldName)) {
			return null;
		}
		for (JavaFieldInfo field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the member method retrieved by the given property or method name and
	 * null otherwise.
	 * 
	 * @param propertyOrMethodName property or method name
	 * 
	 * @return the member field retrieved by the given property or method name and
	 *         null otherwise.
	 */
	public JavaMethodInfo findMethod(String propertyOrMethodName) {
		if (methods == null || methods.isEmpty() || isEmpty(propertyOrMethodName)) {
			return null;
		}
		String getterMethodName = computeGetterName(propertyOrMethodName);
		String booleanGetterName = computeBooleanGetterName(propertyOrMethodName);
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, propertyOrMethodName, getterMethodName, booleanGetterName)) {
				return method;
			}
		}
		return null;
	}

	private static String computeGetterName(String propertyOrMethodName) {
		return "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	private static String computeBooleanGetterName(String propertyOrMethodName) {
		return "is" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	public static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName) {
		String getterMethodName = computeGetterName(propertyOrMethodName);
		String booleanGetterName = computeBooleanGetterName(propertyOrMethodName);
		return isMatchMethod(method, propertyOrMethodName, getterMethodName, booleanGetterName);
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName, String getterMethodName,
			String booleanGetterName) {
		if (propertyOrMethodName.equals(method.getName()) || getterMethodName.equals(method.getName())
				|| booleanGetterName.equals(method.getName())) {
			return true;
		}
		return false;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
