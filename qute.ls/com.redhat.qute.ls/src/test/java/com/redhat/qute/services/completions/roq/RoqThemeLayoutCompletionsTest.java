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
package com.redhat.qute.services.completions.roq;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import java.util.Collections;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.CompletionParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test completion with Roq Quarkus extension and theme layout.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqThemeLayoutCompletionsTest {

	@Test
	public void completionOnThemeLayoutProperty() throws Exception {
		String template = "---\r\n" + //
				"theme-layout: |\r\n";
		// default,:theme/post,theme-layouts/roq-default/post,theme-layouts/roq-default/index,:theme/main,theme-layouts/roq-default/404,theme-layouts/roq-default/tag,page,resources-layout,theme-layouts/roq-default/main,:theme/page,:theme/default,:theme/404,theme-layouts/roq-default/page,:theme/index,:theme/tag,theme-layouts/roq-default/default
		testCompletionFor(template, //
				7, //
					// Binaries
				c("index", "index", r(1, 14, 1, 14)), // theme-layouts/roq-default/index
				c("post", "post", r(1, 14, 1, 14))); // theme-layouts/roq-default/post
	}

	@Test
	public void completionOnTagIncludedByBinaryThemeLayout() throws Exception {
		String template = "---\r\n" + //
				"theme-layout: main\r\n" + //
				"---\r\n" + //
				"{#| /}"; // <-- header coming from theme-layouts/roq-default/main.html binary
		testCompletionFor(template, //
				null, //
				c("header", "{#header}$1{/header}$0", r(3, 0, 3, 2)));
	}

	public static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		CompletionParameters p = new CompletionParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.setExpectedCount(expectedCount);
		p.getCompletionSettings().getCompletionCapabilities().getCompletionItem().setSnippetSupport(true);
		QuteAssert.testCompletionFor(value, p, expectedItems);
	}

}