/*******************************************************************************
 * Copyright (c) 2026 Red Hat Inc. and others.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ParameterDeclaration} parsing.
 *
 * <p>
 * Each test documents the exact character layout of the template string above
 * the offsets it verifies, for example:
 * </p>
 * 
 * <pre>
 *   { @ S t r i n g   s t r }
 *   0 1 2 3 4 5 6 7 8 9 ...
 * </pre>
 *
 * <p>
 * Covered forms:
 * </p>
 * <ul>
 * <li>{@code {@String str}} — simple type + alias</li>
 * <li>{@code {@java.lang.String str}} — qualified type + alias</li>
 * <li>{@code {@java.util.List<String> items}} — single-arg generic</li>
 * <li>{@code {@java.util.Map<String,Integer> map}} — multi-arg generic</li>
 * <li>{@code {@String str = "foo"}} — double-quoted default</li>
 * <li>{@code {@String str = 'foo'}} — single-quoted default</li>
 * <li>{@code {@Integer i = 123}} — numeric default</li>
 * <li>{@code {@String}} — type only, no alias</li>
 * <li>unclosed variants of the above</li>
 * </ul>
 */
public class ParameterDeclarationTest {

	// -------------------------------------------------------------------------
	// Helper
	// -------------------------------------------------------------------------

