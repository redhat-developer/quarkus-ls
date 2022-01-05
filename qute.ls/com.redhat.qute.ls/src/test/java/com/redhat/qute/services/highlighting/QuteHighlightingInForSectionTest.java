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
 * Tests for Qute highlighting with #for section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteHighlightingInForSectionTest {

	@Test
	public void fromAlias() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 2, 1, 6), Read));
	}

	@Test
	public void fromAliasInElseBlock() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"{#else}" + //
				"	{item.name}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void toAlias() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void toAliasInElseBlock() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{#else}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(0, 6, 0, 10), Write));

		template = "{#for item in items}\r\n" + //
				"{#else}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(2, 2, 2, 6), Read));
	}

	@Test
	public void metadataFromAlias() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{item_count}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(2, 2, 2, 6), Read));
	}

	@Test
	public void metadataFromAliasInElseBlock() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{item_count}\r\n" + //
				"	{#else}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(2, 2, 2, 6), Read));

		template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#else}\r\n" + //
				"	{item_count}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 2, 1, 6), Read));

		template = "{#for it|em in items}\r\n" + //
				"	{#else}\r\n" + //
				"	{item.name}\r\n" + //
				"	{item_count}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void metadataFromAliasWithLetParameter() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#let name=item.name count=item_count}\r\n" + //
				"	{/#let}\r\n" + //
				"	{item_count}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(2, 12, 2, 16), Read), // #let name=ite|m.name
				hl(r(2, 28, 2, 32), Read), // #let count=item|_count
				hl(r(4, 2, 4, 6), Read));
	}

	@Test
	public void metadataToAlias() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{item_|count}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(2, 2, 2, 12), Read), //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void metadataToAliasWithLetParameter() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{item.name}\r\n" + //
				"	{#let name=item.name count=it|em_count}\r\n" + //
				"	{/#let}\r\n" + //
				"	{item_count}\r\n" + //
				"	{item_XXXX}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(2, 28, 2, 38), Read), // #let count=ite|m_count
				hl(r(0, 6, 0, 10), Write)); // {#for ite|m
	}

	@Test
	public void metadataFromAliasWithLetParameterNoExpression() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"    <h2>{item.name}</h2>    \r\n" + //
				"    {#let foo=\"bar\"}\r\n" + //
				"      <p>{foo}</p>\r\n" + //
				"    {/}\r\n" + //
				"{/}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), // {#for ite|m
				hl(r(1, 9, 1, 13), Read)); // {ite|m.name
	}

}
