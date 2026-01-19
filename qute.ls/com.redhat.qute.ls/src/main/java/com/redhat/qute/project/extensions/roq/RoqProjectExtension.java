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
package com.redhat.qute.project.extensions.roq;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.CustomValueResolver;
import com.redhat.qute.project.extensions.DataModelTemplateParticipant;
import com.redhat.qute.project.extensions.ProjectExtension;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;
import com.redhat.qute.project.extensions.roq.data.json.JsonDataLoader;
import com.redhat.qute.project.extensions.roq.data.yaml.YamlDataLoader;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * Qute project extension for Roq integration.
 * 
 * <p>
 * This extension enables Roq data file support in Qute templates by:
 * </p>
 * <ul>
 * <li>Discovering data files (YAML, JSON) in the configured data directory</li>
 * <li>Loading and parsing data files using appropriate loaders</li>
 * <li>Registering data files as value resolvers for template access</li>
 * <li>Watching for file changes and updating resolvers accordingly</li>
 * </ul>
 * 
 * <h3>Lifecycle:</h3>
 * <ol>
 * <li><b>init()</b> - Detects Roq projects, locates data directory</li>
 * <li><b>loadRoqDataFiles()</b> - Scans and loads all data files</li>
 * <li><b>didChangeWatchedFile()</b> - Handles file
 * changes/creation/deletion</li>
 * </ol>
 * 
 * <h3>Data File Registration:</h3>
 * <p>
 * Each data file is registered with two namespaces:
 * <ul>
 * <li>{@code cdi:} - CDI namespace for backward compatibility</li>
 * <li>{@code inject:} - Inject namespace (primary access method)</li>
 * </ul>
 * </p>
 * 
 * <h3>Example:</h3>
 * 
 * <pre>
 * data/
 * └── authors.yaml
 * 
 * Template access:
 * {inject:authors.name}  ← Primary
 * {cdi:authors.name}     ← Alternative
 * </pre>
 * 
 * <h3>Supported File Types:</h3>
 * <ul>
 * <li><b>.yaml, .yml</b> - YAML files (YamlDataLoader)</li>
 * <li><b>.json</b> - JSON files (JsonDataLoader)</li>
 * </ul>
 * 
 * @see ProjectExtension
 * @see RoqDataFile
 * @see DataLoader
 */
public class RoqProjectExtension implements ProjectExtension, DataModelTemplateParticipant {

	// We might need to allow plugins to contribute to this at some point
	private static final Set<String> HTML_OUTPUT_EXTENSIONS = Set.of("md", "markdown", "html", "htm", "xhtml",
			"asciidoc", "adoc");
	private static final Set<String> INDEX_FILES = HTML_OUTPUT_EXTENSIONS.stream().map(e -> "index." + e)
			.collect(Collectors.toSet());

	/**
	 * Registry mapping file extensions to their corresponding data loaders.
	 * 
	 * <p>
	 * This static registry is initialized once and shared across all instances. It
	 * determines which loader to use based on the file extension.
	 * </p>
	 * 
	 * <h3>Registered Loaders:</h3>
	 * <ul>
	 * <li>"yml" → YamlDataLoader</li>
	 * <li>"yaml" → YamlDataLoader</li>
	 * <li>"json" → JsonDataLoader</li>
	 * </ul>
	 * 
	 * <h3>Adding New Loaders:</h3>
	 * 
	 * <pre>
	 * // Example: Add TOML support
	 * dataLoaderRegistry.put("toml", new TomlDataLoader());
	 * </pre>
	 */
	private static final Map<String /* file extension */, DataLoader> dataLoaderRegistrty;

	static {
		// Initialize the loader registry
		dataLoaderRegistrty = new HashMap<>();

		// YAML files (.yml and .yaml)
		YamlDataLoader yamlDataLoader = new YamlDataLoader();
		dataLoaderRegistrty.put("yml", yamlDataLoader);
		dataLoaderRegistrty.put("yaml", yamlDataLoader);

		// JSON files (.json)
		dataLoaderRegistrty.put("json", new JsonDataLoader());
	}

	/**
	 * Cache entry for a registered data file.
	 * 
	 * <p>
	 * Each data file is registered with two resolvers (cdi and inject namespaces).
	 * This class keeps both references together for efficient cache management.
	 * </p>
	 * 
	 * <h3>Purpose:</h3>
	 * <p>
	 * When a file changes or is deleted, we need to remove both resolvers from the
	 * data model. This cache entry provides quick access to both.
	 * </p>
	 */
	private static class ResolverCache {
		/** Resolver for cdi namespace (e.g., {cdi:authors}) */
		public final CustomValueResolver cdi;

