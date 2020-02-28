/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.redhat.microprofile.jdt.core.MicroProfileCorePlugin;
import com.redhat.microprofile.jdt.core.java.IJavaDiagnosticsParticipant;
import com.redhat.microprofile.jdt.core.java.IJavaHoverParticipant;

/**
 * Registry to hold the extension point
 * "com.redhat.microprofile.jdt.core.javaFeaturesParticipants".
 * 
 */
public class JavaFeaturesRegistry {

	private static final String EXTENSION_JAVA_FEATURE_PARTICIPANTS = "javaFeatureParticipants";
	private static final String DIAGNOSTICS_ELT = "diagnostics";
	private static final String HOVER_ELT = "hover";
	private static final String CLASS_ATTR = "class";

	private static final Logger LOGGER = Logger.getLogger(JavaFeaturesRegistry.class.getName());

	private static final JavaFeaturesRegistry INSTANCE = new JavaFeaturesRegistry();

	private final List<JavaFeatureDefinition> javaFeatureDefinitions;

	private boolean javaFeatureDefinitionsLoaded;

	public static JavaFeaturesRegistry getInstance() {
		return INSTANCE;
	}

	public JavaFeaturesRegistry() {
		javaFeatureDefinitionsLoaded = false;
		javaFeatureDefinitions = new ArrayList<>();
	}

	/**
	 * Returns a list of javaFeature label definitions
	 *
	 * @return a list of javaFeature label definitions
	 */
	public List<JavaFeatureDefinition> getJavaFeatureDefinitions() {
		loadJavaFeatureDefinitions();
		return javaFeatureDefinitions;
	}

	private synchronized void loadJavaFeatureDefinitions() {
		if (javaFeatureDefinitionsLoaded)
			return;

		// Immediately set the flag, as to ensure that this method is never
		// called twice
		javaFeatureDefinitionsLoaded = true;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(MicroProfileCorePlugin.PLUGIN_ID,
				EXTENSION_JAVA_FEATURE_PARTICIPANTS);
		addJavaFeatureDefinition(cf);
	}

	private void addJavaFeatureDefinition(IConfigurationElement[] cf) {
		for (IConfigurationElement ce : cf) {
			try {
				JavaFeatureDefinition definition = createDefinition(ce);
				synchronized (javaFeatureDefinitions) {
					this.javaFeatureDefinitions.add(definition);
				}
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, "Error while collecting java features extension contributions", t);
			}
		}
	}

	private static JavaFeatureDefinition createDefinition(IConfigurationElement ce) throws CoreException {
		switch (ce.getName()) {
		case HOVER_ELT:
			IJavaHoverParticipant hoverParticipant = (IJavaHoverParticipant) ce.createExecutableExtension(CLASS_ATTR);
			return new JavaFeatureDefinition(hoverParticipant, null);
		case DIAGNOSTICS_ELT:
			IJavaDiagnosticsParticipant diagnosticsParticipant = (IJavaDiagnosticsParticipant) ce
					.createExecutableExtension(CLASS_ATTR);
			return new JavaFeatureDefinition(null, diagnosticsParticipant);
		default:
			return null;
		}
	}
}
