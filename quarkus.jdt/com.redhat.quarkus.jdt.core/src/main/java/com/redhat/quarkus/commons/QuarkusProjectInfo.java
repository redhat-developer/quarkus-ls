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

	private String projectName;

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
	 * Returns the project name.
	 * 
	 * @return the project name.
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Set the project name.
	 * 
	 * @param projectName the project name.
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
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

	/**
	 * Returns the Quarkus property from the given property name and null otherwise.
	 * 
	 * @param propertyName the property name
	 * @return the Quarkus property from the given property name and null otherwise.
	 */
	public ExtendedConfigDescriptionBuildItem getProperty(String propertyName) {
		for (ExtendedConfigDescriptionBuildItem property : properties) {
			if (propertyName.equals(property.getPropertyName())) {
				return property;
			}
		}
		// TODO: retrieve Map property
		// Ex : quarkus.log.category."com.lordofthejars".level property name should
		// return
		// quarkus.log.category.{*}.level Quarkus property
		return null;
	}

}
