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
package com.redhat.qute.resolvers;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;
import static com.redhat.qute.QuteAssert.testDiagnosticsFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for 'orEmpty' value resolver.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
 *
 */
public class OrEmptyValueResolverTest {

	@Test
	public void diagnostics() {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.orEmpty}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.orEmpty}";
		testDiagnosticsFor(template);

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items.orEmpty}\r\n" + //
				"	{item.name}    \r\n" + //
				"{/for}";
		testDiagnosticsFor(template);
	}

	@Test
	public void completionWithNoIterable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"Item: {item.orEmpty.|}";
		testCompletionFor(template, //
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 20, 1, 20)));
	}

	@Test
	public void completionWithIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items.|}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("orEmpty(base : Iterable<T>) : List<T>", "orEmpty", r(2, 20, 2, 20)));

		template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items.orEmpty}\r\n" + //
				"	{item.|}    \r\n" + //
				"{/for}";
		testCompletionFor(template, //
				c("name : String", "name", r(3, 7, 3, 7)), //
				c("price : BigInteger", "price", r(3, 7, 3, 7)), //
				c("review : Review", "review", r(3, 7, 3, 7)), //
				c("review2 : Review", "review2", r(3, 7, 3, 7)), //
				c("getReview2() : Review", "getReview2", r(3, 7, 3, 7)));
	}

	@Test
	public void hover() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				" \r\n" + //
				"{#for item in items.orE|mpty}\r\n" + //
				"	{item}    \r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"List<Item> orEmpty()" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"Outputs an empty list if the previous part cannot be resolved or resolves to null." + //
				System.lineSeparator() + //
				System.lineSeparator() + //
				"`Sample`:" + //
				System.lineSeparator() + //
				"```qute-html" + //
				System.lineSeparator() + //
				"{#for pet in pets.orEmpty}{pet.name}" + //
				System.lineSeparator() + //
				"{/for}" + //
				System.lineSeparator() + //
				"```" + //
				System.lineSeparator() + //
				"See [here](https://quarkus.io/guides/qute-reference#built-in-resolvers) for more informations.", //
				r(2, 20, 2, 27));
	}
}