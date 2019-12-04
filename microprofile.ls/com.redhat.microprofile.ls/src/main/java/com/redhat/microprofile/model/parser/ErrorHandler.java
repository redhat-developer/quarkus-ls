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

/**
 * A handler that gets notified on {@link ErrorEvent}s by
 * {@link PropertiesParser}. Note that the basic {@link #THROWING},
 * {@link #THROW_SYNTAX_ERRORS_IGNORE_OTHERS} and {@link #IGNORING}
 * implementations are available in this class.
 *
 * <p>
 * This file is a copy of
 * https://github.com/ec4j/ec4j/blob/master/core/src/main/java/org/ec4j/core/parser/ErrorHandler.java
 * adapted for properties file.
 * </p>
 * 
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public interface ErrorHandler {

	/**
	 * An {@link ErrorHandler} that does nothing in
	 * {@link #error(ParseContext, ErrorEvent)}
	 */
	ErrorHandler IGNORING = new ErrorHandler() {
		@Override
		public void error(ParseContext context, ErrorEvent errorEvent) {
		}
	};

	/**
	 * An {@link ErrorHandler} that throws a {@link ParseException} for every
	 * {@link ErrorEvent} it gets via {@link #error(ParseContext, ErrorEvent)}
	 */
	ErrorHandler THROWING = new ErrorHandler() {
		@Override
		public void error(ParseContext context, ErrorEvent errorEvent) throws ParseException {
			throw new ParseException(errorEvent);
		}
	};

	/**
	 * An {@link ErrorHandler} that throws a {@link ParseException} only for those
	 * {@link ParseException}s whose
	 * {@code errorEvent.getErrorType().isSyntaxError()} returns {@code true}
	 */
	ErrorHandler THROW_SYNTAX_ERRORS_IGNORE_OTHERS = new ErrorHandler() {
		@Override
		public void error(ParseContext context, ErrorEvent errorEvent) throws ParseException {
			if (errorEvent.getErrorType().isSyntaxError()) {
				throw new ParseException(errorEvent);
			}
		}
	};

	/**
	 * A {@link ErrorEvent} occured. Implementations may choose to throw a
	 * {@link ParseException} in reaction to an {@link ErrorEvent}.
	 *
	 * @param context    the {@link ParseContext}
	 * @param errorEvent the error to handle
	 */
	void error(ParseContext context, ErrorEvent errorEvent) throws ParseException;

}