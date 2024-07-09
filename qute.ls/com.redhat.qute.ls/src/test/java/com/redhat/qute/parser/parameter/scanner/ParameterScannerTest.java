package com.redhat.qute.parser.parameter.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class ParameterScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void parameterDeclarations() {
		// {@org.acme.Foo foo}
		scanner = ParameterScanner.createScanner("org.acme.Foo foo");
		assertOffsetAndToken(0, TokenType.ParameterName, "org.acme.Foo");
		assertOffsetAndToken(12, TokenType.Whitespace, " ");
		assertOffsetAndToken(13, TokenType.ParameterName, "foo");
		assertOffsetAndToken(16, TokenType.EOS, "");
	}

	@Test
	public void eachSection() {
		// {#each items}
		scanner = ParameterScanner.createScanner("items");
		assertOffsetAndToken(0, TokenType.ParameterName, "items");
		assertOffsetAndToken(5, TokenType.EOS, "");
	}

	@Test
	public void forSection() {
		// {#for item in items}
		scanner = ParameterScanner.createScanner("item in items");
		assertOffsetAndToken(0, TokenType.ParameterName, "item");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterName, "in");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.ParameterName, "items");
		assertOffsetAndToken(13, TokenType.EOS, "");
	}
	
	@Test
	public void forSectionWithMethod() {
		// {#for item in map.values()}
		scanner = ParameterScanner.createScanner("item in map.values()");
		assertOffsetAndToken(0, TokenType.ParameterName, "item");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterName, "in");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.ParameterName, "map.values()");
		assertOffsetAndToken(20, TokenType.EOS, "");
	}
	
	@Test
	public void forSectionWithTwoMethods() {
		// {#for item in map.get('foo',  10).values()}
		scanner = ParameterScanner.createScanner("item in map.get('foo',  10).values()");
		assertOffsetAndToken(0, TokenType.ParameterName, "item");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterName, "in");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.ParameterName, "map.get('foo',  10).values()");
		assertOffsetAndToken(36, TokenType.EOS, "");
	}

	@Test
	public void forSectionWithTwoMethodsAndOneParameter() {
		// {#for item in map.get('foo',  10).values()   bar}
		scanner = ParameterScanner.createScanner("item in map.get('foo',  10).values()   bar");
		assertOffsetAndToken(0, TokenType.ParameterName, "item");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterName, "in");
		assertOffsetAndToken(7, TokenType.Whitespace, " ");
		assertOffsetAndToken(8, TokenType.ParameterName, "map.get('foo',  10).values()");
		assertOffsetAndToken(36, TokenType.Whitespace, "   ");
		assertOffsetAndToken(39, TokenType.ParameterName, "bar");
		assertOffsetAndToken(42, TokenType.EOS, "");
	}
	
	@Test
	public void letSection() {
		// {#let myParent=order.item.parent myPrice=order.price}
		scanner = ParameterScanner.createScanner("myParent=order.item.parent myPrice=order.price");
		assertOffsetAndToken(0, TokenType.ParameterName, "myParent");
		assertOffsetAndToken(8, TokenType.Assign, "=");
		assertOffsetAndToken(9, TokenType.ParameterValue, "order.item.parent");
		assertOffsetAndToken(26, TokenType.Whitespace, " ");
		assertOffsetAndToken(27, TokenType.ParameterName, "myPrice");
		assertOffsetAndToken(34, TokenType.Assign, "=");
		assertOffsetAndToken(35, TokenType.ParameterValue, "order.price");
		assertOffsetAndToken(46, TokenType.EOS, "");
	}

	@Test
	public void letSectionWithSpaces() {
		// {#let    myParent  =order.item.parent   myPrice=    order.price}
		scanner = ParameterScanner.createScanner("   myParent  =order.item.parent   myPrice=    order.price");
		assertOffsetAndToken(0, TokenType.Whitespace, "   ");
		assertOffsetAndToken(3, TokenType.ParameterName, "myParent");
		assertOffsetAndToken(11, TokenType.Whitespace, "  ");
		assertOffsetAndToken(13, TokenType.Assign, "=");
		assertOffsetAndToken(14, TokenType.ParameterValue, "order.item.parent");
		assertOffsetAndToken(31, TokenType.Whitespace, "   ");
		assertOffsetAndToken(34, TokenType.ParameterName, "myPrice");
		assertOffsetAndToken(41, TokenType.Assign, "=");
		assertOffsetAndToken(42, TokenType.Whitespace, "    ");
		assertOffsetAndToken(46, TokenType.ParameterValue, "order.price");
		assertOffsetAndToken(57, TokenType.EOS, "");
	}

	@Test
	public void parameterAndAssignOnly() {
		// {#let myParent= myPrice=order.price}
		scanner = ParameterScanner.createScanner("myParent= myPrice=order.price");
		assertOffsetAndToken(0, TokenType.ParameterName, "myParent");
		assertOffsetAndToken(8, TokenType.Assign, "=");
		assertOffsetAndToken(9, TokenType.Whitespace, " ");
		assertOffsetAndToken(10, TokenType.ParameterValue, "myPrice");
		assertOffsetAndToken(17, TokenType.Unknown, "=");
		assertOffsetAndToken(18, TokenType.ParameterName, "order.price");
		assertOffsetAndToken(29, TokenType.EOS, "");
	}

	@Test
	public void stringParameter() {
		// {#formElement name="User Name"}
		scanner = ParameterScanner.createScanner("name=\"User Name\"");
		assertOffsetAndToken(0, TokenType.ParameterName, "name");
		assertOffsetAndToken(4, TokenType.Assign, "=");
		assertOffsetAndToken(5, TokenType.ParameterValue, "\"User Name\"");
		assertOffsetAndToken(16, TokenType.EOS, "");
	}
	
	@Test
	public void parameterWithMethod() {
		// {#let foo= bar(0,1)}
		scanner = ParameterScanner.createScanner("foo= bar(0,1)");
		assertOffsetAndToken(0, TokenType.ParameterName, "foo");
		assertOffsetAndToken(3, TokenType.Assign, "=");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterValue, "bar(0,1)");
		assertOffsetAndToken(13, TokenType.EOS, "");
	}

	@Test
	public void parameterWithMethodAndSpaceInParameter() {
		// {#let foo= bar(0,1)}
		scanner = ParameterScanner.createScanner("foo= bar(0,  1)");
		assertOffsetAndToken(0, TokenType.ParameterName, "foo");
		assertOffsetAndToken(3, TokenType.Assign, "=");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterValue, "bar(0,  1)");
		assertOffsetAndToken(15, TokenType.EOS, "");
	}

	@Test
	public void parameterWithSeveralMethodAndSpaceInParameter() {
		// {#let p1 = 10  p2= bar(0,  1) p3= 10  p4 = foo(  0,  1, 3)}
		scanner = ParameterScanner.createScanner("p1 = 10  p2= bar(0,  1) p3= 10  p4 = foo(  0,  1, 3)");
		
		// p1 = 10
		assertOffsetAndToken(0, TokenType.ParameterName, "p1");
		assertOffsetAndToken(2, TokenType.Whitespace, " ");
		assertOffsetAndToken(3, TokenType.Assign, "=");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterValue, "10");
		assertOffsetAndToken(7, TokenType.Whitespace, "  ");
		
		// p2= bar(0,  1)
		assertOffsetAndToken(9, TokenType.ParameterName, "p2");
		assertOffsetAndToken(11, TokenType.Assign, "=");
		assertOffsetAndToken(12, TokenType.Whitespace, " ");
		assertOffsetAndToken(13, TokenType.ParameterValue, "bar(0,  1)");

		// p3= 10
		assertOffsetAndToken(23, TokenType.Whitespace, " ");
		assertOffsetAndToken(24, TokenType.ParameterName, "p3");
		assertOffsetAndToken(26, TokenType.Assign, "=");
		assertOffsetAndToken(27, TokenType.Whitespace, " ");
		assertOffsetAndToken(28, TokenType.ParameterValue, "10");
		
		// p4 = bar3(0,  1)
		assertOffsetAndToken(30, TokenType.Whitespace, "  ");
		assertOffsetAndToken(32, TokenType.ParameterName, "p4");
		assertOffsetAndToken(34, TokenType.Whitespace, " ");
		assertOffsetAndToken(35, TokenType.Assign, "=");
		assertOffsetAndToken(36, TokenType.Whitespace, " ");
		assertOffsetAndToken(37, TokenType.ParameterValue, "foo(  0,  1, 3)");

		assertOffsetAndToken(52, TokenType.EOS, "");
	}
	
	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
