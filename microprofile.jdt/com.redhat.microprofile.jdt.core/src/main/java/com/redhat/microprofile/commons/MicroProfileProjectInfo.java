/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;

/**
 * MicroProfile Project Information
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfo extends ConfigurationMetadata {

	private String projectURI;

	private ClasspathKind classpathKind;

	/**
	 * Returns the project URI.
	 * 
	 * @return the project URI.
	 */
	public String getProjectURI() {
		return projectURI;
	}

	/**
	 * Set the project URI.
	 * 
	 * @param projectURI the project URI.
	 */
	public void setProjectURI(String projectURI) {
		this.projectURI = projectURI;
	}

	/**
	 * Returns the class path kind.
	 * 
	 * @return
	 */
	public ClasspathKind getClasspathKind() {
		return classpathKind;
	}

	/**
	 * Set the class path kind.
	 * 
	 * @param classpathKind
	 */
	public void setClasspathKind(ClasspathKind classpathKind) {
		this.classpathKind = classpathKind;
	}

}
