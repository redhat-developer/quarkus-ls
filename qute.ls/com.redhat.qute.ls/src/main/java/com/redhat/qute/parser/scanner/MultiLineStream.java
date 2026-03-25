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

import static com.redhat.qute.parser.scanner.Constants._CAR;
import static com.redhat.qute.parser.scanner.Constants._LFD;
import static com.redhat.qute.parser.scanner.Constants._NWL;
import static com.redhat.qute.parser.scanner.Constants._TAB;
import static com.redhat.qute.parser.scanner.Constants._WSP;

import java.util.function.Predicate;

/**
 * Provides a mutable cursor over a multi-line text input.
 *
 * <p>
 * This class is a low-level utility used by scanners and parsers to navigate
 * through a character stream efficiently.
 * </p>
 */
public class MultiLineStream {

	private static final Predicate<Integer> WHITESPACE_PREDICATE = ch -> {
		return ch == _WSP || ch == _TAB || ch == _NWL || ch == _LFD || ch == _CAR;
	};

	private static final Predicate<Integer> WHITESPACE_ONLY_PREDICATE = ch -> ch == _WSP || ch == _TAB;

	private final String source;
	private final int len;
	private int position;

	/**
	 * Creates a new MultiLineStream over the given source text.
	 *
	 * @param source   the source text
	 * @param position the initial cursor position
	 * @param len      the maximum position (exclusive)
	 */
	public MultiLineStream(String source, int position, int len) {
		this.source = source;
		this.len = Math.min(len, source.length());
		this.position = position;
	}

	/**
	 * Returns true if the cursor has reached the end of the stream.
	 *
	 * @return true if end of stream
	 */
	public boolean eos() {
		return this.len <= this.position;
	}

	/**
	 * Returns the underlying source text.
	 *
	 * @return the source text
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * Returns the current cursor position.
	 *
	 * @return current cursor position
	 */
	public int pos() {
		return this.position;
	}

	/**
	 * Moves the cursor to a specific absolute position.
	 *
	 * @param pos the target position
	 */
	public void goBackTo(int pos) {
		this.position = pos;
	}

	/**
	 * Moves the cursor backward by the given number of characters.
	 *
	 * @param n number of characters to move back
	 */
	public void goBack(int n) {
		this.position -= n;
	}

	/**
	 * Advances the cursor forward by the given number of characters.
	 *
	 * @param n number of characters to advance
	 */
	public void advance(int n) {
		this.position += n;
	}

	/**
	 * Moves the cursor to the end of the stream.
	 */
	public void goToEnd() {
		this.position = len;
	}

	/**
	 * Returns the code point at the current cursor position without advancing.
	 *
	 * @return the current character code point, or 0 if end of stream
	 */
	public int peekChar() {
		return peekChar(0);
	}

	/**
	 * Returns the code point at position + n without advancing.
	 *
	 * @param n offset from current position
	 * @return the character code point, or 0 if out of bounds
	 */
	public int peekChar(int n) {
		int pos = this.position + n;
		if (pos >= len) {
			return 0;
		}
		return this.source.codePointAt(pos);
	}

	/**
	 * Returns the code point at an absolute offset in the source.
	 *
	 * @param offset absolute offset
	 * @return the character code point, or 0 if out of bounds
	 */
	public int peekCharAtOffset(int offset) {
		if (offset >= len || offset < 0) {
			return 0;
		}
		return this.source.codePointAt(offset);
	}

	/**
	 * Advances the cursor by one if the current character matches the given value.
	 *
	 * @param ch the expected character
	 * @return true if matched and advanced, false otherwise
	 */
	public boolean advanceIfChar(int ch) {
		if (ch == peekChar()) {
			this.position++;
			return true;
		}
		return false;
	}

	/**
	 * Advances the cursor if the upcoming characters exactly match the given
	 * sequence.
	 *
	 * @param ch the sequence of expected characters
	 * @return true if matched and advanced, false otherwise
	 */
	public boolean advanceIfChars(int[] ch) {
		int i;
		if (this.position + ch.length > this.len) {
			return false;
		}
		for (i = 0; i < ch.length; i++) {
			if (peekChar(i) != ch[i]) {
				return false;
			}
		}
		this.advance(i);
		return true;
	}

