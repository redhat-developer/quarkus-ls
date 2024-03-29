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
		String template = "|{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template);

		template = "{|#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#|each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#e|ach items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#each| items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#each |items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each}";
		testHighlightsFor(template);
	}

	@Test
	public void highlightingEndTagEachSection() throws BadLocationException {
		String template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/e|ach}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/|each}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/each|}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 6), Read));
	}

	@Test
	public void highlightingEndTagEachSectionWithOptionalName() throws BadLocationException {
		String template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{|/}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 2), Read));

		template = "{#each items}\r\n" + //
				"  {it.name} \r\n" + //
				"{/|}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 6), Read), //
				hl(r(2, 1, 2, 2), Read));
	}

	@Test
	public void highlightingStartTagCustomSection() throws BadLocationException {
		String template = "{#tit|le}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#|title}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#title|}Book {book.id}{/title}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));
	}

	@Test
	public void highlightingEndTagCustomSection() throws BadLocationException {
		String template = "{#title}Book {book.id}{/tit|le}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#title}Book {book.id}{|/title}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#title}Book {book.id}{/|title}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#title}Book {book.id}{/title|}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 29), Read));

		template = "{#title}Book {book.id}{|/}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 24), Read));
	}

	@Test
	public void highlightingEndTagCustomSectionWithOptionalName() throws BadLocationException {
		String template = "{#title}Book {book.id}{|/}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 24), Read));

		template = "{#title}Book {book.id}{/|}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 7), Read), //
				hl(r(0, 23, 0, 24), Read));
	}

	@Test
	public void highlightingWithElseForSection() throws BadLocationException {
		String template = "{#f|or item in items}\r\n" + //
				"  {item.name} \r\n" + //
				"{#else}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 5), Read), //
				hl(r(3, 1, 3, 5), Read), //
				hl(r(2, 1, 2, 6), Read));
	}

	@Test
	public void highlightingWithElseForAndIfSection() throws BadLocationException {
		String template = "{#f|or item in items}\r\n" + //
				"  {item.name} \r\n" + //
				"{#else}\r\n" + //
				"{#if}\r\n" + //
				"{#else}\r\n" + //
				"{/if}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 5), Read), //
				hl(r(6, 1, 6, 5), Read), //
				hl(r(2, 1, 2, 6), Read));

		template = "{#for item in items}\r\n" + //
				"  {item.name} \r\n" + //
				"{#else}\r\n" + //
				"{#i|f}\r\n" + //
				"{#else}\r\n" + //
				"{/if}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(3, 1, 3, 4), Read), //
				hl(r(5, 1, 5, 4), Read), //
				hl(r(4, 1, 4, 6), Read));
	}

	@Test
	public void highlightingWithEmptyEndSection() throws BadLocationException {
		String template = "{#fo|r item in items}\r\n"
				+ "	  {#for item in items}\r\n"
				+ "	    \r\n"
				+ "	  {/}\r\n"
				+ "	{/}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 5), Read), //
				hl(r(4, 2, 4, 3), Read));

		template = "{#for item in items}\r\n"
				+ "	  {#for item in items}\r\n"
				+ "	    \r\n"
				+ "	  {/}\r\n"
				+ "	{/|}";
		testHighlightsFor(template, //
				hl(r(0, 1, 0, 5), Read), //
				hl(r(4, 2, 4, 3), Read));

		template = "{#for item in items}\r\n"
				+ "	  {#for item in items}\r\n"
				+ "	    \r\n"
				+ "	  \r\n" // <-- here it misses the {\} for the second #for
				+ "	{/|}";
		testHighlightsFor(template, //
				hl(r(1, 4, 1, 8), Read), //
				hl(r(4, 2, 4, 3), Read));

		template = "{#for item in items}\r\n"
				+ "	  {#fo|r item in items}\r\n"
				+ "	    \r\n"
				+ "	  {/}\r\n"
				+ "	{/}";
		testHighlightsFor(template, //
				hl(r(1, 4, 1, 8), Read), //
				hl(r(3, 4, 3, 5), Read));
	}

	@Test
	public void highlightingWithEmptyEndSectionNotClosed() throws BadLocationException {
		String template = "{#for items}\r\n"
				+ "	  {#each items}\r\n"
				+ "	    {#f|or items }{/for}\r\n"
				+ "	{/for}";
		testHighlightsFor(template, //
				hl(r(2, 6, 2, 10), Read), //
				hl(r(2, 19, 2, 23), Read));

		template = "{#for items}\r\n"
				+ "	  {#each items}\r\n"
				+ "	    {#f|or items }{/for}\r\n"
				+ "	{/}";
		testHighlightsFor(template, //
				hl(r(2, 6, 2, 10), Read), //
				hl(r(2, 19, 2, 23), Read));
	}

	@Test
	public void highlightingWithEmptyEndSectionNotClosed2() throws BadLocationException {
		String template = "{#each items}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "\r\n"
				+ "{#ea|ch items}\r\n"
				+ "  {it.name}\r\n"
				+ "{/each}\r\n"
				+ "\r\n"
				+ "{/}\r\n"
				+ "";
		testHighlightsFor(template, //
				hl(r(4, 1, 4, 6), Read), //
				hl(r(6, 1, 6, 6), Read));
	}
	
	@Test
	public void highlightingWithEmptyEndSectionNotClosed3() throws BadLocationException {
		String template = "{#each items}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "  {it.name}\r\n"
				+ "{/each}\r\n"
				+ "\r\n"
				+ "{#ea|ch items}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "  {it.name}\r\n"
				+ "{/}\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "{/}";
		testHighlightsFor(template, //
				hl(r(6, 1, 6, 6), Read), //
				hl(r(14, 1, 14, 2), Read));
		
		template = "{#each items}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "  {it.name}\r\n"
				+ "{/each}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "\r\n"
				+ "{#each items}\r\n"
				+ "  {it.name}\r\n"
				+ "{/|}\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "\r\n"
				+ "{/}";
		testHighlightsFor(template, //
				hl(r(8, 1, 8, 6), Read), //
				hl(r(10, 1, 10, 2), Read));
	}
}
