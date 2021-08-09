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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
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
	private static final Logger LOGGER = Logger.getLogger(QuarkusConfigSourceProvider.class.getName());

	@Override
	public List<IConfigSource> getConfigSources(IJavaProject project) {
		List<IConfigSource> quarkusConfigSources = new ArrayList<>(3);
		collectStaticConfigSources(quarkusConfigSources, project);
		collectProfileConfigSources(quarkusConfigSources, project);
		return quarkusConfigSources;
	}

	private static void collectStaticConfigSources(List<IConfigSource> quarkusConfigSources, IJavaProject project) {
		quarkusConfigSources.add(new YamlConfigSource(APPLICATION_YAML_FILE, project));
		quarkusConfigSources.add(new YamlConfigSource(APPLICATION_YML_FILE, project));
		quarkusConfigSources.add(new PropertiesConfigSource(APPLICATION_PROPERTIES_FILE, project, 250));
	}

	private static void collectProfileConfigSources(List<IConfigSource> quarkusConfigSources, IJavaProject project) {
		File classesFolder = getClassesFolder(project);
		if (classesFolder != null) {
			for (File file : classesFolder.listFiles()) {
				if (file.isFile()) {
					Matcher m = PER_PROFILE_FILE_NAME_PTN.matcher(file.getName());
					if (m.matches()) {
						quarkusConfigSources
								.add(new ApplicationProfileConfigSource(file.getName(), m.group(1), project));
					}
				}
			}
		}
	}

	/**
	 * Returns the folder where the classes are placed for the given project and
	 * null if there is none
	 *
	 * @param project the project to find the resources folder for
	 * @return the folder where the classes are placed for the given project and
	 *         null if there is none
	 */
	private static File getClassesFolder(IJavaProject project) {
		try {
			for (IClasspathEntry sourceEntry : ((JavaProject) project).getResolvedClasspath(true)) {
				if (!sourceEntry.isTest() && sourceEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE
						&& sourceEntry.getOutputLocation() != null) {
					IPath output = sourceEntry.getOutputLocation();
					File outputFile = project.getProject().getLocation().append(output.removeFirstSegments(1)).toFile();
					if (outputFile.exists() && outputFile.isDirectory()) {
						return outputFile;
					}
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.WARNING, "Error while finding the resources folder", e);
		}
		return null;
	}

}
