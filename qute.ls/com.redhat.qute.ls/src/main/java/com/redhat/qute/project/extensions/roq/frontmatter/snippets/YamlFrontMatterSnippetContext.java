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

import java.util.Map;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.services.snippets.AbstractQuteSnippetContext;

public abstract class YamlFrontMatterSnippetContext extends AbstractQuteSnippetContext {

	public static final YamlFrontMatterSnippetContext ROQ_FRONTMATTER = new YamlFrontMatterSnippetContext() {

		@Override
		public boolean isMatch(CompletionRequest request, Map<String, String> model) {
			if (request.getOffset() > 3) {
				return false;
			}
			QuteProject project = request.getTemplate().getProject();
			if (project == null) {
				return false;
			}
			return project.hasProjectFeature(ProjectFeature.Roq);
		}

	};

}
