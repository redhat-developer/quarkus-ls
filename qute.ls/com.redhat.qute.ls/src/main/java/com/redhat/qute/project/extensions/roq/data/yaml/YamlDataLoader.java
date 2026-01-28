package com.redhat.qute.project.extensions.roq.data.yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.project.extensions.roq.data.ArrayDataMapping;
import com.redhat.qute.project.extensions.roq.data.DataLoader;
import com.redhat.qute.project.extensions.roq.data.RoqDataField;
import com.redhat.qute.project.extensions.roq.data.RoqDataFile;

public class YamlDataLoader implements DataLoader {

	private static final YAMLMapper yamlMapper = new YAMLMapper();

	@Override
	public void load(RoqDataFile roqDataFile) {
		try {
			String content = Files.readString(roqDataFile.getFilePath());
			loadFromYaml(roqDataFile, content);
		} catch (IOException e) {
			roqDataFile.setFields(List.of());
			roqDataFile.setSignature(roqDataFile.getName() + " : Object");
		}
	}

	private static void loadFromYaml(RoqDataFile roqDataFile, String content) {
		try {
			JsonNode rootNode = yamlMapper.readTree(content);
			String name = roqDataFile.getName();

			List<JavaFieldInfo> fields = new ArrayList<>();

			if (rootNode.isObject()) {
				ObjectNode rootObject = (ObjectNode) rootNode;
				fields = extractFieldsFromObject(rootObject, roqDataFile);
			} else if (rootNode.isArray()) {
				// If root is an array, create a single field representing the array
				ArrayNode rootArray = (ArrayNode) rootNode;
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

	private static List<JavaFieldInfo> extractFieldsFromObject(ObjectNode objectNode, RoqDataFile fileInfo) {
		List<JavaFieldInfo> fields = new ArrayList<>();

		objectNode.fields().forEachRemaining(entry -> {
			String fieldName = entry.getKey();
			JsonNode value = entry.getValue();

			JavaFieldInfo field = createFieldFromValue(fieldName, value, fileInfo);
			if (field != null) {
				fields.add(field);
			}
		});

		return fields;
	}

	private static JavaFieldInfo createFieldFromValue(String fieldName, JsonNode value, RoqDataFile fileInfo) {
		if (value.isNull()) {
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isValueNode()) {
			// Primitive value (string, number, boolean)
			String javaType = inferJavaType(value);
			RoqDataField field = new RoqDataField(fieldName, null, fileInfo);
			field.setSignature(fieldName + " : " + javaType);
			return field;

		} else if (value.isObject()) {
			ObjectNode objectNode = (ObjectNode) value;
			ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
			objectType.setResolvedType(objectType);

			List<JavaFieldInfo> nestedFields = extractFieldsFromObject(objectNode, fileInfo);
			objectType.setFields(nestedFields);
			objectType.setSignature(fieldName + " : java.lang.Object");

			RoqDataField field = new RoqDataField(fieldName, objectType, fileInfo);
			field.setSignature(fieldName + " : java.lang.Object");
			return field;

		} else if (value.isArray()) {
			ArrayNode arrayNode = (ArrayNode) value;
			ResolvedJavaTypeInfo itemType = inferArrayItemType(arrayNode, fileInfo);
			ResolvedJavaTypeInfo arrayType = new ArrayDataMapping(itemType);

			RoqDataField field = new RoqDataField(fieldName, arrayType, fileInfo);
			field.setSignature(fieldName + " : java.util.Collection<" + itemType.getJavaElementType() + ">");
			return field;
		}

		return null;
	}

	private static ResolvedJavaTypeInfo inferArrayItemType(ArrayNode arrayNode, RoqDataFile fileInfo) {
		if (arrayNode.size() == 0) {
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		JsonNode firstItem = arrayNode.get(0);

		if (firstItem.isValueNode()) {
			String javaType = inferJavaType(firstItem);
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature(javaType);
			return type;

		} else if (firstItem.isObject()) {
			ObjectNode objectNode = (ObjectNode) firstItem;
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setResolvedType(type);

			List<JavaFieldInfo> fields = extractFieldsFromObject(objectNode, fileInfo);
			type.setFields(fields);
			type.setSignature("java.lang.Object");
			return type;
		}

		ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
		type.setSignature("java.lang.String");
		return type;
	}

	private static String inferJavaType(JsonNode node) {
		if (node.isBoolean()) {
			return "java.lang.Boolean";
		} else if (node.isNumber()) {
			if (node.isInt() || node.isLong()) {
				// Check if it fits in an Integer
				if (node.canConvertToInt()) {
					return "java.lang.Integer";
				}
				return "java.lang.Long";
			} else if (node.isFloat() || node.isDouble()) {
				return "java.lang.Double";
			}
			return "java.lang.Number";
		} else if (node.isTextual()) {
			return "java.lang.String";
		}

		return "java.lang.String";
	}
}