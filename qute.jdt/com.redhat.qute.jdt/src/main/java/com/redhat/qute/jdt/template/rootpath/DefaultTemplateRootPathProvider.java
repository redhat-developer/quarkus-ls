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
package com.redhat.qute.jdt.template.rootpath;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.jdt.utils.JDTQuteProjectUtils;

/**
 * Default template root path provider for Qute project (src/main/resources/templates)
 */
public class DefaultTemplateRootPathProvider implements ITemplateRootPathProvider{

	private static final String ORIGIN = "core";
	public static final String TEMPLATES_BASE_DIR = "src/main/resources/templates/";
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return JDTQuteProjectUtils.hasQuteSupport(project);
	}

	@Override
	public void collectTemplateRootPaths(IJavaProject javaProject, List<TemplateRootPath> rootPaths) {
		IProject project = javaProject.getProject();
		String templateBaseDir = project.getFile(TEMPLATES_BASE_DIR).getLocationURI().toString();
		rootPaths.add(new TemplateRootPath(templateBaseDir, ORIGIN));		
	}

}
