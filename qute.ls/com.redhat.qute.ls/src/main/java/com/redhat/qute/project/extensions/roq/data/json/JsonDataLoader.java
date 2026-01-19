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
import com.redhat.qute.project.extensions.roq.data.ArrayDataMapping;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataField;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;

public class JsonDataLoader implements DataLoader {

	@Override
	public void load(RoqDataFile roqDataFile) {
		try {
			String content = Files.readString(roqDataFile.getFilePath());
			loadFromJson(roqDataFile, content);
		} catch (IOException e) {
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	private static void loadFromJson(RoqDataFile roqDataFile, String content) {
		try {
			JsonElement rootElement = JsonParser.parseString(content);
			String name = roqDataFile.getName();

			List<JavaFieldInfo> fields = new ArrayList<>();

			if (rootElement.isJsonObject()) {
				JsonObject rootObject = rootElement.getAsJsonObject();
				fields = extractFieldsFromObject(rootObject, roqDataFile);
			} else if (rootElement.isJsonArray()) {
				// If root is an array, create a single field representing the array
				JsonArray rootArray = rootElement.getAsJsonArray();
				ResolvedJavaTypeInfo itemType = inferArrayItemType(rootArray, roqDataFile);
				ResolvedJavaTypeInfo arrayType = new ArrayDataMapping(itemType);

				RoqDataField field = new RoqDataField("items", arrayType, roqDataFile);
				field.setSignature("items : java.util.Collection<" + itemType.getJavaElementType() + ">");
				fields.add(field);
			}

			roqDataFile.setFields(fields);
			roqDataFile.setSignature(name + " : file");

		} catch (Exception e) {
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	private static List<JavaFieldInfo> extractFieldsFromObject(JsonObject jsonObject, RoqDataFile fileInfo) {
		List<JavaFieldInfo> fields = new ArrayList<>();

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String fieldName = entry.getKey();
			JsonElement value = entry.getValue();

			JavaFieldInfo field = createFieldFromValue(fieldName, value, fileInfo);
			if (field != null) {
				fields.add(field);
			}
		}

		return fields;
	}

	private static JavaFieldInfo createFieldFromValue(String fieldName, JsonElement value, RoqDataFile fileInfo) {
		if (value.isJsonNull()) {
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isJsonPrimitive()) {
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			String javaType = inferJavaType(primitive);
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : " + javaType);
			return field;

		} else if (value.isJsonObject()) {
			JsonObject jsonObject = value.getAsJsonObject();
			ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
			objectType.setResolvedType(objectType);

			List<JavaFieldInfo> nestedFields = extractFieldsFromObject(jsonObject, fileInfo);
			objectType.setFields(nestedFields);
			objectType.setSignature(fieldName + " : java.lang.Object");

			RoqDataField field = new RoqDataField(fieldName, objectType, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isJsonArray()) {
			JsonArray jsonArray = value.getAsJsonArray();
			ResolvedJavaTypeInfo itemType = inferArrayItemType(jsonArray, fileInfo);
			ResolvedJavaTypeInfo arrayType = new ArrayDataMapping(itemType);

			RoqDataField field = new RoqDataField(fieldName, arrayType, fileInfo);
			field.setSignature(fieldName + " : java.util.Collection<" + itemType.getJavaElementType() + ">");
			return field;
		}

		return null;
	}

	private static ResolvedJavaTypeInfo inferArrayItemType(JsonArray jsonArray, RoqDataFile fileInfo) {
		if (jsonArray.size() == 0) {
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		JsonElement firstItem = jsonArray.get(0);

		if (firstItem.isJsonPrimitive()) {
			JsonPrimitive primitive = firstItem.getAsJsonPrimitive();
			String javaType = inferJavaType(primitive);
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature(javaType);
			return type;

		} else if (firstItem.isJsonObject()) {
			JsonObject jsonObject = firstItem.getAsJsonObject();
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setResolvedType(type);

			// For nested objects, we need a dummy fileInfo - you might want to pass this
			// properly
			List<JavaFieldInfo> fields = extractFieldsFromObject(jsonObject, fileInfo);
			type.setFields(fields);
			type.setSignature("java.lang.Object");
			return type;
		}

		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature("java.lang.String");
		return type;
	}

	private static String inferJavaType(JsonPrimitive primitive) {
		if (primitive.isBoolean()) {
			return "java.lang.Boolean";
		} else if (primitive.isNumber()) {
			Number number = primitive.getAsNumber();
			String str = primitive.getAsString();

			// Check if it's a floating point number
			if (str.contains(".") || str.toLowerCase().contains("e")) {
				return "java.lang.Double";
			}

			// Check the actual value range
			if (number instanceof Double || number instanceof Float) {
				return "java.lang.Double";
			} else if (number instanceof Long) {
				long val = number.longValue();
				if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
					return "java.lang.Integer";
				}
				return "java.lang.Long";
			} else {
				return "java.lang.Integer";
			}
		} else if (primitive.isString()) {
			return "java.lang.String";
		}

		return "java.lang.String";
	}
}