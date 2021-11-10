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

import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Tests for Qute highlighting.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteHighlightingInSectionTagTest {

	@Test
	public void noHighlightingEachSection() throws BadLocationException {
		String template = "{#each |items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template);

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}|";
		testHighlightsFor(template);
	}

	@Test
	public void highlightingStartTagEachSection() throws BadLocationException {
		String template = "{#e|ach items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));

		template = "{#|each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));

		template = "{#each| items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));
	}

	@Test
	public void highlightingEndTagEachSection() throws BadLocationException {
		String template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/e|ach}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/|each}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each|}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 6), Read), //
				hl(r(2, 2, 2, 6), Read));
	}

	@Test
	public void highlightingStartTagCustomSection() throws BadLocationException {
		String template = "{#tit|le}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));

		template = "{#|title}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));

		template = "{#title|}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));
	}

	@Test
	public void highlightingEndTagCustomSection() throws BadLocationException {
		String template = "{#title}Book {book.id}{/tit|le}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));

		template = "{#title}Book {book.id}{/|title}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));

		template = "{#title}Book {book.id}{/title|}";
		testHighlightsFor(template, //
				hl(r(0, 2, 0, 7), Read), //
				hl(r(0, 24, 0, 29), Read));
	}
}
