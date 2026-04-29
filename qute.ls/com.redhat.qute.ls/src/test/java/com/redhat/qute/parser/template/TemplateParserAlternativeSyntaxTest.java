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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;

/**
 * Test with template parser which builds a Template AST with alternative syntax
 * expression command (e.g., {=foo} instead of {foo}).
 *
 * This syntax is activated via quarkus.qute.alt-...=true configuration.
 *
 * @author Angelo ZERR
 *
 */
public class TemplateParserAlternativeSyntaxTest {

	private static final Character EXPRESSION_COMMAND = '=';

	@Test
	public void simpleExpression() {
		String content = "{=name}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Expression, first.getKind());
		Expression expression = (Expression) first;

		assertEquals(0, expression.getStart());
		assertEquals(7, expression.getEnd());
		assertEquals("name", expression.getContent());
	}

	@Test
	public void expressionWithProperty() {
		String content = "{=item.name}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Expression, first.getKind());
		Expression expression = (Expression) first;

		assertEquals(0, expression.getStart());
		assertEquals(12, expression.getEnd());
		assertEquals("item.name", expression.getContent());

		// Check expression parts
		List<Node> exprContent = expression.getExpressionContent();
		assertEquals(1, exprContent.size());
		Parts parts = (Parts) exprContent.get(0);
		assertEquals(2, parts.getChildCount());

		// ObjectPart --> item
		Part itemPart = (Part) parts.getChild(0);
		assertEquals(PartKind.Object, itemPart.getPartKind());
		assertEquals("item", itemPart.getPartName());

		// PropertyPart --> name
		Part namePart = (Part) parts.getChild(1);
		assertEquals(PartKind.Property, namePart.getPartKind());
		assertEquals("name", namePart.getPartName());
	}

	@Test
	public void expressionWithMethodCall() {
		String content = "{=item.getPrice()}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Expression, first.getKind());
		Expression expression = (Expression) first;

		assertEquals(0, expression.getStart());
		assertEquals(18, expression.getEnd());
		assertEquals("item.getPrice()", expression.getContent());

		// Check expression parts
		List<Node> exprContent = expression.getExpressionContent();
		assertEquals(1, exprContent.size());
		Parts parts = (Parts) exprContent.get(0);
		assertEquals(2, parts.getChildCount());

		// ObjectPart --> item
		Part itemPart = (Part) parts.getChild(0);
		assertEquals(PartKind.Object, itemPart.getPartKind());
		assertEquals("item", itemPart.getPartName());

		// MethodPart --> getPrice()
		Part methodPart = (Part) parts.getChild(1);
		assertEquals(PartKind.Method, methodPart.getPartKind());
		assertEquals("getPrice", methodPart.getPartName());
	}

	@Test
	public void mixedTextAndExpression() {
		String content = "Hello {=name}!";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(3, template.getChildCount());

		// Text node: "Hello "
		Node text1 = template.getChild(0);
		assertEquals(NodeKind.Text, text1.getKind());

		// Expression: {=name}
		Node expr = template.getChild(1);
		assertEquals(NodeKind.Expression, expr.getKind());
		Expression expression = (Expression) expr;
		assertEquals(6, expression.getStart());
		assertEquals(13, expression.getEnd());
		assertEquals("name", expression.getContent());

		// Text node: "!"
		Node text2 = template.getChild(2);
		assertEquals(NodeKind.Text, text2.getKind());
	}

	@Test
	public void expressionInSection() {
		String content = "{#if item.active}\n" +
				"  {=item.name}\n" +
				"{/if}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		// {#if item.active}
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.IF, section.getSectionKind());
		assertTrue(section.isClosed());

		// Check section has 3 children: text, expression, text
		assertEquals(3, section.getChildCount());

		// Text node: "\n  "
		Node text1 = section.getChild(0);
		assertEquals(NodeKind.Text, text1.getKind());

		// Expression: {=item.name}
		Node expr = section.getChild(1);
		assertEquals(NodeKind.Expression, expr.getKind());
		Expression expression = (Expression) expr;
		assertEquals("item.name", expression.getContent());

