/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.providers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSource;
import org.eclipse.lsp4mp.jdt.core.project.IConfigSourceProvider;
import org.eclipse.lsp4mp.jdt.core.project.PropertiesConfigSource;

/**
 * Provides configuration sources specific to Quarkus.
 *
 * <ul>
 * <li><code>application.properties</code></li>
 * <li><code>application.yaml</code></li>
 * <li><code>application.yml</code></li>
 * </ul>
 *
 */
public class QuarkusConfigSourceProvider implements IConfigSourceProvider {

	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
	public static final String APPLICATION_YAML_FILE = "application.yaml";
	public static final String APPLICATION_YML_FILE = "application.yml";

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject project) {
		return Arrays.asList(new YamlConfigSource(APPLICATION_YAML_FILE, project),
				new YamlConfigSource(APPLICATION_YML_FILE, project),
				new PropertiesConfigSource(APPLICATION_PROPERTIES_FILE, project, 250));
	}

}
