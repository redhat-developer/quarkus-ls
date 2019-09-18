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
import org.eclipse.jdt.core.IJavaProject;

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

}
