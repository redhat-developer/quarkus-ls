package com.redhat.qute.project.extensions.roq.frontmatter;

import static com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDocumentationUtils.getDocumentation;
import static com.redhat.qute.services.QuteHover.NO_HOVER;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlNodeKind;
import com.redhat.qute.parser.yaml.YamlPositionUtility;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider;
import com.redhat.qute.project.extensions.roq.frontmatter.schema.YamlFrontMatterSchemaProvider.FrontMatterProperty;
import com.redhat.qute.services.hover.HoverRequest;

public class YamlFrontMatterHover {

	public CompletableFuture<Hover> doHover(YamlDocument document, int offset, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		YamlNode yamlNode = document.findNodeAt(offset);
		if (yamlNode != null && yamlNode.getKind() == YamlNodeKind.YamlProperty) {
			YamlProperty property = (YamlProperty) yamlNode;
			if (property.isInKey(offset)) {
				String propertyKey = property.getKey().toString();
				FrontMatterProperty frontMatterProperty = YamlFrontMatterSchemaProvider.getInstance()
						.getProperty(propertyKey);
				if (frontMatterProperty != null) {
					boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
					MarkupContent content = getDocumentation(frontMatterProperty, hasMarkdown);
					Range range = YamlPositionUtility.createRange(property.getKey());
					if (range != null) {
						Hover hover = new Hover(content, range);
						return CompletableFuture.completedFuture(hover);
					}
				}
			}
		}
		return NO_HOVER;
	}

}
