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
package com.redhat.qute.project.extensions.roq.frontmatter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.NodeBase;
import com.redhat.qute.parser.injection.LanguageInjectionNode;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlParser;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.extensions.LanguageInjectionService;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * {@link LanguageInjectionService} implementation to support completion, hover,
 * document links for yaml frontmatter content.
 */
public class YamlFrontMatterLanguageInjectionService implements LanguageInjectionService {

	private final YamlFrontMatterCompletion completion;
	private final YamlFrontMatterHover hover;
	private final YamlFrontMatterDocumentLink documentLink;

	public YamlFrontMatterLanguageInjectionService() {
		completion = new YamlFrontMatterCompletion();
		hover = new YamlFrontMatterHover();
		documentLink = new YamlFrontMatterDocumentLink();
	}

	@Override
	public String getLanguageId() {
		return YamlFrontMatterDetector.YAML_FRONT_MATTER_LANGUAGE_ID;
	}

	@Override
	public NodeBase<?> parse(TextDocument textDocument, int start, int end,
			com.redhat.qute.parser.CancelChecker cancelChecker) {
		return YamlParser.parse(textDocument, start, end, cancelChecker);
	}

	@Override
	public CompletableFuture<CompletionList> doComplete(LanguageInjectionNode node, CompletionRequest completionRequest,
			CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		Template template = completionRequest.getTemplate();
		int offset = completionRequest.getOffset();
		return completion.doComplete(completionRequest, document, template, offset, cancelChecker);
	}

	@Override
	public void findDocumentLinks(LanguageInjectionNode node, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		documentLink.findDocumentLinks(document, template, links, cancelChecker);
	}

	@Override
	public CompletableFuture<Hover> doHover(LanguageInjectionNode node, HoverRequest hoverRequest,
			CancelChecker cancelChecker) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> cancelChecker.checkCanceled());
		int offset = hoverRequest.getOffset();
		return hover.doHover(document, offset, hoverRequest, cancelChecker);
	}

	@Override
	public void collectDiagnostics(LanguageInjectionNode node, Template template,
			QuteValidationSettings validationSettings, List<Diagnostic> diagnostics) {
		YamlDocument document = (YamlDocument) node.getInjectedNode(() -> {
		});
		document.accept(new YamlFrontMatterDiagnosticsVisitor(template, validationSettings, diagnostics));
	}

	@Override
	public void collectUsages(LanguageInjectionNode node, QuteTextDocument document) {
		YamlDocument yamlDocument = (YamlDocument) node.getInjectedNode(() -> {
		});
		Template template = node.getOwnerTemplate();
		QuteProject project = template.getProject();
		if (project != null) {
			yamlDocument.accept(new YamlFrontMatterUsagesVisitor(document, project));
		}
	}

}
