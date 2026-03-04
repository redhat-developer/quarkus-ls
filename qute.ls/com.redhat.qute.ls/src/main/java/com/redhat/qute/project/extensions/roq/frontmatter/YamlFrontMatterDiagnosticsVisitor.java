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

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.parser.yaml.YamlASTVisitor;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * Yaml frontmatter diagnostics support.
 */
public class YamlFrontMatterDiagnosticsVisitor extends YamlASTVisitor {

	private final Template template;
	private final QuteValidationSettings validationSettings;
	private final List<Diagnostic> diagnostics;
	private final RoqProjectExtension roq;
	private final Path filePath;

	public YamlFrontMatterDiagnosticsVisitor(Template template, QuteValidationSettings validationSettings,
			List<Diagnostic> diagnostics) {
		this.template = template;
		this.validationSettings = validationSettings;
		this.diagnostics = diagnostics;
		this.roq = RoqProjectExtension.getRoqProjectExtension(template);
		this.filePath = FileUtils.createPath(template.getUri());
	}

	@Override
	public boolean visit(YamlProperty node) {
		if (node.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)) {
			validateLayoutProperty(node);
		} else if (node.isProperty(FrontMatterProperty.IMAGE_PROPERTY)) {
			validateImageProperty(node);
		}
		return super.visit(node);
	}

	private void validateLayoutProperty(YamlProperty node) {
		YamlNode propertyValue = node.getValue();
		if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
			String layoutFilePath = ((YamlScalar) propertyValue).getValue();
			try {
				TemplatePath layoutPath = roq.getLayoutPath(filePath, layoutFilePath);
				if (layoutPath != null && !layoutPath.isExists()) {
					Range range = YamlPositionUtility.createRange(propertyValue);
					Diagnostic d = createDiagnostic(range, DiagnosticSeverity.Warning,
							YamlFrontMatterErrorCode.LayoutNotFound, layoutFilePath);
					diagnostics.add(d);
				}
			} catch (Exception e) {
				Range range = YamlPositionUtility.createRange(propertyValue);
				Diagnostic d = createDiagnostic(range, DiagnosticSeverity.Warning,
						YamlFrontMatterErrorCode.InvalidLayoutPath, layoutFilePath, e.getMessage());
				diagnostics.add(d);
			}
		}
	}

	private void validateImageProperty(YamlProperty node) {
		YamlNode propertyValue = node.getValue();
		if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
			String imageFilePath = ((YamlScalar) propertyValue).getValue();
			try {
				TemplatePath imagePath = roq.getImagePath(filePath, imageFilePath);
				if (imagePath != null && !imagePath.isExists()) {
					Range range = YamlPositionUtility.createRange(propertyValue);
					Diagnostic d = createDiagnostic(range, DiagnosticSeverity.Warning,
							YamlFrontMatterErrorCode.ImageNotFound, imageFilePath);
					diagnostics.add(d);
				}
			} catch (Exception e) {
				Range range = YamlPositionUtility.createRange(propertyValue);
				Diagnostic d = createDiagnostic(range, DiagnosticSeverity.Warning,
						YamlFrontMatterErrorCode.InvalidImagePath, imageFilePath, e.getMessage());
				diagnostics.add(d);
			}
		}
	}

}
