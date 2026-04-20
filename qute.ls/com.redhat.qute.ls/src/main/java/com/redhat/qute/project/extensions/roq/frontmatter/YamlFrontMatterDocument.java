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
package com.redhat.qute.project.extensions.roq.frontmatter;

import static com.redhat.qute.project.extensions.roq.JsonVertxConstants.IO_VERTX_CORE_JSON_JSON_ARRAY_CLASS;
import static com.redhat.qute.project.extensions.roq.JsonVertxConstants.IO_VERTX_CORE_JSON_JSON_OBJECT_CLASS;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.yaml.YamlASTVisitor;
import com.redhat.qute.parser.yaml.YamlDocument;
import com.redhat.qute.parser.yaml.YamlMapping;
import com.redhat.qute.parser.yaml.YamlNode;
import com.redhat.qute.parser.yaml.YamlProperty;
import com.redhat.qute.parser.yaml.YamlScalar;
import com.redhat.qute.parser.yaml.YamlSequence;

/**
 * YAML document representing front matter with cached type information.
 *
 * <p>
 * Extends {@link YamlDocument} to provide caching of the
 * {@link ResolvedJavaTypeInfo} that represents the YAML properties as Java
 * fields. This avoids recreating the type information on every
 * completion/hover/definition request.
 * </p>
 *
 * <h3>Example:</h3>
 * 
 * <pre>
 * ---
 * layout: main
 * title: My title
 * ---
 * </pre>
 *
 * <p>
 * The cached type will contain two fields: layout (String) and title (String).
 * </p>
 */
public class YamlFrontMatterDocument extends YamlDocument {

	private ResolvedJavaTypeInfo cachedResolvedType;

	public YamlFrontMatterDocument(TextDocument textDocument) {
		super(textDocument);
	}

	/**
	 * Returns the resolved Java type info representing YAML properties as fields.
	 *
	 * <p>
	 * The type is created once and cached for subsequent calls. The cache is
	 * invalidated when the document structure changes.
	 * </p>
	 *
	 * @return the resolved type info, or null if no properties found
	 */
	public ResolvedJavaTypeInfo getResolvedType() {
		if (cachedResolvedType == null) {
			cachedResolvedType = createResolvedType();
		}
		return cachedResolvedType;
	}

	/**
	 * Invalidates the cached type. Should be called when the document is reparsed.
	 */
	public void invalidateCache() {
		cachedResolvedType = null;
	}

	/**
	 * Creates the resolved type from YAML properties.
	 *
	 * @return the resolved type, or null if no properties
	 */
	private ResolvedJavaTypeInfo createResolvedType() {
		// Extract properties from YAML
		List<JavaFieldInfo> yamlProperties = extractYamlProperties();
		if (yamlProperties.isEmpty()) {
			return null;
		}

		// Create a ResolvedJavaTypeInfo with YAML properties
		ResolvedJavaTypeInfo yamlType = new ResolvedJavaTypeInfo();
		yamlType.setFields(yamlProperties);
		yamlType.setSignature("YamlFrontMatter : Object");

		return yamlType;
	}

	/**
	 * Extracts properties from this YAML document.
	 *
	 * @return list of Java field info representing YAML properties
	 */
	private List<JavaFieldInfo> extractYamlProperties() {
		YamlPropertyExtractor extractor = new YamlPropertyExtractor();
		this.accept(extractor);
		return extractor.getFields();
	}

	/**
	 * Visitor that extracts properties from YAML front matter.
	 */
	private static class YamlPropertyExtractor extends YamlASTVisitor {

		private final List<JavaFieldInfo> fields = new ArrayList<>();

		@Override
		public boolean visit(YamlProperty node) {
			// Extract key-value pair
			YamlScalar key = node.getKey();
			if (key == null) {
				return true;
			}

			String propertyName = key.getValue();
			if (propertyName == null || propertyName.isEmpty()) {
				return true;
			}

			// Create field with resolved type from value
			JavaFieldInfo field = createFieldFromValue(propertyName, node.getValue(), node);
			if (field != null) {
				fields.add(field);
			}

			return true;
		}

