package com.redhat.qute.project.extensions.roq.frontmatter;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlMapping;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.project.extensions.roq.RoqProjectExtension;

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
						if (propertyValue != null && propertyValue.getKind() == YamlNodeKind.YamlScalar
								&& YamlFrontMatterConfig.isPropertyConfig(property,
										YamlFrontMatterConfig.LAYOUT_PROPERTY)) {
							Range range = YamlPositionUtility.createRange(propertyValue);
							if (range != null) {
								String layoutFileName = ((YamlScalar) propertyValue).getValue();
								Path layoutPath = roq.getLayoutPath(filePath, layoutFileName);
								if (layoutPath != null) {
									String target = layoutPath.toUri().toASCIIString();
									links.add(new DocumentLink(range, target != null ? target : ""));
								}
							}
						}
					}

				}
			}
		}
	}

}
