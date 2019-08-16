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
package com.redhat.quarkus.model.parser;

/**
 * An unchecked exception to indicate that an input does not qualify as valid
 * {@code .editorconfig} file.
 * 
 * <p>
 * This file is a copy of
 * https://github.com/ec4j/ec4j/blob/master/core/src/main/java/org/ec4j/core/parser/ParseException.java
 * adapted for properties file.
 * </p>
 *
 * * @author <a href="mailto:angelo.zerr@gmail.com">Angelo Zerr</a>
 * 
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class ParseException extends RuntimeException {

	/**  */
	private static final long serialVersionUID = -3857553902141819279L;

	public ParseException(ErrorEvent errorEvent) {
		super(errorEvent.getMessage() + " at " + errorEvent.getStart());
	}

}