/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.core.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.lsp4mp.commons.ClasspathKind;
import org.eclipse.lsp4mp.jdt.core.MicroProfileConfigConstants;

/**
 * JDT MicroProfile utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTMicroProfileUtils {

	private static final Logger LOGGER = Logger.getLogger(JDTMicroProfileUtils.class.getName());

	private JDTMicroProfileUtils() {

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

	/**
	 * Returns true if the given <code>project</code> has a nature specified by
	 * <code>natureId</code> and false otherwise.
	 * 
	 * @param project  the project
	 * @param natureId the nature id
	 * @return true if the given <code>project</code> has a nature specified by
	 *         <code>natureId</code> and false otherwise.
	 */
	public static boolean hasNature(IProject project, String natureId) {
		try {
			return project != null && project.hasNature(natureId);
		} catch (CoreException e) {
			return false;
		}
	}

	private static ClasspathKind getClasspathKind(IClasspathEntry entry) {
		return entry.isTest() ? ClasspathKind.TEST : ClasspathKind.SRC;
	}

	/**
	 * Returns true if <code>javaProject</code> is a MicroProfile project. Returns
	 * false otherwise.
	 * 
	 * @param javaProject the Java project to check
	 * @return true only if <code>javaProject</code> is a MicroProfile project.
	 */
	public static boolean isMicroProfileProject(IJavaProject javaProject) {
		try {
			return javaProject.findType(MicroProfileConfigConstants.CONFIG_PROPERTY_ANNOTATION) != null;
		} catch (JavaModelException e) {
			LOGGER.log(Level.INFO, "Current Java project is not a MicroProfile project", e);
			return false;
		}
	}
}
