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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4mp.jdt.core.project.PropertiesConfigSource;

/**
 * Represents <code>application-${profile}.properties</code> config sources.
 *
 * This config source contributes configuration only for a particular profile.
 * It takes precedence over <code>application.properties</code>.
 *
 */
public class ApplicationProfileConfigSource extends PropertiesConfigSource {

	private final String profile;

	public ApplicationProfileConfigSource(String configFileName, String profile, IJavaProject javaProject) {
		// I don't think Quarkus assigns a specific ordinal to
		// application-${profile}.properties files.
		// This ordinal means that application-${profile}.properties files take
		// precedence over application.properties
		super(configFileName, javaProject, 261);
		this.profile = profile;
	}

	@Override
	protected Properties loadConfig(InputStream input) throws IOException {
		// Prefix all properties with profile
		Properties properties = new Properties();
		properties.load(input);
		Properties adjustedProperties = new Properties();
		properties.forEach((key, val) -> {
			// Ignore any properties with a profile,
			// since they are not valid
			if (!((String) key).startsWith("%")) {
				adjustedProperties.putIfAbsent("%" + profile + "." + key, val);
			}
		});
		return adjustedProperties;
	}

}
