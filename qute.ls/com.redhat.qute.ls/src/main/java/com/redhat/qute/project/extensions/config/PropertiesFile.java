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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Represents a properties file with its parsed content.
 * 
 * <p>
 * Manages loading and reloading of Java properties file.
 * </p>
 * 
 * @author Angelo ZERR
 */
public class PropertiesFile implements Comparable<PropertiesFile> {

	/**
	 * Holds properties file name information including locale.
	 */
	public static class PropertiesFileName {

		private final String fileName;
		private final String locale;

		public PropertiesFileName(String fileName, String locale) {
			this.fileName = fileName;
			this.locale = locale;
		}

		public String getFileName() {
			return fileName;
		}

		public String getLocale() {
			return locale;
		}
	}

	private final Properties properties;
	private final Path propertiesFile;
	private final PropertiesFileName propertiesFileName;

	private boolean defaultFile;

	/**
	 * Creates a new messages file info and loads its content.
	 * 
	 * @param messagesFile       the path to the messages.properties file
	 * @param propertiesFileName
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException           if an error occurs reading the file
	 */
	public PropertiesFile(Path messagesFile, PropertiesFileName propertiesFileName) throws IOException {
		this.propertiesFile = messagesFile;
		this.propertiesFileName = propertiesFileName;
		properties = new Properties();
		reload();
	}

	/**
	 * Checks if a message key exists in this properties file.
	 * 
	 * @param messageKey the message key to check (e.g., "main.login")
	 * @return true if the key exists
	 */
	public boolean hasMessage(String messageKey) {
		return properties.containsKey(messageKey);
	}

	/**
	 * Returns the path to the.properties file.
	 * 
	 * @return the file path
	 */
	public Path getPropertiesFile() {
		return propertiesFile;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getLocale() {
		return propertiesFileName.getLocale();
	}

	/**
	 * Reloads the properties from the file.
	 * 
	 * <p>
	 * Called when the file is modified to refresh the in-memory content.
	 * </p>
	 * 
	 * @throws IOException if an error occurs reading the file
	 */
	public void reload() throws IOException {
		properties.clear();
		try (Reader reader = Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
	}

	public boolean isDefaultFile() {
		return defaultFile;
	}

	public void setDefaultFile(boolean defaultFile) {
		this.defaultFile = defaultFile;
	}
	
	public Properties getProperties() {
		return properties;
	}

	@Override
	public int compareTo(PropertiesFile other) {
		// Default file (no locale) always comes first
		if (this.isDefaultFile() && !other.isDefaultFile()) {
			return -1;
		}
		if (!this.isDefaultFile() && other.isDefaultFile()) {
			return 1;
		}
		// Then sort alphabetically by locale
		String thisLocale = this.getLocale() != null ? this.getLocale() : "";
		String otherLocale = other.getLocale() != null ? other.getLocale() : "";
		return thisLocale.compareTo(otherLocale);
	}

}