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

import static com.redhat.qute.utils.FileUtils.createPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateConfiguration;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.documents.QuteClosedTextDocuments;
import com.redhat.qute.project.documents.SearchInfoQuery;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagRegistry;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.NativeModeJavaTypeFilter;
import com.redhat.qute.utils.FileUtils;
import com.redhat.qute.utils.StringUtils;
import com.redhat.qute.utils.UserTagUtils;

/**
 * A Qute project.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProject {

	private static String[] TEMPLATE_VARIANTS = { "",
			".html", ".qute.html",
			".json", ".qute.json",
			".txt", ".qute.txt",
			".yaml", ".qute.yaml", ".yml", ".qute.yml" };

	private final String uri;

	private final Path templateBaseDir;

	private final QuteClosedTextDocuments closedDocuments;

	// Map which stores opened/closed documents identified by template id.
	private final Map<String /* template id */, QuteTextDocument> documents;

	private final Map<String /* Full qualified name of Java class */, CompletableFuture<ResolvedJavaTypeInfo>> resolvedJavaTypes;

	private Map<String /* Full qualified name of Java class */, JavaTypeAccessibiltyRule> targetAnnotations;

	private CompletableFuture<ExtendedDataModelProject> dataModelProjectFuture;

	private final QuteProjectRegistry projectRegistry;

	private final UserTagRegistry tagRegistry;

	private final NativeModeJavaTypeFilter filterInNativeMode;

	private final TemplateValidator validator;

	private final QuteProjectFilesWatcher watcher;

	public QuteProject(ProjectInfo projectInfo, QuteProjectRegistry projectRegistry,
			TemplateValidator validator) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
		this.documents = new HashMap<>();
		this.closedDocuments = new QuteClosedTextDocuments(this, documents);
		this.projectRegistry = projectRegistry;
		this.resolvedJavaTypes = new HashMap<>();
		this.tagRegistry = new UserTagRegistry(this, templateBaseDir, projectRegistry);
		this.filterInNativeMode = new NativeModeJavaTypeFilter(this);
		this.validator = validator;
		// Create a Java file watcher to track create/delete Qute file in
		// src/main/resources/templates to update cache of closed documents
		// ONLY if LSP client cannot support DidChangeWatchedFiles.
		this.watcher = !projectRegistry.isDidChangeWatchedFilesSupported() ? createFilesWatcher(this) : null;
	}

	private static QuteProjectFilesWatcher createFilesWatcher(QuteProject project) {
		try {
			return new QuteProjectFilesWatcher(project);
		} catch (IOException e) {
			return null;
		}
	}

	public void validateClosedTemplates() {
		if (validator != null) {
			// Load closed document if needed and validate all closed documents when data
			// model is ready.
			closedDocuments.loadClosedTemplatesIfNeeded();
			for (QuteTextDocument document : documents.values()) {
				if (!document.isOpened()) {
					validator.triggerValidationFor(document);
				}
			}
		}
	}

	public QuteProjectRegistry getProjectRegistry() {
		return projectRegistry;
	}

	/**
	 * Returns the templates base dir folder of the project (ex :
	 * src/main/resources/templates).
	 *
	 * @return the templates base dir folder of the project (ex :
	 *         src/main/resources/templates).
	 */
	public Path getTemplateBaseDir() {
		return templateBaseDir;
	}

	/**
	 * Returns the template id of the given template file path.
	 * 
	 * @param templateFilePath the Qute template file path.
	 * 
	 * @return the template id of the given template file path.
	 */
	public String getTemplateId(Path templateFilePath) {
		if (templateFilePath == null || templateBaseDir == null) {
			return null;
		}
		try {
			return templateBaseDir.relativize(templateFilePath).toString().replace('\\', '/');
		} catch (Exception e) {
			return templateFilePath.getFileName().toString();
		}
	}

	/**
	 * Returns true if the document retrieved by template id is opened and false
	 * otherwise.
	 * 
	 * @param templateId the template id.
	 * 
	 * @return true if the document retrieved by template id is opened and false
	 *         otherwise.
	 */
	public boolean isTemplateOpened(String templateId) {
		QuteTextDocument document = findDocumentByTemplateId(templateId);
		return document != null && document.isOpened();
	}

	/**
	 * Returns the project Uri.
	 *
	 * @return the project Uri.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Returns the insert parameter list with the given name
	 * <code>insertParamater</code> ({#insert name}) declared in the template
	 * identified by the given template id and null otherwise.
	 * 
	 * @param templateId      the template id.
	 * @param insertParamater the name of the insert parameter. If the name is
	 *                        equals to {@link SearchInfoQuery#ALL}, the methods
	 *                        will returns all declared insert parameters.
	 * @return the insert parameter list with the given name
	 *         <code>insertParamater</code> ({#insert name}) declared in the
	 *         template
	 *         identified by the given template id and null otherwise.
	 */
	public List<Parameter> findInsertTagParameter(String templateId, String insertParamater) {
		closedDocuments.loadClosedTemplatesIfNeeded();
		QuteTextDocument document = findDocumentByTemplateId(templateId);
		if (document != null) {
			return document.findInsertTagParameter(insertParamater);
		}
		return null;
	}

	public List<Section> findSectionsByTag(String tag) {
		closedDocuments.loadClosedTemplatesIfNeeded();
		List<Section> allSections = new ArrayList<>();
		for (QuteTextDocument document : documents.values()) {
			List<Section> sections = document.findSectionsByTag(tag);
			if (sections != null && !sections.isEmpty()) {
				allSections.addAll(sections);
			}
		}
		return allSections;
	}

	private QuteTextDocument findDocumentByTemplateId(String templateId) {
		if (templateId.indexOf('.') != -1) {
			// ex: base.html
			return documents.get(templateId);
		}
		// ex : base
		for (String variant : getTemplateVariants()) {
			String id = templateId + variant;
			QuteTextDocument document = documents.get(id);
			if (document != null) {
				return document;
			}
		}
		return null;
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(QuteTextDocument document) {
		documents.put(document.getTemplateId(), document);
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(QuteTextDocument document) {
		Path path = FileUtils.createPath(document.getUri());
		closedDocuments.onDidCloseTemplate(path);
	}

	/**
	 * Save a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidSaveTextDocument(QuteTextDocument document) {
		UserTag userTag = getUserTag(document.getTemplate());
		if (userTag != null) {
			// The user tag has been saved, refresh it.
			userTag.clear();
		}
	}

	/**
	 * Delete a Qute template file.
	 *
	 * @param templateFilePath the Qute template file path.
	 */
	public QuteTextDocument onDidDeleteTemplate(Path templateFilePath) {
		return closedDocuments.onDidDeleteTemplate(templateFilePath);
	}

	/**
	 * Create a Qute template file.
	 *
	 * @param templateFilePath the Qute template file path.
	 */
	public QuteTextDocument onDidCreateTemplate(Path templateFilePath) {
		return closedDocuments.onDidCreateTemplate(templateFilePath);
	}

	/**
	 * Returns list of all opened/closed Qute template document of the project.
	 * 
	 * @return list of all opened/closed Qute template document of the project.
	 */
	public Collection<QuteTextDocument> getDocuments() {
		closedDocuments.loadClosedTemplatesIfNeeded();
		return documents.values();
	}

	public CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(String typeName) {
		return resolvedJavaTypes.get(typeName);
	}

	void registerResolvedJavaType(String typeName, CompletableFuture<ResolvedJavaTypeInfo> future) {
		resolvedJavaTypes.put(typeName, future);
		future //
				.thenApply(c -> {
					if (targetAnnotations != null) {
						// Update target annotations @TemplateData, @RegisterForReflection
						updateTargetAnnotation(c, targetAnnotations);
					}
					return c;
				});
	}

	public CompletableFuture<ExtendedDataModelProject> getDataModelProject() {
		if (dataModelProjectFuture == null || dataModelProjectFuture.isCancelled()
				|| dataModelProjectFuture.isCompletedExceptionally()) {
			dataModelProjectFuture = null;
			dataModelProjectFuture = loadDataModelProject();
		}
		return dataModelProjectFuture;
	}

	protected synchronized CompletableFuture<ExtendedDataModelProject> loadDataModelProject() {
		if (dataModelProjectFuture != null) {
			return dataModelProjectFuture;
		}
		QuteDataModelProjectParams params = new QuteDataModelProjectParams();
		params.setProjectUri(getUri());
		return getDataModelProject(params) //
				.thenApply(project -> {
					if (project == null) {
						return null;
					}
					return new ExtendedDataModelProject(project);
				})
				.thenApply(p -> {
					tagRegistry.refreshDataModel();
					return p;
				});
	}

	protected CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return projectRegistry.getDataModelProject(params);
	}

	public void resetJavaTypes() {
		if (dataModelProjectFuture != null) {
			dataModelProjectFuture.cancel(true);
			dataModelProjectFuture = null;
		}
		resolvedJavaTypes.clear();
		targetAnnotations = null;
	}

	/**
	 * Returns the template configuration of the project.
	 *
	 * @return the template configuration of the project.
	 */
	public TemplateConfiguration getTemplateConfiguration() {
		// TODO : load template configuration from JDT side
		return null;
	}

	/**
	 * Returns list of source ('src/main/resources/templates/tags') user tags.
	 *
	 * @return list of source ('src/main/resources/templates/tags') user tags.
	 */
	public Collection<UserTag> getSourceUserTags() {
		return tagRegistry.getSourceUserTags();
	}

	/**
	 * Returns list of binary ('templates.tags') user tags.
	 *
	 * @return list of binary ('templates.tags') user tags.
	 */
	public CompletableFuture<List<UserTag>> getBinaryUserTags() {
		return tagRegistry.getBinaryUserTags();
	}

	/**
	 * Find user tag by the given tagName and null otherwise.
	 *
	 * @param tagName the tag name.
	 *
	 * @return user tag by the given tagName and null otherwise.
	 */
	public UserTag findUserTag(String tagName) {
		// Source tags
		Collection<UserTag> tags = getSourceUserTags();
		for (UserTag userTag : tags) {
			if (tagName.equals(userTag.getName())) {
				return userTag;
			}
		}
		// Binary tags
		tags = getBinaryUserTags().getNow(Collections.emptyList());
		for (UserTag userTag : tags) {
			if (tagName.equals(userTag.getName())) {
				return userTag;
			}
		}
		return null;
	}

	/**
	 * Returns the user tag from the given template and null otherwise.
	 *
	 * @param template the Qute template.
	 *
	 * @return the user tag from the given template and null otherwise.
	 */
	public UserTag getUserTag(Template template) {
		if (!UserTagUtils.isUserTag(template)) {
			return null;
		}
		String templateId = template.getTemplateId();
		int index = templateId.indexOf('.');
		if (index != -1) {
			templateId = templateId.substring(0, index);
		}
		for (UserTag userTag : getSourceUserTags()) {
			if (userTag.getTemplateId().equals(templateId)) {
				return userTag;
			}
		}
		return null;
	}

	/**
	 * Collect user tags suggestions.
	 *
	 * @param completionRequest completion request.
	 * @param prefixFilter      prefix filter.
	 * @param suffixToFind      suffix to found to eat it when completion snippet is
	 *                          applied.
	 * @param completionItems   set of completion items to update
	 */
	public void collectUserTagSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			Set<CompletionItem> completionItems) {
		tagRegistry.collectUserTagSuggestions(completionRequest, prefixFilter, suffixToFind, completionItems);
	}

	/**
	 * Returns the src/main/resources/templates/tags directory.
	 *
	 * @return the src/main/resources/templates/tags directory.
	 */
	public Path getTagsDir() {
		return tagRegistry.getTagsDir();
	}

	public JavaTypeAccessibiltyRule getJavaTypeAccessibiltyInNativeMode(String javaTypeName) {
		if (getJavaTypesSupportedInNativeMode().contains(javaTypeName)) {
			return JavaTypeAccessibiltyRule.ALLOWED_WITHOUT_RESTRICTION;
		}

		// Native images mode : the java reflection is supported only if the Java type
		// is
		// annotated with:
		// - @TemplateData
		// - @RegisterForReflection

		// Case 1 : Item
		// javaType = Item
		// @TemplateData
		// public class Item
		//
		// Or
		//
		// @RegisterForReflection
		// public class Item
		// return Arrays.asList(javaType);

		// Case 1 : BigDecimal
		// javaType = BigDecimal
		// @TemplateData(target = BigDecimal.class)
		// public class Item
		//
		// Or
		//
		// @RegisterForReflection(targets = {BigDecimal.class})
		// public class Item
		if (targetAnnotations == null) {
			targetAnnotations = loadTargetAnnotations();
		}
		return targetAnnotations.get(javaTypeName);
	}

	private synchronized Map<String, JavaTypeAccessibiltyRule> loadTargetAnnotations() {
		if (targetAnnotations != null) {
			return targetAnnotations;
		}

		Map<String /* Full qualified name of Java class */, JavaTypeAccessibiltyRule> targetAnnotations = new HashMap<>();
		resolvedJavaTypes.values().forEach(future -> {
			updateTargetAnnotation(future.getNow(null), targetAnnotations);
		});
		return targetAnnotations;
	}

	private void updateTargetAnnotation(ResolvedJavaTypeInfo baseType,
			Map<String /* Full qualified name of Java class */, JavaTypeAccessibiltyRule> targetAnnotations) {
		if (baseType == null) {
			return;
		}
		if (baseType.getTemplateDataAnnotations() != null && !baseType.getTemplateDataAnnotations().isEmpty()) {
			// Loop for @TemplateData
			for (TemplateDataAnnotation annotation : baseType.getTemplateDataAnnotations()) {
				// Merge information about @TemplateData (ignore, etc) in the proper
				// JavaTypeAccessResult
				String target = StringUtils.isEmpty(annotation.getTarget()) ? baseType.getName()
						: annotation.getTarget();
				JavaTypeAccessibiltyRule result = getJavaTypeAccessibiltyRule(target, targetAnnotations);
				result.merge(annotation);
			}
		}
		if (baseType.getRegisterForReflectionAnnotation() != null) {
			// Merge information about @RegisterForReflection (fields, etc) in the proper
			// JavaTypeAccessResult
			List<String> targets = baseType.getRegisterForReflectionAnnotation().getTargets();
			if (targets != null && !targets.isEmpty()) {
				for (String target : targets) {
					JavaTypeAccessibiltyRule result = getJavaTypeAccessibiltyRule(target, targetAnnotations);
					result.merge(baseType.getRegisterForReflectionAnnotation());
				}
			} else {
				String target = baseType.getName();
				JavaTypeAccessibiltyRule result = getJavaTypeAccessibiltyRule(target, targetAnnotations);
				result.merge(baseType.getRegisterForReflectionAnnotation());
			}
		}
	}

	private static JavaTypeAccessibiltyRule getJavaTypeAccessibiltyRule(String target,
			Map<String, JavaTypeAccessibiltyRule> targetAnnotations) {
		return targetAnnotations.computeIfAbsent(target, (x -> new JavaTypeAccessibiltyRule()));
	}

	public JavaTypeFilter getJavaTypeFilterInNativeMode() {
		return filterInNativeMode;
	}

	public Collection<? extends String> getJavaTypesSupportedInNativeMode() {
		ExtendedDataModelProject dataModel = getDataModelProject().getNow(null);
		if (dataModel == null) {
			return Collections.emptySet();
		}
		return dataModel.getJavaTypesSupportedInNativeMode();
	}

	public void dispose() {
		if (watcher != null) {
			watcher.stop();
		}
	}

	/**
	 * Returns all supported file extension for a Qute template (*.html, *.txt,
	 * etc).
	 * 
	 * @return all supported file extension for a Qute template (*.html, *.txt,
	 *         etc).
	 */
	public String[] getTemplateVariants() {
		return TEMPLATE_VARIANTS;
	}

}