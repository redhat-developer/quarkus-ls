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
import static java.lang.System.lineSeparator;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with user tag section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInUserTagTest {

	@Test
	public void input() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("input", "{#input name=\"name\" /}", r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("input", "{#input name=\"${1:name}\" /}$0", r(0, 0, 0, 2)));
	}

	@Test
	public void form() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("form", //
						"{#form it }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/form}",
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("form", //
						"{#form ${1:it} }" + lineSeparator() + //
								"	$2" + lineSeparator() + //
								"{/form}$0",
						r(0, 0, 0, 2)));
	}

	@Test
	public void formAndParameters() throws Exception {
		String template = "{#form |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7 + 3, //
				c("method", "method=\"method\"", r(0, 7, 0, 7)), //
				c("class", "class=\"class\"", r(0, 7, 0, 7)), //
				c("id", "id=\"id\"", r(0, 7, 0, 7)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7 + 3, //
				c("method", "method=\"${1:method}\"$0", r(0, 7, 0, 7)), //
				c("class", "class=\"${1:class}\"$0", r(0, 7, 0, 7)), //
				c("id", "id=\"${1:id}\"$0", r(0, 7, 0, 7)));
	}

	@Test
	public void formElement() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"name\" label=\"label\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/formElement}", //
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"${1:name}\" label=\"${2:label}\" }" + lineSeparator() + //
								"	$3" + lineSeparator() + //
								"{/formElement}$0", //
						r(0, 0, 0, 2)));
	}

	@Test
	public void formElementAndParameters() throws Exception {
		String template = "{#formElement |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7 + 3, //
				c("name", "name=\"name\"", r(0, 14, 0, 14)), //
				c("label", "label=\"label\"", r(0, 14, 0, 14)), //
				c("class", "class=\"class\"", r(0, 14, 0, 14)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7 + 3, //
				c("name", "name=\"${1:name}\"$0", r(0, 14, 0, 14)), //
				c("label", "label=\"${1:label}\"$0", r(0, 14, 0, 14)), //
				c("class", "class=\"${1:class}\"$0", r(0, 14, 0, 14)));
	}

	@Test
	public void formElementAndParametersInParamName() throws Exception {
		String template = "{#formElement cl|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7 + 3, //
				c("name", "name=\"name\"", r(0, 14, 0, 16)), //
				c("label", "label=\"label\"", r(0, 14, 0, 16)), //
				c("class", "class=\"class\"", r(0, 14, 0, 16)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7 + 3, //
				c("name", "name=\"${1:name}\"$0", r(0, 14, 0, 16)), //
				c("label", "label=\"${1:label}\"$0", r(0, 14, 0, 16)), //
				c("class", "class=\"${1:class}\"$0", r(0, 14, 0, 16)));
	}

	@Test
	public void formElementAndParametersInParamValue() throws Exception {
		String template = "{#formElement class=|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7);

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7);
	}

	@Test
	public void simpleTitle() throws Exception {
		String template = "{#|";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("simpleTitle", //
						"{#simpleTitle title=\"title\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/simpleTitle}",
						r(0, 0, 0, 2)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("simpleTitle", //
						"{#simpleTitle title=\"${1:title}\" }" + lineSeparator() + //
								"	$2" + lineSeparator() + //
								"{/simpleTitle}$0",
						r(0, 0, 0, 2)));
	}

	@Test
	public void simpleTitleAndParameters() throws Exception {
		String template = "{#simpleTitle |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7 + 1, //
				c("title", "title=\"title\"", r(0, 14, 0, 14)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7 + 1, //
				c("title", "title=\"${1:title}\"$0", r(0, 14, 0, 14)));
	}

	@Test
	public void titleAndParameters() throws Exception {
		String template = "{#title |";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				7 + 1, //
				c("titularTitle", "titularTitle=\"titularTitle\"", r(0, 8, 0, 8)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				7 + 1, //
				c("titularTitle", "titularTitle=\"${1:titularTitle}\"$0", r(0, 8, 0, 8)));
	}

	@Test
	public void userTagAndCloseBracket() throws Exception {
		String template = "{#f|or   }";

		// Without snippet
		testCompletionFor(template, //
				false, // no snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"name\" label=\"label\" }" + lineSeparator() + //
								"	" + lineSeparator() + //
								"{/formElement}", //
						r(0, 0, 0, 3)));

		// With snippet support
		testCompletionFor(template, //
				true, // snippet support
				SECTION_SNIPPET_SIZE, //
				c("formElement", //
						"{#formElement name=\"${1:name}\" label=\"${2:label}\" }" + lineSeparator() + //
								"	$3" + lineSeparator() + //
								"{/formElement}$0", //
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
				8, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
				c("uri:Login", "uri:Login", r(1, 1, 1, 1)));

		// In user tag
		testCompletionFor(template, //
				"src/main/resources/templates/tags/form.html", //
				"tags/form", //
				7 /* item, inject:bean, config:getConfigProperty */ + 2 /* it, nested-content */ + 1 /*
																										 * global
																										 * variables
																										 */, //
				c("item", "item", r(1, 1, 1, 1)), //
				c("inject:bean", "inject:bean", r(1, 1, 1, 1)), //
				c("inject:plexux", "inject:plexux", r(1, 1, 1, 1)), //
				c("config:*(propertyName : String) : Object", "config:propertyName", r(1, 1, 1, 1)),
				c("config:property(propertyName : String) : Object", "config:property(propertyName)", r(1, 1, 1, 1)), //
				c("GLOBAL", "GLOBAL", r(1, 1, 1, 1)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 1, 1, 1)), //
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