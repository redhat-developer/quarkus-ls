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
package com.redhat.qute.parser.template;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Java type provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface JavaTypeInfoProvider {

	/**
	 * Returns the Java type as string and null otherwise.
	 * 
	 * For instance parameter declaration implement this API:
	 * 
	 * <code>
	 * {@org.acme.Item item}
	 * </code>
	 * 
	 * In this sample {@link #getJavaType()} will return org.acme.Item.
	 * 
	 * @return the Java type as string and null otherwise.
	 */
	default String getJavaType() {
		return null;
	}

	/**
	 * Returns the Java type as expression to evaluate to get the Java type and null
	 * otherwise.
	 * 
	 * @return the Java type as expression to evaluate to get the Java type and null
	 *         otherwise.
	 */
	default Expression getJavaTypeExpression() {
		return null;
	}

	/**
	 * Returns the owner node where the Java type comes from and null otherwise.
	 * 
	 * For instance parameter declaration implement this API:
	 * 
	 * <code>
	 * {@org.acme.Item item}
	 * </code>
	 * 
	 * In this sample {@link #getJavaTypeOwnerNode()} will return the
	 * {@link ParameterDeclaration} node.
	 * 
	 * @return the owner node where the Java type comes from and null otherwise.
	 */
	Node getJavaTypeOwnerNode();

	default ResolvedJavaTypeInfo getResolvedType() {
		return null;
	}
}
