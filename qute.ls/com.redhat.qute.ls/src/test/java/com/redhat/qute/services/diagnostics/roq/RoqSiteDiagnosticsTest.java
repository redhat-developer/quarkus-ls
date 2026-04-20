/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics.roq;

import java.util.Collections;

import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

import com.redhat.qute.DiagnosticsParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test diagnostics with Roq Quarkus extension and site data model.
 *
 * @author Angelo ZERR
 *
 */
public class RoqSiteDiagnosticsTest {

	@Test
	public void noErrorByUsingMatchNameAny() throws Exception {
		// Here posts is declared although collections doesn't declare posts
		// It uses a template extension collections.get('posts') with matchName=*
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for post in site.collections.posts.paginated(null)}\r\n" + //
				"{#if post.hidden}\r\n" + //
				"{/if}" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void noErrorWithStringParameter() {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage page}\r\n" + //
				"{page.date.format('yyyy, MMM dd')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void pageWithNoDeclaration() {
		// As 'page' and 'site' are injected for all Qute templates for a Roq
		// application
		// no need to declare
		// {@io.quarkiverse.roq.frontmatter.runtime.model.DocumentPage page}
		String template = "{page.date.format('yyyy, MMM dd')}";
		testDiagnosticsFor(template);
	}

	@Test
	public void colletions_subList() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for c in site.collections.posts.subList(0,1)}\r\n" + //
				" {c}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void collections_orEmpty() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for c in site.collections.posts.orEmpty}\r\n" + //
				" {c}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void collections_orEmpty_data() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for c in site.collections.posts.orEmpty}\r\n" + //
				" {c.data}\r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void siteDataTitle() throws Exception {
		// YAML front matter defines properties
		// Accessing nested property on String should work
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{site.data.title.codePointCount(0,0)}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void siteDataNavigation() throws Exception {
		// Test with site.data.navigation pattern
		String template = "---\r\n" + //
				"navigation:\r\n" + //
				"  - url: /about\r\n" + //
				"    title: About\r\n" + //
				"  - url: /contact\r\n" + //
				"    title: Contact\r\n" + //
				"---\r\n" + //
				"{#for item in site.data.navigation}\r\n" + //
				"  <a href=\"{site.url.resolve(item.url)}\" class=\"nav-item\">\r\n" + //
				"    {item.title}\r\n" + //
				"  </a>\r\n" + //
				"{/for}";
		testDiagnosticsForWithFrontMatter(template);
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, true, null, expected);
	}

	private static void testDiagnosticsForWithFrontMatter(String value, Diagnostic... expected) {
		DiagnosticsParameters p = new DiagnosticsParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.setFilter(false);
		QuteAssert.testDiagnosticsFor(value, p, expected);
	}
}
