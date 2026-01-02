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
import java.util.Set;

/**
 * Project information where a Qute template belongs to.
 * 
 * @author Angelo ZERR
 *
 */
public class ProjectInfo {

	private String uri;

	private List<TemplateRootPath> templateRootPaths;

	private Set<String> sourceFolders;

	private List<String> projectDependencyUris;

	public ProjectInfo() {
	}

	public ProjectInfo(String projectUri, List<String> projectDependencies, List<TemplateRootPath> templateRootPaths,
			Set<String> sourceFolders) {
		setUri(projectUri);
		setProjectDependencyUris(projectDependencies);
		setTemplateRootPaths(templateRootPaths);
		setSourceFolders(sourceFolders);
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

	/**
	 * Returns the list of source folders (full path) like
	 * file:///C:/Users/foo/project/src/main/resources
	 * 
	 * @return the list of source folders (full path) like
	 *         file:///C:/Users/foo/project/src/main/resources
	 */
	public Set<String> getSourceFolders() {
		return sourceFolders;
	}

	/**
	 * Set the list of source folders (full path) like
	 * file:///C:/Users/foo/project/src/main/resources
	 * 
	 * @param sourceFolders the list of source folders (full path) like
	 *                      file:///C:/Users/foo/project/src/main/resources
	 */
	public void setSourceFolders(Set<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}
}
