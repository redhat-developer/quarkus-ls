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

import static com.redhat.qute.commons.config.roq.RoqConfig.ROQ_DIR;
import static com.redhat.qute.commons.config.roq.RoqConfig.SITE_CONTENT_DIR;
import static com.redhat.qute.jdt.internal.JDTMicroProfileProjectUtils.getProperty;
import static com.redhat.qute.jdt.internal.extensions.roq.RoqUtils.isRoqProject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.config.roq.RoqConfig;
import com.redhat.qute.jdt.template.rootpath.ITemplateRootPathProvider;

/**
 * Roq template root path provider for Roq project.
 */
public class RoqTemplateRootPathProvider implements ITemplateRootPathProvider {

	private static final String[] DEFAULT_TEMPLATES_BASE_DIRS = { "templates/", //
			"content/", //
			TemplateRootPath.RESOURCE_DIR + "content/" };

	@Override
	public boolean isApplicable(IJavaProject javaProject) {
		return isRoqProject(javaProject);
	}

	@Override
	public void collectTemplateRootPaths(IJavaProject javaProject, String projectFolder, Set<String> sourceFolders,
			List<TemplateRootPath> rootPaths) {
		for (String templateBaseDir : getTemplateBaseDirs(javaProject, projectFolder, sourceFolders)) {
			rootPaths.add(new TemplateRootPath(templateBaseDir, RoqConfig.EXTENSION_ID));
		}
	}

	private static Set<String> getTemplateBaseDirs(IJavaProject javaProject, String projectFolder,
			Set<String> sourceFolders) {
		Set<String> dirs = new HashSet<>();
		try {
			JDTMicroProfileProject mpProject = JDTMicroProfileProjectManager.getInstance()
					.getJDTMicroProfileProject(javaProject);
			String roqDir = getProperty(ROQ_DIR, mpProject);
			String contentDir = getProperty(SITE_CONTENT_DIR, mpProject);

			// templates
			dirs.addAll(
					TemplateRootPath.resolvePath(projectFolder, sourceFolders, roqDir, DEFAULT_TEMPLATES_BASE_DIRS[0]));

			// content
			dirs.addAll(TemplateRootPath.resolvePath(projectFolder, sourceFolders, roqDir, contentDir));

			// src/main/resources/content
			dirs.addAll(TemplateRootPath.resolvePath(projectFolder, sourceFolders, TemplateRootPath.RESOURCE_DIR,
					contentDir));

		} catch (Exception e) {
			for (String dir : dirs) {
				dirs.addAll(TemplateRootPath.resolvePath(projectFolder, sourceFolders, dir));
			}
		}
		return dirs;
	}

}
