/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Path pattern matcher.
 * 
 * @author Angelo ZERR
 *
 */
public class PathPatternMatcher {

	private static final boolean isWindows;

	static {
		String osName = System.getProperty("os.name");
		isWindows = osName != null && osName.toLowerCase().contains("win");
	}

	private static final Logger LOGGER = Logger.getLogger(PathPatternMatcher.class.getName());

	private transient PathMatcher pathMatcher;
	private final String pattern;

	public PathPatternMatcher(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean matches(URI templateUri) {
		if (pattern == null || pattern.length() < 1) {
			return false;
		}
		try {
			return getPathMatcher().matches(Paths.get(templateUri));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,
					"Error while matching uri '" + templateUri.toString() + "' with pattern '" + pattern + "'.", e);
			return false;
		}
	}

	private PathMatcher getPathMatcher() {
		if (pathMatcher != null) {
			return pathMatcher;
		}
		pathMatcher = createPathMatcher();
		return pathMatcher;
	}

	private synchronized PathMatcher createPathMatcher() {
		if (pathMatcher != null) {
			return pathMatcher;
		}
		String glob = getGlobPattern(pattern);
		return FileSystems.getDefault().getPathMatcher("glob:" + glob);
	}

	private static String getGlobPattern(String pattern) {
		boolean schemaFile = pattern.startsWith("file:");
		if (schemaFile) {
			String convertedUri = pattern.replace("file:///", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			convertedUri = convertedUri.replace("file://", "file:/"); //$NON-NLS-1$//$NON-NLS-2$
			if (isWindows) {
				// file:///C:/Users/azerr --> C:/Users/azerr
				convertedUri = convertedUri.replace("file:/", "");
			} else {
				// file:///home/Users/azerr --> /home/Users/azerr
				convertedUri = convertedUri.replace("file:", "");
			}
			try {
				convertedUri = URLDecoder.decode(convertedUri, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.log(Level.SEVERE, "Error while decoding uri '" + pattern + "'.", e);
			}
			return convertedUri;
		}
		char c = pattern.charAt(0);
		if (c != '*' && c != '?' && c != '/') {
			// in case of pattern like this pattern="myFile*.xml", we must add '**/' before
			return "**/" + pattern;
		}
		return pattern;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathPatternMatcher other = (PathPatternMatcher) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		return true;
	}

}