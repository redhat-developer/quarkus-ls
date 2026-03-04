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

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlMapping;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;

/**
 * Yaml FrontMatter Document links support.
 */
public class YamlFrontMatterDocumentLink {

	public void findDocumentLinks(YamlDocument document, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker) {
		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq == null) {
			return;
		}

		Path filePath = FileUtils.createPath(template.getUri());
		List<YamlNode> children = document.getChildren();
		for (YamlNode yamlNode : children) {
			if (yamlNode.getKind() == YamlNodeKind.YamlMapping) {
				YamlMapping mapping = (YamlMapping) yamlNode;
				for (YamlNode mappingChild : mapping.getChildren()) {
					if (mappingChild.getKind() == YamlNodeKind.YamlProperty) {
						YamlProperty property = (YamlProperty) mappingChild;
						YamlNode propertyValue = property.getValue();
						if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
							if (property.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)) {
								// ex: layout: page --> page must be a link.
								Range range = YamlPositionUtility.createRange(propertyValue);
								if (range != null) {
									String layoutFileName = ((YamlScalar) propertyValue).getValue();
									try {
										TemplatePath layoutPath = roq.getLayoutPath(filePath, layoutFileName);
										if (layoutPath != null) {
											String target = layoutPath.getUri();
											links.add(new DocumentLink(range, target != null ? target : ""));
										}
									} catch (Exception e) {
										// Ignore error with invalid path
									}

								}
							} else if (property.isProperty(FrontMatterProperty.IMAGE_PROPERTY)) {
								// ex: image: foo.png --> foo.png must be a link.
								Range range = YamlPositionUtility.createRange(propertyValue);
								if (range != null) {
									String imageFilePath = ((YamlScalar) propertyValue).getValue();
									try {
										TemplatePath imagePath = roq.getImagePath(filePath, imageFilePath);
										if (imagePath != null) {
											String target = imagePath.getUri();
											links.add(new DocumentLink(range, target != null ? target : ""));
										}
									} catch (Exception e) {
										// Ignore error with invalid path
									}
								}
							}
						}
					}
				}
			}
		}
	}

}
