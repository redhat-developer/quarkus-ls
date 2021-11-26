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
package com.redhat.qute.services;

import static com.redhat.qute.QuteAssert.ds;
import static com.redhat.qute.QuteAssert.r;
import static com.redhat.qute.QuteAssert.testDocumentSymbolsFor;

import java.util.Arrays;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.Test;

import com.redhat.qute.ls.commons.BadLocationException;

/**
 * Qute symbols test with {@link DocumentSymbol} (with hierarchies).
 */
public class QuteDocumentSymbolsTest {

	@Test
	public void startBracket() throws BadLocationException {
		String template = "{";
		testDocumentSymbolsFor(template, //
				ds("Text", SymbolKind.Constant, r(0, 0, 0, 1), r(0, 0, 0, 1), null, //
						Arrays.asList()));
	}

	@Test
	public void startEndBrackets() throws BadLocationException {
		String template = "{}";
		testDocumentSymbolsFor(template, //
				ds("Text", SymbolKind.Constant, r(0, 0, 0, 2), r(0, 0, 0, 2), null, //
						Arrays.asList()));
	}

	@Test
	public void expression() throws BadLocationException {
		String template = "{foo}";
		testDocumentSymbolsFor(template, //
				ds("Expression", SymbolKind.Function, r(0, 0, 0, 5), r(0, 0, 0, 5), null, //
						Arrays.asList()));
	}

}
