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
package com.redhat.qute.project.datamodel.resolvers;

import java.util.List;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;

/**
 * Value resolver API.
 * 
 * @author Angelo ZERR
 *
 */
public interface ValueResolver {

	public static final String MATCH_NAME_ANY = "*";

	/**
	 * Returns the named of the resolver.
	 * 
	 * @return the named of the resolver.
	 */
	String getNamed();

	/**
	 * Returns the resolver name (field name, method name).
	 * 
	 * @return the resolver name (field name, method name).
	 */
	String getName();

	/**
	 * Returns the namespace of the resolver and null otherwise.
	 *
	 * @return the namespace of the resolver and null otherwise.
	 */
	String getNamespace();
	
	/**
	 * Returns match names of the resolver.
	 * 
	 * @return the match names of the resolver.
	 */
	List<String> getMatchNames();
	
	/**
	 * Returns the Java element signature.
	 *
	 * @return the Java element signature.
	 */
	String getSignature();

	/**
	 * Returns the java source type and null otherwise.
	 * 
	 * @return the java source type and null otherwise.
	 */
	String getSourceType();

	/**
	 * Returns the Java element kind (type, method, field).
	 *
	 * @return the Java element kind (type, method, field).
	 */
	JavaElementKind getJavaElementKind();

	/**
	 * Returns true if it is a global variable and false otherwise.
	 * 
	 * @return true if it is a global variable and false otherwise.
	 */
	boolean isGlobalVariable();

	ValueResolverKind getKind();
}
