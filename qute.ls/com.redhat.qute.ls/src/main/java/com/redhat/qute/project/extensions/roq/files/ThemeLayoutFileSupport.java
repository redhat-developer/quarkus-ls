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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.utils.StringUtils;

/**
 * Theme Layout files support.
 */
public class ThemeLayoutFileSupport extends RoqFileSupport {

	public ThemeLayoutFileSupport(RoqProjectExtension roq) {
		super(roq);
	}

	public void collectThemeLayouts(ExtendedDataModelProject dataModelProject, RoqFileSupport.FileCollector collector) {
		// collect layout from theme-layouts binaries without :/theme syntax
		collectBinaryThemeLayouts(collector, dataModelProject);
	}

	private void collectBinaryThemeLayouts(RoqFileSupport.FileCollector collector,
			ExtendedDataModelProject dataModelProject) {
		String currentTheme = getRoq().getCurrentTheme();
		if (StringUtils.isEmpty(currentTheme)) {
			return;
		}

		String themeLayoutBase = THEME_LAYOUTS_FOLDER_WITH_SLASH + currentTheme + "/"; // ex: theme-layouts/roq-default/

		Map<String, List<QuteTextDocument>> templateIds = createTemplateIds(dataModelProject.getBinaryDocuments(),
				null);
		for (Map.Entry<String, List<QuteTextDocument>> ids : templateIds.entrySet()) {
			List<QuteTextDocument> documentsForId = ids.getValue();
			if (documentsForId.size() == 1) {
				// One document (ex : base.html) matches the short syntax temple id (ex : base)
				// here completion shows 'base' short syntax.
				String templateId = ids.getKey();
				addThemeLayoutTemplateIdForLayout(templateId, documentsForId.get(0).getOrigin(), themeLayoutBase,
						collector);
			} else {
				// Several documents (ex : base.html, base.txt) matches the short syntax temple
				// id (ex : base)
				// here we generate a completion per document by using the template id of the
				// document (ex : base.html, base.txt).
				for (QuteTextDocument document : documentsForId) {
					String templateId = document.getTemplateId();
					String origin = document.getOrigin();
					addThemeLayoutTemplateIdForLayout(templateId, origin, themeLayoutBase, collector);
				}
			}
		}
	}

	private void addThemeLayoutTemplateIdForLayout(String templateId, String origin, String themeLayoutBase,
			RoqFileSupport.FileCollector collector) {
		// 1. collect template from /theme-layouts folder
		if (templateId.startsWith(themeLayoutBase)) {
			collector.collect(null, null, templateId.substring(themeLayoutBase.length()), true, origin);
		}
	}

	public TemplatePath getLayoutPath(Path filePath, String layoutFileName, ExtendedDataModelProject dataModelProject) {
		String currentTheme = getRoq().getCurrentTheme();
		if (StringUtils.isEmpty(currentTheme)) {
			return null;
		}

		String resolvedlayoutFileName = THEME_LAYOUTS_FOLDER_WITH_SLASH + currentTheme + "/" + layoutFileName; // ex:
																												// theme-layouts/roq-default/
		String binaryUri = dataModelProject.findTemplateUriByTemplateId(resolvedlayoutFileName);
		if (binaryUri != null) {
			return new TemplatePath(binaryUri, resolvedlayoutFileName, true);
		}

		return null;
	}

}
