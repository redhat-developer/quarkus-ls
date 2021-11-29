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
package com.redhat.qute.services.highlighting;

import static com.redhat.qute.QuteAssert.hl;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testHighlightsFor;
import static org.eclipse.lsp4j.DocumentHighlightKind.Read;
import static org.eclipse.lsp4j.DocumentHighlightKind.Write;

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Tests for Qute highlighting.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteHighlightingInParameterDeclarationTest {

	@Test
	public void inClassName() throws BadLocationException {
		String template = "{@org.acme.I|tem";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 15), Write));
		
		template = "{@org.acme.I|tem ";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 15), Write));
		
		template = "{@org.acme.I|tem item";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 15), Write));
		
		template = "{@org.acme.I|tem item}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 15), Write));
	}

	@Test
	public void inGenericClassName() throws BadLocationException {
		String template = "{@java.util.List<org.acme.I|tem";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
		
		template = "{@java.util.List<org.acme.I|tem>";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
		
		template = "{@java.util.List<org.acme.I|tem> item";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
		
		template = "{@java.util.List<org.acme.I|tem> item}";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
		
		template = "{@java.util.List<org.acme.I|tem item";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
		
		template = "{@java.util.List<org.acme.I|tem item}";
		testHighlightsFor(template, //
				hl(r(0, 17, 0, 30), Write));
	}
	
	@Test
	public void fromExpression() throws BadLocationException {
		String template = "{@org.acme.Item item}\r\n" + //
				"{it|em.name}";
		testHighlightsFor(template, //
				hl(r(1, 1, 1, 5), Read), //
				hl(r(0, 16, 0, 20), Write));
	}

	@Test
	public void toIterableInForSection() throws BadLocationException {
		String template = "{@java.util.List<org.acme.Item> it|ems}\r\n" + //
				"{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}\r\n" + //
				"{items.size}";
		testHighlightsFor(template, //
				hl(r(0, 32, 0, 37), Write), //
				hl(r(1, 14, 1, 19), Read), //
				hl(r(4, 1, 4, 6), Read));

		template = "{@java.util.List<org.acme.Item> it|ems}\r\n" + //
				"{#for item in items.items}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}\r\n" + //
				"{items.size}";
		testHighlightsFor(template, //
				hl(r(0, 32, 0, 37), Write), //
				hl(r(1, 14, 1, 19), Read), //
				hl(r(4, 1, 4, 6), Read));
	}

	@Test
	public void fromIterableInForSection() throws BadLocationException {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#for item in it|ems}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}\r\n" + //
				"{items.size}";
		testHighlightsFor(template, //
				hl(r(1, 14, 1, 19), Read), //
				hl(r(0, 32, 0, 37), Write));
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 * 
	 * @throws Exception
	 */
	@Test
	public void toIterableInForSectionWithDerived() throws BadLocationException {
		String template = "{@org.acme.Item it|em}\r\n" + // <-- cursor here
				"<html>\r\n" + //
				"{item.name} \r\n" + // <-- 1) match
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + // <-- 2) match for item.derivedItems
				"  <li>\r\n" + //
				"  {item.name} \r\n" + // <-- No match
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + // 3) match
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(2, 1, 2, 5), Read), //
				hl(r(4, 14, 4, 18), Read), //
				hl(r(8, 8, 8, 12), Read));
	}

	/**
	 * @see https://quarkus.io/guides/qute-reference#expression_resolution
	 * 
	 * @throws Exception
	 */
	@Test
	public void fromIterableInForSectionWithDerived() throws BadLocationException {
		String template = "{@org.acme.Item item}\r\n" + // <-- 1) match
				"<html>\r\n" + //
				"{ite|m.name} \r\n" + // <-- cursor here
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + //
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(2, 1, 2, 5), Read), //
				hl(r(0, 16, 0, 20), Write));

		template = "{@org.acme.Item item}\r\n" + // <-- 1) match
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in it|em.derivedItems} \r\n" + // <-- cursor in it|em.derivedItems
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(4, 14, 4, 18), Read), //
				hl(r(0, 16, 0, 20), Write));

		template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for it|em in item.derivedItems} \r\n" + // <-- cursor in it|em
				"  <li>\r\n" + //
				"  {item.name} \r\n" + // <-- 1) match
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(4, 6, 4, 10), Write), //
				hl(r(6, 3, 6, 7), Read));

		template = "{@org.acme.Item item}\r\n" + //
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + // <-- 1) match
				"  <li>\r\n" + //
				"  {ite|m.name} \r\n" + // <-- cursor here
				"  is derived from\r\n" + //
				"  {data:item.name} \r\n" + //
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(6, 3, 6, 7), Read), //
				hl(r(4, 6, 4, 10), Write));

		template = "{@org.acme.Item item}\r\n" + // <-- 1) match
				"<html>\r\n" + //
				"{item.name} \r\n" + //
				"<ul>\r\n" + //
				"{#for item in item.derivedItems} \r\n" + //
				"  <li>\r\n" + //
				"  {item.name} \r\n" + //
				"  is derived from\r\n" + //
				"  {data:it|em.name} \r\n" + // <-- cursor here
				"  </li>\r\n" + //
				"{/for}\r\n" + //
				"</ul>\r\n" + //
				"</html>";
		testHighlightsFor(template, //
				hl(r(8, 8, 8, 12), Read), //
				hl(r(0, 16, 0, 20), Write));
	}

	@Test
	public void toParamValueInLetSection() throws BadLocationException {
		String template = "{@java.util.List<org.acme.Item> it|ems}\r\n" + //
				"{#let size=items.size}\r\n" + //
				"	{size}\r\n" + //
				"{/let}\r\n" + //
				"{items.size}";
		testHighlightsFor(template, //
				hl(r(0, 32, 0, 37), Write), //
				hl(r(1, 11, 1, 16), Read), //
				hl(r(4, 1, 4, 6), Read));
	}

	@Test
	public void fromParamValueInLetSection() throws BadLocationException {
		String template = "{@java.util.List<org.acme.Item> items}\r\n" + //
				"{#let size=ite|ms.size}\r\n" + //
				"	{size}\r\n" + //
				"{/let}\r\n" + //
				"{items.size}";
		testHighlightsFor(template, //
				hl(r(1, 11, 1, 16), Read), //
				hl(r(0, 32, 0, 37), Write));
	}

	@Test
	public void toObjectInWithSection() throws BadLocationException {
		String template = "{@org.acme.Item it|em}\r\n" + //
				"{#with item}\r\n" + //
				"	{name}\r\n" + //
				"{/with}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(1, 7, 1, 11), Read), //
				hl(r(4, 1, 4, 5), Read));

		template = "{@org.acme.Item it|em}\r\n" + //
				"{#with item.review}\r\n" + //
				"	{name}\r\n" + //
				"{/with}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(1, 7, 1, 11), Read), //
				hl(r(4, 1, 4, 5), Read));
	}

	@Test
	public void fromObjectInWithSection() throws BadLocationException {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#with it|em}\r\n" + //
				"	{name}\r\n" + //
				"{/with}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(1, 7, 1, 11), Read), //
				hl(r(0, 16, 0, 20), Write));
	}

	@Test
	public void toValueInSwitchSection() throws BadLocationException {
		String template = "{@org.acme.Item ite|m}\r\n" + //
				"{#switch item}\r\n" + //
				"	{#case 'A'}\r\n" + //
				"{/switch}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(1, 9, 1, 13), Read), //
				hl(r(4, 1, 4, 5), Read));

		template = "{@org.acme.Item ite|m}\r\n" + //
				"{#switch item.name}\r\n" + //
				"	{#case 'A'}\r\n" + //
				"{/switch}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(1, 9, 1, 13), Read), //
				hl(r(4, 1, 4, 5), Read));
	}

	@Test
	public void fromValueInSwitchSection() throws BadLocationException {
		String template = "{@org.acme.Item item}\r\n" + //
				"{#switch it|em.name}\r\n" + //
				"	{#case 'A'}\r\n" + //
				"{/switch}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(1, 9, 1, 13), Read), //
				hl(r(0, 16, 0, 20), Write));
	}
	
	@Test
	public void toExpression() throws BadLocationException {
		String template = "{@org.acme.Item it|em}\r\n" + //
				"{item.name}";
		testHighlightsFor(template, //
				hl(r(0, 16, 0, 20), Write), //
				hl(r(1, 1, 1, 5), Read));
	}
}
