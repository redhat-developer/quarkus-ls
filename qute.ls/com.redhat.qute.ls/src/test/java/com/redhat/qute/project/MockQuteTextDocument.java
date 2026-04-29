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
package com.redhat.qute.project;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.sections.CustomSection;
import com.redhat.qute.parser.template.sections.FragmentSection;
import com.redhat.qute.project.documents.SearchInfoQuery;
import com.redhat.qute.project.documents.TemplateInfoCollector;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.usages.UsagesCollector;
import com.redhat.qute.project.usages.UsagesCollector.UsageTracker;

public class MockQuteTextDocument implements QuteTextDocument {

	private final Template template;
	private TemplateInfoCollector collector;
	private UserTag userTag;

	public MockQuteTextDocument(Template template) {
		this.template = template;
		processCallVisitor(template, getProject());
	}

	@Override
	public boolean isOpened() {
		return true;
	}

	@Override
	public Template getTemplate() {
		return template;
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
			if (project != null && template.getTemplateId() != null) {
				// Trackers created inline — single use only, not stored
				UsageTracker userTagTracker = new UsageTracker(project.getTagRegistry());
				UsageTracker includeTracker = new UsageTracker(project.getIncludeUsagesRegistry());
				UsagesCollector collector = new UsagesCollector(this, userTagTracker, includeTracker);
				template.accept(collector);
			}
		}
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QuteProject getProject() {
		return template.getProject();
	}

	@Override
	public String getTemplateId() {
		return template.getTemplateId();
	}

	@Override
	public String getUri() {
		return template.getUri();
	}

	@Override
	public Path getTemplatePath() {
		return null;
	}

	@Override
	public UserTag getUserTag() {
		if (!isUserTag()) {
			return null;
		}
		if (userTag == null) {
			userTag = new UserTag(this, getProject().getProjectRegistry().getSharedSettings().getFormattingSettings());
		}
		return userTag;
	}

	@Override
	public String getOrigin() {
		return null;
	}

	@Override
	public String getRelativePath() {
		return null;
	}

	@Override
	public <T> T getUserData(Key<T> key) {
		return null;
	}

	@Override
	public <T> void putUserData(Key<T> key, T data) {

	}

	@Override
	public Character getExpressionCommand() {
		return null;
	}

	@Override
	public TemplateRootPath getTemplateRootPath() {
		return null;
	}
}
