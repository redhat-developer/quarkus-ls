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

	private static final Predicate<Integer> PARAMETER_NAME_OR_VALUE_PREDICATE = ch -> {
		return ch != ' ' && ch != '=';
	};

	public static ParameterScanner createScanner(String input) {
		return createScanner(input, false);
	}

	public static ParameterScanner createScanner(String input, boolean methodParameters) {
		return createScanner(input, 0, input.length(), methodParameters);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			boolean methodParameters) {
		return createScanner(input, initialOffset, endOffset, methodParameters, ScannerState.WithinParameter);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			boolean methodParameters, ScannerState initialState) {
		return new ParameterScanner(input, initialOffset, endOffset, methodParameters, initialState);
	}

	private final boolean methodParameters;

	private int bracket;

	ParameterScanner(String input, int initialOffset, int endOffset, boolean methodParameters,
			ScannerState initialState) {
		super(input, initialOffset, endOffset, initialState, TokenType.Unknown, TokenType.EOS);
		this.methodParameters = methodParameters;
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
			if (methodParameters) {

			} else {
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
		return stream.advanceWhileChar(PARAMETER_NAME_OR_VALUE_PREDICATE) > 0;
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
