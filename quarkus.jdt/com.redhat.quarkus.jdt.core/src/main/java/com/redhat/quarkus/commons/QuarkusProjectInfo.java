/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.commons;

import java.util.List;

/**
 * Quarkus Project Information
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusProjectInfo {

	private String projectURI;

	private ClasspathKind classpathKind;

	private List<ExtendedConfigDescriptionBuildItem> properties;

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

	public ClasspathKind getClasspathKind() {
		return classpathKind;
	}

	public void setClasspathKind(ClasspathKind classpathKind) {
		this.classpathKind = classpathKind;
	}

	/**
	 * Returns list of available properties for the Quarkus project and null if it's
	 * not a Quarkus project.
	 * 
	 * @return list of available properties for the Quarkus project and null if it's
	 *         not a Quarkus project.
	 */
	public List<ExtendedConfigDescriptionBuildItem> getProperties() {
		return properties;
	}

	/**
	 * Set list of available properties for the Quarkus project and null if it's not
	 * a Quarkus project.
	 * 
	 * @param properties list of available properties for the Quarkus project and
	 *                   null if it's not a Quarkus project.
	 */
	public void setProperties(List<ExtendedConfigDescriptionBuildItem> properties) {
		this.properties = properties;
	}

}
