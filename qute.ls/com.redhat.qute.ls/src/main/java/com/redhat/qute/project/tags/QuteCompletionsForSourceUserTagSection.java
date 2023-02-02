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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.project.QuteProject;

/**
 * User tag completion based on source 'sr/main/resources/templates/tags'
 * directory.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForSourceUserTagSection extends QuteCompletionsForUserTagSection {

	private static final Logger LOGGER = Logger.getLogger(QuteCompletionsForSourceUserTagSection.class.getName());

	/**
	 * Loop for files from src/main/resources/tags to update list of user tags.
	 * 
	 * @param tagsDir the src/main/resources/tags directory.
	 * @param project
	 */
	public void refresh(Path tagsDir, QuteProject project) {
		if (!Files.exists(tagsDir)) {
			return;
		}
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		List<UserTag> snippets = snippetRegistry.getSnippets();
		try {
			if (snippets.isEmpty()) {
				// create all user tags
				Files.list(tagsDir) //
						.forEach(path -> {
							snippetRegistry.registerSnippet(createUserTag(path, tagsDir, project));
						});
			} else {
				// Remove all user tags which doesn't exist anymore
				List<UserTag> existingSnippets = new ArrayList<UserTag>(snippets);
				for (UserTag userTag : existingSnippets) {
					if (!Files.exists(((SourceUserTag) userTag).getPath())) {
						snippets.remove(userTag);
					}
				}
				// Add new snippets
				Set<Path> existingSnippetPaths = snippets //
						.stream() //
						.map(userTag -> ((SourceUserTag) userTag).getPath()) //
						.collect(Collectors.toSet());
				Files.list(tagsDir) //
						.forEach(path -> {
							if (!existingSnippetPaths.contains(path)) {
								snippetRegistry.registerSnippet(createUserTag(path, tagsDir, project));
							}
						});
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while collecting source user tags", e);
		}

	}

	private static UserTag createUserTag(Path path, Path tagsDir, QuteProject project) {
		String fileName = path.getName(path.getNameCount() - 1).toString();
		return new SourceUserTag(fileName, path, project);
	}

	/**
	 * Clear cache of all user tag.
	 */
	public void clear() {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		List<UserTag> snippets = snippetRegistry.getSnippets();
		for (UserTag userTag : snippets) {
			userTag.clear();
		}
	}
}
