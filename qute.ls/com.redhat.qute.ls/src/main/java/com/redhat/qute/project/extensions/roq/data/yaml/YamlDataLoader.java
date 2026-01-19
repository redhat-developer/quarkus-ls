/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.roq.data.yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.project.extensions.roq.data.RoqDataCollection;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataField;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;

/**
 * YAML data loader for Roq.
 * 
 * Converts YAML files into JavaFieldInfo structures that provide: -
 * Autocomplete support in Qute templates - Type validation - Field discovery
 * for data files
 * 
 * Uses Jackson's YAMLMapper to parse YAML into a JsonNode tree, then
 * recursively builds the Java type structure.
 */
public class YamlDataLoader implements DataLoader {

	// Static YAML parser instance - reused across all file loads
	private static final YAMLMapper yamlMapper = new YAMLMapper();

	/**
	 * Main entry point - loads a YAML file and populates the RoqDataFile with
	 * discovered field information.
	 * 
	 * @param roqDataFile The data file object to populate with field info
	 */
	@Override
	public void load(RoqDataFile roqDataFile) {
		try {
			// Read the raw YAML content from disk
			String content = Files.readString(roqDataFile.getFilePath());
			loadFromYaml(roqDataFile, content);
		} catch (IOException e) {
			// On error, initialize with empty structure to prevent NPEs
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	/**
	 * Parses YAML content and extracts the field structure.
	 * 
	 * Handles two root cases: 1. Object/Map root (most common) - extracts all
	 * key-value pairs as fields 2. Array root (less common) - wraps in a synthetic
	 * "items" collection field
	 * 
	 * @param roqDataFile The file to populate
	 * @param content     Raw YAML string content
	 */
	private static void loadFromYaml(RoqDataFile roqDataFile, String content) {
		try {
			// Parse YAML into Jackson's JsonNode tree structure
			// (YAMLMapper produces the same JsonNode as JSON parsing)
			JsonNode rootNode = yamlMapper.readTree(content);
			String name = roqDataFile.getName();

			List<JavaFieldInfo> fields = new ArrayList<>();

			if (rootNode.isObject()) {
				// Case 1: Root is a YAML map/object
				// Example: { name: "John", age: 30 }
				ObjectNode rootObject = (ObjectNode) rootNode;
				fields = extractFieldsFromObject(rootObject, roqDataFile);

			} else if (rootNode.isArray()) {
				// Case 2: Root is a YAML sequence/array
				// Example: [{ name: "John" }, { name: "Jane" }]
				// We wrap this in a synthetic "items" field for template access
				ArrayNode rootArray = (ArrayNode) rootNode;
				ResolvedJavaTypeInfo itemType = inferArrayItemType(rootArray, roqDataFile);
				ResolvedJavaTypeInfo arrayType = new RoqDataCollection(itemType);

				RoqDataField field = new RoqDataField("items", arrayType, roqDataFile);
				field.setSignature("items : java.util.Collection<" + itemType.getJavaElementType() + ">");
				fields.add(field);
			}

			// Set the discovered fields and overall signature
			roqDataFile.setFields(fields);
			roqDataFile.setSignature(name + " : file");

		} catch (Exception e) {
			// Graceful fallback on any parsing error
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	/**
	 * Extracts all fields from a YAML object/map.
	 * 
	 * Iterates over all key-value pairs in the object and converts each to a
	 * JavaFieldInfo with appropriate type information.
	 * 
	 * @param objectNode The Jackson ObjectNode representing a YAML map
	 * @param fileInfo   Reference to the parent file (for traceability)
	 * @return List of discovered fields
	 */
	private static List<JavaFieldInfo> extractFieldsFromObject(ObjectNode objectNode, RoqDataFile fileInfo) {
		List<JavaFieldInfo> fields = new ArrayList<>();

		// Iterate over all entries in the YAML map
		objectNode.fields().forEachRemaining(entry -> {
			String fieldName = entry.getKey(); // YAML key becomes field name
			JsonNode value = entry.getValue(); // YAML value determines type

			// Convert the value to a typed field
			JavaFieldInfo field = createFieldFromValue(fieldName, value, fileInfo);
			if (field != null) {
				fields.add(field);
			}
		});

		return fields;
	}

	/**
	 * Creates a JavaFieldInfo from a YAML value, recursively handling nested
	 * structures.
	 * 
	 * Type mapping: - null → java.lang.Object - scalar (string/number/boolean) →
	 * primitive Java type - object/map → java.lang.Object with nested fields -
	 * array/sequence → java.util.Collection<T> with item type
	 * 
	 * @param fieldName The field name (from YAML key)
	 * @param value     The YAML value as JsonNode
	 * @param fileInfo  Parent file reference
	 * @return JavaFieldInfo representing this field, or null if invalid
	 */
	private static JavaFieldInfo createFieldFromValue(String fieldName, JsonNode value, RoqDataFile fileInfo) {
		if (value.isNull()) {
			// YAML: key: null or key: ~
			// Maps to Object since we don't know the intended type
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isValueNode()) {
			// YAML: key: "value" or key: 123 or key: true
			// Primitive scalar value (string, number, boolean)
			String javaType = inferJavaType(value);
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : " + javaType);
			return field;

		} else if (value.isObject()) {
			// YAML: key: { nested: "value" }
			// Nested object - recursively extract its fields
			ObjectNode objectNode = (ObjectNode) value;
			ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
			objectType.setResolvedType(objectType);

			// Recursively extract fields from the nested object
			List<JavaFieldInfo> nestedFields = extractFieldsFromObject(objectNode, fileInfo);
			objectType.setFields(nestedFields);
			objectType.setSignature(fieldName + " : java.lang.Object");

			RoqDataField field = new RoqDataField(fieldName, objectType, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isArray()) {
			// YAML: key: [item1, item2] or key: - item1
			// - item2
			// Array/sequence - infer the item type from first element
			ArrayNode arrayNode = (ArrayNode) value;
			ResolvedJavaTypeInfo itemType = inferArrayItemType(arrayNode, fileInfo);
			ResolvedJavaTypeInfo arrayType = new RoqDataCollection(itemType);

			RoqDataField field = new RoqDataField(fieldName, arrayType, fileInfo);
			field.setSignature(fieldName + " : java.util.Collection<" + itemType.getJavaElementType() + ">");
			return field;
		}

		return null;
	}

	/**
	 * Infers the item type of a YAML array by examining the first element.
	 * 
	 * Strategy: - Empty array → defaults to String - Array of scalars → infer type
	 * from first scalar - Array of objects → infer object structure from first
	 * object
	 * 
	 * Limitation: Assumes homogeneous arrays (all items same type)
	 * 
	 * @param arrayNode The Jackson ArrayNode representing a YAML sequence
	 * @param fileInfo  Parent file reference
	 * @return The inferred type of array items
	 */
	private static ResolvedJavaTypeInfo inferArrayItemType(ArrayNode arrayNode, RoqDataFile fileInfo) {
		if (arrayNode.size() == 0) {
			// Empty array - default to String as a safe fallback
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		// Examine the first item to infer the type
		JsonNode firstItem = arrayNode.get(0);

		if (firstItem.isValueNode()) {
			// Array of primitives: ["a", "b"] or [1, 2] or [true, false]
			String javaType = inferJavaType(firstItem);
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature(javaType);
			return type;

		} else if (firstItem.isObject()) {
			// Array of objects: [{ name: "John" }, { name: "Jane" }]
			// Extract the structure from the first object
			ObjectNode objectNode = (ObjectNode) firstItem;
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setResolvedType(type);

			// Recursively extract fields from the first array item
			List<JavaFieldInfo> fields = extractFieldsFromObject(objectNode, fileInfo);
			type.setFields(fields);
			type.setSignature("java.lang.Object");
			return type;
		}

		// Fallback for other types (arrays of arrays, etc.)
		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature("java.lang.String");
		return type;
	}

	/**
	 * Infers the Java type from a YAML scalar value.
	 * 
	 * Type inference rules: - true/false → Boolean - 123 → Integer (if fits in int
	 * range) - 999999999999 → Long (if exceeds int range) - 3.14 → Double - "text"
	 * → String
	 * 
	 * @param node A Jackson JsonNode representing a scalar value
	 * @return The fully qualified Java type name
	 */
	private static String inferJavaType(JsonNode node) {
		if (node.isBoolean()) {
			// YAML: true, false, yes, no, on, off
			return "java.lang.Boolean";

		} else if (node.isNumber()) {
			if (node.isInt() || node.isLong()) {
				// Integer or Long - check if it fits in Integer range
				if (node.canConvertToInt()) {
					return "java.lang.Integer";
				}
				return "java.lang.Long";
			} else if (node.isFloat() || node.isDouble()) {
				// Floating point number
				return "java.lang.Double";
			}
			// Fallback for other numeric types
			return "java.lang.Number";

		} else if (node.isTextual()) {
			// YAML: "text", 'text', or unquoted text
			return "java.lang.String";
		}

		// Default fallback
		return "java.lang.String";
	}
}