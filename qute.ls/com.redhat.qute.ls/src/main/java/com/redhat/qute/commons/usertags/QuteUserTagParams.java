/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.usertags;

/**
 * Qute user tag parameters used to collect user tags.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteUserTagParams {

	private String projectUri;

	public QuteUserTagParams() {
	}

	public QuteUserTagParams(String projectUri) {
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
}
