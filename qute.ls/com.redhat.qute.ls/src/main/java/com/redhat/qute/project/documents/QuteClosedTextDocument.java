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

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.utils.IOUtils;

/**
 * Qute template document closed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteClosedTextDocument implements QuteTextDocument {

	private static final Logger LOGGER = Logger.getLogger(QuteClosedTextDocument.class.getName());

	private final String uri;

	private final Path templatePath;
	private final String templateId;

	private final QuteProject project;

	private Template template;

	private TemplateInfoCollector collector;

	private UserTagUsageCollector callVisitor;

	public QuteClosedTextDocument(Path templatePath, String templateId, QuteProject project) {
		this.templatePath = templatePath;
		this.templateId = templateId;
		this.uri = FileUtils.toUri(templatePath);
		this.project = project;
		// Force the parse the of template
		getTemplate();
	}

	@Override
	public Template getTemplate() {
		if (template == null) {
			template = loadTemplate();
		}
		return template;
	}

	private synchronized Template loadTemplate() {
		if (template != null) {
			return template;
		}
		try {
			TextDocument document = new TextDocument(IOUtils.getContent(templatePath), uri);
			Template template = TemplateParser.parse(document, getInjectionDetectors(), CancelChecker.NO_CANCELLABLE);
			template.setTemplateId(templateId);
			template.setProjectRegistry(project.getProjectRegistry());
			template.setProjectUri(project.getUri());
			processCallVisitor(template, project);
			return template;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while loading closed template '" + uri + "'.", e);
		}
		return null;
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		return null;
	}

	@Override
	public QuteProject getProject() {
		return project;
	}

	@Override
	public String getTemplateId() {
		return templateId;
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
		return parameters.stream().filter(p -> insertParameter.equals(p.getValue())).collect(Collectors.toList());
	}

	@Override
	public List<Section> findSectionsByTag(String tag) {
		List<Section> sections = getCollector().getSectionsByTag();
		if (sections == null) {
			return Collections.emptyList();
		}
		if (SearchInfoQuery.ALL.equals(tag)) {
			return sections;
		}
		return sections.stream().filter(s -> tag.equals(s.getTag())).collect(Collectors.toList());
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public boolean isOpened() {
		return false;
	}

	private void processCallVisitor(Template template, QuteProject project) {
		if (template != null) {
			if (project == null) {
				project = template.getProject();
			}
			if (project != null && templateId != null) {
				if (callVisitor == null) {
					callVisitor = new UserTagUsageCollector(getTemplateId(), project.getTagRegistry());
				}
				template.accept(callVisitor);
			}

		}
	}

	@Override
	public Collection<InjectionDetector> getInjectionDetectors() {
		return project.getInjectionDetectorsFor(templatePath);
	}

	@Override
	public Path getTemplatePath() {
		return templatePath;
	}
}