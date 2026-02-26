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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.qute.commons.FileUtils;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.utils.IOUtils;

/**
 * Qute template document closed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteClosedTextDocument extends QuteReadOnlyTextDocument {

	private static final Logger LOGGER = Logger.getLogger(QuteClosedTextDocument.class.getName());

	private final Path templatePath;

	private TemplateInfoCollector collector;

	public QuteClosedTextDocument(Path templatePath, QuteProject project) {
		super(FileUtils.toUri(templatePath), project.getTemplateId(templatePath), getContent(templatePath), project);
		this.templatePath = templatePath;
	}

	private static String getContent(Path templatePath) {
		try {
			return IOUtils.getContent(templatePath);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while loading template '" + templatePath.toUri().toASCIIString() + "'.", e);
			return "";
		}
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
	public Collection<InjectionDetector> getInjectionDetectors() {
		QuteProject project = getProject();
		return project.getInjectionDetectorsFor(templatePath);
	}

	@Override
	public Path getTemplatePath() {
		return templatePath;
	}

	@Override
	public String getFileName() {
		return templatePath.getFileName().toString();
	}

	@Override
	public String getOrigin() {
		return null;
	}
}