/*******************************************************************************
* Copyright (c) 2019-2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.redhat.microprofile.commons.MicroProfileJavaProjectLabelsParams;
import com.redhat.microprofile.commons.ProjectLabelInfoEntry;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;
import com.redhat.microprofile.jdt.internal.core.ProjectLabelRegistry;

/**
 * Project label manager which provides <code>ProjectLabelInfo</code> containing
 * project labels for all projects in the workspace
 *
 */
public class ProjectLabelManager {

	private static final ProjectLabelManager INSTANCE = new ProjectLabelManager();
	
	private static final Logger LOGGER = Logger.getLogger(ProjectLabelManager.class.getName());

	public static ProjectLabelManager getInstance() {
		return INSTANCE;
	}

	private ProjectLabelManager() {

	}

	/**
	 * Returns project label results for all projects in the workspace
	 *
	 * @return project label results for all projects in the workspace
	 */
	public List<ProjectLabelInfoEntry> getProjectLabelInfo() {
		List<ProjectLabelInfoEntry> results = new ArrayList<>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (IProject project : projects) {
			ProjectLabelInfoEntry info = getProjectLabelInfo(project, null);
			if (info != null) {
				results.add(info);
			}
		}
		return results;
	}

	/**
	 * Returns project label results for the given Eclipse project.
	 *
	 * @param project Eclipse project.
	 * @param types   the Java type list to check.
	 * @return project label results for the given Eclipse project.
	 */
	private ProjectLabelInfoEntry getProjectLabelInfo(IProject project, List<String> types) {
		String uri = JDTMicroProfileUtils.getProjectURI(project);
		if (uri != null) {
			try {
				return new ProjectLabelInfoEntry(uri, project.getDescription().getName(), getProjectLabels(project, types));
			} catch (CoreException e) {
				LOGGER.log(Level.SEVERE, "Error while retrieving project name.", e);
			}
		}
		return null;
	}

	/**
	 * Returns project label results for the given Java file uri parameter.
	 * 
	 * @param params  the Java file uri parameter.
	 * @param utils   the JDT utilities.
	 * @param monitor the progress monitor.
	 * @return project label results for the given Java file uri parameter.
	 */
	public ProjectLabelInfoEntry getProjectLabelInfo(MicroProfileJavaProjectLabelsParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		IFile file = utils.findFile(params.getUri());
		if (file == null || file.getProject() == null) {
			// The uri doesn't belong to an Eclipse project
			return ProjectLabelInfoEntry.EMPTY_PROJECT_INFO;
		}
		return getProjectLabelInfo(file.getProject(), params.getTypes());
	}

	/**
	 * Returns the project labels for the given project.
	 * 
	 * @param project the Eclipse project.
	 * @param types   the Java type list to check.
	 * @return the project labels for the given project.
	 */
	private List<String> getProjectLabels(IProject project, List<String> types) {
		IJavaProject javaProject = JavaCore.create(project);

		if (javaProject == null) {
			return Collections.emptyList();
		}

		// Update labels by using the
		// "com.redhat.microprofile.jdt.core.projectLabelProviders" extension point (ex
		// : "maven", "gradle", "quarkus", "microprofile").
		List<String> projectLabels = new ArrayList<>();
		List<ProjectLabelDefinition> definitions = ProjectLabelRegistry.getInstance().getProjectLabelDefinitions();
		for (ProjectLabelDefinition definition : definitions) {
			projectLabels.addAll(definition.getProjectLabels(javaProject));
		}
		// Update labels by checking if some Java types are in the classpath of the Java
		// project.
		if (types != null) {
			for (String type : types) {
				if (JDTTypeUtils.findType(javaProject, type) != null) {
					projectLabels.add(type);
				}
			}
		}

		return projectLabels;
	}

}
