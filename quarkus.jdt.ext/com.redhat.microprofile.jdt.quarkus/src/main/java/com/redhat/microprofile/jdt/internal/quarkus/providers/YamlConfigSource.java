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

import com.redhat.microprofile.jdt.internal.quarkus.utils.YamlUtils;

/**
 * Yaml config source implementation
 *
 * @author dakwon
 *
 */
public class YamlConfigSource extends PropertiesConfigSource {

	public YamlConfigSource(String configFileName, IJavaProject javaProject) {
		super(configFileName, javaProject);
	}

	@Override
	protected Properties loadConfig(InputStream input) throws IOException {
		// Convert Yaml document into flattern properties
		return YamlUtils.loadYamlAsProperties(input);
	}

	@Override
	public int getOrdinal() {
		// See
		// https://github.com/quarkusio/quarkus/blob/main/extensions/config-yaml/runtime/src/main/java/io/quarkus/config/yaml/runtime/ApplicationYamlConfigSourceLoader.java#L29
		// (or Quarkus --> yaml config extension --> ApplicationYamlConfigSourceLoader
		// if the link is dead)
		return 255;
	}

}
