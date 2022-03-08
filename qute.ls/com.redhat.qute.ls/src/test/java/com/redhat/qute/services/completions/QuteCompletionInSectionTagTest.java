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
 * Tests for Qute completion in section tag node.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInSectionTagTest {

	@Test
	public void openBracket() throws Exception {
		String template = "{|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));
	}

	@Test
	public void openAndCloseBracket() throws Exception {
		String template = "{|}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));
	}

	@Test
	public void openAndCloseBracketWithSpaces() throws Exception {
		String template = "{|   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				6, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(0, 1, 0, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(0, 1, 0, 1)), //
				c("GLOBAL", "GLOBAL", r(0, 1, 0, 1)), //
				c("uri:Login", "uri:Login", r(0, 1, 0, 1)));
	}

	@Test
	public void openBracketAndHash() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 2)));
	}

	@Test
	public void openAndCloseBracketAndHash() throws Exception {
		String template = "{#|}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));
	}

	@Test
	public void openAndCloseBracketWithSpacesAndHash() throws Exception {
		String template = "{#|   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 6)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 6)));
	}

	@Test
	public void openBracketAndTag() throws Exception {
		String template = "{#f|or";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));
	}

	@Test
	public void openAndCloseBracketAndTag() throws Exception {
		String template = "{#f|or}";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));
	}

	@Test
	public void openAndCloseBracketWithSpacesAndTag() throws Exception {
		String template = "{#f|or   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for item in items}" + System.lineSeparator() + //
						"	{item.name}" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("for", "{#for ${1:item} in ${2:items}}" + System.lineSeparator() + //
						"	{${1:item}.${3:name}}$0" + System.lineSeparator() + //
						"{/for}", //
						r(0, 0, 0, 3)));
	}
}