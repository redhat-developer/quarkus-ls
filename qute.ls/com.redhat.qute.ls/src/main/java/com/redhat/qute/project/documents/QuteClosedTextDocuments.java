/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.documents;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;

/**
 * Registry which stores Qute template document closed
 * {@link QuteClosedTextDocument}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteClosedTextDocuments {

	private static final Logger LOGGER = Logger.getLogger(QuteClosedTextDocuments.class.getName());

	private final QuteProject project;

	private final Map<String /* template id */, QuteTextDocument> documents;

	private boolean scanned;

	public QuteClosedTextDocuments(QuteProject project, Map<String, QuteTextDocument> documents) {
		this.project = project;
		this.documents = documents;
	}

	/**
	 * Scan if needed all files (html, json, txt, yaml) from the
	 * 'src/main/resources/templates' folder of the project which are closed.
	 */
	public void loadClosedTemplatesIfNeeded() {
		if (scanned) {
			return;
		}
		scan();
	}

	/**
	 * Scan all files (html, json, txt, yaml) from the
	 * 'src/main/resources/templates' folder of the project which are closed.
	 */
	private synchronized void scan() {
		if (scanned) {
			return;
		}

		List<TemplateRootPath> rootPaths = project.getTemplateRootPaths();
		for (TemplateRootPath templateRootPath : rootPaths) {
			scan(templateRootPath.getBasePath());
		}
		scanned = true;
	}

	private void scan(Path basePath) {
		if (basePath == null) {
			return;
		}
		if (!Files.exists(basePath)) {
			// The Qute project doesn't contain the src/main/resources/templates directory
			return;
		}
		// Scan all directories from src/main/resources/templates directory to collect
		// closed Templates
		try {
			Files.walk(basePath).forEach(path -> {
				try {
					tryToAddClosedTemplate(path, false);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error while loading template '" + FileUtils.toUri(path) + "'.", e);
				}
			});
		} catch (Exception e) {
			// Do nothing
		}
	}

	/**
	 * Try to add closed template in the cache.
	 * 
	 * @param path  the template file path.
	 * @param force true if cache must be updated even if it exists an opened
	 *              document and false otherwise.
	 * 
	 * @return the closed document and null otherwise.
	 */
	private QuteTextDocument tryToAddClosedTemplate(Path path, boolean force) {
		if (!isValidTemplate(path)) {
			return null;
		}
		String templateId = project.getTemplateId(path);
		if (force || !project.isTemplateOpened(templateId)) {
			// The template is opened or force is true
			synchronized (documents) {
				if (!force) {
					QuteTextDocument document = documents.get(templateId);
					if (document != null && !document.isOpened()) {
						// The closed document already exists
						return document;
					}
				}
				// Create and cache the closed document.
				QuteTextDocument document = new QuteClosedTextDocument(path, templateId, project);
				documents.put(templateId, document);
				return document;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given path is a valid Qute template (*.html, *.txt,
	 * *.yaml, *.json) and false otherwise.
	 * 
	 * @param path the file/directory path.
	 * 
	 * @return true if the given path is a valid Qute template (*.html, *.txt,
	 *         *.yaml, *.json) and false otherwise.
	 */
	private boolean isValidTemplate(Path path) {
		if (Files.isDirectory(path)) {
			return false;
		}
		String uri = path.toString();
		for (String variant : project.getTemplateVariants()) {
			if (!variant.isEmpty() && uri.endsWith(variant)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Callback called when a Qute text file is created.
	 * 
	 * @param path the template file path.
	 */
	public QuteTextDocument onDidCreateTemplate(Path path) {
		return tryToAddClosedTemplate(path, true);
	}

	/**
	 * Callback called when a Qute text file is deleted.
	 * 
	 * @param path the template file path.
	 */
	public QuteTextDocument onDidDeleteTemplate(Path path) {
		String templateId = project.getTemplateId(path);
		synchronized (documents) {
			return documents.remove(templateId);
		}
	}

	/**
	 * Callback called when a Qute text document is closed (when the editor is
	 * closed).
	 * 
	 * @param path the template file path.
	 */
	public void onDidCloseTemplate(Path path) {
		onDidCreateTemplate(path);
	}

}
