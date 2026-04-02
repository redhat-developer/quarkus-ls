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

import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getDocumentation;
import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getImageDocumentation;
import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getLayoutDocumentation;
import static com.redhat.qute.services.QuteHover.NO_HOVER;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider;
import com.redhat.qute.services.hover.HoverRequest;

/**
 * Yaml FrontMatter Hover support.
 */
public class YamlFrontMatterHover {

	public CompletableFuture<Hover> doHover(YamlDocument document, int offset, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		YamlNode yamlNode = document.findNodeAt(offset);
		if (yamlNode != null && yamlNode.getKind() == YamlNodeKind.YamlProperty) {
			YamlProperty property = (YamlProperty) yamlNode;
			if (property.isInKey(offset)) {
				// Hover on key (ex: layo|ut:, ima|ge: )
				return doHoverOnKey(property, hoverRequest);
			} else if (property.isInValue(offset)) {
				if (property.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)) {
					// Hover on layout value (ex: layout: som|e-layout)
					return doHoverOnLayoutValue(property, hoverRequest, false);
				}
				if (property.isProperty(FrontMatterProperty.THEME_LAYOUT_PROPERTY)) {
					// Hover on default-layout value (ex: theme-layout: som|e-layout)
					return doHoverOnLayoutValue(property, hoverRequest, true);
				}
				if (property.isProperty(FrontMatterProperty.IMAGE_PROPERTY)) {
					// Hover on image value (ex: image: som|e/path/file)
					return doHoverOnImageValue(property, hoverRequest);
				}
			}
		}
		return NO_HOVER;
	}

	private CompletableFuture<Hover> doHoverOnLayoutValue(YamlProperty property, HoverRequest hoverRequest,
			boolean themeLayout) {
		Template template = hoverRequest.getTemplate();
		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			String layoutFileName = ((YamlScalar) property.getValue()).getValue();
			try {
				TemplatePath layoutPath = themeLayout ? roq.getThemeLayoutPath(null, layoutFileName)
						: roq.getLayoutPath(null, layoutFileName);
				if (layoutPath != null) {
					boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
					QuteTextDocument document = getDocument(layoutPath, template.getProject());
					MarkupContent content = getLayoutDocumentation(layoutFileName, layoutPath, document, hasMarkdown);
					Range range = YamlPositionUtility.createRange(property.getKey());
					if (range != null) {
						Hover hover = new Hover(content, range);
						return CompletableFuture.completedFuture(hover);
					}
				}
			} catch (Exception e) {
				// Ignore error with invalid path
			}

		}
		return NO_HOVER;

	}

	private QuteTextDocument getDocument(TemplatePath layoutPath, QuteProject project) {
		if (layoutPath == null || !layoutPath.isExists()) {
			return null;
		}
		var document = project.findDocumentByTemplateId(layoutPath.getTemplateId());
		if (document != null) {
			return document;
		}
		try {
			String uri = layoutPath.getUri();
			return uri != null ? project.findSourceDocument(FileUtils.createPath(uri)) : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static CompletableFuture<Hover> doHoverOnKey(YamlProperty property, HoverRequest hoverRequest) {
		String propertyKey = property.getKey().toString();
		FrontMatterProperty frontMatterProperty = YamlFrontMatterSchemaProvider.getInstance().getProperty(propertyKey);
		if (frontMatterProperty != null) {
			boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
			MarkupContent content = getDocumentation(frontMatterProperty, hasMarkdown);
			Range range = YamlPositionUtility.createRange(property.getKey());
			if (range != null) {
				Hover hover = new Hover(content, range);
				return CompletableFuture.completedFuture(hover);
			}
		}
		return NO_HOVER;
	}

	private static CompletableFuture<Hover> doHoverOnImageValue(YamlProperty property, HoverRequest hoverRequest) {
		Template template = hoverRequest.getTemplate();
		RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(template);
		if (roq != null) {
			Path filePath = FileUtils.createPath(template.getUri());
			String imageFilePath = ((YamlScalar) property.getValue()).getValue();
			try {
				TemplatePath imagePath = roq.getImagePath(filePath, imageFilePath);
				if (imagePath != null && imagePath.isExists()) {
					boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
					MarkupContent content = getImageDocumentation(imagePath, hasMarkdown);
					Range range = YamlPositionUtility.createRange(property.getKey());
					if (range != null) {
						Hover hover = new Hover(content, range);
						return CompletableFuture.completedFuture(hover);
					}
				}
			} catch (Exception e) {
				// Ignore error with invalid path
			}

		}
		return NO_HOVER;
	}

}
