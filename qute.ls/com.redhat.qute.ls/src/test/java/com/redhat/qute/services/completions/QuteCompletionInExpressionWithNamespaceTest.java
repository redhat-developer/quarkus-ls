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

import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression with namespace.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithNamespaceTest {

	@Test
	public void dataNamespace() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let name=123 }\r\n" + //
				"  {|}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				5, // name and item
				c("item", "item", r(2, 3, 2, 3)), //
				c("name", "name", r(2, 3, 2, 3)), //
				c("inject:bean", "inject:bean", r(2, 3, 2, 3)), //
				c("inject:plexux", "inject:plexux", r(2, 3, 2, 3)), //
				c("config:getConfigProperty(propertyName : String) : Object",
						"config:getConfigProperty(${1:propertyName})$0", r(2, 3, 2, 3)));

		template = "{@org.acme.Item item}\r\n" + //
				"{#let name=123 }\r\n" + //
				"  {data:|}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				1, // only item (from data) and not name from #let.
				c("item", "item", r(2, 8, 2, 8)));
	}

	@Test
	public void dataNamespaceWithParameterDeclaration() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{data:|}";
		testCompletionFor(template, //
				1, //
				c("item", "item", r(1, 6, 1, 6)));

		template = "{@org.acme.Item item}\r\n" + //
				"{data:item.|}";
		testCompletionFor(template, //
				c("base : String", "base", r(1, 11, 1, 11)), // comes from BaseItem extended by Item
				c("name : String", "name", r(1, 11, 1, 11)), //
				c("price : BigInteger", "price", r(1, 11, 1, 11)), //
				c("review : Review", "review", r(1, 11, 1, 11)), //
				c("review2 : Review", "review2", r(1, 11, 1, 11)), //
				c("getReview2() : Review", "getReview2", r(1, 11, 1, 11)));
	}

	@Test
	public void dataNamespaceWithCheckedTemplate() throws Exception {
		String template = "{data:|}";
		testCompletionFor(template, //
				0);

		testCompletionFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html", //
				"ItemResource/items", //
				1, //
				c("items", "items", r(0, 6, 0, 6)));

		template = "{data:items.|}";
		testCompletionFor(template, //
				"src/main/resources/templates/ItemResource/items.qute.html", //
				"ItemResource/items", //
				null, //
				c("size() : int", "size", r(0, 12, 0, 12)));
	}

	@Test
	public void injectNamespace() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{inject:|}";
		testCompletionFor(template, //
				2, // only inject: and not item.
				c("bean", "bean", r(1, 8, 1, 8)), //
				c("plexux", "plexux", r(1, 8, 1, 8)));

		template = "{@org.acme.Item item}\r\n" + //
				"{inject:bean.|}";
		testCompletionFor(template, //
				c("empty : boolean", "empty", r(1, 13, 1, 13)));
	}

	@Test
	public void namespaceResolver() throws Exception {
		String template = "{|}";
		testCompletionFor(template, //
				3, //
				c("inject:bean", "inject:bean", r(0, 1, 0, 1)), //
				c("inject:plexux", "inject:plexux", r(0, 1, 0, 1)), //
				c("config:getConfigProperty(propertyName : String) : Object",
						"config:getConfigProperty(${1:propertyName})$0", r(0, 1, 0, 1)));
	}

	@Test
	public void namespaceResolverAfterNamespacePart() throws Exception {
		String template = "{inject:|}";
		testCompletionFor(template, //
				2, //
				c("bean", "bean", r(0, 8, 0, 8)), //
				c("plexux", "plexux", r(0, 8, 0, 8)));

		template = "{inject:be|a}";
		testCompletionFor(template, //
				2, //
				c("bean", "bean", r(0, 8, 0, 11)), //
				c("plexux", "plexux", r(0, 8, 0, 11)));
	}

}