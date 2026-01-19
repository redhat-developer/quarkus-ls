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
package com.redhat.qute.jdt.template.project;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.jdt.internal.AbstractQuteExtensionPointRegistry;

/**
 * Registry to handle instances of {@link IProjectFeatureProvider}
 *
 * @author Angelo ZERR
 */
public class ProjectFeatureProviderRegistry extends AbstractQuteExtensionPointRegistry<IProjectFeatureProvider> {

	private static final Logger LOGGER = Logger.getLogger(ProjectFeatureProviderRegistry.class.getName());

	private static final String PROJECT_FEATURE_PROVIDERS_EXTENSION_POINT_ID = "projectFeatureProviders";
	private static final ProjectFeatureProviderRegistry INSTANCE = new ProjectFeatureProviderRegistry();

	private ProjectFeatureProviderRegistry() {
		super();
	}

	public static ProjectFeatureProviderRegistry getInstance() {
		return INSTANCE;
	}

	@Override
	public String getProviderExtensionId() {
		return PROJECT_FEATURE_PROVIDERS_EXTENSION_POINT_ID;
	}

	/**
	 * Returns the project feature list for the given java project.
	 * 
	 * @param javaProject the java project.
	 * @param monitor     the progress monitor.
	 * 
	 * @return the project feature list for the given java project.
	 * 
	 * @throws CoreException
	 */
	public Set<ProjectFeature> getProjectFeatures(IJavaProject javaProject, IProgressMonitor monitor) {
		Set<ProjectFeature> projectFeatures = new HashSet<>();
		for (IProjectFeatureProvider provider : super.getProviders()) {
			try {
				provider.collectProjectFeatures(javaProject, projectFeatures);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error while collecting project feature with the provider '"
						+ provider.getClass().getName() + "'.", e);
			}
		}
		return projectFeatures;
	}
}
