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
package com.redhat.qute.services.completions;

import static com.redhat.qute.QuteAssert.SECTION_SNIPPET_SIZE;
import static com.redhat.qute.QuteAssert.USER_TAG_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with #include section.
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
				18 /* all files from src/test/resources/templates */ - 1 /* README.md */ - USER_TAG_SIZE, //
				c("base", "base", r(0, 10, 0, 10)),
				c("test.json", "test.json", r(0, 10, 0, 10)),
				c("test.html", "test.html", r(0, 10, 0, 10)),
				c("BookPage/book", "BookPage/book", r(0, 10, 0, 10)),
				c("BookPage/books", "BookPage/books", r(0, 10, 0, 10)));

	}

	@Test
	public void includeTemplateIdsSelf() throws Exception {
		String template = "{#include |} \r\n"
				+ "  |\r\n"
				+ "{/include}";

		// Without snippet
		testCompletionFor(template, //
				"src/test/resources/templates/base.html",
				false, // no snippet support
				18 /* all files from src/test/resources/templates */ - 1 /* base.html */ - 1 /* README.md */
						- USER_TAG_SIZE, //
				// c("base", "base", r(0, 10, 0, 10)),
				c("test.json", "test.json", r(0, 10, 0, 10)),
				c("test.html", "test.html", r(0, 10, 0, 10)),
				c("BookPage/book", "BookPage/book", r(0, 10, 0, 10)),
				c("BookPage/books", "BookPage/books", r(0, 10, 0, 10)));

	}

	@Test
	public void insideInclude() throws Exception {
		String template = "{#include base.html} \r\n"
				+ "  |\r\n"
				+ "{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE + 2 /* includedTitle + body #insert */, //
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"name\" /}", r(1, 2, 1, 2)), //
				// #insert parameters
				c("includedTitle", "{#includedTitle}{/includedTitle}", r(1, 2, 1, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE + 2 /* includedTitle + body #insert */, //
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"${1:name}\" /}$0", r(1, 2, 1, 2)), //
				// #insert parameters
				c("includedTitle", "{#includedTitle}$1{/includedTitle}$0", r(1, 2, 1, 2)));
	}

	@Test
	public void insideIncludeWithShortSyntax() throws Exception {
		String template = "{#include base} \r\n"
				+ "  |\r\n"
				+ "{/include}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE + 2 /* includedTitle + body #insert */, //
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"name\" /}", r(1, 2, 1, 2)), //
				// #insert parameters
				c("includedTitle", "{#includedTitle}{/includedTitle}", r(1, 2, 1, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE + 2 /* includedTitle + body #insert */, //
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"${1:name}\" /}$0", r(1, 2, 1, 2)), //
				// #insert parameters
				c("includedTitle", "{#includedTitle}$1{/includedTitle}$0", r(1, 2, 1, 2)));
	}

	@Test
	public void outsideInclude() throws Exception {
		String template = "{#include base.html} \r\n"
				+ "  \r\n"
				+ "{/include}\r\n|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE,
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"name\" /}", r(3, 0, 3, 0)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				// core sections (#for, #if) + user tag sections
				c("input", "{#input name=\"${1:name}\" /}$0", r(3, 0, 3, 0)));

	}

}