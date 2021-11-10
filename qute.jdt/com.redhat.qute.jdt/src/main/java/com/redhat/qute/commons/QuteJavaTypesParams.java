/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

/**
 * Qute Java types parameters used to collect Java classes, packages.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteJavaTypesParams {

	private String projectUri;

	private String pattern;

	public QuteJavaTypesParams() {
	}

	public QuteJavaTypesParams(String pattern, String projectUri) {
		setPattern(pattern);
		setProjectUri(projectUri);
	}

	/**
	 * Returns the Qute project Uri.
	 * 
	 * @return the Qute project Uri.
	 */
	public String getProjectUri() {
		return projectUri;
	}

	/**
	 * Set the Qute project Uri.
	 * 
	 * @param projectUri the Qute project Uri.
	 */
	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	/**
	 * Returns the java type pattern
	 * 
	 * @return the java type pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Set the java type pattern
	 * 
	 * @param pattern the java type pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
}
