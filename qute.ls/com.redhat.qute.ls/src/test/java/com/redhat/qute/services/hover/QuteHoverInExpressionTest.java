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
 * Tests for Qute hover in expression.
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverInExpressionTest {

	@Test
	public void booleanLiteral() throws Exception {
		String template = "{tr|ue}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Boolean" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 5));

		template = "{tru|eX}";
		assertHover(template);

		template = "{fal|se}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Boolean" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 6));

		template = "{fals|eX}";
		assertHover(template);
	}

	@Test
	public void nullLiteral() throws Exception {
		String template = "{nu|ll}";
		assertHover(template);
	}

	@Test
	public void stringLiteral() throws Exception {
		String template = "{\"ab|cd\"}";
		assertHover(template);

		template = "{'ab|cd'}";
		assertHover(template);

	}

	@Test
	public void intLiteral() throws Exception {
		String template = "{1|23}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Integer" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 4));

		template = "{1|23X}";
		assertHover(template);
	}

	@Test
	public void longLiteral() throws Exception {
		String template = "{1|23L}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Long" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 5));

		template = "{1|23LX}";
		assertHover(template);
	}

	@Test
	public void doubleLiteral() throws Exception {
		String template = "{1|23d}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Double" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 5));

		template = "{1|23dX}";
		assertHover(template);
	}

	@Test
	public void floatLiteral() throws Exception {
		String template = "{1|23f}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Float" + //
				System.lineSeparator() + //
				"```", // ,
				r(0, 1, 0, 5));

		template = "{1|23fX}";
		assertHover(template);
	}

	@Test
	public void undefinedVariable() throws Exception {
		String template = "{i|tem}";
		assertHover(template);
	}

	@Test
	public void definedVariable() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{i|tem}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 1, 1, 5));

		template = "{@org.acme.Item item}\r\n" + //
				"{|item}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 1, 1, 5));

		template = "{@org.acme.Item item}\r\n" + //
				"{item|}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", // ,
				r(1, 1, 1, 5));
	}

	@Test
	public void undefinedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX}";
		assertHover(template);
	}

	@Test
	public void undefinedPropertyWithIterable() throws Exception {
		String template = "{@java.util.List items}\r\n" + //
				"{items.nam|eXXX}";
		assertHover(template);
	}

	@Test
	public void definedProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|e}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 10));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name|}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 10));
	}

	@Test
	public void undefinedMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.nam|eXXX()}";
		assertHover(template);
	}

	@Test
	public void definedMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.review|2}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Review org.acme.Item.getReview2()" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 13));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.getReview|2()}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Review org.acme.Item.getReview2()" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 16));
	}

	@Test
	public void objectHoverForIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{ite|ms.size}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"List<Item>" + //
				System.lineSeparator() + //
				"```", //
				r(1, 1, 1, 6));

	}

	@Test
	public void methodHoverForIterable() throws Exception {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{items.siz|e}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"int java.util.List.size()" + //
				System.lineSeparator() + //
				"```", //
				r(1, 7, 1, 11));

	}

	@Test
	public void methodHoverForString() throws Exception {
		String template = "{@java.lang.String foo}\r\n" + //
				"{foo.getByte|s('abcd')}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"byte[] java.lang.String.getBytes(String charsetName)" + //
				System.lineSeparator() + //
				"```", //
				r(1, 5, 1, 13));

	}
}