		/** Resolver for inject namespace (e.g., {inject:authors}) */
		public final CustomValueResolver inject;

		public ResolverCache(CustomValueResolver cdi, CustomValueResolver inject) {
			this.cdi = cdi;
			this.inject = inject;
		}
	}

	/** Whether this extension is enabled (true for Roq projects) */
	private boolean enabled;

	/** Path to the data directory (e.g., project-root/data/) */
	private Path dataDir;
	private Path contentDir;
	private Set<String> configuredCollections;

	/**
	 * Cache mapping file paths to their registered resolvers.
	 * 
	 * <p>
	 * Used for efficient lookup when handling file change events:
	 * <ul>
	 * <li>File modified → Remove old resolvers, register new ones</li>
	 * <li>File deleted → Remove resolvers</li>
	 * </ul>
	 * </p>
	 */
	private final Map<Path, ResolverCache> dataResolverCache;

	/** Reference to the parent data model project */
	private ExtendedDataModelProject dataModelProject;

	/**
	 * Creates a new Roq project extension.
	 */
	public RoqProjectExtension() {
		this.dataResolverCache = new HashMap<>();
		this.configuredCollections = new HashSet<>();
		// TODO: load collections from application.propertes
		this.configuredCollections.add("posts");
	}

	/**
	 * Initializes the extension for a Qute project.
	 * 
	 * <p>
	 * This method is called when the project is loaded. It:
	 * <ol>
	 * <li>Checks if Roq is enabled via ProjectFeature.Roq</li>
	 * <li>Resolves the data directory from configuration</li>
	 * <li>Loads all data files from the directory</li>
	 * </ol>
	 * </p>
	 * 
	 * <h3>Configuration:</h3>
	 * <p>
	 * The data directory is determined by:
	 * 
	 * <pre>
	 * dataDir = quarkus.roq.dir / quarkus.roq.data.dir
	 * 
	 * Example:
	 * quarkus.roq.dir=          (project root)
	 * quarkus.roq.data.dir=data (relative path)
	 * → dataDir = project-root/data/
	 * </pre>
	 * </p>
	 * 
	 * @param dataModelProject The project data model to extend
	 */
	@Override
	public void init(ExtendedDataModelProject dataModelProject) {
		this.dataModelProject = dataModelProject;

		// Check if Roq is enabled for this project
		enabled = dataModelProject.hasProjectFeature(ProjectFeature.Roq);

		if (enabled) {
			scanDataDir(dataModelProject);
			contentDir = dataModelProject.getConfigAsPath(RoqConfig.ROQ_CONTENT_DIR);
		} else {
			// Roq not enabled - clear data directory
			dataDir = null;
		}
	}

	private void scanDataDir(ExtendedDataModelProject dataModelProject) {
		if (dataDir == null) {
			// Resolve the Roq root directory
			Path roqDir = dataModelProject.getConfigAsPath(RoqConfig.ROQ_DIR);

			if (roqDir != null) {
				// Resolve the data subdirectory
				dataDir = roqDir.resolve(dataModelProject.getConfig(RoqConfig.ROQ_DATA_DIR));

				if (dataDir != null) {
					// Load all data files from the directory
					loadRoqDataFiles(dataModelProject);
				}
			}
		}
	}