		/**
		 * Creates a JavaFieldInfo from a YAML value, handling nested structures.
		 *
		 * @param fieldName the field name
		 * @param value     the YAML value node
		 * @param property  the YAML property node (for location info)
		 * @return the field info, or null if invalid
		 */
		private JavaFieldInfo createFieldFromValue(String fieldName, YamlNode value, YamlProperty property) {
			if (value == null) {
				return new YamlFrontMatterField(fieldName, "java.lang.Object", property);
			}

			if (value instanceof YamlScalar) {
				// Scalar value: infer primitive type
				String javaType = inferScalarType((YamlScalar) value);
				return new YamlFrontMatterField(fieldName, javaType, property);

			} else if (value instanceof YamlSequence) {
				// Array/sequence: infer item type from first element
				YamlSequence sequence = (YamlSequence) value;
				ResolvedJavaTypeInfo itemType = inferSequenceItemType(sequence);

				// Create a collection type with item structure (for completion)
				com.redhat.qute.project.extensions.roq.data.RoqDataCollection arrayType =
					new com.redhat.qute.project.extensions.roq.data.RoqDataCollection(itemType);

				YamlFrontMatterField field = new YamlFrontMatterField(fieldName, null, property);
				field.setResolvedType(arrayType);
				field.setSignature(fieldName + " : " + IO_VERTX_CORE_JSON_JSON_ARRAY_CLASS);
				return field;

			} else if (value instanceof YamlMapping) {
				// Nested object: extract its fields
				YamlMapping mapping = (YamlMapping) value;
				List<JavaFieldInfo> nestedFields = extractFieldsFromMapping(mapping);

				ResolvedJavaTypeInfo objectType = new ResolvedJavaTypeInfo();
				objectType.setResolvedType(objectType);
				objectType.setFields(nestedFields);
				objectType.setSignature(IO_VERTX_CORE_JSON_JSON_OBJECT_CLASS);

				YamlFrontMatterField field = new YamlFrontMatterField(fieldName, null, property);
				field.setResolvedType(objectType);
				field.setSignature(fieldName + " : " + IO_VERTX_CORE_JSON_JSON_OBJECT_CLASS);
				return field;

			} else {
				// Other complex type: default to JsonObject
				return new YamlFrontMatterField(fieldName, IO_VERTX_CORE_JSON_JSON_OBJECT_CLASS, property);
			}
		}

		/**
		 * Infers the item type of a YAML sequence by examining the first element.
		 *
		 * @param sequence the YAML sequence
		 * @return the inferred type of sequence items
		 */
		private ResolvedJavaTypeInfo inferSequenceItemType(YamlSequence sequence) {
			if (sequence.getChildCount() == 0) {
				// Empty sequence: default to String
				ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
				type.setSignature("java.lang.String");
				return type;
			}

			// Examine first item to infer type
			YamlNode firstItem = sequence.getChild(0);

			if (firstItem instanceof YamlScalar) {
				// Sequence of scalars: ["a", "b"] or [1, 2]
				String javaType = inferScalarType((YamlScalar) firstItem);
				ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
				type.setSignature(javaType);
				return type;

			} else if (firstItem instanceof YamlMapping) {
				// Sequence of objects: extract structure from first object
				YamlMapping mapping = (YamlMapping) firstItem;
				ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
				type.setResolvedType(type);

				// Extract fields from the first item
				List<JavaFieldInfo> nestedFields = extractFieldsFromMapping(mapping);
				type.setFields(nestedFields);
				type.setSignature(IO_VERTX_CORE_JSON_JSON_OBJECT_CLASS);
				return type;
			}

			// Fallback
			ResolvedJavaTypeInfo type = new ResolvedJavaTypeInfo();
			type.setSignature("java.lang.String");
			return type;
		}

		/**
		 * Extracts fields from a YAML mapping (nested object).
		 *
		 * @param mapping the YAML mapping
		 * @return list of fields
		 */
		private List<JavaFieldInfo> extractFieldsFromMapping(YamlMapping mapping) {
			List<JavaFieldInfo> fields = new ArrayList<>();

			// Iterate over all properties in the mapping
			for (int i = 0; i < mapping.getChildCount(); i++) {
				YamlNode child = mapping.getChild(i);
				if (child instanceof YamlProperty) {
					YamlProperty property = (YamlProperty) child;
					YamlScalar key = property.getKey();
					if (key != null && key.getValue() != null && !key.getValue().isEmpty()) {
						JavaFieldInfo field = createFieldFromValue(key.getValue(), property.getValue(), property);
						if (field != null) {
							fields.add(field);
						}
					}
				}
			}

			return fields;
		}

		/**
		 * Infers Java type from YAML scalar value.
		 *
		 * @param scalar the YAML scalar
		 * @return the Java type name
		 */
		private String inferScalarType(YamlScalar scalar) {
			String scalarValue = scalar.getValue();

			// Null or empty
			if (scalarValue == null || scalarValue.isEmpty() || "null".equals(scalarValue)
					|| "~".equals(scalarValue)) {
				return "java.lang.Object";
			}

			// Boolean
			if ("true".equalsIgnoreCase(scalarValue) || "false".equalsIgnoreCase(scalarValue)
					|| "yes".equalsIgnoreCase(scalarValue) || "no".equalsIgnoreCase(scalarValue)) {
				return "java.lang.Boolean";
			}

			// Number (simple check)
			try {
				Integer.parseInt(scalarValue);
				return "java.lang.Integer";
			} catch (NumberFormatException e) {
				// Not an integer
			}

			try {
				Double.parseDouble(scalarValue);
				return "java.lang.Double";
			} catch (NumberFormatException e) {
				// Not a double
			}

			// Default to String
			return "java.lang.String";
		}

		public List<JavaFieldInfo> getFields() {
			return fields;
		}
	}
}
