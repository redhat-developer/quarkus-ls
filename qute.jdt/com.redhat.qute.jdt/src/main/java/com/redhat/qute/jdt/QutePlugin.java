/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.jdt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.redhat.qute.jdt.internal.JavaDataModelListenerManager;
import com.redhat.qute.jdt.internal.template.datamodel.DataModelProviderRegistry;

/**
 * The activator class controls the Qute JDT LS Extension plug-in life cycle
 */
public class QutePlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.redhat.qute.jdt";

	// The shared instance
	private static QutePlugin plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
		JavaDataModelListenerManager.getInstance().initialize();
		DataModelProviderRegistry.getInstance().initialize();
	}

	public void stop(BundleContext context) throws Exception {
		JavaDataModelListenerManager.getInstance().destroy();
		DataModelProviderRegistry.getInstance().destroy();
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QutePlugin getDefault() {
		return plugin;
	}

	/**
	 * Add the given Java data model changed listener.
	 *
	 * @param listener the listener to add
	 */
	public void addJavaDataModelChangedListener(IJavaDataModelChangedListener listener) {
		JavaDataModelListenerManager.getInstance().addJavaDataModelChangedListener(listener);
	}

	/**
	 * Remove the given Java data model changed listener.
	 *
	 * @param listener the listener to remove
	 */
	public void removeJavaDataModelChangedListener(IJavaDataModelChangedListener listener) {
		JavaDataModelListenerManager.getInstance().removeJavaDataModelChangedListener(listener);
	}

}
