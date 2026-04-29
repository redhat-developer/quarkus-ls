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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.project.extensions.config.PropertiesFile.PropertiesFileName;

/**
 * Registry for .qute configuration files associated with template root paths.
 *
 * <p>
 * Manages .qute files marking template root directories and their configurations.
 * </p>
 */
public class DotQuteFileRegistry {

	private static final Logger LOGGER = Logger.getLogger(DotQuteFileRegistry.class.getName());

	private final Map<TemplateRootPath, DotQuteFile> dotQuteFiles;

	public DotQuteFileRegistry() {
		this.dotQuteFiles = new HashMap<>();
	}

	/**
	 * Loads .qute files from template root paths and updates their altExprSyntax property.
	 *
	 * @param templateRootPaths the template root paths to scan
	 */
	public void load(List<TemplateRootPath> templateRootPaths) {
		if (templateRootPaths == null) {
			return;
		}
		for (TemplateRootPath templateRootPath : templateRootPaths) {
			loadDotQuteFile(templateRootPath);
		}
	}

	/**
	 * Loads a .qute file from a template root path if it exists and updates the template root path's altExprSyntax property.
	 *
	 * @param templateRootPath the template root path
	 */
	private void loadDotQuteFile(TemplateRootPath templateRootPath) {
		Path basePath = templateRootPath.getBasePath();
		if (basePath == null) {
			return;
		}
		Path quteFile = basePath.resolve(".qute");
		if (Files.exists(quteFile)) {
			try {
				PropertiesFileName propertiesFileName = new PropertiesFileName(".qute", null);
				DotQuteFile dotQuteFile = new DotQuteFile(quteFile, templateRootPath, propertiesFileName);
				dotQuteFiles.put(templateRootPath, dotQuteFile);
				// Update the template root path's altExprSyntax property
				templateRootPath.setAltExprSyntax(dotQuteFile.isAltExprSyntax());
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error loading .qute file: " + quteFile, e);
			}
		} else {
			// No .qute file, set to null to delegate to application.properties
			templateRootPath.setAltExprSyntax(null);
		}
	}

	/**
	 * Finds the .qute file for a given template root path.
	 *
	 * @param templateRootPath the template root path
	 * @return the .qute file, or null if none found
	 */
	public DotQuteFile findDotQuteFileFor(TemplateRootPath templateRootPath) {
		return dotQuteFiles.get(templateRootPath);
	}

	/**
	 * Handles file system changes for .qute files and updates the template root path's altExprSyntax property.
	 *
	 * @param filePath the changed file path
	 * @param templateRootPaths the template root paths
	 * @return true if a .qute file was affected
	 */
	public boolean didChangeWatchedFile(Path filePath, List<TemplateRootPath> templateRootPaths) {
		String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
		if (!".qute".equals(fileName)) {
			return false;
		}

		// Find which template root path contains this .qute file
		for (TemplateRootPath templateRootPath : templateRootPaths) {
			Path basePath = templateRootPath.getBasePath();
			if (basePath != null && filePath.equals(basePath.resolve(".qute"))) {
				try {
					if (Files.exists(filePath)) {
						// Reload the .qute file
						PropertiesFileName propertiesFileName = new PropertiesFileName(".qute", null);
						DotQuteFile dotQuteFile = new DotQuteFile(filePath, templateRootPath, propertiesFileName);
						dotQuteFiles.put(templateRootPath, dotQuteFile);
						// Update the template root path's altExprSyntax property
						templateRootPath.setAltExprSyntax(dotQuteFile.isAltExprSyntax());
					} else {
						// Remove the .qute file, set to null to delegate to application.properties
						dotQuteFiles.remove(templateRootPath);
						templateRootPath.setAltExprSyntax(null);
					}
					return true;
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Error handling .qute file change: " + filePath, e);
					return false;
				}
			}
		}
		return false;
	}

	public void clearFiles() {
		dotQuteFiles.clear();
	}

	public boolean hasFiles() {
		return !dotQuteFiles.isEmpty();
	}

}
