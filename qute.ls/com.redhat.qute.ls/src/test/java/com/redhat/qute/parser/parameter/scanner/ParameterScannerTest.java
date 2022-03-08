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
	public void parameterAndAssignOnly() {
		// {#let myParent= myPrice=order.price}
		scanner = ParameterScanner.createScanner("myParent= myPrice=order.price");
		assertOffsetAndToken(0, TokenType.ParameterName, "myParent");
		assertOffsetAndToken(8, TokenType.Assign, "=");
		assertOffsetAndToken(9, TokenType.Whitespace, " ");
		assertOffsetAndToken(10, TokenType.ParameterName, "myPrice");
		assertOffsetAndToken(17, TokenType.Assign, "=");
		assertOffsetAndToken(18, TokenType.ParameterValue, "order.price");
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
