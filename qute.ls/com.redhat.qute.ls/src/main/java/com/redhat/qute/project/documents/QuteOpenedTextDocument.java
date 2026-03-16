/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package com.redhat.qute.project.documents;

import static com.redhat.qute.commons.FileUtils.createPath;
import static com.redhat.qute.utils.FutureUtils.isFutureLoaded;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.usages.UsagesCollector;
import com.redhat.qute.project.usages.UsagesCollector.UsageTracker;

public class QuteOpenedTextDocument extends ModelTextDocument<Template> implements QuteTextDocument {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	private QuteProjectRegistry projectRegistry;

	private final Path templatePath;

	private String projectUri;

	private String templateId;

	/**
	 * Reusable visitor that collects user tag and include usages on each parse.
	 * Created lazily once the project and template id are known, then reused across
	 * all subsequent parses to avoid repeated allocations.
	 */
	private UsagesCollector callVisitor;

	/**
	 * Tracks user tag usages across visits for this template. Owned here so that
	 * its internal maps survive between keystrokes.
	 */
	private UsageTracker userTagTracker;

	/**
	 * Tracks include section usages across visits for this template. Owned here so
	 * that its internal maps survive between keystrokes.
	 */
	private UsageTracker includeTracker;

	private UserTag userTag;

	private Map<Key<Object>, Object> cache;

	public QuteOpenedTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
		this.projectRegistry = projectRegistry;
		this.templatePath = createPath(document.getUri());
		QuteProject project = projectRegistry.findProjectFor(templatePath);
		if (project != null) {
			this.projectUri = project.getUri();
			this.templateId = project.getTemplateId(templatePath);
		}
	}

	@Override
	public Template getModel() {
		var template = super.getModel();
		if (template != null) {

			template.setTemplateId(templateId);
			template.setProjectRegistry(projectRegistry);
			template.setUserTagName(getUserTagName());

			if (template.getProjectUri() == null) {
				template.setTemplateInfoProvider(this);
				QuteProject project = findProject();
				if (project != null) {
					template.setProjectUri(project.getUri());
					processCallVisitor(template, project);
				}
			}
		}
		return template;
	}

	private QuteProject findProject() {
		if (this.projectUri != null) {
			return projectRegistry.getProject(projectUri);
		}
		ProjectInfo projectInfo = getProjectInfoFuture().getNow(null);
		return projectInfo != null ? projectRegistry.getProject(projectInfo) : null;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		if (!isFutureLoaded(projectInfoFuture)) {
			QuteProjectParams params = new QuteProjectParams(super.getUri());
			projectInfoFuture = projectInfoProvider.getProjectInfo(params) //
					.thenApply(projectInfo -> {
						if (projectInfo != null && this.projectUri == null) {
							QuteProject project = projectRegistry.getProject(projectInfo);
							this.projectUri = projectInfo.getUri();
							this.templateId = project.getTemplateId(templatePath);
							projectRegistry.onDidOpenTextDocument(this);
							processCallVisitor(super.getModel(), project);
						}
						return projectInfo;
					});
		}
		return projectInfoFuture;
	}

	@Override
	public Template getTemplate() {
		return getModel();
	}

	@Override
	public String getTemplateId() {
		if (templateId != null) {
			return templateId;
		}
		getProjectInfoFuture().getNow(null);
		return templateId;
	}

	@Override
	public QuteProject getProject() {
		if (projectUri == null) {
			getProjectInfoFuture().getNow(null);
		}
		return projectUri != null ? projectRegistry.getProject(projectUri) : null;
	}

	@Override
	public List<Parameter> findInsertTagParameter(String insertParameter) {
		SearchInfoQuery query = new SearchInfoQuery();
		query.setInsertParameter(insertParameter);
		TemplateInfoCollector collector = new TemplateInfoCollector(query);
		getTemplate().accept(collector);
		return collector.getInsertParameters();
	}

	@Override
	public List<Section> findSectionsByTag(String tag) {
		SearchInfoQuery query = new SearchInfoQuery();
		query.setSectionTag(tag);
		TemplateInfoCollector collector = new TemplateInfoCollector(query);
		getTemplate().accept(collector);
		return collector.getSectionsByTag();
	}

	@Override
	public boolean isOpened() {
		return true;
	}

	@Override
	public void save() {
		processCallVisitor(getModel(), null);
		if (userTag != null) {
			userTag.clear();
		}
	}

	/**
	 * Runs the usage collector on the given template if the project is known.
	 *
	 * <p>
	 * The {@link UsagesCollector} and its {@link UsageTracker} instances are
	 * created lazily on the first call, once both the project and the template id
	 * are available. On subsequent calls (i.e. every keystroke), the existing
	 * instances are reused — no allocations occur beyond the AST traversal itself.
	 * </p>
	 *
	 * @param template the parsed template to visit, may be {@code null}
	 * @param project  the owning project, or {@code null} to resolve from the
	 *                 template
	 */
	private void processCallVisitor(Template template, QuteProject project) {
		if (template != null) {
			if (project == null) {
				project = template.getProject();
			}
			if (project != null && templateId != null) {
				if (callVisitor == null) {
					// Trackers are allocated once per opened document and reused across
					// all subsequent visits via an internal map-swap strategy
					userTagTracker = new UsageTracker(project.getTagRegistry());
					includeTracker = new UsageTracker(project.getIncludeUsagesRegistry());
					callVisitor = new UsagesCollector(this, userTagTracker, includeTracker);
				}
				template.accept(callVisitor);
			}
		}
	}

	@Override
	public Collection<InjectionDetector> getInjectionDetectors() {
		if (templatePath == null) {
			return Collections.emptyList();
		}
		QuteProject project = projectUri != null ? projectRegistry.getProject(projectUri) : null;
		if (project == null) {
			return Collections.emptyList();
		}
		return project.getInjectionDetectorsFor(templatePath);
	}

	@Override
	public Path getTemplatePath() {
		return templatePath;
	}

	@Override
	protected void cancelModel() {
		super.cancelModel();
		if (userTag != null) {
			userTag.clear();
		}
	}

	@Override
	public UserTag getUserTag() {
		if (!isUserTag()) {
			return null;
		}
		if (userTag == null) {
			userTag = new UserTag(this);
		}
		return userTag;
	}

	@Override
	public String getOrigin() {
		return null;
	}

	@Override
	public void reparseTemplate() {
		cancelModel();
		getTemplate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getUserData(Key<T> key) {
		if (cache == null) {
			return null;
		}
		return (T) cache.get(key);
	}

	@Override
	public <T> void putUserData(Key<T> key, T data) {
		if (cache == null) {
			cache = new HashMap<>();
		}
		cache.put((Key<Object>) key, data);
	}

}