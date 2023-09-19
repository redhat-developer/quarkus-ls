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
		String template = "{#include |} \r\n"
				+ "  |\r\n"
				+ "{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI,
				2, //
				c("root", "root", r(0, 10, 0, 10)),
				c("index", "index", r(0, 10, 0, 10)));

	}

	@Test
	public void includeTemplateIdsSelf() throws Exception {
		String template = "{#include |} \r\n"
				+ "  |\r\n"
				+ "{/include}";

		// Without snippet
		testCompletionFor(template, //
				"src/test/resources/projects/project-b/src/main/resources/templates/index.html",
				false, // no snippet support
				QuteProjectB.PROJECT_URI,
				1, //
				c("root", "root", r(0, 10, 0, 10)) // ,
		// c("index", "index", r(0, 10, 0, 10))
		);

	}

	private static void testCompletionFor(String value, boolean snippetSupport,
			String projectUri, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, QuteAssert.FILE_URI, snippetSupport, projectUri, expectedCount, expectedItems);
	}

	private static void testCompletionFor(String value, String fileUri, boolean snippetSupport,
			String projectUri, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		QuteAssert.testCompletionFor(value, snippetSupport, fileUri, null, projectUri, "", expectedCount,
				expectedItems);
	}

}