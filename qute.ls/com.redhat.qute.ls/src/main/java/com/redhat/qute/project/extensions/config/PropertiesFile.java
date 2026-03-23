/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Properties file.
 */
public class PropertiesFile {

	private final Path file;

	private final Properties properties;

	public PropertiesFile(Path file) {
		this.file = file;
		properties = new Properties();
		if (Files.exists(file)) {
			try (InputStream input = Files.newInputStream(file)) {
				properties.load(input);
			} catch (IOException e) {
				// throw new RuntimeException("Failed to load properties file: " + file, e);
			}
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

}
