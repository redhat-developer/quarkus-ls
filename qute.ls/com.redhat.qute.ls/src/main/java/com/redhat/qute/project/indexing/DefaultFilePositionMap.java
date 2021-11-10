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
package com.redhat.qute.project.indexing;

/**
 * FilePositionMap can be used to get the line number for a large number of nodes (starting from 1).
 * It works the most efficiently when the requested node is close to the previously requested node.
 *
 * Other designs that weren't chosen:
 * - Precomputing all of the start/end offsets when initializing was slower - Some offsets weren't needed, and walking the tree was slower.
 * - Caching line numbers for previously requested offsets wasn't really necessary, since offsets are usually close together and weren't requested repeatedly.
 */
public class DefaultFilePositionMap implements FilePositionMap {

	/** @var string the full file contents */
	private final String fileContents;

	/**
	 * @var int the 0-based byte offset of the most recent request for a line
	 *      number.
	 */
	private int currentOffset;

	/**
	 * @var int the 1-based line number for this.currentOffset (updated whenever
	 *      currentOffset is updated)
	 */
	private int lineForCurrentOffset;

	public DefaultFilePositionMap(String fileContents) {
		this.fileContents = fileContents;
		this.currentOffset = 0;
		this.lineForCurrentOffset = 1;
	}

	/**
	 * @param int offset - A 0-based byte offset
	 * @return int - gets the 1-based line number for offset
	 */
	@Override
	public int getLineNumberForOffset(int offset) {
		if (offset < 0) {
			offset = 0;
		} else if (offset > this.getFileContentsLength()) {
			offset = this.getFileContentsLength();
		}
		int currentOffset = this.currentOffset;
		if (offset > currentOffset) {
			this.lineForCurrentOffset += getLineCount(this.fileContents, currentOffset, offset);
			this.currentOffset = offset;
		} else if (offset < currentOffset) {
			this.lineForCurrentOffset -= getLineCount(this.fileContents, offset, currentOffset);
			this.currentOffset = offset;
		}
		return this.lineForCurrentOffset;
	}

	/**
	 * @param int offset - A 0-based byte offset
	 * @return int - gets the 1-based column number for offset
	 */
	@Override
	public int getColumnForOffset(int offset) {
		int length = this.getFileContentsLength();
		if (offset <= 1) {
			return 1;
		} else if (offset > length) {
			offset = length;
		}
		// Postcondition: offset >= 1, (lastNewlinePos < offset)
		// If there was no previous newline, lastNewlinePos = 0

		// Start strrpos check from the character before the current character,
		// in case the current character is a newline.
		int lastNewlinePos = getLinePosition(this.fileContents, offset - 1);
		return 1 + offset - (lastNewlinePos == -1 ? 0 : lastNewlinePos + 1);
	}

	private int getFileContentsLength() {
		return fileContents.length();
	}

	private static int getLineCount(String fileContents, int start, int end) {
		int line = 0;
		for (int i = start; i < end; i++) {
			char ch = fileContents.charAt(i);
			if (ch == '\n') {
				line++;
			}
		}
		return line;
	}

	private static int getLinePosition(String fileContents, int offset) {
		if (offset >= fileContents.length()) {
			return -1;
		}
		for (int i = offset; i >= 0; i--) {
			char ch = fileContents.charAt(i);
			if (ch == '\n') {
				return i;
			}
		}
		return -1;
	}

}