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

import static com.redhat.qute.QuteAssert.RESOLVERS_SIZE;
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
public class QuteCompletionInExpressionWithEachSectionTest {

	@Test
	public void objectPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 3)), //
				c("it", "it", r(3, 3, 3, 3)), //
				c("it_count", "it_count", r(3, 3, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{i|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 4)), //
				c("it", "it", r(3, 3, 3, 4)), //
				c("it_count", "it_count", r(3, 3, 3, 4)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{|i}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 3)), //
				c("it", "it", r(3, 3, 3, 3)), //
				c("it_count", "it_count", r(3, 3, 3, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{i|t}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(3, 3, 3, 5)), //
				c("it", "it", r(3, 3, 3, 5)), //
				c("it_count", "it_count", r(3, 3, 3, 5)));
	}

	@Test
	public void propertyPart() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 6, 3, 6)), //
				c("price : BigInteger", "price", r(3, 6, 3, 6)), //
				c("review : Review", "review", r(3, 6, 3, 6)), //
				c("review2 : Review", "review2", r(3, 6, 3, 6)), //
				c("getReview2() : Review", "getReview2", r(3, 6, 3, 6)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.n|}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 6, 3, 7)), //
				c("price : BigInteger", "price", r(3, 6, 3, 7)), //
				c("review : Review", "review", r(3, 6, 3, 7)), //
				c("review2 : Review", "review2", r(3, 6, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 6, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.|n}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 6, 3, 7)), //
				c("price : BigInteger", "price", r(3, 6, 3, 7)), //
				c("review : Review", "review", r(3, 6, 3, 7)), //
				c("review2 : Review", "review2", r(3, 6, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 6, 3, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"		{it.n|a}\r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 6, 3, 8)), //
				c("price : BigInteger", "price", r(3, 6, 3, 8)), //
				c("review : Review", "review", r(3, 6, 3, 8)), //
				c("review2 : Review", "review2", r(3, 6, 3, 8)), //
				c("getReview2() : Review", "getReview2", r(3, 6, 3, 8)));
	}

	@Test
	public void expressionInsideIf() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{#if it.price > 0}\r\n" + //
				"		{|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("it", "it", r(4, 3, 4, 3)), //
				c("it_count", "it_count", r(4, 3, 4, 3)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{#if it.price > 0}\r\n" + //
				"		{it.|}\r\n" + //
				"	{/if}	    \r\n" + //
				"{/each}";
		testCompletionFor(template, //
				c("name : String", "name", r(4, 6, 4, 6)), //
				c("price : BigInteger", "price", r(4, 6, 4, 6)), //
				c("review : Review", "review", r(4, 6, 4, 6)), //
				c("review2 : Review", "review2", r(4, 6, 4, 6)), //
				c("getReview2() : Review", "getReview2", r(4, 6, 4, 6)));
	}

	@Test
	public void noCompletionWithNoIterableClass() throws Exception {
		String template = "{@org.acme.Item items}\r\n" + //
				" \r\n" + //
				"{#each items}\r\n" + //
				"	{it.|}    \r\n" + //
				"{/each}}";
		testCompletionFor(template, 0);
	}

	@Test
	public void objectPartWith2partInExpression() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#each item.reviews}\r\n" + // <- here 2 part in expression
				"		{|}    \r\n" + //
				"	{/each}\r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("items", "items", r(4, 3, 4, 3)), //
				c("item", "item", r(4, 3, 4, 3)), //
				c("it", "it", r(4, 3, 4, 3)), //
				c("it_count", "it_count", r(4, 3, 4, 3)));
	}

	@Test
	public void propertyPartWith2partInExpression() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items}\r\n" + //
				"	{#each item.reviews}\r\n" + // <- here 2 part in expression
				"		{it.|}    \r\n" + //
				"	{/each}\r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(4, 6, 4, 6)), //
				c("average : Integer", "average", r(4, 6, 4, 6)));
	}

	@Test
	public void inIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each |}";
		testCompletionFor(template, RESOLVERS_SIZE + 1, //
				c("items", "items", r(2, 7, 2, 7)), //
				c("inject:bean", "inject:bean", r(2, 7, 2, 7)), //
				c("inject:plexux", "inject:plexux", r(2, 7, 2, 7)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(2, 7, 2, 7)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(2, 7, 2, 7)), //
				c("GLOBAL", "GLOBAL", r(2, 7, 2, 7)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(2, 7, 2, 7)), //
				c("uri:Login", "uri:Login", r(2, 7, 2, 7)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(2, 7, 2, 7)), //
				c("msg2:hello() : String", "msg2:hello", r(2, 7, 2, 7)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#each |";
		testCompletionFor(template, RESOLVERS_SIZE + 1, //
				c("items", "items", r(2, 7, 2, 7)), //
				c("inject:bean", "inject:bean", r(2, 7, 2, 7)), //
				c("inject:plexux", "inject:plexux", r(2, 7, 2, 7)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(2, 7, 2, 7)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(2, 7, 2, 7)), //
				c("GLOBAL", "GLOBAL", r(2, 7, 2, 7)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(2, 7, 2, 7)), //
				c("uri:Login", "uri:Login", r(2, 7, 2, 7)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(2, 7, 2, 7)), //
				c("msg2:hello() : String", "msg2:hello", r(2, 7, 2, 7)));

	}

}