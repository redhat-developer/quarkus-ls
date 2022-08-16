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
package com.redhat.qute.jdt.utils;

import static org.eclipse.jdt.core.Signature.SIG_VOID;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.jdt.internal.resolver.AbstractTypeResolver;

/**
 * JDT Type utilities.
 *
 * @author Angelo ZERR
 *
 */
public class JDTTypeUtils {

	private static final Logger LOGGER = Logger.getLogger(JDTTypeUtils.class.getName());

	public static String getSimpleClassName(String className) {
		if (className.endsWith(".java")) {
			return className.substring(0, className.length() - ".java".length());
		}
		return className;
	}

	public static IType findType(IJavaProject project, String className) {
		try {
			return project.findType(className);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while finding type for '" + className + "'.", e);
			return null;
		}
	}

	/**
	 * Returns the resolved type name of the given <code>field</code> and null
	 * otherwise
	 *
	 * @param field the field
	 * @return the resolved type name of the given <code>field</code> and null
	 *         otherwise
	 */
	public static String getResolvedTypeName(IField field) {
		try {
			String signature = field.getTypeSignature();
			IType primaryType = field.getTypeRoot().findPrimaryType();
			return JavaModelUtil.getResolvedTypeName(signature, primaryType);
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getFullQualifiedName(String className, IJavaProject javaProject, IProgressMonitor monitor)
			throws JavaModelException {
		if (className.indexOf('.') != -1) {
			return className;
		}
		IType nameType = findType(className, javaProject, monitor);
		if (nameType != null && nameType.exists()) {
			return AbstractTypeResolver.resolveJavaTypeSignature(nameType);
		}
		return className;
	}

	public static IType findType(String className, IJavaProject javaProject, IProgressMonitor monitor)
			throws JavaModelException {
		try {
			IType type = javaProject.findType(className, monitor);
			if (type != null) {
				return type;
			}
			if (className.indexOf('.') == -1) {
				// No package, try with java.lang package
				// ex : if className = String we should find type of java.lang.String
				return javaProject.findType("java.lang." + className, monitor);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while finding type for '" + className + "'.", e);
		}
		return null;
	}

	/**
	 * Return true if member is static, and false otherwise
	 *
	 * @param member the member to check for static
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean isStaticMember(IMember member) throws JavaModelException {
		return Modifier.isStatic(member.getFlags());
	}

	/**
	 * Return true if member is private, and false otherwise
	 *
	 * @param member the member to check for private access modifier
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean isPrivateMember(IMember member) throws JavaModelException {
		return Modifier.isPrivate(member.getFlags());
	}

	/**
	 * Return true if member is public, and false otherwise
	 *
	 * @param member the member to check for public access modifier
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean isPublicMember(IMember member) throws JavaModelException {
		return Modifier.isPublic(member.getFlags());
	}

	/**
	 * Return true if method returns `void`, and false otherwise
	 *
	 * @param method the method to check return value of
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean isVoidReturnType(IMethod method) throws JavaModelException {
		return SIG_VOID.equals(method.getReturnType());
	}

	/**
	 * Return the JavaTypeKind of the given IType type
	 *
	 * @param the IType of the type to get the JavaTypeKind of
	 * @return the JavaTypeKind of the given IType type
	 * @throws JavaModelException
	 */
	public static JavaTypeKind getJavaTypeKind(IType type) throws JavaModelException {
		if (type.isClass()) {
			return JavaTypeKind.Class;
		}
		if (type.isEnum()) {
			return JavaTypeKind.Enum;
		}
		if (type.isInterface()) {
			return JavaTypeKind.Interface;
		}
		return JavaTypeKind.Unknown;
	}
}
