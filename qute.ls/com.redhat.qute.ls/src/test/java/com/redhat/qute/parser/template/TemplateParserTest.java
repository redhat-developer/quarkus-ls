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
package com.redhat.qute.parser.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test with template parser which builds a Template AST.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateParserTest {

	@Test
	public void let() {
		String content = "{#let name=value}\r\n" + //
				"    \r\n" + //
				"{/let}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(16, section.getStartTagCloseOffset()); // {#let name=value|}
		assertEquals(25, section.getEndTagOpenOffset());
		assertEquals(30, section.getEndTagCloseOffset());

	}

	@Test
	public void let2() {
		String content = "{#let name=value}\r\n" + //
				"    \r\n" + //
				"{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(1, section.getStartTagNameOpenOffset()); // {|#let
		assertEquals(5, section.getStartTagNameCloseOffset()); // {#let| name=value}
		assertEquals(16, section.getStartTagCloseOffset()); // {#let name=value|}
		assertEquals(25, section.getEndTagOpenOffset()); // |{/}
		assertEquals(27, section.getEndTagCloseOffset()); // {/|
	}

	@Test
	public void let3() {
		String content = "{#let name=value}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		// {#let name=value}{/}
		Node letSection = template.getChild(0);
		assertEquals(NodeKind.Section, letSection.getKind());
		Section section = (Section) letSection;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(1, section.getStartTagNameOpenOffset()); // {|#let
		assertEquals(5, section.getStartTagNameCloseOffset()); // {#let| name=value}
		assertEquals(16, section.getStartTagCloseOffset()); // {#let name=value|}
		assertEquals(17, section.getEndTagOpenOffset()); // |{/}
		assertEquals(19, section.getEndTagCloseOffset()); // {/|}
	}

	@Test
	public void parameterNotClosed() {
		String content = "{@\r\n" + //
				"{#for todo in todos}\r\n" + //
				"{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(2, template.getChildCount());

		// {@
		Node parameterDeclaration = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, parameterDeclaration.getKind());
		assertFalse(parameterDeclaration.isClosed());

		// {#for todo in todos}
		// {/}
		Node forSection = template.getChild(1);
		assertEquals(NodeKind.Section, forSection.getKind());
		Section section = (Section) forSection;
		assertEquals(SectionKind.FOR, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(4, section.getStartTagOpenOffset()); // |{#for
		assertEquals(5, section.getStartTagNameOpenOffset()); // {|#for
		assertEquals(9, section.getStartTagNameCloseOffset()); // {#for| todo in todos}
		assertEquals(23, section.getStartTagCloseOffset()); // {#for| todo in todos|}
		assertEquals(26, section.getEndTagOpenOffset()); // |{/}
		assertEquals(28, section.getEndTagCloseOffset()); // {/|}
	}
}
