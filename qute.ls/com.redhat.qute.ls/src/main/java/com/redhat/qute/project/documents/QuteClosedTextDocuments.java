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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.project.ProgressContext;
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

	private final QuteProject project;

	private final Map<String /* template id */, QuteTextDocument> allDocumentsByTemplateId;

	private boolean scanned;

	public QuteClosedTextDocuments(QuteProject project, Map<String, QuteTextDocument> allDocumentsByTemplateId) {
		this.project = project;
		this.allDocumentsByTemplateId = allDocumentsByTemplateId;
	}

	public void loadClosedTemplatesIfNeeded() {
		loadClosedTemplatesIfNeeded(null);
	}

	/**
	 * Scan if needed all files (html, json, txt, yaml) from the
	 * 'src/main/resources/templates' folder of the project which are closed.
	 */
	public void loadClosedTemplatesIfNeeded(ProgressContext progressContext) {
		if (scanned) {
			return;
		}
		scan(progressContext);
	}

	/**
	 * Scan all files (html, json, txt, yaml) from the
	 * 'src/main/resources/templates' folder of the project which are closed.
	 */
	private synchronized void scan(ProgressContext progressContext) {
		if (scanned) {
			return;
		}

		if (progressContext != null) {
			progressContext.report("Collecting template files", 5);
		}
		// Step 1: collect all template file path from all root paths
		// (src/main/resources/templates, src/main/resources/content, etc)
		List<Path> rootPaths = project.getTemplateRootPaths() //
				.stream() //
				.map(TemplateRootPath::getBasePath) //
				.collect(Collectors.toList());
		List<Path> templatePaths = collectTemplatePaths(rootPaths);

		int totalFiles = templatePaths.size();
		if (progressContext != null) {
			progressContext.report("Parsing the " + totalFiles + " template files", 5);
		}
		// Step 2: loop for each template path to parse file as Qute template
		for (int i = 0; i < totalFiles; i++) {
			Path path = templatePaths.get(i);
			if (progressContext != null) {
				int percent = (i * 100) / totalFiles;
				progressContext.report("Parsing " + path.getFileName() + " template (" + i + "/" + totalFiles + ")",
						percent);
			}
			tryToAddClosedTemplate(path, false);
		}
		scanned = true;
	}

	private List<Path> collectTemplatePaths(List<Path> rootPaths) {
		List<Path> templatePaths = new ArrayList<>();
		for (Path rootPath : rootPaths) {
			try (Stream<Path> stream = Files.walk(rootPath)) {
				stream.forEach(path -> {
					if (isValidTemplate(path)) {
						templatePaths.add(path);
					}
				});
			} catch (Exception e) {
				// Do nothing
			}
		}
		return templatePaths;
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
			synchronized (allDocumentsByTemplateId) {
				if (!force) {
					QuteTextDocument document = allDocumentsByTemplateId.get(templateId);
					if (document != null && !document.isOpened()) {
						// The closed document already exists
						return document;
					}
				}
				// Create and cache the closed document.
				QuteTextDocument document = new QuteClosedTextDocument(path, templateId, project);
				allDocumentsByTemplateId.put(templateId, document);
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
	 * Callback called when a Qute text document is closed (when the editor is
	 * closed).
	 * 
	 * @param path the template file path.
	 */
	public void onDidCloseTemplate(Path path) {
		onDidCreateTemplate(path);
	}

}
