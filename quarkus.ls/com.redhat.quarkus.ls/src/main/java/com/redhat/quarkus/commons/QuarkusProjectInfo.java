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

	/**
	 * Returns the Quarkus property and profile information from the given property
	 * name and null otherwise.
	 * 
	 * @param propertyName the property name
	 * @return the Quarkus property and profile information from the given property
	 *         name and null otherwise.
	 */
	public PropertyInfo getProperty(String propertyName) {
		String profile = null;
		if (propertyName.charAt(0) == '%') {
			// property starts with profile (ex : %dev.property-name)
			int dotIndex = propertyName.indexOf('.');
			profile = propertyName.substring(1, dotIndex != -1 ? dotIndex : propertyName.length());
			if (dotIndex == -1) {
				return new PropertyInfo(null, profile);
			}
			propertyName = propertyName.substring(dotIndex + 1, propertyName.length());
		}

		if (propertyName.isEmpty()) {
			return new PropertyInfo(null, profile);
		}

		for (ExtendedConfigDescriptionBuildItem property : properties) {
			if (propertyName.equals(property.getPropertyName())) {
				return new PropertyInfo(property, profile);
			}
		}
		// TODO: retrieve Map property
		// Ex : quarkus.log.category."com.lordofthejars".level property name should
		// return
		// quarkus.log.category.{*}.level Quarkus property
		return null;
	}

}
