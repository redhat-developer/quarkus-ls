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
package com.redhat.qute.jdt.internal.extensions.flags;

import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.config.flags.FlagsConfig;
import com.redhat.qute.jdt.template.project.IProjectFeatureProvider;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Flags project feature.
 */
public class FlagsProjectFeatureProvider implements IProjectFeatureProvider {

	@Override
	public void collectProjectFeatures(IJavaProject javaProject, Set<ProjectFeature> projectFeatures) {
		if (isFlagsProject(javaProject)) {
			projectFeatures.add(FlagsConfig.PROJECT_FEATURE);
		}
	}

	private static boolean isFlagsProject(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, FlagsConfig.FLAG_NAMESPACE_RESOLVER_CLASS) != null;
	}

}
