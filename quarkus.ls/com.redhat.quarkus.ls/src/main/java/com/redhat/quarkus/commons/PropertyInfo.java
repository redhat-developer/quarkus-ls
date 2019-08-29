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
 * Property information which contains:
 * 
 * <ul>
 * <li>a Quarkus property</li>
 * <li>a profile</li>
 * <ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertyInfo {

	private final ExtendedConfigDescriptionBuildItem property;

	private final String profile;

	public PropertyInfo(ExtendedConfigDescriptionBuildItem property, String profile) {
		this.property = property;
		this.profile = profile;
	}

	/**
	 * Returns the Quarkus property and null otherwise.
	 * 
	 * @return the Quarkus property and null otherwise.
	 */
	public ExtendedConfigDescriptionBuildItem getProperty() {
		return property;
	}

	/**
	 * Returns the profile.
	 * 
	 * @return the profile.
	 */
	public String getProfile() {
		return profile;
	}

}
