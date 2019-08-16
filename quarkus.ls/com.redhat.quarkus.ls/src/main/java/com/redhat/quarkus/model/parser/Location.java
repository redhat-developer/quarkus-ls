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

import org.eclipse.lsp4j.Position;

/**
 * An immutable object that represents a location in the parsed text.
 * <p>
 * This file is a copy of
 * https://github.com/ec4j/ec4j/blob/master/core/src/main/java/org/ec4j/core/parser/Location
 * .java adapted for properties file.
 * </p>
 *
 * 
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo Zerr</a>
 */
public class Location extends Position {

	/**
	 * The absolute character index, starting at 0.
	 */
	private final int offset;

	public Location(int offset, int line, int character) {
		super(line, character);
		this.offset = offset;
	}

	/**
	 * @return an absolute index within the file; the first offset is {@code 0}
	 */
	public int getOffset() {
		return offset;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return offset;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getLine() + ":" + getCharacter() + " (" + offset + ")";
	}

}