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

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Tests for template scanner {@link TemplateScanner}.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateScannerTest {

	private Scanner<TokenType, ScannerState> scanner;

	@Test
	public void testExpression() {
		scanner = TemplateScanner.createScanner("{abcd}");
		assertOffsetAndToken(0, TokenType.StartExpression, "{");
		assertOffsetAndToken(5, TokenType.EndExpression, "}");
		assertOffsetAndToken(6, TokenType.EOS, "");
	}

	@Test
	public void testExpressionWithString() {
		scanner = TemplateScanner.createScanner("{abc'}'d}");
		assertOffsetAndToken(0, TokenType.StartExpression, "{");
		assertOffsetAndToken(4, TokenType.StartString, "'");
		assertOffsetAndToken(5, TokenType.String, "}");
		assertOffsetAndToken(6, TokenType.EndString, "'");
		assertOffsetAndToken(8, TokenType.EndExpression, "}");
		assertOffsetAndToken(9, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithSpace() {
		scanner = TemplateScanner.createScanner("{ abcd}");
		assertOffsetAndToken(0, TokenType.Content, "{ abcd}");
		assertOffsetAndToken(7, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithQuote() {
		scanner = TemplateScanner.createScanner("{\"abcd\"}");
		assertOffsetAndToken(0, TokenType.Content, "{\"abcd\"}");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithBracket() {
		scanner = TemplateScanner.createScanner("{{abcd}}");
		assertOffsetAndToken(0, TokenType.Content, "{{abcd}}");
		assertOffsetAndToken(8, TokenType.EOS, "");
	}

	@Test
	public void noExpressionWithEmpty() {
		scanner = TemplateScanner.createScanner("{}");
		assertOffsetAndToken(0, TokenType.Content, "{}");
		assertOffsetAndToken(2, TokenType.EOS, "");
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#identifiers
	 */
	@Test
	public void identifiersAndTags() {
		scanner = TemplateScanner.createScanner("<html>\r\n" + //
				"<body>\r\n" + // text
				"   {_foo.bar}   \r\n" + // expression
				"   {! comment !}\r\n" + // comment
				"   {  foo}      \r\n" + // text
				"   {{foo}}      \r\n" + // text
				"   {\"foo\":true} \r\n" + // text
				"</body>\r\n" + // text
				"</html>"); // text
		assertOffsetAndToken(0, TokenType.Content, "<html>\r\n" + //
				"<body>\r\n" + //
				"   ");
		// {_foo.bar}
		assertOffsetAndToken(19, TokenType.StartExpression, "{");
		assertOffsetAndToken(28, TokenType.EndExpression, "}");
		assertOffsetAndToken(29, TokenType.Content, "   \r\n" + //
				"   ");

		// {! comment !}
		assertOffsetAndToken(37, TokenType.StartComment, "{!");
		assertOffsetAndToken(39, TokenType.Comment, " comment ");
		assertOffsetAndToken(48, TokenType.EndComment, "!}");
		assertOffsetAndToken(50, TokenType.Content, "\r\n" + //
				"   ");

		// { foo}
		assertOffsetAndToken(55, TokenType.Content, "{  foo}      \r\n" + //
				"   ");

		// {{foo}}
		assertOffsetAndToken(73, TokenType.Content, "{{foo}}      \r\n" + "   ");
		// {\"foo\":true}
		assertOffsetAndToken(91, TokenType.Content, "{\"foo\":true} \r\n" + // text
				"</body>\r\n" + //
				"</html>");
		assertOffsetAndToken(122, TokenType.EOS, "");
	}

	@Test
	public void withoutUnparsedCData() {
		scanner = TemplateScanner.createScanner("<script>if(true){alert('Qute is cute!')};</script>");
		assertOffsetAndToken(0, TokenType.Content, "<script>if(true)");
		assertOffsetAndToken(16, TokenType.StartExpression, "{");
		assertOffsetAndToken(23, TokenType.StartString, "'");
		assertOffsetAndToken(24, TokenType.String, "Qute is cute!");
		assertOffsetAndToken(37, TokenType.EndString, "'");
		assertOffsetAndToken(39, TokenType.EndExpression, "}");
		assertOffsetAndToken(40, TokenType.Content, ";</script>");
		assertOffsetAndToken(50, TokenType.EOS, "");
	}

	@Test
	public void withUnparsedCData() {
		scanner = TemplateScanner.createScanner("{|<script>if(true){alert('Qute is cute!')};</script>|}");
		assertOffsetAndToken(0, TokenType.CDATATagOpen, "{|");
		assertOffsetAndToken(2, TokenType.CDATAContent, "<script>if(true){alert('Qute is cute!')};</script>");
		assertOffsetAndToken(52, TokenType.CDATATagClose, "|}");
		assertOffsetAndToken(54, TokenType.EOS, "");
	}

	@Test
	public void withUnparsedCDataOldSyntax() {
		scanner = TemplateScanner.createScanner("{[<script>if(true){alert('Qute is cute!')};</script>]}");
		assertOffsetAndToken(0, TokenType.CDATAOldTagOpen, "{[");
		assertOffsetAndToken(2, TokenType.CDATAContent, "<script>if(true){alert('Qute is cute!')};</script>");
		assertOffsetAndToken(52, TokenType.CDATAOldTagClose, "]}");
		assertOffsetAndToken(54, TokenType.EOS, "");
	}

	@Test
	public void includeSection() {
		scanner = TemplateScanner.createScanner("{#include footer /}");
		assertOffsetAndToken(0, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(2, TokenType.StartTag, "include");
		assertOffsetAndToken(9, TokenType.Whitespace, " ");
		assertOffsetAndToken(10, TokenType.ParameterTag, "footer");
		assertOffsetAndToken(16, TokenType.Whitespace, " ");
		assertOffsetAndToken(17, TokenType.StartTagSelfClose, "/}");
		assertOffsetAndToken(19, TokenType.EOS, "");
	}

	@Test
	public void letSection() {
		scanner = TemplateScanner.createScanner("{#let name='value' /}");
		assertOffsetAndToken(0, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(2, TokenType.StartTag, "let");
		assertOffsetAndToken(5, TokenType.Whitespace, " ");
		assertOffsetAndToken(6, TokenType.ParameterTag, "name='value'");
		assertOffsetAndToken(18, TokenType.Whitespace, " ");
		assertOffsetAndToken(19, TokenType.StartTagSelfClose, "/}");
		assertOffsetAndToken(21, TokenType.EOS, "");
	}

	@Test
	public void letSectionWithBracketInString() {
		scanner = TemplateScanner.createScanner("{#let name='v/a}lue' /}");
		assertOffsetAndToken(0, TokenType.StartTagOpen, "{#");
		assertOffsetAndToken(2, TokenType.StartTag, "let");
		assertOffsetAndToken(5, TokenType.Whitespace, " ");
		assertOffsetAndToken(6, TokenType.ParameterTag, "name='v/a}lue'");
		assertOffsetAndToken(20, TokenType.Whitespace, " ");
		assertOffsetAndToken(21, TokenType.StartTagSelfClose, "/}");
		assertOffsetAndToken(23, TokenType.EOS, "");
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
