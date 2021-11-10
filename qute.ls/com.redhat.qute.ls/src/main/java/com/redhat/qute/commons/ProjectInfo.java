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
 * Project information where a Qute template belongs to.
 * 
 * @author Angelo ZERR
 *
 */
public class ProjectInfo {

	private String uri;

	private String templateBaseDir;

	public ProjectInfo() {
	}

	public ProjectInfo(String uri, String templateBaseDir) {
		setUri(uri);
		setTemplateBaseDir(templateBaseDir);
	}

	/**
	 * Returns the project Uri.
	 * 
	 * @return the project Uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the project Uri.
	 * 
	 * @param uri the project Uri.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the Qute templates base directory and null otherwise.
	 * 
	 * @return the Qute templates base directory and null otherwise.
	 */
	public String getTemplateBaseDir() {
		return templateBaseDir;
	}

	/**
	 * Set the Qute templates base directory.
	 * 
	 * @param templateBaseDir the Qute templates base directory.
	 */
	public void setTemplateBaseDir(String templateBaseDir) {
		this.templateBaseDir = templateBaseDir;
	}
}
