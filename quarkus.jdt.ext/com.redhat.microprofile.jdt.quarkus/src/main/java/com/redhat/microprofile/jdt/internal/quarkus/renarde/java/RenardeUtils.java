/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.renarde.java;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

/**
 * Utilities for working with Renarde applications.
 */
public class RenardeUtils {

	private RenardeUtils() {
	}

	/**
	 * Returns true if the given class extends Renarde's <code>Controller</code>
	 * class and false otherwise.
	 *
	 * @param project  the project that the class to check is in
	 * @param typeRoot the class (compilation unit or class file) to check
	 * @param monitor  the progress monitor
	 * @return true if the given class extends Renarde's <code>Controller</code>
	 *         class and false otherwise
	 * @throws JavaModelException
	 */
	public static boolean isControllerClass(IJavaProject project, ITypeRoot typeRoot, IProgressMonitor monitor) {
		// This doesn't seem to work
		if (JDTTypeUtils.findType(project, RenardeConstants.CONTROLLER_FQN) == null) {
			return false;
		}
		IType type = typeRoot.findPrimaryType();
		try {
			if (type == null || !type.isClass()) {
				return false;
			}
		} catch (JavaModelException e) {
			return false;
		}
		ITypeHierarchy hierarchy;
		try {
			hierarchy = type.newSupertypeHierarchy(monitor);
		} catch (JavaModelException e) {
			return false;
		}
		boolean isControllerSubtype = Stream.of(hierarchy.getAllClasses()) //
				.anyMatch(supertype -> RenardeConstants.CONTROLLER_FQN.equals(supertype.getFullyQualifiedName()));
		return isControllerSubtype;
	}

	/**
	 * Returns a set of all classes in the given project that extend Renarde's
	 * <code>Controller</code> class.
	 *
	 * @param project the project to search in
	 * @param monitor the progress monitor
	 * @return a set of all classes in the given project that extend Renarde's
	 *         <code>Controller</code> class
	 */
	public static Set<ITypeRoot> getAllControllerClasses(IJavaProject project, IProgressMonitor monitor) {
		IType controllerType = JDTTypeUtils.findType(project, RenardeConstants.CONTROLLER_FQN);
		if (controllerType == null) {
			return Collections.emptySet();
		}
		ITypeHierarchy hierarchy;
		try {
			hierarchy = controllerType.newTypeHierarchy(monitor);
		} catch (JavaModelException e) {
			return Collections.emptySet();
		}
		return Stream.of(hierarchy.getAllSubtypes(controllerType)) //
				.map(subType -> subType.getTypeRoot()) //
				.collect(Collectors.toSet());
	}

}
