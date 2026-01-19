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
package com.redhat.qute.services.completions.roq;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.roq.RoqProject;

/**
 * Test completion with Roq Quarkus extension.
 *
 * @author Angelo ZERR
 * 
 */
public class RoqSiteCompletionsTest {

	@Test
	public void site() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{site.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 6, 1, 6)), //
				c("img() : RoqUrl", "img", r(1, 6, 1, 6)), //
				c("toString() : String", "toString", r(1, 6, 1, 6)), //
				c("hashCode() : int", "hashCode", r(1, 6, 1, 6)), //
				c("imagesUrl : RoqUrl", "imagesUrl", r(1, 6, 1, 6)), //
				c("url() : RoqUrl", "url", r(1, 6, 1, 6)), //
				c("collections : RoqCollections", "collections", r(1, 6, 1, 6)), //
				c("collections() : RoqCollections", "collections", r(1, 6, 1, 6)), //
				c("document(id : String) : DocumentPage", "document(id)", r(1, 6, 1, 6)), //
				c("pages : List<NormalPage>", "pages", r(1, 6, 1, 6)), //
				c("pages() : List<NormalPage>", "pages", r(1, 6, 1, 6)), //
				c("data() : JsonObject", "data", r(1, 6, 1, 6)), //
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 6, 1, 6)), //
				c("page(id : String) : NormalPage", "page(id)", r(1, 6, 1, 6)), //
				c("safe(base : Object) : RawString", "safe", r(1, 6, 1, 6)), //
				c("title() : String", "title", r(1, 6, 1, 6)), //
				c("url(path : Object, others : Object...) : RoqUrl", "url(path, others)", r(1, 6, 1, 6)), //
				c("title() : String", "title", r(1, 6, 1, 6)));
	}

	@Test
	public void siteWithNoDeclaration() throws Exception {
		String template = "{site.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(0, 6, 0, 6)), //
				c("img() : RoqUrl", "img", r(0, 6, 0, 6)), //
				c("toString() : String", "toString", r(0, 6, 0, 6)), //
				c("hashCode() : int", "hashCode", r(0, 6, 0, 6)), //
				c("imagesUrl : RoqUrl", "imagesUrl", r(0, 6, 0, 6)), //
				c("url() : RoqUrl", "url", r(0, 6, 0, 6)), //
				c("collections : RoqCollections", "collections", r(0, 6, 0, 6)), //
				c("collections() : RoqCollections", "collections", r(0, 6, 0, 6)), //
				c("document(id : String) : DocumentPage", "document(id)", r(0, 6, 0, 6)), //
				c("pages : List<NormalPage>", "pages", r(0, 6, 0, 6)), //
				c("pages() : List<NormalPage>", "pages", r(0, 6, 0, 6)), //
				c("data() : JsonObject", "data", r(0, 6, 0, 6)), //
				c("orEmpty(base : T) : List<T>", "orEmpty", r(0, 6, 0, 6)), //
				c("page(id : String) : NormalPage", "page(id)", r(0, 6, 0, 6)), //
				c("safe(base : Object) : RawString", "safe", r(0, 6, 0, 6)), //
				c("title() : String", "title", r(0, 6, 0, 6)), //
				c("url(path : Object, others : Object...) : RoqUrl", "url(path, others)", r(0, 6, 0, 6)), //
				c("title() : String", "title", r(0, 6, 0, 6)));
	}
	
	@Test
	public void site_collections() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{site.collections.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 18, 1, 18)), //
				c("collections : Map<String,RoqCollection>", "collections", r(1, 18, 1, 18)), //
				c("resolveCollection(page : DocumentPage) : RoqCollection", "resolveCollection(page)", r(1, 18, 1, 18)), //
				c("hashCode() : int", "hashCode", r(1, 18, 1, 18)), //
				c("get(name : String) : RoqCollection", "get(name)", r(1, 18, 1, 18)));
	}

	@Test
	public void site_collections_get() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{site.collections.get('posts').|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 31, 1, 31)), //
				c("group(keys : String...) : Map<Object,List<Page>>", "group(keys)", r(1, 31, 1, 31)), //
				c("resolvePreviousPage(page : DocumentPage) : DocumentPage", "resolvePreviousPage(page)",
						r(1, 31, 1, 31)), //
				c("paginated(paginator : Paginator) : List<DocumentPage>", "paginated(paginator)", r(1, 31, 1, 31)));
	}

	@Test
	public void site_collections_get_useTemlateExtension() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{site.collections.posts.|}";
		testCompletionFor(template, //
				c("or(base : T, arg : Object) : T", "or(arg)", r(1, 24, 1, 24)), //
				c("group(keys : String...) : Map<Object,List<Page>>", "group(keys)", r(1, 24, 1, 24)), //
				c("resolvePreviousPage(page : DocumentPage) : DocumentPage", "resolvePreviousPage(page)",
						r(1, 24, 1, 24)), //
				c("paginated(paginator : Paginator) : List<DocumentPage>", "paginated(paginator)", r(1, 24, 1, 24)));
	}

	@Test
	public void paginated() throws Exception {
		String template = "{@io.quarkiverse.roq.frontmatter.runtime.model.Site site}\r\n" + //
				"{#for post in site.collections.posts.paginated(page.paginator)}\r\n" + //
				"{#if post.|}";
		testCompletionFor(template, //
				c("info : PageInfo", "info", r(2, 10, 2, 10)), //
				c("hidden : boolean", "hidden", r(2, 10, 2, 10)), //
				c("url : RoqUrl", "url", r(2, 10, 2, 10)), //
				c("data : JsonObject", "data", r(2, 10, 2, 10)));
	}

	public static void testCompletionFor(String value,
			CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, false, QuteAssert.FILE_URI, null, RoqProject.PROJECT_URI,
				QuteAssert.TEMPLATE_BASE_DIR,
				null,
				expectedItems);
	}

}