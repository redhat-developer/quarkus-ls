/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.microprofile.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is a copy/paste from
 * https://github.com/spring-projects/spring-framework/blob/243f2890ee9e98c97c8dc7278f033def3bf33f86/spring-core/src/main/java/org/springframework/util/StringUtils.java
 * which contains only method required by {@link AntPathMatcher}.
 * 
 * @author Angelo ZERR
 *
 */
public class StringUtils {

	private static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * Check whether the given {@code String} contains actual <em>text</em>.
	 * <p>
	 * More specifically, this method returns {@code true} if the {@code String} is
	 * not {@code null}, its length is greater than 0, and it contains at least one
	 * non-whitespace character.
	 * 
	 * @param str the {@code String} to check (may be {@code null})
	 * @return {@code true} if the {@code String} is not {@code null}, its length is
	 *         greater than 0, and it does not contain whitespace only
	 * @see #hasText(CharSequence)
	 * @see #hasLength(String)
	 * @see Character#isWhitespace
	 */
	public static boolean hasText(String str) {
		return (str != null && !str.isEmpty() && containsText(str));
	}

	private static boolean containsText(CharSequence str) {
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tokenize the given {@code String} into a {@code String} array via a
	 * {@link StringTokenizer}.
	 * <p>
	 * Trims tokens and omits empty tokens.
	 * <p>
	 * The given {@code delimiters} string can consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using {@link #delimitedListToStringArray}.
	 * 
	 * @param str        the {@code String} to tokenize (potentially {@code null} or
	 *                   empty)
	 * @param delimiters the delimiter characters, assembled as a {@code String}
	 *                   (each of the characters is individually considered as a
	 *                   delimiter)
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true);
	}

	/**
	 * Tokenize the given {@code String} into a {@code String} array via a
	 * {@link StringTokenizer}.
	 * <p>
	 * The given {@code delimiters} string can consist of any number of delimiter
	 * characters. Each of those characters can be used to separate tokens. A
	 * delimiter is always a single character; for multi-character delimiters,
	 * consider using {@link #delimitedListToStringArray}.
	 * 
	 * @param str               the {@code String} to tokenize (potentially
	 *                          {@code null} or empty)
	 * @param delimiters        the delimiter characters, assembled as a
	 *                          {@code String} (each of the characters is
	 *                          individually considered as a delimiter)
	 * @param trimTokens        trim the tokens via {@link String#trim()}
	 * @param ignoreEmptyTokens omit empty tokens from the result array (only
	 *                          applies to tokens that are empty after trimming;
	 *                          StringTokenizer will not consider subsequent
	 *                          delimiters as token in the first place).
	 * @return an array of the tokens
	 * @see java.util.StringTokenizer
	 * @see String#trim()
	 * @see #delimitedListToStringArray
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
			boolean ignoreEmptyTokens) {

		if (str == null) {
			return EMPTY_STRING_ARRAY;
		}

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				tokens.add(token);
			}
		}
		return toStringArray(tokens);
	}

	/**
	 * Copy the given {@link Collection} into a {@code String} array.
	 * <p>
	 * The {@code Collection} must contain {@code String} elements only.
	 * 
	 * @param collection the {@code Collection} to copy (potentially {@code null} or
	 *                   empty)
	 * @return the resulting {@code String} array
	 */
	public static String[] toStringArray(Collection<String> collection) {
		return (!collection.isEmpty() ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
	}
}
