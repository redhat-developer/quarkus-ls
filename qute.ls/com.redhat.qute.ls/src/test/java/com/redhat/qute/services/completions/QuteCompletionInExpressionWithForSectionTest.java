/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithForSectionTest {

	@Test
	public void iterableParameter() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in |}\r\n" + //
				"	{}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(2, 14, 2, 14)));
	}

	@Test
	public void objectPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 2)), //
				c("item", "item", r(3, 2, 3, 2)), //
				c("item_count", "item_count", r(3, 2, 3, 2)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{i|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 3)), //
				c("item", "item", r(3, 2, 3, 3)), //
				c("item_count", "item_count", r(3, 2, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{|i}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 2)), //
				c("item", "item", r(3, 2, 3, 2)), //
				c("item_count", "item_count", r(3, 2, 3, 2)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{i|t}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(3, 2, 3, 4)), //
				c("item", "item", r(3, 2, 3, 4)), //
				c("item_count", "item_count", r(3, 2, 3, 4)));
	}

	@Test
	public void propertyPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.n|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 8)), //
				c("price : BigInteger", "price", r(3, 7, 3, 8)), //
				c("review : Review", "review", r(3, 7, 3, 8)), //
				c("review2 : Review", "review2", r(3, 7, 3, 8)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 8)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|n}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 8)), //
				c("price : BigInteger", "price", r(3, 7, 3, 8)), //
				c("review : Review", "review", r(3, 7, 3, 8)), //
				c("review2 : Review", "review2", r(3, 7, 3, 8)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 8)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.n|a}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 9)), //
				c("price : BigInteger", "price", r(3, 7, 3, 9)), //
				c("review : Review", "review", r(3, 7, 3, 9)), //
				c("review2 : Review", "review2", r(3, 7, 3, 9)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 9)));

	}

	@Test
	public void expressionInsideIf() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#if item.price > 0}\r\n" + //
				"		{|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("item", "item", r(4, 3, 4, 3)), //
				c("item_count", "item_count", r(4, 3, 4, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#if item.price > 0}\r\n" + //
				"		{item.|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(4, 8, 4, 8)), //
				c("price : BigInteger", "price", r(4, 8, 4, 8)), //
				c("review : Review", "review", r(4, 8, 4, 8)), //
				c("review2 : Review", "review2", r(4, 8, 4, 8)), //
				c("getReview2() : Review", "getReview2", r(4, 8, 4, 8)));
	}

	@Test
	public void noCompletionWithNoIterableClass() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, 0);
	}

	@Test
	public void objectPartWith2partInExpression() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.reviews}\r\n" + // <- here 2 part in expression
				"		{|}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("item", "item", r(4, 3, 4, 3)), //
				c("review", "review", r(4, 3, 4, 3)), //
				c("item_count", "item_count", r(4, 3, 4, 3)));
	}

	@Test
	public void propertyPartWith2partInExpression() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#for review in item.reviews}\r\n" + // <- here 2 part in expression
				"		{review.|}    \r\n" + //
				"	{/for}\r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(4, 10, 4, 10)), //
				c("average : Integer", "average", r(4, 10, 4, 10)));
	}

	@Test
	public void noCompletionInElseBlock() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"{#else}\r\n" + //
				"	{item.|}    \r\n" + // <-- here item is not available because it is on #else block
				"{/for}";
		testCompletionFor(template, 0);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"{#else}\r\n" + //
				"	{|}    \r\n" + // <-- here items is only available because it is on #else block
				"{/for}";
		testCompletionFor(template, 4, //
				c("items", "items", r(4, 2, 4, 2)), //
				c("inject:bean", "inject:bean", r(4, 2, 4, 2)), //
				c("inject:plexux", "inject:plexux", r(4, 2, 4, 2)), //
				c("config:getConfigProperty(propertyName : String) : Object",
						"config:getConfigProperty(${1:propertyName})$0", r(4, 2, 4, 2)));
	}
}