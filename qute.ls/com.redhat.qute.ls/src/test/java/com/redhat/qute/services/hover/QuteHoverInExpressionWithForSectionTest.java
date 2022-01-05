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
package com.redhat.qute.services.hover;

import static com.redhat.qute.QuteAssert.assertHover;
import static com.redhat.qute.QuteAssert.r;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute hover in expression with #for section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInExpressionWithForSectionTest {

	@Test
	public void undefinedVariableInIterable() throws Exception {
		String template = "{#for item in ite|ms}\r\n" + //
				"		{it.name}\r\n" + //
				"{/for item in}";
		assertHover(template);
	}

	@Test
	public void definedVariableInIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in ite|ms}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"java.util.List<org.acme.Item>" + //
				System.lineSeparator() + //
				"```", //
				r(1, 14, 1, 19));
	}

	@Test
	public void undefinedVariableInAlias() throws Exception {
		String template = "{#for ite|m in items}\r\n" + //
				"		{it.name}\r\n" + //
				"{/for item in}";
		assertHover(template);
	}

	@Test
	public void definedVariableInAlias() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for ite|m in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 10));
	}

	@Test
	public void definedItemVariable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{ite|m.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 7));
	}

	@Test
	public void undefinedItemVariableInElseBlock() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"{#else}" + //
				"		{ite|m.name}\r\n" + //
				"{/for}";
		assertHover(template);
	}

	@Test
	public void definedItemProperty() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item.na|me}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(2, 8, 2, 12));
	}

	@Test
	public void definedItemMethod() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item.get|Review2()}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Review org.acme.Item.getReview2()" + //
				System.lineSeparator() + //
				"```", //
				r(2, 8, 2, 18));
	}

	@Test
	public void metadata() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item_cou|nt}\r\n" + //
				"{/for}";
		assertHover(template, "`count` - 1-based index" + //
				System.lineSeparator() + //
				"```java" + //
				System.lineSeparator() + //
				"java.lang.Integer" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 13));
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 * 
	 * @throws Exception
	 */
	@Test
	public void derived() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name} \r\n" + //
				"{#for item in item.reviews} \r\n" + //
				"  {it|em.average} \r\n" + // <-- item is an instance of Review
				"  {data:item.price} \r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Review" + //
				System.lineSeparator() + //
				"```", //
				r(3, 3, 3, 7));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name} \r\n" + //
				"{#for item in item.reviews} \r\n" + //
				"  {item.average} \r\n" + // <--
				"  {data:it|em.price} \r\n" + // <-- data:item is an instance of Item
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"org.acme.Item" + //
				System.lineSeparator() + //
				"```", //
				r(4, 8, 4, 12));
	}

	@Test
	public void integers() throws Exception {
		// total = integer
		String template = "{#let total=3}\r\n" + //
				"	{#for i in total}\r\n" + //
				"		{i|}:\r\n" + //
				"	{/for}	\r\n" + //
				"{/let}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"int" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 4));

		// total = double
		template = "{#let total=3d}\r\n" + //
				"	{#for i in total}\r\n" + //
				"		{i|}:\r\n" + //
				"	{/for}	\r\n" + //
				"{/let}";
		assertHover(template);

		// total = integer
		template = "{#for i in 3}\r\n" + //
				"	{i|}:\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"int" + //
				System.lineSeparator() + //
				"```", //
				r(1, 2, 1, 3));

		// total = double
		template = "{#for i in 3d}\r\n" + //
				"	{i|}:\r\n" + //
				"{/for}";
		assertHover(template);
	}
}
