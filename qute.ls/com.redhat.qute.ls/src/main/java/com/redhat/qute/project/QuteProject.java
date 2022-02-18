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

	private final Map<String /* template id */, TemplateProvider> openedDocuments;

	private final Map<String /* Full qualified name of Java class */, CompletableFuture<ResolvedJavaTypeInfo>> resolvedJavaTypes;

	private CompletableFuture<ExtendedDataModelProject> dataModelProjectFuture;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final UserTagRegistry tagRegistry;

	public QuteProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider userTagProvider) {
		this.uri = projectInfo.getUri();
		this.templateBaseDir = createPath(projectInfo.getTemplateBaseDir());
		this.indexer = new QuteIndexer(this);
		this.openedDocuments = new HashMap<>();
		this.dataModelProvider = dataModelProvider;
		this.resolvedJavaTypes = new HashMap<>();
		this.tagRegistry = new UserTagRegistry(uri, templateBaseDir, userTagProvider);
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
		TemplateProvider provider = openedDocuments.get(templateId);
		if (provider != null) {
			Template template = provider.getTemplate().getNow(null);
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
	public void onDidOpenTextDocument(TemplateProvider document) {
		openedDocuments.put(document.getTemplateId(), document);
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(TemplateProvider document) {
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

	public boolean isUserTag(Template template) {
		String templateId = template.getTemplateId();
		return templateId != null && templateId.startsWith("tags");
	}
}
