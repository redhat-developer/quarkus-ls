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

	private final QuteProject project;

	private LayoutNode oldLayoutValueNode;

	private LayoutNode newLayoutValueNode;

	private final QuteTextDocument document;

	public YamlFrontMatterUsagesVisitor(QuteTextDocument document, QuteProject project) {
		this.document = document;
		this.project = project;
	}

	@Override
	public boolean visit(YamlDocument node) {
		this.oldLayoutValueNode = document.getUserData(YAML_FRONTMATTER_LAYOUT_VALUE_NODE);
		return super.visit(node);
	}

	@Override
	public void endVisit(YamlDocument node) {
		String oldLayout = oldLayoutValueNode != null ? oldLayoutValueNode.value : "";
		String newLayout = newLayoutValueNode != null ? newLayoutValueNode.value : "";
		if (!oldLayout.equals(newLayout)) {
			RoqProjectExtension roq = RoqProjectExtension.getRoqProjectExtension(project);
			IncludeUsagesRegistry registry = project.getIncludeUsagesRegistry();
			if (!StringUtils.isEmpty(oldLayout)) {
				registry.removeUsage(document.getTemplateId(), roq.getLayoutPath(null, oldLayout));
			}
			if (!StringUtils.isEmpty(newLayout)) {
				registry.addUsage(document.getTemplateId(), roq.getLayoutPath(null, newLayout),
						newLayoutValueNode.node);
			}
		}
		document.putUserData(YAML_FRONTMATTER_LAYOUT_VALUE_NODE, newLayoutValueNode);
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
		}
		return super.visit(node);
	}

}
