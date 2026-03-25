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

/**
 * Represents a Roq data file (YAML, JSON, etc.) as a top-level value resolver
 * in Qute templates.
 * 
 * <p>
 * This class enables IDE support for data files in Qute templates by:
 * </p>
 * <ul>
 * <li><b>Autocomplete</b> - Data file appears in completion lists with File
 * icon</li>
 * <li><b>Go to Definition</b> - Navigate from template usage to the source
 * file</li>
 * <li><b>Hover</b> - Show file path in tooltip with clickable link</li>
 * <li><b>Field Resolution</b> - Fields from the data file are accessible as
 * properties</li>
 * </ul>
 * 
 * <h3>Example:</h3>
 * <p>
 * Given a YAML file {@code data/authors.yaml}:
 * 
 * <pre>
 * name: John Doe
 * email: john@example.com
 * books:
 *   - title: Book 1
 *   - title: Book 2
 * </pre>
 * </p>
 * 
 * <p>
 * In a Qute template:
 * 
 * <pre>
 * {authors.name}           // "John Doe" - authors is a RoqDataFile
 * {authors.email}          // "john@example.com"
 * {#for book in authors.books}
 *   {book.title}
 * {/for}
 * </pre>
 * </p>
 * 
 * <h3>Namespace Support:</h3>
 * <p>
 * Data files can be accessed via namespaces:
 * <ul>
 * <li>{@code data:authors.name} - Global namespace "data"</li>
 * <li>{@code site:authors.name} - Custom namespace "site"</li>
 * </ul>
 * </p>
 * 
 * <h3>Lifecycle:</h3>
 * <ol>
 * <li>RoqDataFile is created with file path and namespace</li>
 * <li>DataLoader (YAML/JSON) parses the file and extracts fields</li>
 * <li>Fields are set via {@code setFields()} - each becomes a RoqDataField</li>
 * <li>Template engine resolves {@code {authors.name}} to the field</li>
 * <li>LSP provides IDE features (hover, go-to-def, completion)</li>
 * </ol>
 * 
 * @see CustomValueResolver
 * @see RoqDataField
 * @see DataLoader
 * @see DefinitionExtensionProvider
 * @see HoverExtensionProvider
 */
public class RoqDataFile extends CustomValueResolver implements DefinitionExtensionProvider, HoverExtensionProvider {

	/** The file system path to the data file (e.g., /path/to/data/authors.yaml) */
	private final Path filePath;

	/** The file name without extension (e.g., "authors" from "authors.yaml") */
	private final String name;

	/**
	 * Creates a new RoqDataFile and loads its contents.
	 * 
	 * <p>
	 * The constructor:
	 * <ol>
	 * <li>Sets up the resolver to resolve itself as a type</li>
	 * <li>Assigns the namespace (e.g., "data", "site")</li>
	 * <li>Extracts the file name without extension</li>
	 * <li>Delegates to the DataLoader to parse and extract fields</li>
	 * </ol>
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * Path yamlFile = Paths.get("data/authors.yaml");
	 * DataLoader loader = new YamlDataLoader();
	 * RoqDataFile dataFile = new RoqDataFile(yamlFile, "data", loader);
	 * 
	 * // Now dataFile contains:
	 * // - name: "authors"
	 * // - namespace: "data"
	 * // - fields: [name, email, books, ...]
	 * </pre>
	 * 
	 * @param filePath   The path to the data file on disk
	 * @param namespace  The namespace for accessing this file (e.g., "data",
	 *                   "site")
	 * @param dataLoader The loader to parse this file type (YamlDataLoader,
	 *                   JsonDataLoader, etc.) May be null if fields will be set
	 *                   manually later
	 */
	public RoqDataFile(Path filePath, String namespace, DataLoader dataLoader) {
		// Set this instance as its own resolved type (acts as both resolver and type)
		super.setResolvedType(this);

		// Set the namespace for template access (e.g., "data" in {data:authors})
		super.setNamespace(namespace);

		// Store the file path for LSP features (go-to-def, hover)
		this.filePath = filePath;

		// Extract the base name without extension (e.g., "authors" from "authors.yaml")
		this.name = getFileNameWithoutExtension(filePath);

		// Load the file contents and populate fields
		if (dataLoader != null) {
			dataLoader.load(this);
		}
	}

