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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

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

	private final QuteCompletionsForSnippets completionsForSnippets;

	public QuteCompletionForTagSection(QuteCompletionsForSnippets completionsForSnippets) {
		this.completionsForSnippets = completionsForSnippets;
	}

	public void doCompleteTagSection(CompletionRequest completionRequest, String filterPrefix,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker, CompletionList list) {
		completionsForSnippets.collectSnippetSuggestions(completionRequest, filterPrefix, "}", list);
	}

	public CompletableFuture<CompletionList> doCompleteTagSection(CompletionRequest completionRequest,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		CompletionList list = new CompletionList();
		doCompleteTagSection(completionRequest, "{#", completionSettings, formattingSettings, cancelChecker, list);
		return CompletableFuture.completedFuture(list);
	}

}
