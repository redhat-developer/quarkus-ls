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
package com.redhat.qute.services.completions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;

/**
 * Qute completion for tag section (#for, #let, etc).
 * 
 * <ul>
 * <li>{|</li>
 * <li>{#|</li>
 * <li>{#e|c</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionForTagSection {

	private final QuteCompletionsForSnippets<Snippet> completionsForSnippets;

	public QuteCompletionForTagSection(QuteCompletionsForSnippets<Snippet> completionsForSnippets) {
		this.completionsForSnippets = completionsForSnippets;
	}

	public void doCompleteTagSection(CompletionRequest completionRequest, String filterPrefix,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker, Set<CompletionItem> completionItems) {
		// Completion for user tags
		Template template = completionRequest.getTemplate();
		QuteProject project = template.getProject();
		if (project != null) {
			project.collectUserTagSuggestions(completionRequest, filterPrefix, "}", completionItems);
		}
		// Completion for #for, #if, etc
		completionsForSnippets.collectSnippetSuggestions(completionRequest, filterPrefix, "}", completionItems);
	}

	public CompletableFuture<CompletionList> doCompleteTagSection(CompletionRequest completionRequest,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		Set<CompletionItem> completionItems = new HashSet<>();
		doCompleteTagSection(completionRequest, "{#", completionSettings, formattingSettings, cancelChecker, completionItems);
		CompletionList list = new CompletionList();
		list.setItems(completionItems.stream().collect(Collectors.toList()));
		return CompletableFuture.completedFuture(list);
	}

}
