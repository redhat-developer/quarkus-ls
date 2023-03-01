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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.template.sections.ForSection;

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
	public void let4() {
		String content = "{#let name=value}\r\n" + //
				"    {#for item in items}\r\n" + //
				"{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		// {#let name=value}
		Node letSection = template.getChild(0);
		assertEquals(NodeKind.Section, letSection.getKind());
		Section section = (Section) letSection;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertFalse(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(1, section.getStartTagNameOpenOffset()); // {|#let
		assertEquals(5, section.getStartTagNameCloseOffset()); // {#let| name=value}
		assertEquals(16, section.getStartTagCloseOffset()); // {#let name=value|}
		assertFalse(section.hasEndTag());

		assertEquals(2, letSection.getChildCount());

		// {#for item in items}
		// {/}
		Node forSection = letSection.getChild(1);
		assertEquals(NodeKind.Section, forSection.getKind());
		section = (Section) forSection;
		assertEquals(SectionKind.FOR, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(23, section.getStartTagOpenOffset()); // |{#for
		assertEquals(24, section.getStartTagNameOpenOffset()); // {|#for
		assertEquals(28, section.getStartTagNameCloseOffset()); // {#for| item in items}
		assertEquals(42, section.getStartTagCloseOffset()); // {#for item in items|}

		assertTrue(section.hasEndTag());
		assertTrue(section.hasEmptyEndTag());
		assertEquals(45, section.getEndTagOpenOffset()); // |{/}
		assertEquals(47, section.getEndTagCloseOffset()); // {/|}
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

	@Test
	public void infixNotation() {
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
	public void for1() {
		String content = "{#for item in items}" + //
				"{/for}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.FOR, section.getSectionKind());
		assertTrue(section.isClosed());
		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(19, section.getStartTagCloseOffset()); // {#let name=value|}
		assertEquals(20, section.getEndTagOpenOffset());
		assertEquals(25, section.getEndTagCloseOffset());

		ForSection forSection = (ForSection) section;
		assertEquals(3, forSection.getParameters().size());
		Parameter parameter = forSection.getParameters().get(0);
		assertEquals(6, parameter.getStart());
		assertEquals(10, parameter.getEnd());
		assertEquals("item", parameter.getName());
		parameter = forSection.getParameters().get(1);
		assertEquals(11, parameter.getStart());
		assertEquals(13, parameter.getEnd());
		assertEquals("in", parameter.getName());
		parameter = forSection.getParameters().get(2);
		assertEquals(14, parameter.getStart());
		assertEquals(19, parameter.getEnd());
		assertEquals("items", parameter.getName());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(14, expression.getStart());
		assertEquals(19, expression.getEnd());
	}

}
