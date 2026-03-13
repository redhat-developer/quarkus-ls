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

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.CancelChecker;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.tags.UserTag;
import com.redhat.qute.project.usages.UsagesCollector;

/**
 * Qure read only text document.
 */
public abstract class QuteReadOnlyTextDocument implements QuteTextDocument {

	private static final Logger LOGGER = Logger.getLogger(QuteReadOnlyTextDocument.class.getName());

	private final String uri;

	private final String templateId;

	private final QuteProject project;

	private final Template template;

	private UsagesCollector callVisitor;

	private UserTag userTag;

	public QuteReadOnlyTextDocument(String uri, String templateId, String templateContent, QuteProject project) {
		this.uri = uri;
		this.templateId = templateId;
		this.project = project;
		this.template = loadTemplate(uri, templateId, templateContent);
	}

	private Template loadTemplate(String uri, String templateId, String templateContent) {
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
		}
		return null;
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

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		return null;
	}

	private void processCallVisitor(Template template, QuteProject project) {
		if (template != null) {
			if (project == null) {
				project = template.getProject();
			}
			String templateId = getTemplateId();
			if (project != null && templateId != null) {
				if (callVisitor == null) {
					callVisitor = new UsagesCollector(getTemplateId(), project);
				}
				template.accept(callVisitor);
			}

		}
	}

}
