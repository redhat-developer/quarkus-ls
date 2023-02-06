/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.html.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;

/**
 * Html scanner.
 *
 */
public class HtmlScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> PARAM_SPLITTED_BY_ONLY_SPACE = ch -> {
		return ch != ' ';
	};

	private static final int[] SPACE_ASSIGN = new int[] { ' ', '=' };

	public static HtmlScanner createScanner(String input) {
		return createScanner(input, 0, input.length());
	}

	public static HtmlScanner createScanner(String input, int initialOffset, int endOffset) {
		return new HtmlScanner(input, initialOffset, endOffset, ScannerState.WithinElementName);
	}

	HtmlScanner(String input, int initialOffset, int endOffset, ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

			case WithinElementName: {
				if (stream.skipWhitespace()) {
					return finishToken(offset, TokenType.Whitespace);
				}
				if (stream.advanceIfChar('<')){
					return finishToken(offset, TokenType.Bracket);
				}
				if (hasNextParameterNameOrValue()) {
					state = ScannerState.WithinAttributeName;
					return finishToken(offset, TokenType.ElementName);
				}
				return finishToken(offset, TokenType.Unknown);
			}

			case WithinAttributeName: {
				if (stream.skipWhitespace()) {
					return finishToken(offset, TokenType.Whitespace);
				}
				if (stream.advanceIfChar('=')) {
					state = ScannerState.AfterAssign;
					return finishToken(offset, TokenType.Assign);
				}
				if (stream.advanceIfChar('>')) {
					state = ScannerState.AfterAssign;
					return finishToken(offset, TokenType.Bracket);
				}
				if (stream.advanceUntilAnyOfChars(SPACE_ASSIGN)) {
					int c = stream.peekCharAtOffset(stream.pos() - 1);
					state = ScannerState.WithinAttributeName;
					if (c == ' ') {
						return finishToken(offset, TokenType.Whitespace);
					}
					return finishToken(offset, TokenType.AttributeName);
				}
				return finishToken(offset, TokenType.Unknown);
			}

			case AfterAssign: {
				if (stream.skipWhitespace()) {
					return finishToken(offset, TokenType.Whitespace);
				}
				int c = stream.peekChar();
				if (c == '"' || c == '\'') {
					stream.advance(1);
					if (stream.advanceUntilChar(c)) {
						stream.advance(1);
					}
					state = ScannerState.WithinAttributeName;
					return finishToken(offset, TokenType.AttributeValue);
				}
			}

			default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private boolean hasNextParameterNameOrValue() {
		return stream.advanceWhileChar(PARAM_SPLITTED_BY_ONLY_SPACE) > 0;
	}
}
