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
package com.redhat.qute.parser.html.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

public class HtmlScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void renardeInput() {
		// <input name="userName" >
		scanner = HtmlScanner.createScanner("<input name=\"userName\" >");
		assertOffsetAndToken(0, TokenType.Bracket, "<");
		assertOffsetAndToken(1, TokenType.ElementName, "input");
		assertOffsetAndToken(6, TokenType.Whitespace, " ");
		assertOffsetAndToken(7, TokenType.AttributeName, "name");
		assertOffsetAndToken(11, TokenType.Assign, "=");
		assertOffsetAndToken(12, TokenType.AttributeValue, "\"userName\"");
		assertOffsetAndToken(22, TokenType.Whitespace, " ");
		assertOffsetAndToken(23, TokenType.Bracket, ">");
		assertOffsetAndToken(24, TokenType.EOS, "");
	}

	@Test
	public void renardeInputMultipleAttr() {
		// <input name="userName" type="User Name" >
		scanner = HtmlScanner.createScanner("<input name=\"userName\" type=\"User Name\" >");
		assertOffsetAndToken(0, TokenType.Bracket, "<");
		assertOffsetAndToken(1, TokenType.ElementName, "input");
		assertOffsetAndToken(6, TokenType.Whitespace, " ");
		assertOffsetAndToken(7, TokenType.AttributeName, "name");
		assertOffsetAndToken(11, TokenType.Assign, "=");
		assertOffsetAndToken(12, TokenType.AttributeValue, "\"userName\"");
		assertOffsetAndToken(22, TokenType.Whitespace, " ");
		assertOffsetAndToken(23, TokenType.AttributeName, "type");
		assertOffsetAndToken(27, TokenType.Assign, "=");
		assertOffsetAndToken(28, TokenType.AttributeValue, "\"User Name\"");
		assertOffsetAndToken(39, TokenType.Whitespace, " ");
		assertOffsetAndToken(40, TokenType.Bracket, ">");
		assertOffsetAndToken(41, TokenType.EOS, "");
	}

	@Test
	public void renardeInputMultipleAttrWhitespace() {
		// <input name ="userName" type= "User Name" >
		scanner = HtmlScanner.createScanner("<input name =\"userName\" type= \"User Name\" >");
		assertOffsetAndToken(0, TokenType.Bracket, "<");
		assertOffsetAndToken(1, TokenType.ElementName, "input");
		assertOffsetAndToken(6, TokenType.Whitespace, " ");
		assertOffsetAndToken(7, TokenType.AttributeName, "name");
		assertOffsetAndToken(11, TokenType.Whitespace, " ");
		assertOffsetAndToken(12, TokenType.Assign, "=");
		assertOffsetAndToken(13, TokenType.AttributeValue, "\"userName\"");
		assertOffsetAndToken(23, TokenType.Whitespace, " ");
		assertOffsetAndToken(24, TokenType.AttributeName, "type");
		assertOffsetAndToken(28, TokenType.Assign, "=");
		assertOffsetAndToken(29, TokenType.Whitespace, " ");
		assertOffsetAndToken(30, TokenType.AttributeValue, "\"User Name\"");
		assertOffsetAndToken(41, TokenType.Whitespace, " ");
		assertOffsetAndToken(42, TokenType.Bracket, ">");
		assertOffsetAndToken(43, TokenType.EOS, "");
	}

	public void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
