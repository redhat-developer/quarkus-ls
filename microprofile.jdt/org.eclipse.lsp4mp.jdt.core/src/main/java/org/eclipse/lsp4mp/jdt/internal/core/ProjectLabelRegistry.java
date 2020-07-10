/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.jdt.internal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4mp.jdt.core.IProjectLabelProvider;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.core.ProjectLabelDefinition;

/**
 *  Registry to hold the extension point
 * "org.eclipse.lsp4mp.jdt.core.projectLabelProviders".
 * 
 */
public class ProjectLabelRegistry {

	private static final String CLASS_ATTR = "class";

	private static final String EXTENSION_PROJECT_LABEL_PROVIDERS = "projectLabelProviders";
	
	private static final Logger LOGGER = Logger.getLogger(ProjectLabelRegistry.class.getName());

	private static final ProjectLabelRegistry INSTANCE = new ProjectLabelRegistry();

	private final List<ProjectLabelDefinition> projectLabelDefinitions;

	private boolean projectDefinitionsLoaded;
	
	public static ProjectLabelRegistry getInstance() {
		return INSTANCE;
	}

	public ProjectLabelRegistry() {
		projectDefinitionsLoaded = false;
		projectLabelDefinitions = new ArrayList<>();
	}

	/**
	 * Returns a list of project label definitions
	 *
	 * @return a list of project label definitions
	 */
	public  List<ProjectLabelDefinition> getProjectLabelDefinitions() {
		loadProjectLabelDefinitions();
		return projectLabelDefinitions;
	}
	
	private synchronized void loadProjectLabelDefinitions() {
		if (projectDefinitionsLoaded)
			return;

		// Immediately set the flag, as to ensure that this method is never
		// called twice
		projectDefinitionsLoaded = true;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(MicroProfileCorePlugin.PLUGIN_ID, EXTENSION_PROJECT_LABEL_PROVIDERS);
		addProjectLabelDefinition(cf);
	}
	
	private void addProjectLabelDefinition(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				IProjectLabelProvider provider = (IProjectLabelProvider) ce.createExecutableExtension(CLASS_ATTR);
				synchronized (projectLabelDefinitions) {
					this.projectLabelDefinitions.add(new ProjectLabelDefinition(provider));
				}
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, "Error while collecting project label extension contributions", t);
			}
		}
	}
}