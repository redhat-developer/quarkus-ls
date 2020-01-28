/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Wrapper class defining a project label provider
 *
 */
public class ProjectLabelDefinition {
	private static final Logger LOGGER = Logger.getLogger(ProjectLabelDefinition.class.getName());
	private final IProjectLabelProvider projectLabelProvider;
	
	public ProjectLabelDefinition(IProjectLabelProvider projectLabelProvider) {
		this.projectLabelProvider = projectLabelProvider;
	}
	
	public List<String> getProjectLabels(IJavaProject project) {
		try {
			return projectLabelProvider.getProjectLabels(project);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting project labels", e);
			return Collections.emptyList();
		}
	}
}
