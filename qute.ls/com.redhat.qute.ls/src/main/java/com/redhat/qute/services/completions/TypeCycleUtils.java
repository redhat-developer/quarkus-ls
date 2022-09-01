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
package com.redhat.qute.services.completions;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.project.IJavaTypeResolver;

/**
 * Utilities class for checking for cycles in types
 * 
 * @author datho7561
 */
public class TypeCycleUtils {

	private TypeCycleUtils() {
	}

	/**
	 * Returns true if the given type is a part of a cyclic inheritance chain, and
	 * false otherwise.
	 * 
	 * @param typeToCheck      the type to check for a cyclic inheritance chain
	 * @param javaTypeResolver the java type resolver
	 * @param projectUri       the project uri
	 * @return true if the given type is a part of a cyclic inheritance chain, and
	 *         false otherwise
	 */
	public static boolean hasCycle(ResolvedJavaTypeInfo typeToCheck, IJavaTypeResolver javaTypeResolver,
			String projectUri) {
		Queue<ResolvedJavaTypeInfo> toVisit = new LinkedList<>();
		Set<JavaTypeInfo> visited = new HashSet<>();
		toVisit.add(typeToCheck);

		while (!toVisit.isEmpty()) {
			ResolvedJavaTypeInfo current = toVisit.poll();
			if (visited.contains(current)) {
				return true;
			}
			visited.add(current);
			if (current.getExtendedTypes() != null) {
				for (String extendedTypeString : current.getExtendedTypes()) {
					ResolvedJavaTypeInfo resolvedTypeInfo = javaTypeResolver.resolveJavaType(extendedTypeString, projectUri)
							.getNow(null);
					if (resolvedTypeInfo != null) {
						toVisit.add(resolvedTypeInfo);
					}
				}
			}
		}
		return false;
	}

}
