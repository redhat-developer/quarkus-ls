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
	 * Returns the resolved field type.
	 * 
	 * @param field the field
	 * 
	 * @return the resolved field type.
	 */
	String resolveFieldType(IField field);

	/**
	 * Returns the resolved method signature.
	 * 
	 * @param method the method
	 * 
	 * @return the resolved method signature.
	 */
	String resolveMethodSignature(IMethod method);

}
