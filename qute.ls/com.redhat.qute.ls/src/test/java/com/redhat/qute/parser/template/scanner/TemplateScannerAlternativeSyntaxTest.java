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
package com.redhat.qute.parser.template.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Tests for template scanner {@link TemplateScanner} with alternative syntax
 * expression command (e.g., {=foo} instead of {foo}).
 *
 * This syntax is activated via quarkus.qute.alt-...=true configuration.
 *
 * @author Angelo ZERR
 *
 */
public class TemplateScannerAlternativeSyntaxTest {

	private static final Character EXPRESSION_COMMAND = '=';

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void testExpression() {
		scanner = TemplateScanner.createScanner("{=abcd}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(6, TokenType.EndExpression, "}");
		assertOffsetAndToken(7, TokenType.EOS, "");
	}

	@Test
	public void testExpressionWithString() {
		scanner = TemplateScanner.createScanner("{=abc'}'d}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(5, TokenType.StartString, "'");
		assertOffsetAndToken(6, TokenType.String, "}");
		assertOffsetAndToken(7, TokenType.EndString, "'");
		assertOffsetAndToken(9, TokenType.EndExpression, "}");
		assertOffsetAndToken(10, TokenType.EOS, "");
	}

	@Test
	public void testExpressionWithProperty() {
		scanner = TemplateScanner.createScanner("{=item.name}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(11, TokenType.EndExpression, "}");
		assertOffsetAndToken(12, TokenType.EOS, "");
	}

	@Test
	public void testExpressionWithMethod() {
		scanner = TemplateScanner.createScanner("{=item.getPrice()}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(17, TokenType.EndExpression, "}");
		assertOffsetAndToken(18, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithoutCommand() {
		// Without the '=' command, {abcd} should be treated as content, not an expression
		scanner = TemplateScanner.createScanner("{abcd}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.Content, "{abcd}");
		assertOffsetAndToken(6, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithSpace() {
		scanner = TemplateScanner.createScanner("{= abcd}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(2, TokenType.Whitespace, " ");
		assertOffsetAndToken(7, TokenType.EndExpression, "}");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	@Test
	public void mixedExpressionAndText() {
		scanner = TemplateScanner.createScanner("Hello {=name}!", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.Content, "Hello ");
		assertOffsetAndToken(6, TokenType.StartExpression, "{=");
		assertOffsetAndToken(12, TokenType.EndExpression, "}");
		assertOffsetAndToken(13, TokenType.Content, "!");
		assertOffsetAndToken(14, TokenType.EOS, "");
	}

	@Test
	public void multipleExpressions() {
		scanner = TemplateScanner.createScanner("{=foo} and {=bar}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(5, TokenType.EndExpression, "}");
		assertOffsetAndToken(6, TokenType.Content, " and ");
		assertOffsetAndToken(11, TokenType.StartExpression, "{=");
		assertOffsetAndToken(16, TokenType.EndExpression, "}");
		assertOffsetAndToken(17, TokenType.EOS, "");
	}

	@Test
	public void commentStillWorks() {
		// Comments should still work normally with {! !}
		scanner = TemplateScanner.createScanner("{! This is a comment !}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartComment, "{!");
		assertOffsetAndToken(2, TokenType.Comment, " This is a comment ");
		assertOffsetAndToken(21, TokenType.EndComment, "!}");
		assertOffsetAndToken(23, TokenType.EOS, "");
	}

	@Test
	public void cdataStillWorks() {
		// CDATA should still work normally with {| |}
		scanner = TemplateScanner.createScanner("{|<script>alert('test');</script>|}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.CDATATagOpen, "{|");
		assertOffsetAndToken(2, TokenType.CDATAContent, "<script>alert('test');</script>");
		assertOffsetAndToken(33, TokenType.CDATATagClose, "|}");
		assertOffsetAndToken(35, TokenType.EOS, "");
	}

	@Test
	public void sectionTagStillWorks() {
		// Section tags should still work normally with {#tag}
		scanner = TemplateScanner.createScanner("{#if item.active}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(2, TokenType.StartTag, "if");
		assertOffsetAndToken(4, TokenType.Whitespace, " ");
		assertOffsetAndToken(5, TokenType.ParameterTag, "item.active");
		assertOffsetAndToken(16, TokenType.StartTagClose, "}");
		assertOffsetAndToken(17, TokenType.EOS, "");
	}

	@Test
	public void endTagStillWorks() {
		// End tags should still work normally with {/tag}
		scanner = TemplateScanner.createScanner("{/if}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.EndTagOpen, "{/");
		assertOffsetAndToken(2, TokenType.EndTag, "if");
		assertOffsetAndToken(4, TokenType.EndTagClose, "}");
		assertOffsetAndToken(5, TokenType.EOS, "");
	}
	
	@Test
	public void expressionWithNestedBraces() {
		scanner = TemplateScanner.createScanner("{=items.size}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(12, TokenType.EndExpression, "}");
		assertOffsetAndToken(13, TokenType.EOS, "");
	}

	@Test
	public void emptyExpression() {
		scanner = TemplateScanner.createScanner("{=}", EXPRESSION_COMMAND, Collections.emptyList());
		assertOffsetAndToken(0, TokenType.StartExpression, "{=");
		assertOffsetAndToken(2, TokenType.EndExpression, "}");
		assertOffsetAndToken(3, TokenType.EOS, "");
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
