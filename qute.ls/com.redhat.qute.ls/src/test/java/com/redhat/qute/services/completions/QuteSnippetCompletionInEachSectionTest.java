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
public class QuteSnippetCompletionInEachSectionTest {

	@Test
	public void forSection() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("each", //
						"{#each ${1:items}}" + System.lineSeparator() + //
								"\t{it.${2:name}}$0" + System.lineSeparator() + //
								"{/each}",
						r(0, 0, 0, 0)));
	}

	@Test
	public void snippetCompletionInNestedEach() throws Exception {
		String template = "{#each items}\n" + //
				"\t{it.name}\n" + //
				"|\n" + //
				"{/each}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(2, 0, 2, 0)));
	}

	@Test
	public void sectionStartSnippetCompletionInNestedEach() throws Exception {
		String template = "{#each items}\n" + //
				"\t{it.name}\n" + //
				"{#|}\n" + //
				"{/each}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(2, 0, 2, 3)));
	}

	@Test
	public void elseSnippetCompletionInNestedEach() throws Exception {
		String template = "{#each items}\n" + //
				"\t{it.name}\n" + //
				"{#el|}\n" + //
				"{/each}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #else */ //
				c("else", "{#else}$0", r(2, 0, 2, 5)));
	}

	@Test
	public void snippetCompletionInNestedEachWithElse() throws Exception {
		String template = "{#each items}\n" + //
				"\t{it.name}\n" + //
				"{#else}\n" + //
				"|\n" + //
				"{/each}";
		testCompletionFor(template, true, SECTION_SNIPPET_SIZE);
	}
}