	/**
	 * Creates a clone of this data file with a different namespace.
	 * 
	 * <p>
	 * Used when the same data file needs to be accessible from multiple namespaces.
	 * For example, the same file might be accessible as both {@code data:authors}
	 * and {@code site:authors}.
	 * </p>
	 * 
	 * <p>
	 * The clone shares the same fields and file reference but has a different
	 * namespace for template resolution.
	 * </p>
	 * 
	 * @param namespace The new namespace for the clone
	 * @return A new RoqDataFile instance with the same data but different namespace
	 */
	public RoqDataFile create(String namespace) {
		// Create a new instance without loading (dataLoader = null)
		RoqDataFile resolver = new RoqDataFile(getFilePath(), namespace, null);

		// Copy the fields from this instance
		resolver.setFields(getFields());
		resolver.setSignature("name : Object");

		return resolver;
	}

	/**
	 * Returns the file name without extension.
	 * 
	 * <p>
	 * This name is used as the identifier in templates. For example,
	 * {@code authors.yaml} → "authors"
	 * </p>
	 * 
	 * @return The base file name (e.g., "authors")
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns the Java element type for this resolver.
	 * 
	 * <p>
	 * Data files don't have a specific Java type (they're dynamic), so this returns
	 * null. The actual types are in the individual fields.
	 * </p>
	 * 
	 * @return null - no specific Java type
	 */
	@Override
	public String getJavaElementType() {
		return null;
	}

	/**
	 * Returns the completion item kind for IDE autocomplete.
	 * 
	 * <p>
	 * Data files appear in completion lists with a "File" icon to distinguish them
	 * from Java classes, methods, etc.
	 * </p>
	 * 
	 * @return CompletionItemKind.File - displays with file icon
	 */
	@Override
	public CompletionItemKind getCompletionKind() {
		return CompletionItemKind.File;
	}

	/**
	 * Returns the file system path to the data file.
	 * 
	 * <p>
	 * Used for:
	 * <ul>
	 * <li>Go to Definition - navigate to the file</li>
	 * <li>Hover - display file path/link</li>
	 * <li>File watching - reload on changes</li>
	 * </ul>
	 * </p>
	 * 
	 * @return The path to the data file
	 */
	public Path getFilePath() {
		return filePath;
	}

	/**
	 * Extracts the file name without extension from a path.
	 * 
	 * <h3>Examples:</h3>
	 * <ul>
	 * <li>{@code authors.yaml} → "authors"</li>
	 * <li>{@code site-config.json} → "site-config"</li>
	 * <li>{@code .hidden} → ".hidden" (no extension)</li>
	 * <li>{@code noext} → "noext"</li>
	 * </ul>
	 * 
	 * @param path The file path to process
	 * @return The file name without extension, or null if path/filename is null
	 */
	public static String getFileNameWithoutExtension(Path path) {
		if (path == null || path.getFileName() == null) {
			return null;
		}

		String fileName = path.getFileName().toString();
		int lastDot = fileName.lastIndexOf('.');

		// No extension or starts with dot (hidden file like .gitignore)
		if (lastDot <= 0) {
			return fileName;
		}

		// Return everything before the last dot
		return fileName.substring(0, lastDot);
	}

	/**
	 * Returns the owner node for Java type resolution.
	 * 
	 * <p>
	 * Data files don't have a Java type owner (they're not Java classes), so this
	 * returns null.
	 * </p>
	 * 
	 * @return null - no Java type owner
	 */
	@Override
	public Node getJavaTypeOwnerNode() {
		return null;
	}

	// ========================================================================
	// LSP Feature: Go to Definition
	// ========================================================================