	/**
	 * Parses {@code content}, asserts exactly one child whose kind is
	 * {@link NodeKind#ParameterDeclaration}, and returns it cast.
	 */
	private static ParameterDeclaration parseFirst(String content) {
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, first.getKind());
		return (ParameterDeclaration) first;
	}

	// -------------------------------------------------------------------------
	// Simple type
	// -------------------------------------------------------------------------

	@Test
	public void simpleTypeWithAlias() {
		// { @ S t r i n g s t r }
		// 0 1 2 3 4 5 6 7 8 9 0 1 2
		// 1 1 1
		String content = "{@String str}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(13, param.getEnd());
		assertEquals(2, param.getStartContent()); // after "{@"
		assertEquals(12, param.getEndContent()); // before "}"

		// Java type: "String" → [2, 8)
		assertEquals("String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(8, param.getClassNameEnd());
		assertTrue(param.isInJavaTypeName(2)); // 'S'
		assertTrue(param.isInJavaTypeName(7)); // 'g'
		assertFalse(param.isInJavaTypeName(9)); // 's' of alias

		// Alias: "str" → [9, 12)
		assertTrue(param.hasAlias());
		assertEquals("str", param.getAlias());
		assertEquals(9, param.getAliasStart());
		assertEquals(12, param.getAliasEnd());
		assertTrue(param.isInAlias(9)); // 's'
		assertTrue(param.isInAlias(11)); // 'r'
		assertFalse(param.isInAlias(8)); // space before alias
		assertFalse(param.isInAlias(13)); // '}'

		// No default value
		assertFalse(param.hasDefaultValue());
		assertNull(param.getDefaultValue());
		assertEquals(-1, param.getDefaultValueStart());
		assertEquals(-1, param.getDefaultValueEnd());
		assertFalse(param.isInDefaultValue(9));

		// Generic ranges: single segment
		List<ParameterDeclaration.JavaTypeRangeOffset> ranges = param.getJavaTypeNameRanges();
		assertEquals(1, ranges.size());
		assertEquals(2, ranges.get(0).getStart());
		assertEquals(8, ranges.get(0).getEnd());
		assertFalse(ranges.get(0).isInGeneric());
	}

	@Test
	public void simpleTypeWithoutAlias() {
		// { @ S t r i n g }
		// 0 1 2 3 4 5 6 7 8
		String content = "{@String}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(9, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(8, param.getEndContent());

		// Java type: "String" → [2, 8)
		assertEquals("String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(8, param.getClassNameEnd());
		assertTrue(param.isInJavaTypeName(2));
		assertTrue(param.isInJavaTypeName(7));
		assertFalse(param.isInJavaTypeName(9));

		// No alias
		assertFalse(param.hasAlias());
		assertNull(param.getAlias());
		assertEquals(-1, param.getAliasStart());
		assertFalse(param.isInAlias(2));

		// No default value
		assertFalse(param.hasDefaultValue());
		assertNull(param.getDefaultValue());
		assertEquals(-1, param.getDefaultValueStart());
		assertEquals(-1, param.getDefaultValueEnd());
	}

	// -------------------------------------------------------------------------
	// Qualified type
	// -------------------------------------------------------------------------

	@Test
	public void qualifiedTypeWithAlias() {
		// { @ j a v a . l a n g . S t r i n g s t r }
		// 0 1 2 3 4 5 6 7 8 9 ... 18 19 ...22
		String content = "{@java.lang.String str}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(23, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(22, param.getEndContent());

		// Java type: "java.lang.String" → [2, 18)
		assertEquals("java.lang.String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(18, param.getClassNameEnd());
		assertTrue(param.isInJavaTypeName(2)); // 'j'
		assertTrue(param.isInJavaTypeName(17)); // 'g'
		assertFalse(param.isInJavaTypeName(19)); // 's' of alias

		// Alias: "str" → [19, 22)
		assertTrue(param.hasAlias());
		assertEquals("str", param.getAlias());
		assertEquals(19, param.getAliasStart());
		assertEquals(22, param.getAliasEnd());
		assertTrue(param.isInAlias(19));
		assertTrue(param.isInAlias(21));

		assertFalse(param.hasDefaultValue());
	}

	@Test
	public void qualifiedTypeWithoutAlias() {
		// { @ j a v a . l a n g . S t r i n g }
		// 0 1 2 ... 18 19
		String content = "{@java.lang.String}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		assertEquals(0, param.getStart());
		assertEquals(19, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(18, param.getEndContent());

		assertEquals("java.lang.String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(18, param.getClassNameEnd());

		assertFalse(param.hasAlias());
		assertNull(param.getAlias());
		assertEquals(-1, param.getAliasStart());

		assertFalse(param.hasDefaultValue());
	}

	// -------------------------------------------------------------------------
	// Generic types
	// -------------------------------------------------------------------------

	@Test
	public void genericTypeWithAlias() {
		// { @ j a v a . u t i l . L i s t < S t r i n g > i t e m s }
		// 0 1 2 ... 15 16 17 18 ... 23 24 25 ... 30
		String content = "{@java.util.List<String> items}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(31, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(30, param.getEndContent());

		// Java type: "java.util.List<String>" → [2, 24)
		assertEquals("java.util.List<String>", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(24, param.getClassNameEnd()); // stops at '<'

		// Generic ranges
		List<ParameterDeclaration.JavaTypeRangeOffset> ranges = param.getJavaTypeNameRanges();
		assertEquals(2, ranges.size());

		// "java.util.List" → [2, 16)
		ParameterDeclaration.JavaTypeRangeOffset main = ranges.get(0);
		assertEquals(2, main.getStart());
		assertEquals(16, main.getEnd());
		assertFalse(main.isInGeneric());
		assertEquals("java.util.List", content.substring(main.getStart(), main.getEnd()));

		// "String" → [17, 23), inside <>
		ParameterDeclaration.JavaTypeRangeOffset generic = ranges.get(1);
		assertEquals(17, generic.getStart());
		assertEquals(23, generic.getEnd());
		assertTrue(generic.isInGeneric());
		assertTrue(generic.isGenericClosed());
		assertEquals("String", content.substring(generic.getStart(), generic.getEnd()));

		// Alias: "items" → [25, 30)
		assertTrue(param.hasAlias());
		assertEquals("items", param.getAlias());
		assertEquals(25, param.getAliasStart());
		assertEquals(30, param.getAliasEnd());
		assertTrue(param.isInAlias(25));
		assertTrue(param.isInAlias(29));
		assertFalse(param.isInAlias(24)); // space before alias

		assertFalse(param.hasDefaultValue());
	}

	@Test
	public void genericTypeUnclosedDiamond() {
		// {@java.util.List<String items} — '>' is missing
		// "String" segment must be inGeneric=true, genericClosed=false
		String content = "{@java.util.List<String items}";
		ParameterDeclaration param = parseFirst(content);

		List<ParameterDeclaration.JavaTypeRangeOffset> ranges = param.getJavaTypeNameRanges();
		assertEquals(2, ranges.size());

		ParameterDeclaration.JavaTypeRangeOffset generic = ranges.get(1);
		assertTrue(generic.isInGeneric());
		assertFalse(generic.isGenericClosed());
	}

	@Test
	public void multiArgGenericTypeWithAlias() {
		// { @ j a v a . u t i l . M a p < S t r i n g , I n t e g e r > m a p }
		// 0 1 2 ... 15 16 17 ... 22 23 24 ... 30 31 32..34 35
		String content = "{@java.util.Map<String,Integer> map}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(36, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(35, param.getEndContent());

		List<ParameterDeclaration.JavaTypeRangeOffset> ranges = param.getJavaTypeNameRanges();
		assertEquals(3, ranges.size());

		// "java.util.Map" → [2, 15)
		assertEquals(2, ranges.get(0).getStart());
		assertEquals(15, ranges.get(0).getEnd());
		assertFalse(ranges.get(0).isInGeneric());
		assertEquals("java.util.Map", content.substring(ranges.get(0).getStart(), ranges.get(0).getEnd()));

		// "String" → [16, 22)
		assertEquals(16, ranges.get(1).getStart());
		assertEquals(22, ranges.get(1).getEnd());
		assertTrue(ranges.get(1).isInGeneric());
		assertEquals("String", content.substring(ranges.get(1).getStart(), ranges.get(1).getEnd()));

		// "Integer" → [23, 30)
		assertEquals(23, ranges.get(2).getStart());
		assertEquals(30, ranges.get(2).getEnd());
		assertTrue(ranges.get(2).isInGeneric());
		assertTrue(ranges.get(2).isGenericClosed());
		assertEquals("Integer", content.substring(ranges.get(2).getStart(), ranges.get(2).getEnd()));

		// Alias: "map" → [32, 35)
		assertTrue(param.hasAlias());
		assertEquals("map", param.getAlias());
		assertEquals(32, param.getAliasStart());
		assertEquals(35, param.getAliasEnd());
		assertTrue(param.isInAlias(32));
		assertTrue(param.isInAlias(34));
		assertFalse(param.isInAlias(31)); // space
		assertFalse(param.isInAlias(36)); // '}'
	}

	// -------------------------------------------------------------------------
	// Default value — double-quoted string
	// -------------------------------------------------------------------------

	@Test
	public void defaultValueDoubleQuote() {
		// { @ S t r i n g s t r = " f o o " }
		// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
		// 1 1 1 1 1 1 1 1 1 1 2
		String content = "{@String str = \"foo\"}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(21, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(20, param.getEndContent());

		// Java type: "String" → [2, 8)
		assertEquals("String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(8, param.getClassNameEnd());

		// Alias: "str" → [9, 12) — must stop before ' ='
		assertTrue(param.hasAlias());
		assertEquals("str", param.getAlias());
		assertEquals(9, param.getAliasStart());
		assertEquals(12, param.getAliasEnd());
		assertTrue(param.isInAlias(9));
		assertTrue(param.isInAlias(11));
		assertFalse(param.isInAlias(13)); // '='
		assertFalse(param.isInAlias(15)); // '"'

		// Default value: "\"foo\"" → [15, 20)
		assertTrue(param.hasDefaultValue());
		assertEquals("\"foo\"", param.getDefaultValue());
		assertEquals(15, param.getDefaultValueStart());
		assertEquals(20, param.getDefaultValueEnd());
		assertTrue(param.isInDefaultValue(15)); // opening '"'
		assertTrue(param.isInDefaultValue(17)); // 'o' inside
		assertTrue(param.isInDefaultValue(19)); // closing '"'
		assertFalse(param.isInDefaultValue(13)); // '='
		assertFalse(param.isInDefaultValue(14)); // space after '='
		assertFalse(param.isInDefaultValue(21)); // '}'
		assertEquals("\"foo\"", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	@Test
	public void defaultValueDoubleQuoteEmpty() {
		// { @ S t r i n g s t r = " " }
		// 0 1 2 ... 8 9 ..11 12 13 14 15 16
		String content = "{@String str = \"\"}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		assertEquals(9, param.getAliasStart());
		assertEquals(12, param.getAliasEnd());

		assertTrue(param.hasDefaultValue());
		assertEquals("\"\"", param.getDefaultValue());
		assertEquals(15, param.getDefaultValueStart());
		assertEquals(17, param.getDefaultValueEnd());
	}

	// -------------------------------------------------------------------------
	// Default value — single-quoted string
	// -------------------------------------------------------------------------

	@Test
	public void defaultValueSingleQuote() {
		// { @ S t r i n g s t r = ' f o o ' }
		// 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0
		// 1 1 1 1 1 1 1 1 1 1 2
		String content = "{@String str = 'foo'}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(21, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(20, param.getEndContent());

		// Alias: "str" → [9, 12)
		assertEquals(9, param.getAliasStart());
		assertEquals(12, param.getAliasEnd());
		assertEquals("str", param.getAlias());
		assertFalse(param.isInAlias(13)); // '='

		// Default value: "'foo'" → [15, 20)
		assertTrue(param.hasDefaultValue());
		assertEquals("'foo'", param.getDefaultValue());
		assertEquals(15, param.getDefaultValueStart());
		assertEquals(20, param.getDefaultValueEnd());
		assertTrue(param.isInDefaultValue(15)); // opening '\''
		assertTrue(param.isInDefaultValue(17)); // 'o'
		assertTrue(param.isInDefaultValue(19)); // closing '\''
		assertFalse(param.isInDefaultValue(13)); // '='
		assertFalse(param.isInDefaultValue(21)); // '}'
		assertEquals("'foo'", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	@Test
	public void defaultValueSingleQuoteEmpty() {
		// {@String str = ''}
		String content = "{@String str = ''}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		assertTrue(param.hasDefaultValue());
		assertEquals("''", param.getDefaultValue());
		assertEquals(15, param.getDefaultValueStart());
		assertEquals(17, param.getDefaultValueEnd());
		assertEquals("''", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	// -------------------------------------------------------------------------
	// Default value — numeric
	// -------------------------------------------------------------------------

	@Test
	public void defaultValueInteger() {
		// { @ I n t e g e r i = 1 2 3 }
		// 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17
		String content = "{@Integer i = 123}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(18, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(17, param.getEndContent());

		// Java type: "Integer" → [2, 9)
		assertEquals("Integer", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(9, param.getClassNameEnd());

		// Alias: "i" → [10, 11)
		assertTrue(param.hasAlias());
		assertEquals("i", param.getAlias());
		assertEquals(10, param.getAliasStart());
		assertEquals(11, param.getAliasEnd());
		assertTrue(param.isInAlias(10));
		assertFalse(param.isInAlias(12)); // '='

		// Default value: "123" → [14, 17)
		assertTrue(param.hasDefaultValue());
		assertEquals("123", param.getDefaultValue());
		assertEquals(14, param.getDefaultValueStart());
		assertEquals(17, param.getDefaultValueEnd());
		assertTrue(param.isInDefaultValue(14)); // '1'
		assertTrue(param.isInDefaultValue(16)); // '3'
		assertFalse(param.isInDefaultValue(12)); // '='
		assertFalse(param.isInDefaultValue(18)); // '}'
		assertEquals("123", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	@Test
	public void defaultValueNegativeInteger() {
		// { @ I n t e g e r i = - 4 2 }
		// 0 1 2 ... 9 10 11 12 13 14 15 16 17
		String content = "{@Integer i = -42}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		assertTrue(param.hasDefaultValue());
		assertEquals("-42", param.getDefaultValue());
		assertEquals(14, param.getDefaultValueStart());
		assertEquals(17, param.getDefaultValueEnd());
		assertEquals("-42", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	@Test
	public void defaultValueLong() {
		// {@Long l = 9876543210}
		// 0 1 2345 6 7 8 9 ...
		String content = "{@Long l = 9876543210}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Java type: "Long" → [2, 6)
		assertEquals("Long", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(6, param.getClassNameEnd());

		// Alias: "l" → [7, 8)
		assertEquals("l", param.getAlias());
		assertEquals(7, param.getAliasStart());
		assertEquals(8, param.getAliasEnd());

		// Default value: "9876543210" → [11, 21)
		assertTrue(param.hasDefaultValue());
		assertEquals("9876543210", param.getDefaultValue());
		assertEquals(11, param.getDefaultValueStart());
		assertEquals(21, param.getDefaultValueEnd());
		assertEquals("9876543210", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	// -------------------------------------------------------------------------
	// Default value — qualified type
	// -------------------------------------------------------------------------

	@Test
	public void qualifiedTypeWithDefaultValue() {
		// { @ j a v a . l a n g . S t r i n g s t r = " h e l l o " }
		// 0 1 2 ... 18 19..21 22 23 24 25 ... 31 32
		String content = "{@java.lang.String str = \"hello\"}";
		ParameterDeclaration param = parseFirst(content);
		assertTrue(param.isClosed());

		// Node bounds
		assertEquals(0, param.getStart());
		assertEquals(33, param.getEnd());
		assertEquals(2, param.getStartContent());
		assertEquals(32, param.getEndContent());

		// Java type: "java.lang.String" → [2, 18)
		assertEquals("java.lang.String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(18, param.getClassNameEnd());

		// Alias: "str" → [19, 22)
		assertEquals("str", param.getAlias());
		assertEquals(19, param.getAliasStart());
		assertEquals(22, param.getAliasEnd());
		assertFalse(param.isInAlias(23)); // '='

		// Default value: "\"hello\"" → [25, 32)
		assertTrue(param.hasDefaultValue());
		assertEquals("\"hello\"", param.getDefaultValue());
		assertEquals(25, param.getDefaultValueStart());
		assertEquals(32, param.getDefaultValueEnd());
		assertTrue(param.isInDefaultValue(25));
		assertTrue(param.isInDefaultValue(29));
		assertFalse(param.isInDefaultValue(23)); // '='
		assertEquals("\"hello\"", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	// -------------------------------------------------------------------------
	// Unclosed nodes
	// -------------------------------------------------------------------------

	@Test
	public void unclosedSimpleType() {
		// { @ S t r i n g s t r (no '}')
		// 0 1 2 3 4 5 6 7 8 9 0 1
		// 1 1
		String content = "{@String str";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, first.getKind());
		ParameterDeclaration param = (ParameterDeclaration) first;

		assertFalse(param.isClosed());

		assertEquals(0, param.getStart());
		assertEquals(2, param.getStartContent());

		// Java type and alias are still resolvable
		assertEquals("String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(8, param.getClassNameEnd());

		assertTrue(param.hasAlias());
		assertEquals("str", param.getAlias());
		assertEquals(9, param.getAliasStart());

		assertFalse(param.hasDefaultValue());
	}

	@Test
	public void unclosedWithTrailingNewline() {
		// {@\r\n followed by another node
		String content = "{@\r\n" + "{#for todo in todos}\r\n" + "{/}";
		Template template = TemplateParser.parse(content, "test.qute");

		Node first = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, first.getKind());
		ParameterDeclaration param = (ParameterDeclaration) first;

		assertFalse(param.isClosed());
		assertEquals(0, param.getStart());
		assertEquals(2, param.getStartContent());

		// Empty content: no type, no alias, no default
		assertEquals("", param.getJavaType());
		assertFalse(param.hasAlias());
		assertEquals(-1, param.getAliasStart());
		assertFalse(param.hasDefaultValue());
		assertEquals(-1, param.getDefaultValueStart());
		assertEquals(-1, param.getDefaultValueEnd());

		// The for section is still parsed as the second child
		Node second = template.getChild(1);
		assertEquals(NodeKind.Section, second.getKind());
		assertEquals(4, second.getStart()); // right after "\r\n"
	}

	@Test
	public void unclosedWithDefaultValue() {
		// { @ S t r i n g s t r = " f o o " (no '}')
		// 0 1 2 ... 8 9..11 12 13 14 15..19
		String content = "{@String str = \"foo\"";
		Template template = TemplateParser.parse(content, "test.qute");
		assertEquals(1, template.getChildCount());
		Node first = template.getChild(0);
		assertEquals(NodeKind.ParameterDeclaration, first.getKind());
		ParameterDeclaration param = (ParameterDeclaration) first;

		assertFalse(param.isClosed());
		assertEquals(0, param.getStart());
		assertEquals(2, param.getStartContent());

		assertEquals("String", param.getJavaType());
		assertEquals(2, param.getClassNameStart());
		assertEquals(8, param.getClassNameEnd());

		assertEquals("str", param.getAlias());
		assertEquals(9, param.getAliasStart());
		assertEquals(12, param.getAliasEnd());

		assertTrue(param.hasDefaultValue());
		assertEquals("\"foo\"", param.getDefaultValue());
		assertEquals(15, param.getDefaultValueStart());
		assertEquals(20, param.getDefaultValueEnd());
		assertEquals("\"foo\"", content.substring(param.getDefaultValueStart(), param.getDefaultValueEnd()));
	}

	// -------------------------------------------------------------------------
	// getJavaTypeNameRange(offset) — lookup by offset
	// -------------------------------------------------------------------------

	@Test
	public void javaTypeNameRangeByOffsetSimple() {
		// {@String str} — single segment, offset inside "String"
		String content = "{@String str}";
		ParameterDeclaration param = parseFirst(content);

		// Inside "String"
		ParameterDeclaration.JavaTypeRangeOffset range = param.getJavaTypeNameRange(4);
		assertNotNull(range);
		assertEquals(2, range.getStart());
		assertEquals(8, range.getEnd());
		assertFalse(range.isInGeneric());

		// Outside type range (inside alias)
		assertNull(param.getJavaTypeNameRange(10));
	}

	@Test
	public void javaTypeNameRangeByOffsetGeneric() {
		// {@java.util.Map<String,Integer> map}
		String content = "{@java.util.Map<String,Integer> map}";
		ParameterDeclaration param = parseFirst(content);

		// Inside "java.util.Map"
		ParameterDeclaration.JavaTypeRangeOffset main = param.getJavaTypeNameRange(4);
		assertNotNull(main);
		assertFalse(main.isInGeneric());
		assertEquals(2, main.getStart());
		assertEquals(15, main.getEnd());

		// Inside "String" (first generic arg)
		ParameterDeclaration.JavaTypeRangeOffset str = param.getJavaTypeNameRange(18);
		assertNotNull(str);
		assertTrue(str.isInGeneric());
		assertEquals(16, str.getStart());
		assertEquals(22, str.getEnd());

		// Inside "Integer" (second generic arg)
		ParameterDeclaration.JavaTypeRangeOffset intg = param.getJavaTypeNameRange(26);
		assertNotNull(intg);
		assertTrue(intg.isInGeneric());
		assertTrue(intg.isGenericClosed());
		assertEquals(23, intg.getStart());
		assertEquals(30, intg.getEnd());

		// Inside alias "map" — no type range
		assertNull(param.getJavaTypeNameRange(33));
	}

	// -------------------------------------------------------------------------
	// Offset exclusivity: '=' belongs to none of the ranges
	// -------------------------------------------------------------------------

	@Test
	public void equalsSignBelongsToNoRange() {
		// {@String str = "foo"}
		// ^
		// 13
		String content = "{@String str = \"foo\"}";
		ParameterDeclaration param = parseFirst(content);

		int equalsOffset = content.indexOf('='); // 13
		assertFalse(param.isInJavaTypeName(equalsOffset));
		assertFalse(param.isInAlias(equalsOffset));
		assertFalse(param.isInDefaultValue(equalsOffset));
		assertNull(param.getJavaTypeNameRange(equalsOffset));
	}
}