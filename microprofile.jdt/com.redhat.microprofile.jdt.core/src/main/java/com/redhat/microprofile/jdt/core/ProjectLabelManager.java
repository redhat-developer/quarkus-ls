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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.redhat.microprofile.commons.MicroProfileJavaProjectLabelsParams;
import com.redhat.microprofile.commons.ProjectLabelInfoEntry;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;
import com.redhat.microprofile.jdt.internal.core.ProjectLabelRegistry;

/**
 * Project label manager which provides <code>ProjectLabelInfo</code> containing
 * project labels for all projects in the workspace
 *
 */
public class ProjectLabelManager {
	private static final ProjectLabelManager INSTANCE = new ProjectLabelManager();

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
			ProjectLabelInfoEntry info = getProjectLabelInfo(project);
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
	 * @return project label results for the given Eclipse project.
	 */
	private ProjectLabelInfoEntry getProjectLabelInfo(IProject project) {
		String uri = JDTMicroProfileUtils.getProjectURI(project);
		if (uri != null) {
			return new ProjectLabelInfoEntry(uri, getProjectLabels(project));
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
		return getProjectLabelInfo(file.getProject());
	}

	private List<String> getProjectLabels(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);

		if (javaProject == null) {
			return Collections.emptyList();
		}

		List<String> projectLabels = new ArrayList<>();
		List<ProjectLabelDefinition> definitions = ProjectLabelRegistry.getInstance().getProjectLabelDefinitions();
		for (ProjectLabelDefinition definition : definitions) {
			projectLabels.addAll(definition.getProjectLabels(javaProject));
		}

		return projectLabels;
	}

}
