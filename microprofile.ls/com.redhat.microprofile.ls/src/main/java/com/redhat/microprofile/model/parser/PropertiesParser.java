/**
 * Copyright (c) 2017 Angelo Zerr and other contributors as
 * indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.microprofile.model.parser;

import com.redhat.microprofile.model.parser.ErrorEvent.ErrorType;

/**
 * Properties parser.
 * 
 * <p>
 * This file is a copy of
 * https://github.com/ec4j/ec4j/blob/master/core/src/main/java/org/ec4j/core/parser/EditorConfigParser.java
 * adapted for properties file.
 * </p>
 * 
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo Zerr</a>
 */
public class PropertiesParser implements ParseContext {

	private PropertiesHandler handler;
	private ErrorHandler errorHandler;
	private ParseState parseState;
	private String text;
	private int bufferOffset;
	private int index;
	private int line;
	private int lineOffset;
	private int last;
	private int current;

	/**
	 * Enum that keep tracks of what will be parsed on the next line
	 */
	private enum ParseState {
		Property,
		PropertyName,
		PropertyValue
	}

	/**
	 * Reads the entire input from the {@code resource} and transforms it into a
	 * sequence of parse events which are sent to the given
	 * {@link PropertiesHandler}.
	 *
	 * @param resource     the {@link Resource} to parse
	 * @param handler      the handler to send the parse events to
	 * @param errorHandler an {@link ErrorHandler} to notify on parse errors @ on
	 *                     I/O problems when reading out of the given
	 *                     {@link Resource}
	 * @throws ParseException only if the supplied {@link ErrorHandler} chooses to
	 *                        react on some {@link ErrorEvent} by throwing
	 *                        {@code ParseException}s for them
	 */
	public void parse(String text, PropertiesHandler handler, ErrorHandler errorHandler) {
		this.handler = handler;
		this.errorHandler = errorHandler;
		this.parseState = ParseState.Property;
		bufferOffset = 0;
		index = 0;
		line = 1;
		lineOffset = 0;
		current = 0;
		last = -1;

		this.text = text;
		readLines();
		if (!isEndOfText()) {
			Location location = getLocation();
			ErrorEvent e = new ErrorEvent(location, location, "Found unexpected character; expected end of input",
					ErrorType.EXPECTED_END_OF_INPUT);
			errorHandler.error(this, e);
		}
	}

	private void readLines() {
		handler.startDocument(this);
		int currentLine = 0;
		do {
			read();
			if (currentLine != line) {
				currentLine = line;
				readLine();
			}
		} while (!isEndOfText());

		// reached end of file
		if (parseState == ParseState.PropertyName) {
			handler.endPropertyName(this);
			handler.endProperty(this);
		} else if (parseState == ParseState.PropertyValue) {
			handler.endPropertyValue(this);
			handler.endProperty(this);
		}
		handler.endDocument(this);
	}

	private void readLine() {
		skipWhiteSpace();
		if (isNewLine()) {
			// blank line
			handler.blankLine(this);
			return;
		} else if (current == '\ufeff') {
			// BOM character, do nothing
			return;
		}
		switch (current) {
			case '#':
			case ';':
				// comment line
				readComment();
				break;
			default:
				// property line
				readProperty();
		}
	}

	private void readComment() {
		handler.startComment(this);
		do {
			read();
		} while (!isEndOfText() && !isNewLine());
		handler.endComment(this);
	}

	private enum StopReading {
		PropertyName, PropertyValue
	}

	private void readString(StopReading stop) {
		while (!isStopReading(stop)) {
			if (isEndOfText()) {
				final Location location = getLocation();
				ErrorEvent e = new ErrorEvent(location, location, "Unexpected end of input",
						ErrorType.UNEXPECTED_END_OF_INPUT);
				errorHandler.error(this, e);
			} else if (current < 0x20) {
				final Location location = getLocation();
				ErrorEvent e = new ErrorEvent(location, location, "Expected a valid string character",
						ErrorType.EXPECTED_STRING_CHARACTER);
				errorHandler.error(this, e);
			} else {
				read();
			}
		}
	}

	private boolean isStopReading(StopReading stop) {
		if (isEndOfText() || isNewLine()) {
			return true;
		}
		switch (stop) {
			case PropertyName:
				return isColonSeparator() || isWhiteSpace();
			case PropertyValue:
				return false;
			default:
				return isWhiteSpace();
		}
	}

	/**
	 * Reads the Property in the current line
	 */
	private void readProperty() {

		switch (parseState) {
			case PropertyName:
				if (continueReadPropertyKey()) {
					readAfterPropertyKey();
				}
				break;
			case PropertyValue:
				if (continueReadPropertyValue()) {
					handler.endProperty(this);
					parseState = ParseState.Property;
				}
				break;
			case Property:
				handler.startProperty(this);
				skipWhiteSpace();
				if (!readPropertyKey()) {
					// property name continues on the next line
					parseState = ParseState.PropertyName;
					return;
				};
				readAfterPropertyKey();
				break;
			default:
				return;
		}
	}