	/**
	 * Scans the data directory and loads all data files.
	 * 
	 * <p>
	 * Iterates through all regular files in the data directory and registers them
	 * as data file resolvers if they have a supported file extension (.yaml, .yml,
	 * .json).
	 * </p>
	 * 
	 * <h3>Error Handling:</h3>
	 * <p>
	 * IOException is silently ignored - this handles cases where:
	 * <ul>
	 * <li>The data directory doesn't exist yet</li>
	 * <li>Permission errors occur</li>
	 * <li>I/O errors happen during scanning</li>
	 * </ul>
	 * </p>
	 * 
	 * @param dataModelProject The project to register resolvers with
	 */
	private void loadRoqDataFiles(ExtendedDataModelProject dataModelProject) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDir)) {
			for (Path file : stream) {
				// Only process regular files (skip directories, symlinks, etc.)
				if (Files.isRegularFile(file)) {
					regsterRoqDataFile(file, dataModelProject);
				}
			}
		} catch (IOException e) {
			// Silently ignore - data directory may not exist yet
		}
	}

	/**
	 * Registers a single data file as a value resolver.
	 * 
	 * <p>
	 * This method:
	 * <ol>
	 * <li>Determines the file extension</li>
	 * <li>Looks up the appropriate DataLoader</li>
	 * <li>Creates two RoqDataFile instances (cdi and inject namespaces)</li>
	 * <li>Registers both with the data model</li>
	 * <li>Caches them for future updates</li>
	 * </ol>
	 * </p>
	 * 
	 * <h3>Example:</h3>
	 * 
	 * <pre>
	 * File: data/authors.yaml
	 * 
	 * Creates:
	 * - RoqDataFile(authors.yaml, "cdi", YamlDataLoader)
	 * - RoqDataFile(authors.yaml, "inject", YamlDataLoader)
	 * 
	 * Template access:
	 * {cdi:authors.name}
	 * {inject:authors.name}
	 * </pre>
	 * 
	 * <h3>Unsupported Files:</h3>
	 * <p>
	 * If the file extension has no registered loader (e.g., .txt, .xml), the file
	 * is silently ignored.
	 * </p>
	 * 
	 * @param file             The data file to register
	 * @param dataModelProject The project to register with
	 */
	private void regsterRoqDataFile(Path file, ExtendedDataModelProject dataModelProject) {
		// Get file extension (e.g., "yaml", "json")
		String fileExtension = getFileExtension(file);

		// Look up the appropriate loader
		DataLoader dataLoader = dataLoaderRegistrty.get(fileExtension);

		if (dataLoader != null) {
			// Create resolver for cdi namespace
			RoqDataFile cdiResolver = new RoqDataFile(file, "cdi", dataLoader);

			// Create resolver for inject namespace (reuses fields from cdi)
			RoqDataFile injectResolver = cdiResolver.create("inject");

			// Cache both resolvers for this file
			dataResolverCache.put(file, new ResolverCache(cdiResolver, injectResolver));

			// Register both resolvers with the data model
			dataModelProject.getCustomValueResolvers().add(cdiResolver);
			dataModelProject.getCustomValueResolvers().add(injectResolver);
		}
		// Else: Unsupported file type, silently ignore
	}

	/**
	 * Handles autocomplete requests.
	 * 
	 * <p>
	 * Currently not implemented for Roq extension. Autocomplete for data files is
	 * handled by the standard Qute completion mechanism using the registered
	 * resolvers.
	 * </p>
	 */
	@Override
	public void doComplete(CompletionRequest completionRequest, Part part, Parts parts,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<CompletionItem> completionItems, CancelChecker cancelChecker) {
		// Not implemented - standard completion handles data file resolvers
	}

	/**
	 * Handles go-to-definition requests.
	 * 
	 * <p>
	 * Currently not implemented for Roq extension. Definition navigation for data
	 * files is handled by the DefinitionExtensionProvider interface implemented by
	 * RoqDataFile.
	 * </p>
	 */
	@Override
	public void definition(Part part, List<LocationLink> locationLinks, CancelChecker cancelChecker) {
		// Not implemented - RoqDataFile handles its own definition navigation
	}

	/**
	 * Validates template expressions.
	 * 
	 * <p>
	 * Currently not implemented for Roq extension. Validation is handled by the
	 * standard Qute validation mechanism using the registered resolvers.
	 * </p>
	 */
	@Override
	public boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		// Not implemented - standard validation handles data file resolvers
		return false;
	}

	/**
	 * Handles file system change events for data files.
	 * 
	 * <p>
	 * This method watches for changes to files in the data directory and:
	 * <ul>
	 * <li><b>Created</b> - Registers the new file as a resolver</li>
	 * <li><b>Changed</b> - Reloads the file and updates resolvers</li>
	 * <li><b>Deleted</b> - Removes resolvers from the data model</li>
	 * </ul>
	 * </p>
	 * 
	 * <h3>Hot Reload:</h3>
	 * <p>
	 * This enables hot reload of data files during development:
	 * 
	 * <pre>
	 * 1. User edits data/authors.yaml
	 * 2. didChangeWatchedFile() is called
	 * 3. Old resolvers removed, new ones created
	 * 4. Templates immediately reflect new data
	 * </pre>
	 * </p>
	 * 
	 * <h3>Return Value:</h3>
	 * <p>
	 * Returns true if the file was handled by this extension, false otherwise. This
	 * allows other extensions to handle the event if needed.
	 * </p>
	 * 
	 * @param filePath    The file that changed
	 * @param changeTypes The types of changes (Created, Changed, Deleted)
	 * @return true if this extension handled the file, false otherwise
	 */
	@Override
	public boolean didChangeWatchedFile(Path filePath, Set<FileChangeType> changeTypes) {
		// Check if the file is in our data directory
		if (dataDir != null && filePath.startsWith(dataDir)) {

			if (changeTypes.contains(FileChangeType.Changed)) {
				// File modified - reload it

				// Remove old resolvers
				ResolverCache resolver = dataResolverCache.remove(filePath);
				if (resolver != null) {
					dataModelProject.getCustomValueResolvers().remove(resolver.cdi);
					dataModelProject.getCustomValueResolvers().remove(resolver.inject);
				}

				// Register new resolvers with fresh data
				regsterRoqDataFile(filePath, dataModelProject);
				return true;

			} else if (changeTypes.contains(FileChangeType.Created)) {
				// New file created - register it
				regsterRoqDataFile(filePath, dataModelProject);
				return true;

			} else if (changeTypes.contains(FileChangeType.Deleted)) {
				// File deleted - remove resolvers
				ResolverCache resolver = dataResolverCache.remove(filePath);
				if (resolver != null) {
					dataModelProject.getCustomValueResolvers().remove(resolver.cdi);
					dataModelProject.getCustomValueResolvers().remove(resolver.inject);
					return true;
				}
				return false;
			}
		}

		// Not our file
		return false;
	}

	/**
	 * Handles hover requests.
	 * 
	 * <p>
	 * Currently not implemented for Roq extension. Hover support for data files is
	 * handled by the HoverExtensionProvider interface implemented by RoqDataFile.
	 * </p>
	 */
	@Override
	public void doHover(Part part, List<Hover> hovers, CancelChecker cancelChecker) {
		// Not implemented - RoqDataFile handles its own hover documentation
	}

	/**
	 * Handles inlay hint requests.
	 * 
	 * <p>
	 * Currently not implemented for Roq extension. Inlay hints (inline parameter
	 * names, type hints, etc.) are not currently provided for data file access.
	 * </p>
	 */
	@Override
	public void inlayHint(Expression node, List<InlayHint> inlayHints, CancelChecker cancelChecker) {
		// Not implemented - no inlay hints for data files yet
	}

	/**
	 * Checks if this extension is enabled.
	 * 
	 * <p>
	 * Returns true if the project has the Roq feature enabled, false otherwise.
	 * When disabled, all extension methods are skipped for performance.
	 * </p>
	 * 
	 * @return true if Roq is enabled for this project
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Extracts the file extension from a path.
	 * 
	 * <h3>Examples:</h3>
	 * <ul>
	 * <li>{@code authors.yaml} → "yaml"</li>
	 * <li>{@code data.json} → "json"</li>
	 * <li>{@code no-extension} → ""</li>
	 * <li>{@code .hidden} → "hidden"</li>
	 * </ul>
	 * 
	 * @param path The file path to process
	 * @return The file extension (without dot) or empty string if none
	 */
	static String getFileExtension(Path path) {
		String name = path.getFileName().toString();
		int lastDot = name.lastIndexOf('.');
		return (lastDot == -1) ? "" : name.substring(lastDot + 1);
	}

	@Override
	public ExtendedDataModelTemplate contributeToDataModel(String templateUri, Path templatePath,
			ExtendedDataModelTemplate dataModelTemplate) {
		if (dataModelTemplate == null) {
			dataModelTemplate = new ExtendedDataModelTemplate(templateUri);
		}

		DataModelParameter site = new DataModelParameter();
		site.setKey("site");
		site.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model.Site");
		dataModelTemplate.addParameter(new ExtendedDataModelParameter(site, dataModelTemplate));

		// page
		TemplateType templateType = getTemplateType(templateUri, templatePath);
		DataModelParameter page = new DataModelParameter();
		page.setKey("page");
		page.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model."
				+ (templateType == TemplateType.DOCUMENT_PAGE ? "Document" : "Normal") + "Page");
		dataModelTemplate.addParameter(new ExtendedDataModelParameter(page, dataModelTemplate));

		return dataModelTemplate;
	}

	private TemplateType getTemplateType(String templateUri, Path p) {
		if (contentDir == null) {
			return TemplateType.NORMAL_PAGE;
		}
		if (p != null && p.startsWith(contentDir)) {
			final Path relativize = contentDir.relativize(p);
			final String topDirName = relativize.getName(0).toString();
			final boolean isCollectionDir = configuredCollections.contains(topDirName);
			final boolean isCollectionIndex = isCollectionDir && INDEX_FILES.contains(p.getFileName().toString())
					&& relativize.getNameCount() == 2;
			if (isCollectionDir && !isCollectionIndex) {
				return TemplateType.DOCUMENT_PAGE;
			}
		}
		return TemplateType.NORMAL_PAGE;
	}
}