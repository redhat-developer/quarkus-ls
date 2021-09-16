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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <li><code>application-${profile}.properties</code></li>
 * </ul>
 *
 */
public class QuarkusConfigSourceProvider implements IConfigSourceProvider {

	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
	public static final String APPLICATION_YAML_FILE = "application.yaml";
	public static final String APPLICATION_YML_FILE = "application.yml";
	private static final Pattern PER_PROFILE_FILE_NAME_PTN = Pattern.compile("application-([A-Za-z]+)\\.properties");

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject javaProject, File outputFolder) {
		List<IConfigSource> configSources = new ArrayList<>();
		for (File file : outputFolder.listFiles()) {
			if (file.isFile()) {
				String fileName = file.getName();
				IConfigSource configSource = createConfigSource(fileName, javaProject);
				if (configSource != null) {
					configSources.add(configSource);
				}
			}
		}
		return configSources;
	}

	private static IConfigSource createConfigSource(String fileName, IJavaProject javaProject) {
		if (APPLICATION_PROPERTIES_FILE.equals(fileName)) {
			return new PropertiesConfigSource(fileName, 250, javaProject);
		}
		if (APPLICATION_YAML_FILE.equals(fileName) || APPLICATION_YML_FILE.equals(fileName)) {
			return new YamlConfigSource(fileName, javaProject);
		}
		Matcher m = PER_PROFILE_FILE_NAME_PTN.matcher(fileName);
		if (m.matches()) {
			// I don't think Quarkus assigns a specific ordinal to
			// application-${profile}.properties files.
			// This ordinal means that application-${profile}.properties files take
			// precedence over application.properties
			return new PropertiesConfigSource(fileName, m.group(1), 261, javaProject);
		}
		return null;
	}

	@Override
	public boolean isConfigSource(String fileName) {
		return APPLICATION_PROPERTIES_FILE.equals(fileName) || APPLICATION_YAML_FILE.equals(fileName)
				|| APPLICATION_YML_FILE.equals(fileName) || PER_PROFILE_FILE_NAME_PTN.matcher(fileName).matches();
	}
}