		// Text node: "\n"
		Node text2 = section.getChild(2);
		assertEquals(NodeKind.Text, text2.getKind());
	}

	@Test
	public void letSectionWithAlternativeExpression() {
		String content = "{#let greeting='Hello'}\n" +
				"  {=greeting} {=name}\n" +
				"{/let}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		// {#let greeting='Hello'}
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		// Check parameter
		Parameter parameter = section.getParameters().get(0);
		assertEquals("greeting", parameter.getName());

		// Check section children
		assertEquals(5, section.getChildCount());

		// Text: "\n  "
		assertEquals(NodeKind.Text, section.getChild(0).getKind());

		// Expression: {=greeting}
		Node expr1 = section.getChild(1);
		assertEquals(NodeKind.Expression, expr1.getKind());
		assertEquals("greeting", ((Expression) expr1).getContent());

		// Text: " "
		assertEquals(NodeKind.Text, section.getChild(2).getKind());

		// Expression: {=name}
		Node expr2 = section.getChild(3);
		assertEquals(NodeKind.Expression, expr2.getKind());
		assertEquals("name", ((Expression) expr2).getContent());

		// Text: "\n"
		assertEquals(NodeKind.Text, section.getChild(4).getKind());
	}

	@Test
	public void forSectionWithAlternativeExpression() {
		String content = "{#for item in items}\n" +
				"  {=item.name}: {=item.price}\n" +
				"{/for}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		// {#for item in items}
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.FOR, section.getSectionKind());
		assertTrue(section.isClosed());

		// Check section children: text, expr, text, expr, text
		assertEquals(5, section.getChildCount());

		// Expression: {=item.name}
		Node expr1 = section.getChild(1);
		assertEquals(NodeKind.Expression, expr1.getKind());
		assertEquals("item.name", ((Expression) expr1).getContent());

		// Expression: {=item.price}
		Node expr2 = section.getChild(3);
		assertEquals(NodeKind.Expression, expr2.getKind());
		assertEquals("item.price", ((Expression) expr2).getContent());
	}

	@Test
	public void noExpressionWithoutCommand() {
		// Without '=' command, {name} should be treated as text
		String content = "{name}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Text, first.getKind());
	}

	@Test
	public void complexTemplate() {
		String content = "{@java.util.List items}\n" +
				"{#for item in items}\n" +
				"  {! Item details !}\n" +
				"  Name: {=item.name}\n" +
				"  Price: {=item.price}\n" +
				"{/for}";

		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(3, template.getChildCount());

		// {@java.util.List items}
		Node paramDecl = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, paramDecl.getKind());

		// {#for item in items}
		Node forSection = template.getChild(2);
		assertEquals(NodeKind.Section, forSection.getKind());
		Section section = (Section) forSection;
		assertEquals(SectionKind.FOR, section.getSectionKind());
	}

	@Test
	public void expressionWithMethodChain() {
		String content = "{=item.getName().toUpperCase()}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Expression, first.getKind());
		Expression expression = (Expression) first;

		assertEquals("item.getName().toUpperCase()", expression.getContent());

		// Check expression parts
		List<Node> exprContent = expression.getExpressionContent();
		assertEquals(1, exprContent.size());
		Parts parts = (Parts) exprContent.get(0);
		assertEquals(3, parts.getChildCount());

		// ObjectPart --> item
		Part itemPart = (Part) parts.getChild(0);
		assertEquals(PartKind.Object, itemPart.getPartKind());
		assertEquals("item", itemPart.getPartName());

		// MethodPart --> getName()
		Part getNamePart = (Part) parts.getChild(1);
		assertEquals(PartKind.Method, getNamePart.getPartKind());
		assertEquals("getName", getNamePart.getPartName());

		// MethodPart --> toUpperCase()
		Part toUpperCasePart = (Part) parts.getChild(2);
		assertEquals(PartKind.Method, toUpperCasePart.getPartKind());
		assertEquals("toUpperCase", toUpperCasePart.getPartName());
	}

	@Test
	public void expressionWithInfixNotation() {
		String content = "{#let result=(a ?: b)}{=result}{/let}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());

		// Check parameter with infix notation
		Parameter parameter = section.getParameters().get(0);
		assertEquals("result", parameter.getName());
		assertNotNull(parameter.getJavaTypeExpression());

		Expression paramExpr = parameter.getJavaTypeExpression();
		assertEquals("a ?: b", paramExpr.getContent());
		assertTrue(paramExpr.canSupportInfixNotation());

		// Check expression in section body
		assertEquals(1, section.getChildCount());
		Node expr = section.getChild(0);
		assertEquals(NodeKind.Expression, expr.getKind());
		assertEquals("result", ((Expression) expr).getContent());
	}

	@Test
	public void multipleExpressionsWithDifferentPatterns() {
		String content = "{=name} and {=item.property} and {=obj.method()}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(5, template.getChildCount());

		// Expression: {=name}
		Node expr1 = template.getChild(0);
		assertEquals(NodeKind.Expression, expr1.getKind());
		assertEquals("name", ((Expression) expr1).getContent());

		// Text: " and "
		assertEquals(NodeKind.Text, template.getChild(1).getKind());

		// Expression: {=item.property}
		Node expr2 = template.getChild(2);
		assertEquals(NodeKind.Expression, expr2.getKind());
		assertEquals("item.property", ((Expression) expr2).getContent());

		// Text: " and "
		assertEquals(NodeKind.Text, template.getChild(3).getKind());

		// Expression: {=obj.method()}
		Node expr3 = template.getChild(4);
		assertEquals(NodeKind.Expression, expr3.getKind());
		assertEquals("obj.method()", ((Expression) expr3).getContent());
	}

	@Test
	public void expressionWithStringParameter() {
		String content = "{=item.format('Hello')}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Expression, first.getKind());
		Expression expression = (Expression) first;

		assertEquals("item.format('Hello')", expression.getContent());

		// Check expression parts
		List<Node> exprContent = expression.getExpressionContent();
		assertEquals(1, exprContent.size());
		Parts parts = (Parts) exprContent.get(0);
		assertEquals(2, parts.getChildCount());

		// MethodPart --> format('Hello')
		Part methodPart = (Part) parts.getChild(1);
		assertEquals(PartKind.Method, methodPart.getPartKind());
		assertEquals("format", methodPart.getPartName());

		MethodPart method = (MethodPart) methodPart;
		List<Parameter> params = method.getParameters();
		assertEquals(1, params.size());

		Parameter param = params.get(0);
		Expression paramExpr = param.getJavaTypeExpression();
		assertNotNull(paramExpr);
		assertEquals("'Hello'", paramExpr.getContent());
	}

	@Test
	public void nestedSectionsWithAlternativeExpressions() {
		String content = "{#if active}\n" +
				"  {#for item in items}\n" +
				"    {=item.name}\n" +
				"  {/for}\n" +
				"{/if}";

		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(1, template.getChildCount());

		// Outer {#if active}
		Node ifSection = template.getChild(0);
		assertEquals(NodeKind.Section, ifSection.getKind());
		Section ifSec = (Section) ifSection;
		assertEquals(SectionKind.IF, ifSec.getSectionKind());

		// Should have text, for section, text
		assertEquals(3, ifSec.getChildCount());

		// Inner {#for item in items}
		Node forSection = ifSec.getChild(1);
		assertEquals(NodeKind.Section, forSection.getKind());
		Section forSec = (Section) forSection;
		assertEquals(SectionKind.FOR, forSec.getSectionKind());

		// For section should have text, expression, text
		assertEquals(3, forSec.getChildCount());

		// Expression: {=item.name}
		Node expr = forSec.getChild(1);
		assertEquals(NodeKind.Expression, expr.getKind());
		assertEquals("item.name", ((Expression) expr).getContent());
	}

	@Test
	public void commentStillWorks() {
		String content = "{! This is a comment !}{=name}";
		Template template = TemplateParser.parse(content, "test.qute", EXPRESSION_COMMAND);
		assertEquals(2, template.getChildCount());

		// Comment
		Node comment = template.getChild(0);
		assertEquals(NodeKind.Comment, comment.getKind());

		// Expression
		Node expr = template.getChild(1);
		assertEquals(NodeKind.Expression, expr.getKind());
		assertEquals("name", ((Expression) expr).getContent());
	}

}