	/**
	 * Provides "Go to Definition" support for this data file.
	 * 
	 * <p>
	 * When a user Ctrl+Clicks on the data file name in a template (e.g.,
	 * {@code {authors.name}}), the IDE opens the source data file.
	 * </p>
	 * 
	 * <p>
	 * Currently navigates to the start of the file (position 0:0). Future
	 * enhancement could navigate to a specific field if the user clicked on a field
	 * access like {@code authors.name}.
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * Template: {authors.name}
	 *           ^^^^^^^^
	 *           Ctrl+Click here → Opens authors.yaml
	 * </pre>
	 * 
	 * @param part The template expression part being navigated from
	 * @return A list containing a single LocationLink to the data file
	 */
	@Override
	public List<? extends LocationLink> getLocations(Part part) {
		LocationLink link = new LocationLink();

		// Origin: The position in the template where the user clicked
		link.setOriginSelectionRange(QutePositionUtility.createRange(part));

		// Target: The data file URI (e.g., file:///path/to/authors.yaml)
		link.setTargetUri(FileUtils.toUri(getFilePath()));

		// Target position: Start of file (0:0)
		// TODO: Could enhance to navigate to specific field location
		link.setTargetRange(QutePositionUtility.ZERO_RANGE);
		link.setTargetSelectionRange(QutePositionUtility.ZERO_RANGE);

		return Collections.singletonList(link);
	}

	// ========================================================================
	// LSP Feature: Hover
	// ========================================================================

	/**
	 * Provides hover documentation for this data file.
	 * 
	 * <p>
	 * When a user hovers over the data file name in a template, displays a
	 * clickable link to open the file.
	 * </p>
	 * 
	 * <h3>Example hover content (Markdown):</h3>
	 * 
	 * <pre>
	 * Open [authors.yaml](file:///path/to/data/authors.yaml)
	 * </pre>
	 * 
	 * <h3>Example hover content (Plain Text):</h3>
	 * 
	 * <pre>
	 * file:///path/to/data/authors.yaml
	 * </pre>
	 * 
	 * @param part         The template expression part being hovered over
	 * @param hoverRequest The hover request with client capabilities
	 * @return A Hover object with the file link
	 */
	@Override
	public Hover getHover(Part part, HoverRequest hoverRequest) {
		// Check if the client supports Markdown formatting
		boolean hasMarkdown = hoverRequest.canSupportMarkupKind(MarkupKind.MARKDOWN);

		// Generate the hover content (file link)
		MarkupContent content = getDocumentation(this, null, hasMarkdown);

		// The range in the template that this hover applies to
		Range range = QutePositionUtility.createRange(part);

		return new Hover(content, range);
	}

	/**
	 * Generates the hover documentation content for a data file.
	 * 
	 * <p>
	 * Creates formatted documentation showing a clickable link to open the source
	 * data file.
	 * </p>
	 * 
	 * <h3>Markdown Format:</h3>
	 * 
	 * <pre>
	 * [description]
	 * Open [authors.yaml](file:///path/to/authors.yaml)
	 * </pre>
	 * 
	 * <h3>Plain Text Format:</h3>
	 * 
	 * <pre>
	 * [description]
	 * file:///path/to/authors.yaml
	 * </pre>
	 * 
	 * @param file        The data file to document
	 * @param description Optional description text to prepend (may be null)
	 * @param markdown    Whether to use Markdown formatting
	 * @return Formatted documentation as MarkupContent
	 */
	private static MarkupContent getDocumentation(RoqDataFile file, String description, boolean markdown) {
		StringBuilder documentation = new StringBuilder();

		// Optional description section (if provided)
		if (description != null) {
			documentation.append(description);
			documentation.append(System.lineSeparator());
		}

		// File path and link
		Path filePath = file.getFilePath();
		String fileUri = filePath.toUri().toString();

		if (markdown) {
			// Create a clickable link: Open [filename](file://...)
			documentation.append("Open [");
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