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
public class QuteCompletionInExpressionTest {

	@Test
	public void completionInExpressionForObjectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {|}";
		

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item. |}";
		testCompletionFor(template, //
				c("item", "item", r(1, 13, 1, 13)));
	}

	@Test
	public void completionInExpressionNotClosedForObjectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {|";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 8)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {|i";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|te";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 10)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item|";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 11)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item. |";
		testCompletionFor(template, //
				c("item", "item", r(1, 13, 1, 13)));
	}

	@Test
	public void completionInExpressionForPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : Review", "review", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 13)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 13)), //
				c("price : BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : Review", "review", r(1, 12, 1, 13)), //
				c("review2 : Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|n}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 13)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 13)), //
				c("price : BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : Review", "review", r(1, 12, 1, 13)), //
				c("review2 : Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|a}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 12, 1, 14)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 12, 1, 14)), //
				c("price : BigInteger", "price", r(1, 12, 1, 14)), //
				c("review : Review", "review", r(1, 12, 1, 14)), //
				c("review2 : Review", "review2", r(1, 12, 1, 14)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 14)));
	}

	@Test
	public void listGeneric() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.getByIndex(0).|}";
		testCompletionFor(template, c("base : String", "base", r(1, 21, 1, 21)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 21, 1, 21)), //
				c("price : BigInteger", "price", r(1, 21, 1, 21)), //
				c("review : Review", "review", r(1, 21, 1, 21)), //
				c("review2 : Review", "review2", r(1, 21, 1, 21)), //
				c("getReview2() : Review", "getReview2", r(1, 21, 1, 21)));
	}

	@Test
	public void completionInExpressionNotClosedForPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 12, 1, 12)), //
				c("price : BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : Review", "review", r(1, 12, 1, 12)), //
				c("review2 : Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 12, 1, 13)), //
				c("price : BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : Review", "review", r(1, 12, 1, 13)), //
				c("review2 : Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|n";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 12, 1, 13)), //
				c("price : BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : Review", "review", r(1, 12, 1, 13)), //
				c("review2 : Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|a";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 12, 1, 14)), //
				c("price : BigInteger", "price", r(1, 12, 1, 14)), //
				c("review : Review", "review", r(1, 12, 1, 14)), //
				c("review2 : Review", "review2", r(1, 12, 1, 14)), //
				c("getReview2() : Review", "getReview2", r(1, 12, 1, 14)));
	}

	@Test
	public void completionInExpressionForSecondPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 19, 1, 19)), //
				c("average : Integer", "average", r(1, 19, 1, 19)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 19, 1, 20)), //
				c("average : Integer", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|n}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 19, 1, 20)), //
				c("average : Integer", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|a}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 19, 1, 21)), //
				c("average : Integer", "average", r(1, 19, 1, 21)));
	}

	@Test
	public void completionInExpressionWithMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 25, 1, 25)), //
				c("average : Integer", "average", r(1, 25, 1, 25)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 25, 1, 26)), //
				c("average : Integer", "average", r(1, 25, 1, 26)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|n}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 25, 1, 26)), //
				c("average : Integer", "average", r(1, 25, 1, 26)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|a}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 25, 1, 27)), //
				c("average : Integer", "average", r(1, 25, 1, 27)));
	}

	@Test
	public void completionInExpressionWithGetterMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 20, 1, 20)), //
				c("average : Integer", "average", r(1, 20, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 20, 1, 21)), //
				c("average : Integer", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|n}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 20, 1, 21)), //
				c("average : Integer", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|a}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 20, 1, 22)), //
				c("average : Integer", "average", r(1, 20, 1, 22)));
	}

	@Test
	public void completionInExpressionWithBracketNotation() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item['review2'].|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 23, 1, 23)), //
				c("average : Integer", "average", r(1, 23, 1, 23)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item['review2'].n|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 23, 1, 24)), //
				c("average : Integer", "average", r(1, 23, 1, 24)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item['review2'].|n}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 23, 1, 24)), //
				c("average : Integer", "average", r(1, 23, 1, 24)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item['review2'].n|a}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 23, 1, 25)), //
				c("average : Integer", "average", r(1, 23, 1, 25)));
	}
	
	@Test
	public void completionInExpressionWithOnlyStartBracket() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {|";
		testCompletionFor(template, //
				5, //
				c("item", "item", r(1, 7, 1, 7)), //
				c("inject:bean", "inject:bean", r(1, 7, 1, 7)), //
				c("inject:plexux", "inject:plexux", r(1, 7, 1, 7)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 7, 1, 7)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 7, 1, 7)));

	}

	@Test
	public void noCompletionInsideCData() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {[{|}]}";
		testCompletionFor(template, 0);
	}

	@Test
	public void noCompletionInsideSeveralBrackets() throws Exception {
		// two brackets -> no expression
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {{|";
		testCompletionFor(template, 0);

		// three brackets -> expression
		template = "{@org.acme.Item item}\r\n" + //
				"Item: {{{|";
		testCompletionFor(template, 5, //
				c("item", "item", r(1, 9, 1, 9)), //
				c("inject:bean", "inject:bean", r(1, 9, 1, 9)), //
				c("inject:plexux", "inject:plexux", r(1, 9, 1, 9)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 9, 1, 9)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 9, 1, 9)));

	}

	@Test
	public void virtualMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|}";
		testCompletionFor(template, //
				c("discountedPrice(item : Item) : BigDecimal", "discountedPrice", r(1, 12, 1, 12)));
	}

	@Test
	public void completionInsideMethodParameter() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.name.codePointCount(item.|}";
		testCompletionFor(template, //
				c("name : String", "name", r(1, 37, 1, 37)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.name.codePointCount(|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 32, 1, 32)));
	}
}