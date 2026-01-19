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
 * This class contains metadata about a Qute project, including its location,
 * template roots, source folders, project dependencies, and supported features.
 * 
 * Example of features: renarde, roq, renarde-qrcode
 */
public class ProjectInfo {

	/**
	 * The project URI (typically a file URI like file:///C:/path/to/project).
	 */
	private String uri;

	/**
	 * Absolute project folder URI (full path) like:
	 * file:///C:/Users/foo/workspace/my-project
	 */
	private String projectFolder;

	/**
	 * List of template root paths supported by the Qute project.
	 */
	private List<TemplateRootPath> templateRootPaths;

	/**
	 * Set of source folders (full path) like:
	 * file:///C:/Users/foo/project/src/main/java
	 * file:///C:/Users/foo/project/src/main/resources
	 */
	private Set<String> sourceFolders;

	/**
	 * List of project dependency URIs (for multi-module projects).
	 */
	private List<String> projectDependencyUris;

	/**
	 * Set of detected project features. Examples: renarde, roq, renarde-qrcode
	 */
	private Set<ProjectFeature> features;

	/**
	 * Default constructor.
	 */
	public ProjectInfo() {
	}

	/**
	 * Full constructor.
	 *
	 * @param projectUri          the project URI like file:///C:/path/to/project
	 * @param projectFolder       absolute project folder URI like file:///...
	 * @param projectDependencies list of project dependency URIs
	 * @param templateRootPaths   list of template root paths
	 * @param sourceFolders       set of source folders (full path) like file:///...
	 * @param features            set of project features
	 */
	public ProjectInfo(String projectUri, String projectFolder, List<String> projectDependencies,
			List<TemplateRootPath> templateRootPaths, Set<String> sourceFolders, Set<ProjectFeature> features) {
		setUri(projectUri);
		setProjectFolder(projectFolder);
		setProjectDependencyUris(projectDependencies);
		setTemplateRootPaths(templateRootPaths);
		setSourceFolders(sourceFolders);
		setFeatures(features);
	}

	// ------------------- Getters & Setters -------------------

	/**
	 * Returns the project URI.
	 * 
	 * @return the project URI like file:///C:/path/to/project
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the project URI.
	 * 
	 * @param uri the project URI like file:///C:/path/to/project
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Returns the absolute project folder URI.
	 * 
	 * @return the project folder URI like file:///C:/Users/foo/workspace/my-project
	 */
	public String getProjectFolder() {
		return projectFolder;
	}

	/**
	 * Sets the absolute project folder URI.
	 * 
	 * @param projectFolder the project folder URI like
	 *                      file:///C:/Users/foo/workspace/my-project
	 */
	public void setProjectFolder(String projectFolder) {
		this.projectFolder = projectFolder;
	}

	/**
	 * Returns the list of template root paths supported by the Qute project.
	 * 
	 * @return the list of template root paths
	 */
	public List<TemplateRootPath> getTemplateRootPaths() {
		return templateRootPaths;
	}

	/**
	 * Sets the list of template root paths supported by the Qute project.
	 * 
	 * @param templateRootPaths the list of template root paths
	 */
	public void setTemplateRootPaths(List<TemplateRootPath> templateRootPaths) {
		this.templateRootPaths = templateRootPaths;
	}

	/**
	 * Returns the set of source folders (full paths) like:
	 * file:///C:/Users/foo/project/src/main/java
	 * file:///C:/Users/foo/project/src/main/resources
	 * 
	 * @return the set of source folders
	 */
	public Set<String> getSourceFolders() {
		return sourceFolders;
	}

	/**
	 * Sets the set of source folders (full paths) like:
	 * file:///C:/Users/foo/project/src/main/java
	 * file:///C:/Users/foo/project/src/main/resources
	 * 
	 * @param sourceFolders the set of source folders
	 */
	public void setSourceFolders(Set<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}

	/**
	 * Returns the list of project dependency URIs.
	 * 
	 * @return the project dependency URIs
	 */
	public List<String> getProjectDependencyUris() {
		return projectDependencyUris;
	}

	/**
	 * Sets the list of project dependency URIs.
	 * 
	 * @param projectDependencyUris the project dependency URIs
	 */
	public void setProjectDependencyUris(List<String> projectDependencyUris) {
		this.projectDependencyUris = projectDependencyUris;
	}

	/**
	 * Returns the set of detected project features.
	 * 
	 * @return the set of features like renarde, roq, renarde-qrcode
	 */
	public Set<ProjectFeature> getFeatures() {
		return features;
	}

	/**
	 * Sets the set of detected project features.
	 * 
	 * @param features the set of features like renarde, roq, renarde-qrcode
	 */
	public void setFeatures(Set<ProjectFeature> features) {
		this.features = features;
	}
}
