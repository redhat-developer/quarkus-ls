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
import static com.redhat.qute.QuteAssert.SECTION_SNIPPET_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion in expression for #set/#let section.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInExpressionWithLetSectionTest {

	@Test
	public void objectPart() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=item.name isActive=false age=10} \r\n" + //
				"  <h1>{myParent.name}</h1>\r\n" + //
				"  Is active: {|}\r\n" + // <-- completion here
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("item", "item", r(3, 14, 3, 14)), //
				c("myParent", "myParent", r(3, 14, 3, 14)), //
				c("isActive", "isActive", r(3, 14, 3, 14)), //
				c("age", "age", r(3, 14, 3, 14)));
	}

	@Test
	public void objectPartWithSpaces() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent =  item.name         isActive =false age= 10} \r\n" + //
				"  <h1>{myParent.name}</h1>\r\n" + //
				"  Is active: {|}\r\n" + // <-- completion here
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("item", "item", r(3, 14, 3, 14)), //
				c("myParent", "myParent", r(3, 14, 3, 14)), //
				c("isActive", "isActive", r(3, 14, 3, 14)), //
				c("age", "age", r(3, 14, 3, 14)));
	}
	
	@Test
	public void objectPartWithOnlyStartBracket() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=item.name isActive=false age=10} \r\n" + //
				"  <h1>{myParent.name}</h1>\r\n" + //
				"  Is active: {|\r\n" + // <-- completion here
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("item", "item", r(3, 14, 3, 14)), //
				c("myParent", "myParent", r(3, 14, 3, 14)), //
				c("isActive", "isActive", r(3, 14, 3, 14)), //
				c("age", "age", r(3, 14, 3, 14)));
	}

	@Test
	public void badAssignment() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=XXXXXX isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, 0);

		template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent= isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, 0);

		template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, 0);
	}

	@Test
	public void propertyPartAssignedWithClass() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=item isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("name : String", "name", r(2, 16, 2, 16)), //
				c("price : BigInteger", "price", r(2, 16, 2, 16)), //
				c("review : Review", "review", r(2, 16, 2, 16)), //
				c("review2 : Review", "review2", r(2, 16, 2, 16)), //
				c("getReview2() : Review", "getReview2", r(2, 16, 2, 16)));
	}

	@Test
	public void propertyPartAssignedWithClassProperty() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=item.name isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("UTF16 : byte", "UTF16", r(2, 16, 2, 16)));
	}

	@Test
	public void propertyPartAssignedWithClassMethod() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#let myParent=item.getReview2() isActive=false age=10} \r\n" + //
				"  <h1>{myParent.|}</h1>\r\n" + //
				"  Is active: {isActive}\r\n" + //
				"  Age: {age}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				c("name : String", "name", r(2, 16, 2, 16)));
	}

	@Test
	public void noCompletionInStartTag() throws Exception {
		String template = "{#|let foo=bar}\r\n" + //
				"{/}";
		testCompletionFor(template, 0 + SECTION_SNIPPET_SIZE /* section snippets */);
		
		template = "{#|let foo=bar}\r\n" + //
				"{/let}";
		testCompletionFor(template, 0);
	}

	@Test
	public void completionInStartTagWhenNotClosed() throws Exception {
		String template = "{#|let\r\n";
		testCompletionFor(template, SECTION_SNIPPET_SIZE /* see qute-snippets.json #for, #each, etc */);
	}

	@Test
	public void noCompletionInEndTag() throws Exception {
		String template;
		template = "{#let foo=bar}\r\n" + //
				"{|/}";
		testCompletionFor(template, 0);

		template = "{#let foo=bar}\r\n" + //
				"{/|}";
		testCompletionFor(template, 0);

		template = "{#let foo=bar}\r\n" + //
				"{/l|et}";
		testCompletionFor(template, 0);

	}

	@Test
	public void completionInMethodParameter() throws Exception {
		String template = "{#let name=1}\r\n" + //
				"	{foo.method(n|}\r\n" + //
				"{/let}";
		testCompletionFor(template, //
				RESOLVERS_SIZE + 1, //
				c("name", "name", r(1, 13, 1, 14)), //
				c("inject:bean", "inject:bean", r(1, 13, 1, 14)), //
				c("inject:plexux", "inject:plexux", r(1, 13, 1, 14)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 13, 1, 14)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 13, 1, 14)), //
				c("GLOBAL", "GLOBAL", r(1, 13, 1, 14)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 13, 1, 14)), //
				c("uri:Login", "uri:Login", r(1, 13, 1, 14)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(1, 13, 1, 14)),//
				c("msg2:hello() : String", "msg2:hello", r(1, 13, 1, 14)), //
				c("bundle", "bundle", r(1, 13, 1, 14)));
	}
}