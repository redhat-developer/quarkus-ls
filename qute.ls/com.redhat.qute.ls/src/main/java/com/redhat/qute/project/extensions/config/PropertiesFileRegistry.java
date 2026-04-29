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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.lsp4j.FileChangeType;

import com.redhat.qute.commons.config.PropertyConfig;
import com.redhat.qute.project.extensions.config.PropertiesFile.PropertiesFileName;

/**
 * Base registry for managing properties files.
 *
 * <p>
 * Handles loading, reloading, and querying of properties files with file
 * watching support.
 * </p>
 *
 * @param <T> the properties file type
 */
public abstract class PropertiesFileRegistry<T extends PropertiesFile> {

	private static final Logger LOGGER = Logger.getLogger(PropertiesFileRegistry.class.getName());

	private final List<T> propertiesFiles;

	public PropertiesFileRegistry() {
		propertiesFiles = new ArrayList<>();
	}

	/**
	 * Scans source folders for properties.properties files.
	 * 
	 * @param sourcePaths the source paths to scan
	 */
	public void load(Set<Path> sourcePaths) {
		for (Path sourcePath : sourcePaths) {
			if (Files.exists(sourcePath)) {
				try (Stream<Path> stream = Files.list(sourcePath)) {
					stream.forEach(this::loadPropertiesFile);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error scanning source folder: " + sourcePath, e);
				}
			}
		}
	}

	/**
	 * Loads a properties file if it matches the naming pattern.
	 * 
	 * @param filePath the file path to check and load
	 */
	private void loadPropertiesFile(Path filePath) {
		PropertiesFileName propertiesFileName = getPropertiesFileName(filePath);
		if (propertiesFileName != null) {
			try {
				T propertiesFile = createPropertiesFile(filePath, propertiesFileName);
				// TODO: improve default by reading 'quarkus.default-locale=en' from
				// application.properties
				propertiesFile.setDefaultFile(propertiesFile.getLocale() == null);
				propertiesFiles.add(propertiesFile);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error loading properties file: " + filePath, e);
			}
		}
	}

	/**
	 * Gets a configuration property value.
	 *
	 * @param property the property config
	 * @return the property value, or the default value if not found
	 */
	public String getConfig(PropertyConfig property) {
		for (PropertiesFile propertiesFile : propertiesFiles) {
			String value = propertiesFile.getProperty(property.getName());
			if (value != null) {
				return value;
			}
		}
		return property.getDefaultValue();
	}

	/**
	 * Handles file system changes for properties files.
	 *
	 * @param filePath    the changed file path
	 * @param sourcePaths the source paths to check
	 * @param changeTypes the types of changes
	 * @return true if a properties file was affected
	 */
	public boolean didChangeWatchedFile(Path filePath, Set<Path> sourcePaths, Set<FileChangeType> changeTypes) {
		PropertiesFileName propertiesFileName = getPropertiesFileName(filePath, sourcePaths);
		if (propertiesFileName == null) {
			return false;
		}

		PropertiesFile file = findPropertiesFile(filePath);
		boolean fileDeleted = changeTypes.contains(FileChangeType.Deleted);

		try {
			if (file != null) {
				if (fileDeleted) {
					propertiesFiles.remove(file);
				} else {
					file.reload();
				}
			} else if (!fileDeleted) {
				propertiesFiles.add(createPropertiesFile(filePath, propertiesFileName));
			}
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error handling file change: " + filePath, e);
			return false;
		}
	}

	/**
	 * Finds a properties file info by filename.
	 * 
	 * @param name the filename to search for
	 * @return the properties file info, or null if not found
	 */
	private PropertiesFile findPropertiesFile(Path filePath) {
		return propertiesFiles.stream().filter(info -> {
			Path path = info.getPropertiesFile();
			// String fileName = path.getName(path.getNameCount() - 1).toString();
			// return name.equals(fileName);
			return filePath.equals(path);
		}).findFirst().orElse(null);
	}

	/**
	 * Gets properties file name info if the file is in source paths.
	 *
	 * @param filePath    the file path
	 * @param sourcePaths the source paths to check
	 * @return the properties file name info, or null if not in source paths
	 */
	public PropertiesFileName getPropertiesFileName(Path filePath, Set<Path> sourcePaths) {
		if (sourcePaths == null || sourcePaths.isEmpty()) {
			return null;
		}

		PropertiesFileName fileName = getPropertiesFileName(filePath);
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

	public void clearFiles() {
		propertiesFiles.clear();
	}

	public boolean hasFiles() {
		return !propertiesFiles.isEmpty();
	}

	public void sortFiles(Comparator<T> comparator) {
		propertiesFiles.sort(comparator);
	}

	public List<T> getPropertiesFiles() {
		return propertiesFiles;
	}

	/**
	 * Extracts properties file name info from a file path.
	 *
	 * @param filePath the file path
	 * @return the properties file name info, or null if not a valid properties file
	 */
	protected abstract PropertiesFileName getPropertiesFileName(Path filePath);

	/**
	 * Creates a properties file instance.
	 *
	 * @param messagesFile       the file path
	 * @param propertiesFileName the file name info
	 * @return the properties file instance
	 * @throws IOException if an error occurs reading the file
	 */
	protected abstract T createPropertiesFile(Path messagesFile, PropertiesFileName propertiesFileName)
			throws IOException;
}
