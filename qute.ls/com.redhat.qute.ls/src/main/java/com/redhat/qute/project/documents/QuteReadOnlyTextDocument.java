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
package com.redhat.qute.project.documents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.usages.UsagesCollector;
import com.redhat.qute.project.usages.UsagesCollector.UsageTracker;

/**
 * Qute read-only text document.
 *
 * <p>
 * Unlike {@link QuteOpenedTextDocument}, this document is parsed only once at
 * construction time and never re-parsed. The {@link UsagesCollector} and its
 * {@link UsageTracker} instances are therefore created inline, used once, and
 * discarded — no reuse is needed.
 * </p>
 */
public abstract class QuteReadOnlyTextDocument implements QuteTextDocument {

	private static final Logger LOGGER = Logger.getLogger(QuteReadOnlyTextDocument.class.getName());

	private final String uri;

	private final String templateId;

	private final QuteProject project;

	protected Template template;

	private UserTag userTag;

	private TemplateInfoCollector collector;

	private Map<Key<Object>, Object> cache;

	private String userTagName;

	public QuteReadOnlyTextDocument(String uri, String templateId, String templateContent, QuteProject project) {
		this.uri = uri;
		this.templateId = templateId;
		this.project = project;
		this.template = loadTemplate(uri, templateId, templateContent);
	}

	protected Template loadTemplate(String uri, String templateId, String templateContent) {
		try {
			QuteProject project = getProject();
			TextDocument document = new TextDocument(templateContent, uri);
			Template template = TemplateParser.parse(document, getInjectionDetectors(), CancelChecker.NO_CANCELLABLE);
			template.setTemplateId(templateId);
			template.setProjectRegistry(project.getProjectRegistry());
			template.setProjectUri(project.getUri());
			processCallVisitor(template, project);
			return template;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while loading template '" + uri + "'.", e);
			return null;
		}
	}

	@Override
	public QuteProject getProject() {
		return project;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getTemplateId() {
		return templateId;
	}

	@Override
	public Template getTemplate() {
		return template;
	}

	@Override
	public boolean isOpened() {
		return false;
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
	
	public String getUserTagName() {
		if (userTagName == null) {
			userTagName = QuteTextDocument.super.getUserTagName();
		}
		return userTagName;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		return null;
	}

	private TemplateInfoCollector getCollector() {
		if (collector == null) {
			collector = getSynchCollector();
		}
		return collector;
	}

	private synchronized TemplateInfoCollector getSynchCollector() {
		if (collector != null) {
			return collector;
		}
		SearchInfoQuery query = new SearchInfoQuery();
		query.setInsertParameter(SearchInfoQuery.ALL);
		query.setSectionTag(SearchInfoQuery.ALL);
		query.setFragmentId(SearchInfoQuery.ALL);
		TemplateInfoCollector collector = new TemplateInfoCollector(query);
		getTemplate().accept(collector);
		return collector;
	}

	@Override
	public List<Parameter> findInsertTagParameter(String insertParameter) {
		List<Parameter> parameters = getCollector().getInsertParameters();
		if (parameters == null) {
			return Collections.emptyList();
		}
		if (SearchInfoQuery.ALL.equals(insertParameter)) {
			return parameters;
		}
		return parameters.stream() //
				.filter(p -> insertParameter.equals(p.getValue())) //
				.collect(Collectors.toList());
	}

	@Override
	public List<CustomSection> findCustomSectionsByTag(String tag) {
		List<CustomSection> sections = getCollector().getCustomSections();
		if (sections == null) {
			return Collections.emptyList();
		}
		if (SearchInfoQuery.ALL.equals(tag)) {
			return sections;
		}
		return sections.stream() //
				.filter(s -> tag.equals(s.getTag()))//
				.collect(Collectors.toList());
	}

	@Override
	public List<FragmentSection> findFragmentSectionById(String fragmentId) {
		List<FragmentSection> fragmentSections = getCollector().getFragmentSections();
		if (fragmentSections == null) {
			return Collections.emptyList();
		}
		if (SearchInfoQuery.ALL.equals(fragmentId)) {
			return fragmentSections;
		}
		return fragmentSections.stream() //
				.filter(s -> fragmentId.equals(s.getId()))//
				.collect(Collectors.toList());
	}

	@Override
	public Collection<InjectionDetector> getInjectionDetectors() {
		QuteProject project = getProject();
		return project.getInjectionDetectorsFor(getTemplatePath());
	}

	/**
	 * Runs the usage collector on the given template once at load time.
	 *
	 * <p>
	 * Since this document is read-only and parsed only once, the
	 * {@link UsageTracker} instances are created inline and discarded after the
	 * single visit. There is no need to store them for reuse.
	 * </p>
	 *
	 * @param template the parsed template to visit
	 * @param project  the owning project
	 */
	private void processCallVisitor(Template template, QuteProject project) {
		if (template != null) {
			if (project == null) {
				project = template.getProject();
			}
			if (project != null && templateId != null) {
				// Trackers created inline — single use only, not stored
				UsageTracker userTagTracker = new UsageTracker(project.getTagRegistry());
				UsageTracker includeTracker = new UsageTracker(project.getIncludeUsagesRegistry());
				UsagesCollector collector = new UsagesCollector(this, userTagTracker, includeTracker);
				template.accept(collector);
			}
		}
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