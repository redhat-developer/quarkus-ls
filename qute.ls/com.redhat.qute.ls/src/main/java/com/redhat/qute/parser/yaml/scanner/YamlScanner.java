/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.parser.yaml.scanner;

import java.util.function.Predicate;

import com.redhat.qute.parser.scanner.AbstractScanner;
import com.redhat.qute.parser.scanner.Scanner;

/**
 * YAML scanner (fault-tolerant).
 */
public class YamlScanner extends AbstractScanner<YamlTokenType, YamlScannerState> {

	private static final int[] QUOTE = new int[] { '"', '\'', };
	private static final char[] QUOTE_C = new char[] { '"', '\'', };
	private static final int[] NEWLINE_CHARS = new int[] { '\n', '\r' };
	private static final int[] STRUCTURAL_CHARS = new int[] { ':', '-', '#', '[', ']', '{', '}', ',', '\n', '\r' };
	private static final int[] VALUE_END_CHARS = new int[] { '\n', '\r', '#', ',', ']', '}' };
	private static final int[] FLOW_END_CHARS = new int[] { ',', ']', '}', '\n', '\r' };

	private static final Predicate<Integer> KEY_CHAR_PREDICATE = ch -> {
		return ch != ':' && ch != '\n' && ch != '\r' && ch != '#' && ch != '[' && ch != ']' && ch != '{' && ch != '}'
				&& ch != ',';
	};

	private static final Predicate<Integer> VALUE_CHAR_PREDICATE = ch -> {
		return ch != '\n' && ch != '\r' && ch != '#' && ch != ',' && ch != ']' && ch != '}';
	};

	private static final Predicate<Integer> VALUE_CHAR_PREDICATE2 = ch -> {
		return ch != '\n' && ch != '\r' && ch != '#' && ch != ',' && ch != ']' && ch != '}' && ch != ':';
	};

	public static Scanner<YamlTokenType, YamlScannerState> createScanner(String input) {
		return createScanner(input, 0);
	}

	public static Scanner<YamlTokenType, YamlScannerState> createScanner(String input, int initialOffset) {
		return createScanner(input, initialOffset, YamlScannerState.WithinContent);
	}

	public static Scanner<YamlTokenType, YamlScannerState> createScanner(String input, int initialOffset,
			YamlScannerState initialState) {
		return new YamlScanner(input, initialOffset, initialState);
	}

	private int flowSequenceDepth = 0;
	private int flowMappingDepth = 0;
	private YamlScannerState returnStateAfterString;
	private char stringQuote;
	private YamlTokenType previousNoWhitespacesToken;

	YamlScanner(String input, int initialOffset, YamlScannerState initialState) {
		super(input, initialOffset, initialState, YamlTokenType.Unknown, YamlTokenType.EOS);
	}

