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
		scanner = createInfixNotationScanner("item");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndPropertyPart() {
		scanner = createInfixNotationScanner("item.name");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.PropertyPart, "name");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndMethodPart() {
		scanner = createInfixNotationScanner("item.name()");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.MethodPart, "name");
		assertOffsetAndToken(9, TokenType.OpenBracket, "(");
		assertOffsetAndToken(10, TokenType.CloseBracket, ")");
		assertOffsetAndToken(11, TokenType.EOS, "");
	}

	@Test
	public void testObjectAndMethodPartWithParameters() {
		scanner = createInfixNotationScanner("item.name(1, base, 'abcd')");
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
	public void testNamespaceStartWithObject() {
		scanner = createInfixNotationScanner("data:foo");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceStartWithMethod() {
		scanner = createInfixNotationScanner("data:foo()");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.MethodPart, "foo");
		assertOffsetAndToken(8, TokenType.OpenBracket, "(");
		assertOffsetAndToken(9, TokenType.CloseBracket, ")");
		assertOffsetAndToken(10, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceWithProperty() {
		scanner = createInfixNotationScanner("data:foo.bar");
		assertOffsetAndToken(0, TokenType.NamespacePart, "data");
		assertOffsetAndToken(4, TokenType.ColonSpace, ":");
		assertOffsetAndToken(5, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(8, TokenType.Dot, ".");
		assertOffsetAndToken(9, TokenType.PropertyPart, "bar");
		assertOffsetAndToken(12, TokenType.EOS, "");
	}

	@Test
	public void testNamespaceWithMethod() {
		scanner = createInfixNotationScanner("data:foo.bar()");
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
		scanner = createInfixNotationScanner("data:foo.bar().baz");
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
		scanner = createInfixNotationScanner("person.name ?: 'John'");
		assertOffsetAndToken(0, TokenType.ObjectPart, "person");
		assertOffsetAndToken(6, TokenType.Dot, ".");
		assertOffsetAndToken(7, TokenType.PropertyPart, "name");
		assertOffsetAndToken(11, TokenType.Whitespace, " ");
		assertOffsetAndToken(12, TokenType.InfixMethodPart, "?:");
		assertOffsetAndToken(14, TokenType.Whitespace, " ");
		assertOffsetAndToken(15, TokenType.InfixParameter, "'John'");
		assertOffsetAndToken(21, TokenType.EOS, "");
	}

	@Test
	public void underscore() {
		scanner = createInfixNotationScanner("nested-content.toString()");
		assertOffsetAndToken(0, TokenType.ObjectPart, "nested-content");
		assertOffsetAndToken(14, TokenType.Dot, ".");
		assertOffsetAndToken(15, TokenType.MethodPart, "toString");
		assertOffsetAndToken(23, TokenType.OpenBracket, "(");
		assertOffsetAndToken(24, TokenType.CloseBracket, ")");
		assertOffsetAndToken(25, TokenType.EOS, "");
	}

	// Infix notation tests
	
	@Test
	public void testTwoPartsWithInfixNotation() {
		scanner = createInfixNotationScanner("a b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "b"); // infix notation -> method part
		assertOffsetAndToken(3, TokenType.EOS, "");
	}

	@Test
	public void testTwoPartsWithoutInfixNotation() {
		scanner = createNoInfixNotationScanner("a b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.ObjectPart, "b"); // No infix notation -> object part
		assertOffsetAndToken(3, TokenType.EOS, "");
	}
	
	@Test
	public void testThreePartsWithInfixNotation() {
		scanner = createInfixNotationScanner("a b c");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "b");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "c");
		assertOffsetAndToken(5, TokenType.EOS, "");
	}
	
	@Test
	public void testSeveralPartsWithInfixNotation() {
		scanner = createInfixNotationScanner("a b c d e");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "b");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "c");
		assertOffsetAndToken(5, TokenType.Whitespace, " ");
		assertOffsetAndToken(6, TokenType.InfixMethodPart, "d");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.InfixParameter, "e");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}
	
	@Test
	public void testThreePartsWithoutInfixNotation() {
		scanner = createNoInfixNotationScanner("a b c");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.ObjectPart, "b");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.ObjectPart, "c");
		assertOffsetAndToken(5, TokenType.EOS, "");
	}
	
	@Test
	public void testOrInfixNotation() {
		scanner = createInfixNotationScanner("person.name or 'John'");
		assertOffsetAndToken(0, TokenType.ObjectPart, "person");
		assertOffsetAndToken(6, TokenType.Dot, ".");
		assertOffsetAndToken(7, TokenType.PropertyPart, "name");
		assertOffsetAndToken(11, TokenType.Whitespace, " ");
		assertOffsetAndToken(12, TokenType.InfixMethodPart, "or");
		assertOffsetAndToken(14, TokenType.Whitespace, " ");
		assertOffsetAndToken(15, TokenType.InfixParameter, "'John'");
		assertOffsetAndToken(21, TokenType.EOS, "");
	}
	
	@Test
	public void testCharAtInfixNotation() {
		scanner = createInfixNotationScanner("foo charAt '1'");
		assertOffsetAndToken(0, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixMethodPart, "charAt");
		assertOffsetAndToken(10, TokenType.Whitespace, " ");
		assertOffsetAndToken(11, TokenType.InfixParameter, "'1'");
		assertOffsetAndToken(14, TokenType.EOS, "");
	}
	
	@Test
	public void testCharAtNoInfixNotation() {
		scanner = createNoInfixNotationScanner("foo charAt '1'");
		assertOffsetAndToken(0, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.ObjectPart, "charAt");
		assertOffsetAndToken(10, TokenType.Whitespace, " ");
		assertOffsetAndToken(11, TokenType.StartString, "'");
		assertOffsetAndToken(12, TokenType.String, "1");
		assertOffsetAndToken(13, TokenType.EndString, "'");
		assertOffsetAndToken(14, TokenType.EOS, "");
	}
	
	@Test
	public void testMethodsAndInfixNotation() {
		scanner = createInfixNotationScanner("items.get(0) or 1");
		assertOffsetAndToken(0, TokenType.ObjectPart, "items");
		assertOffsetAndToken(5, TokenType.Dot, ".");
		assertOffsetAndToken(6, TokenType.MethodPart, "get");
		assertOffsetAndToken(9, TokenType.OpenBracket, "(");
		assertOffsetAndToken(11, TokenType.CloseBracket, ")");
		assertOffsetAndToken(12, TokenType.Whitespace, " ");
		assertOffsetAndToken(13, TokenType.InfixMethodPart, "or");
		assertOffsetAndToken(15, TokenType.Whitespace, " ");
		assertOffsetAndToken(16, TokenType.InfixParameter, "1");
		assertOffsetAndToken(17, TokenType.EOS, "");
	}
	
	@Test
	public void dotSpace() {
		scanner = createInfixNotationScanner("items. ");
		assertOffsetAndToken(0, TokenType.ObjectPart, "items");
		assertOffsetAndToken(5, TokenType.Dot, ".");
		assertOffsetAndToken(6, TokenType.Whitespace, " ");
		assertOffsetAndToken(7, TokenType.EOS, "");
	}
	
	@Test
	public void twoMethodsWithOr() {
		scanner = createInfixNotationScanner("item.name or item.name");
		assertOffsetAndToken(0, TokenType.ObjectPart, "item");
		assertOffsetAndToken(4, TokenType.Dot, ".");
		assertOffsetAndToken(5, TokenType.PropertyPart, "name");
		assertOffsetAndToken(9, TokenType.Whitespace, " ");
		assertOffsetAndToken(10, TokenType.InfixMethodPart, "or");
		assertOffsetAndToken(12, TokenType.Whitespace, " ");
		assertOffsetAndToken(13, TokenType.InfixParameter, "item.name");
		assertOffsetAndToken(22, TokenType.EOS, "");
	}
	
	@Test
	public void infixNotationWithBracket() {
		scanner = createInfixNotationScanner("foo getBytes()");
		assertOffsetAndToken(0, TokenType.ObjectPart, "foo");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixMethodPart, "getBytes()");
		assertOffsetAndToken(14, TokenType.EOS, "");
	}

	@Test
	public void elvisOperator() {
		scanner = createInfixNotationScanner("name ?: \"Quarkus Insights\"");
		assertOffsetAndToken(0, TokenType.ObjectPart, "name");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.InfixMethodPart, "?:");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.InfixParameter, "\"Quarkus Insights\"");
		assertOffsetAndToken(26, TokenType.EOS, "");
	}
	
	private void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}

	private ExpressionScanner createInfixNotationScanner(String input) {
		return ExpressionScanner.createScanner(input, true);
	}
	
	private ExpressionScanner createNoInfixNotationScanner(String input) {
		return ExpressionScanner.createScanner(input, false);
	}
}
