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
package com.redhat.qute.project;

import java.util.concurrent.CompletableFuture;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;

/**
 * Represents an object that can resolve information about java types
 * 
 * @author datho7561
 */
public interface IJavaTypeResolver {

	/**
	 * Returns resolved type information for the given type as a future.
	 * 
	 * @param className  the fully qualified name of the Java type to the resolved
	 *                   information of
	 * @param projectUri the uri of the project that the Java type is in
	 * @return resolved type information for the given type as a future
	 */
	CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className, String projectUri);

}
