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
package com.redhat.qute.project.extensions.renarde;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a messages.properties file with its parsed content.
 * 
 * <p>
 * Manages loading and reloading of Java properties files used for
 * internationalization (i18n) in Renarde applications.
 * </p>
 * 
 * @author Angelo ZERR
 */
public class MessagesFileInfo {

	public static class MessagesFileName {

		private final String fileName;
		private final String locale;

		public MessagesFileName(String fileName, String locale) {
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
	private final Path messagesFile;
	private final MessagesFileName messagesFileName;

	private boolean defaultFile;

	/**
	 * Creates a new messages file info and loads its content.
	 * 
	 * @param messagesFile     the path to the messages.properties file
	 * @param messagesFileName
	 * @throws FileNotFoundException if the file does not exist
	 * @throws IOException           if an error occurs reading the file
	 */
	public MessagesFileInfo(Path messagesFile, MessagesFileName messagesFileName) throws IOException {
		this.messagesFile = messagesFile;
		this.messagesFileName = messagesFileName;
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
	 * Returns the path to the messages.properties file.
	 * 
	 * @return the file path
	 */
	public Path getMessagesFile() {
		return messagesFile;
	}

	/**
	 * Returns the properties loaded from this file.
	 * 
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	public String getLocale() {
		return messagesFileName.getLocale();
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
		try (Reader reader = Files.newBufferedReader(messagesFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
	}

	public boolean isDefaultFile() {
		return defaultFile;
	}

	public void setDefaultFile(boolean defaultFile) {
		this.defaultFile = defaultFile;
	}

	public static MessagesFileName getMessagesFileName(Path filePath, Set<Path> sourcePaths) {
		if (sourcePaths == null || sourcePaths.isEmpty()) {
			return null;
		}

		MessagesFileName fileName = getMessagesFileName(filePath);
		if (fileName == null) {
			return null;
		}
		// Check if file is stored in src/main/resources
		Path baseDir = filePath.getParent();
		for (Path sourcePath : sourcePaths) {
			if (sourcePath.equals(baseDir)) {
				return fileName;
			}
		}
		return null;
	}

	public static MessagesFileName getMessagesFileName(Path filePath) {
		String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
		if (fileName.equals("messages.properties")) {
			return new MessagesFileName(fileName, null);
		} else {
			if (fileName.startsWith("messages_")) {
				int start = 9;
				int end = fileName.indexOf(".properties", start);
				if (end != -1) {
					String locale = fileName.substring(start, end);
					return new MessagesFileName(fileName, locale);
				}
			}
		}
		return null;
	}

}