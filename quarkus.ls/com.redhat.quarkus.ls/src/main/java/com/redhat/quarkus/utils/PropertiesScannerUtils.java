/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.utils;

/**
 * Scanner to retrieve token from application.properties:
 * 
 * <ul>
 * <li>start / end offset of line Comments</li>
 * <li>start / end offset of property key</li>
 * <li>start / end offset of property value</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesScannerUtils {

	public static enum PropertiesTokenType {
		COMMENTS, KEY, VALUE;
	}

	/**
	 * Properties token
	 */
	public static class PropertiesToken {

		private final int start;
		private final int end;
		private final PropertiesTokenType type;

		public PropertiesToken(int start, int end, PropertiesTokenType type) {
			this.start = start;
			this.end = end;
			this.type = type;
		}

		/**
		 * Returns the start offset of the token
		 * 
		 * @return the start offset of the token
		 */
		public int getStart() {
			return start;
		}

		/**
		 * Returns the end offset of the token
		 * 
		 * @return the end offset of the token
		 */
		public int getEnd() {
			return end;
		}

		/**
		 * Returns the token type.
		 * 
		 * @return the token type.
		 */
		public PropertiesTokenType getType() {
			return type;
		}
	}

	private PropertiesScannerUtils() {

	}

	/**
	 * Returns the properties token at the given offset <code>offset</code>.
	 * 
	 * @param text            the text to scan
	 * @param startLineOffset the start line offset used to start scan
	 * @param offset          the offset
	 * @return the properties token at the given offset <code>offset</code>.
	 */
	public static PropertiesToken getTokenAt(String text, int startLineOffset, int offset) {
		// Search the token type (Comments, key, value) at the given offset
		PropertiesTokenType type = null;
		boolean stop = false;
		for (int i = startLineOffset; i < offset && !stop; i++) {
			char c = text.charAt(i);
			switch (c) {
			case ' ':
				break;
			case '#':
				if (type == null) {
					type = PropertiesTokenType.COMMENTS;
					stop = true;
				}
				break;
			case '=':
				type = PropertiesTokenType.VALUE;
				stop = true;
				break;
			default:
				type = PropertiesTokenType.KEY;
			}
		}
		if (type == null) {
			type = PropertiesTokenType.KEY;
		}
		// Compute the end offset of the token
		switch (type) {
		case COMMENTS:
			// In line comments type, end offset is the end of line
			for (int i = startLineOffset; i < text.length() - startLineOffset; i++) {
				char c = text.charAt(i);
				if (c == '\r' || c == '\n') {
					return new PropertiesToken(startLineOffset, i - 1, PropertiesTokenType.COMMENTS);
				}
			}
			return new PropertiesToken(startLineOffset, text.length() - 1, PropertiesTokenType.COMMENTS);
		case KEY:
			// In key type, end offset is the end of line or '='
			for (int i = startLineOffset; i < text.length() - startLineOffset; i++) {
				char c = text.charAt(i);
				if (c == '=' || c == '\r' || c == '\n') {
					return new PropertiesToken(startLineOffset, i - 1, PropertiesTokenType.KEY);
				}
			}
			return new PropertiesToken(startLineOffset, text.length() - 1, PropertiesTokenType.KEY);
		case VALUE:
			// In value type, end offset is the end of line only if there are not a '\'
			// character before (multi value)
			boolean lastCharIsBackSlash = false;
			for (int i = startLineOffset; i < text.length() - startLineOffset; i++) {
				char c = text.charAt(i);
				if (c == '\\') {
					lastCharIsBackSlash = true;
				} else if (c == '\r' || c == '\n') {
					if (!lastCharIsBackSlash) {
						return new PropertiesToken(startLineOffset, i - 1, PropertiesTokenType.VALUE);
					}
				} else {
					lastCharIsBackSlash = false;
				}
			}
			return new PropertiesToken(startLineOffset, text.length() - 1, PropertiesTokenType.VALUE);
		default:
			return new PropertiesToken(startLineOffset, text.length() - 1, PropertiesTokenType.KEY);
		}
	}
}
