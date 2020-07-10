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
package org.eclipse.lsp4mp.model.parser;

import org.eclipse.lsp4j.Range;

/**
 * An occurrence of syntax error or other error in an {@code .editorconfig}
 * file.
 * 
 * <p>
 * This file is a copy of
 * https://github.com/ec4j/ec4j/blob/master/core/src/main/java/org/ec4j/core/parser/ErrorEvent.java
 * adapted for properties file.
 * </p>
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ErrorEvent extends Range {

	/**
	 * A way to keep apart various kinds of errors in a type safe way.
	 */
	public enum ErrorType {
		EXPECTED_END_OF_INPUT(true), //
		EXPECTED_STRING_CHARACTER(true), //
		GLOB_NOT_CLOSED(true), //
		INVALID_GLOB(false), //
		INVALID_PROPERTY_VALUE(false), //
		PROPERTY_ASSIGNMENT_MISSING(true), //
		PROPERTY_VALUE_MISSING(true), //
		UNEXPECTED_END_OF_INPUT(true), //
		/** For third parties who may want to report an error not envisaged here */
		OTHER(false);

		private final boolean syntaxError;

		ErrorType(boolean syntaxError) {
			this.syntaxError = syntaxError;
		}

		/**
		 * Syntax errors are such ones that have to do with the {@code .editorconfig}
		 * file structure, such a as property without value, or property name not
		 * followed by equal sign. Non-syntax errors that may happen with the current
		 * implementation are only of two kinds: (i) broken {@link Glob} pattern or (ii)
		 * invalid value for the given registered {@link PropertyType}.
		 *
		 * @return {@code true} is this is a syntax error; {@code false} otherwise
		 */
		public boolean isSyntaxError() {
			return syntaxError;
		}
	};

	private final ErrorType errorType;

	private final String message;

	public ErrorEvent(Location start, Location end, String message, ErrorType errorType) {
		super(start, end);
		this.message = message;
		this.errorType = errorType;
	}

	/**
	 * {@link ErrorType} is a way to keep apart various kinds of errors in a type
	 * safe way.
	 *
	 * @return the {@link ErrorType}
	 */
	public ErrorType getErrorType() {
		return errorType;
	}

	/**
	 * @return a message describing the error
	 */
	public String getMessage() {
		return message;
	}

}