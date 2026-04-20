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
 * Test completion with Roq page.data support.
 *
 * @author Angelo ZERR
 */
public class RoqPageCompletionsTest {

	// @Test
	public void page() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.NormalPage page}\r\n" + //
				"{page.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(1, 6, 1, 6)), //
				c("toString() : String", "toString", r(1, 6, 1, 6)), //
				c("hashCode() : int", "hashCode", r(1, 6, 1, 6)), //
				// c("info : PageInfo", "info", r(1, 6, 1, 6)), //
				// c("hidden : boolean", "hidden", r(1, 6, 1, 6)), //
				// c("url : RoqUrl", "url", r(1, 6, 1, 6)), //
				c("data : JsonObject", "data", r(1, 6, 1, 6)));
	}

	// @Test
	public void pageWithNoDeclaration() throws Exception {
		String template = "{page.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(0, 6, 0, 6)), //
				c("toString() : String", "toString", r(0, 6, 0, 6)), //
				c("hashCode() : int", "hashCode", r(0, 6, 0, 6)), //
				c("info : PageInfo", "info", r(0, 6, 0, 6)), //
				c("hidden : boolean", "hidden", r(0, 6, 0, 6)), //
				c("url : RoqUrl", "url", r(0, 6, 0, 6)), //
				c("data : JsonObject", "data", r(0, 6, 0, 6)));
	}

	@Test
	public void pageDataWithYamlFrontMatter() throws Exception {
		// YAML front matter defines custom properties: layout and title
		// These should appear in completion for page.data
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.|}";
		testCompletionFor(template, //
				// JsonObject methods
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(4, 11, 4, 11)), //
				c("toString() : String", "toString", r(4, 11, 4, 11)), //
				c("hashCode() : int", "hashCode", r(4, 11, 4, 11)), //
				c("isEmpty() : boolean", "isEmpty", r(4, 11, 4, 11)), //
				c("size() : int", "size", r(4, 11, 4, 11)), //
				// Tagging plugin properties (always available)
				c("tag : String", "tag", r(4, 11, 4, 11)), //
				c("tagCollection : String", "tagCollection", r(4, 11, 4, 11)), //
				// YAML front matter properties (should be added by MemberResolutionParticipant)
				c("layout : String", "layout", r(4, 11, 4, 11)), //
				c("title : String", "title", r(4, 11, 4, 11)));
	}

	@Test
	public void pageDataWithNoFrontMatter() throws Exception {
		// No YAML front matter - JsonObject methods + tagging properties should appear
		String template = "{page.data.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(0, 11, 0, 11)), //
				c("toString() : String", "toString", r(0, 11, 0, 11)), //
				c("hashCode() : int", "hashCode", r(0, 11, 0, 11)), //
				c("isEmpty() : boolean", "isEmpty", r(0, 11, 0, 11)), //
				c("size() : int", "size", r(0, 11, 0, 11)), //
				// Tagging plugin properties (always available even without front matter)
				c("tag : String", "tag", r(0, 11, 0, 11)), //
				c("tagCollection : String", "tagCollection", r(0, 11, 0, 11)));
	}

	@Test
	public void pageDataTagProperty() throws Exception {
		// Test completion on page.data.tag (String methods)
		String template = "{page.data.tag.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(0, 15, 0, 15)), //
				c("isEmpty() : boolean", "isEmpty", r(0, 15, 0, 15)), //
				c("empty : boolean", "empty", r(0, 15, 0, 15)), //
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(0, 15, 0, 15)));
	}

	@Test
	public void pageDataTagCollectionProperty() throws Exception {
		// Test completion on page.data.tagCollection (String methods)
		String template = "{page.data.tagCollection.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(0, 25, 0, 25)), //
				c("isEmpty() : boolean", "isEmpty", r(0, 25, 0, 25)), //
				c("empty : boolean", "empty", r(0, 25, 0, 25)), //
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(0, 25, 0, 25)));
	}

	// @Test
	public void pageDataPropertyAccess() throws Exception {
		// Access a specific property from YAML front matter and get String methods
		String template = "---\r\n" + //
				"layout: main\r\n" + //
				"title: My title\r\n" + //
				"---\r\n" + //
				"{page.data.layout.|}";
		testCompletionFor(template, //
				c("or(base : Object, arg : T) : T", "or(${1:arg})$0", r(4, 18, 4, 18)), //
				c("toString() : String", "toString", r(4, 18, 4, 18)), //
				c("length() : int", "length", r(4, 18, 4, 18)), //
				c("isEmpty() : boolean", "isEmpty", r(4, 18, 4, 18)), //
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(4, 18, 4, 18)));
	}

	private static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		CompletionParameters p = new CompletionParameters();
		p.setProjectUri(RoqProject.PROJECT_URI);
		p.setInjectionDetectors(Collections.singletonList(new YamlFrontMatterDetector()));
		p.getCompletionSettings().getCompletionCapabilities().getCompletionItem().setSnippetSupport(true);
		QuteAssert.testCompletionFor(value, p, expectedItems);
	}

}
