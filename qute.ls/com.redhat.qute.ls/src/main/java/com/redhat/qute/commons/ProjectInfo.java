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

import java.util.List;

/**
 * Project information where a Qute template belongs to.
 * 
 * @author Angelo ZERR
 *
 */
public class ProjectInfo {

	private String uri;

	private List<TemplateRootPath> templateRootPaths;

	private List<String> projectDependencyUris;

	public ProjectInfo() {
	}

	public ProjectInfo(String projectUri, List<String> projectDependencies, List<TemplateRootPath> templateRootPaths) {
		setUri(projectUri);
		setProjectDependencyUris(projectDependencies);
		setTemplateRootPaths(templateRootPaths);
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
	 * Returns the project dependency Uris.
	 * 
	 * @return the project dependency Uris.
	 */
	public List<String> getProjectDependencyUris() {
		return projectDependencyUris;
	}

	/**
	 * Set the project dependency Uris.
	 * 
	 * @param projectDependencyUris the project dependency Uris.
	 */
	public void setProjectDependencyUris(List<String> projectDependencyUris) {
		this.projectDependencyUris = projectDependencyUris;
	}

	/**
	 * Returns the list of the template root path supported by the Qute project.
	 * 
	 * @return the list of the template root path supported by the Qute project.
	 */
	public List<TemplateRootPath> getTemplateRootPaths() {
		return templateRootPaths;
	}

	/**
	 * Set the list of the template root path supported by the Qute project.
	 * 
	 * @param templateRootPaths the list of the template root path.
	 */
	public void setTemplateRootPaths(List<TemplateRootPath> templateRootPaths) {
		this.templateRootPaths = templateRootPaths;
	}
}
