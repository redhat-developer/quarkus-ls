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
 * Wrapper class around <code>IProjectLabelProvider</code>
 *
 */
public class ProjectLabelDefinition {
	private static final Logger LOGGER = Logger.getLogger(ProjectLabelDefinition.class.getName());
	private final IProjectLabelProvider projectLabelProvider;
	
	public ProjectLabelDefinition(IProjectLabelProvider projectLabelProvider) {
		this.projectLabelProvider = projectLabelProvider;
	}
		
	/**
	 * Returns a list of project labels ("maven", "microprofile", etc.) for the
	 * given <code>project</code>
	 * @param project the Java project
	 * @return a list of project labels for the given <code>project</code>
	 */
	public List<String> getProjectLabels(IJavaProject project) {
		try {
			return projectLabelProvider.getProjectLabels(project);
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting project labels", e);
			return Collections.emptyList();
		}
	}
}
