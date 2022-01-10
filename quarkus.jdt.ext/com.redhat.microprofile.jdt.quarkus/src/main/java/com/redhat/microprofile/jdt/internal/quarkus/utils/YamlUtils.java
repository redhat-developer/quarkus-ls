/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.utils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/**
 * Yaml utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class YamlUtils {

	private YamlUtils() {
	}

	/**
	 * Load the Yaml document from the given <code>input</code> and flattern the
	 * properties to return an instance of {@link Properties}.
	 * 
	 * @param input the
	 * 
	 * @return the flattern properties.
	 */
	public static Properties loadYamlAsProperties(InputStream input) {
		Properties properties = new Properties();
		// Load Yaml document
		Yaml yaml = new Yaml(new SafeConstructor());
		Object yamlDocument = yaml.load(input);
		if (yamlDocument != null) {
			// flattern Yaml document to properties.
			Map<String, Object> yamlMap = toMap(yamlDocument);
			properties.putAll(flattenMap(yamlMap));
		}
		return properties;
	}

	private static Map<String, Object> toMap(Object object) {
		if (object instanceof Map) {
			return (Map<String, Object>) object;
		}
		return null;
	}

	private static Map<String, String> flattenMap(Map<String, Object> sourceMap) {
		Map<String, String> resultMap = new LinkedHashMap<>();
		// Loop for key/values of source Map
		for (Map.Entry<String, Object> sourceMapEntry : sourceMap.entrySet()) {
			String key = sourceMapEntry.getKey();
			Object value = sourceMapEntry.getValue();

			// flatten the value
			if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> valueMap = flattenMap((Map<String, Object>) value);
				for (Map.Entry<String, String> valueMapEntry : valueMap.entrySet()) {
					String subKey = valueMapEntry.getKey();
					String subValue = valueMapEntry.getValue();
					resultMap.put(subKey != null ? key + "." + subKey : key, subValue);
				}
			} else if (value instanceof Collection) {
				StringBuilder joiner = new StringBuilder();
				String separator = "";
				for (Object element : ((Collection<?>) value)) {
					Map<String, String> subMap = flattenMap(Collections.singletonMap(key, element));
					joiner.append(separator).append(subMap.entrySet().iterator().next().getValue().toString());
					separator = ",";
				}
				resultMap.put(key, joiner.toString());
			} else {
				// Simple value (String, Integer, Boolean, etc)
				resultMap.put(key, value != null ? value.toString() : "");
			}
		}
		return resultMap;
	}

}
