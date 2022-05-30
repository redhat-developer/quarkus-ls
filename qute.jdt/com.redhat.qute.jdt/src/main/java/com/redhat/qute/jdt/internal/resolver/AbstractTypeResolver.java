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
package com.redhat.qute.jdt.internal.resolver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Abstract class for {@link ITypeResolver}.
 * 
 * @author Angelo ZERR
 *
 */
public abstract class AbstractTypeResolver implements ITypeResolver {

	private static final Logger LOGGER = Logger.getLogger(AbstractTypeResolver.class.getName());

	public static String resolveJavaTypeSignature(IType type) {
		StringBuilder typeName = new StringBuilder(type.getFullyQualifiedName('.'));
		try {
			ITypeParameter[] parameters = type.getTypeParameters();
			if (parameters.length > 0) {
				typeName.append("<");
				for (int i = 0; i < parameters.length; i++) {
					if (i > 0) {
						typeName.append(",");
					}
					typeName.append(parameters[i].getElementName());
				}
				typeName.append(">");
			}
			return typeName.toString();
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while collecting Java Types for Java type '" + typeName + "'.", e);
		}
		return typeName.toString();
	}

	@Override
	public String resolveFieldSignature(IField field) {
		StringBuilder signature = new StringBuilder(field.getElementName());
		signature.append(" : ");
		try {
			signature.append(resolveTypeSignature(field.getTypeSignature(), false));
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving field type '" + field.getElementName() + "'", e);
		}
		return signature.toString();
	}

	@Override
	public String resolveMethodSignature(IMethod method) {
		StringBuilder signature = new StringBuilder(method.getElementName());
		signature.append('(');
		try {
			ILocalVariable[] parameters = method.getParameters();
			if (parameters.length > 0) {
				boolean varargs = Flags.isVarargs(method.getFlags());
				for (int i = 0; i < parameters.length; i++) {
					if (i > 0) {
						signature.append(", ");
					}
					ILocalVariable parameter = parameters[i];
					signature.append(parameter.getElementName());
					signature.append(" : ");
					signature.append(
							resolveTypeSignature(parameter.getTypeSignature(), varargs && i == parameters.length - 1));
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while resolving method parameters type of '" + method.getElementName() + "'", e);
		}
		signature.append(')');
		try {
			String returnType = resolveTypeSignature(method.getReturnType(), false);
			signature.append(" : ");
			signature.append(returnType);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving method return type of '" + method.getElementName() + "'",
					e);
		}
		return signature.toString();
	}

	private String resolveTypeSignature(String typeSignature, boolean varargs) {
		if (typeSignature.charAt(0) == '[') {
			return doResolveTypeSignature(typeSignature.substring(1, typeSignature.length()))
					+ (varargs ? "..." : "[]");
		}
		return doResolveTypeSignature(typeSignature);
	}

	private String doResolveTypeSignature(String typeSignature) {
		int arrayCount = Signature.getArrayCount(typeSignature);
		char type = typeSignature.charAt(arrayCount);
		if (type == Signature.C_UNRESOLVED) {
			String name = ""; //$NON-NLS-1$
			String genericName = null;
			int bracket = typeSignature.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
			if (bracket > 0) {
				name = typeSignature.substring(arrayCount + 1, bracket);
				int endBracket = typeSignature.indexOf(Signature.C_GENERIC_END, bracket);
				genericName = typeSignature.substring(bracket + 2, endBracket - 1);
			} else {
				int semi = typeSignature.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
				if (semi == -1) {
					throw new IllegalArgumentException();
				}
				name = typeSignature.substring(arrayCount + 1, semi);
			}

			if (genericName != null) {
				genericName = resolveSimpleType(genericName);
			}

			String resolved = resolveSimpleType(name);
			if (resolved != null) {
				if (genericName != null) {
					return resolved + '<' + genericName + '>';
				}
				return resolved;
			}

			if (genericName != null) {
				return name + '<' + genericName + '>';
			}
			return name;
		} else {
			return Signature.toString(typeSignature.substring(arrayCount));
		}
	}

	protected abstract String resolveSimpleType(String name);
}
