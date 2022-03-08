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
 * Tests for Qute completion with user tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInUserTagTest {

	@Test
	public void userTag() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement /}", //
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement /}$0", //
						r(0, 0, 0, 2)));
	}

	@Test
	public void userTagAndCloseBracket() throws Exception {
		String template = "{#f|or   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement /}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", "{#formElement /}$0", //
						r(0, 0, 0, 3)));
	}

	@Test
	public void parameterExpressionInUserTag() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user name=| /}";
		testCompletionFor(template, //
				c("item", "item", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"{#user name=item.| /}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 17, 1, 17)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 17, 1, 17)), //
				c("price : BigInteger", "price", r(1, 17, 1, 17)), //
				c("review : Review", "review", r(1, 17, 1, 17)), //
				c("review2 : Review", "review2", r(1, 17, 1, 17)), //
				c("getReview2() : Review", "getReview2", r(1, 17, 1, 17)));
	}

	@Test
	public void specialKeys() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{|}";

		// In qute template
		testCompletionFor(template, //
				7, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)));

		// In user tag
		testCompletionFor(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				6 /* item, inject:bean, config:getConfigProperty */ + 2 /* it, nested-content */ + 1 /*
																										 * global
																										 * variables
																										 */, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("it", "it", r(1, 1, 1, 1)), //
				c("nested-content", "nested-content", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)));

	}

	@Test
	public void itParameterExpressionInUserTag() throws Exception {
		// With user tag
		String template = "{@org.acme.Item item}\r\n" + //
				"{#user | /}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"{#user item.| /}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : Review", "review", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		// With #let
		template = "{@org.acme.Item item}\r\n" + //
				"{#let item.| }";
		testCompletionFor(template, 0);
	}
}