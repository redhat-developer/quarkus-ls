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
 * Tests for Qute snippet completion in #switch section.
 *
 */
public class QuteSnippetCompletionInSwitchSectionTest {

	@Test
	public void switchSection() throws Exception {
		String template = "|";
		testCompletionFor(template, //
				true, // snippet support
				c("switch", //
						"{#switch ${1:value}}" + System.lineSeparator() + //
								"\t{#case ${2:case}}$0" + System.lineSeparator() + //
								"{/switch}", //
						r(0, 0, 0, 0)));
	}

	@Test
	public void snippetCompletionInNestedSwitch() throws Exception {
		String template = "{#switch value}\n" + //
				"\t|\n" + //
				"{/switch}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #case */ //
				c("case", "{#case ${1:case}}$0", r(1, 1, 1, 1)));
	}

	@Test
	public void caseSnippetCompletionInNestedSwitch() throws Exception {
		String template = "{#switch value}\n" + //
				"\t{#ca|}\n" + //
				"{/switch}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #case */ //
				c("case", "{#case ${1:case}}$0", r(1, 1, 1, 6)));
	}

	@Test
	public void caseSnippetCompletionInNestedSwitchWithCase() throws Exception {
		String template = "{#switch value}\n" + //
				"\t{#case case}\n" + //
				"\t|\n" + //
				"{/switch}";
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE /* #each, #if, ... */ + 1, /* #case */ //
				c("case", "{#case ${1:case}}$0", r(2, 1, 2, 1)));
	}
}
