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
package com.redhat.qute.jdt.internal.extensions.webbundler;

import static com.redhat.qute.jdt.internal.JDTMicroProfileProjectUtils.getProperty;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProject;
import org.eclipse.lsp4mp.jdt.core.project.JDTMicroProfileProjectManager;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.config.webbundler.WebBundlerConfig;
import com.redhat.qute.jdt.internal.JDTMicroProfileProjectUtils;
import com.redhat.qute.jdt.template.rootpath.ITemplateRootPathProvider;

/**
 * Web Bundler template root path provider for Web Bundler project.
 */
public class WebBundlerTemplateRootPathProvider implements ITemplateRootPathProvider {

	private static final String DEFAULT_WEB_DIR = WebBundlerConfig.WEB_DIR.getDefaultValue(); // web
	private static final String DEFAULT_WEB_ROOT = TemplateRootPath.RESOURCE_DIR
			+ WebBundlerConfig.WEB_ROOT.getDefaultValue(); // ${resources-dir}/web

	@Override
	public boolean isApplicable(IJavaProject javaProject) {
		return WebBundlerUtils.isWebBundlerProject(javaProject);
	}

	@Override
	public void collectTemplateRootPaths(IJavaProject javaProject, String projectFolder, Set<String> sourceFolders,
			List<TemplateRootPath> rootPaths) {

		JDTMicroProfileProject mpProject = getMicroProfileProject(javaProject);

		String webDir = getWebDir(mpProject);
		// web/templates
		addTemplatesDirs(TemplateRootPath.resolvePath(projectFolder, sourceFolders, webDir, "templates"), rootPaths);

		// src/main/resources/web/templates
		String webRoot = getWebRoot(mpProject);
		addTemplatesDirs(TemplateRootPath.resolvePath(projectFolder, sourceFolders, webRoot, "templates"), rootPaths);

		// Qute tags
		// See
		// https://docs.quarkiverse.io/quarkus-web-bundler/dev/config-reference.html#quarkus-web-bundler_quarkus-web-bundler-bundle-bundle-qute-tags
		// quarkus.web-bundler.bundle."bundle".qute-tags
		if (mpProject != null) {
			Set<String> mathingSegments = JDTMicroProfileProjectUtils
					.getMatchingSegments(WebBundlerConfig.QUTE_TAGS.getName(), mpProject);
			for (String segment : mathingSegments) {
				addTemplatesDirs(TemplateRootPath.resolvePath(projectFolder, sourceFolders, webRoot, segment), true,
						false, rootPaths);
			}
		}
	}

	private static JDTMicroProfileProject getMicroProfileProject(IJavaProject javaProject) {
		try {
			return JDTMicroProfileProjectManager.getInstance().getJDTMicroProfileProject(javaProject);
		} catch (Exception e) {
			return null;
		}
	}

	private static String getWebRoot(JDTMicroProfileProject mpProject) {
		if (mpProject == null) {
			return DEFAULT_WEB_ROOT;
		}
		return TemplateRootPath.getPath(TemplateRootPath.RESOURCE_DIR,
				getProperty(WebBundlerConfig.WEB_ROOT, mpProject));
	}

	private static String getWebDir(JDTMicroProfileProject mpProject) {
		if (mpProject == null) {
			return DEFAULT_WEB_DIR;
		}
		return getProperty(WebBundlerConfig.WEB_DIR, mpProject);
	}

	private static void addTemplatesDirs(Set<String> templatesDirs, List<TemplateRootPath> rootPaths) {
		addTemplatesDirs(templatesDirs, false, true, rootPaths);
	}

	private static void addTemplatesDirs(Set<String> templatesDirs, boolean onlyTags, boolean namespacedTagSupported,
			List<TemplateRootPath> rootPaths) {
		for (String templatesDir : templatesDirs) {
			rootPaths.add(new TemplateRootPath(templatesDir, onlyTags, namespacedTagSupported,
					WebBundlerConfig.EXTENSION_ID));
		}
	}

}
