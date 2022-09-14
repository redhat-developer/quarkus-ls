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
package com.redhat.qute.resolvers.collections;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for List 'integer' inside bracket value resolver.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#collections
 *
 */
public class CollectionsIntegerValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.0}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.0.name}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completion() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"Item: {items.|}";
		// test to check @java.lang.Integer(base : T[]) : T is not returned by the
		// completion
		testCompletionFor(template, //
				11, //				
				c("iterator() : Iterator<Item>", "iterator", r(1, 13, 1, 13)), //
				c("size() : int", "size", r(1, 13, 1, 13)), //
				c("get(index : int) : Item", "get(${1:index})$0", r(1, 13, 1, 13)), //
				c("raw(base : Object) : RawString", "raw", r(1, 13, 1, 13)), //
				c("safe(base : Object) : RawString", "safe", r(1, 13, 1, 13)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"Item: {items.0.|}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 15, 1, 15)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 15, 1, 15)), //
				c("price : BigInteger", "price", r(1, 15, 1, 15)), //
				c("review : Review", "review", r(1, 15, 1, 15)), //
				c("review2 : Review", "review2", r(1, 15, 1, 15)), //
				c("getReview2() : Review", "getReview2", r(1, 15, 1, 15)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"Item: {items[0|]}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item @java.lang.Integer()" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Returns the element at the specified `index` from the given list." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{myList.0}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#collections) for more informations.", //
				r(1, 13, 1, 14));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"Item: {items.0.nam|e}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 15, 1, 19));
	}
}