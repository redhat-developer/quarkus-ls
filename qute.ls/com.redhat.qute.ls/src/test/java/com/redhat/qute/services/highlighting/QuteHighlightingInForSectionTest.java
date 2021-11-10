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
	public void toAlias() throws BadLocationException {
		String template = "{#for item in items}\r\n" + //
				"	{it|em.name}\r\n" + //
				"{/for}";
		testHighlightsFor(template, //
				hl(r(1, 2, 1, 6), Read), //
				hl(r(0, 6, 0, 10), Write));
	}
}
