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

public class ParameterScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> PARAMETER_NAME_OR_VALUE_PREDICATE = ch -> {
		return ch != ' ' && ch != '=';
	};
	
	public static ParameterScanner createScanner(String input) {
		return createScanner(input, 0, input.length());
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset) {
		return createScanner(input, initialOffset, endOffset, ScannerState.WithinParameter);
	}

	public static ParameterScanner createScanner(String input, int initialOffset, int endOffset,
			ScannerState initialState) {
		return new ParameterScanner(input, initialOffset, endOffset, initialState);
	}

	ParameterScanner(String input, int initialOffset, int endOffset, ScannerState initialState) {
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

		case WithinParameters: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			if (hasNextParameterNameOrValue()) {
				state = ScannerState.WithinParameter;
				return finishToken(offset, TokenType.ParameterName);
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case WithinParameter: {			
			if (stream.advanceIfChar('=')) {
				if(!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '=')) {
					state = ScannerState.WithinParameters;		
				} else {
					state = ScannerState.AfterAssign;
				}
				return finishToken(offset, TokenType.Assign);
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
}
