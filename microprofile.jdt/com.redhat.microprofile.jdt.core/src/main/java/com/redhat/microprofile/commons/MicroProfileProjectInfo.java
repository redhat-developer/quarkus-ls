/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.Collections;

import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;

/**
 * MicroProfile Project Information
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileProjectInfo extends ConfigurationMetadata {

	public static final MicroProfileProjectInfo EMPTY_PROJECT_INFO;

	static {
		EMPTY_PROJECT_INFO = new MicroProfileProjectInfo();
		EMPTY_PROJECT_INFO.setProperties(Collections.emptyList());
		EMPTY_PROJECT_INFO.setHints(Collections.emptyList());
		EMPTY_PROJECT_INFO.setProjectURI("");
	}

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
