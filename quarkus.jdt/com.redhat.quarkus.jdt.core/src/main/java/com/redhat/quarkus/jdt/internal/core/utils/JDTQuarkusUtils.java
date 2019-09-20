/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

import com.redhat.quarkus.commons.ClasspathKind;

/**
 * JDT Quarkus utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTQuarkusUtils {

	private JDTQuarkusUtils() {

	}

	/**
	 * Returns the project URI of the given project.
	 * 
	 * @param project the java project
	 * @return the project URI of the given project.
	 */
	public static String getProjectURI(IJavaProject project) {
		return getProjectURI(project.getProject());
	}

	/**
	 * returns the project URI of the given project.
	 * 
	 * @param project the project
	 * @return the project URI of the given project.
	 */
	public static String getProjectURI(IProject project) {
		return project.getLocation().toOSString();
	}

	/**
	 * Returns the extension name (ex: quarkus-core) from the given JAR location (ex
	 * :
	 * C:/Users/azerr/.m2/repository/io/quarkus/quarkus-core/0.21.1/quarkus-core-0.21.1.jar).
	 * 
	 * @param location the JAR location
	 * @return the extension name (ex: quarkus-core) from the given JAR location.
	 */
	public static String getExtensionName(String location) {
		if (location == null) {
			return null;
		}
		if (!location.endsWith(".jar")) {
			return null;
		}
		int start = location.lastIndexOf('/');
		if (start == -1) {
			return null;
		}
		start++;
		int end = location.lastIndexOf('-');
		if (end == -1) {
			end = location.lastIndexOf('.');
		}
		if (end < start) {
			return null;
		}
		String extensionName = location.substring(start, end);
		if (extensionName.endsWith("-deployment")) {
			extensionName = extensionName.substring(0, extensionName.length() - "-deployment".length());
		}
		return extensionName;
	}

	/**
	 * Returns true if the given resource <code>resource</code> is on the 'test'
	 * classpath of the given java project <code>javaProject</code> and false
	 * otherwise.
	 * 
	 * @param resource    the resource
	 * @param javaProject the project.
	 * @return true if the given resource <code>resource</code> is on the 'test'
	 *         classpath of the given java project <code>javaProject</code> and
	 *         false otherwise.
	 */
	public static ClasspathKind getClasspathKind(IResource resource, IJavaProject javaProject) {
		IPath exactPath = resource.getFullPath();
		IPath path = exactPath;

		// ensure that folders are only excluded if all of their children are excluded
		int resourceType = resource.getType();
		boolean isFolderPath = resourceType == IResource.FOLDER || resourceType == IResource.PROJECT;

		IClasspathEntry[] classpath;
		try {
			classpath = ((JavaProject) javaProject).getResolvedClasspath();
		} catch (JavaModelException e) {
			return ClasspathKind.NONE; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.equals(exactPath)) { // package fragment roots must match exactly entry pathes (no exclusion
												// there)
				return getClasspathKind(entry);
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=276373
			// When a classpath entry is absolute, convert the resource's relative path to a
			// file system path and compare
			// e.g - /P/lib/variableLib.jar and /home/P/lib/variableLib.jar when compared
			// should return true
			if (entryPath.isAbsolute()
					&& entryPath.equals(ResourcesPlugin.getWorkspace().getRoot().getLocation().append(exactPath))) {
				return getClasspathKind(entry);
			}
			if (entryPath.isPrefixOf(path)) {
				// && !Util.isExcluded(path, ((ClasspathEntry)
				// entry).fullInclusionPatternChars(),
				// ((ClasspathEntry) entry).fullExclusionPatternChars(), isFolderPath)) {
				return getClasspathKind(entry);
			}
		}
		return ClasspathKind.NONE;

	}

	private static ClasspathKind getClasspathKind(IClasspathEntry entry) {
		return entry.isTest() ? ClasspathKind.TEST : ClasspathKind.SRC;
	}

}
