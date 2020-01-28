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
import org.eclipse.jdt.ls.core.internal.ProjectUtils;

import com.redhat.microprofile.jdt.core.IProjectLabelProvider;
import com.redhat.microprofile.jdt.core.utils.JDTMicroProfileUtils;

/**
 * Provides a Gradle-specific label to a project if the project is a
 * Gradle project
 *
 * @author dakwon
 *
 */
public class GradleProjectLabelProvider implements IProjectLabelProvider {
	
	public static final String GRADLE_LABEL = "gradle";
	private static final String GRADLE_NATURE_ID = "org.eclipse.buildship.core.gradleprojectnature";

	@Override
	public List<String> getProjectLabels(IJavaProject project) throws JavaModelException {
		if (GradleProjectLabelProvider.isGradleProject(project.getProject())) {
			return Collections.singletonList(GRADLE_LABEL);
		}
		return Collections.emptyList();
	}
	
	private static boolean isGradleProject(IProject project) {
		return JDTMicroProfileUtils.hasNature(project, GRADLE_NATURE_ID);
	}
}
