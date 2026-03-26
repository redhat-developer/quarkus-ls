/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions.multiple;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Tests for Qute completion with #include section and project dependencies.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithIncludeSectionTest {

	@Test
	public void includeTemplateIds() throws Exception {
		String template = "{#include |} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				2, //
				c("root", "root", r(0, 10, 0, 10)), //
				c("index", "index", r(0, 10, 0, 10)));

		template = "{#include r|o} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				2, //
				c("root", "root", r(0, 10, 0, 12)), //
				c("index", "index", r(0, 10, 0, 12)));

		template = "{#include |ro} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				2, //
				c("root", "root", r(0, 10, 0, 12)), //
				c("index", "index", r(0, 10, 0, 12)));

		template = "{#include ro|} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				2, //
				c("root", "root", r(0, 10, 0, 12)), //
				c("index", "index", r(0, 10, 0, 12)));
	}

	@Test
	public void includeTemplateIdsSelf() throws Exception {
		String template = "{#include |} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				"src/test/resources/projects/project-b/src/main/resources/templates/index.html", //
				"index.html", //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, 1, //
				c("root", "root", r(0, 10, 0, 10)) // ,
		// c("index", "index", r(0, 10, 0, 10))
		);

	}

	@Test
	public void noCompletionWhenIncludedTemplateIdIsDefined() throws Exception {
		String template = "{#include root |} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				3, //
				c("_isolated", "_isolated", r(0, 15, 0, 15)), //
				c("_unisolated", "_unisolated", r(0, 15, 0, 15)), //
				c("_ignoreFragments", "_ignoreFragments", r(0, 15, 0, 15)));

		template = "{#include root r|o} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, 3, //
				c("_isolated", "_isolated", r(0, 15, 0, 17)), //
				c("_unisolated", "_unisolated", r(0, 15, 0, 17)), //
				c("_ignoreFragments", "_ignoreFragments", r(0, 15, 0, 17)));

		template = "{#include root |ro} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				3, //
				c("_isolated", "_isolated", r(0, 15, 0, 15)), //
				c("_unisolated", "_unisolated", r(0, 15, 0, 15)), //
				c("_ignoreFragments", "_ignoreFragments", r(0, 15, 0, 15)));

		template = "{#include root ro|} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				3, //
				c("_isolated", "_isolated", r(0, 15, 0, 17)), //
				c("_unisolated", "_unisolated", r(0, 15, 0, 17)), //
				c("_ignoreFragments", "_ignoreFragments", r(0, 15, 0, 17)));

		template = "{#include root |ro} \r\n" + //
				"  |\r\n" + //
				"{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, 3, //
				c("_isolated", "_isolated", r(0, 15, 0, 15)), //
				c("_unisolated", "_unisolated", r(0, 15, 0, 15)), //
				c("_ignoreFragments", "_ignoreFragments", r(0, 15, 0, 15)));

	}

	private static void testCompletionFor(String value, boolean snippetSupport, String projectUri,
			Integer expectedCount, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, QuteAssert.FILE_URI, null, snippetSupport, projectUri, expectedCount, expectedItems);
	}

	private static void testCompletionFor(String value, String fileUri, String templateId, boolean snippetSupport,
			String projectUri, Integer expectedCount, CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, snippetSupport, fileUri, templateId, projectUri, "", expectedCount,
				expectedItems);
	}

}