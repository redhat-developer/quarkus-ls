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
 * Abstract base implementation of {@link Scanner}.
 *
 * <p>
 * This class provides common infrastructure for scanners that tokenize input
 * text into high-level structural tokens.
 * </p>
 *
 * <p>
 * It manages:
 * <ul>
 * <li>input navigation through a {@link MultiLineStream}</li>
 * <li>token offsets, lengths and text extraction</li>
 * <li>scanner state handling</li>
 * <li>fault-tolerant behavior when a scanner implementation does not advance
 * the input</li>
 * </ul>
 * </p>
 *
 * <p>
 * Concrete implementations must implement {@link #internalScan()} to detect
 * tokens and advance the input stream accordingly.
 * </p>
 *
 * <p>
 * This base class guarantees that each call to {@link #scan()} always makes
 * progress unless the end-of-stream token is reached. If an implementation
 * fails to advance the input, the scanner automatically consumes one character
 * and returns an {@code unknown} token.
 * </p>
 *
 * @param <T> token type enum
 * @param <S> scanner state enum
 */
public abstract class AbstractScanner<T, S> implements Scanner<T, S> {

	/**
	 * Stream used to navigate through the input text.
	 */
	protected final MultiLineStream stream;

	/**
	 * Token type used when an unexpected or invalid token is encountered.
	 */
	protected final T unknownTokenType;

	/**
	 * Token type representing end of stream.
	 */
	protected final T eosTokenType;

	/**
	 * Current scanner state.
	 */
	protected S state;

	private int tokenOffset;
	private T tokenType;
	private String tokenError;

	protected AbstractScanner(String input, int initialOffset, S initialState, T unknownTokenType, T eosTokenType) {
		this(input, initialOffset, input.length(), initialState, unknownTokenType, eosTokenType);
	}

	protected AbstractScanner(String input, int initialOffset, int endOffset, S initialState, T unknownTokenType,
			T eosTokenType) {
		this.stream = new MultiLineStream(input, initialOffset, endOffset);
		this.unknownTokenType = unknownTokenType;
		this.eosTokenType = eosTokenType;
		this.state = initialState;
		this.tokenOffset = 0;
		this.tokenType = unknownTokenType;
	}

	/**
	 * Advances to the next token and returns its type.
	 *
	 * <p>
	 * This method delegates token detection to {@link #internalScan()}.
	 * </p>
	 *
	 * <p>
	 * To ensure fault tolerance, if the scanner implementation does not advance the
	 * input stream and the end-of-stream token has not been reached, this method
	 * automatically consumes one character and returns an {@code unknown} token.
	 * </p>
	 *
	 * @return the type of the scanned token
	 */
	@Override
	public final T scan() {
		int offset = stream.pos();
		S oldState = state;

		T token = internalScan();

		if (token != eosTokenType && offset == stream.pos()) {
			log("Scanner.scan has not advanced at offset " + offset + ", state before: " + oldState + ", after: "
					+ state);
			stream.advance(1);
			return finishToken(offset, unknownTokenType);
		}
		return token;
	}

	/**
	 * Performs the actual scanning logic.
	 *
	 * <p>
	 * Implementations are responsible for:
	 * <ul>
	 * <li>analyzing the input at the current stream position</li>
	 * <li>advancing the stream as needed</li>
	 * <li>calling {@link #finishToken(int, Object)} or
	 * {@link #finishToken(int, Object, String)}</li>
	 * </ul>
	 * </p>
	 *
	 * @return the type of the detected token
	 */
	protected abstract T internalScan();

	/**
	 * Finalizes the current token.
	 *
	 * @param offset token starting offset
	 * @param type   token type
	 * @return the token type
	 */
	protected T finishToken(int offset, T type) {
		return finishToken(offset, type, null);
	}

	/**
	 * Finalizes the current token with an optional error message.
	 *
	 * @param offset       token starting offset
	 * @param type         token type
	 * @param errorMessage error message associated with the token, or {@code null}
	 * @return the token type
	 */
	protected T finishToken(int offset, T type, String errorMessage) {
		this.tokenType = type;
		this.tokenOffset = offset;
		this.tokenError = errorMessage;
		return type;
	}

	@Override
	public T getTokenType() {
		return tokenType;
	}

	@Override
	public int getTokenOffset() {
		return tokenOffset;
	}

	@Override
	public int getTokenLength() {
		return stream.pos() - tokenOffset;
	}

	@Override
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
