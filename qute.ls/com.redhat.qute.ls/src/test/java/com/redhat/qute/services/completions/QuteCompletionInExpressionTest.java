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
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 8)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {|i}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 7)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {i|te}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 10)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item|}";
		testCompletionFor(template, //
				c("item", "item", r(1, 7, 1, 11)));

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
				c("base : java.lang.String", "base", r(1, 12, 1, 12)), // comes from BaseItem extended by Item
				c("name : java.lang.String", "name", r(1, 12, 1, 12)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 12)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|}";
		testCompletionFor(template, //
				c("base : java.lang.String", "base", r(1, 12, 1, 13)), // comes from BaseItem extended by Item
				c("name : java.lang.String", "name", r(1, 12, 1, 13)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 13)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|n}";
		testCompletionFor(template, //
				c("base : java.lang.String", "base", r(1, 12, 1, 13)), // comes from BaseItem extended by Item
				c("name : java.lang.String", "name", r(1, 12, 1, 13)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 13)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|a}";
		testCompletionFor(template, //
				c("base : java.lang.String", "base", r(1, 12, 1, 14)), // comes from BaseItem extended by Item
				c("name : java.lang.String", "name", r(1, 12, 1, 14)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 14)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 14)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 14)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 14)));
	}

	@Test
	public void completionInExpressionNotClosedForPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 12, 1, 12)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 12)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 12)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 12)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 12)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 12, 1, 13)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 13)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.|n";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 12, 1, 13)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 13)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 13)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 13)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 13)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.n|a";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 12, 1, 14)), //
				c("price : java.math.BigInteger", "price", r(1, 12, 1, 14)), //
				c("review : org.acme.Review", "review", r(1, 12, 1, 14)), //
				c("review2 : org.acme.Review", "review2", r(1, 12, 1, 14)), //
				c("getReview2() : org.acme.Review", "getReview2", r(1, 12, 1, 14)));
	}

	@Test
	public void completionInExpressionForSecondPropertyPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 19, 1, 19)), //
				c("average : java.lang.Integer", "average", r(1, 19, 1, 19)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 19, 1, 20)), //
				c("average : java.lang.Integer", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.|n}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 19, 1, 20)), //
				c("average : java.lang.Integer", "average", r(1, 19, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review.n|a}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 19, 1, 21)), //
				c("average : java.lang.Integer", "average", r(1, 19, 1, 21)));
	}

	@Test
	public void completionInExpressionWithMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 25, 1, 25)), //
				c("average : java.lang.Integer", "average", r(1, 25, 1, 25)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 25, 1, 26)), //
				c("average : java.lang.Integer", "average", r(1, 25, 1, 26)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().|n}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 25, 1, 26)), //
				c("average : java.lang.Integer", "average", r(1, 25, 1, 26)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.getReview2().n|a}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 25, 1, 27)), //
				c("average : java.lang.Integer", "average", r(1, 25, 1, 27)));
	}

	@Test
	public void completionInExpressionWithGetterMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 20, 1, 20)), //
				c("average : java.lang.Integer", "average", r(1, 20, 1, 20)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 20, 1, 21)), //
				c("average : java.lang.Integer", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.|n}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 20, 1, 21)), //
				c("average : java.lang.Integer", "average", r(1, 20, 1, 21)));

		template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.review2.n|a}";
		testCompletionFor(template, //
				c("name : java.lang.String", "name", r(1, 20, 1, 22)), //
				c("average : java.lang.Integer", "average", r(1, 20, 1, 22)));
	}

	@Test
	public void completionInExpressionWithOnlyStartBracket() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {|";
		testCompletionFor(template, 1, //
				c("item", "item", r(1, 7, 1, 7)));
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
		testCompletionFor(template, 1, //
				c("item", "item", r(1, 9, 1, 9)));
	}
}