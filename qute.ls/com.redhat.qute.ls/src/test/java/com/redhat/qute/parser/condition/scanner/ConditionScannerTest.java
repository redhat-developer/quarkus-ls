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
package com.redhat.qute.parser.condition.scanner; 

import static com.redhat.qute.parser.condition.scanner.ConditionScanner.createScanner;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Condition scanner tests.
 * 
 * @author Angelo ZERR
 *
 */
public class ConditionScannerTest {


	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void empty() {
		scanner = createScanner("");
		assertOffsetAndToken(0, TokenType.EOS, "");
	}
	
	@Test
	public void space() {
		scanner = createScanner(" ");
		assertOffsetAndToken(1, TokenType.EOS, "");
	}
	
	@Test
	public void string() {
		scanner = createScanner("'()'");
		assertOffsetAndToken(4, TokenType.EOS, "");
	}
	
	@Test
	public void bracketAndString() {
		scanner = createScanner("('()')");
		assertOffsetAndToken(0, TokenType.StartBracketCondition, "(");
		assertOffsetAndToken(5, TokenType.EndBracketCondition, ")");
		assertOffsetAndToken(6, TokenType.EOS, "");
	}
	
	@Test
	public void objectPart() {
		scanner = createScanner("item.");
		assertOffsetAndToken(5, TokenType.EOS, "");
	}
	
	@Test
	public void twoObjectParts() {
		scanner = createScanner("abcd != '' && item. ");
		assertOffsetAndToken(20, TokenType.EOS, "");
	}

	@Test
	public void emptyConditionBracket() {
		scanner = createScanner("()");
		assertOffsetAndToken(0, TokenType.StartBracketCondition, "(");
		assertOffsetAndToken(1, TokenType.EndBracketCondition, ")");
		assertOffsetAndToken(2, TokenType.EOS, "");
	}

	@Test
	public void composite() {
		scanner = createScanner("(item.age > 10 || item.price > 500) && user.loggedIn");
		assertOffsetAndToken(0, TokenType.StartBracketCondition, "(");
		assertOffsetAndToken(34, TokenType.EndBracketCondition, ")");
		assertOffsetAndToken(52, TokenType.EOS, "");
	}
	
	@Test
	public void composite2() {
		scanner = createScanner("(a > b or (c > d and e > f)) or g > h");
		assertOffsetAndToken(0, TokenType.StartBracketCondition, "("); // (a > b ...
		assertOffsetAndToken(10, TokenType.StartBracketCondition, "("); // ... or (c > d ...
		assertOffsetAndToken(26, TokenType.EndBracketCondition, ")"); // ... e > f)
		assertOffsetAndToken(27, TokenType.EndBracketCondition, ")"); // ... e > f))
		assertOffsetAndToken(37, TokenType.EOS, "");
	}
	
	private void assertOffsetAndToken(int tokenOffset, TokenType tokenType, String tokenText) {
		TokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}

}
