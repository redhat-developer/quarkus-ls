/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.util.List;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalJavaProject;

import com.redhat.microprofile.commons.MicroProfilePropertiesScope;

/**
 * Fake Java project used to add extra JARs in classpath.
 * 
 * @author Angelo ZERR
 *
 */
public class FakeJavaProject extends ExternalJavaProject {

	private final IJavaProject rootProject;

	public FakeJavaProject(IJavaProject rootProject, IClasspathEntry[] entries) throws JavaModelException {
		super(entries);
		this.rootProject = rootProject;
	}

	/**
	 * Returns the java elements to search according the scope:
	 * 
	 * <ul>
	 * <li>sources scope: only Quarkus Java project</li>
	 * <li>classpath scope:
	 * <ul>
	 * <li>the Quarkus project</li>
	 * <li>all deployment JARs</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param scopes
	 * 
	 * @return the java elements to search
	 * @throws JavaModelException
	 */
	public IJavaElement[] getElementsToSearch(List<MicroProfilePropertiesScope> scopes) throws JavaModelException {
		if (MicroProfilePropertiesScope.isOnlySources(scopes)) {
			return new IJavaElement[] { rootProject };
		}
		IPackageFragmentRoot[] roots = super.getPackageFragmentRoots();
		IJavaElement[] elements = new IJavaElement[1 + roots.length];
		elements[0] = rootProject;
		for (int i = 0; i < roots.length; i++) {
			elements[i + 1] = roots[i];
		}
		return elements;
	}

	@Override
	public boolean exists() {
		return rootProject.exists();
	}

	public IJavaProject getRootProject() {
		return rootProject;
	}

	@Override
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		IType type = rootProject.findType(fullyQualifiedName);
		return type != null ? type : super.findType(fullyQualifiedName);
	}

	/**
	 * Returns the real java project.
	 * 
	 * @param javaProject the java project or the wrapped java project (fake java
	 *                    project).
	 * @return the real java project.
	 */
	public static IJavaProject getRealJavaProject(IJavaProject javaProject) {
		if (javaProject instanceof FakeJavaProject) {
			return ((FakeJavaProject) javaProject).getRootProject();
		}
		return javaProject;
	}
}
