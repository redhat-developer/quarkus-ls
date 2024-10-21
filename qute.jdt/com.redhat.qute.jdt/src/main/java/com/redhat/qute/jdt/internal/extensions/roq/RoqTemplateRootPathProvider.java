/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt.internal.extensions.roq;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.jdt.template.rootpath.ITemplateRootPathProvider;
import com.redhat.qute.jdt.utils.JDTTypeUtils;

/**
 * Roq template root path provider for Roq project.
 */
public class RoqTemplateRootPathProvider implements ITemplateRootPathProvider {

	private static final String ORIGIN = "roq";

	private static final String[] TEMPLATES_BASE_DIRS = { "templates/", "content/", "src/main/resources/content/" };

	@Override
	public boolean isApplicable(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, RoqJavaConstants.SITE_CLASS) != null;
	}

	@Override
	public void collectTemplateRootPaths(IJavaProject javaProject, List<TemplateRootPath> rootPaths) {
		IProject project = javaProject.getProject();
		for (String baseDir : TEMPLATES_BASE_DIRS) {
			String templateBaseDir = project.getFile(baseDir).getLocationURI().toString();
			rootPaths.add(new TemplateRootPath(templateBaseDir, ORIGIN));
		}
	}

}
