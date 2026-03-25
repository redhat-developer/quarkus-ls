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
package com.redhat.qute.project.extensions.roq.frontmatter;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.lsp4j.MarkupContent;

import com.redhat.qute.QuteLanguageIds;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.StringUtils;

/**
 * Yaml frontmatter documentation utility.
 */
public class YamlFrontMatterDocumentationUtils {

	private YamlFrontMatterDocumentationUtils() {

	}

	public static MarkupContent getDocumentation(FrontMatterProperty frontMatterProperty, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("```");
			documentation.append(System.lineSeparator());
		}
		documentation.append(frontMatterProperty.getName());
		if (!StringUtils.isEmpty(frontMatterProperty.getType())) {
			documentation.append(": ");
			documentation.append(frontMatterProperty.getType());
		}
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}

		String description = frontMatterProperty.getDescription();
		if (description != null) {
			documentation.append(System.lineSeparator());
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}

		List<String> examples = frontMatterProperty.getExamples();
		if (examples != null && !examples.isEmpty()) {
			documentation.append(System.lineSeparator());
			for (String example : examples) {
				if (markdown) {
					documentation.append("```" + QuteLanguageIds.QUTE_HTML);
					documentation.append(System.lineSeparator());
				}
				documentation.append(example);
				documentation.append(System.lineSeparator());
				if (markdown) {
					documentation.append("```");
					documentation.append(System.lineSeparator());
				}
			}
		}
		return DocumentationUtils.createMarkupContent(documentation, markdown);
	}

	public static MarkupContent getImageDocumentation(TemplatePath imagePath, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Title
		if (markdown) {
			documentation.append("![image](");
			documentation.append(imagePath.getUri());
			documentation.append(")");
			documentation.append(System.lineSeparator());
		}
		return DocumentationUtils.createMarkupContent(documentation, markdown);
	}
}
