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
import java.util.Map;
import java.util.stream.Collectors;

import com.redhat.qute.commons.binary.BinaryTemplate;
import com.redhat.qute.parser.injection.InjectionDetector;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.project.QuteProject;

/**
 * Qute binary template document.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteBinaryTextDocument extends QuteReadOnlyTextDocument {

	private final BinaryTemplate binaryTemplate;

	private final String binaryName;

	private final Map<String, String> properties;

	private TemplateInfoCollector collector;

	public QuteBinaryTextDocument(BinaryTemplate binaryTemplate, String binaryName, Map<String, String> properties,
			QuteProject project) {
		super(binaryTemplate.getUri(), binaryTemplate.getPath(), binaryTemplate.getContent(), project);
		this.binaryTemplate = binaryTemplate;
		this.binaryName = binaryName;
		this.properties = properties;
	}

	@Override
	public boolean isBinary() {
		return true;
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
		return parameters.stream() //
				.filter(p -> insertParameter.equals(p.getValue())) //
				.collect(Collectors.toList());
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
		return project.getInjectionDetectorsFor(getTemplatePath());
	}

	@Override
	public Path getTemplatePath() {
		return null;
	}

	@Override
	public String getOrigin() {
		return binaryName;
	}

	@Override
	public String getProperty(String name) {
		if (properties == null) {
			return null;
		}
		return properties.get(name);
	}

	@Override
	public void reparseTemplate() {
		super.template = loadTemplate(binaryTemplate.getUri(), binaryTemplate.getPath(), binaryTemplate.getContent());
	}
}