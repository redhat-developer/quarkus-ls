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
import java.nio.file.Path;

import com.redhat.qute.project.extensions.config.PropertiesFile.PropertiesFileName;

/**
 * Registry for application.properties files.
 *
 * <p>
 * Manages application.properties and application-{profile}.properties files.
 * </p>
 */
public class ApplicationPropertiesFileRegistry extends PropertiesFileRegistry<PropertiesFile> {

	@Override
	protected PropertiesFileName getPropertiesFileName(Path filePath) {
		String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
		if (fileName.equals("application.properties")) {
			return new PropertiesFileName(fileName, null);
		} else {
			if (fileName.startsWith("application-")) {
				int start = 12;
				int end = fileName.indexOf(".properties", start);
				if (end != -1) {
					String locale = fileName.substring(start, end);
					return new PropertiesFileName(fileName, locale);
				}
			}
		}
		return null;
	}

	@Override
	protected PropertiesFile createPropertiesFile(Path propertiesFile, PropertiesFileName propertiesFileName)
			throws IOException {
		return new PropertiesFile(propertiesFile, propertiesFileName);
	}

}