	/**
	 * Reads the contents after a PropertyKey.
	 * This method should only be called after a PropertyKey
	 * has been finsished reading:
	 * (ProperyKey.start != -1 and ProperyKey.end != -1)
	 */
	private void readAfterPropertyKey(){
		skipWhiteSpace();

		if (current != '=' && current != ':') {
			skipUntilEndOfLine();
			handler.endProperty(this);
			parseState = ParseState.Property;
			return;
		}
		
		readDelimiter();
		skipWhiteSpace();

		if (isEndOfText()) {
			handler.endProperty(this);
			parseState = ParseState.Property;
			return;
		}

		if (readPropertyValue()) {
			handler.endProperty(this);
			parseState = ParseState.Property;
		} else {
			// property value continues on the next line
			parseState = ParseState.PropertyValue;
		}
	}

	/**
	 * Creates a new PropertyKey in the handler and
	 * reads the current line for the PropertyKey.
	 * 
	 * Returns true if the full PropertyKey has been read and
	 * false otherwise. If false is returned, the contents
	 * of the PropertyKey continues on the next line.
	 * 
	 * @return true if the full PropertyKey has been read and
	 * false otherwise.
	 */
	private boolean readPropertyKey() {
		handler.startPropertyName(this);
		return continueReadPropertyKey();
	}

	/**
	 * Creates a new PropertyValue in the handler and
	 * reads the remaining content from the current line
	 * for the PropertyValue.
	 * 
	 * Returns true if the full PropertyValue has been read and
	 * false otherwise. If false is returned, the contents
	 * of the PropertyValue continues on the next line.
	 * 
	 * @return true if the full PropertyValue has been read and
	 * false otherwise.
	 */
	private boolean readPropertyValue() {
		handler.startPropertyValue(this);
		return continueReadPropertyValue();
	}

	/**
	 * Reads the current line for the PropertyKey without
	 * creating a new PropertyKey.
	 * 
	 * Prerequiste: A PropertyKey must already exist in the
	 * handler.
	 * 
	 * Returns true if the full PropertyKey has been read and
	 * false otherwise. If false is returned, the contents
	 * of the PropertyKey continues on the next line.
	 * 
	 * @return true if the full PropertyKey has been read and
	 * false otherwise. If false is returned, the contents
	 * of the PropertyKey continues on the next line.
	 */
	private boolean continueReadPropertyKey() {
		readString(StopReading.PropertyName);
		if (last != '\\') {
			handler.endPropertyName(this);
			return true;
		}
		return false;
	}

	/**
	 * Reads the delimiter
	 */
	private void readDelimiter() {
		handler.delimiterAssign(this);
		// read the '=' or ':' sign
		read();
	}

	/**
	 * Reads the current line for the PropertyValue without
	 * creating a new PropertyValue.
	 * 
	 * Prerequiste: A PropertyValue must already exist in the
	 * handler.
	 * 
	 * Returns true if the full PropertyValue has been read and
	 * false otherwise. If false is returned, the contents
	 * of the PropertyValue continues on the next line.
	 * 
	 * @return true if the full PropertyValue has been read and
	 * false otherwise.
	 */
	private boolean continueReadPropertyValue() {
		readString(StopReading.PropertyValue);
		if (last != '\\') {
			handler.endPropertyValue(this);
			return true;
		}
		return false;
	}

	private void skipWhiteSpace() {
		while (isWhiteSpace()) {
			read();
		}
	}

	private void skipUntilEndOfLine() {
		while (!isNewLine() && !isEndOfText()) {
			read();
		}
	}

	private void read() {
		if (current == '\n') {
			line++;
			lineOffset = bufferOffset + index;
		}
		last = current;
		current = index >= text.length() ? -1 : text.charAt(index++);
		if (current == -1) {
			bufferOffset++;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Location getLocation() {
		int offset = getLocationOffset();
		int column = offset - lineOffset + 1;
		return new Location(offset, line, column);
	}

	@Override
	public int getLocationOffset() {
		return bufferOffset + index - 1;
	}

	private boolean isWhiteSpace() {
		return isWhiteSpace(current);
	}

	private static boolean isWhiteSpace(int c) {
		return c == ' ' || c == '\t';
	}

	private boolean isNewLine() {
		return current == '\n' || current == '\r';
	}

	private boolean isEndOfText() {
		return current == -1;
	}

	private boolean isColonSeparator() {
		return current == '=' || current == ':';
	}

	/** {@inheritDoc} */
	@Override
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
}