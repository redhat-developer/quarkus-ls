package com.redhat.qute.project.extensions.roq.frontmatter;

import java.util.List;

import org.eclipse.lsp4j.MarkupContent;

import com.redhat.qute.QuteLanguageIds;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider.FrontMatterProperty;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.StringUtils;

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
}
