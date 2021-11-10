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
 * Qute resolved java type parameters.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteResolvedJavaTypeParams {

	private String className;

	private String projectUri;

	public QuteResolvedJavaTypeParams() {

	}

	public QuteResolvedJavaTypeParams(String className, String projectUri) {
		setClassName(className);
		setProjectUri(projectUri);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
