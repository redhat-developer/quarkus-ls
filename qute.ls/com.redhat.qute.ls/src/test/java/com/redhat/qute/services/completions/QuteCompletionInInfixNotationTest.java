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

import static com.redhat.qute.QuteAssert.RESOLVERS_SIZE;
import static com.redhat.qute.QuteAssert.c;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testCompletionFor;

import org.junit.jupiter.api.Test;

/**
 * Tests for Qute completion with infix notation.
 *
 * @author Angelo ZERR
 *
 */
public class QuteCompletionInInfixNotationTest {

	@Test
	public void noInfixNotation() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name.|}";

		// NO Infix notation : methods with any parameters, fields... of String class
		testCompletionFor(template, //
				13, //
				// - resolvers
				c("orEmpty(base : T) : List<T>", "orEmpty", r(1, 11, 1, 11)),
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy(${1:arg})$0", r(1, 11, 1, 11)),
				c("or(base : T, arg : Object) : T", "or(${1:arg})$0", r(1, 11, 1, 11)),
				// - String Java fields
				c("UTF16 : byte", "UTF16", r(1, 11, 1, 11)),
				// - String Java methods
				c("getBytes() : byte[]", "getBytes", r(1, 11, 1, 11)),
				c("getBytes(charsetName : String) : byte[]", "getBytes(${1:charsetName})$0", r(1, 11, 1, 11)),
				c("charAt(index : int) : char", "charAt(${1:index})$0", r(1, 11, 1, 11)));
	}

	@Test
	public void infixNotation() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name |}";

		// Infix notation : only methods with one parameter of String class
		testCompletionFor(template, //
				4,
				// - resolvers
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy ${1:arg}$0", r(1, 11, 1, 11)),
				c("or(base : T, arg : Object) : T", "or ${1:arg}$0", r(1, 11, 1, 11)),
				// - String Java methods
				c("getBytes(charsetName : String) : byte[]", "getBytes ${1:charsetName}$0", r(1, 11, 1, 11)),
				c("charAt(index : int) : char", "charAt ${1:index}$0", r(1, 11, 1, 11)));
	}

	@Test
	public void infixNotationWithSeveralParts() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name or item.name |}";

		// Infix notation : only methods with one parameter of String class
		testCompletionFor(template, //
				4,
				// - resolvers
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy ${1:arg}$0", r(1, 24, 1, 24)),
				c("or(base : T, arg : Object) : T", "or ${1:arg}$0", r(1, 24, 1, 24)),
				// - String Java methods
				c("getBytes(charsetName : String) : byte[]", "getBytes ${1:charsetName}$0", r(1, 24, 1, 24)),
				c("charAt(index : int) : char", "charAt ${1:index}$0", r(1, 24, 1, 24)));
	}

	@Test
	public void elvisOperator() throws Exception {
		String template = "{@org.acme.Item item}\r\n" + //
				"{item.name ?: |}";

		// Infix notation : only methods with one parameter of String class
		testCompletionFor(template, //
				RESOLVERS_SIZE + 1, // , //
				c("item", "item", r(1, 14, 1, 14)), //
				c("inject:bean", "inject:bean", r(1, 14, 1, 14)), //
				c("inject:plexux", "inject:plexux", r(1, 14, 1, 14)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 14, 1, 14)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 14, 1, 14)), //
				c("GLOBAL", "GLOBAL", r(1, 14, 1, 14)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 14, 1, 14)), //
				c("uri:Login", "uri:Login", r(1, 14, 1, 14)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(1, 14, 1, 14)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 14, 1, 14)), //
				c("bundle", "bundle", r(1, 14, 1, 14)));

		template = "{@org.acme.Item item}\r\n" + //
				"{item.name ?: item.name :|}";

		// Infix notation : only methods with one parameter of String class
		testCompletionFor(template, //
				RESOLVERS_SIZE + 1, //
				c("item", "item", r(1, 25, 1, 25)), //
				c("inject:bean", "inject:bean", r(1, 25, 1, 25)), //
				c("inject:plexux", "inject:plexux", r(1, 25, 1, 25)), //
				c("config:*(propertyName : String) : Object", "config:${1:propertyName}$0", r(1, 25, 1, 25)),
				c("config:property(propertyName : String) : Object", "config:property(${1:propertyName})$0",
						r(1, 25, 1, 25)), //
				c("GLOBAL", "GLOBAL", r(1, 25, 1, 25)), //
				c("VARCHAR_SIZE", "VARCHAR_SIZE", r(1, 25, 1, 25)), //
				c("uri:Login", "uri:Login", r(1, 25, 1, 25)), //
				c("msg:hello_name(name : String) : String", "msg:hello_name(${1:name})$0", r(1, 25, 1, 25)), //
				c("msg2:hello() : String", "msg2:hello", r(1, 25, 1, 25)), //
				c("bundle", "bundle", r(1, 25, 1, 25)));
	}

	@Test
	public void infixNotationWithNumbers() throws Exception {
		String template = "{@int foo}\r\n" + //
				"{foo + |}";

		// Infix notation : only methods with one parameter of String class
		testCompletionFor(template, //
				5,
				// - resolvers
				c("ifTruthy(base : T, arg : Object) : T", "ifTruthy ${1:arg}$0", r(1, 7, 1, 7)),
				c("or(base : T, arg : Object) : T", "or ${1:arg}$0", r(1, 7, 1, 7)),
				// Numbers resolvers
				c("plus(number : Integer, name : String, other : Integer) : Integer", "plus ${1:other}$0",
						r(1, 7, 1, 7)),
				c("minus(number : Integer, name : String, other : Integer) : Integer", "minus ${1:other}$0",
						r(1, 7, 1, 7)),
				c("mod(number : Integer, mod : Integer) : Integer", "mod ${1:mod}$0", r(1, 7, 1, 7)));
	}

}