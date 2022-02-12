/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.tags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.redhat.qute.ls.commons.snippets.SnippetRegistry;

/**
 * User tag completion based on bainary 'templates.tags' entry.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForBinaryUserTagSection extends QuteCompletionsForUserTagSection {

	/**
	 * Update from the 'templates.tags' entries of JARs of the classpath
	 * 
	 * @param binaryUserTags the future which scans JARs to collect user tag from
	 *                       'templates.tags' entries
	 */
	public void refresh(CompletableFuture<List<UserTag>> binaryUserTags) {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		List<UserTag> snippets = snippetRegistry.getSnippets();
		if (snippets.isEmpty()) {
			registerUserTagBinary(binaryUserTags, snippetRegistry);
		}
	}

	private synchronized void registerUserTagBinary(CompletableFuture<List<UserTag>> binaryUserTags,
			SnippetRegistry<UserTag> snippetRegistry) {
		List<UserTag> snippets = snippetRegistry.getSnippets();
		if (!snippets.isEmpty()) {
			return;
		}
		List<UserTag> tags = binaryUserTags.getNow(null);
		if (tags != null) {
			for (UserTag userTag : tags) {
				snippetRegistry.registerSnippet(userTag);
			}
		}
	}
}
