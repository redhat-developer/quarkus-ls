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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.config.roq.RoqConfig;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.parser.expression.Part;
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
import com.redhat.qute.project.extensions.AbstractProjectExtension;
import com.redhat.qute.project.extensions.CodeLensParticipant;
import com.redhat.qute.project.extensions.DataModelTemplateParticipant;
import com.redhat.qute.project.extensions.DidChangeWatchedFilesParticipant;
import com.redhat.qute.project.extensions.MemberResolutionParticipant;
import com.redhat.qute.project.extensions.ProjectExtension;
import com.redhat.qute.project.extensions.TemplateLanguageInjectionParticipant;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;
import com.redhat.qute.project.extensions.roq.data.json.JsonDataLoader;
import com.redhat.qute.project.extensions.roq.data.yaml.YamlDataLoader;
import com.redhat.qute.project.extensions.roq.files.ImageFileSupport;
import com.redhat.qute.project.extensions.roq.files.LayoutFileSupport;
import com.redhat.qute.project.extensions.roq.files.RoqFileSupport;
import com.redhat.qute.project.extensions.roq.files.ThemeLayoutFileSupport;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterMemberSupport;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute project extension for Roq integration.
 * 
 * @see <a href="https://github.com/quarkiverse/quarkus-roq">quarkus-roq</a>
 */
public class RoqProjectExtension extends AbstractProjectExtension
		implements ProjectExtension, DidChangeWatchedFilesParticipant, DataModelTemplateParticipant,
		TemplateLanguageInjectionParticipant, CodeLensParticipant, MemberResolutionParticipant {

	// Data model parameter keys
	private static final String PAGE_PARAMETER_KEY = "page";
	private static final String SITE_PARAMETER_KEY = "site";

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

	private Set<String> availableThemes;

	private String currentTheme;

	// Layout, Layout Theme and Image support
	private final LayoutFileSupport layoutSupport;
	private final ThemeLayoutFileSupport themeLayoutSupport;
	private final ImageFileSupport imageSupport;

	// YAML Front Matter support
	private final YamlFrontMatterMemberSupport yamlFrontMatterSupport;

	/**
	 * Creates a new Roq project extension.
	 */
	public RoqProjectExtension() {
		super(RoqConfig.PROJECT_FEATURE);
		this.layoutSupport = new LayoutFileSupport(this);
		this.themeLayoutSupport = new ThemeLayoutFileSupport(this);
		this.imageSupport = new ImageFileSupport(this);
		this.yamlFrontMatterSupport = new YamlFrontMatterMemberSupport(this);
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
	protected void init(ExtendedDataModelProject dataModelProject, boolean enabled) {
		if (enabled) {
			scanDataDir(dataModelProject);
			contentDir = dataModelProject.getConfigAsPath(RoqConfig.SITE_CONTENT_DIR);

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
			String theme = document.getProperty(RoqConfig.SITE_THEME.getName());
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
			var dataModelProject = getDataModelProject();

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

		// site : Site
		if (dataModelTemplate.getParameter(SITE_PARAMETER_KEY) == null) {
			DataModelParameter site = new DataModelParameter();
			site.setKey(SITE_PARAMETER_KEY);
			site.setSourceType(RoqConfig.SITE_CLASS);
			dataModelTemplate.addParameter(new ExtendedDataModelParameter(site, dataModelTemplate));
		}

		// page : NormalPage | DocumentPage
		if (dataModelTemplate.getParameter(PAGE_PARAMETER_KEY) == null) {
			DataModelParameter page = new DataModelParameter();
			page.setKey(PAGE_PARAMETER_KEY);
			page.setSourceType(templateType == TemplateType.DOCUMENT_PAGE ? RoqConfig.DOCUMENT_PAGE_CLASS
					: RoqConfig.NORMAL_PAGE_CLASS);
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

	public static RoqProjectExtension getRoqProjectExtension(Template template) {
		return getRoqProjectExtension(template.getProject());
	}

	public static RoqProjectExtension getRoqProjectExtension(QuteProject project) {
		if (project == null) {
			return null;
		}
		ProjectExtension extension = project.getExtension(RoqConfig.EXTENSION_ID);
		return extension != null ? (RoqProjectExtension) extension : null;
	}

	public Set<String> getConfiguredCollections() {
		return configuredCollections;
	}

	@Override
	public void collectCodeLenses(Template template, SharedSettings settings, List<CodeLens> lenses,
			CancelChecker cancelChecker) {
		var dataModelProject = getDataModelProject();
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

	public String getCurrentTheme() {
		if (currentTheme == null && !getAvailableThemes().isEmpty()) {
			currentTheme = getAvailableThemes().iterator().next();
		}
		return currentTheme;
	}

	public Set<String> getAvailableThemes() {
		return availableThemes;
	}

	// ------------ Layout, Theme Layout, Images support

	/**
	 * Collect layouts and theme layouts both.
	 * 
	 * @param filePath
	 * @param collector
	 */
	public void collectLayouts(Path filePath, RoqFileSupport.FileCollector collector) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}
		layoutSupport.collectLayouts(filePath, dataModelProject, collector);
	}

	public TemplatePath getLayoutPath(Path filePath, String layoutFileName) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return null;
		}
		return layoutSupport.getLayoutPath(filePath, layoutFileName, dataModelProject);
	}

	/**
	 * Collect only theme layouts.
	 * 
	 * @param collector
	 */
	public void collectThemeLayouts(RoqFileSupport.FileCollector collector) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}
		themeLayoutSupport.collectThemeLayouts(dataModelProject, collector);
	}

	public TemplatePath getThemeLayoutPath(Path filePath, String layoutFileName) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return null;
		}
		return themeLayoutSupport.getLayoutPath(filePath, layoutFileName, dataModelProject);
	}

	public void collectImages(Path filePath, RoqFileSupport.FileCollector collector) {
		var dataModelProject = getDataModelProject();
		if (dataModelProject == null) {
			return;
		}
		imageSupport.collectImages(filePath, dataModelProject, collector);
	}

	public TemplatePath getImagePath(Path filePath, String imageFilePath) {
		return imageSupport.getImagePath(filePath, imageFilePath);
	}

	// ======================== MemberResolutionParticipant ========================

	@Override
	public List<ResolvedJavaTypeInfo> getAdditionalTypes(ResolvedJavaTypeInfo baseType, Part previousPart, Part part, Template template) {
		return yamlFrontMatterSupport.getAdditionalTypes(baseType, previousPart, part, template);
	}

}