	@Override
	protected YamlTokenType internalScan() {
		if (getTokenType() != YamlTokenType.Whitespace) {
			previousNoWhitespacesToken = getTokenType();
		}
		int offset = stream.pos();
		if (stream.eos()) {
			return finishToken(offset, YamlTokenType.EOS);
		}

		String errorMessage = null;

		switch (state) {

		case WithinContent: {
			// Handle comments
			if (stream.advanceIfChar('#')) {
				state = YamlScannerState.WithinComment;
				return finishToken(offset, YamlTokenType.StartComment);
			}

			// Handle whitespace (but not newlines)
			if (stream.peekChar() == ' ' || stream.peekChar() == '\t') {
				while (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
					stream.advance(1);
				}
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			// Handle newlines
			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
					stream.advance(1);
				}
				state = YamlScannerState.WithinContent;
				return finishToken(offset, YamlTokenType.Newline);
			}

			// Handle flow sequence start
			if (stream.advanceIfChar('[')) {
				flowSequenceDepth++;
				state = YamlScannerState.WithinFlowSequence;
				return finishToken(offset, YamlTokenType.ArrayOpen);
			}

			// Handle flow mapping start
			if (stream.advanceIfChar('{')) {
				flowMappingDepth++;
				state = YamlScannerState.WithinFlowMapping;
				return finishToken(offset, YamlTokenType.ObjectOpen);
			}

			// Handle list dash
			if (stream.peekChar() == '-') {
				int next = stream.peekChar(1);
				if (next == ' ' || next == '\t' || next == '\n' || next == '\r' || next == 0) {
					stream.advance(1);
					state = YamlScannerState.AfterDash;
					return finishToken(offset, YamlTokenType.Dash);
				}
			}

			// Handle quoted strings
			YamlTokenType startStringToken = handleStartString(offset);
			if (startStringToken != null) {
				return startStringToken;
			}

			// Handle key or value
			return scanKeyOrValue(offset);
		}

		case WithinComment: {
			int ch = stream.peekChar();
			if (ch == '\n' || ch == '\r') {
				// Newline after comment
				stream.advance(1);
				if (ch == '\r' && stream.peekChar() == '\n') {
					stream.advance(1); // handle CRLF
				}
				state = YamlScannerState.WithinContent;
				return finishToken(offset, YamlTokenType.Newline);
			}

			// consume comment content until newline
			int startCommentOffset = stream.pos();
			stream.advanceUntilChar(NEWLINE_CHARS);
			return finishToken(startCommentOffset, YamlTokenType.Comment);
		}

		case WithinString: {
			int start = stream.pos();

			while (!stream.eos()) {
				int ch = stream.peekChar();

				// end of string
				if (ch == stringQuote) {
					if (stream.pos() > start) {
						return finishToken(start, YamlTokenType.String);
					}
					stream.advance(1);
					state = returnStateAfterString;
					return finishToken(offset, YamlTokenType.EndString);
				}

				// YAML single-quoted escape: '' -> '
				if (stringQuote == '\'' && ch == '\'' && stream.peekChar(1) == '\'') {
					stream.advance(2);
					continue;
				}

				stream.advance(1);
			}

			// unterminated string (fault tolerant)
			state = returnStateAfterString;
			return finishToken(start, YamlTokenType.String);
		}

		case AfterKey: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			if (stream.advanceIfChar(':')) {
				state = YamlScannerState.AfterColon;
				return finishToken(offset, YamlTokenType.Colon);
			}

			// Fault-tolerant: missing colon
			state = YamlScannerState.WithinContent;
			return finishToken(offset, YamlTokenType.Unknown, "Expected ':' after key");
		}

