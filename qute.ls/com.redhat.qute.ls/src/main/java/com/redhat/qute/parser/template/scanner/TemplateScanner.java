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
package com.redhat.qute.parser.template.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;
import com.redhat.qute.parser.scanner.Scanner;

/**
 * Qute Template scanner.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateScanner extends AbstractScanner<TokenType, ScannerState> {

	private static final Predicate<Integer> TAG_NAME_PREDICATE = ch -> {
		return Character.isLetter(ch);
	};

	public static Scanner<TokenType, ScannerState> createScanner(String input) {
		return createScanner(input, 0);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, ScannerState.WithinContent);
	}

	public static Scanner<TokenType, ScannerState> createScanner(String input, int initialOffset,
			ScannerState initialState) {
		return new TemplateScanner(input, initialOffset, initialState);
	}

	TemplateScanner(String input, int initialOffset, ScannerState initialState) {
		super(input, initialOffset, initialState, TokenType.Unknown, TokenType.EOS);
	}

	@Override
	protected TokenType internalScan() {
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, TokenType.EOS);
		}

		String errorMessage = null;
		switch (state) {

		case WithinContent: {
			if (stream.advanceIfChar('{')) {
				// A valid identifier must start with a digit, alphabet, underscore, comment
				// delimiter, cdata start delimiter or a tag command (e.g. # for sections)
				// see
				// https://github.com/quarkusio/quarkus/blob/7164bfa115d9096a3ba0b2929c98f89ac01c2dce/independent-projects/qute/core/src/main/java/io/quarkus/qute/Parser.java#L332
				if (stream.advanceIfChar('!')) {
					// Comment -> {! This is a comment !}
					state = ScannerState.WithinComment;
					return finishToken(offset, TokenType.StartComment);
				} else if (stream.advanceIfChar('|')) {
					// Unparsed Character Data -> {| <script>if(true){alert('Qute is
					// cute!')};</script> |}
					state = ScannerState.WithinCDATA;
					return finishToken(offset, TokenType.CDATATagOpen);
				} else if (stream.advanceIfChar('[')) {
					// Unparsed Character Data (old syntax) -> {[ <script>if(true){alert('Qute is
					// cute!')};</script> ]}
					state = ScannerState.WithinCDATAOld;
					return finishToken(offset, TokenType.CDATAOldTagOpen);
				} else if (stream.advanceIfChar('#')) {
					// Section (start) tag -> {#if
					state = ScannerState.AfterOpeningStartTag;
					return finishToken(offset, TokenType.StartTagOpen);
				} else if (stream.advanceIfChar('/')) {
					if (stream.advanceIfChar('}')) {
						// Section (end) tag with name optional syntax -> {/}
						state = ScannerState.WithinContent;
						return finishToken(offset, TokenType.EndTagSelfClose);
					}
					// Section (end) tag -> {/if}
					state = ScannerState.AfterOpeningEndTag;
					return finishToken(offset, TokenType.EndTagOpen);
				} else if (stream.advanceIfChar('@')) {
					// Parameter declaration -> {@org.acme.Foo foo}
					state = ScannerState.WithinParameterDeclaration;
					return finishToken(offset, TokenType.StartParameterDeclaration);
				} else {
					int ch = stream.peekChar();
					if (isValidIdentifierStart(ch)) {
						// Expression
						state = ScannerState.WithinExpression;
						return finishToken(offset, TokenType.StartExpression);
					} else {
						// Text node, increment position if needed
						if (!stream.eos()) {
							stream.advance(1);
						}
					}
				}
			}
			stream.advanceUntilChar('{');
			return finishToken(offset, TokenType.Content);
		}

		case WithinComment: {
			if (stream.advanceIfChars('!', '}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndComment);
			}
			stream.advanceUntilChars('!', '}');
			return finishToken(offset, TokenType.Comment);
		}

		case WithinCDATA: {
			if (stream.advanceIfChars('|', '}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.CDATATagClose);
			}
			stream.advanceUntilChars('|', '}');
			return finishToken(offset, TokenType.CDATAContent);
		}

		case WithinCDATAOld: {
			if (stream.advanceIfChars(']', '}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.CDATAOldTagClose);
			}
			stream.advanceUntilChars(']', '}');
			return finishToken(offset, TokenType.CDATAContent);
		}

		case WithinParameterDeclaration: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}
			
			stream.advanceUntilChar('}', '{');
			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndParameterDeclaration);
			}
			if (stream.peekChar() == '{') {
				state = ScannerState.WithinContent;
				return internalScan();
			}
			return finishToken(offset, TokenType.ParameterDeclaration);
		}

		case WithinExpression: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndExpression);
			}
			stream.advanceUntilChar('}', '"', '\'');
			if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
				stream.advance(1);
				state = ScannerState.WithinString;
				return finishToken(stream.pos() - 1, TokenType.StartString);
			}
			return internalScan(); // (offset, TokenType.Expression);
		}

		case WithinString: {
			if (stream.advanceIfAnyOfChars('"', '\'')) {
				state = ScannerState.WithinExpression;
				return finishToken(offset, TokenType.EndString);
			}
			stream.advanceUntilChar('"', '\'');
			return finishToken(offset, TokenType.String);
		}

		case AfterOpeningStartTag: {
			if (hasNextTagName()) {
				state = ScannerState.WithinTag;
				return finishToken(offset, TokenType.StartTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace, "Tag name must directly follow the open bracket.");
			}
			state = ScannerState.WithinTag;
			if (stream.advanceUntilCharOrNewTag('}')) {
				if (stream.peekChar() == '{') {
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);
		}

		case AfterOpeningEndTag:
			if (hasNextTagName()) {
				state = ScannerState.WithinEndTag;
				return finishToken(offset, TokenType.EndTag);
			}
			if (stream.skipWhitespace()) { // white space is not valid here
				return finishToken(offset, TokenType.Whitespace, "Tag name must directly follow the open bracket.");
			}
			state = ScannerState.WithinEndTag;
			if (stream.advanceUntilCharOrNewTag('}')) {
				if (stream.peekChar() == '{') {
					state = ScannerState.WithinContent;
				}
				return internalScan();
			}
			return finishToken(offset, TokenType.Unknown);

		case WithinEndTag:
			if (stream.skipWhitespace()) { // white space is valid here
				return finishToken(offset, TokenType.Whitespace);
			}
			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.EndTagClose);
			}
			if (stream.advanceUntilChar('{')) {
				state = ScannerState.WithinContent;
				return internalScan();
			}
			return finishToken(offset, TokenType.Whitespace);

		case WithinTag: {
			if (stream.skipWhitespace()) {
				return finishToken(offset, TokenType.Whitespace);
			}

			if (stream.advanceIfChar('/')) {
				state = ScannerState.WithinTag;
				if (stream.advanceIfChar('}')) {
					state = ScannerState.WithinContent;
					return finishToken(offset, TokenType.StartTagSelfClose);
				}
				return finishToken(offset, TokenType.Unknown);
			}
			if (stream.advanceIfChar('}')) {
				state = ScannerState.WithinContent;
				return finishToken(offset, TokenType.StartTagClose);
			}

			stream.advanceUntilChar('}', '/', '"', '\'', ' ');
			int c = stream.peekChar();
			if (c == '"' || c == '\'') {
				stream.advance(1);
				stream.advanceUntilChar(c);
				if (stream.peekChar() == c) {
					stream.advance(1);
				}
			}

			return finishToken(offset, TokenType.ParameterTag);
		}

		default:
		}
		stream.advance(1);
		return

		finishToken(offset, TokenType.Unknown, errorMessage);
	}

	private static boolean isValidIdentifierStart(int ch) {
		return Character.isDigit(ch) || Character.isAlphabetic(ch) || ch == '_';
	}

	private boolean hasNextTagName() {
		return stream.advanceWhileChar(TAG_NAME_PREDICATE) > 0;
	}

}
