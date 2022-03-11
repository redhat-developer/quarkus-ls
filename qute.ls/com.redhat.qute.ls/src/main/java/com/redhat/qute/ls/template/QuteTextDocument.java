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
package com.redhat.qute.ls.template;

import static com.redhat.qute.utils.FileUtils.createPath;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.commons.ModelTextDocument;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.TemplateInfoProvider;

public class QuteTextDocument extends ModelTextDocument<Template> implements TemplateInfoProvider {

	private CompletableFuture<ProjectInfo> projectInfoFuture;

	private final QuteProjectInfoProvider projectInfoProvider;

	private QuteProjectRegistry projectRegistry;

	private final Path templatePath;

	private String projectUri;

	private String templateId;

	public QuteTextDocument(TextDocumentItem document, BiFunction<TextDocument, CancelChecker, Template> parse,
			QuteProjectInfoProvider projectInfoProvider, QuteProjectRegistry projectRegistry) {
		super(document, parse);
		this.projectInfoProvider = projectInfoProvider;
		this.projectRegistry = projectRegistry;
		this.templatePath = createPath(document.getUri());
	}

	@Override
	public CompletableFuture<Template> getModel() {
		return super.getModel() //
				.thenApply(template -> {
					if (template != null && template.getProjectUri() == null) {
						template.setTemplateInfoProvider(this);
						ProjectInfo projectInfo = getProjectInfoFuture().getNow(null);
						if (projectInfo != null) {
							QuteProject project = projectRegistry.getProject(projectInfo);
							template.setProjectUri(project.getUri());
							template.setTemplateId(templateId);
						}
						template.setProjectRegistry(projectRegistry);
					}
					return template;
				});
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfoFuture() {
		if (projectInfoFuture == null || projectInfoFuture.isCompletedExceptionally()
				|| projectInfoFuture.isCancelled()) {
			QuteProjectParams params = new QuteProjectParams(super.getUri());
			projectInfoFuture = projectInfoProvider.getProjectInfo(params) //
					.thenApply(projectInfo -> {
						if (projectInfo != null && this.projectUri == null) {
							QuteProject project = projectRegistry.getProject(projectInfo);
							this.projectUri = projectInfo.getUri();
							this.templateId = project.getTemplateId(templatePath);
							projectRegistry.onDidOpenTextDocument(this);
						}
						return projectInfo;
					});
		}
		return projectInfoFuture;
	}

	@Override
	public CompletableFuture<Template> getTemplate() {
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
	public String getProjectUri() {
		if (projectUri != null) {
			return projectUri;
		}
		getProjectInfoFuture().getNow(null);
		return null;
	}

}