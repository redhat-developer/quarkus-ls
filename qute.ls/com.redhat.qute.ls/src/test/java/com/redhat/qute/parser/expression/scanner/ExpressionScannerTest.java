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

	@Test
	public void configNamespaceWithString() {
		scanner = createInfixNotationScanner("config:\"quarkus.application.name\"");
		assertOffsetAndToken(0, TokenType.NamespacePart, "config");
		assertOffsetAndToken(6, TokenType.ColonSpace, ":");
		assertOffsetAndToken(7, TokenType.StartString, "\"");
		assertOffsetAndToken(8, TokenType.String, "quarkus.application.name");
		assertOffsetAndToken(32, TokenType.EndString, "\"");
		assertOffsetAndToken(33, TokenType.EOS, "");
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

	@Test
	public void testTernaryWithMethodAndSpaces() {
		// a ? s.substring( b, c ) : d
		scanner = createInfixNotationScanner("a ? s.substring(   b,       c   ) : d");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "s.substring(   b,       c   )");
		assertOffsetAndToken(33, TokenType.Whitespace, " ");
		assertOffsetAndToken(34, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(35, TokenType.Whitespace, " ");
		assertOffsetAndToken(36, TokenType.InfixParameter, "d");
		assertOffsetAndToken(37, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithMethodWithoutSpaces() {
		// a ? s.substring(b,c) : d
		scanner = createInfixNotationScanner("a ? s.substring(b,c) : d");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "s.substring(b,c)");
		assertOffsetAndToken(20, TokenType.Whitespace, " ");
		assertOffsetAndToken(21, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(22, TokenType.Whitespace, " ");
		assertOffsetAndToken(23, TokenType.InfixParameter, "d");
		assertOffsetAndToken(24, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithSimpleValues() {
		// a ? b : c
		scanner = createInfixNotationScanner("a ? b : c");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "b");
		assertOffsetAndToken(5, TokenType.Whitespace, " ");
		assertOffsetAndToken(6, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.InfixParameter, "c");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithNestedMethodCallAndSpaces() {
		// a ? s:foo(s.bar( x, y ), z ) : d
		scanner = createInfixNotationScanner("a ? s:foo(s.bar(   x,   y   ),   z   ) : d");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "s:foo(s.bar(   x,   y   ),   z   )");
		assertOffsetAndToken(38, TokenType.Whitespace, " ");
		assertOffsetAndToken(39, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(40, TokenType.Whitespace, " ");
		assertOffsetAndToken(41, TokenType.InfixParameter, "d");
		assertOffsetAndToken(42, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithStringParameter() {
		// a ? s.substring(b, 'foo bar') : d
		scanner = createInfixNotationScanner("a ? s.substring(b, 'foo bar') : d");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "s.substring(b, 'foo bar')");
		assertOffsetAndToken(29, TokenType.Whitespace, " ");
		assertOffsetAndToken(30, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(31, TokenType.Whitespace, " ");
		assertOffsetAndToken(32, TokenType.InfixParameter, "d");
		assertOffsetAndToken(33, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithNamespaceInFalseParameter() {
		// extDialogId ? extDialogId : str:concat('form-dlg-', title)
		scanner = createInfixNotationScanner("extDialogId ? extDialogId : str:concat('form-dlg-', title)");
		assertOffsetAndToken(0, TokenType.ObjectPart, "extDialogId");
		assertOffsetAndToken(11, TokenType.Whitespace, " ");
		assertOffsetAndToken(12, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(13, TokenType.Whitespace, " ");
		assertOffsetAndToken(14, TokenType.InfixParameter, "extDialogId");
		assertOffsetAndToken(25, TokenType.Whitespace, " ");
		assertOffsetAndToken(26, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(27, TokenType.Whitespace, " ");
		assertOffsetAndToken(28, TokenType.InfixParameter, "str:concat('form-dlg-', title)");
		assertOffsetAndToken(58, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithNamespaceInTrueAndFalseParameter() {
		// a ? str:format('hello %s', name) : str:concat('foo-', title)
		scanner = createInfixNotationScanner("a ? str:format('hello %s', name) : str:concat('foo-', title)");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "str:format('hello %s', name)");
		assertOffsetAndToken(32, TokenType.Whitespace, " ");
		assertOffsetAndToken(33, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(34, TokenType.Whitespace, " ");
		assertOffsetAndToken(35, TokenType.InfixParameter, "str:concat('foo-', title)");
		assertOffsetAndToken(60, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithNamespaceAndSpacesInParameters() {
		// a ? str:concat( 'foo-', title ) : str:concat( 'bar-', name )
		scanner = createInfixNotationScanner(
				"a ? str:concat(   'foo-',   title   ) : str:concat(   'bar-',   name   )");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "str:concat(   'foo-',   title   )");
		assertOffsetAndToken(37, TokenType.Whitespace, " ");
		assertOffsetAndToken(38, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(39, TokenType.Whitespace, " ");
		assertOffsetAndToken(40, TokenType.InfixParameter, "str:concat(   'bar-',   name   )");
		assertOffsetAndToken(72, TokenType.EOS, "");
	}

	@Test
	public void testTernaryWithNamespaceOnly() {
		// a ? str:concat('foo-', title) : b
		scanner = createInfixNotationScanner("a ? str:concat('foo-', title) : b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "str:concat('foo-', title)");
		assertOffsetAndToken(29, TokenType.Whitespace, " ");
		assertOffsetAndToken(30, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(31, TokenType.Whitespace, " ");
		assertOffsetAndToken(32, TokenType.InfixParameter, "b");
		assertOffsetAndToken(33, TokenType.EOS, "");
	}

	@Test
	public void testTernary() {
		// a ? str.concat('foo-', title) : b
		scanner = createInfixNotationScanner("a ? str.concat('foo-', title) : b");
		assertOffsetAndToken(0, TokenType.ObjectPart, "a");
		assertOffsetAndToken(1, TokenType.Whitespace, " ");
		assertOffsetAndToken(2, TokenType.InfixMethodPart, "?");
		assertOffsetAndToken(3, TokenType.Whitespace, " ");
		assertOffsetAndToken(4, TokenType.InfixParameter, "str.concat('foo-', title)");
		assertOffsetAndToken(29, TokenType.Whitespace, " ");
		assertOffsetAndToken(30, TokenType.InfixMethodPart, ":");
		assertOffsetAndToken(31, TokenType.Whitespace, " ");
		assertOffsetAndToken(32, TokenType.InfixParameter, "b");
		assertOffsetAndToken(33, TokenType.EOS, "");
	}

	@Test
	public void testMethodWithInfixNotationAsParameter() {
		// s.substring(list ?: 0) -- outer expression WITHOUT infix notation
		scanner = createNoInfixNotationScanner("s.substring(list ?: 0)");
		assertOffsetAndToken(0, TokenType.ObjectPart, "s");
		assertOffsetAndToken(1, TokenType.Dot, ".");
		assertOffsetAndToken(2, TokenType.MethodPart, "substring");
		assertOffsetAndToken(11, TokenType.OpenBracket, "(");
		assertOffsetAndToken(21, TokenType.CloseBracket, ")");
		assertOffsetAndToken(22, TokenType.EOS, "");
	}

	@Test
	public void testMethodWithInfixNotationAsParameterWithInfixOuter() {
		// s.substring(list ?: 0) -- outer expression WITH infix notation
		// content inside '(' ')' is still consumed as a single opaque block
		scanner = createInfixNotationScanner("s.substring(list ?: 0)");
		assertOffsetAndToken(0, TokenType.ObjectPart, "s");
		assertOffsetAndToken(1, TokenType.Dot, ".");
		assertOffsetAndToken(2, TokenType.MethodPart, "substring");
		assertOffsetAndToken(11, TokenType.OpenBracket, "(");
		assertOffsetAndToken(21, TokenType.CloseBracket, ")");
		assertOffsetAndToken(22, TokenType.EOS, "");
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
