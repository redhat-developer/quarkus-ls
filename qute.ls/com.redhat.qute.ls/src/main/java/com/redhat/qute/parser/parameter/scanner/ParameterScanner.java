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

	private static final Predicate<Integer> PARAM_SPLITTED_BY_ONLY_SPACE = ch -> {
		return ch != ' ';
	};

	private static final Predicate<Integer> PARAM_SPLITTED_BY_SPACE_OR_EQUALS = ch -> {
		return ch != ' ' && ch != '=';
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
					state = ScannerState.WithinParameter;
					return finishToken(offset, TokenType.ParameterName);
				}
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case WithinParameter: {
			if (!methodParameters && splitWithEquals) {
				if (stream.advanceIfChar('=')) {
					if (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '=')) {
						state = ScannerState.WithinParameters;
					} else {
						state = ScannerState.AfterAssign;
					}
					return finishToken(offset, TokenType.Assign);
				}
			}
			state = ScannerState.WithinParameters;
			return internalScan();
		}

		case AfterAssign: {
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
				state = ScannerState.WithinParameters;
				return finishToken(offset, TokenType.ParameterValue);
			}
		}

		default:
		}
		stream.advance(1);
		return finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private boolean hasNextParameterNameOrValue() {
		if (splitWithEquals) {
			return stream.advanceWhileChar(PARAM_SPLITTED_BY_SPACE_OR_EQUALS) > 0;
		}
		return stream.advanceWhileChar(PARAM_SPLITTED_BY_ONLY_SPACE) > 0;
	}

	private TokenType parseParameterName(int offset) {
		if (methodParameters) {
			stream.advanceUntilChar('(', ')', ',');
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
