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

/**
 * Represents a field from a Roq data file (YAML, JSON, etc.).
 * 
 * <p>
 * This class extends JavaFieldInfo to provide LSP (Language Server Protocol)
 * features for data file fields in Qute templates:
 * </p>
 * <ul>
 * <li><b>Go to Definition</b> - Navigate from template usage to source data
 * file</li>
 * <li><b>Hover</b> - Show field type and source file in tooltip</li>
 * <li><b>Autocomplete</b> - Field appears in template completion lists</li>
 * </ul>
 * 
 * <h3>Example:</h3>
 * <p>
 * Given a YAML file {@code authors.yaml}:
 * 
 * <pre>
 * name: John Doe
 * email: john@example.com
 * </pre>
 * </p>
 * 
 * <p>
 * In a Qute template {@code {authors.name}}:
 * <ul>
 * <li>Hovering over "name" shows: {@code String name} (Source:
 * authors.yaml)</li>
 * <li>Ctrl+Click on "name" opens authors.yaml</li>
 * </ul>
 * </p>
 * 
 * @see JavaFieldInfo
 * @see RoqDataFile
 * @see DefinitionExtensionProvider
 * @see HoverExtensionProvider
 */
public class RoqDataField extends JavaFieldInfo implements DefinitionExtensionProvider, HoverExtensionProvider {

	/** The resolved field name (e.g., "name", "email", "items") */
	private final String resolvedName;

	/** Reference to the parent data file that contains this field */
	private final RoqDataFile document;

	/**
	 * Creates a new RoqDataField.
	 * 
	 * @param resolvedName The field name as it appears in the data file
	 * @param resolvedType The Java type of this field (String, Integer, Object,
	 *                     Collection, etc.) May be null for simple scalar types
	 * @param document     The parent RoqDataFile containing this field
	 */
	public RoqDataField(String resolvedName, ResolvedJavaTypeInfo resolvedType, RoqDataFile document) {
		this.resolvedName = resolvedName;
		super.setResolvedType(resolvedType);
		this.document = document;
	}

	/**
	 * Indicates that documentation should not be lazily loaded.
	 * 
	 * <p>
	 * Data file fields generate their documentation on-the-fly from the field type
	 * and source file, so there's no need to load external JavaDoc or similar
	 * resources.
	 * </p>
	 * 
	 * @return false - documentation is generated dynamically, not loaded
	 */
	@Override
	public boolean shouldLoadDocumentation() {
		return false;
	}

	/**
	 * Returns the field name.
	 * 
	 * <p>
	 * This is the name used in templates, matching the key from the source data
	 * file (e.g., "name" from {@code name: John}).
	 * </p>
	 * 
	 * @return The field name
	 */
	@Override
	public String getName() {
		return resolvedName;
	}

	/**
	 * Returns the parent data file that contains this field.
	 * 
	 * <p>
	 * Used for "Go to Definition" to navigate to the source file, and for hover
	 * documentation to show the file path.
	 * </p>
	 * 
	 * @return The parent RoqDataFile
	 */
	public RoqDataFile getDocument() {
		return document;
	}

	/**
	 * Provides "Go to Definition" support for this field.
	 * 
	 * <p>
	 * When a user Ctrl+Clicks on this field in a template, the IDE will open the
	 * source data file (YAML, JSON, etc.) that defines it.
	 * </p>
	 * 
	 * <p>
	 * Currently navigates to the start of the file (position 0:0) rather than the
	 * exact field location. Future enhancement could parse the file to find the
	 * precise line/column of the field definition.
	 * </p>
	 * 
	 * @param part The template expression part being navigated from (e.g., "name"
	 *             in {@code {authors.name}})
	 * @return A list containing a single LocationLink to the source file
	 */
	@Override
	public List<? extends LocationLink> getLocations(Part part) {
		LocationLink link = new LocationLink();

		// Origin: The position in the template where the user clicked
		link.setOriginSelectionRange(QutePositionUtility.createRange(part));

		// Target: The data file URI (e.g., file:///path/to/authors.yaml)
		link.setTargetUri(FileUtils.toUri(document.getFilePath()));

		// Target position: Start of file (0:0)
		// TODO: Could enhance to find exact field location in the data file
		link.setTargetRange(QutePositionUtility.ZERO_RANGE);
		link.setTargetSelectionRange(QutePositionUtility.ZERO_RANGE);

		return Collections.singletonList(link);
	}

	/**
	 * Provides hover documentation for this field.
	 * 
	 * <p>
	 * When a user hovers over this field in a template, displays:
	 * <ul>
	 * <li>Field type and name in code block (e.g., {@code String name})</li>
	 * <li>Source file as a clickable link</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Example hover content (Markdown):
	 * 
	 * <pre>
	 * ```java
	 * String name
	 * ```
	 * Source: [authors.yaml](file:///path/to/authors.yaml)
	 * </pre>
	 * </p>
	 * 
	 * @param part         The template expression part being hovered over
	 * @param hoverRequest The hover request with client capabilities
	 * @return A Hover object with formatted documentation
	 */
	@Override
	public Hover getHover(Part part, HoverRequest hoverRequest) {
		// Check if the client supports Markdown formatting
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);

		// Generate the hover content
		MarkupContent content = getDocumentation(this, hasMarkdown);

		// The range in the template that this hover applies to
		Range range = QutePositionUtility.createRange(part);

		return new Hover(content, range);
	}

	/**
	 * Generates the hover documentation content for a field.
	 * 
	 * <p>
	 * Creates formatted documentation showing:
	 * <ol>
	 * <li>Field signature in a code block (if Markdown supported)</li>
	 * <li>Source file path as a clickable link (if Markdown supported)</li>
	 * </ol>
	 * </p>
	 * 
	 * <h3>Markdown Format:</h3>
	 * 
	 * <pre>
	 * ```java
	 * String name
	 * ```
	 * Source: [authors.yaml](file:///path/to/authors.yaml)
	 * </pre>
	 * 
	 * <h3>Plain Text Format:</h3>
	 * 
	 * <pre>
	 * String name
	 * Source: file:///path/to/authors.yaml
	 * </pre>
	 * 
	 * @param field    The field to document
	 * @param markdown Whether to use Markdown formatting
	 * @return Formatted documentation as MarkupContent
	 */
	private static MarkupContent getDocumentation(RoqDataField field, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Field signature section
		if (markdown) {
			documentation.append("```java");
			documentation.append(System.lineSeparator());
		}

		// Type and name (e.g., "String name")
		documentation.append(getSimpleType(field.getType()));
		documentation.append(" ");
		documentation.append(field.getName());

		if (markdown) {
			documentation.append(System.lineSeparator());
			documentation.append("```");
		}
		documentation.append(System.lineSeparator());

		// Source file section
		Path filePath = field.getDocument().getFilePath();
		String fileUri = filePath.toUri().toString();
		documentation.append("Source: ");

		if (markdown) {
			// Create a clickable link: [filename](file://...)
			documentation.append("[");
			documentation.append(filePath.getFileName().toString());
			documentation.append("]");
			documentation.append("(");
			documentation.append(fileUri);
			documentation.append(")");
		} else {
			// Plain text: just show the URI
			documentation.append(fileUri);
		}

		return DocumentationUtils.createMarkupContent(documentation, markdown);
	}

}