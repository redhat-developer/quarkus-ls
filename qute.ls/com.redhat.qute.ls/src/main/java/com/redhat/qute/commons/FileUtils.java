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
package com.redhat.qute.commons;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * File utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class FileUtils {

	private static final String FILE_SCHEME = "file";

	/**
	 * Returns true if the given uri is a file uri and false otherwise.
	 * 
	 * @param uri the uri
	 * 
	 * @return true if the given uri is a file uri and false otherwise.
	 */
	public static boolean isFileURI(URI uri) {
		return isEmpty(uri.getScheme()) || FILE_SCHEME.equals(uri.getScheme());
	}

	public static URI createUri(String uriString) {
		if (isEmpty(uriString)) {
			return null;
		}

		try {
			if (uriString.startsWith("file:/")) {
				String convertedUri = uriString.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
				convertedUri = convertedUri.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			}
			if (uriString.indexOf('%') != -1) {
				uriString = uriDecode(uriString, StandardCharsets.UTF_8);
			}
			URI uri = URI.create(uriString);
			if (!isFileURI(uri)) {
				// The uri is not a file URI, ignore it.
				return null;
			}
			return uri;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the path for the given file uri and null otherwise.
	 *
	 * @param uriString the Uri as string.
	 *
	 * @return the path for the given file uri and null otherwise.
	 */
	public static Path createPath(String uriString) {
		URI fileURi = createUri(uriString);
		if (fileURi == null) {
			return null;
		}
		if (fileURi.isAbsolute()) {
			return new File(fileURi).toPath();
		}
		return new File(uriString).toPath();
	}

	public static String toUri(Path path) {
		return path.toUri().toASCIIString();
	}

	/**
	 * Decode the given encoded URI component value. Based on the following rules:
	 * <ul>
	 * <li>Alphanumeric characters {@code "a"} through {@code "z"}, {@code "A"}
	 * through {@code "Z"}, and {@code "0"} through {@code "9"} stay the same.</li>
	 * <li>Special characters {@code "-"}, {@code "_"}, {@code "."}, and {@code "*"}
	 * stay the same.</li>
	 * <li>A sequence "{@code %<i>xy</i>}" is interpreted as a hexadecimal
	 * representation of the character.</li>
	 * <li>For all other characters (including those already decoded), the output is
	 * undefined.</li>
	 * </ul>
	 * 
	 * @param source  the encoded String
	 * @param charset the character set
	 * @return the decoded value
	 * @throws IllegalArgumentException when the given source contains invalid
	 *                                  encoded sequences
	 * @since 5.0
	 * @see java.net.URLDecoder#decode(String, String)
	 * 
	 *      This method is a copy / paste from
	 *      https://github.com/spring-projects/spring-framework/blob/67c7b80c2bed0f4f91b27d735541e9dfad8ce5b3/spring-core/src/main/java/org/springframework/util/StringUtils.java#L821
	 */
	private static String uriDecode(String source, Charset charset) {
		int length = source.length();
		if (length == 0) {
			return source;
		}
		// Assert.notNull(charset, "Charset must not be null");

		ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
		boolean changed = false;
		for (int i = 0; i < length; i++) {
			int ch = source.charAt(i);
			if (ch == '%') {
				if (i + 2 < length) {
					char hex1 = source.charAt(i + 1);
					char hex2 = source.charAt(i + 2);
					int u = Character.digit(hex1, 16);
					int l = Character.digit(hex2, 16);
					if (u == -1 || l == -1) {
						throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
					}
					baos.write((char) ((u << 4) + l));
					i += 2;
					changed = true;
				} else {
					throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
				}
			} else {
				baos.write(ch);
			}
		}
		return (changed ? copyToString(baos, charset) : source);
	}

	private static String copyToString(ByteArrayOutputStream baos, Charset charset) {
		try {
			return baos.toString(charset.name());
		} catch (UnsupportedEncodingException e) {
			return baos.toString();
		}
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
