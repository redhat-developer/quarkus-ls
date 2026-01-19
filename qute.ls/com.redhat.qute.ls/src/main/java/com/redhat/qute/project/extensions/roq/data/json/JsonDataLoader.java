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
package com.redhat.qute.project.extensions.roq.data.json;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.project.extensions.roq.data.RoqDataCollection;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataField;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;

/**
 * JSON data loader for Roq.
 * 
 * Converts JSON files into JavaFieldInfo structures that provide: -
 * Autocomplete support in Qute templates - Type validation - Field discovery
 * for data files
 * 
 * Uses Gson's JsonParser to parse JSON into a JsonElement tree, then
 * recursively builds the Java type structure.
 */
public class JsonDataLoader implements DataLoader {

	/**
	 * Main entry point - loads a JSON file and populates the RoqDataFile with
	 * discovered field information.
	 * 
	 * @param roqDataFile The data file object to populate with field info
	 */
	@Override
	public void load(RoqDataFile roqDataFile) {
		try {
			// Read the raw JSON content from disk
			String content = Files.readString(roqDataFile.getFilePath());
			loadFromJson(roqDataFile, content);
		} catch (IOException e) {
			// On error, initialize with empty structure to prevent NPEs
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	/**
	 * Parses JSON content and extracts the field structure.
	 * 
	 * Handles two root cases: 1. Object root (most common) - extracts all key-value
	 * pairs as fields 2. Array root (less common) - wraps in a synthetic "items"
	 * collection field
	 * 
	 * @param roqDataFile The file to populate
	 * @param content     Raw JSON string content
	 */
	private static void loadFromJson(RoqDataFile roqDataFile, String content) {
		try {
			// Parse JSON into Gson's JsonElement tree structure
			JsonElement rootElement = JsonParser.parseString(content);
			String name = roqDataFile.getName();

			List<JavaFieldInfo> fields = new ArrayList<>();

			if (rootElement.isJsonObject()) {
				// Case 1: Root is a JSON object
				// Example: { "name": "John", "age": 30 }
				JsonObject rootObject = rootElement.getAsJsonObject();
				fields = extractFieldsFromObject(rootObject, roqDataFile);

			} else if (rootElement.isJsonArray()) {
				// Case 2: Root is a JSON array
				// Example: [{ "name": "John" }, { "name": "Jane" }]
				// We wrap this in a synthetic "items" field for template access
				JsonArray rootArray = rootElement.getAsJsonArray();
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
			// Graceful fallback on any parsing error (malformed JSON, etc.)
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	/**
	 * Extracts all fields from a JSON object.
	 * 
	 * Iterates over all key-value pairs in the object and converts each to a
	 * JavaFieldInfo with appropriate type information.
	 * 
	 * @param jsonObject The Gson JsonObject representing a JSON object
	 * @param fileInfo   Reference to the parent file (for traceability)
	 * @return List of discovered fields
	 */
	private static List<JavaFieldInfo> extractFieldsFromObject(JsonObject jsonObject, RoqDataFile fileInfo) {
		List<JavaFieldInfo> fields = new ArrayList<>();

		// Iterate over all entries in the JSON object
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String fieldName = entry.getKey(); // JSON key becomes field name
			JsonElement value = entry.getValue(); // JSON value determines type

			// Convert the value to a typed field
			JavaFieldInfo field = createFieldFromValue(fieldName, value, fileInfo);
			if (field != null) {
				fields.add(field);
			}
		}

		return fields;
	}

	/**
	 * Creates a JavaFieldInfo from a JSON value, recursively handling nested
	 * structures.
	 * 
	 * Type mapping: - null → java.lang.Object - primitive (string/number/boolean) →
	 * primitive Java type - object → java.lang.Object with nested fields - array →
	 * java.util.Collection<T> with item type
	 * 
	 * @param fieldName The field name (from JSON key)
	 * @param value     The JSON value as JsonElement
	 * @param fileInfo  Parent file reference
	 * @return JavaFieldInfo representing this field, or null if invalid
	 */
	private static JavaFieldInfo createFieldFromValue(String fieldName, JsonElement value, RoqDataFile fileInfo) {
		if (value.isJsonNull()) {
			// JSON: "key": null
			// Maps to Object since we don't know the intended type
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isJsonPrimitive()) {
			// JSON: "key": "value" or "key": 123 or "key": true
			// Primitive value (string, number, boolean)
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			String javaType = inferJavaType(primitive);
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : " + javaType);
			return field;

		} else if (value.isJsonObject()) {
			// JSON: "key": { "nested": "value" }
			// Nested object - recursively extract its fields
			JsonObject jsonObject = value.getAsJsonObject();
			ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
			objectType.setResolvedType(objectType);

			// Recursively extract fields from the nested object
			List<JavaFieldInfo> nestedFields = extractFieldsFromObject(jsonObject, fileInfo);
			objectType.setFields(nestedFields);
			objectType.setSignature(fieldName + " : java.lang.Object");

			RoqDataField field = new RoqDataField(fieldName, objectType, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isJsonArray()) {
			// JSON: "key": ["item1", "item2"]
			// Array - infer the item type from first element
			JsonArray jsonArray = value.getAsJsonArray();
			ResolvedJavaTypeInfo itemType = inferArrayItemType(jsonArray, fileInfo);
			ResolvedJavaTypeInfo arrayType = new RoqDataCollection(itemType);

			RoqDataField field = new RoqDataField(fieldName, arrayType, fileInfo);
			field.setSignature(fieldName + " : java.util.Collection<" + itemType.getJavaElementType() + ">");
			return field;
		}

		return null;
	}

	/**
	 * Infers the item type of a JSON array by examining the first element.
	 * 
	 * Strategy: - Empty array → defaults to String - Array of primitives → infer
	 * type from first primitive - Array of objects → infer object structure from
	 * first object
	 * 
	 * Limitation: Assumes homogeneous arrays (all items same type)
	 * 
	 * @param jsonArray The Gson JsonArray representing a JSON array
	 * @param fileInfo  Parent file reference
	 * @return The inferred type of array items
	 */
	private static ResolvedJavaTypeInfo inferArrayItemType(JsonArray jsonArray, RoqDataFile fileInfo) {
		if (jsonArray.size() == 0) {
			// Empty array - default to String as a safe fallback
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		// Examine the first item to infer the type
		JsonElement firstItem = jsonArray.get(0);

		if (firstItem.isJsonPrimitive()) {
			// Array of primitives: ["a", "b"] or [1, 2] or [true, false]
			JsonPrimitive primitive = firstItem.getAsJsonPrimitive();
			String javaType = inferJavaType(primitive);
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature(javaType);
			return type;

		} else if (firstItem.isJsonObject()) {
			// Array of objects: [{ "name": "John" }, { "name": "Jane" }]
			// Extract the structure from the first object
			JsonObject jsonObject = firstItem.getAsJsonObject();
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setResolvedType(type);

			// Recursively extract fields from the first array item
			List<JavaFieldInfo> fields = extractFieldsFromObject(jsonObject, fileInfo);
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
	 * Infers the Java type from a JSON primitive value.
	 * 
	 * Type inference rules: - true/false → Boolean - Integer literals (123) →
	 * Integer - Large integers that exceed int range → Long - Floating point (3.14)
	 * → Double - Scientific notation (1e10) → Double - "text" → String
	 * 
	 * Note: Gson parses all JSON numbers as the most appropriate Java type
	 * (Integer, Long, Double), so we check both the string representation and the
	 * actual Number instance type.
	 * 
	 * @param primitive A Gson JsonPrimitive representing a scalar value
	 * @return The fully qualified Java type name
	 */
	private static String inferJavaType(JsonPrimitive primitive) {
		if (primitive.isBoolean()) {
			// JSON: true or false
			return "java.lang.Boolean";

		} else if (primitive.isNumber()) {
			Number number = primitive.getAsNumber();
			String str = primitive.getAsString();

			// Check if it's a floating point number by looking at the string representation
			// This catches cases like 3.14 or 1e10 that should be Double
			if (str.contains(".") || str.toLowerCase().contains("e")) {
				return "java.lang.Double";
			}

			// Check the actual Number instance type that Gson created
			if (number instanceof Double || number instanceof Float) {
				return "java.lang.Double";
			} else if (number instanceof Long) {
				// For Long, check if it actually fits in an Integer
				long val = number.longValue();
				if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
					return "java.lang.Integer";
				}
				return "java.lang.Long";
			} else {
				// Default case for Integer and other number types
				return "java.lang.Integer";
			}

		} else if (primitive.isString()) {
			// JSON: "text"
			return "java.lang.String";
		}

		// Fallback (should rarely be reached)
		return "java.lang.String";
	}
}