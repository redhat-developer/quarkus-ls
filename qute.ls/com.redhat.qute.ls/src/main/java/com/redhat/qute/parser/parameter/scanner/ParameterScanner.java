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
package com.redhat.qute.parser.parameter.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;

/**
 * Parameter scanner.
 *
 * @author Angelo ZERR
 *
 */
public class ParameterScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final int[] PAREN_COMMA = new int[] { '(', ')', ',' };

	private static final Predicate<Integer> PARAM_SPLIT_BY_ONLY_SPACE_OR_PAREN  = ch -> {
		return ch != ' ' && ch != '(';
	};

	private static final Predicate<Integer> PARAM_SPLIT_BY_SPACE_OR_EQUALS_OR_PAREN = ch -> {
		return ch != ' ' && ch != '=' && ch != '(';
	};

	public static ParameterScanner createScanner(String input) {
		return createScanner(input, false, true);
	}

	public static ParameterScanner createScanner(String input, boolean methodParameters, boolean splitWithEquals) {
		return createScanner(input, 0, input.length(), methodParameters, splitWithEquals);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			boolean methodParameters, boolean splitWithEquals) {
		return createScanner(input, initialOffset, endOffset, methodParameters, splitWithEquals,
				ScannerState.WithinParameter);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			boolean methodParameters, boolean splitWithEquals, ScannerState initialState) {
		return new ParameterScanner(input, initialOffset, endOffset, methodParameters, splitWithEquals, initialState);
	}

	private final boolean methodParameters;
	private final boolean splitWithEquals;

	private int bracket;

	ParameterScanner(String input, int initialOffset, int endOffset, boolean methodParameters, boolean splitWithEquals,
			ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
		this.methodParameters = methodParameters;
		this.splitWithEquals = splitWithEquals;
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

			case WithinParameters: {
				if (stream.skipWhitespace()) {
					return finishToken(offset, TokenType.Whitespace);
				}
				if (methodParameters) {
					return parseParameterName(offset);
				} else {
					if (hasNextParameterNameOrValue()) {
						adjustPositionWithComma();
						state = ScannerState.WithinParameter;
						return finishToken(offset, TokenType.ParameterName);
					}
				}
				return finishToken(offset, TokenType.Unknown);
			}

			case WithinParameter: {
				if (!methodParameters && splitWithEquals) {
					if (stream.skipWhitespace()) {
						return finishToken(offset, TokenType.Whitespace);
					}
					if (stream.advanceIfChar('=')) {
						state = ScannerState.AfterAssign;
						return finishToken(offset, TokenType.Assign);
					}
				}
				state = ScannerState.WithinParameters;
				return internalScan();
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
					state = ScannerState.WithinParameters;
					return finishToken(offset, TokenType.ParameterValue);
				}
				
				if (hasNextParameterNameOrValue()) {
					adjustPositionWithComma();
					state = ScannerState.WithinParameters;
					return finishToken(offset, TokenType.ParameterValue);
				}
			}

			default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private void adjustPositionWithComma() {
		if (stream.peekChar() == '(') {
			// ex: utils.values(a,  b) 
			// in '#for items in utils.values(a,  b)
			// we need to adjust the end parameter name/value offset after the ')'
			stream.advance(1);
			int bracket = 1;
			while(stream.advanceUntilChar(PAREN_COMMA)) {
				int p = stream.peekChar();
				stream.advance(1);
				if (p == '(') {
					bracket++;
				} else if (p == ')') {
					bracket--;
					if(bracket ==0) {
						break;
					}
				}						}
		}
		if (!stream.eos() && stream.peekChar(1) != ' ') {
			if (hasNextParameterNameOrValue()) {
				adjustPositionWithComma();
			}
		}
	}

	private boolean hasNextParameterNameOrValue() {
		if (splitWithEquals) {
			return stream.advanceWhileChar(PARAM_SPLIT_BY_SPACE_OR_EQUALS_OR_PAREN) > 0;
		}
		return stream.advanceWhileChar(PARAM_SPLIT_BY_ONLY_SPACE_OR_PAREN) > 0;
	}

	private TokenType parseParameterName(int offset) {
		if (methodParameters) {
			stream.advanceUntilChar(PAREN_COMMA);
			if (stream.peekChar() == '(') {
				stream.advance(1);
				bracket++;
				return parseParameterName(offset);
			} else if (stream.peekChar() == ')') {
				stream.advance(1);
				if (bracket > 0) {
					bracket--;
				}
				return parseParameterName(offset);
			}
			if (stream.peekChar() == ',') {
				if (bracket > 0) {
					stream.advance(1);
					return parseParameterName(offset);
				}
				if (offset == stream.pos()) {
					stream.advance(1);
					return internalScan();
				}
			}
		}
		state = ScannerState.WithinParameters;
		return finishToken(offset, TokenType.ParameterName);
	}
}
