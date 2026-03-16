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

import static com.redhat.qute.services.completions.QuteCompletionForTemplateIds.createTemplateIds;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.TemplatePath;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.datamodel.ExtendedDataModelParameter;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.CustomValueResolver;
import com.redhat.qute.project.extensions.CodeLensParticipant;
import com.redhat.qute.project.extensions.DataModelTemplateParticipant;
import com.redhat.qute.project.extensions.DidChangeWatchedFilesParticipant;
import com.redhat.qute.project.extensions.ProjectExtension;
import com.redhat.qute.project.extensions.TemplateLanguageInjectionParticipant;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;
import com.redhat.qute.project.extensions.roq.data.json.JsonDataLoader;
import com.redhat.qute.project.extensions.roq.data.yaml.YamlDataLoader;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

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
public class RoqProjectExtension implements ProjectExtension, DidChangeWatchedFilesParticipant,
		DataModelTemplateParticipant, TemplateLanguageInjectionParticipant, CodeLensParticipant {

	private static final String ROG_PROJECT_EXTENSION_ID = "roq";

	// Data model parameter keys
	private static final String PAGE_PARAMETER_KEY = "page";
	private static final String SITE_PARAMETER_KEY = "site";

	// Roq folders
	private static final String TEMPLATES_FOLDER = "templates";
	private static final String LAYOUTS_FOLDER = "layouts";
	private static final String THEME_LAYOUTS_FOLDER = "theme-layouts/";
	private static final String PUBLIC_IMAGES_FOLDER = "public/images";

	// Roq variables
	private static final String THEME_VAR = ":theme/";

	// We might need to allow plugins to contribute to this at some point
	private static final Set<String> HTML_OUTPUT_EXTENSIONS = Set.of("md", "markdown", "html", "htm", "xhtml",
			"asciidoc", "adoc");
	private static final Set<String> INDEX_FILES = HTML_OUTPUT_EXTENSIONS.stream().map(e -> "index." + e)
			.collect(Collectors.toSet());

	private static final Set<String> IMAGE_EXTENSIONS = Set.of(//
			"jpg", "jpeg", "png", "gif", "webp", //
			"bmp", "tiff", "tif", "svg", "ico", //
			"avif", "heic", "heif");

	private static final Predicate<Path> LAYOUT_FILE_FILTER = p -> !isImage(p);

	private static final Predicate<Path> IMAGE_FILE_FILTER = p -> isImage(p);

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

	private static Collection<InjectionDetector> INJECTOR_DETECTORS = Collections
			.singletonList(new YamlFrontMatterDetector());

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

	private Set<String> availableThemes;

	private String currentTheme;

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

			if (availableThemes == null) {
				// Find available themes
				availableThemes = findAvailableThemes(dataModelProject.getBinaryDocuments());

				// When Qute language server is started, opened and binary templates are parsed
				// without parsing the Yaml frontmatter (because the Roq project extension is
				// not initialized and
				// template are parsed with INJECTOR_DETECTORS.
				// We need to reparse all binary and opened templates.
				reparseTemplates(dataModelProject);
			}

		} else {
			// Roq not enabled - clear data directory
			dataDir = null;
		}
	}

	private static void reparseTemplates(ExtendedDataModelProject dataModelProject) {
		// Reparse opened source document
		for (QuteTextDocument document : dataModelProject.getSourceDocuments()) {
			if (!document.isUserTag() && document.isOpened()) {
				document.reparseTemplate();
			}
		}
		// Reparse binary document
		for (QuteTextDocument document : dataModelProject.getBinaryDocuments()) {
			if (!document.isUserTag()) {
				document.reparseTemplate();
			}
		}
	}

	private static Set<String> findAvailableThemes(Collection<QuteTextDocument> documents) {
		Set<String> themes = new HashSet<>();
		for (QuteTextDocument document : documents) {
			String theme = document.getProperty(RoqConfig.ROQ_THEME.getName());
			if (theme != null) {
				themes.add(theme);
			}
		}
		return themes;
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
		} else {
			// Register old inject/cdi data resolver in the new data model project
			Collection<ResolverCache> resolvers = dataResolverCache.values();
			for (ResolverCache resolver : resolvers) {
				// Register both resolvers with the data model
				dataModelProject.getCustomValueResolvers().add(resolver.cdi);
				dataModelProject.getCustomValueResolvers().add(resolver.inject);
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
					registerRoqDataFile(file, dataModelProject);
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
	private void registerRoqDataFile(Path file, ExtendedDataModelProject dataModelProject) {
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
				registerRoqDataFile(filePath, dataModelProject);
				return true;

			} else if (changeTypes.contains(FileChangeType.Created)) {
				// New file created - register it
				registerRoqDataFile(filePath, dataModelProject);
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
	public ExtendedDataModelTemplate contributeToDataModel(String templateUri, boolean userTags, Path templatePath,
			ExtendedDataModelTemplate dataModelTemplate) {
		if (userTags) {
			// Template is an user tag, page and site must not be injected in the template
			return null;
		}
		if (dataModelTemplate == null) {
			dataModelTemplate = new ExtendedDataModelTemplate(templateUri);
		}

		TemplateType templateType = getTemplateType(templateUri, templatePath);

		// site
		if (dataModelTemplate.getParameter(SITE_PARAMETER_KEY) == null) {
			DataModelParameter site = new DataModelParameter();
			site.setKey(SITE_PARAMETER_KEY);
			site.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model.Site");
			dataModelTemplate.addParameter(new ExtendedDataModelParameter(site, dataModelTemplate));
		}

		// page
		if (dataModelTemplate.getParameter(PAGE_PARAMETER_KEY) == null) {
			DataModelParameter page = new DataModelParameter();
			page.setKey(PAGE_PARAMETER_KEY);
			page.setSourceType("io.quarkiverse.roq.frontmatter.runtime.model."
					+ (templateType == TemplateType.DOCUMENT_PAGE ? "Document" : "Normal") + "Page");
			dataModelTemplate.addParameter(new ExtendedDataModelParameter(page, dataModelTemplate));
		}

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

	@Override
	public Collection<InjectionDetector> getInjectionDetectorsFor(Path path) {
		return INJECTOR_DETECTORS;
	}

	public Path getContentDir() {
		return contentDir;
	}

	public Path getDataDir() {
		return dataDir;
	}

	@FunctionalInterface
	public static interface FileCollector {

		void collect(Path baseFolder, Path file, String templateId, boolean binary, String origin);
	}

	public void collectLayouts(Path filePath, FileCollector collector) {

		// 1. collect layouts from templates/layouts
		Path projectFolder = dataModelProject.getProjectFolder();
		if (projectFolder != null) {
			Path layoutsFolder = projectFolder.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}

		// 2. collect layouts from src/main/resources/templates/layouts
		Set<Path> sourcePaths = dataModelProject.getSourcePaths();
		for (Path sourcePath : sourcePaths) {
			Path layoutsFolder = sourcePath.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER);
			collectFiles(layoutsFolder, layoutsFolder, collector, LAYOUT_FILE_FILTER);
		}

		// 3. collect layout from theme-layouts binaries
		String currentTheme = getCurrentTheme();
		Map<String, List<QuteTextDocument>> templateIds = createTemplateIds(dataModelProject.getBinaryDocuments(),
				null);
		for (Map.Entry<String, List<QuteTextDocument>> ids : templateIds.entrySet()) {
			List<QuteTextDocument> documentsForId = ids.getValue();
			if (documentsForId.size() == 1) {
				// One document (ex : base.html) matches the short syntax temple id (ex : base)
				// here completion shows 'base' short syntax.
				String templateId = ids.getKey();
				addLayoutTemplateId(templateId, documentsForId.get(0).getOrigin(), currentTheme, collector);
			} else {
				// Several documents (ex : base.html, base.txt) matches the short syntax temple
				// id (ex : base)
				// here we generate a completion per document by using the template id of the
				// document (ex : base.html, base.txt).
				for (QuteTextDocument document : documentsForId) {
					String templateId = document.getTemplateId();
					String origin = document.getOrigin();
					addLayoutTemplateId(templateId, origin, currentTheme, collector);
				}
			}
		}
	}

	private void addLayoutTemplateId(String templateId, String origin, String currentTheme, FileCollector collector) {
		// 1. collect template from template-layouts folder
		if (templateId.startsWith(THEME_LAYOUTS_FOLDER)) {
			collector.collect(null, null, templateId, true, origin);
		}

		// 2. collect template with :theme
		for (String theme : availableThemes) {
			String start = THEME_LAYOUTS_FOLDER + theme + "/";
			if (templateId.startsWith(start)) {
				String shortId = THEME_VAR + templateId.substring(start.length());
				collector.collect(null, null, shortId, true, origin);
			}
		}
	}

	public void collectImages(Path filePath, FileCollector collector) {
		// 1. collect images from the folder of the given file path
		Path imagesFolder = filePath.getParent();
		collectFiles(imagesFolder, imagesFolder, collector, IMAGE_FILE_FILTER);

		// 2. collect images from public/images
		Path projectFolder = dataModelProject.getProjectFolder();
		if (projectFolder != null) {
			imagesFolder = projectFolder.resolve(PUBLIC_IMAGES_FOLDER);
			collectFiles(imagesFolder, imagesFolder, collector, IMAGE_FILE_FILTER);
		}
	}

	private void collectFiles(Path parent, Path dir, FileCollector collector, Predicate<Path> filterFile) {
		if (dir != null && Files.isDirectory(dir)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
				for (Path file : stream) {
					if (Files.isDirectory(file)) {
						collectFiles(parent, file, collector, filterFile);
					} else {
						if (filterFile.test(file)) {
							collector.collect(parent, file, null, false, null);
						}
					}
				}
			} catch (IOException e) {
				// Silently ignore - data directory may not exist yet
			}
		}
	}

	public static RoqProjectExtension getRoqProjectExtension(Template template) {
		return getRoqProjectExtension(template.getProject());
	}

	public static RoqProjectExtension getRoqProjectExtension(QuteProject project) {
		if (project == null) {
			return null;
		}
		ProjectExtension extension = project.getExtension(ROG_PROJECT_EXTENSION_ID);
		return extension != null ? (RoqProjectExtension) extension : null;
	}

	public TemplatePath getLayoutPath(Path filePath, String layoutFileName) {
		if (dataModelProject != null && layoutFileName.startsWith(THEME_VAR)) {
			for (String theme : availableThemes) {
				String expanded = THEME_LAYOUTS_FOLDER + theme + "/" + layoutFileName.substring(THEME_VAR.length());
				String uri = dataModelProject.findTemplateUriByTemplateId(expanded);
				if (uri != null) {
					return new TemplatePath(uri, layoutFileName, true);
				}
			}
			return new TemplatePath((String) null, layoutFileName, false);
		}

		// 1. Template from source
		TemplatePath sourcePath = getLayoutPathFromSource(layoutFileName);
		if ((sourcePath == null || !sourcePath.isExists()) && dataModelProject != null) {
			// 2. template from binary
			String binaryUri = dataModelProject.findTemplateUriByTemplateId(layoutFileName);
			if (binaryUri != null) {
				return new TemplatePath(binaryUri, layoutFileName, true);
			}
		}
		return sourcePath;
	}

	private TemplatePath getLayoutPathFromSource(String layoutFileName) {
		Path projectFolder = dataModelProject.getProjectFolder();

		// 1. Collect existing templates folder (templates,
		// src/main/resources/templates)
		// templates
		List<Path> existingTemplatesFolder = new ArrayList<>();
		if (projectFolder != null) {
			Path templatesFolder = projectFolder.resolve(TEMPLATES_FOLDER);
			if (Files.exists(templatesFolder)) {
				existingTemplatesFolder.add(templatesFolder);
			}
		}
		// src/main/resources/temmplates
		for (Path sourcePath : dataModelProject.getSourcePaths()) {
			Path templatesFolder = sourcePath.resolve(TEMPLATES_FOLDER);
			if (Files.exists(templatesFolder)) {
				existingTemplatesFolder.add(templatesFolder);
			}
		}

		// templates folder doesn't exist
		if (existingTemplatesFolder.isEmpty()) {
			Path baseDir = projectFolder != null ? projectFolder : null;
			if (baseDir == null) {
				if (dataModelProject.getSourcePaths().isEmpty()) {
					return null;
				}
				baseDir = dataModelProject.getSourcePaths().iterator().next();
			}
			return new TemplatePath(
					baseDir.resolve(TEMPLATES_FOLDER).resolve(LAYOUTS_FOLDER).resolve(layoutFileName + ".html"),
					layoutFileName);
		}

		// 2. Collect existing templates/layouts folder (templates,
		// src/main/resources/templates)
		// templates
		List<Path> existingLayoutFolder = new ArrayList<>();
		for (Path templatesFolder : existingTemplatesFolder) {
			Path layoutsFolder = templatesFolder.resolve(LAYOUTS_FOLDER);
			if (Files.exists(layoutsFolder)) {
				existingLayoutFolder.add(layoutsFolder);
			}
		}

		// layouts folder doesn't exist
		if (existingLayoutFolder.isEmpty()) {
			return new TemplatePath(
					existingTemplatesFolder.get(0).resolve(LAYOUTS_FOLDER).resolve(layoutFileName + ".html"),
					layoutFileName);
		}

		for (Path layoutsFolder : existingLayoutFolder) {
			Path layoutFile = layoutsFolder.resolve(layoutFileName + ".html");
			if (Files.exists(layoutFile)) {
				return new TemplatePath(layoutFile, layoutFileName, true);
			}
		}

		return new TemplatePath(existingLayoutFolder.get(0).resolve(layoutFileName + ".html"), layoutFileName);
	}

	public TemplatePath getImagePath(Path filePath, String imageFilePath) {
		imageFilePath = imageFilePath.trim();
		if (imageFilePath.isEmpty()) {
			return null;
		}
		if (imageFilePath.charAt(0) == '/') {
			imageFilePath = imageFilePath.substring(1);
		}
		// 1. Check if image exists in the folder of the given file path
		Path imagesFolder = filePath.getParent();
		Path imagesPath = imagesFolder.resolve(imageFilePath);
		if (Files.exists(imagesPath)) {
			return new TemplatePath(imagesPath, imageFilePath, true);
		}
		// 2. Check if image exists in the public.images folder
		Path projectFolder = dataModelProject.getProjectFolder();
		imagesFolder = projectFolder.resolve(PUBLIC_IMAGES_FOLDER);
		Path publicImagePath = imagesFolder.resolve(imageFilePath);
		return new TemplatePath(publicImagePath, imageFilePath);
	}

	public Set<String> getConfiguredCollections() {
		return configuredCollections;
	}

	@Override
	public void collectCodeLenses(Template template, SharedSettings settings, List<CodeLens> lenses,
			CancelChecker cancelChecker) {
		if (template.isUserTag() || dataModelProject.isBinary(template) || hasYamlFrontMatter(template)) {
			// Don't show "Insert FrontMatter" codelens when:
			// - template is an user tag
			// - template is binary
			// -the template already defines a yaml front matter,
			return;
		}
		CodeLens codeLens = new CodeLens(QutePositionUtility.ZERO_RANGE);
		Command command = new Command("Insert Frontmatter", RoqInsertFrontMatterCommanHandler.COMMAND_ID);
		command.setArguments(List.of(template.getUri()));
		codeLens.setCommand(command);
		lenses.add(codeLens);
	}

	private static boolean hasYamlFrontMatter(Template template) {
		Node node = template.getChildCount() > 0 ? template.getChild(0) : null;
		if (node != null && node.getKind() == NodeKind.LanguageInjection) {
			LanguageInjectionNode injectionNode = (LanguageInjectionNode) node;
			if (YamlFrontMatterDetector.YAML_FRONT_MATTER_LANGUAGE_ID.equals(injectionNode.getLanguageId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given path points to a file with an image extension. Does
	 * not check if the file actually exists on disk.
	 *
	 * @param path the file path to check
	 * @return true if the file has an image extension, false otherwise
	 */
	private static boolean isImage(Path path) {
		if (path == null) {
			return false;
		}
		String fileName = path.getFileName() != null ? path.getFileName().toString() : "";
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
			return false;
		}
		String extension = fileName.substring(dotIndex + 1).toLowerCase();
		return IMAGE_EXTENSIONS.contains(extension);
	}

	private String getCurrentTheme() {
		return currentTheme;
	}

	@Override
	public String getId() {
		return ROG_PROJECT_EXTENSION_ID;
	}
}