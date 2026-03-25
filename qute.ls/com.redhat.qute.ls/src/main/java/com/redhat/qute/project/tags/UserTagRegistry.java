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
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.usages.UsagesRegistry;
import com.redhat.qute.services.completions.CompletionRequest;

/**
 * User tag (from source and binary) registry.
 * 
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public class UserTagRegistry extends UsagesRegistry<UserTagUsages> {

	private final List<TemplateRootPath> templateRootPaths;

	private final QuteCompletionsForUserTagSection completionsForUserTag;

	public UserTagRegistry(QuteProject project, List<TemplateRootPath> templateRootPaths) {
		super();
		this.templateRootPaths = templateRootPaths;
		this.completionsForUserTag = new QuteCompletionsForUserTagSection();
	}

	/**
	 * Returns list of source ('src/main/resources/templates/tags') and binary user
	 * tags.
	 * 
	 * @return list of source ('src/main/resources/templates/tags') and binary user
	 *         tags.
	 */
	public Collection<UserTag> getUserTags() {
		return completionsForUserTag.getUserTags();
	}

	/**
	 * Collect user tags suggestions.
	 *
	 * @param completionRequest completion request.
	 * @param prefixFilter      prefix filter.
	 * @param suffixToFind      suffix to found to eat it when completion snippet is
	 *                          applied.
	 * @param completionItems   set of completion items to update
	 */
	public void collectUserTagSuggestions(CompletionRequest completionRequest, String prefixFilter, String suffixToFind,
			Set<CompletionItem> completionItems) {
		// Completion for binaries + sources user tags (from
		// src/main/resources/templates/tags)
		completionsForUserTag.collectSnippetSuggestions(completionRequest, prefixFilter, suffixToFind, completionItems);
	}

	/**
	 * Returns the preferred tags dir (ex : src/main/resources/templates/tags)
	 * directory and null otherwise.
	 * 
	 * @return the preferred tags dir (ex : src/main/resources/templates/tags)
	 *         directory and null otherwise.
	 */
	public Path getPreferredTagsDir() {
		for (TemplateRootPath templateRootPath : templateRootPaths) {
			Path tagsDir = templateRootPath.getTagsDir();
			if (tagsDir != null) {
				return tagsDir;
			}
		}
		return null;
	}

	public void registerUserTag(UserTag tag) {
		completionsForUserTag.registerUserTag(tag);
	}

	public void unregisterUserTag(UserTag tag) {
		// Handle removed usages
		super.removeUsages(tag.getName());
		// Unregister snippet
		completionsForUserTag.unregisterUserTag(tag);
	}

	@Override
	protected UserTagUsages createUsages(String tagName) {
		return new UserTagUsages(tagName);
	}
}
