/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.template.resolvedtype;

import static com.redhat.qute.jdt.QuteSupportForTemplate.createTypeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.jdt.internal.resolver.AbstractTypeResolver;
import com.redhat.qute.jdt.internal.resolver.ITypeResolver;
import com.redhat.qute.jdt.utils.JDTTypeUtils;
import com.redhat.qute.jdt.utils.QuteReflectionAnnotationUtils;

/**
 * Abstract class for {@link ResolvedJavaTypeInfo} factory.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractResolvedJavaTypeFactory implements IResolvedJavaTypeFactory {

	private static final Logger LOGGER = Logger.getLogger(AbstractResolvedJavaTypeFactory.class.getName());

	@Override
	public ResolvedJavaTypeInfo create(IType type) throws CoreException {
		ITypeResolver typeResolver = createTypeResolver(type);
		String typeSignature = AbstractTypeResolver.resolveJavaTypeSignature(type);
		
		// 1) Collect fields
		List<JavaFieldInfo> fieldsInfo = new ArrayList<>();

		// Standard fields
		IField[] fields = type.getFields();
		for (IField field : fields) {
			if (isValidField(field, type)) {
				// Only public fields are available
				JavaFieldInfo info = new JavaFieldInfo();
				info.setSignature(typeResolver.resolveFieldSignature(field));
				fieldsInfo.add(info);
			}
		}

		// Record fields
		if (type.isRecord()) {
			for (IField field : type.getRecordComponents()) {
				if (isValidRecordField(field, type)) {
					// All record components are valid
					JavaFieldInfo info = new JavaFieldInfo();
					info.setSignature(typeResolver.resolveFieldSignature(field));
					fieldsInfo.add(info);
				}
			}
		}

		// 2) Collect methods
		List<JavaMethodInfo> methodsInfo = new ArrayList<>();
		Map<String, InvalidMethodReason> invalidMethods = new HashMap<>();
		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (isValidMethod(method, type)) {
				try {
					InvalidMethodReason invalid = getValidMethodForQute(method, typeSignature);
					if (invalid != null) {
						invalidMethods.put(method.getElementName(), invalid);
					} else {
						JavaMethodInfo info = createMethod(method, typeResolver);
						methodsInfo.add(info);
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE,
							"Error while getting method signature of '" + method.getElementName() + "'.", e);
				}
			}
		}

		ResolvedJavaTypeInfo resolvedType = new ResolvedJavaTypeInfo();		
		resolvedType.setBinary(type.isBinary());
		resolvedType.setSignature(typeSignature);
		resolvedType.setFields(fieldsInfo);
		resolvedType.setMethods(methodsInfo);
		resolvedType.setInvalidMethods(invalidMethods);
		resolvedType.setExtendedTypes(typeResolver.resolveExtendedType());
		resolvedType.setJavaTypeKind(JDTTypeUtils.getJavaTypeKind(type));
		QuteReflectionAnnotationUtils.collectAnnotations(resolvedType, type, typeResolver);
		return resolvedType;
	}

	protected JavaMethodInfo createMethod(IMethod method, ITypeResolver typeResolver) {
		JavaMethodInfo info = new JavaMethodInfo();
		info.setSignature(typeResolver.resolveMethodSignature(method));
		return info;
	}

	protected abstract boolean isValidRecordField(IField field, IType type);

	protected abstract boolean isValidField(IField field, IType type) throws JavaModelException;

	protected boolean isValidMethod(IMethod method, IType type) {
		try {
			if (method.isConstructor() || !method.exists() || Flags.isSynthetic(method.getFlags())) {
				return false;
			}
			if (!type.isInterface() && !Flags.isPublic(method.getFlags())) {
				return false;
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while checking if '" + method.getElementName() + "' is valid.", e);
			return false;
		}
		return true;
	}

	protected abstract InvalidMethodReason getValidMethodForQute(IMethod method, String typeName);
}
