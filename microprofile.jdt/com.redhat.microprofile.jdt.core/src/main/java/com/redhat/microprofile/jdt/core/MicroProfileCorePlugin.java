/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.microprofile.jdt.internal.core.MicroProfilePropertiesListenerManager;
import com.redhat.microprofile.jdt.internal.core.PropertiesProviderRegistry;

/**
 * The activator class controls the Quarkus JDT LS Extension plug-in life cycle
 */
public class MicroProfileCorePlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.redhat.microprofile.jdt.core";

	// The shared instance
	private static MicroProfileCorePlugin plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
		MicroProfilePropertiesListenerManager.getInstance().initialize();
		PropertiesProviderRegistry.getInstance().initialize();
	}

	public void stop(BundleContext context) throws Exception {
		MicroProfilePropertiesListenerManager.getInstance().destroy();
		PropertiesProviderRegistry.getInstance().destroy();
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MicroProfileCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Add the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addQuarkusPropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		MicroProfilePropertiesListenerManager.getInstance().addMicroProfilePropertiesChangedListener(listener);
	}

	/**
	 * Remove the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeQuarkusPropertiesChangedListener(IMicroProfilePropertiesChangedListener listener) {
		MicroProfilePropertiesListenerManager.getInstance().removeMicroProfilePropertiesChangedListener(listener);
	}
}
