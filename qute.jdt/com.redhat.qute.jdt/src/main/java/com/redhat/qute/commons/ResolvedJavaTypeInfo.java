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

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;

/**
 * Resolved Java type information.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvedJavaTypeInfo extends JavaTypeInfo {

	private static final String ITERABLE_TYPE = "Iterable";

	private static final String JAVA_LANG_ITERABLE_TYPE = "java.lang.Iterable";

	private List<String> extendedTypes;

	private List<JavaFieldInfo> fields;

	private List<JavaMethodInfo> methods;

	private Boolean binary;

	private RegisterForReflectionAnnotation registerForReflectionAnnotation;

	private List<TemplateDataAnnotation> templateDataAnnotations;

	private transient String iterableOf;

	private transient Boolean isIterable;

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
	
	protected boolean isFieldsInitialized() {
		return fields != null;
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

	protected boolean isMethodsInitialized() {
		return methods != null;
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
		if (iterableOf == null && isArray()) {
			iterableOf = getName().substring(0, getName().length() - 2);
		}
		return iterableOf;
	}

	/**
	 * Returns true if the Java type is iterable (ex :
	 * java.util.List<org.acme.item>) and false otherwise.
	 * 
	 * @return true if the Java type is iterable and false otherwise.
	 */
	public boolean isIterable() {
		if (isArray()) {
			return true;
		}
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		isIterable = computeIsIterable();
		return isIterable.booleanValue();
	}

	/**
	 * Returns true if this Java type is in a binary file, and false otherwise.
	 * 
	 * @return true if this Java type is in a binary file, and false otherwise
	 */
	public boolean isBinary() {
		return binary != null && binary.booleanValue();
	}

	/**
	 * Set if this type comes from a binary file.
	 * 
	 * @param binary true if the type comes from a binary file, false otherwise
	 */
	public void setBinary(Boolean binary) {
		this.binary = binary;
	}

	/**
	 * Returns true if the Java type is an integer and false otherwise.
	 * 
	 * @return true if the Java type is an integer and false otherwise.
	 */
	public boolean isInteger() {
		String name = getName();
		return "int".equals(name) || "java.lang.Integer".equals(name);
	}

	private synchronized boolean computeIsIterable() {
		if (isIterable != null) {
			return isIterable.booleanValue();
		}
		if (iterableOf != null) {
			return true;
		}
		boolean iterable = getName().equals(JAVA_LANG_ITERABLE_TYPE);
		if (!iterable && extendedTypes != null) {
			for (String extendedType : extendedTypes) {
				if (ITERABLE_TYPE.equals(extendedType) || extendedType.equals(JAVA_LANG_ITERABLE_TYPE)) {
					iterable = true;
					break;
				}
			}
		}

		if (iterable) {
			List<JavaParameterInfo> typeParameters = getTypeParameters();
			if (!typeParameters.isEmpty()) {
				this.iterableOf = typeParameters.get(0).getType();
			} else {
				this.iterableOf = "java.lang.Object";
			}
		}
		return iterable;
	}

	/**
	 * Returns the list of '@io.quarkus.qute.TemplateData' annotations for the Java
	 * type.
	 * 
	 * @return the list of '@io.quarkus.qute.TemplateData' annotations for the Java
	 *         type.
	 */
	public List<TemplateDataAnnotation> getTemplateDataAnnotations() {
		return templateDataAnnotations;
	}

	/**
	 * Set the list of '@io.quarkus.qute.TemplateData' annotations for the Java
	 * type.
	 * 
	 * @param templateDataAnnotations the list of '@io.quarkus.qute.TemplateData'
	 *                                annotations for the Java type.
	 */
	public void setTemplateDataAnnotations(List<TemplateDataAnnotation> templateDataAnnotations) {
		this.templateDataAnnotations = templateDataAnnotations;
	}

	/**
	 * Returns '@io.quarkus.runtime.annotations.RegisterForReflection' for the Java
	 * type.
	 * 
	 * @return '@io.quarkus.runtime.annotations.RegisterForReflection' for the Java
	 *         type.
	 */
	public RegisterForReflectionAnnotation getRegisterForReflectionAnnotation() {
		return registerForReflectionAnnotation;
	}

	/**
	 * Set the '@io.quarkus.runtime.annotations.RegisterForReflection' for the Java
	 * type.
	 * 
	 * @param registerForReflectionAnnotation '@io.quarkus.runtime.annotations.RegisterForReflection'
	 *                                        for the Java type.
	 */
	public void setRegisterForReflectionAnnotation(RegisterForReflectionAnnotation registerForReflectionAnnotation) {
		this.registerForReflectionAnnotation = registerForReflectionAnnotation;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("signature", this.getSignature());
		b.add("binary", this.isBinary() ? "BINARY" : "SOURCE");
		b.add("iterableOf", this.getIterableOf());
		b.add("templateDataAnnotations", this.getTemplateDataAnnotations());
		b.add("registerForReflectionAnnotation", this.getRegisterForReflectionAnnotation());
		return b.toString();
	}

	/**
	 * Returns true if the java type kind is Unknown
	 * 
	 * @return true if the java type kind is Unknown
	 */
	public boolean isUnknown() {
		return getJavaTypeKind() == JavaTypeKind.Unknown;
	}

	/**
	 * Returns true if the java type kind is Package
	 * 
	 * @return true if the java type kind is Package
	 */
	public boolean isPackage() {
		return getJavaTypeKind() == JavaTypeKind.Package;
	}

	/**
	 * Returns true if the java type kind is Class
	 * 
	 * @return true if the java type kind is Class
	 */
	public boolean isClass() {
		return getJavaTypeKind() == JavaTypeKind.Class;
	}

	/**
	 * Returns true if the java type kind is Interface
	 * 
	 * @return true if the java type kind is Interface
	 */
	public boolean isInterface() {
		return getJavaTypeKind() == JavaTypeKind.Interface;
	}

	/**
	 * Returns true if the java type kind is Enum
	 * 
	 * @return true if the java type kind is Enum
	 */
	public boolean isEnum() {
		return getJavaTypeKind() == JavaTypeKind.Enum;
	}

}
