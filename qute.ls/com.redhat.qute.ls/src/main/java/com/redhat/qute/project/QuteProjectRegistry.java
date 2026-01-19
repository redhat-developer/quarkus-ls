/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import static com.redhat.qute.services.QuteCompletableFutures.EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent.ProjectChangeInfo;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteJavadocProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolversRegistry;
import com.redhat.qute.project.documents.QuteOpenedTextDocument;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.project.extensions.DidChangeWatchedFilesParticipant;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.ReflectionJavaTypeFilter;
import com.redhat.qute.settings.QuteNativeSettings;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry implements QuteDataModelProjectProvider, QuteUserTagProvider, QuteJavadocProvider {

	private final ValueResolversRegistry valueResolversRegistry;

	private final Map<String /* project uri */, QuteProject> projects;

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteResolvedJavaTypeProvider resolvedTypeProvider;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final QuteUserTagProvider userTagProvider;

	private final QuteJavaTypesProvider javaTypeProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	private final QuteJavadocProvider javadocProvider;

	private final TemplateValidator validator;

	private final Supplier<ProgressSupport> progressSupportProvider;

	private boolean didChangeWatchedFilesSupported;

	private boolean asyncValidation = true;

	public QuteProjectRegistry(QuteProjectInfoProvider projectInfoProvider, QuteJavaTypesProvider javaTypeProvider,
			QuteJavaDefinitionProvider definitionProvider, QuteResolvedJavaTypeProvider resolvedClassProvider,
			QuteDataModelProjectProvider dataModelProvider, QuteUserTagProvider userTagsProvider,
			QuteJavadocProvider javadocProvider, TemplateValidator validator,
			Supplier<ProgressSupport> progressSupportProvider) {
		this.projectInfoProvider = projectInfoProvider;
		this.javaTypeProvider = javaTypeProvider;
		this.definitionProvider = definitionProvider;
		this.projects = new HashMap<>();
		this.resolvedTypeProvider = resolvedClassProvider;
		this.dataModelProvider = dataModelProvider;
		this.userTagProvider = userTagsProvider;
		this.javadocProvider = javadocProvider;
		this.valueResolversRegistry = new ValueResolversRegistry();
		this.validator = validator;
		this.progressSupportProvider = progressSupportProvider;
	}

	/**
	 * Enable/disable did change watched file support.
	 *
	 * @param didChangeWatchedFilesSupported true if did changed file is supported
	 *                                       by the LSP client and false otherwise.
	 */
	public void setDidChangeWatchedFilesSupported(boolean didChangeWatchedFilesSupported) {
		this.didChangeWatchedFilesSupported = didChangeWatchedFilesSupported;
	}

	/**
	 * Returns true if did changed file is supported by the LSP client and false
	 * otherwise.
	 *
	 * @return true if did changed file is supported by the LSP client and false
	 *         otherwise.
	 */
	public boolean isDidChangeWatchedFilesSupported() {
		return didChangeWatchedFilesSupported;
	}

	/**
	 * Returns the Qute project by the given uri <code>projectUri</code> and null
	 * otherwise.
	 *
	 * @param projectUri the project Uri.
	 *
	 * @return the Qute project by the given uri <code>projectUri</code> and null
	 *         otherwise.
	 */
	public QuteProject getProject(String projectUri) {
		return projects.get(projectUri);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo the project information.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	public QuteProject getProject(ProjectInfo projectInfo) {
		return getProject(projectInfo, true);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo    the project information.
	 * @param validateOnLoad true if validation of templates must be validated if
	 *                       project is created and false otherwise.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	private QuteProject getProject(ProjectInfo projectInfo, boolean validateOnLoad) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			project = registerProjectSync(projectInfo);
			if (validator != null && validateOnLoad) {
				QuteProject newProject = project;
				// Validate closed Qute template on project load.
				if (asyncValidation) {
					CompletableFuture.runAsync(() -> newProject.validateClosedTemplates(null));
				} else {
					newProject.validateClosedTemplates(null);
				}
			}
		}
		return project;
	}

	private synchronized QuteProject registerProjectSync(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			return project;
		}
		project = createProject(projectInfo);
		registerProject(project);
		return project;
	}

	protected QuteProject createProject(ProjectInfo projectInfo) {
		return new QuteProject(projectInfo, this);
	}

	protected TemplateValidator getValidator() {
		return validator;
	}

	protected void registerProject(QuteProject project) {
		projects.put(project.getUri(), project);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidOpenTextDocument(document);
		}
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidCloseTextDocument(document);
		}
	}

	public void onDidSaveTextDocument(QuteOpenedTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidSaveTextDocument(document);
		}
	}

	protected CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return resolvedTypeProvider.getResolvedJavaType(params);
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		Set<ProjectChangeInfo> projects = event.getProjects();
		for (ProjectChangeInfo projectIndo : projects) {
			QuteProject project = getProject(projectIndo.getUri());
			if (project != null) {
				project.resetJavaTypes(projectIndo.getSources());
			}
		}
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		QuteProject existingProject = template.getProject();
		if (existingProject == null) {
			// The project uri is not already get (it occurs when Qute template is opened
			// and the project information takes some times).
			// Load the project information and call the data model.
			return template.getProjectFuture() //
					.thenCompose(projectInfo -> {
						if (projectInfo == null) {
							return EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
						}
						QuteProject project = getProject(projectInfo);
						return project.getDataModelTemplate(template);
					});
		}
		return existingProject.getDataModelTemplate(template);
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return javaTypeProvider.getJavaTypes(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return dataModelProvider.getDataModelProject(params);
	}

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return userTagProvider.getUserTags(params);
	}

	/**
	 * Returns the commons value resolvers available for any Qute project.
	 * 
	 * @return the commons value resolvers available for any Qute project.
	 */
	List<MethodValueResolver> getCommmonsResolvers() {
		return valueResolversRegistry.getResolvers();
	}

	/**
	 * Returns the java type filter according the given root java type and the
	 * native mode.
	 *
	 * @param rootJavaType         the Java root type.
	 * @param nativeImagesSettings the native images settings.
	 *
	 * @return the java type filter according the given root java type and the
	 *         native mode.
	 */
	public JavaTypeFilter getJavaTypeFilter(String projectUri, QuteNativeSettings nativeImagesSettings) {
		if (nativeImagesSettings != null && nativeImagesSettings.isEnabled()) {
			if (projectUri != null) {
				QuteProject project = getProject(projectUri);
				if (project != null) {
					return project.getJavaTypeFilterInNativeMode();
				}
			}
		}
		return ReflectionJavaTypeFilter.INSTANCE;
	}

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return javadocProvider.getJavadoc(params);
	}

	private QuteProject findProjectFor(Path path) {
		for (QuteProject project : projects.values()) {
			if (isBelongToProject(path, project)) {
				return project;
			}
		}
		return null;
	}

	private static boolean isBelongToProject(Path path, QuteProject project) {
		return (project.isInProjectFolder(path) || project.isInTemplateFolders(path)
				|| project.isInSourceFolders(path));
	}

	public Collection<QuteProject> getProjects() {
		return projects.values();
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		Set<QuteProject> projects = new HashSet<>();
		List<FileEvent> changes = params.getChanges();
		// For some reason, vscode fill changes with several FileEvent which are the
		// same
		// Filter it.
		Map<Path, Set<FileChangeType>> fileEvents = toFileEventMap(changes);
		for (Map.Entry<Path, Set<FileChangeType>> pathEvent : fileEvents.entrySet()) {
			Path filePath = pathEvent.getKey();
			Set<FileChangeType> changeTypes = pathEvent.getValue();
			QuteProject project = findProjectFor(filePath);
			if (project != null) {
				if (project.isInTemplateFolders(filePath)) {
					// Some qute templates are deleted, created, or changed
					// Collect impacted Qute projects
					Path templatePath = filePath;
					String templateId = project.getTemplateId(templatePath);
					if (project.isTemplateOpened(templateId)) {
						projects.add(project);
					} else {
						// In case of closed document, we collect the project and update the cache
						if (changeTypes.contains(FileChangeType.Changed)
								|| changeTypes.contains(FileChangeType.Created)) {
							// The template is created, update the cache and collect the project
							QuteTextDocument closedTemplate = project.onDidCreateTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
							}
						} else if (changeTypes.contains(FileChangeType.Deleted)) {
							// The template is deleted, update the cache, collect the project and publish
							// empty diagnostics for this file
							QuteTextDocument closedTemplate = project.onDidDeleteTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
								if (validator != null) {
									validator.clearDiagnosticsFor(FileUtils.toUri(filePath));
								}
							}
						}
					}
				}
				for (DidChangeWatchedFilesParticipant participant : project.getExtensions()) {
					if (participant.isEnabled()) {
						if (participant.didChangeWatchedFile(filePath, changeTypes)) {
							projects.add(project);
						}
					}
				}
			}
		}

		if (projects.isEmpty()) {
			return;
		}

		// trigger validation for all opened and closed Qute template files which belong
		// to the project list.
		if (validator != null) {
			validator.triggerValidationFor(projects);
		}
	}

	private Map<Path, Set<FileChangeType>> toFileEventMap(List<FileEvent> changes) {
		Map<Path, Set<FileChangeType>> result = new HashMap<>();
		for (FileEvent fileEvent : changes) {
			String fileUri = fileEvent.getUri();
			Path filePath = FileUtils.createPath(fileUri);
			if (filePath != null) {
				Set<FileChangeType> changeTypes = result.get(filePath);
				if (changeTypes == null) {
					changeTypes = new HashSet<>();
					result.put(filePath, changeTypes);
				}
				changeTypes.add(fileEvent.getType());
			}
		}
		return result;
	}

	public void dispose() {
		for (QuteProject project : projects.values()) {
			project.dispose();
		}
	}

	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return projectInfoProvider.getProjectInfo(params);
	}

	public void loadQuteProjects(Collection<ProjectInfo> projects) {
		// 1. Load all Qute projects
		for (ProjectInfo projectInfo : projects) {
			// Load the Qute project
			loadQuteProject(projectInfo);
		}
		// 2. Update project dependencies
		for (ProjectInfo projectInfo : projects) {
			if (projectInfo.getProjectDependencyUris() != null && !projectInfo.getProjectDependencyUris().isEmpty()) {
				QuteProject project = getProject(projectInfo.getUri());
				for (String projectDependencyUri : projectInfo.getProjectDependencyUris()) {
					QuteProject projectDependency = getProject(projectDependencyUri);
					if (projectDependency != null) {
						project.getProjectDependencies().add(projectDependency);
					}
				}
			}
		}

	}

	/**
	 * Load the Qute project from the given Qute project information.
	 * 
	 * @param projectInfo the project information.
	 */
	private QuteProject loadQuteProject(ProjectInfo projectInfo) {
		// Get the LSP client progress support (or null if LSP client cannot support
		// progress)
		ProgressSupport progressSupport = progressSupportProvider.get();
		ProgressContext progressContext = progressSupport != null ? new ProgressContext(progressSupport) : null;
		String projectName = projectInfo.getUri();

		if (progressContext != null) {
			progressContext.startProgress("Loading '" + projectName + "' project",
					"Trying to load '" + projectName + "' as Qute project.");
		}

		// Load Qute project from the Java component (collect Java data model)
		QuteProject project = getProject(projectInfo, false);

		if (progressContext != null) {
			progressContext.report("Loading data model for '" + projectName + "' Qute project.", 10);
		}

		project.getDataModelProject().thenAccept(dataModel -> {
			// The Java data model is collected for the project, validate all templates of
			// the project
			if (progressContext != null) {
				progressContext.report("Loading Qute templates for '" + projectName + "' Qute project.", 40);
			}
			// Validate Qute templates
			project.validateClosedTemplates(progressContext);

			// End progress
			if (progressContext != null) {
				progressContext.endProgress();
			}

		}).exceptionally((a) -> {
			if (progressContext != null) {
				progressContext.endProgress();
			}
			return null;
		});
		return project;
	}

	public void projectAdded(ProjectInfo project) {
		loadQuteProject(project);
	}

	public void projectRemoved(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			project.dispose();
			projects.remove(projectUri);
		}
	}

	public void setAsyncValidation(boolean asyncValidation) {
		this.asyncValidation = asyncValidation;
	}

	public boolean isAsyncValidation() {
		return asyncValidation;
	}
}