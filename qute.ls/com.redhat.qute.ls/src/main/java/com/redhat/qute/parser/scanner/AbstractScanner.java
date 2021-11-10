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
package com.redhat.qute.parser.scanner;

/**
 * Abstract scanner.
 * 
 * @author Angelo ZERR
 *
 * @param <T>
 * @param <S>
 */
public abstract class AbstractScanner<T, S> implements Scanner<T, S> {

	protected final MultiLineStream stream;

	private final T unknownTokenType;

	private final T eosTokenType;

	protected S state;

	private int tokenOffset;

	private T tokenType;
	private String tokenError;

	protected AbstractScanner(String input, int initialOffset, S initialState, T unknownTokenType, T eosTokenType) {
		this(input, initialOffset, input.length(), initialState, unknownTokenType, eosTokenType);
	}

	protected AbstractScanner(String input, int initialOffset, int endOffset, S initialState, T unknownTokenType,
			T eosTokenType) {
		stream = new MultiLineStream(input, initialOffset, endOffset);
		this.unknownTokenType = unknownTokenType;
		this.eosTokenType = eosTokenType;
		state = initialState;
		tokenOffset = 0;
		tokenType = unknownTokenType;
	}

	@Override
	public T scan() {
		int offset = stream.pos();
		S oldState = state;
		T token = internalScan();
		if (token != eosTokenType && offset == stream.pos()) {
			log("Scanner.scan has not advanced at offset " + offset + ", state before: " + oldState + " after: "
					+ state);
			stream.advance(1);
			return finishToken(offset, unknownTokenType);
		}
		return token;
	}

	protected abstract T internalScan();

	protected T finishToken(int offset, T type) {
		return finishToken(offset, type, null);
	}

	protected T finishToken(int offset, T type, String errorMessage) {
		tokenType = type;
		tokenOffset = offset;
		tokenError = errorMessage;
		return type;
	}

	@Override
	public T getTokenType() {
		return tokenType;
	}

	/**
	 * Starting offset position of the current token
	 * 
	 * @return Starting offset position of the current token
	 */
	@Override
	public int getTokenOffset() {
		return tokenOffset;
	}

	@Override
	public int getTokenLength() {
		return stream.pos() - tokenOffset;
	}

	@Override
	/**
	 * Ending offset position of the current token
	 * 
	 * @return Ending offset position of the current token
	 */
	public int getTokenEnd() {
		return stream.pos();
	}

	@Override
	public String getTokenText() {
		return stream.getSource().substring(tokenOffset, stream.pos());
	}

	@Override
	public S getScannerState() {
		return state;
	}

	@Override
	public String getTokenError() {
		return tokenError;
	}

	private void log(String message) {
		// System.err.println(message);
	}
}
