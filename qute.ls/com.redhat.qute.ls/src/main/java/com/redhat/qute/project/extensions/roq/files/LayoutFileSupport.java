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
package com.redhat.qute.project.extensions.roq.files;

import static com.redhat.qute.services.completions.QuteCompletionForTemplateIds.createTemplateIds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.utils.StringUtils;

/**
 * Layout files support.
 */
public class LayoutFileSupport extends RoqFileSupport {

	private static final String THEME_VAR = ":theme/";

	public LayoutFileSupport(RoqProjectExtension roq) {
		super(roq);
	}

	/**
	 * Collect layouts and theme layouts both.
	 * 
	 * @param filePath
	 * @param dataModelProject
	 * @param collector
	 */
	public void collectLayouts(Path filePath, ExtendedDataModelProject dataModelProject,
			RoqFileSupport.FileCollector collector) {
		// 1. collect layouts from sources:
		// - /templates/layouts
		// - /src/main/resources/templates/layouts
		collectLayouts(collector, dataModelProject);

		// 2. collect theme layouts from sources (with :/theme syntax)
		// - /templates/theme-layouts +
		// - /src/main/resources/templates/theme-layouts
		collectSourceThemeLayouts(collector, getDataModelProject());

		// 3. collect theme layouts from binaries (with :/theme syntax)
		collectBinaryThemeLayouts(collector, dataModelProject);
	}

	private void collectLayouts(RoqFileSupport.FileCollector collector, ExtendedDataModelProject dataModelProject) {
		// 1. collect layouts from /templates/layouts
		Path projectFolder = dataModelProject.getProjectFolder();
		if (projectFolder != null) {
			Path layoutsFolder = projectFolder.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}

		// 2. collect layouts from /src/main/resources/templates/layouts
		Set<Path> sourcePaths = dataModelProject.getSourcePaths();
		for (Path sourcePath : sourcePaths) {
			Path layoutsFolder = sourcePath.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}
	}

