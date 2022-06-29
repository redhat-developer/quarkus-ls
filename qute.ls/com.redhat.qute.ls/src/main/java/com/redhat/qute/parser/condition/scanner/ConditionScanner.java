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
package com.redhat.qute.parser.condition.scanner;

import com.redhat.qute.parser.scanner.AbstractScanner;

/**
 * Condition scanner.
 *
 * @author Angelo ZERR
 *
 */
public class ConditionScanner extends AbstractScanner<TokenType, ScannerState> {

	public static ConditionScanner createScanner(String input) {
		return createScanner(input, 0, input.length());
	}

	public static ConditionScanner createScanner(String input, int initialOffset, int endOffset) {
		return new ConditionScanner(input, initialOffset, endOffset, ScannerState.WithinConditions);
	}

	ConditionScanner(String input, int initialOffset, int endOffset, ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
		this.nbMethods = 0;
	}

	private int nbMethods;

	private static final int[] QUOTE_OR_PAREN = new int[] {'(', ')', '\'', '"'};

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinConditions: {
			if (stream.advanceIfChar('(')) {
				int pos = stream.pos();
				if (stream.pos() > 1 && stream.peekCharAtOffset(pos - 2) != ' ') {
					// '(' coming from a method
					// ex foo.or|(a)
					nbMethods++;
					return internalScan();
				}
				// '(' coming from a space
				// ex foo or (a > b)
				return finishToken(offset, TokenType.StartBracketCondition);
			}
			if (stream.advanceIfChar(')')) {
				if (nbMethods > 0) {
					// '(' coming from a method
					// ex foo.or(a|)
					nbMethods--;
					return internalScan();
				}
				return finishToken(offset, TokenType.EndBracketCondition);
			}
			if (stream.advanceUntilChar(QUOTE_OR_PAREN)) {
				int c = stream.peekChar();
				if (c == '"' || c == '\'') {
					stream.advance(1);
					if (stream.advanceUntilChar(c)) {
						stream.advance(1);
					}
				}
			}
			return internalScan();
		}

		default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

}
