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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;

import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.services.completions.CompletionRequest;

/**
 * User tag (from source and binary) registry.
 * 
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public class UserTagRegistry {
	private final List<TemplateRootPath> templateRootPaths;

	private final QuteCompletionsForUserTagSection completionsForUserTag;
	private final Map<String, UserTagUsages> usagesByTag;

	public UserTagRegistry(QuteProject project, List<TemplateRootPath> templateRootPaths) {
		this.templateRootPaths = templateRootPaths;
		this.completionsForUserTag = new QuteCompletionsForUserTagSection();
		this.usagesByTag = new HashMap<>();
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

	/**
	 * Updates user tag usages for a given template.
	 *
	 * @param templateId  template URI
	 * @param usages      collected parameters per user tag
	 * @param oldTagNames previously known tag names for this template
	 */
	public void updateUsages(String templateId, Map<String, List<Parameter>> usages, Set<String> oldTagNames) {

		for (Map.Entry<String, List<Parameter>> entry : usages.entrySet()) {
			String tagName = entry.getKey();
			UserTagUsages tagUsages = getOrCreateUsages(tagName);
			tagUsages.updateUsages(templateId, entry.getValue());
		}

		// Handle removed usages
		for (String tagName : oldTagNames) {
			UserTagUsages tagUsages = usagesByTag.get(tagName);
			if (tagUsages != null) {
				tagUsages.updateUsages(templateId, List.of());
			}
		}
	}

	private UserTagUsages getOrCreateUsages(String tagName) {
		return usagesByTag.computeIfAbsent(tagName, UserTagUsages::new);
	}

	public UserTagUsages getUsages(String tagName) {
		return usagesByTag.get(tagName);
	}

	public void registerUserTag(UserTag tag) {
		completionsForUserTag.registerUserTag(tag);
	}

	public void unregisterUserTag(UserTag tag) {
		// Handle removed usages
		usagesByTag.remove(tag.getName());
		// Unregister snippet
		completionsForUserTag.unregisterUserTag(tag);
	}
}
