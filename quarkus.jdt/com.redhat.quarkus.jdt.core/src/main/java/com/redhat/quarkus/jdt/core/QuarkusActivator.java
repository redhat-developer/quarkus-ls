/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.quarkus.jdt.internal.core.QuarkusPropertiesListenerManager;

/**
 * The activator class controls the Quarkus JDT LS Extension plug-in life cycle
 */
public class QuarkusActivator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.redhat.quarkus.jdt.core";

	// The shared instance
	private static QuarkusActivator plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
		QuarkusPropertiesListenerManager.getInstance().initialize();
	}

	public void stop(BundleContext context) throws Exception {
		QuarkusPropertiesListenerManager.getInstance().destroy();
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QuarkusActivator getDefault() {
		return plugin;
	}

	/**
	 * Add the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addQuarkusPropertiesChangedListener(IQuarkusPropertiesChangedListener listener) {
		QuarkusPropertiesListenerManager.getInstance().addQuarkusPropertiesChangedListener(listener);
	}

	/**
	 * Remove the given quarkus properties changed listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeQuarkusPropertiesChangedListener(IQuarkusPropertiesChangedListener listener) {
		QuarkusPropertiesListenerManager.getInstance().removeQuarkusPropertiesChangedListener(listener);
	}
}
