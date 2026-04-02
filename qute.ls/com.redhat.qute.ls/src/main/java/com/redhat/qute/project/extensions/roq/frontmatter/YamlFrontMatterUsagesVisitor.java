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

import com.redhat.qute.parser.yaml.YamlASTVisitor;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.QuteTextDocument.Key;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.FrontMatterProperty;
import com.redhat.qute.project.usages.IncludeUsagesRegistry;
import com.redhat.qute.utils.StringUtils;

/**
 * Update {@link IncludeUsagesRegistry} usages from layout and theme-layout.
 */
public class YamlFrontMatterUsagesVisitor extends YamlASTVisitor {

	private static class LayoutNode {
		public final YamlScalar node;
		public final String value;

		public LayoutNode(YamlScalar node) {
			this.value = node.getValue();
			this.node = node;
		}
	}

	private static final Key<LayoutNode> YAML_FRONTMATTER_LAYOUT_VALUE_NODE = Key
			.create(YamlFrontMatterUsagesVisitor.class.getName());
	private static final Key<LayoutNode> YAML_FRONTMATTER_THEME_LAYOUT_VALUE_NODE = Key
			.create(YamlFrontMatterUsagesVisitor.class.getName());

	private final QuteProject project;

	// Layout usage
	private LayoutNode oldLayoutValueNode;
	private LayoutNode newLayoutValueNode;

	// Theme layout usage
	private LayoutNode oldThemeLayoutValueNode;
	private LayoutNode newThemeLayoutValueNode;

	private final QuteTextDocument document;

	public YamlFrontMatterUsagesVisitor(QuteTextDocument document, QuteProject project) {
		this.document = document;
		this.project = project;
	}

	@Override
	public boolean visit(YamlDocument node) {
		this.oldLayoutValueNode = document.getUserData(YAML_FRONTMATTER_LAYOUT_VALUE_NODE);
		this.oldThemeLayoutValueNode = document.getUserData(YAML_FRONTMATTER_THEME_LAYOUT_VALUE_NODE);
		return super.visit(node);
	}

	@Override
	public void endVisit(YamlDocument node) {

		// Update Layout usage
		String oldLayout = oldLayoutValueNode != null ? oldLayoutValueNode.value : "";
		String newLayout = newLayoutValueNode != null ? newLayoutValueNode.value : "";
		if (!oldLayout.equals(newLayout)) {
			RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(project);
			IncludeUsagesRegistry registry = project.getIncludeUsagesRegistry();
			if (!StringUtils.isEmpty(oldLayout)) {
				try {
					registry.removeUsage(document.getTemplateId(), roq.getLayoutPath(null, oldLayout));
				} catch (Exception e) {
					// Ignore error with invalid path
				}
			}
			if (!StringUtils.isEmpty(newLayout)) {
				try {
					registry.addUsage(document.getTemplateId(), roq.getLayoutPath(null, newLayout),
							newLayoutValueNode.node);
				} catch (Exception e) {
					// Ignore error with invalid path
				}
			}
		}
		document.putUserData(YAML_FRONTMATTER_LAYOUT_VALUE_NODE, newLayoutValueNode);

		// Update Theme Layout usage
		String oldThemeLayout = oldThemeLayoutValueNode != null ? oldThemeLayoutValueNode.value : "";
		String newThemeLayout = newThemeLayoutValueNode != null ? newThemeLayoutValueNode.value : "";
		if (!oldThemeLayout.equals(newThemeLayout)) {
			RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(project);
			IncludeUsagesRegistry registry = project.getIncludeUsagesRegistry();
			if (!StringUtils.isEmpty(oldThemeLayout)) {
				try {
					registry.removeUsage(document.getTemplateId(), roq.getThemeLayoutPath(null, oldThemeLayout));
				} catch (Exception e) {
					// Ignore error with invalid path
				}
			}
			if (!StringUtils.isEmpty(newThemeLayout)) {
				try {
					registry.addUsage(document.getTemplateId(), roq.getThemeLayoutPath(null, newThemeLayout),
							newThemeLayoutValueNode.node);
				} catch (Exception e) {
					// Ignore error with invalid path
				}
			}
		}
		document.putUserData(YAML_FRONTMATTER_THEME_LAYOUT_VALUE_NODE, newThemeLayoutValueNode);

		super.endVisit(node);
	}

	@Override
	public boolean visit(YamlProperty node) {
		if (node.isProperty(FrontMatterProperty.LAYOUT_PROPERTY)) {
			YamlNode propertyValue = node.getValue();
			if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
				String layoutFilePath = ((YamlScalar) propertyValue).getValue();
				if (!StringUtils.isEmpty(layoutFilePath)) {
					this.newLayoutValueNode = new LayoutNode(((YamlScalar) propertyValue));
				}
			}
		} else if (node.isProperty(FrontMatterProperty.THEME_LAYOUT_PROPERTY)) {
			YamlNode propertyValue = node.getValue();
			if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar) {
				String themeLayoutFilePath = ((YamlScalar) propertyValue).getValue();
				if (!StringUtils.isEmpty(themeLayoutFilePath)) {
					this.newThemeLayoutValueNode = new LayoutNode(((YamlScalar) propertyValue));
				}
			}
		}
		return super.visit(node);
	}

}
