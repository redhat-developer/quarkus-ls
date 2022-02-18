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
package com.redhat.qute.parser.expression.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Expression scanner tests.
 * 
 * @author Angelo ZERR
 *
 */
public class ExpressionScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void testObjectPart() {
		scanner = ExpressionScanner.createScanner("item");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndPropertyPart() {
		scanner = ExpressionScanner.createScanner("item.name");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.PropertyPart, "name");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndMethodPart() {
		scanner = ExpressionScanner.createScanner("item.name()");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.MethodPart, "name");
		assertOffsetAndToken(9, TokenType.OpenBracket, "(");
		assertOffsetAndToken(10, TokenType.CloseBracket, ")");
		assertOffsetAndToken(11, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndMethodPartWithParameters() {
		scanner = ExpressionScanner.createScanner("item.name(1, base, 'abcd')");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.MethodPart, "name");
		assertOffsetAndToken(9, TokenType.OpenBracket, "(");
		assertOffsetAndToken(19, TokenType.StartString, "'");
		assertOffsetAndToken(20, TokenType.String, "abcd");
		assertOffsetAndToken(24, TokenType.EndString, "'");
		assertOffsetAndToken(25, TokenType.CloseBracket, ")");
		assertOffsetAndToken(26, TokenType.EOS, "");
	}

	@Test
	public void testTwoParts() {
		scanner = ExpressionScanner.createScanner("a b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.ObjectPart, "b");
		assertOffsetAndToken(3, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceStartWithObject() {
		scanner = ExpressionScanner.createScanner("data:foo");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceStartWithMethod() {
		scanner = ExpressionScanner.createScanner("data:foo()");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.MethodPart, "foo");
		assertOffsetAndToken(8, TokenType.OpenBracket, "(");
		assertOffsetAndToken(9, TokenType.CloseBracket, ")");
		assertOffsetAndToken(10, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceWithProperty() {
		scanner = ExpressionScanner.createScanner("data:foo.bar");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.PropertyPart, "bar");
		assertOffsetAndToken(12, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceWithMethod() {
		scanner = ExpressionScanner.createScanner("data:foo.bar()");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.MethodPart, "bar");
		assertOffsetAndToken(12, TokenType.OpenBracket, "(");
		assertOffsetAndToken(13, TokenType.CloseBracket, ")");
		assertOffsetAndToken(14, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceWithMethodAndProperty() {
		scanner = ExpressionScanner.createScanner("data:foo.bar().baz");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.MethodPart, "bar");
		assertOffsetAndToken(12, TokenType.OpenBracket, "(");
		assertOffsetAndToken(13, TokenType.CloseBracket, ")");
		assertOffsetAndToken(14, TokenType.Dot, ".");
		assertOffsetAndToken(15, TokenType.PropertyPart, "baz");
		assertOffsetAndToken(18, TokenType.EOS, "");
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#built-in-resolvers
	 */
	@Test
	public void testElvisOperator() {
		scanner = ExpressionScanner.createScanner("person.name ?: 'John'");
		assertOffsetAndToken(0, TokenType.ObjectPart, "person");
		assertOffsetAndToken(6, TokenType.Dot, ".");
		assertOffsetAndToken(7, TokenType.PropertyPart, "name");
		assertOffsetAndToken(11, TokenType.Whitespace, " ");
		assertOffsetAndToken(12, TokenType.ElvisOperator, "?:");
		assertOffsetAndToken(14, TokenType.Whitespace, " ");
		assertOffsetAndToken(15, TokenType.StartString, "'");
		assertOffsetAndToken(16, TokenType.String, "John");
		assertOffsetAndToken(20, TokenType.EndString, "'");
		assertOffsetAndToken(21, TokenType.EOS, "");
	}

	@Test
	public void underscore() {
		scanner = ExpressionScanner.createScanner("nested-content.toString()");
		assertOffsetAndToken(0, TokenType.ObjectPart, "nested-content");
		assertOffsetAndToken(14, TokenType.Dot, ".");
		assertOffsetAndToken(15, TokenType.MethodPart, "toString");
		assertOffsetAndToken(23, TokenType.OpenBracket, "(");
		assertOffsetAndToken(24, TokenType.CloseBracket, ")");
		assertOffsetAndToken(25, TokenType.EOS, "");
	}

	private void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
