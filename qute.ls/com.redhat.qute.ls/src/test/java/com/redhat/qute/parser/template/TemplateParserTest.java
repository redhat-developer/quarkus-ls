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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
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

		// Check parameter: name=value
		Parameter parameter = section.getParameters().get(0);
		assertEquals("name", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(16, parameter.getEnd());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(11, expression.getStart());
		assertEquals(16, expression.getEnd());
		assertEquals("value", expression.getContent());
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

		// Check parameter: name=value
		Parameter parameter = section.getParameters().get(0);
		assertEquals("name", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(16, parameter.getEnd());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(11, expression.getStart());
		assertEquals(16, expression.getEnd());
		assertEquals("value", expression.getContent());
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

		// Check parameter: name=value
		Parameter parameter = section.getParameters().get(0);
		assertEquals("name", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(16, parameter.getEnd());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(11, expression.getStart());
		assertEquals(16, expression.getEnd());
		assertEquals("value", expression.getContent());
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

		// Check parameter: name=value
		Parameter parameter = section.getParameters().get(0);
		assertEquals("name", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(16, parameter.getEnd());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(11, expression.getStart());
		assertEquals(16, expression.getEnd());
		assertEquals("value", expression.getContent());

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
	public void letWithTernaryExpression() {
		// {#let param1=(p1 ? p2 : p3)}{/}
		String content = "{#let param1=(p1 ? p2 : p3)}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(27, section.getStartTagCloseOffset()); // {#let param1=(p1 ? p2 : p3)|}
		assertEquals(28, section.getEndTagOpenOffset()); // |{/}
		assertEquals(30, section.getEndTagCloseOffset()); // {/|}

		// Check parameter name
		Parameter parameter = section.getParameters().get(0);
		assertEquals("param1", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(27, parameter.getEnd());

		// Check value expression: (p1 ? p2 : p3) starts at 13, ends at 27
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(14, expression.getStart());
		assertEquals(26, expression.getEnd());
		assertEquals("p1 ? p2 : p3", expression.getContent());
	}

	@Test
	public void letWithTernaryExpressionWithSpaces() {
		// {#let param1 = (p1 ? p2 : p3)}{/}
		String content = "{#let param1 = (p1 ? p2 : p3)}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(29, section.getStartTagCloseOffset()); // {#let param1 = (p1 ? p2 : p3)|}
		assertEquals(30, section.getEndTagOpenOffset()); // |{/}
		assertEquals(32, section.getEndTagCloseOffset()); // {/|}

		// Check parameter name
		Parameter parameter = section.getParameters().get(0);
		assertEquals("param1", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(29, parameter.getEnd());

		// Check value expression: (p1 ? p2 : p3) starts at 15, ends at 29
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(16, expression.getStart());
		assertEquals(28, expression.getEnd());
		assertEquals("p1 ? p2 : p3", expression.getContent());
	}

	@Test
	public void letWithMultipleTernaryExpressions() {
		// {#let p1=(a ? b : c) p2=(d ? e : f)}{/}
		String content = "{#let p1=(a ? b : c) p2=(d ? e : f)}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(35, section.getStartTagCloseOffset()); // {#let p1=(a ? b : c) p2=(d ? e : f)|}
		assertEquals(36, section.getEndTagOpenOffset()); // |{/}
		assertEquals(38, section.getEndTagCloseOffset()); // {/|}

		// Check first parameter: name at 6-8, value (a ? b : c) at 9-20
		Parameter p1 = section.getParameters().get(0);
		assertEquals("p1", p1.getName());
		assertEquals(6, p1.getStart());
		assertEquals(20, p1.getEnd());
		assertNotNull(p1.getJavaTypeExpression());
		Expression expr1 = p1.getJavaTypeExpression();
		assertEquals(10, expr1.getStart());
		assertEquals(19, expr1.getEnd());
		assertEquals("a ? b : c", expr1.getContent());

		// Check second parameter: name at 21-23, value (d ? e : f) at 24-35
		Parameter p2 = section.getParameters().get(1);
		assertEquals("p2", p2.getName());
		assertEquals(21, p2.getStart());
		assertEquals(35, p2.getEnd());
		assertNotNull(p2.getJavaTypeExpression());
		Expression expr2 = p2.getJavaTypeExpression();
		assertEquals(25, expr2.getStart());
		assertEquals(34, expr2.getEnd());
		assertEquals("d ? e : f", expr2.getContent());
	}

	@Test
	public void letWithTernaryExpressionAndOtherParameter() {
		// {#let p1=(a ? b : c) p2=foo}{/}
		String content = "{#let p1=(a ? b : c) p2=foo}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(27, section.getStartTagCloseOffset()); // {#let p1=(a ? b : c) p2=foo|}
		assertEquals(28, section.getEndTagOpenOffset()); // |{/}
		assertEquals(30, section.getEndTagCloseOffset()); // {/|}

		// Check first parameter: name at 6-8, value (a ? b : c) at 9-20
		Parameter p1 = section.getParameters().get(0);
		assertEquals("p1", p1.getName());
		assertEquals(6, p1.getStart());
		assertEquals(20, p1.getEnd());
		assertNotNull(p1.getJavaTypeExpression());
		Expression expr1 = p1.getJavaTypeExpression();
		assertEquals(10, expr1.getStart());
		assertEquals(19, expr1.getEnd());
		assertEquals("a ? b : c", expr1.getContent());

		// Check second parameter: name at 21-23, plain value "foo" at 24-27
		Parameter p2 = section.getParameters().get(1);
		assertEquals("p2", p2.getName());
		assertEquals(21, p2.getStart());
		assertEquals(27, p2.getEnd());
		assertNotNull(p2.getJavaTypeExpression());
		Expression expr2 = p2.getJavaTypeExpression();
		assertEquals(24, expr2.getStart());
		assertEquals(27, expr2.getEnd());
		assertEquals("foo", expr2.getContent());
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
	public void letWithTernaryExpressionAndMethodCall() {
		// {#let p1=(a ? s.substring(b,c) : d)}{/}
		String content = "{#let p1=(a ? s.substring(b,c) : d)}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(35, section.getStartTagCloseOffset()); // {#let p1=(a ? s.substring(b,c) : d)|}
		assertEquals(36, section.getEndTagOpenOffset()); // |{/}
		assertEquals(38, section.getEndTagCloseOffset()); // {/|}

		// Check parameter: name at 6-8, value (a ? s.substring(b,c) : d) at 9-35
		Parameter p1 = section.getParameters().get(0);
		assertEquals("p1", p1.getName());
		assertEquals(6, p1.getStart());
		assertEquals(35, p1.getEnd());

		// Check value expression: outer parens stripped -> "a ? s.substring(b,c) : d"
		Expression expr1 = p1.getJavaTypeExpression();
		assertNotNull(expr1);
		assertEquals(10, expr1.getStart()); // after '('
		assertEquals(34, expr1.getEnd()); // before ')'
		assertEquals("a ? s.substring(b,c) : d", expr1.getContent());

		// Test with expression content: (a ? s.substring(b,c) : d)
		List<Node> exprContent = expr1.getExpressionContent();
		assertEquals(1, exprContent.size());
		Node firstParts = exprContent.get(0);
		assertEquals(NodeKind.ExpressionParts, firstParts.getKind());
		Parts parts = (Parts) firstParts;

		assertEquals(3, parts.getChildCount()); // a --> ObjectPart, ? --> InfixNotationMethodPart, : -->
												// InfixNotationMethodPart

		// ObjectPart --> a
		Node firstPart = parts.getChild(0);
		assertEquals(NodeKind.ExpressionPart, firstPart.getKind());
		Part objectPart = (Part) firstPart;
		assertEquals(PartKind.Object, objectPart.getPartKind());
		assertEquals(10, objectPart.getStart()); // 'a'
		assertEquals(11, objectPart.getEnd()); // after 'a'
		assertEquals("a", objectPart.getPartName());

		// InfixNotationMethodPart --> '?' with parameter s.substring(b,c)
		Node secondPart = parts.getChild(1);
		assertEquals(NodeKind.ExpressionPart, secondPart.getKind());
		Part infixTruePart = (Part) secondPart;
		assertEquals(PartKind.Method, infixTruePart.getPartKind());
		MethodPart infixTrueMethodPart = (MethodPart) infixTruePart;
		assertTrue(infixTrueMethodPart.isInfixNotation());
		assertEquals(12, infixTruePart.getStart()); // '?'
		assertEquals(30, infixTruePart.getEnd()); // after 's.substring(b,c)'
		assertEquals("?", infixTruePart.getPartName());

		// Parameter of '?' --> s.substring(b,c)
		List<Parameter> trueParams = infixTrueMethodPart.getParameters();
		assertEquals(1, trueParams.size());
		Parameter trueParam = trueParams.get(0);
		assertEquals(14, trueParam.getStart()); // 's' of s.substring
		assertEquals(30, trueParam.getEnd()); // after ')'

		// Check expression of parameter: s.substring(b,c)
		Expression trueParamExpr = trueParam.getJavaTypeExpression();
		assertNotNull(trueParamExpr);
		List<Node> trueParamContent = trueParamExpr.getExpressionContent();
		assertEquals(1, trueParamContent.size());
		Parts trueParamParts = (Parts) trueParamContent.get(0);
		assertEquals(2, trueParamParts.getChildCount());

		// ObjectPart --> s
		Part sPart = (Part) trueParamParts.getChild(0);
		assertEquals(PartKind.Object, sPart.getPartKind());
		assertEquals(14, sPart.getStart()); // 's'
		assertEquals(15, sPart.getEnd()); // after 's'
		assertEquals("s", sPart.getPartName());

		// MethodPart --> substring(b,c)
		Part substringPart = (Part) trueParamParts.getChild(1);
		assertEquals(PartKind.Method, substringPart.getPartKind());
		MethodPart substringMethodPart = (MethodPart) substringPart;
		assertEquals(16, substringPart.getStart()); // 's' of substring
		assertEquals(29, substringPart.getEnd()); // after ')'
		assertEquals(25, substringMethodPart.getOpenBracketOffset()); // '('
		assertEquals(29, substringMethodPart.getCloseBracketOffset()); // ')'
		assertEquals("substring", substringPart.getPartName());

		// InfixNotationMethodPart --> ':' with parameter d
		Node thirdPart = parts.getChild(2);
		assertEquals(NodeKind.ExpressionPart, thirdPart.getKind());
		Part infixFalsePart = (Part) thirdPart;
		assertEquals(PartKind.Method, infixFalsePart.getPartKind());
		MethodPart infixFalseMethodPart = (MethodPart) infixFalsePart;
		assertTrue(infixFalseMethodPart.isInfixNotation());
		assertEquals(31, infixFalsePart.getStart()); // ':'
		assertEquals(34, infixFalsePart.getEnd()); // after 'd'
		assertEquals(":", infixFalsePart.getPartName());

		// Parameter of ':' --> d
		List<Parameter> falseParams = infixFalseMethodPart.getParameters();
		assertEquals(1, falseParams.size());
		Parameter falseParam = falseParams.get(0);
		assertEquals(33, falseParam.getStart()); // 'd'
		assertEquals(34, falseParam.getEnd()); // after 'd'

		// Check expression of parameter: d
		Expression falseParamExpr = falseParam.getJavaTypeExpression();
		assertNotNull(falseParamExpr);
		List<Node> falseParamContent = falseParamExpr.getExpressionContent();
		assertEquals(1, falseParamContent.size());
		Parts falseParamParts = (Parts) falseParamContent.get(0);
		assertEquals(1, falseParamParts.getChildCount());

		// ObjectPart --> d
		Part dPart = (Part) falseParamParts.getChild(0);
		assertEquals(PartKind.Object, dPart.getPartKind());
		assertEquals(33, dPart.getStart()); // 'd'
		assertEquals(34, dPart.getEnd()); // after 'd'
		assertEquals("d", dPart.getPartName());
	}

	@Test
	public void letWithTernaryExpressionAndMethodCallWithSpaces() {
		// {#let p1=(a ? s.substring( b, c ) : d)}{/}
		String content = "{#let p1=(a ? s.substring(   b,       c   ) : d)}{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());

		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
		Section section = (Section) first;
		assertEquals(SectionKind.LET, section.getSectionKind());
		assertTrue(section.isClosed());

		assertEquals(0, section.getStartTagOpenOffset()); // |{#let
		assertEquals(48, section.getStartTagCloseOffset()); // {#let p1=(a ? s.substring( b, c ) : d)|}
		assertEquals(49, section.getEndTagOpenOffset()); // |{/}
		assertEquals(51, section.getEndTagCloseOffset()); // {/|}

		// Check parameter: name at 6-8, value (a ? s.substring( b, c ) : d) at 9-48
		Parameter p1 = section.getParameters().get(0);
		assertEquals("p1", p1.getName());
		assertEquals(6, p1.getStart());
		assertEquals(48, p1.getEnd());

		// Check value expression: outer parens stripped -> "a ? s.substring( b, c ) :
		// d"
		Expression expr1 = p1.getJavaTypeExpression();
		assertNotNull(expr1);
		assertEquals(10, expr1.getStart()); // after '('
		assertEquals(47, expr1.getEnd()); // before ')'
		assertEquals("a ? s.substring(   b,       c   ) : d", expr1.getContent());

		// Test with expression content
		List<Node> exprContent = expr1.getExpressionContent();
		assertEquals(1, exprContent.size());
		Node firstParts = exprContent.get(0);
		assertEquals(NodeKind.ExpressionParts, firstParts.getKind());
		Parts parts = (Parts) firstParts;

		assertEquals(3, parts.getChildCount()); // a --> ObjectPart, ? --> InfixNotationMethodPart, : -->
												// InfixNotationMethodPart

		// ObjectPart --> a
		Node firstPart = parts.getChild(0);
		assertEquals(NodeKind.ExpressionPart, firstPart.getKind());
		Part objectPart = (Part) firstPart;
		assertEquals(PartKind.Object, objectPart.getPartKind());
		assertEquals(10, objectPart.getStart()); // 'a'
		assertEquals(11, objectPart.getEnd()); // after 'a'
		assertEquals("a", objectPart.getPartName());

		// InfixNotationMethodPart --> '?' with parameter s.substring( b, c )
		Node secondPart = parts.getChild(1);
		assertEquals(NodeKind.ExpressionPart, secondPart.getKind());
		Part infixTruePart = (Part) secondPart;
		assertEquals(PartKind.Method, infixTruePart.getPartKind());
		MethodPart infixTrueMethodPart = (MethodPart) infixTruePart;
		assertTrue(infixTrueMethodPart.isInfixNotation());
		assertEquals(12, infixTruePart.getStart()); // '?'
		assertEquals(43, infixTruePart.getEnd()); // after 's.substring( b, c )'
		assertEquals("?", infixTruePart.getPartName());

		// Parameter of '?' --> s.substring( b, c )
		List<Parameter> trueParams = infixTrueMethodPart.getParameters();
		assertEquals(1, trueParams.size());
		Parameter trueParam = trueParams.get(0);
		assertEquals(14, trueParam.getStart()); // 's' of s.substring
		assertEquals(43, trueParam.getEnd()); // after ')'

		// Check expression of parameter: s.substring( b, c )
		Expression trueParamExpr = trueParam.getJavaTypeExpression();
		assertNotNull(trueParamExpr);
		List<Node> trueParamContent = trueParamExpr.getExpressionContent();
		assertEquals(1, trueParamContent.size());
		Parts trueParamParts = (Parts) trueParamContent.get(0);
		assertEquals(2, trueParamParts.getChildCount());

		// ObjectPart --> s
		Part sPart = (Part) trueParamParts.getChild(0);
		assertEquals(PartKind.Object, sPart.getPartKind());
		assertEquals(14, sPart.getStart()); // 's'
		assertEquals(15, sPart.getEnd()); // after 's'
		assertEquals("s", sPart.getPartName());

		// MethodPart --> substring( b, c )
		Part substringPart = (Part) trueParamParts.getChild(1);
		assertEquals(PartKind.Method, substringPart.getPartKind());
		MethodPart substringMethodPart = (MethodPart) substringPart;
		assertEquals(16, substringPart.getStart()); // 's' of substring
		assertEquals(42, substringPart.getEnd()); // after ')'
		assertEquals(25, substringMethodPart.getOpenBracketOffset()); // '('
		assertEquals(42, substringMethodPart.getCloseBracketOffset()); // ')'
		assertEquals("substring", substringPart.getPartName());

		// InfixNotationMethodPart --> ':' with parameter d
		Node thirdPart = parts.getChild(2);
		assertEquals(NodeKind.ExpressionPart, thirdPart.getKind());
		Part infixFalsePart = (Part) thirdPart;
		assertEquals(PartKind.Method, infixFalsePart.getPartKind());
		MethodPart infixFalseMethodPart = (MethodPart) infixFalsePart;
		assertTrue(infixFalseMethodPart.isInfixNotation());
		assertEquals(44, infixFalsePart.getStart()); // ':'
		assertEquals(47, infixFalsePart.getEnd()); // after 'd'
		assertEquals(":", infixFalsePart.getPartName());

		// Parameter of ':' --> d
		List<Parameter> falseParams = infixFalseMethodPart.getParameters();
		assertEquals(1, falseParams.size());
		Parameter falseParam = falseParams.get(0);
		assertEquals(46, falseParam.getStart()); // 'd'
		assertEquals(47, falseParam.getEnd()); // after 'd'

		// Check expression of parameter: d
		Expression falseParamExpr = falseParam.getJavaTypeExpression();
		assertNotNull(falseParamExpr);
		List<Node> falseParamContent = falseParamExpr.getExpressionContent();
		assertEquals(1, falseParamContent.size());
		Parts falseParamParts = (Parts) falseParamContent.get(0);
		assertEquals(1, falseParamParts.getChildCount());

		// ObjectPart --> d
		Part dPart = (Part) falseParamParts.getChild(0);
		assertEquals(PartKind.Object, dPart.getPartKind());
		assertEquals(46, dPart.getStart()); // 'd'
		assertEquals(47, dPart.getEnd()); // after 'd'
		assertEquals("d", dPart.getPartName());
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

		// Check parameter: name=value
		Parameter parameter = section.getParameters().get(0);
		assertEquals("name", parameter.getName());
		assertEquals(6, parameter.getStart());
		assertEquals(16, parameter.getEnd());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(11, expression.getStart());
		assertEquals(16, expression.getEnd());
		assertEquals("value", expression.getContent());
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
		assertEquals(0, section.getStartTagOpenOffset()); // |{#for
		assertEquals(19, section.getStartTagCloseOffset()); // {#for item in items|}
		assertEquals(20, section.getEndTagOpenOffset());
		assertEquals(25, section.getEndTagCloseOffset());

		ForSection forSection = (ForSection) section;
		assertEquals(3, forSection.getParameters().size());

		// Check "item" parameter
		Parameter parameter = forSection.getParameters().get(0);
		assertEquals(6, parameter.getStart());
		assertEquals(10, parameter.getEnd());
		assertEquals("item", parameter.getName());

		// Check "in" parameter
		parameter = forSection.getParameters().get(1);
		assertEquals(11, parameter.getStart());
		assertEquals(13, parameter.getEnd());
		assertEquals("in", parameter.getName());

		// Check "items" parameter and its expression
		parameter = forSection.getParameters().get(2);
		assertEquals(14, parameter.getStart());
		assertEquals(19, parameter.getEnd());
		assertEquals("items", parameter.getName());
		assertNotNull(parameter.getJavaTypeExpression());
		Expression expression = parameter.getJavaTypeExpression();
		assertEquals(14, expression.getStart());
		assertEquals(19, expression.getEnd());
		assertEquals("items", expression.getContent());
	}

	@Test
	public void emptyEndSection() {
		String content = "{/}";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.Section, first.getKind());
	}
}