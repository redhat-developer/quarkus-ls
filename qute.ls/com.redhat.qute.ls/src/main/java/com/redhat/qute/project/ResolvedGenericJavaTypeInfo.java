/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.RegisterForReflectionAnnotation;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;

/**
 * {@link ResolvedJavaTypeInfo} with apply generic type invocation.
 * 
 * @author Angelo ZERR
 *
 */
class ResolvedGenericJavaTypeInfo extends ResolvedJavaTypeInfo {

	private final ResolvedJavaTypeInfo genericType;

	private final Map<String, String> genericMap;

	public ResolvedGenericJavaTypeInfo(ResolvedJavaTypeInfo genericType, Map<String, String> genericMap) {
		this.genericType = genericType;
		this.genericMap = genericMap;
		List<String> extendedTypes = genericType.getExtendedTypes();
		if (extendedTypes != null && !extendedTypes.isEmpty()) {
			List<String> newExtendedTypes = new ArrayList<>(extendedTypes.size());
			for (String extendedType : extendedTypes) {
				newExtendedTypes.add(JavaTypeInfo.applyGenericTypeInvocation(extendedType, genericMap));
			}
			super.setExtendedTypes(newExtendedTypes);
		}
		super.setSignature(JavaTypeInfo.applyGenericTypeInvocation(genericType, genericMap));

	}

	@Override
	public String getDocumentation() {
		return genericType.getDocumentation();
	}

	@Override
	public boolean isBinary() {
		return genericType.isBinary();
	}

	@Override
	public RegisterForReflectionAnnotation getRegisterForReflectionAnnotation() {
		return genericType.getRegisterForReflectionAnnotation();
	}

	@Override
	public List<TemplateDataAnnotation> getTemplateDataAnnotations() {
		return genericType.getTemplateDataAnnotations();
	}

	@Override
	public JavaTypeKind getJavaTypeKind() {
		return genericType.getJavaTypeKind();
	}

	@Override
	public InvalidMethodReason getInvalidMethodReason(String methodName) {
		return genericType.getInvalidMethodReason(methodName);
	}

	@Override
	public List<JavaFieldInfo> getFields() {
		if (super.isFieldsInitialized()) {
			return super.getFields();
		}
		return computeFields();
	}

	private synchronized List<JavaFieldInfo> computeFields() {
		if (super.isFieldsInitialized()) {
			return super.getFields();
		}
		List<JavaFieldInfo> fields = new ArrayList<>(genericType.getFields().size());
		for (JavaFieldInfo field : genericType.getFields()) {
			JavaFieldInfo newField = JavaFieldInfo.applyGenericTypeInvocation(field, genericMap);
			newField.setJavaType(this);
			fields.add(newField);
		}
		super.setFields(fields);
		return fields;
	}

	@Override
	public List<JavaMethodInfo> getMethods() {
		if (super.isMethodsInitialized()) {
			return super.getMethods();
		}
		return computeMethods();
	}

	private synchronized List<JavaMethodInfo> computeMethods() {
		if (super.isMethodsInitialized()) {
			return super.getMethods();
		}
		List<JavaMethodInfo> methods = new ArrayList<>(genericType.getMethods().size());
		for (JavaMethodInfo field : genericType.getMethods()) {
			JavaMethodInfo newMethod = JavaMethodInfo.applyGenericTypeInvocation(field, genericMap);
			newMethod.setJavaType(this);
			methods.add(newMethod);
		}
		super.setMethods(methods);
		return methods;
	}

}
