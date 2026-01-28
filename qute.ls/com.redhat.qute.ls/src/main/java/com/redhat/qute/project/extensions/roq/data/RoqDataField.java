package com.redhat.qute.project.extensions.roq.data;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.services.extensions.DefinitionExtensionProvider;
import com.redhat.qute.services.extensions.HoverExtensionProvider;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;

public class RoqDataField extends JavaFieldInfo implements DefinitionExtensionProvider, HoverExtensionProvider {

	private final String resolvedName;
	private final RoqDataFile document;

	public RoqDataField(String resolvedName, ResolvedJavaTypeInfo resolvedType, RoqDataFile document) {
		this.resolvedName = resolvedName;
		super.setResolvedType(resolvedType);
		this.document = document;
	}

	@Override
	public boolean shouldLoadDocumentation() {
		return false;
	}

	@Override
	public String getName() {
		return resolvedName;
	}

	public RoqDataFile getDocument() {
		return document;
	}

	@Override
	public List<? extends LocationLink> getLocations(Part part) {
		LocationLink link = new LocationLink();
		link.setOriginSelectionRange(QutePositionUtility.createRange(part));
		link.setTargetUri(FileUtils.toUri(document.getFilePath()));
		link.setTargetRange(QutePositionUtility.ZERO_RANGE);
		link.setTargetSelectionRange(QutePositionUtility.ZERO_RANGE);
		return Collections.singletonList(link);
	}

	@Override
	public Hover getHover(Part part, HoverRequest hoverRequest) {
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		MarkupContent content = getDocumentation(this, hasMarkdown);
		Range range = QutePositionUtility.createRange(part);
		return new Hover(content, range);
	}

	private static MarkupContent getDocumentation(RoqDataField field, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Field info
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}
		documentation.append(getSimpleType(field.getType()));
		documentation.append(" ");
		documentation.append(field.getName());
		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}
		documentation.append(System.lineSeparator());

		// Source
		Path filePath = field.getDocument().getFilePath();
		String fileUri = filePath.toUri().toString();
		documentation.append("Source: ");
		if (markdown) {
			documentation.append("[");
			documentation.append(filePath.getFileName().toString());
			documentation.append("]");
			documentation.append("(");
			documentation.append(fileUri);
			documentation.append(")");
		} else {
			documentation.append(fileUri);
		}
		return DocumentationUtils.createMarkupContent(documentation, markdown);
	}

}
