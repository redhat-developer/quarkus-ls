/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

/**
 * Config utilities to load properties, yaml config files.
 * 
 * @author Angelo ZERR
 *
 */
public class ConfigUtils {

	private static final String YML_FILE_EXT = "yml";
	private static final String YAML_FILE_EXT = "yaml";
	private static final String PROPERTIES_FILE_EXT = "properties";

	/**
	 * Returns the value of the given property name declared in the given file (ex :
	 * application.properties, application.yaml) and null otherwise.
	 * 
	 * @param file         the file or null.
	 * @param propertyName the property name.
	 * @return the value of the given property name declared in the given file (ex :
	 *         application.properties, application.yaml) and null otherwise.
	 */
	public static String getProperty(IFile file, String propertyName) {
		if (file == null) {
			return null;
		}
		if (PROPERTIES_FILE_EXT.equals(file.getFileExtension())) {
			try {
				Properties properties = loadConfig(file.getContents());
				return getProperty(propertyName, properties);
			} catch (Exception e) {
				return null;
			}
		} else if (YAML_FILE_EXT.equals(file.getFileExtension()) || YML_FILE_EXT.equals(file.getFileExtension())) {
			// TODO : load YAML
		}
		return null;
	}

	/**
	 * Load the file properties.
	 * 
	 * @param input the stream of file properties.
	 * @return the loaded properties.
	 * @throws IOException
	 */
	public static Properties loadConfig(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}

	/**
	 * Returns the value of the given <code>key</code> of the given
	 * <code>properties</code> and null otherwise.
	 * 
	 * @param key        the property key.
	 * @param properties the properties.
	 * @return the value of the given <code>key</code> of the given
	 *         <code>properties</code> and null otherwise.
	 */
	public static String getProperty(String key, Properties properties) {
		return properties.getProperty(key);
	}

}
