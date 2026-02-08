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
package com.redhat.qute.project.extensions.roq.frontmatter.snippets;

import com.redhat.qute.ls.commons.snippets.ISnippetRegistryLoader;
import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetRegistry;

/**
 * Load default Qute Roq snippets.
 *
 */
public class YamlFrontMatterSnippetRegistryLoader implements ISnippetRegistryLoader<Snippet> {

	@Override
	public void load(SnippetRegistry<Snippet> registry) throws Exception {
		registry.registerSnippets(YamlFrontMatterSnippetRegistryLoader.class.getResourceAsStream("roq-snippets.json"),
				YamlFrontMatterSnippetContext.ROQ_FRONTMATTER);
	}

}
