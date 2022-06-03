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

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;

/**
 * Type resolver API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ITypeResolver {

	/**
	 * Returns the Java signature from the given JDT <code>javaElement</code>.
	 * 
	 * @param javaElement the Java element (field, method).
	 * 
	 * @return the Java signature from the given JDT <code>javaElement</code>.
	 */
	default String resolveSignature(IJavaElement javaElement) {
		switch (javaElement.getElementType()) {
		case IJavaElement.FIELD:
			return resolveFieldSignature((IField) javaElement);
		case IJavaElement.METHOD:
			return resolveMethodSignature((IMethod) javaElement);
		default:
			throw new UnsupportedOperationException("Unsupported java element type: " + javaElement.getElementType());
		}
	}

	/**
	 * Returns the resolved Java type signature from the given String
	 * <code>typeSignature</code>.
	 * 
	 * Example:
	 * 
	 * <code>
	 * String
	 * </code>
	 * 
	 * will returns:
	 * 
	 * <code>
	 * java.lang.String
	 * </code>
	 * 
	 * @param typeSignature the Java type signature.
	 * 
	 * @return the resolved Java type signature from the given String
	 *         <code>typeSignature</code>.
	 */
	String resolveTypeSignature(String typeSignature);

	/**
	 * Returns the Java field signature from the given JDT <code>field</code>.
	 * 
	 * Example:
	 * 
	 * <code>
	 * name : java.lang.String
	 * </code>
	 * 
	 * @param field the JDT field
	 * 
	 * @return the Java field signature.
	 */
	String resolveFieldSignature(IField field);

	/**
	 * Returns the Java method signature from the given JDT <code>method</code>.
	 * 
	 * Example:
	 * 
	 * <code>
	 * find(query : java.lang.String, params : java.util.Map<java.lang.String,java.lang.Object>) : io.quarkus.hibernate.orm.panache.PanacheQuery<T>
	 * </code>
	 * 
	 * @param method the JDT method
	 * 
	 * @return the Java method signature.
	 */
	String resolveMethodSignature(IMethod method);

}
