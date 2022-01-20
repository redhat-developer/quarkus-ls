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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;

/**
 * Test for path pattern matcher {@link PathPatternMatcher}.
 * 
 * @author Angelo ZERR
 *
 */
public class PathPatternMatcherTest {

	private static final boolean isWindows;

	static {
		String osName = System.getProperty("os.name");
		isWindows = osName != null && osName.toLowerCase().contains("win");
	}

	@Test
	public void fileSchemaWithoutPattern() {
		String pattern = getFileSystemPath("/Users/azerr/qute.txt");
		PathPatternMatcher matcher = new PathPatternMatcher(pattern);

		String uri1 = getFileSystemPath("/Users/azerr/qute.txt");
		assertTrue(matcher.matches(URI.create(uri1)), uri1 + " should match " + pattern);

		String uri2 = getFileSystemPath("/Users/azerr/qute.html");
		assertFalse(matcher.matches(URI.create(uri2)), uri2 + " should NOT match " + pattern);
	}

	@Test
	public void fileSchemaWithPattern() {
		String pattern = getFileSystemPath("/Users/azerr/qute.*");
		PathPatternMatcher matcher = new PathPatternMatcher(pattern);

		String uri1 = getFileSystemPath("/Users/azerr/qute.txt");
		assertTrue(matcher.matches(URI.create(uri1)), uri1 + " should match " + pattern);

		String uri2 = getFileSystemPath("/Users/azerr/qute.html");
		assertTrue(matcher.matches(URI.create(uri2)), uri2 + " should match " + pattern);
	}

	@Test
	public void pattern() {
		String pattern = "qute.*";
		PathPatternMatcher matcher = new PathPatternMatcher(pattern);

		String uri1 = getFileSystemPath("/Users/azerr/qute.txt");
		assertTrue(matcher.matches(URI.create(uri1)), uri1 + " should match " + pattern);

		String uri2 = getFileSystemPath("/Users/azerr/qute.html");
		assertTrue(matcher.matches(URI.create(uri2)), uri2 + " should match " + pattern);
	}

	@Test
	public void pattern2() {
		String pattern = "**/qute.*";
		PathPatternMatcher matcher = new PathPatternMatcher(pattern);

		String uri1 = getFileSystemPath("/Users/azerr/qute.txt");
		assertTrue(matcher.matches(URI.create(uri1)), uri1 + " should match " + pattern);

		String uri2 = getFileSystemPath("/Users/azerr/qute.html");
		assertTrue(matcher.matches(URI.create(uri2)), uri2 + " should match " + pattern);
	}

	@Test
	public void windowsPattern() {
		if (!isWindows) {
			return;
		}
		PathPatternMatcher matcher = new PathPatternMatcher("file:///c%3A/Users/azerr/qute.*");

		URI uri1 = URI.create("file:///c%3A/Users/azerr/qute.txt");
		assertTrue(matcher.matches(uri1));
		URI uri2 = URI.create(getFileSystemPath("/Users/azerr/qute.html"));
		assertTrue(matcher.matches(uri2));
	}

	private static String getFileSystemPath(String path) {
		return isWindows ? "file:///C:" + path : "file:///home" + path;
	}
}
