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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionList;

import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.utils.UserTagUtils;

/**
 * User tag (from source and binary) registry.
 * 
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public class UserTagRegistry {

	private final String projectUri;

	private final Path tagsDir;

	private final QuteCompletionsForSourceUserTagSection completionsSourceUserTag;

	private final QuteUserTagProvider userTagProvider;
	private final QuteCompletionsForBinaryUserTagSection completionsBinaryUserTag;

	private CompletableFuture<List<UserTag>> userTagFuture;

	public UserTagRegistry(String projectUri, Path templateBaseDir, QuteUserTagProvider userTagProvider) {
		this.projectUri = projectUri;
		this.tagsDir = templateBaseDir.resolve(UserTagUtils.TAGS_DIR);
		this.userTagProvider = userTagProvider;
		this.completionsSourceUserTag = new QuteCompletionsForSourceUserTagSection();
		this.completionsBinaryUserTag = new QuteCompletionsForBinaryUserTagSection();
	}

	/**
	 * Returns list of source ('src/main/resources/templates/tags') user tags.
	 * 
	 * @return list of source ('src/main/resources/templates/tags') user tags.
	 */
	public Collection<UserTag> getSourceUserTags() {
		refresh();
		return completionsSourceUserTag.getUserTags();
	}

	/**
	 * Refresh user tags
	 */
	private void refresh() {
		// Loop for files from src/main/resources/tags to update list of user tags.
		completionsSourceUserTag.refresh(getTagsDir());
		// Update from the 'templates.tags' entries of JARs of the classpath
		completionsBinaryUserTag.refresh(getBinaryUserTags());
	}

	/**
	 * Collect user tags suggestions.
	 *
	 * @param completionRequest completion request.
	 * @param prefixFilter      prefix filter.
	 * @param suffixToFind      suffix to found to eat it when completion snippet is
	 *                          applied.
	 * @param list              completion list to update.
	 */
	public void collectUserTagSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			CompletionList list) {
		// Completion for sources user tags (from src/main/resources/templates/tags)
		refresh();
		completionsSourceUserTag.collectSnippetSuggestions(completionRequest, prefixFilter, suffixToFind, list);
		// Completion for binaries user tags
		completionsBinaryUserTag.collectSnippetSuggestions(completionRequest, prefixFilter, suffixToFind, list);
	}

	/**
	 * Returns list of binary ('templates.tags') user tags.
	 * 
	 * @return list of binary ('templates.tags') user tags.
	 */
	public CompletableFuture<List<UserTag>> getBinaryUserTags() {
		if (userTagFuture == null || userTagFuture.isCancelled() || userTagFuture.isCompletedExceptionally()) {
			userTagFuture = null;
			userTagFuture = loadBinaryUserTags();
		}
		return userTagFuture;
	}

	protected synchronized CompletableFuture<List<UserTag>> loadBinaryUserTags() {
		if (userTagFuture != null) {
			return userTagFuture;
		}
		QuteUserTagParams params = new QuteUserTagParams();
		params.setProjectUri(projectUri);
		return getBinaryUserTags(params) //
				.thenApply(tagInfos -> {
					if (tagInfos == null) {
						return null;
					}
					return tagInfos //
							.stream() //
							.map(info -> new BinaryUserTag(info)) //
							.collect(Collectors.toList());
				});
	}

	protected CompletableFuture<List<UserTagInfo>> getBinaryUserTags(QuteUserTagParams params) {
		return userTagProvider.getUserTags(params);
	}

	/**
	 * Returns the src/main/resources/templates/tags directory.
	 * 
	 * @return the src/main/resources/templates/tags directory.
	 */
	public Path getTagsDir() {
		return tagsDir;
	}
}
