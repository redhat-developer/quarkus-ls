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
	 * True if the current OS is Windows.
	 * Stored as a constant to avoid repeated system property lookups.
	 */
	private static final boolean IS_WINDOWS =
			System.getProperty("os.name").toLowerCase().contains("windows");

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
			// Normalize "file://" and "file:///" to "file:/" for consistent parsing.
			// Some LSP clients (e.g. VSCode on Windows) may send "file:///c:/..." or
			// "file://c:/..." which need to be unified before further processing.
			if (uriString.startsWith("file:/")) {
				uriString = uriString.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
				uriString = uriString.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			}

			// Decode percent-encoded characters (e.g. %3A -> :, %2F -> /).
			// Some LSP clients (e.g. VSCode) send URIs with percent-encoded characters
			// for open files while closed files may have decoded URIs, causing mismatches
			// in the document map lookups.
			if (uriString.indexOf('%') != -1) {
				uriString = uriDecode(uriString, StandardCharsets.UTF_8);
			}

			// On Windows, normalize the drive letter to uppercase.
			// Some LSP clients send "file:/c:/..." while others send "file:/C:/...",
			// which would result in different URIs for the same file and cause
			// mismatches in document map lookups.
			// ex: file:/c:/Users/... -> file:/C:/Users/...
			if (IS_WINDOWS) {
				// Search for the drive letter colon, skipping the "file:/" prefix (6 chars).
				// The drive letter is the char just before the colon, e.g. "file:/c:/" -> 'c'.
				int colonIdx = uriString.indexOf(':', 6);
				if (colonIdx > 0) {
					char drive = uriString.charAt(colonIdx - 1);
					if (drive >= 'a' && drive <= 'z') {
						uriString = uriString.substring(0, colonIdx - 1)
								+ Character.toUpperCase(drive)
								+ uriString.substring(colonIdx);
					}
				}
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
			// Real usecase
			// ex: uriString = file://C/Users/XXX/renarde/src/main/resources
			return new File(fileURi).toPath();
		}
		// Usecase coming from Tests
		// ex: uriString = src/test/resources/projects/renarde/src/main/resources
		return new File(uriString).toPath().toAbsolutePath().normalize();
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

	/**
	 * Returns a canonical URI string from a Path, normalized the same way as
	 * {@link #createUri(String)} to ensure consistent map lookups between
	 * open files (URI from LSP client) and closed files (Path from filesystem scan).
	 *
	 * @param path the path
	 * @return the canonical URI string
	 */
	public static URI toCanonicalUri(Path path) {
	    // path.toUri() on Windows gives file:///C:/... (uppercase, not encoded)
	    // so we just reuse createUri to apply the same normalization pipeline
	    return createUri(path.toUri().toString());
	}
}