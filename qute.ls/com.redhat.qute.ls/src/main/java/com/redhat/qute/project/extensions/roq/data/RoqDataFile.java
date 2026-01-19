package com.redhat.qute.project.extensions.roq.data;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.project.datamodel.resolvers.CustomValueResolver;
import com.redhat.qute.services.extensions.DefinitionExtensionProvider;
import com.redhat.qute.services.extensions.HoverExtensionProvider;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;

public class RoqDataFile extends CustomValueResolver implements DefinitionExtensionProvider, HoverExtensionProvider {

	private final Path filePath;
	private final String name;

	public RoqDataFile(Path filePath, String namespace, DataLoader dataLoader) {
		super.setResolvedType(this);
		super.setNamespace(namespace);
		this.filePath = filePath;
		this.name = getFileNameWithoutExtension(filePath);

		if (dataLoader != null) {
			dataLoader.load(this);
		}
	}

	public RoqDataFile create(String namespace) {
		RoqDataFile resolver = new RoqDataFile(getFilePath(), namespace, null);
		resolver.setFields(getFields());
		resolver.setSignature("name : Object");
		return resolver;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getJavaElementType() {
		return null;
	}

	@Override
	public CompletionItemKind getCompletionKind() {
		return CompletionItemKind.File;
	}

	public Path getFilePath() {
		return filePath;
	}

	public static String getFileNameWithoutExtension(Path path) {
		if (path == null || path.getFileName() == null) {
			return null;
		}

		String fileName = path.getFileName().toString();
		int lastDot = fileName.lastIndexOf('.');

		if (lastDot <= 0) {
			return fileName;
		}

		return fileName.substring(0, lastDot);
	}

	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

	// Definition

	@Override
	public List<? extends LocationLink> getLocations(Part part) {
		LocationLink link = new LocationLink();
		link.setOriginSelectionRange(QutePositionUtility.createRange(part));
		link.setTargetUri(FileUtils.toUri(getFilePath()));
		link.setTargetRange(QutePositionUtility.ZERO_RANGE);
		link.setTargetSelectionRange(QutePositionUtility.ZERO_RANGE);
		return Collections.singletonList(link);
	}

	// Hover

	@Override
	public Hover getHover(Part part, HoverRequest hoverRequest) {
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);
		MarkupContent content = getDocumentation(this, null, hasMarkdown);
		Range range = QutePositionUtility.createRange(part);
		return new Hover(content, range);
	}

	private static MarkupContent getDocumentation(RoqDataFile file, String description, boolean markdown) {
		StringBuilder documentation = new StringBuilder();
		if (description != null) {
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}
		Path filePath = file.getFilePath();
		String fileUri = filePath.toUri().toString();
		if (markdown) {
			documentation.append("Open [");
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