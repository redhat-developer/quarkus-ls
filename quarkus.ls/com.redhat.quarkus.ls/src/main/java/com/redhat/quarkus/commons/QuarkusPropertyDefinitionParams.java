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

/**
 * Quarkus property definition parameters to retrieve the definition of the
 * Quarkus property in Java class field.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertyDefinitionParams {

	private String uri;

	private String propertySource;

	/**
	 * Returns the application.properties URI.
	 * 
	 * @return the application.properties URI
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the application.properties URI
	 * 
	 * @param uri the application.properties URI
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the Quarkus property source. This source contains the class name and
	 * field name where Quarkus property is defined like
	 * <code>io.quarkus.deployment.ApplicationConfig#name</code>.
	 * 
	 * @return the Quarkus property source.
	 */
	public String getPropertySource() {
		return propertySource;
	}

	/**
	 * Set the Quarkus property source. This source contains the class name and
	 * field name where Quarkus property is defined like
	 * <code>io.quarkus.deployment.ApplicationConfig#name</code>.
	 * 
	 * @param propertySource the Quarkus property source.
	 */
	public void setPropertySource(String propertySource) {
		this.propertySource = propertySource;
	}

}