	/**
	 * Advances the cursor if the current character matches any of the provided
	 * characters.
	 *
	 * @param ch the array of characters to test
	 * @return true if any matched and advanced, false otherwise
	 */
	public boolean advanceIfAnyOfChars(char[] ch) {
		int i;
		if (this.position + 1 > this.len) {
			return false;
		}
		for (i = 0; i < ch.length; i++) {
			if (advanceIfChar(ch[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Advances the cursor until any of the given characters in the array is reached
	 * or the end of stream.
	 *
	 * @param ch array of target characters
	 * @return true if a character was found, false if end of stream
	 */
	public boolean advanceUntilChar(int[] ch) {
		while (this.position < this.len) {
			for (int c : ch) {
				if (peekChar() == c) {
					return true;
				}
			}
			this.advance(1);
		}
		return false;
	}

	/**
	 * Advances the cursor until the given character is reached or the end of
	 * stream.
	 *
	 * @param ch target character
	 * @return true if found, false if end of stream
	 */
	public boolean advanceUntilChar(int ch) {
		while (this.position < this.len) {
			if (peekChar() == ch) {
				return true;
			}
			this.advance(1);
		}
		return false;
	}

	/**
	 * Advances the cursor until any of the specified characters is found.
	 *
	 * @param ch array of target characters
	 * @return true if any character is found, false otherwise
	 */
	public boolean advanceUntilAnyOfChars(int[] ch) {
		while (this.position < this.len) {
			for (int i = 0; i < ch.length; i++) {
				if (peekChar() == ch[i]) {
					return true;
				}
			}

			this.advance(1);
		}
		return false;
	}

	/**
	 * Advances until the specified sequence of characters is found.
	 *
	 * @param ch target character sequence
	 * @return true if sequence found, false if end of stream
	 */
	public boolean advanceUntilChars(int[] ch) {
		while (this.position + ch.length <= this.len) {
			int i = 0;
			for (; i < ch.length && peekChar(i) == ch[i]; i++) {
			}
			if (i == ch.length) {
				return true;
			}
			this.advance(1);
		}
		this.goToEnd();
		return false;
	}

	/**
	 * Skips over all whitespace characters (spaces, tabs, newlines, CR/LF).
	 *
	 * @return true if at least one whitespace character was skipped
	 */
	public boolean skipWhitespace() {
		int n = this.advanceWhileChar(WHITESPACE_PREDICATE);
		return n > 0;
	}

	/**
	 * Skips over spaces and tabs only.
	 *
	 * @return true if at least one space or tab was skipped
	 */
	public boolean skipWhitespaceOnly() {
		int n = this.advanceWhileChar(WHITESPACE_ONLY_PREDICATE);
		return n > 0;
	}

	/**
	 * Skips a single newline character, handling both LF and CRLF sequences.
	 *
	 * @return true if a newline was skipped
	 */
	public boolean skipNewline() {
		if (peekChar() == _NWL) {
			advance(1);
			return true;
		}
		if (peekChar() == _CAR) {
			advance(1);
			if (peekChar() == _NWL) { // handle CRLF
				advance(1);
			}
			return true;
		}
		return false;
	}

	/**
	 * Advances the cursor while the given condition is true.
	 *
	 * @param condition predicate applied to characters
	 * @return number of characters advanced
	 */
	public int advanceWhileChar(Predicate<Integer> condition) {
		int posNow = this.position;
		while (this.position < this.len && condition.test(peekChar())) {
			this.position++;
		}
		return this.position - posNow;
	}

	/**
	 * Advances until the given character or '{' is reached.
	 *
	 * @param ch target character
	 * @return true if the target or '{' is found, false if end of stream
	 */
	public boolean advanceUntilCharOrNewTag(int ch) {
		while (this.position < this.len) {
			if (peekChar() == ch || peekChar() == '{') {
				return true;
			}
			this.advance(1);
		}
		return false;
	}
}
