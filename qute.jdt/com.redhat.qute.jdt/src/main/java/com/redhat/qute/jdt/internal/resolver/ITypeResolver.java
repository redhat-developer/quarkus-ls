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
import org.eclipse.jdt.core.IMethod;

/**
 * Type resolver API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ITypeResolver {

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
