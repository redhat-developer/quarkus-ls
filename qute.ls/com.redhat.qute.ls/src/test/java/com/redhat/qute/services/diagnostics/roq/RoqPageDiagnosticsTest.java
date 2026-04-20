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
package com.redhat.qute.services.diagnostics.roq;

import static com.redhat.qute.QuteAssert.d;

import java.util.Collections;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import com.redhat.qute.DiagnosticsParameters;
import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.extensions.roq.frontmatter.YamlFrontMatterDetector;
import com.redhat.qute.project.roq.RoqProject;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Test diagnostics with Roq Quarkus extension and page data model.
 *
 * @author Angelo ZERR
 *
 */
public class RoqPageDiagnosticsTest {

	@Test
	public void pageData() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Page page}\r\n" + //
				"{#for item in page.data.toc}\r\n" + //
				"    {#for subsection in item.subsections} \r\n" + //
				"    {/}    \r\n" + //
				"{/}";
		testDiagnosticsFor(template, //
				d(1, 24, 1, 27, QuteErrorCode.IterationError,
						"Iteration error: {page.data.toc} resolved to [java.lang.Object] which is not iterable.", //
						"qute", DiagnosticSeverity.Error));
	}

	@Test
	public void pageDataWithYamlFrontMatter_validProperty() throws Exception {
		// YAML front matter defines custom properties: layout and title
		// Accessing these properties should not generate diagnostics
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.layout}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataWithYamlFrontMatter_invalidProperty() throws Exception {
		// YAML front matter defines layout and title
		// Accessing an unknown property should fall back to JsonObject.get()
		// which returns Object, so no diagnostic error is expected
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.unknown}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataWithYamlFrontMatter_nestedAccess() throws Exception {
		// YAML front matter defines properties
		// Accessing nested property on String should work
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.title.codePointCount(0,0)}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataTagProperty() throws Exception {
		// Test that page.data.tag works (tagging plugin property)
		String template = "{page.data.tag}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataTagPropertyWithEmpty() throws Exception {
		// Test that page.data.tag.empty works (tag is a String)
		String template = "{page.data.tag.empty}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataTagCollectionProperty() throws Exception {
		// Test that page.data.tagCollection works (tagging plugin property)
		String template = "{page.data.tagCollection}";
		testDiagnosticsForWithFrontMatter(template);
	}

	@Test
	public void pageDataTagCollectionPropertyWithEmpty() throws Exception {
		// Test that page.data.tagCollection.empty works (tagCollection is a String)
		String template = "{page.data.tagCollection.empty}";
		testDiagnosticsForWithFrontMatter(template);
	}

	private static void testDiagnosticsFor(String value, Diagnostic... expected) {
		QuteAssert.testDiagnosticsFor(value, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR, false, null, expected);
	}

	private static void testDiagnosticsForWithFrontMatter(String value, Diagnostic... expected) {
		DiagnosticsParameters p = new DiagnosticsParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.setFilter(false);
		QuteAssert.testDiagnosticsFor(value, p, expected);
	}

}