	private void collectSourceThemeLayouts(RoqFileSupport.FileCollector collector,
			ExtendedDataModelProject dataModelProject) {
		// 1. collect layouts from /templates/theme-layouts
		Path projectFolder = dataModelProject.getProjectFolder();
		if (projectFolder != null) {
			Path layoutsFolder = projectFolder.resolve(TEMPLATES_FOLDER).resolve(THEME_LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}

		// 2. collect layouts from /src/main/resources/templates/theme-layouts
		Set<Path> sourcePaths = dataModelProject.getSourcePaths();
		for (Path sourcePath : sourcePaths) {
			Path layoutsFolder = sourcePath.resolve(TEMPLATES_FOLDER).resolve(THEME_LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}
	}

	private void collectBinaryThemeLayouts(RoqFileSupport.FileCollector collector,
			ExtendedDataModelProject dataModelProject) {
		String currentTheme = getRoq().getCurrentTheme();
		Map<String, List<QuteTextDocument>> templateIds = createTemplateIds(dataModelProject.getBinaryDocuments(),
				null);
		for (Map.Entry<String, List<QuteTextDocument>> ids : templateIds.entrySet()) {
			List<QuteTextDocument> documentsForId = ids.getValue();
			if (documentsForId.size() == 1) {
				// One document (ex : base.html) matches the short syntax temple id (ex : base)
				// here completion shows 'base' short syntax.
				String templateId = ids.getKey();
				addThemeLayoutTemplateIdForLayout(templateId, documentsForId.get(0).getOrigin(), currentTheme,
						collector);
			} else {
				// Several documents (ex : base.html, base.txt) matches the short syntax temple
				// id (ex : base)
				// here we generate a completion per document by using the template id of the
				// document (ex : base.html, base.txt).
				for (QuteTextDocument document : documentsForId) {
					String templateId = document.getTemplateId();
					String origin = document.getOrigin();
					addThemeLayoutTemplateIdForLayout(templateId, origin, currentTheme, collector);
				}
			}
		}
	}

	private void addThemeLayoutTemplateIdForLayout(String templateId, String origin, String currentTheme,
			RoqFileSupport.FileCollector collector) {
		// 1. collect template from /theme-layouts folder
		if (templateId.startsWith(THEME_LAYOUTS_FOLDER_WITH_SLASH)) {
			collector.collect(null, null, templateId, true, origin);
		}

		// 2. collect template with :theme syntax
		for (String theme : getAvailableThemes()) {
			String start = THEME_LAYOUTS_FOLDER_WITH_SLASH + theme + "/";
			if (templateId.startsWith(start)) {
				// Generate :/theme/some-layout and some-layout both
				String relativeTemplateId = templateId.substring(start.length());
				String relativeTemplateIdWithThemeSyntax = THEME_VAR + relativeTemplateId;
				collector.collect(null, null, relativeTemplateId, true, origin);
				collector.collect(null, null, relativeTemplateIdWithThemeSyntax, true, origin);
			}
		}
	}

	public TemplatePath getLayoutPath(Path filePath, String layoutFileName, ExtendedDataModelProject dataModelProject) {
		if (layoutFileName.startsWith(THEME_VAR)) {
			// :theme/some-layout
			String currentTheme = getRoq().getCurrentTheme();
			if (!StringUtils.isEmpty(currentTheme)) {
				// :theme/some-layout --> theme-layouts/roq-default/some-layout
				String resolvedLayout = resolveThemeLayout(layoutFileName, currentTheme);
				String uri = dataModelProject.findTemplateUriByTemplateId(resolvedLayout);
				if (uri != null) {
					return new TemplatePath(uri, resolvedLayout, true);
				}
			}
			return new TemplatePath((String) null, layoutFileName, false);
		}

		// 1. Template from source
		TemplatePath sourcePath = getLayoutPathFromSource(layoutFileName, dataModelProject);
		if ((sourcePath == null || !sourcePath.isExists())) {
			// 2. template from binary
			String currentTheme = getRoq().getCurrentTheme();
			String resolvedLayout = resolveThemeLayout(layoutFileName, currentTheme);
			String binaryUri = dataModelProject.findTemplateUriByTemplateId(resolvedLayout);
			if (binaryUri != null) {
				return new TemplatePath(binaryUri, resolvedLayout, true);
			}
		}
		return sourcePath;
	}

	private String resolveThemeLayout(String layoutFileName, String currentTheme) {
		String baseDir = THEME_LAYOUTS_FOLDER_WITH_SLASH + currentTheme + "/";
		if (layoutFileName.startsWith(THEME_VAR)) {
			return baseDir + layoutFileName.substring(THEME_VAR.length());
		}
		if (layoutFileName.startsWith(baseDir)) {
			return layoutFileName;
		}
		return baseDir + layoutFileName;
	}

	private TemplatePath getLayoutPathFromSource(String layoutFileName, ExtendedDataModelProject dataModelProject) {
		Path projectFolder = dataModelProject.getProjectFolder();

		// 1. Collect existing templates folder (templates,
		// src/main/resources/templates)

		// 1.1 templates
		List<Path> existingTemplatesFolder = new ArrayList<>();
		if (projectFolder != null) {
			Path templatesFolder = projectFolder.resolve(TEMPLATES_FOLDER);
			if (Files.exists(templatesFolder)) {
				existingTemplatesFolder.add(templatesFolder);
			}
		}
		// 1.2 src/main/resources/templates
		for (Path sourcePath : dataModelProject.getSourcePaths()) {
			Path templatesFolder = sourcePath.resolve(TEMPLATES_FOLDER);
			if (Files.exists(templatesFolder)) {
				existingTemplatesFolder.add(templatesFolder);
			}
		}

		if (existingTemplatesFolder.isEmpty()) {
			// templates folder doesn't exist
			Path baseDir = projectFolder != null ? projectFolder : null;
			if (baseDir == null) {
				if (dataModelProject.getSourcePaths().isEmpty()) {
					return null;
				}
				baseDir = dataModelProject.getSourcePaths().iterator().next();
			}
			return new TemplatePath(
					baseDir.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER).resolve(layoutFileName + HTML_EXTENSION),
					layoutFileName);
		}

		// 2. Collect existing templates/layouts folder (templates,
		// src/main/resources/templates)

		// 2.1 templates
		List<Path> existingLayoutFolder = new ArrayList<>();
		for (Path templatesFolder : existingTemplatesFolder) {
			Path layoutsFolder = templatesFolder.resolve(LAYOUTS_FOLDER);
			if (Files.exists(layoutsFolder)) {
				existingLayoutFolder.add(layoutsFolder);
			}
		}

		// layouts folder doesn't exist
		if (existingLayoutFolder.isEmpty()) {
			return new TemplatePath(
					existingTemplatesFolder.get(0).resolve(LAYOUTS_FOLDER).resolve(layoutFileName + HTML_EXTENSION),
					layoutFileName);
		}

		for (Path layoutsFolder : existingLayoutFolder) {
			Path layoutFile = layoutsFolder.resolve(layoutFileName + HTML_EXTENSION);
			if (Files.exists(layoutFile)) {
				return new TemplatePath(layoutFile, layoutFileName, true);
			}
		}

		return new TemplatePath(existingLayoutFolder.get(0).resolve(layoutFileName + HTML_EXTENSION), layoutFileName);
	}

	private Set<String> getAvailableThemes() {
		return getRoq().getAvailableThemes();
	}
}
