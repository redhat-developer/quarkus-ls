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
 * Generic scanner interface for tokenizing input text.
 *
 * <p>
 * This scanner identifies high-level structural tokens such as content blocks,
 * delimiters, and section boundaries.
 * </p>
 *
 * <p>
 * It deliberately does <strong>not</strong> tokenize the internal structure of
 * delimited regions. The content of such regions may be scanned separately by a
 * dedicated scanner and only when needed.
 * </p>
 *
 * <p>
 * This design enables:
 * <ul>
 * <li><b>Lazy parsing</b> – inner content is parsed on demand</li>
 * <li><b>Cancellation-friendly parsing</b> – avoids unnecessary work when
 * scanning is interrupted</li>
 * <li><b>Fault-tolerant scanning</b> – scanning continues even when malformed
 * input is encountered (e.g. missing closing delimiters)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Syntax errors are reported through token error information rather than
 * interrupting the scanning process.
 * </p>
 *
 * <p>
 * This interface is intended to be reusable for different syntaxes such as
 * template languages, markup languages (e.g. HTML), or configuration formats
 * (e.g. YAML).
 * </p>
 *
 * @param <T> Token type enum
 * @param <S> Scanner state enum
 */
public interface Scanner<T, S> {

	/**
	 * Advances to the next token and returns its type.
	 *
	 * <p>
	 * The scanner identifies structural tokens only. The internal structure of
	 * delimited regions is not tokenized by this scanner.
	 * </p>
	 *
	 * <h3>Example:</h3>
	 *
	 * <pre>
	 * Input: "{value}"
	 *
	 * scan();  // StartDelimiter
	 * scan();  // EndDelimiter
	 * scan();  // EOS
	 * </pre>
	 *
	 * @return the type of the token that was just scanned
	 */
	T scan();

	/**
	 * Returns the type of the current token.
	 *
	 * @return the type of the current token
	 */
	T getTokenType();

	/**
	 * Returns the zero-based starting offset of the current token.
	 *
	 * @return zero-based starting offset of the token
	 */
	int getTokenOffset();

	/**
	 * Returns the length of the current token in characters.
	 *
	 * @return length of the current token
	 */
	int getTokenLength();

	/**
	 * Returns the exclusive ending offset of the current token.
	 *
	 * @return exclusive ending offset of the token
	 */
	int getTokenEnd();

	/**
	 * Returns the text content of the current token.
	 *
	 * <p>
	 * For delimiter-related tokens, this method returns only the delimiter text,
	 * not the content inside the delimited region.
	 * </p>
	 *
	 * @return the text content of the current token
	 */
	String getTokenText();

	/**
	 * Returns the error message for the current token, if any.
	 *
	 * <p>
	 * The scanner is fault-tolerant and continues scanning even when malformed
	 * input is encountered.
	 * </p>
	 *
	 * @return error message for the token, or {@code null} if the token is valid
	 */
	String getTokenError();

	/**
	 * Returns the current internal state of the scanner.
	 *
	 * @return the current scanner state
	 */
	S getScannerState();
}
