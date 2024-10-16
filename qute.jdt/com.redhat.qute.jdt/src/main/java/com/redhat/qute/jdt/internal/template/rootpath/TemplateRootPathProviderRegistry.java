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
package com.redhat.qute.jdt.internal.template.rootpath;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.jdt.internal.AbstractQuteExtensionPointRegistry;
import com.redhat.qute.jdt.template.rootpath.ITemplateRootPathProvider;

/**
 * Registry to handle instances of {@link ITemplateRootPathProvider}
 *
 * @author Angelo ZERR
 */
public class TemplateRootPathProviderRegistry extends AbstractQuteExtensionPointRegistry<ITemplateRootPathProvider> {

	private static final Logger LOGGER = Logger.getLogger(TemplateRootPathProviderRegistry.class.getName());

	private static final String TEMPLATE_ROOT_PATH_PROVIDERS_EXTENSION_POINT_ID = "templateRootPathProviders";
	private static final TemplateRootPathProviderRegistry INSTANCE = new TemplateRootPathProviderRegistry();

	private TemplateRootPathProviderRegistry() {
		super();
	}

	public static TemplateRootPathProviderRegistry getInstance() {
		return INSTANCE;
	}

	@Override
	public String getProviderExtensionId() {
		return TEMPLATE_ROOT_PATH_PROVIDERS_EXTENSION_POINT_ID;
	}

	/**
	 * Returns the template root path list for the given java project.
	 * 
	 * @param javaProject the java project.
	 * @param monitor     the progress monitor.
	 * 
	 * @return the template root path list for the given java project.
	 * 
	 * @throws CoreException
	 */
	public List<TemplateRootPath> getTemplateRootPaths(IJavaProject javaProject, IProgressMonitor monitor) {
		List<TemplateRootPath> rootPaths = new ArrayList<>();
		for (ITemplateRootPathProvider provider : super.getProviders()) {
			if (provider.isApplicable(javaProject)) {
				try {
					provider.collectTemplateRootPaths(javaProject, rootPaths);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error while collecting template root path with the provider '"
							+ provider.getClass().getName() + "'.", e);
				}
			}
		}
		return rootPaths;
	}
}
