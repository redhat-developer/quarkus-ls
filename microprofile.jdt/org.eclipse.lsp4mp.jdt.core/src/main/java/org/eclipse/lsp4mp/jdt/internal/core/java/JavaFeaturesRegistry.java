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
package org.eclipse.lsp4mp.jdt.internal.core.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4mp.jdt.core.MicroProfileCorePlugin;
import org.eclipse.lsp4mp.jdt.internal.core.java.codeaction.JavaCodeActionDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.codelens.JavaCodeLensDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.diagnostics.JavaDiagnosticsDefinition;
import org.eclipse.lsp4mp.jdt.internal.core.java.hover.JavaHoverDefinition;

/**
 * Registry to hold the extension point
 * "org.eclipse.lsp4mp.jdt.core.javaFeaturesParticipants".
 * 
 */
public class JavaFeaturesRegistry {

	private static final String EXTENSION_JAVA_FEATURE_PARTICIPANTS = "javaFeatureParticipants";
	private static final String CODEACTION_ELT = "codeAction";
	private static final String CODELENS_ELT = "codeLens";
	private static final String DIAGNOSTICS_ELT = "diagnostics";
	private static final String HOVER_ELT = "hover";

	private static final Logger LOGGER = Logger.getLogger(JavaFeaturesRegistry.class.getName());

	private static final JavaFeaturesRegistry INSTANCE = new JavaFeaturesRegistry();

	private final List<JavaCodeActionDefinition> javaCodeActionDefinitions;

	private final List<JavaCodeLensDefinition> javaCodeLensDefinitions;

	private final List<JavaDiagnosticsDefinition> javaDiagnosticsDefinitions;

	private final List<JavaHoverDefinition> javaHoverDefinitions;

	private boolean javaFeatureDefinitionsLoaded;

	public static JavaFeaturesRegistry getInstance() {
		return INSTANCE;
	}

	public JavaFeaturesRegistry() {
		javaFeatureDefinitionsLoaded = false;
		javaCodeActionDefinitions = new ArrayList<>();
		javaCodeLensDefinitions = new ArrayList<>();
		javaDiagnosticsDefinitions = new ArrayList<>();
		javaHoverDefinitions = new ArrayList<>();
	}

	/**
	 * Returns a list of codeLens definition.
	 *
	 * @return a list of codeLens definition.
	 */
	public List<JavaCodeLensDefinition> getJavaCodeLensDefinitions() {
		loadJavaFeatureDefinitions();
		return javaCodeLensDefinitions;
	}

	/**
	 * Returns a list of diagnostics definition.
	 *
	 * @return a list of diagnostics definition.
	 */
	public List<JavaDiagnosticsDefinition> getJavaDiagnosticsDefinitions() {
		loadJavaFeatureDefinitions();
		return javaDiagnosticsDefinitions;
	}

	/**
	 * Returns a list of hover definition.
	 *
	 * @return a list of hover definition.
	 */
	public List<JavaHoverDefinition> getJavaHoverDefinitions() {
		loadJavaFeatureDefinitions();
		return javaHoverDefinitions;
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
				createAndAddDefinition(ce);
			} catch (Throwable t) {
				LOGGER.log(Level.SEVERE, "Error while collecting java features extension contributions", t);
			}
		}
	}

	private void createAndAddDefinition(IConfigurationElement ce) throws CoreException {
		switch (ce.getName()) {
		case CODEACTION_ELT: {
			JavaCodeActionDefinition definition = new JavaCodeActionDefinition(ce);
			synchronized (javaCodeActionDefinitions) {
				javaCodeActionDefinitions.add(definition);
			}
			break;
		}
		case CODELENS_ELT: {
			JavaCodeLensDefinition definition = new JavaCodeLensDefinition(ce);
			synchronized (javaCodeLensDefinitions) {
				javaCodeLensDefinitions.add(definition);
			}
			break;
		}
		case DIAGNOSTICS_ELT: {
			JavaDiagnosticsDefinition definition = new JavaDiagnosticsDefinition(ce);
			synchronized (javaDiagnosticsDefinitions) {
				javaDiagnosticsDefinitions.add(definition);
			}
			break;
		}
		case HOVER_ELT: {
			JavaHoverDefinition definition = new JavaHoverDefinition(ce);
			synchronized (javaHoverDefinitions) {
				javaHoverDefinitions.add(definition);
			}
			break;
		}
		default:

		}
	}

	public List<JavaCodeActionDefinition> getJavaCodeActionDefinitions(String codeActionKind) {
		loadJavaFeatureDefinitions();
		return javaCodeActionDefinitions.stream().filter(definition -> codeActionKind.startsWith(definition.getKind()))
				.collect(Collectors.toList());
	}
}
