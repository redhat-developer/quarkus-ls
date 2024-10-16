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

import static com.redhat.qute.project.JavaDataModelCache.isSameType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;

import com.redhat.qute.commons.DocumentFormat;
import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.commons.jaxrs.JaxRsParamKind;
import com.redhat.qute.commons.jaxrs.RestParam;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.LiteralSupport;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateConfiguration;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.FieldValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MessageValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.TypeValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.project.documents.QuteClosedTextDocuments;
import com.redhat.qute.project.documents.SearchInfoQuery;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagRegistry;
import com.redhat.qute.services.QuteCompletableFutures;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.NativeModeJavaTypeFilter;
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

	private final List<TemplateRootPath> templateRootPaths;

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

	private final JavaDataModelCache javaCache;

	private List<QuteProject> projectDependencies;

	public QuteProject(ProjectInfo projectInfo, QuteProjectRegistry projectRegistry,
			TemplateValidator validator) {
		this.uri = projectInfo.getUri();
		this.templateRootPaths = projectInfo.getTemplateRootPaths();
		this.documents = new HashMap<>();
		this.closedDocuments = new QuteClosedTextDocuments(this, documents);
		this.projectRegistry = projectRegistry;
		this.resolvedJavaTypes = new HashMap<>();
		this.tagRegistry = new UserTagRegistry(this, templateRootPaths, projectRegistry);
		this.filterInNativeMode = new NativeModeJavaTypeFilter(this);
		this.validator = validator;
		// Create a Java file watcher to track create/delete Qute file in
		// src/main/resources/templates to update cache of closed documents
		// ONLY if LSP client cannot support DidChangeWatchedFiles.
		this.watcher = !projectRegistry.isDidChangeWatchedFilesSupported() ? createFilesWatcher(this) : null;
		this.javaCache = new JavaDataModelCache(this);
		this.projectDependencies = new ArrayList<>();
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
	public List<TemplateRootPath> getTemplateRootPaths() {
		return templateRootPaths;
	}

	/**
	 * Returns the template id of the given template file path.
	 * 
	 * @param templateFilePath the Qute template file path.
	 * 
	 * @return the template id of the given template file path.
	 */
	public String getTemplateId(Path templateFilePath) {
		if (templateFilePath == null || templateRootPaths == null) {
			return null;
		}
		for (TemplateRootPath rootPath : templateRootPaths) {
			Path basePath = rootPath.getBasePath();
			if (basePath != null) {
				if (templateFilePath.startsWith(basePath)) {
					try {
						return basePath.relativize(templateFilePath).toString().replace('\\', '/');
					} catch (Exception e) {
						// Do nothing
					}
				}
			}
		}
		return templateFilePath.getFileName().toString();
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

	public List<QuteProject> getProjectDependencies() {
		return projectDependencies;
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
		if (StringUtils.isEmpty(tagName)) {
			return null;
		}
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
	 * Returns the preferred tags dir (ex : src/main/resources/templates/tags)
	 * directory and null otherwise.
	 * 
	 * @return the preferred tags dir (ex : src/main/resources/templates/tags)
	 *         directory and null otherwise.
	 */
	public Path getPreferredTagsDir() {
		return tagRegistry.getPreferredTagsDir();
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

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Part part) {
		return javaCache.resolveJavaType(part);
	}

	CompletableFuture<ResolvedJavaTypeInfo> wrap2(CompletableFuture<ResolvedJavaTypeInfo> future) {
		if (future.isDone()) {
			ResolvedJavaTypeInfo resolvedJavaType = future.getNow(null);
			return wrap(resolvedJavaType);
		}
		return future.thenCompose(resolvedJavaType -> wrap(resolvedJavaType));
	}

	CompletableFuture<ResolvedJavaTypeInfo> wrap(ResolvedJavaTypeInfo resolvedJavaType) {
		if (resolvedJavaType != null && resolvedJavaType.isWrapperType()) {
			List<JavaParameterInfo> types = resolvedJavaType.getTypeParameters();
			if (types != null && !types.isEmpty()) {
				// java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>>
				JavaParameterInfo type = types.get(0);
				String javaTypeToResolve = type.getType(); // java.util.List<org.acme.Item>
				// Here
				// - javaTypeToResolve = java.util.List<org.acme.Item>
				// - iterTypeName = org.acme.Item
				return resolveJavaType(javaTypeToResolve);
			}
		}
		return CompletableFuture.completedFuture(resolvedJavaType);
	}

	public ResolvedJavaTypeInfo resolveJavaTypeSync(String className) {
		return resolveJavaType(className).getNow(QuteCompletableFutures.RESOLVING_JAVA_TYPE);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className) {
		return javaCache.resolveJavaType(className);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className, boolean wrap) {
		return javaCache.resolveJavaType(className, wrap);
	}

	public ResolvedJavaTypeInfo resolveJavaTypeSync(Parameter parameter) {
		return resolveJavaType(parameter).getNow(QuteCompletableFutures.RESOLVING_JAVA_TYPE);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parameter parameter) {
		return javaCache.resolveJavaType(parameter);
	}

	public InvalidMethodReason getInvalidMethodReason(String property, ResolvedJavaTypeInfo resolvedType) {
		if (resolvedType == null) {
			return InvalidMethodReason.Unknown;
		}
		// Search in the java root type
		InvalidMethodReason reason = resolvedType.getInvalidMethodReason(property);
		if (reason != null) {
			return reason;
		}

		if (resolvedType.getExtendedTypes() != null) {
			// Search in extended types
			for (String extendedType : resolvedType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedExtendedType = resolveJavaTypeSync(extendedType);
				if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedExtendedType)) {
					reason = resolvedExtendedType.getInvalidMethodReason(property);
					if (reason != null) {
						return reason;
					}
				}
			}
		}
		return null;
	}

	public JavaMemberResult findProperty(Part part, ResolvedJavaTypeInfo baseType, boolean nativeMode) {
		return findProperty(baseType, part.getPartName(), nativeMode);
	}

	/**
	 * Returns the Java method / field from the given Java type
	 * <code>baseType</code> which matches the given property name
	 * <code>property</code>.
	 *
	 * @param baseType   the Java base object type.
	 * @param property   the property name to search.
	 * @param nativeMode the native image mode.
	 * @param projectUri the project Uri.
	 *
	 * @return the Java method / field from the given Java type
	 *         <code>baseType</code> which matches the given property name
	 *         <code>property</code>.
	 */
	private JavaMemberResult findProperty(ResolvedJavaTypeInfo baseType, String property, boolean nativeMode) {
		JavaMemberResult result = new JavaMemberResult();

		if (!nativeMode) {

			// Find member with Java reflection.
			JavaMemberInfo member = findPropertyWithJavaReflection(baseType, property);
			if (member != null) {
				result.setMember(member);
				return result;
			}

			// Find member with value resolvers.
			member = findValueResolver(baseType, property);
			if (member != null) {
				result.setMember(member);
			}
			return result;
		}

		// Find member with value resolvers.
		JavaMemberInfo member = findValueResolver(baseType, property);
		if (member != null) {
			result.setMember(member);
			return result;
		}

		// Find member with Java reflection.
		member = findPropertyWithJavaReflection(baseType, property);
		if (member != null) {
			result.setMember(member);
			return result;
		}
		return result;
	}

	private JavaMemberInfo findPropertyWithJavaReflection(ResolvedJavaTypeInfo baseType, String property) {
		return findPropertyWithJavaReflection(baseType, property, new HashSet<>());
	}

	/**
	 * Returns the Java member from the given base type which matches the given
	 * property by using Java reflection and null otherwise.
	 *
	 * @param baseType   the Java type.
	 * @param property   the property member.
	 * @param projectUri the project Uri.
	 * @param visited    the java types that have already been visited
	 *
	 * @return the Java member from the given base type which matches the given
	 *         property by using Java reflection and null otherwise.
	 */
	private JavaMemberInfo findPropertyWithJavaReflection(ResolvedJavaTypeInfo baseType, String property,
			Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(baseType)) {
			return null;
		}
		visited.add(baseType);

		// Search in the java root type
		String getterMethodName = computeGetterName(property);
		String booleanGetterName = computeBooleanGetterName(property);
		JavaMemberInfo memberInfo = findMember(baseType, property, getterMethodName, booleanGetterName);
		if (memberInfo != null) {
			return memberInfo;
		}
		if (baseType.getExtendedTypes() != null) {
			// Search in extended types
			for (String superType : baseType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaTypeSync(superType);
				if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedSuperType)) {
					JavaMemberInfo superMemberInfo = findPropertyWithJavaReflection(resolvedSuperType, property,
							visited);
					if (superMemberInfo != null) {
						return superMemberInfo;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the member retrieved by the given property and null otherwise.
	 *
	 * @param property the property
	 * @return the member retrieved by the given property and null otherwise.
	 */
	private JavaMemberInfo findMember(ResolvedJavaTypeInfo resolvedType, String propertyOrMethodName,
			String getterMethodName, String booleanGetterName) {
		JavaFieldInfo fieldInfo = findField(resolvedType, propertyOrMethodName);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(resolvedType, propertyOrMethodName, getterMethodName, booleanGetterName);
	}

	/**
	 * Returns the member field retrieved by the given name and null otherwise.
	 *
	 * @param baseType  the Java base type.
	 * @param fieldName the field name
	 *
	 * @return the member field retrieved by the given property and null otherwise.
	 */
	protected static JavaFieldInfo findField(ResolvedJavaTypeInfo baseType, String fieldName) {
		List<JavaFieldInfo> fields = baseType.getFields();
		if (fields == null || fields.isEmpty() || isEmpty(fieldName)) {
			return null;
		}
		for (JavaFieldInfo field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the member method retrieved by the given property or method name and
	 * null otherwise.
	 *
	 * @param baseType          the Java base type.
	 * @param methodName        property or method name.
	 * @param getterMethodName  the getter method name.
	 * @param booleanGetterName the boolean getter method name.
	 *
	 * @return the member field retrieved by the given property or method name and
	 *         null otherwise.
	 */
	protected static JavaMethodInfo findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			String getterMethodName, String booleanGetterName) {
		List<JavaMethodInfo> methods = baseType.getMethods();
		if (methods == null || methods.isEmpty() || isEmpty(methodName)) {
			return null;
		}
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, methodName, getterMethodName, booleanGetterName)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns the Java method from the given Java type <code>baseType</code> which
	 * matches the given method name <code>methodName</code> with the given
	 * parameter types <code>parameterTypes</code>.
	 *
	 * @param baseType       the Java base object type.
	 * @param namespace      the namespace part and null otherwise.
	 * @param methodName     the method name to search.
	 * @param parameterTypes the parameter types of the method to search.
	 * @param nativeMode     the native image mode.
	 * @param projectUri     the project Uri.
	 *
	 * @return the Java method from the given Java type <code>baseType</code> which
	 *         matches the given method name <code>methodName</code> with the given
	 *         parameter types <code>parameterTypes</code>.
	 */
	public JavaMemberResult findMethod(ResolvedJavaTypeInfo baseType, String namespace, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, boolean nativeMode) {
		// Search in the java root type
		JavaMemberResult result = new JavaMemberResult();

		if (!nativeMode && baseType != null) {
			// In NO native image mode, search with Java reflection at the begin
			if (findMethod(baseType, methodName, parameterTypes, result)) {
				return result;
			}
		}

		// Search in template extension value resolvers retrieved by @TemplateExtension
		List<MethodValueResolver> dynamicResolvers = getMethodValueResolvers().getNow(null);
		if (findMethodResolver(baseType, namespace, methodName, parameterTypes, dynamicResolvers, result)) {
			return result;
		}

		if (baseType != null) {
			// Search in static value resolvers (ex : orEmpty, take, etc)
			List<MethodValueResolver> staticResolvers = projectRegistry.getCommmonsResolvers();
			if (findMethodResolver(baseType, null, methodName, parameterTypes, staticResolvers, result)) {
				return result;
			}

			if (nativeMode) {
				// In native image mode, search with Java reflection at the end
				if (findMethod(baseType, methodName, parameterTypes, result)) {
					return result;
				}
			}
		}

		return result;
	}

	private boolean findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, JavaMemberResult result) {
		return findMethod(baseType, methodName, parameterTypes, result, new HashSet<>());
	}

	private boolean findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, JavaMemberResult result,
			Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(baseType)) {
			return false;
		}
		visited.add(baseType);

		if (isEmpty(methodName)) {
			return false;
		}
		List<JavaMethodInfo> methods = baseType.getMethods();
		if (methods != null && !methods.isEmpty()) {
			for (JavaMethodInfo method : methods) {
				if (isMatchMethod(method, methodName, null, null)) {
					// The current method matches the method name.

					// Check if the current method matches the parameters.
					boolean matchParameters = isMatchParameters(method, parameterTypes);
					if (result.getMember() == null || matchParameters) {
						result.setMember(method);
						result.setMatchParameters(matchParameters);
						result.setMatchVirtualMethod(true);
					}
					if (matchParameters) {
						// The current method matches the method name and and parameters types,stop the
						// search
						return true;
					}
				}
			}
		}
		if (baseType.getExtendedTypes() != null) {
			// Search in extended types
			for (String superType : baseType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaTypeSync(superType);
				if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedSuperType)) {
					if (findMethod(resolvedSuperType, methodName, parameterTypes, result, visited)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean findMethodResolver(ResolvedJavaTypeInfo baseType, String namespace, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, List<MethodValueResolver> resolvers, JavaMemberResult result) {
		if (resolvers == null) {
			return false;
		}
		for (MethodValueResolver resolver : resolvers) {
			if (isMatchMethod(resolver, methodName, null, null)) {
				// The current resolver matches the method name.
				if (namespace != null) {
					if (namespace.equals(resolver.getNamespace())) {
						result.setMember(resolver);
						result.setMatchVirtualMethod(true);
						// Check if the current resolver matches the parameters.
						boolean matchParameters = isMatchParameters(resolver, parameterTypes);
						result.setMatchParameters(matchParameters);
						return true;
					}
				} else {
					if (baseType == null) {
						return false;
					}
					// Check if the baseType matches the type of the first parameter of the current
					// resolver.
					boolean matchVirtualMethod = matchResolver(baseType, resolver);
					boolean matchParameters = false;
					if (matchVirtualMethod) {
						// Check if the current resolver matches the parameters.
						matchParameters = isMatchParameters(resolver, parameterTypes);
					}
					if (result.getMember() == null || (matchParameters && matchVirtualMethod)) {
						result.setMember(resolver);
						result.setMatchParameters(matchParameters);
						result.setMatchVirtualMethod(matchVirtualMethod);
					}
					if (matchParameters && matchVirtualMethod) {
						// The current resolver matches the method name, the parameters types and the
						// virtual method,stop the search
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isMatchParameters(JavaMethodInfo method, List<ResolvedJavaTypeInfo> parameterTypes) {
		boolean virtualMethod = method.isVirtual();
		int nbParameters = method.getParameterslength();
		int declaredNbParameters = parameterTypes.size();
		JavaParameterInfo lastParameter = method.hasParameters()
				? method.getParameterAt(method.getParameters().size() - 1)
				: null;
		boolean varargs = lastParameter != null && lastParameter.isVarargs();
		if (varargs) {
			if (declaredNbParameters == 0) {
				// Method defines just a varargs parameter
				if (parameterTypes.isEmpty()) {
					// with varargs, the parameter is optional
					return true;
				}
			}
			if (declaredNbParameters < nbParameters) {
				return false;
			}
		} else if (declaredNbParameters != nbParameters) {
			boolean valid = false;
			if (method.getJaxRsMethodKind() != null) {
				// Renarde parameters validation rules:
				// 1) all parameters which are annotated with @RestPath (or @PathParam) are
				// considered as required
				// 2) all parameters which are annotated with @RestQuery (or @QueryParam) are
				// optional
				// 3) all parameters which are annotated with @RestForm (or @FormParam) cannot
				// appear in the method of uri.
				int nbRequiredParameters = 0;
				int nbOptionalParameters = 0;
				for (int i = 0; i < nbParameters - (varargs ? 1 : 0); i++) {
					JavaParameterInfo parameterInfo = method.getParameters().get(i);
					RestParam restParam = method.getRestParameter(parameterInfo.getName());
					if (restParam != null) {
						if (restParam.getParameterKind() == JaxRsParamKind.PATH) {
							nbRequiredParameters++;
						} else if (restParam.getParameterKind() == JaxRsParamKind.QUERY) {
							nbOptionalParameters++;
						}
					}
				}
				if (declaredNbParameters < nbRequiredParameters) {
					return false;
				}
				if (declaredNbParameters > nbRequiredParameters + nbOptionalParameters) {
					return false;
				}
				nbParameters = declaredNbParameters;
				valid = true;
			}
			if (!valid) {
				return false;
			}
		}

		// Validate all mandatory parameters (without varargs)
		for (int i = 0; i < nbParameters - (varargs ? 1 : 0); i++) {
			JavaParameterInfo parameterInfo = method.getParameters().get(i + (virtualMethod ? 1 : 0));
			ResolvedJavaTypeInfo result = parameterTypes.get(i);

			// If the type info isn't available, assume the type matches.
			// This is helpful eg. when getting the docs for a method whose
			// parameters haven't been input correctly yet.
			if (result != null) {
				String parameterType = parameterInfo.getType();
				if (!isMatchType(result, parameterType)) {
					return false;
				}
			}
		}

		if (varargs) {
			// Validate varargs parameters
			for (int i = nbParameters - 1; i < declaredNbParameters; i++) {
				String parameterType = lastParameter.getVarArgType();
				ResolvedJavaTypeInfo result = parameterTypes.get(i);
				// If the type info isn't available, assume the type matches
				// (see note above)
				if (result != null) {
					if (!isMatchType(result, parameterType)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	protected static String computeGetterName(String propertyOrMethodName) {
		return "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	protected static String computeBooleanGetterName(String propertyOrMethodName) {
		return "is" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName) {
		String getterMethodName = computeGetterName(propertyOrMethodName);
		String booleanGetterName = computeBooleanGetterName(propertyOrMethodName);
		return isMatchMethod(method, propertyOrMethodName, getterMethodName, booleanGetterName);
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName, String getterMethodName,
			String booleanGetterName) {
		String methodName = method.getMethodName();
		if (propertyOrMethodName.equals(methodName) || (getterMethodName != null && getterMethodName.equals(methodName))
				|| (booleanGetterName != null && booleanGetterName.equals(methodName))) {
			return true;
		}
		return false;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public MethodValueResolver findValueResolver(ResolvedJavaTypeInfo baseType, String property) {
		// Search in value resolver
		String literalType = LiteralSupport.getLiteralJavaType(property);
		if (literalType != null) {
			// ex : @java.lang.Integer(base : T[]) : T (see qute-resolvers.jsonc)
			property = "@" + literalType;
		}
		List<MethodValueResolver> resolvers = getResolversFor(baseType);
		for (MethodValueResolver resolver : resolvers) {
			if (isMatchMethod(resolver, property) || isMatchValueResolver(resolver, property)) {
				return resolver;
			}
		}
		return null;
	}

	private static boolean isMatchValueResolver(ValueResolver resolver, String property) {
		if (property == null) {
			return false;
		}
		String named = resolver.getNamed();
		if (named != null) {
			return named.equals(property);
		}

		List<String> matchNames = resolver.getMatchNames();
		if (matchNames != null) {
			for (String matchName : matchNames) {
				if (ValueResolver.MATCH_NAME_ANY.equals(matchName) || matchName.equals(property)) {
					return true;
				}
			}
		}
		return property.equals(resolver.getName());
	}

	public List<MethodValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType) {
		// Search in static value resolvers (ex : orEmpty, take, etc)
		List<MethodValueResolver> matches = new ArrayList<>();
		for (MethodValueResolver resolver : projectRegistry.getCommmonsResolvers()) {
			if (matchResolver(javaType, resolver)) {
				matches.add(resolver);
			}
		}
		// Search in template extension value resolvers retrieved by @TemplateExtension
		List<MethodValueResolver> allResolvers = getMethodValueResolvers().getNow(null);
		if (allResolvers != null) {
			for (MethodValueResolver resolver : allResolvers) {
				if (resolver.getNamespace() == null && matchResolver(javaType, resolver)) {
					matches.add(resolver);
				}
			}
		}
		return matches;
	}

	/**
	 * Returns true if the given java type match the value resolver (if the type
	 * matches the first parameter of the value resolver method) and false
	 * otherwise.
	 *
	 * @param javaType   the java type.
	 * @param resolver   the value resolver.
	 * @param projectUri the project Uri.
	 *
	 * @return true if the given java type match the value resolver (if the type
	 *         matches the first parameter of the value resolver method) and false
	 *         otherwise.
	 */
	private boolean matchResolver(ResolvedJavaTypeInfo javaType, MethodValueResolver resolver) {
		// Example with following signature:
		// "orEmpty(arg : java.util.List<T>) : java.lang.Iterable<T>"
		JavaParameterInfo parameter = resolver.getParameterAt(0); // arg : java.util.List<T>
		if (parameter == null) {
			return false;
		}
		if (parameter.getJavaType().isSingleGenericType()) {
			// - <T>
			// - <T[]>
			return javaType.isArray() == parameter.getJavaType().isArray();
		}
		String parameterType = parameter.getJavaType().getName();
		return isMatchType(javaType, parameterType);
	}

	private boolean isMatchType(ResolvedJavaTypeInfo javaType, String parameterType) {
		return isMatchType(javaType, parameterType, new HashSet<>());
	}

	private boolean isMatchType(ResolvedJavaTypeInfo javaType, String parameterType,
			Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(javaType)) {
			return false;
		}
		visited.add(javaType);

		String resolvedTypeName = javaType.getName();
		if ("java.lang.Object".equals(parameterType)) {
			return true;
		}
		if (isSameType(parameterType, resolvedTypeName)) {
			return true;
		}
		// class BigItem <- Item <- SmallItem
		// javaType = BigItem => javaType.getExtendedTypes() = [Item]
		if (javaType.getExtendedTypes() != null) {
			// Loop for first level of super types (ex Item)
			for (String superType : javaType.getExtendedTypes()) {
				if (isSameType(parameterType, superType)) {
					return true;
				}

				// Loop for other levels of super types (ex SmallItem)
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaTypeSync(superType);
				if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(resolvedSuperType)) {
					if (isMatchType(resolvedSuperType, parameterType, visited)) {
						return true;
					}
				}
			}
		}
		if (!javaType.getTypeParameters().isEmpty()) {
			ResolvedJavaTypeInfo result = resolveJavaTypeSync(resolvedTypeName);
			if (!QuteCompletableFutures.isResolvingJavaTypeOrNull(result) && result.getExtendedTypes() != null) {
				for (String superType : result.getExtendedTypes()) {
					if (isSameType(parameterType, superType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private CompletableFuture<List<MethodValueResolver>> getMethodValueResolvers() {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.getMethodValueResolvers();
				});
	}

	/**
	 * Returns the Java member (field or method) from the given Java base type with
	 * the given property and null otherwise.
	 *
	 * @param baseType   the Java base type.
	 * @param property   the member property (field name or getter method name).
	 * @param projectUri the project Uri used to search in the extended Java type.
	 * @return the Java member (field or method) from the given Java base type with
	 *         the given property and null otherwise.
	 */
	public JavaMemberInfo findMember(ResolvedJavaTypeInfo baseType, String property) {
		if (baseType == null) {
			return null;
		}
		JavaMemberInfo member = findPropertyWithJavaReflection(baseType, property);
		if (member != null) {
			return member;
		}
		return findValueResolver(baseType, property);
	}

	public boolean hasNamespace(String namespace) {
		return getAllNamespaces().contains(namespace);
	}

	/**
	 * Return all namespaces from the given Qute project Uri.
	 *
	 * @param projectUri the Qute project Uri
	 *
	 * @return all namespace from the given Qute project Uri.
	 */
	public Set<String> getAllNamespaces() {
		ExtendedDataModelProject dataModel = getDataModelProject().getNow(null);
		return dataModel != null ? dataModel.getAllNamespaces() : Collections.emptySet();
	}

	public CompletableFuture<JavaElementInfo> findJavaElementWithNamespace(String namespace, String partName) {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					// Search in types resolvers
					List<TypeValueResolver> typeResolvers = dataModel.getTypeValueResolvers();
					for (TypeValueResolver resolver : typeResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in methods resolvers
					List<MethodValueResolver> methodResolvers = dataModel.getMethodValueResolvers();
					for (MethodValueResolver resolver : methodResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in field resolvers
					List<FieldValueResolver> fieldResolvers = dataModel.getFieldValueResolvers();
					for (FieldValueResolver resolver : fieldResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					return null;
				});
	}

	public CompletableFuture<JavaElementInfo> findGlobalVariableJavaElement(String partName) {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					// Search in types resolvers
					List<TypeValueResolver> typeResolvers = dataModel.getTypeValueResolvers();
					for (TypeValueResolver resolver : typeResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in methods resolvers
					List<MethodValueResolver> methodResolvers = dataModel.getMethodValueResolvers();
					for (MethodValueResolver resolver : methodResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in field resolvers
					List<FieldValueResolver> fieldResolvers = dataModel.getFieldValueResolvers();
					for (FieldValueResolver resolver : fieldResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					return null;
				});
	}

	public CompletableFuture<MessageValueResolver> findMessageValueResolver(String namespace, String methodName) {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					// Search in methods resolvers
					List<MethodValueResolver> methodResolvers = dataModel.getMethodValueResolvers();
					for (MethodValueResolver resolver : methodResolvers) {
						if (resolver.getKind() == ValueResolverKind.Message
								&& isMatchNamespaceResolver(namespace, methodName, resolver, dataModel)) {
							return (MessageValueResolver) resolver;
						}
					}
					return null;
				});
	}

	private static boolean isMatchNamespaceResolver(String namespace, String partName, ValueResolver resolver,
			ExtendedDataModelProject dataModel) {
		return dataModel.getSimilarNamespace(namespace).equals(resolver.getNamespace())
				&& isMatchValueResolver(resolver, partName);
	}

	private static boolean isMatchGlobalVariableResolver(String partName, ValueResolver resolver,
			ExtendedDataModelProject dataModel) {
		return resolver.isGlobalVariable() && isMatchValueResolver(resolver, partName);
	}

	public CompletableFuture<NamespaceResolverInfo> getNamespaceResolverInfo(String namespace) {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.getNamespaceResolver(namespace);
				});
	}

	public CompletableFuture<List<ValueResolver>> getGlobalVariables() {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					List<ValueResolver> globalVariables = new ArrayList<>();
					for (ValueResolver valueResolver : dataModel.getTypeValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					for (ValueResolver valueResolver : dataModel.getMethodValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					for (ValueResolver valueResolver : dataModel.getFieldValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					return globalVariables;
				});
	}

	public CompletableFuture<List<MessageValueResolver>> getMessageValueResolvers() {
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					List<MessageValueResolver> messages = new ArrayList<>();
					for (ValueResolver valueResolver : dataModel.getMethodValueResolvers()) {
						if (valueResolver.getKind() == ValueResolverKind.Message) {
							messages.add((MessageValueResolver) valueResolver);
						}
					}
					return messages;
				});
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		String templateUri = template.getUri();
		return getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.findDataModelTemplate(templateUri);
				});
	}

	/**
	 * Returns namespace resolvers from the given Qute project Uri.
	 *
	 * @param projectUri the Qute project Uri
	 *
	 * @return namespace resolvers from the given Qute project Uri.
	 */
	public List<ValueResolver> getNamespaceResolvers(String namespace) {
		ExtendedDataModelProject dataModel = getDataModelProject().getNow(null);
		if (dataModel == null) {
			return Collections.emptyList();
		}

		List<ValueResolver> namespaceResolvers = new ArrayList<>();

		List<TypeValueResolver> allTypeResolvers = dataModel.getTypeValueResolvers();
		if (allTypeResolvers != null) {
			for (ValueResolver resolver : allTypeResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}

		List<MethodValueResolver> allMethodResolvers = dataModel.getMethodValueResolvers();
		if (allMethodResolvers != null) {
			for (ValueResolver resolver : allMethodResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}

		List<FieldValueResolver> allFieldResolvers = dataModel.getFieldValueResolvers();
		if (allFieldResolvers != null) {
			for (ValueResolver resolver : allFieldResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}
		return namespaceResolvers;
	}

	private static boolean isMatchNamespace(ValueResolver resolver, String namespace,
			ExtendedDataModelProject dataModel) {
		if (resolver.getNamespace() == null) {
			return false;
		}
		return namespace == null || dataModel.getSimilarNamespace(namespace).equals(resolver.getNamespace());
	}

	/**
	 * Return all template extensions classes from the given Qute project Uri.
	 *
	 * @param projectUri the Qute project Uri
	 *
	 * @return all template extensions classes from the given Qute project Uri.
	 */
	public Set<String> getAllTemplateExtensionsClasses() {
		ExtendedDataModelProject dataModel = getDataModelProject().getNow(null);
		return dataModel != null ? dataModel.getAllTemplateExtensionsClasses() : Collections.emptySet();
	}

	/**
	 * Returns the documentation for the given member as a completable future.
	 * 
	 * @param javaMemberInfo the member to get the documentation for
	 * @param javaTypeInfo   the type that the member belongs to
	 * @param projectUri     the project that the member is in
	 * @return the documentation for the given member as a completable future
	 */
	public CompletableFuture<String> getJavadoc(JavaMemberInfo javaMemberInfo, JavaTypeInfo javaTypeInfo,
			boolean hasMarkdown) {
		String typeName = javaMemberInfo.getJavaTypeInfo() != null ? javaMemberInfo.getJavaTypeInfo().getName()
				: javaTypeInfo.getName();
		String signature = javaMemberInfo.getGenericMember() == null ? javaMemberInfo.getSignature()
				: javaMemberInfo.getGenericMember().getSignature();
		return projectRegistry.getJavadoc(new QuteJavadocParams(typeName, getUri(), javaMemberInfo.getName(),
				signature, hasMarkdown ? DocumentFormat.Markdown : DocumentFormat.PlainText));
	}

}