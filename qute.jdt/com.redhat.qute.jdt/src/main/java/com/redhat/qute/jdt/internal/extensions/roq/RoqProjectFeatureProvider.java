/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.roq;

import static com.redhat.qute.jdt.internal.extensions.roq.RoqUtils.isRoqProject;

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.jdt.template.project.IProjectFeatureProvider;

/**
 * Roq project feature.
 */
public class RoqProjectFeatureProvider implements IProjectFeatureProvider {

	@Override
	public void collectProjectFeatures(IJavaProject javaProject, Set<ProjectFeature> projectFeatures) {
		if (isRoqProject(javaProject)) {
			projectFeatures.add(ProjectFeature.Roq);
		}
	}

}
