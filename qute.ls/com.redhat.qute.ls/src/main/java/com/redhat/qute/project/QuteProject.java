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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.annotations.TemplateDataAnnotation;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.SectionKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateConfiguration;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.indexing.QuteIndex;
import com.redhat.qute.project.indexing.QuteIndexer;
import com.redhat.qute.project.indexing.QuteTemplateIndex;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.tags.UserTagRegistry;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.nativemode.JavaTypeAccessibiltyRule;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.NativeModeJavaTypeFilter;
import com.redhat.qute.utils.StringUtils;

/**
 * A Qute project.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProject {

	private final String uri;

	private final Path templateBaseDir;

	private final QuteIndexer indexer;

	private final Map<String /* template id */, TemplateInfoProvider> openedDocuments;

	private final Map<String /* Full qualified name of Java class */, CompletableFuture<ResolvedJavaTypeInfo>> resolvedJavaTypes;

	private Map<String /* Full qualified name of Java class */, JavaTypeAccessibiltyRule> targetAnnotations;

	private CompletableFuture<ExtendedDataModelProject> dataModelProjectFuture;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final UserTagRegistry tagRegistry;

	private final NativeModeJavaTypeFilter filterInNativeMode;

	public QuteProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider userTagProvider) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
		this.indexer = new QuteIndexer(this);
		this.openedDocuments = new HashMap<>();
		this.dataModelProvider = dataModelProvider;
		this.resolvedJavaTypes = new HashMap<>();
		this.tagRegistry = new UserTagRegistry(uri, templateBaseDir, userTagProvider);
		this.filterInNativeMode = new NativeModeJavaTypeFilter(this);
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

	public String getTemplateId(Path templatePath) {
		if (templatePath == null || templateBaseDir == null) {
			return null;
		}
		try {
			return templateBaseDir.relativize(templatePath).toString().replace('\\', '/');
		} catch (Exception e) {
			return templatePath.getFileName().toString();
		}
	}

	/**
	 * Returns the project Uri.
	 *
	 * @return the project Uri.
	 */
	public String getUri() {
		return uri;
	}

	public int findNbreferencesOfInsertTag(String templateId, String tag) {
		indexer.scanAsync();
		List<QuteIndex> indexes = indexer.find(null, tag, null);
		return indexes.size();
	}

	public List<QuteIndex> findInsertTagParameter(String templateId, String insertParamater) {
		TemplateInfoProvider provider = openedDocuments.get(templateId);
		if (provider != null) {
			Template template = provider.getTemplate();
			if (template != null) {
				List<QuteIndex> indexes = new ArrayList<>();
				collectInsert(insertParamater, template, template, indexes);
				return indexes;
			}
			return Collections.emptyList();
		}
		indexer.scanAsync();
		return indexer.find(templateId, "insert", insertParamater);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(TemplateInfoProvider document) {
		openedDocuments.put(document.getTemplateId(), document);
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(TemplateInfoProvider document) {
		openedDocuments.remove(document.getTemplateId());
		indexer.scanAsync(true);
	}

	private void collectInsert(String insertParamater, Node parent, Template template, List<QuteIndex> indexes) {
		if (parent.getKind() == NodeKind.Section) {
			Section section = (Section) parent;
			if (section.getSectionKind() == SectionKind.INSERT) {
				Parameter parameter = section.getParameterAtIndex(0);
				if (parameter != null) {
					try {
						if (insertParamater == null || insertParamater.equals(parameter.getValue())) {
							Position position = template.positionAt(parameter.getStart());
							Path path = createPath(template.getUri());
							QuteTemplateIndex templateIndex = new QuteTemplateIndex(path, template.getTemplateId());
							QuteIndex index = new QuteIndex("insert", parameter.getValue(), position,
									SectionKind.INSERT, templateIndex);
							indexes.add(index);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}

		}
		List<Node> children = parent.getChildren();
		for (Node node : children) {
			collectInsert(insertParamater, node, template, indexes);
		}
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
				});
	}

	protected CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return dataModelProvider.getDataModelProject(params);
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
	 * Collect user tags suggestions.
	 *
	 * @param completionRequest completion request.
	 * @param prefixFilter      prefix filter.
	 * @param suffixToFind      suffix to found to eat it when completion snippet is
	 *                          applied.
	 * @param list              completion list to update.
	 */
	public void collectUserTagSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			CompletionList list) {
		tagRegistry.collectUserTagSuggestions(completionRequest, prefixFilter, suffixToFind, list);
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

}
