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
package com.redhat.qute.services.completions.multiple;

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;

import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.Test;

import com.redhat.qute.QuteAssert;
import com.redhat.qute.project.multiple.QuteProjectA;
import com.redhat.qute.project.multiple.QuteProjectB;

/**
 * Tests for Qute completion with user tag section and project dependencies.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionWithUserTagSectionTest {

	@Test
	public void completionOnUserTagAInProjectA() throws Exception {
		String template = "{#|}";
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectA.PROJECT_URI, //
				c("tag-a", "{#tag-a /}", r(0, 0, 0, 3)));

		template = "|";
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectA.PROJECT_URI, //
				c("tag-a", "{#tag-a /}", r(0, 0, 0, 0)));
	}

	@Test
	public void completionOnUserTagAInProjectB() throws Exception {
		String template = "{#|}";
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				c("tag-a", "{#tag-a /}", r(0, 0, 0, 3)));

		template = "|";
		testCompletionFor(template, //
				false, // no snippet support
				QuteProjectB.PROJECT_URI, //
				c("tag-a", "{#tag-a /}", r(0, 0, 0, 0)));
	}

	private static void testCompletionFor(String value, boolean snippetSupport, String projectUri,
			CompletionItem... expectedItems) throws Exception {
		QuteAssert.testCompletionFor(value, snippetSupport, QuteAssert.FILE_URI, null, projectUri, "", null,
				expectedItems);
	}

}