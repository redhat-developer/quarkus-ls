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
public class QuteHighlightingInLetSectionTest {

	@Test
	public void fromParam() throws BadLocationException {
		String template = "{#let val|ue=123}\r\n" + //
				"	{value}\r\n" + //
				"	{value}\r\n" + //
				"{/let}";
		testHighlightsFor(template, //
				hl(r(0, 6, 0, 11), Write), //
				hl(r(1, 2, 1, 7), Read),
				hl(r(2, 2, 2, 7), Read));

	}

	@Test
	public void toParam() throws BadLocationException {
		String template = "{#let value=123}\r\n" + //
				"	{va|lue}\r\n" + //
				"	{value}\r\n" + //
				"{/let}";
		testHighlightsFor(template, //
				hl(r(1, 2, 1, 7), Read), //
				hl(r(0, 6, 0, 11), Write));
	}
	
	@Test
	public void noParamReferences() throws BadLocationException {
		String template = "{#let value=1|23}\r\n" + //
				"	{value}\r\n" + //
				"	{value}\r\n" + //
				"{/let}";
		testHighlightsFor(template, //
				hl(r(0, 12, 0, 15), Read));

	}
}
