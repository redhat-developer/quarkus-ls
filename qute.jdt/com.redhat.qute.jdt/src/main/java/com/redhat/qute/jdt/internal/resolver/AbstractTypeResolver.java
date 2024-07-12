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

import java.util.ArrayList;
import java.util.List;
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

	protected final IType primaryType;

	public AbstractTypeResolver(IType primaryType) {
		this.primaryType = primaryType;
	}

	public static String resolveJavaTypeSignature(IType type) {
		StringBuilder typeName = new StringBuilder(type.getFullyQualifiedName());
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
	public List<String> resolveExtendedType() {
		List<String> extendedTypes = new ArrayList<>();
		try {
			String superTypeSignature = primaryType.getSuperclassTypeSignature();
			if (superTypeSignature != null) {
				extendedTypes.add(resolveTypeSignature(superTypeSignature, primaryType));
			}
			String[] superInterfaceTypeSignature = primaryType.getSuperInterfaceTypeSignatures();
			if (superInterfaceTypeSignature != null) {
				for (String string : superInterfaceTypeSignature) {
					extendedTypes.add(resolveTypeSignature(string, primaryType));
				}
			}

		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving super class Java Types for Java type '"
					+ primaryType.getFullyQualifiedName('.') + "'.", e);
		}
		return extendedTypes;
	}

	@Override
	public String resolveFieldSignature(IField field) {
		StringBuilder signature = new StringBuilder(field.getElementName());
		signature.append(" : ");
		try {
			signature.append(resolveTypeSignature(field.getTypeSignature(), false, field.getDeclaringType()));
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
					signature.append(resolveLocalVariableSignature(parameter, varargs && i == parameters.length - 1));
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE,
					"Error while resolving method parameters type of '" + method.getElementName() + "'", e);
		}
		signature.append(')');
		try {
			String returnType = resolveTypeSignature(method.getReturnType(), false, method.getDeclaringType());
			signature.append(" : ");
			signature.append(returnType);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while resolving method return type of '" + method.getElementName() + "'",
					e);
		}
		return signature.toString();
	}

	@Override
	public String resolveLocalVariableSignature(ILocalVariable parameter, boolean varargs) {
		return resolveTypeSignature(parameter.getTypeSignature(), varargs,
				parameter.getDeclaringMember().getDeclaringType());
	}

	@Override
	public String resolveTypeSignature(String typeSignature, IType declaringType) {
		return resolveTypeSignature(typeSignature, false, declaringType);
	}

	private String resolveTypeSignature(String typeSignature, boolean varargs, IType declaringType) {
		if (typeSignature.charAt(0) == '[') {
			return doResolveTypeSignature(typeSignature.substring(1, typeSignature.length()), declaringType)
					+ (varargs ? "..." : "[]");
		}
		return doResolveTypeSignature(typeSignature, declaringType);
	}

	private String doResolveTypeSignature(String typeSignature, IType declaringType) {
		// Example: for 'class A<A1, A2> extends B<A2, String>' the type signature is
		// 'QB<QA2;QString;>;'
		// The method should return 'B<A2,java.lang.String>'
		char type = typeSignature.charAt(0);
		switch (type) {
		case Signature.C_TYPE_VARIABLE: {
			// ex : TK; used in Ljava.util.Map$Entry<TK;TV;>;>
			boolean endsWithColon = typeSignature.charAt(typeSignature.length() - 1) == Signature.C_NAME_END;
			return resolveSimpleType(typeSignature.substring(1, typeSignature.length() - (endsWithColon ? 1 : 0)));
		}
		case Signature.C_RESOLVED: {
			if (typeSignature.indexOf('$') != -1) {
				// We cannot use Signature.toString(typeSignature); from JDT because it replaces
				// '$' (for inner class) with '.'
				// ex : Ljava.util.Set<Ljava.util.Map$Entry<TK;TV;>;>; the JDT
				// Signature.toString returns java.util.Set<java.util.Map.Entry<K,V>>
				// and not java.util.Set<java.util.Map$Entry<K,V>>
				return doResolveTypeSignatureWithoutJDT(typeSignature, declaringType);
			}
			return Signature.toString(typeSignature);
		}
		case Signature.C_UNRESOLVED:
			return doResolveTypeSignatureWithoutJDT(typeSignature, declaringType);
		}
		// ex :
		// - Ljava.lang.Long;
		// - Ljava.util.Set<Ljava.util.Map$Entry<TK;TV;>;>;
		return Signature.toString(typeSignature);
	}

	public String doResolveTypeSignatureWithoutJDT(String typeSignature, IType declaringType) {
		int startGeneric = typeSignature.indexOf('<');
		boolean hasGeneric = startGeneric != -1;
		if (!hasGeneric) {
			// Example : 'QString';
			// Remove the 'Q' start and the ';' end.
			boolean endsWithColon = typeSignature.charAt(typeSignature.length() - 1) == Signature.C_NAME_END;
			String simpleType = typeSignature.substring(1, typeSignature.length() - (endsWithColon ? 1 : 0));
			String resolved = resolveSimpleType(simpleType);

			if (resolved.equals(simpleType) && declaringType != null) {
				try {
					if (declaringType.isEnum()) {
						return resolveJavaTypeSignature(declaringType);
					}
				} catch (JavaModelException e) {
				}
				IType type = declaringType.getType(simpleType);
				while (type != null && type.exists()) {
					resolved = resolveJavaTypeSignature(type);
					if (!resolved.equals(simpleType)) {
						return resolved;
					}
					type = type.getDeclaringType();
				}
			}
			return resolved;
		}

		// Example :
		// - QList<QString;>;
		// - QList<QSet<QString;>;>;
		int endGeneric = typeSignature.lastIndexOf('>');

		String typeErasure = typeSignature.substring(0, startGeneric); // ex : List
		StringBuilder result = new StringBuilder();
		result.append(doResolveTypeSignature(typeErasure + Signature.C_NAME_END, declaringType));
		result.append('<');

		int bracket = 0;
		int start = startGeneric + 1;
		int nbTypeParams = 0;
		for (int i = start; i < endGeneric; i++) {
			char c = typeSignature.charAt(i);
			switch (c) {
			case Signature.C_GENERIC_START:
				bracket++;
				break;
			case Signature.C_GENERIC_END:
				bracket--;
				break;
			case Signature.C_NAME_END:
				if (bracket == 0) {
					if (nbTypeParams > 0) {
						result.append(",");
					}
					String s = typeSignature.substring(start, i);
					result.append(doResolveTypeSignature(s + Signature.C_NAME_END, declaringType));
					nbTypeParams++;
					start = i + 1;
				}
				break;
			}
		}
		result.append('>');
		return result.toString();
	}

	protected abstract String resolveSimpleType(String name);
}
