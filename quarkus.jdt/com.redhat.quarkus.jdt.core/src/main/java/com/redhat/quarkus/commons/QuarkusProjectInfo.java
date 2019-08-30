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

	private boolean quarkusProject;

	private String projectURI;

	private List<ExtendedConfigDescriptionBuildItem> properties;

	/**
	 * Returns <code>true</code> if the project is a Quarkus project and
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the project is a Quarkus project and
	 *         <code>false</code> otherwise.
	 */
	public boolean isQuarkusProject() {
		return quarkusProject;
	}

	/**
	 * Set <code>true</code> if the project is a Quarkus project and
	 * <code>false</code> otherwise.
	 * 
	 * @param quarkusProject <code>true</code> if the project is a Quarkus project
	 *                       and <code>false</code> otherwise.
	 */
	public void setQuarkusProject(boolean quarkusProject) {
		this.quarkusProject = quarkusProject;
	}

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
