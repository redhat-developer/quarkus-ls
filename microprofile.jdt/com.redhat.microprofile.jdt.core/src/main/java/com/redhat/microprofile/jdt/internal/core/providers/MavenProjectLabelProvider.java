/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.providers;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

import com.redhat.microprofile.jdt.core.IProjectLabelProvider;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;

/**
 * Provides a Maven-specific label to a project if the project is a
 * Maven project
 *
 * @author dakwon
 *
 */
public class MavenProjectLabelProvider implements IProjectLabelProvider {
	
	public static final String MAVEN_LABEL = "maven";
	private static final String MAVEN_NATURE_ID = "org.eclipse.m2e.core.maven2Nature";

	@Override
	public List<String> getProjectLabels(IJavaProject project) throws JavaModelException {
		if (MavenProjectLabelProvider.isMavenProject(project.getProject())) {
			return Collections.singletonList(MAVEN_LABEL);
		}
		return Collections.emptyList();
	}
	
	private static boolean isMavenProject(IProject project) {
		return JDTMicroProfileUtils.hasNature(project, MAVEN_NATURE_ID);
	}
}
