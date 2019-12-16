/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.utils;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

/**
 * JDT Type utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTTypeUtils {

	private static final List<String> NUMBER_TYPES = Arrays.asList("short", "int", "long", "double", "float");

	public static IType findType(IJavaProject project, String name) {
		try {
			return project.findType(name);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getResolvedTypeName(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getResolvedResultTypeName(IMethod method) {
		try {
			String signature = method.getReturnType();
			IType primaryType = method.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getPropertyType(IType type, String typeName) {
		return type != null ? type.getFullyQualifiedName() : typeName;
	}

	public static String getSourceType(IMember member) {
		return member.getDeclaringType().getFullyQualifiedName();
	}

	public static String getSourceField(IField field) {
		return field.getElementName();
	}

	public static String getSourceMethod(IMethod method) throws JavaModelException {
		return method.getElementName() + method.getSignature();
	}

	public static boolean isOptional(String fieldTypeName) {
		return fieldTypeName.startsWith("java.util.Optional");
	}

	public static String[] getRawTypeParameters(String fieldTypeName) {
		int start = fieldTypeName.indexOf("<") + 1;
		int end = fieldTypeName.lastIndexOf(">");
		String keyValue = fieldTypeName.substring(start, end);
		int index = keyValue.indexOf(',');
		return new String[] { keyValue.substring(0, index), keyValue.substring(index + 1, keyValue.length()) };
	}

	public static boolean isPrimitiveType(String valueClass) {
		return valueClass.equals("java.lang.String") || valueClass.equals("java.lang.Boolean")
				|| valueClass.equals("java.lang.Integer") || valueClass.equals("java.lang.Long")
				|| valueClass.equals("java.lang.Double") || valueClass.equals("java.lang.Float");
	}

	public static boolean isMap(String mapValueClass) {
		return mapValueClass.startsWith("java.util.Map");
	}

	public static boolean isList(String valueClass) {
		return valueClass.startsWith("java.util.List");
	}

	public static boolean isNumber(String valueClass) {
		return NUMBER_TYPES.contains(valueClass);
	}
	
	public static boolean isPrimitiveBoolean(String valueClass) {
		return valueClass.equals("boolean");
	}

	public static IJarEntryResource findPropertiesResource(IPackageFragmentRoot packageRoot, String propertiesFileName)
			throws JavaModelException {
		Object[] resources = packageRoot.getNonJavaResources();
		if (resources != null) {
			for (Object object : resources) {
				if (object instanceof IJarEntryResource) {
					IJarEntryResource res = (IJarEntryResource) object;
					if ("META-INF".equals(res.getName())) {
						IJarEntryResource[] children = res.getChildren();
						if (children != null) {
							for (IJarEntryResource r : children) {
								if (propertiesFileName.equals(r.getName())) {
									return r;
								}
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}
}
