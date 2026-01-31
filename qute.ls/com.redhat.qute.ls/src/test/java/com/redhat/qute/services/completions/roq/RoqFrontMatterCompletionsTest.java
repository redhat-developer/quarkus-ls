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
 * Test completion with Roq Quarkus extension and data files.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqFrontMatterCompletionsTest {

	@Test
	public void frontMatter() throws Exception {
		String template = "---\r\n" + //
				"|";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(1, 0, 1, 0)), //
				c("title", "title: $0", r(1, 0, 1, 0)));
	}

	@Test
	public void frontMatteBeforeKey() throws Exception {
		String template = "---\r\n" + //
				"|\r\n" + //
				"bar:";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(1, 0, 1, 0)), //
				c("title", "title: $0", r(1, 0, 1, 0)));
	}

	@Test
	public void frontMatteIncudeInKeys() throws Exception {
		String template = "---\r\n" + //
				"foo: 1\r\n" + //
				"|\r\n" + //
				"bar:";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(2, 0, 2, 0)), //
				c("title", "title: $0", r(2, 0, 2, 0)));

		template = "---\r\n" + //
				"foo:\r\n" + //
				"|\r\n" + //
				"bar:";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(2, 0, 2, 0)), //
				c("title", "title: $0", r(2, 0, 2, 0)));
	}

	@Test
	public void frontMatterWithExistingLayout() throws Exception {
		String template = "---\r\n" + //
				"layout: \r\n" + //
				"|";
		testCompletionFor(template, //
				c("title", "title: $0", r(2, 0, 2, 0)));
	}

	@Test
	public void frontMatterInKey() throws Exception {
		String template = "---\r\n" + //
				"lay|";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(1, 0, 1, 3)), //
				c("title", "title: $0", r(1, 0, 1, 3)));

		template = "---\r\n" + //
				"lay|out";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(1, 0, 1, 6)), //
				c("title", "title: $0", r(1, 0, 1, 6)));

		template = "---\r\n" + //
				"lay|out: \r\n";
		testCompletionFor(template, //
				c("layout", "layout", r(1, 0, 1, 6)), //
				c("title", "title", r(1, 0, 1, 6)));
	}

	@Test
	public void frontMatterInLayoutWithNullValue() throws Exception {
		String template = "---\r\n" + //
				"layout: |\r\n";
		testCompletionFor(template, //
				c("default", "default", r(1, 8, 1, 8)), //
				c("page", "page", r(1, 8, 1, 8)));

		template = "---\r\n" + //
				"layout: |\r\n" + //
				"title: My title\r\n";
		testCompletionFor(template, //
				c("default", "default", r(1, 8, 1, 8)), //
				c("page", "page", r(1, 8, 1, 8)));

		template = "---\r\n" + //
				"title: My title\r\n" + //
				"layout: |\r\n";
		testCompletionFor(template, //
				c("default", "default", r(2, 8, 2, 8)), //
				c("page", "page", r(2, 8, 2, 8)));

		template = "---\r\n" + //
				"title: My title\r\n" + //
				"layout: |\r\n" + //
				"description: Some description\r\n";
		testCompletionFor(template, //
				c("default", "default", r(2, 8, 2, 8)), //
				c("page", "page", r(2, 8, 2, 8)));
	}

	@Test
	public void beforePropertyWithNoValue() throws Exception {
		String template = "---\r\n" + //
				"foo: \r\n" + //
				"lay|ou";
		testCompletionFor(template, //
				c("layout", "layout: $0", r(2, 0, 2, 5)), //
				c("title", "title: $0", r(2, 0, 2, 5)));
	}

	@Test
	public void frontMatterInLayoutWithValue() throws Exception {
		String template = "---\r\n" + //
				"layout: p|a\r\n";
		testCompletionFor(template, //
				c("default", "default", r(1, 8, 1, 10)), //
				c("page", "page", r(1, 8, 1, 10)));

		template = "---\r\n" + //
				"layout: p|a\r\n" + //
				"title: My title\r\n";
		testCompletionFor(template, //
				c("default", "default", r(1, 8, 1, 10)), //
				c("page", "page", r(1, 8, 1, 10)));

		template = "---\r\n" + //
				"title: My title\r\n" + //
				"layout: p|a\r\n";
		testCompletionFor(template, //
				c("default", "default", r(2, 8, 2, 10)), //
				c("page", "page", r(2, 8, 2, 10)));

		template = "---\r\n" + //
				"title: My title\r\n" + //
				"layout: p|a\r\n" + //
				"description: Some description\r\n";
		testCompletionFor(template, //
				c("default", "default", r(2, 8, 2, 10)), //
				c("page", "page", r(2, 8, 2, 10)));
	}

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		CompletionParameters p = new CompletionParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.getCompletionSettings().getCompletionCapabilities().getCompletionItem().setSnippetSupport(true);
		QuteAssert.testCompletionFor(value, p, expectedItems);
	}

}