		case AfterColon: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			// Handle newline after colon (nested structure)
			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				state = YamlScannerState.WithinContent;
				return internalScan();
			}

			// Handle comment after colon
			if (stream.peekChar() == '#') {
				state = YamlScannerState.WithinContent;
				return internalScan();
			}

			// Parse value
			state = YamlScannerState.WithinValue;
			return scanValue(offset, true);
		}

		case AfterDash: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			// Handle newline after dash (nested structure)
			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				state = YamlScannerState.WithinContent;
				return internalScan();
			}

			// Handle comment after dash
			if (stream.peekChar() == '#') {
				state = YamlScannerState.WithinContent;
				return internalScan();
			}

			// Parse value
			state = YamlScannerState.WithinValue;
			return scanValue(offset, false);
		}

		case WithinValue: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			// After value, we go back to content
			state = YamlScannerState.WithinContent;
			return internalScan();
		}

		case WithinFlowSequence: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			int ch = stream.peekChar();

			// Nested sequence
			if (ch == '[') {
				stream.advance(1);
				flowSequenceDepth++;
				return finishToken(offset, YamlTokenType.ArrayOpen);
			}

			// Close sequence
			if (ch == ']') {
				stream.advance(1);
				flowSequenceDepth--;
				
				// Determine next state based on what we're still in
				if (flowSequenceDepth > 0) {
					// Still in nested flow sequence
					state = YamlScannerState.WithinFlowSequence;
				} else if (flowMappingDepth > 0) {
					// Back to flow mapping
					state = YamlScannerState.WithinFlowMapping;
				} else {
					// Back to regular content
					state = YamlScannerState.WithinContent;
				}
				return finishToken(offset, YamlTokenType.ArrayClose);
			}

			// Comma
			if (ch == ',') {
				stream.advance(1);
				return finishToken(offset, YamlTokenType.Comma);
			}

			// Nested mapping
			if (ch == '{') {
				stream.advance(1);
				flowMappingDepth++;
				state = YamlScannerState.WithinFlowMapping;
				return finishToken(offset, YamlTokenType.ObjectOpen);
			}

			// Comment
			if (ch == '#') {
				state = YamlScannerState.WithinComment;
				stream.advance(1);
				return finishToken(offset, YamlTokenType.StartComment);
			}

			// Newline
			if (ch == '\n' || ch == '\r') {
				stream.advance(1);
				if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
					stream.advance(1);
				}
				return finishToken(offset, YamlTokenType.Newline);
			}

			// Handle quoted strings
			YamlTokenType startStringToken = handleStartString(offset);
			if (startStringToken != null) {
				return startStringToken;
			}

			// Anything else is a value
			return scanFlowValue(offset, YamlScannerState.WithinFlowSequence);
		}

		case WithinFlowMapping: {
			if (stream.skipWhitespaceOnly()) {
				return finishToken(offset, YamlTokenType.Whitespace);
			}

			if (stream.advanceIfChar('}')) {
				flowMappingDepth--;
				
				// Determine next state based on what we're still in
				if (flowMappingDepth > 0) {
					// Still in nested flow mapping
					state = YamlScannerState.WithinFlowMapping;
				} else if (flowSequenceDepth > 0) {
					// Back to flow sequence
					state = YamlScannerState.WithinFlowSequence;
				} else {
					// Back to regular content
					state = YamlScannerState.WithinContent;
				}
				return finishToken(offset, YamlTokenType.ObjectClose);
			}

			if (stream.advanceIfChar(',')) {
				return finishToken(offset, YamlTokenType.Comma);
			}

			if (stream.advanceIfChar(':')) {
				return finishToken(offset, YamlTokenType.Colon);
			}

			if (stream.peekChar() == '\n' || stream.peekChar() == '\r') {
				stream.advance(1);
				if (stream.peekChar(-1) == '\r' && stream.peekChar() == '\n') {
					stream.advance(1);
				}
				return finishToken(offset, YamlTokenType.Newline);
			}

			if (stream.peekChar() == '#') {
				state = YamlScannerState.WithinComment;
				stream.advance(1);
				return finishToken(offset, YamlTokenType.StartComment);
			}

			// Handle nested structures
			if (stream.peekChar() == '[') {
				stream.advance(1);
				flowSequenceDepth++;
				state = YamlScannerState.WithinFlowSequence;
				return finishToken(offset, YamlTokenType.ArrayOpen);
			}

			if (stream.peekChar() == '{') {
				stream.advance(1);
				flowMappingDepth++;
				return finishToken(offset, YamlTokenType.ObjectOpen);
			}

			// Handle quoted strings
			YamlTokenType startStringToken = handleStartString(offset);
			if (startStringToken != null) {
				return startStringToken;
			}

			// Parse key or value
			return scanFlowValue(offset, YamlScannerState.WithinFlowMapping);
		}

		default:
			stream.advance(1);
			return finishToken(offset, YamlTokenType.Unknown, errorMessage);
		}
	}

	private YamlTokenType scanKeyOrValue(int offset) {
		int start = stream.pos();

		// Skip leading whitespace
		while (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
			stream.advance(1);
		}

		// Check for quoted string
		if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
			// Remember current flow state
			returnStateAfterString = state;
			stringQuote = (char) stream.peekChar();
			stream.advance(1);
			state = YamlScannerState.WithinString;
			return finishToken(stream.pos() - 1, YamlTokenType.StartString);
		}

		// Scan until we hit a structural character
		int consumed = stream.advanceWhileChar(KEY_CHAR_PREDICATE);

		if (consumed == 0 && !stream.eos()) {
			stream.advance(1);
			return finishToken(offset, YamlTokenType.Unknown);
		}

		// Check what comes after to determine if it's a key or value
		int savePos = stream.pos();
		while (!stream.eos() && (stream.peekChar() == ' ' || stream.peekChar() == '\t')) {
			stream.advance(1);
		}

		boolean isKey = stream.peekChar() == ':';
		stream.goBackTo(savePos);

		if (isKey) {
			state = YamlScannerState.AfterKey;
			return finishToken(offset, YamlTokenType.Key);
		} else {
			String text = stream.getSource().substring(offset, stream.pos()).trim();
			YamlTokenType valueType = classifyValue(text);
			state = YamlScannerState.WithinValue;
			return finishToken(offset, valueType);
		}
	}

	private YamlTokenType scanValue(int offset, boolean colonAllowed) {
		// Check for quoted string
		if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
			// Remember current flow state
			returnStateAfterString = state;
			stringQuote = (char) stream.peekChar();
			stream.advance(1);
			state = YamlScannerState.WithinString;
			return finishToken(stream.pos() - 1, YamlTokenType.StartString);
		}

		// Check for nested list
		if (stream.peekChar() == '-') {
			int next = stream.peekChar(1);
			if (next == ' ' || next == '\t' || next == '\n' || next == '\r' || next == 0) {
				state = YamlScannerState.WithinContent;
				return internalScan();
			}
		}

		// Check for flow collections
		if (stream.peekChar() == '[') {
			state = YamlScannerState.WithinContent;
			return internalScan();
		}

		if (stream.peekChar() == '{') {
			state = YamlScannerState.WithinContent;
			return internalScan();
		}

		// Scan the value
		int consumed = stream.advanceWhileChar(colonAllowed ? VALUE_CHAR_PREDICATE : VALUE_CHAR_PREDICATE2);
		if (stream.peekChar() == ':') {
			state = YamlScannerState.AfterKey;
			return finishToken(offset, YamlTokenType.Key);
		}
		if (consumed == 0) {
			state = YamlScannerState.WithinContent;
			return internalScan();
		}

		String text = stream.getSource().substring(offset, stream.pos()).trim();
		YamlTokenType valueType = classifyValue(text);
		state = YamlScannerState.WithinValue;
		return finishToken(offset, valueType);
	}

	private YamlTokenType scanFlowValue(int offset, YamlScannerState returnState) {
		// Scan until we hit a flow terminator
		int consumed = stream.advanceWhileChar(ch -> {
			return ch != ',' && ch != ']' && ch != '}' && ch != '\n' && ch != '\r' && ch != '#' && ch != ':';
		});

		if (consumed == 0 && !stream.eos()) {
			stream.advance(1);
			return finishToken(offset, YamlTokenType.Unknown);
		}

		if (returnState == YamlScannerState.WithinFlowMapping) {
			// In flow mapping, determine if this is a key or value
			// It's a key if we just opened the object, after a comma, or after ObjectOpen
			if (previousNoWhitespacesToken == YamlTokenType.Comma || 
			    previousNoWhitespacesToken == YamlTokenType.ObjectOpen) {
				return finishToken(offset, YamlTokenType.Key);
			}
		}

		String text = stream.getSource().substring(offset, stream.pos()).trim();
		YamlTokenType valueType = classifyValue(text);
		state = returnState;
		return finishToken(offset, valueType);
	}

	private YamlTokenType classifyValue(String text) {
		if (text.isEmpty()) {
			return YamlTokenType.Value;
		}

		// Check for null
		if (text.equals("null") || text.equals("~") || text.equals("Null") || text.equals("NULL")) {
			return YamlTokenType.ScalarNull;
		}

		// Check for boolean
		if (text.equals("true") || text.equals("false") || text.equals("True") || text.equals("False")
				|| text.equals("TRUE") || text.equals("FALSE") || text.equals("yes") || text.equals("no")
				|| text.equals("Yes") || text.equals("No") || text.equals("YES") || text.equals("NO")
				|| text.equals("on") || text.equals("off") || text.equals("On") || text.equals("Off")
				|| text.equals("ON") || text.equals("OFF")) {
			return YamlTokenType.ScalarBoolean;
		}

		// Check for number (basic check)
		if (text.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?") || text.matches("0x[0-9a-fA-F]+")
				|| text.matches("0o[0-7]+")) {
			return YamlTokenType.ScalarNumber;
		}

		// Check for special float values
		if (text.equals(".inf") || text.equals(".Inf") || text.equals(".INF") || text.equals("-.inf")
				|| text.equals("-.Inf") || text.equals("-.INF") || text.equals(".nan") || text.equals(".NaN")
				|| text.equals(".NAN")) {
			return YamlTokenType.ScalarNumber;
		}

		// Default to string scalar
		return YamlTokenType.ScalarString;
	}

	private YamlTokenType handleStartString(int offset) {
		if (stream.peekChar() == '"' || stream.peekChar() == '\'') {
			// Remember current flow state
			returnStateAfterString = state;
			stringQuote = (char) stream.peekChar();
			stream.advance(1);
			state = YamlScannerState.WithinString;
			return finishToken(offset, YamlTokenType.StartString);
		}
		return null;
	}
}