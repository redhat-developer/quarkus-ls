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
 * Tests for Qute snippet completion in #when section.
 *
 */
public class QuteSnippetCompletionInWhenSectionTest {

	@Test
	public void switchSection() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("when", //
						"{#when ${1:value}}" + System.lineSeparator() + //
								"\t{#is ${2:case}}$0" + System.lineSeparator() + //
								"{/when}",
						r(0, 0, 0, 0)));
	}

	@Test
	public void snippetCompletionInNestedWhen() throws Exception {
		String template = "{#when value}\n" + //
				"\t|\n" + //
				"{/when}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #is, #else */ //
				c("is", "{#is ${1:case}}$0", r(1, 1, 1, 1)), //
				c("else", "{#else}$0", r(1, 1, 1, 1)));
	}

	@Test
	public void isSnippetCompletionInNestedWhen() throws Exception {
		String template = "{#when value}\n" + //
				"\t{#i|}\n" + //
				"{/when}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #is, #else */ //
				c("is", "{#is ${1:case}}$0", r(1, 1, 1, 5)));
	}

	@Test
	public void snippetCompletionInNestedWhenWithIs() throws Exception {
		String template = "{#when value}\n" + //
				"\t{#is case}\n" + //
				"\t|\n" + //
				"{/when}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #is, #else */ //
				c("is", "{#is ${1:case}}$0", r(2, 1, 2, 1)), //
				c("else", "{#else}$0", r(2, 1, 2, 1)));
	}

	@Test
	public void elseSnippetCompletionInNestedWhenWithIs() throws Exception {
		String template = "{#when value}\n" + //
				"\t{#is case}\n" + //
				"\t{#el|}\n" + //
				"{/when}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #is, #else */ //
				c("is", "{#is ${1:case}}$0", r(2, 1, 2, 6)), //
				c("else", "{#else}$0", r(2, 1, 2, 6)));
	}

	@Test
	public void sectionStartSnippetCompletionInNestedWhen() throws Exception {
		String template = "{#when value}\n" + //
				"\t{#is case}\n" + //
				"\t{#|}\n" + //
				"{/when}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #is, #else */ //
				c("is", "{#is ${1:case}}$0", r(2, 1, 2, 4)), //
				c("else", "{#else}$0", r(2, 1, 2, 4)));
	}
}
