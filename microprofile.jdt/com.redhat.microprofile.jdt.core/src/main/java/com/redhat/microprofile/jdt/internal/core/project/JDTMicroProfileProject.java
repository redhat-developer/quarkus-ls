/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

/**
 * JDT MicroProfile project.
 * 
 * @author Angelo ZERR
 *
 */
public class JDTMicroProfileProject {

	public static final String MICROPROFILE_CONFIG_PROPERTIES_FILE = "META-INF/microprofile-config.properties";
	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";

	private final List<IConfigSource> configSources;

	private static final String QUARKUS_HTTP_PORT = "quarkus.http.port";

	private static final int DEFAULT_PORT = 8080;

	public JDTMicroProfileProject(IJavaProject javaProject) {
		this.configSources = new ArrayList<IConfigSource>(2);
		configSources.add(new PropertiesConfigSource(APPLICATION_PROPERTIES_FILE, javaProject));
		configSources.add(new PropertiesConfigSource(MICROPROFILE_CONFIG_PROPERTIES_FILE, javaProject));
	}

	/**
	 * Returns the server port.
	 * 
	 * @return the server port.
	 */
	public int getServerPort() {
		return getPropertyAsInteger(QUARKUS_HTTP_PORT, DEFAULT_PORT);
	}

	public String getProperty(String key, String defaultValue) {
		for (IConfigSource configSource : configSources) {
			String property = configSource.getProperty(key);
			if (property != null) {
				return property;
			}
		}
		return defaultValue;
	}

	public Integer getPropertyAsInteger(String key, Integer defaultValue) {
		for (IConfigSource configSource : configSources) {
			Integer property = configSource.getPropertyAsInt(key);
			if (property != null) {
				return property;
			}
		}
		return defaultValue;
	}
}