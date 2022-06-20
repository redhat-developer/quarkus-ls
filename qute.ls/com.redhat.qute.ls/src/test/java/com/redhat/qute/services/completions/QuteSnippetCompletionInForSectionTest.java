/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.SECTION_SNIPPET_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute snippet completion in #for section.
 *
 */
public class QuteSnippetCompletionInForSectionTest {

	@Test
	public void forSection() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("for", //
						"{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
								"\t{${1:item}.${3:name}}$0" + System.lineSeparator() + //
								"{/for}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void snippetCompletionInNestedFor() throws Exception {
		String template = "{#for item in items}\n" + //
				"|\n" + //
				"{/for}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(1, 0, 1, 0)));
	}

	@Test
	public void sectionStartSnippetCompletionInNestedFor() throws Exception {
		String template = "{#for item in items}\n" + //
				"{#|}\n" + //
				"{/for}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(1, 0, 1, 3)));
	}

	@Test
	public void elseSnippetCompletionInNestedFor() throws Exception {
		String template = "{#for item in items}\n" + //
				"{#el|}\n" + //
				"{/for}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(1, 0, 1, 5)));
	}

	@Test
	public void snippetCompletionInNestedForWithElse() throws Exception {
		String template = "{#for item in items}\n" + //
				"{#else}\n" + //
				"|\n" + //
				"{/for}";
		testCompletionFor(template, true, SECTION_SNIPPET_SIZE);
	}
}
