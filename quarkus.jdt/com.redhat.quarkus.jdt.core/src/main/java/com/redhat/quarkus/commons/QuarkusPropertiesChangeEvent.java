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

import java.util.Set;

/**
 * The quarkus project properties change event.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesChangeEvent {

	private QuarkusPropertiesScope type;

	private Set<String> projectURIs;

	/**
	 * Returns the search scope to collect the Quarkus properties.
	 * 
	 * @return the search scope to collect the Quarkus properties.
	 */
	public QuarkusPropertiesScope getType() {
		return type;
	}

	/**
	 * Set the search scope to collect the Quarkus properties.
	 * 
	 * @param type the search scope to collect the Quarkus properties.
	 */
	public void setType(QuarkusPropertiesScope type) {
		this.type = type;
	}

	/**
	 * Returns the project URIs impacted by the type scope changed.
	 * 
	 * @return the project URIs impacted by the type scope changed.
	 */
	public Set<String> getProjectURIs() {
		return projectURIs;
	}

	/**
	 * Set the project URIs impacted by the type scope changed.
	 * 
	 * @param projectURIs the project URIs impacted by the type scope changed.
	 */
	public void setProjectURIs(Set<String> projectURIs) {
		this.projectURIs = projectURIs;
	}

}
