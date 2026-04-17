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
package com.redhat.qute.project.extensions;

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
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.hover.HoverRequest;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * Language injection service.
 */
public interface LanguageInjectionService {

	/**
	 * Returns the language id.
	 * 
	 * @return the language id.
	 */
	String getLanguageId();

	/**
	 * Parser the injected content.
	 * 
	 * @param text          full test document.
	 * @param start         the start of injected content.
	 * @param end           the end of injected content.
	 * @param cancelChecker the cancel checker.
	 * @return
	 */
	NodeBase<?> parse(TextDocument text, int start, int end, com.redhat.qute.parser.CancelChecker cancelChecker);

	/**
	 * Completion support for injected content.
	 * 
	 * @param languageInjection
	 * @param completionRequest
	 * @param cancelChecker
	 * @return
	 */
	CompletableFuture<CompletionList> doComplete(LanguageInjectionNode languageInjection,
			CompletionRequest completionRequest, CancelChecker cancelChecker);

	/**
	 * Document links support for injected content.
	 * 
	 * @param languageInjection
	 * @param template
	 * @param links
	 * @param cancelChecker
	 */
	void findDocumentLinks(LanguageInjectionNode languageInjection, Template template, List<DocumentLink> links,
			CancelChecker cancelChecker);

	/**
	 * Hover support for injected content.
	 * 
	 * @param languageInjection
	 * @param hoverRequest
	 * @param cancelChecker
	 * @return
	 */
	CompletableFuture<Hover> doHover(LanguageInjectionNode languageInjection, HoverRequest hoverRequest,
			CancelChecker cancelChecker);

	void collectDiagnostics(LanguageInjectionNode languageInjection, Template template,
			QuteValidationSettings validationSettings, List<Diagnostic> diagnostics);

	void collectUsages(LanguageInjectionNode node, QuteTextDocument document);

}
