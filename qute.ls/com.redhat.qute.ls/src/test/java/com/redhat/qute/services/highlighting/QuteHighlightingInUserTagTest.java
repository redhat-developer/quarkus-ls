/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
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
 * Tests for Qute highlighting with user section.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteHighlightingInUserTagTest {

	@Test
	public void fromAlias() throws BadLocationException {
		String template = "{#for it|em in items}\r\n" + //
				"	{#linkItem item name=item.name item /}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 10), Write), //
				hl(r(1, 12, 1, 16), Read), //
				hl(r(1, 22, 1, 26), Read));
	}

	@Test
	public void toAliasParam() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem item name=ite|m.name item /}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 22, 1, 26), Read), //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void toAliasIt() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem it|em name=item.name item /}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 12, 1, 16), Read), //
				hl(r(0, 6, 0, 10), Write));
	}

	@Test
	public void toAliasNoIt() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{#linkItem item name=item.name i|tem /}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 32, 1, 36), Write));
	}

}
