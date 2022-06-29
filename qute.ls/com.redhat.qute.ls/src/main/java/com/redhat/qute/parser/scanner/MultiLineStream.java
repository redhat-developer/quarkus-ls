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
 * Multi line stream.
 *
 */
public class MultiLineStream {

	private static final Predicate<Integer> WHITESPACE_PREDICATE = ch -> {
		return ch == _WSP || ch == _TAB || ch == _NWL || ch == _LFD || ch == _CAR;
	};

	private final String source;
	private final int len;
	private int position;

	public MultiLineStream(String source, int position, int len) {
		this.source = source;
		this.len = Math.min(len, source.length());
		this.position = position;
	}

	public boolean eos() {
		return this.len <= this.position;
	}

	public String getSource() {
		return this.source;
	}

	public int pos() {
		return this.position;
	}

	public void goBackTo(int pos) {
		this.position = pos;
	}

	public void goBack(int n) {
		this.position -= n;
	}

	public void advance(int n) {
		this.position += n;
	}

	public void goToEnd() {
		this.position = len;
	}

	public int peekChar() {
		return peekChar(0);
	}

	/**
	 * Peeks at next char at position + n. peekChar() == peekChar(0)
	 *
	 * @param n
	 * @return
	 */
	public int peekChar(int n) {
		int pos = this.position + n;
		if (pos >= len) {
			return 0;
		}
		return this.source.codePointAt(pos);
	}

	/**
	 * Peeks at the char at position 'offset' of the whole document
	 *
	 * @param offset
	 * @return
	 */
	public int peekCharAtOffset(int offset) {
		if (offset >= len || offset < 0) {
			return 0;
		}
		return this.source.codePointAt(offset);
	}

	public boolean advanceIfChar(int ch) {
		if (ch == peekChar()) {
			this.position++;
			return true;
		}
		return false;
	}

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
	 * Advances stream.position no matter what until it hits ch or eof(this.len)
	 *
	 * @return boolean: was the char found
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
	 * Advances stream.position no matter what until it hits ch or eof(this.len)
	 *
	 * @return boolean: was the char found
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
	 * Will advance until any of the provided chars are encountered
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
	 * Advances until it reaches a whitespace character
	 */
	public boolean skipWhitespace() {
		int n = this.advanceWhileChar(WHITESPACE_PREDICATE);
		return n > 0;
	}

	public int advanceWhileChar(Predicate<Integer> condition) {
		int posNow = this.position;
		while (this.position < this.len && condition.test(peekChar())) {
			this.position++;
		}
		return this.position - posNow;
	}

	/**
	 * Will advance the stream position until ch or '{'
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