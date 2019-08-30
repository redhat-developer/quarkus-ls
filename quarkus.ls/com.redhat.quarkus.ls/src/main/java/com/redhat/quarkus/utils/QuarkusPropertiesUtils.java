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

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.PropertyInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfo;

/**
 * Quarkus project information utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusPropertiesUtils {

	/**
	 * Returns the Quarkus property and profile information from the given property
	 * name and null otherwise.
	 * 
	 * @param propertyName the property name
	 * @param info         the quarkus project information which hosts teh Quarkus
	 *                     properties.
	 * @return the Quarkus property and profile information from the given property
	 *         name and null otherwise.
	 */
	public static PropertyInfo getProperty(String propertyName, QuarkusProjectInfo info) {
		Collection<ExtendedConfigDescriptionBuildItem> properties = info.getProperties();
		String profile = null;
		if (propertyName.charAt(0) == '%') {
			// property starts with profile (ex : %dev.property-name)
			int dotIndex = propertyName.indexOf('.');
			profile = propertyName.substring(1, dotIndex != -1 ? dotIndex : propertyName.length());
			if (dotIndex == -1) {
				return new PropertyInfo(null, profile);
			}
			propertyName = propertyName.substring(dotIndex + 1, propertyName.length());
		}

		if (propertyName.isEmpty()) {
			return new PropertyInfo(null, profile);
		}

		for (ExtendedConfigDescriptionBuildItem property : properties) {
			if (match(propertyName, property.getPropertyName())) {
				return new PropertyInfo(property, profile);
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
	public static boolean match(String propertyName, String pattern) {
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
}
