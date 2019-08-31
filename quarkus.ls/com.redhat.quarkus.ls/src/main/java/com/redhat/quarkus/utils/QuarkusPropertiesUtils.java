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

import java.util.Collection;
import java.util.function.BiConsumer;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.SnippetsBuilder;

/**
 * Quarkus project information utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesUtils {

	private static final BiConsumer<Integer, StringBuilder> MARKDOWN_REPLACE = (i, newName) -> newName
			.append("\\{\\*\\}");

	private static final BiConsumer<Integer, StringBuilder> COMPLETION_PLACEHOLDER_REPLACE = (i,
			newName) -> SnippetsBuilder.placeholders(i++, "key", newName);

	/**
	 * Returns the Quarkus property from the given property name and null otherwise.
	 * 
	 * @param propertyName the property name
	 * @param info         the quarkus project information which hosts the Quarkus
	 *                     properties.
	 * @return the Quarkus property from the given property name and null otherwise.
	 */
	public static ExtendedConfigDescriptionBuildItem getProperty(String propertyName, QuarkusProjectInfo info) {
		Collection<ExtendedConfigDescriptionBuildItem> properties = info.getProperties();
		if (propertyName == null || propertyName.isEmpty()) {
			return null;
		}

		for (ExtendedConfigDescriptionBuildItem property : properties) {
			if (match(propertyName, property.getPropertyName())) {
				return property;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given property name matches the given pattern and false
	 * otherwise.
	 * 
	 * The pattern can be:
	 * 
	 * <ul>
	 * <li>a simple pattern: it means that pattern is equals to the property
	 * name</li>
	 * <li>a map pattern: pattern which contains {*}.
	 * </ul>
	 * 
	 * @param propertyName the property name
	 * @param pattern      the pattern
	 * @return true if the given property name matches the given pattern and false
	 *         otherwise.
	 */
	private static boolean match(String propertyName, String pattern) {
		int i2 = 0;
		int len = Math.max(propertyName.length(), pattern.length());
		for (int i1 = 0; i1 < len; i1++) {
			char c1 = getCharAt(pattern, i1);
			boolean keyMap = false;
			if ('{' == c1 && '*' == getCharAt(pattern, i1 + 1) && '}' == getCharAt(pattern, i1 + 2)) {
				i1 = i1 + 2;
				keyMap = true;
			}

			char c2 = getCharAt(propertyName, i2);
			if (keyMap) {
				if (c2 == '\u0000') {
					return false;
				}
				boolean endsWithQuote = (c2 == '"');
				while (c2 != '\u0000') {
					c2 = getCharAt(propertyName, ++i2);
					if (endsWithQuote) {
						if (c2 == '"') {
							i2++;
							break;
						}
					} else if ('.' == c2 && propertyName.charAt(i2 - 1) != '\\'
							&& propertyName.charAt(i2 - 2) != '\\') {
						break;
					}
				}
				keyMap = false;
			} else {
				if (c2 != c1) {
					return false;
				}
				i2++;
			}
		}
		return true;
	}

	private static char getCharAt(String text, int index) {
		if (index >= text.length()) {
			return '\u0000';
		}
		return text.charAt(index);
	}

	public static String formatPropertyForMarkdown(String propertyName) {
		return formatProperty(propertyName, MARKDOWN_REPLACE);
	}

	public static String formatPropertyForCompletion(String propertyName) {
		return formatProperty(propertyName, COMPLETION_PLACEHOLDER_REPLACE);
	}

	public static String formatProperty(String propertyName, BiConsumer<Integer, StringBuilder> replace) {
		int index = propertyName.indexOf("{*}");
		if (index != -1) {
			int i = 1;
			String current = propertyName;
			StringBuilder newName = new StringBuilder();
			while (index != -1) {
				newName.append(current.substring(0, index));
				current = current.substring(index + 3, current.length());
				replace.accept(i++, newName);
				index = current.indexOf("{*}");
			}
			newName.append(current);
			return newName.toString();
		}
		return propertyName;
	}
}
