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

import java.util.Collection;

import com.redhat.qute.ls.commons.snippets.SnippetRegistry;
import com.redhat.qute.services.completions.tags.QuteCompletionsForSnippets;

/**
 * Class for user tag completion (binary and source).
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionsForUserTagSection extends QuteCompletionsForSnippets<UserTag> {

	public QuteCompletionsForUserTagSection() {
		super(false);
	}

	public Collection<UserTag> getUserTags() {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		return snippetRegistry.getSnippets();
	}

	public void registerUserTag(UserTag tag) {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		snippetRegistry.registerSnippet(tag);
	}

	public void unregisterUserTag(UserTag tag) {
		SnippetRegistry<UserTag> snippetRegistry = super.getSnippetRegistry();
		snippetRegistry.unregisterSnippet(tag);
	}
}
