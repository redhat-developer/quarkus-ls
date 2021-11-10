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
package com.redhat.qute.commons.datamodel;

/**
 * Qute data model project parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteDataModelProjectParams {

	private String projectUri;

	public QuteDataModelProjectParams() {

	}

	public QuteDataModelProjectParams(String projectUri) {
		setProjectUri(projectUri);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

}
