/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
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
 * Tests for Qute hover with CompletionStage or Uni..
 *
 * @author Angelo ZERR
 *
 */
public class QuteHoverWithCompletionStageOrUniTest {

	@Test
	public void inIterable() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				"{#for item in ite|ms}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"List<Item>" + //
				System.lineSeparator() + //
				"```", //
				r(1, 14, 1, 19));
	}

	@Test
	public void inAlias() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				"{#for it|em in items}\r\n" + //
				"		{item.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", //
				r(1, 6, 1, 10));
	}

	@Test
	public void inObjectPartOfFor() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{it|em.name}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"Item" + //
				System.lineSeparator() + //
				"```", //
				r(2, 3, 2, 7));
	}
	
	@Test
	public void inPropertyPartOfFor() throws Exception {
		String template = "{@java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>> items}\r\n" + //
				"{#for item in items}\r\n" + //
				"		{item.nam|e}\r\n" + //
				"{/for}";
		assertHover(template, "```java" + //
				System.lineSeparator() + //
				"String org.acme.Item.name" + //
				System.lineSeparator() + //
				"```" + System.lineSeparator() + //
				"---" + System.lineSeparator() + //
				"The name of the item", //
				r(2, 8, 2, 12));
	}
}
