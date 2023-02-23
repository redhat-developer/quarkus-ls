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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Parameter;
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

	private final Path path;
	private final String templateId;

	private final QuteProject project;

	private Template template;

	private TemplateInfoCollector collector;

	public QuteClosedTextDocument(Path path, String templateId, QuteProject project) {
		this.path = path;
		this.templateId = templateId;
		this.uri = path.toFile().toURI().toASCIIString();
		this.project = project;
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
			TextDocument document = new TextDocument(IOUtils.getContent(path), uri);
			Template template = TemplateParser.parse(document, () -> {
			});
			template.setTemplateId(templateId);
			template.setProjectRegistry(project.getProjectRegistry());
			template.setProjectUri(project.getUri());
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
		return parameters.stream()
				.filter(p -> insertParameter.equals(p.getValue()))
				.collect(Collectors.toList());
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public boolean isOpened() {
		return false;
	}
}
