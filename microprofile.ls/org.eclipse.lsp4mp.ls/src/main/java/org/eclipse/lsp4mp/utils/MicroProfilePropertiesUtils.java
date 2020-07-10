/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ConfigurationMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.ls.commons.SnippetsBuilder;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.values.ValuesRulesManager;
import org.eclipse.lsp4mp.services.QuarkusModel;

/**
 * MicroProfile project information utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfilePropertiesUtils {

	private static final BiConsumer<Integer, StringBuilder> KEY_MAP_MARKDOWN_REPLACE = (i, newName) -> newName
			.append("\\{\\*\\}");

	private static final BiConsumer<Integer, StringBuilder> INDEX_ARRAY_MARKDOWN_REPLACE = (i, newName) -> newName
			.append("[\\*\\]");

	private static final BiConsumer<Integer, StringBuilder> KEY_MAP_COMPLETION_PLACEHOLDER_REPLACE = (i,
			newName) -> SnippetsBuilder.placeholders(i++, "key", newName);

	private static final BiConsumer<Integer, StringBuilder> INDEX_ARRAY_COMPLETION_PLACEHOLDER_REPLACE = (i,
			newName) -> {
		newName.append('[');
		SnippetsBuilder.placeholders(i++, "0", newName);
		newName.append(']');
	};

	/**
	 * Result of formatted property name
	 *
	 */
	public static class FormattedPropertyResult {

		private final String propertyName;

		private final int parameterCount;

		public FormattedPropertyResult(String propertyName, int parameterCount) {
			this.propertyName = propertyName;
			this.parameterCount = parameterCount;
		}

		/**
		 * Returns the formatted property name
		 * 
		 * @return the formatted property name
		 */
		public String getPropertyName() {
			return propertyName;
		}

		/**
		 * Returns the mapped and index array parameter count.
		 * 
		 * @return the mapped and index array parameter count.
		 */
		public int getParameterCount() {
			return parameterCount;
		}
	}

	/**
	 * Returns the enums values according the property type.
	 * 
	 * @param property           the property
	 * @param projectInfo
	 * @param valuesRulesManager
	 * @param model
	 * @return the enums values according the property type
	 */
	public static Collection<ValueHint> getEnums(ItemMetadata property, ConfigurationMetadata configuration,
			PropertiesModel model, ValuesRulesManager valuesRulesManager) {
		ItemHint hint = configuration.getHint(property);
		if (hint != null) {
			return hint.getValues();
		}
		if (property.isBooleanType()) {
			return QuarkusModel.BOOLEAN_ENUMS.getValues();
		}
		if (valuesRulesManager != null) {
			return valuesRulesManager.getValues(property, model);
		}
		return null;
	}

	/**
	 * Returns the MicroProfile property from the given property name and null otherwise.
	 * 
	 * @param propertyName the property name
	 * @param info         the MicroProfile project information which hosts the MicroProfile
	 *                     properties.
	 * @return the MicroProfile property from the given property name and null otherwise.
	 */
	public static ItemMetadata getProperty(String propertyName, MicroProfileProjectInfo info) {
		Collection<ItemMetadata> properties = info.getProperties();
		if (propertyName == null || propertyName.isEmpty()) {
			return null;
		}

		for (ItemMetadata property : properties) {
			if (property != null && match(propertyName, property.getName())) {
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
			boolean keyMapOrArrayIndex = false;
			if ('{' == c1 && '*' == getCharAt(pattern, i1 + 1) && '}' == getCharAt(pattern, i1 + 2)) {
				// It's a key map {*}.
				i1 = i1 + 2;
				keyMapOrArrayIndex = true;
			} else if ('[' == c1 && '*' == getCharAt(pattern, i1 + 1) && ']' == getCharAt(pattern, i1 + 2)) {
				// It's an array index [*].
				i1 = i1 + 2;
				keyMapOrArrayIndex = true;
			}

			char c2 = getCharAt(propertyName, i2);
			if (keyMapOrArrayIndex) {
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
				keyMapOrArrayIndex = false;
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
		return formatProperty(propertyName, KEY_MAP_MARKDOWN_REPLACE, INDEX_ARRAY_MARKDOWN_REPLACE).getPropertyName();
	}

	public static FormattedPropertyResult formatPropertyForCompletion(String propertyName) {
		return formatProperty(propertyName, KEY_MAP_COMPLETION_PLACEHOLDER_REPLACE,
				INDEX_ARRAY_COMPLETION_PLACEHOLDER_REPLACE);
	}

	private static FormattedPropertyResult formatProperty(String propertyName,
			BiConsumer<Integer, StringBuilder> keyMapReplace, BiConsumer<Integer, StringBuilder> indexArrayReplace) {
		if (!isMappedProperty(propertyName) && !isIndexArrayProperty(propertyName)) {
			return new FormattedPropertyResult(propertyName, 0);
		}
		StringBuilder newName = new StringBuilder();
		int parameterCount = 0;
		for (int i = 0; i < propertyName.length(); i++) {
			char c = propertyName.charAt(i);
			if (c == '{') {
				i = i + 2;
				parameterCount++;
				keyMapReplace.accept(parameterCount, newName);
			} else if (c == '[') {
				i = i + 2;
				parameterCount++;
				indexArrayReplace.accept(parameterCount, newName);
			} else {
				newName.append(c);
			}
		}
		return new FormattedPropertyResult(newName.toString(), parameterCount);
	}

	/**
	 * Returns true if the given property name is a mapped property and false
	 * otherwise.
	 * 
	 * @param propertyName the property name
	 * @return true if the given property name is a mapped property and false
	 *         otherwise.
	 */
	public static boolean isMappedProperty(String propertyName) {
		return propertyName.indexOf("{*}") != -1;
	}

	/**
	 * Returns true if the given property name is a index array property and false
	 * otherwise.
	 * 
	 * @param propertyName the property name
	 * @return true if the given property name is a index array property and false
	 *         otherwise.
	 */
	public static boolean isIndexArrayProperty(String propertyName) {
		return propertyName.indexOf("[*]") != -1;
	}
}
