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
 * Tests for Qute snippet completion in #if section.
 *
 */
public class QuteSnippetCompletionInIfSectionTest {

	@Test
	public void singleIf() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("if", //
						"{#if ${1:condition}}" + System.lineSeparator() + //
								"	$0" + System.lineSeparator() + //
								"{/if}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void singleElse() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("if-else", //
						"{#if ${1:condition}}" + System.lineSeparator() + //
								"	$2" + System.lineSeparator() + //
								"{#else}" + System.lineSeparator() + //
								"	$0" + System.lineSeparator() + //
								"{/if}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void singleElseIf() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("if-elseif", //
						"{#if ${1:condition}}" + System.lineSeparator() + //
								"	$2" + System.lineSeparator() + //
								"{#else if ${3:condition}}" + System.lineSeparator() + //
								"	$4" + System.lineSeparator() + //
								"{#else}" + System.lineSeparator() + //
								"	$0" + System.lineSeparator() + //
								"{/if}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void snippetCompletionInNestedIf() throws Exception {
		String template = "{#if condition}\n" + //
				"|\n" + //
				"{/if}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #else #else if */ //
				c("else", "{#else}$0", r(1, 0, 1, 0)), //
				c("elseif", "{#else if ${1:condition}}$0", r(1, 0, 1, 0)));
	}

	@Test
	public void elseSnippetCompletionInNestedIf() throws Exception {
		String template = "{#if condition}\n" + //
				"{#el|}\n" + //
				"{/if}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 2, /* #else #else if */ //
				c("else", "{#else}$0", r(1, 0, 1, 5)), //
				c("elseif", "{#else if ${1:condition}}$0", r(1, 0, 1, 5)));
	}
}
