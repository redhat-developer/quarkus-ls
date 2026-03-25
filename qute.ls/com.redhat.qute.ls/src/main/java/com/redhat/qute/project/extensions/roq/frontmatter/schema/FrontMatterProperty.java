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
package com.redhat.qute.project.extensions.roq.frontmatter.schema;

import java.util.Collections;
import java.util.List;

/**
 * Represents a frontmatter property from the schema.
 */
public class FrontMatterProperty {
	public static final String LAYOUT_PROPERTY = "layout";

	public static final String PAGINATE_PROPERTY = "paginate";

	public static final String IMAGE_PROPERTY = "image";

	private final String name;
	private final String description;
	private final String type;
	private final String defaultValue;
	private final List<String> examples;
	private final List<String> enumValues;
	private final String pattern;
	private final boolean isUnionType;
	private final boolean isObjectType;
	private final String itemType; // for arrays

	private FrontMatterProperty(Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.type = builder.type;
		this.defaultValue = builder.defaultValue;
		this.examples = builder.examples != null ? Collections.unmodifiableList(builder.examples)
				: Collections.emptyList();
		this.enumValues = builder.enumValues != null ? Collections.unmodifiableList(builder.enumValues)
				: Collections.emptyList();
		this.pattern = builder.pattern;
		this.isUnionType = builder.isUnionType;
		this.isObjectType = builder.isObjectType;
		this.itemType = builder.itemType;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public List<String> getExamples() {
		return examples;
	}

	public List<String> getEnumValues() {
		return enumValues;
	}

	public String getPattern() {
		return pattern;
	}

	public boolean isUnionType() {
		return isUnionType;
	}

	public boolean isObjectType() {
		return isObjectType;
	}

	public String getItemType() {
		return itemType;
	}

	public boolean hasEnumValues() {
		return !enumValues.isEmpty();
	}

	public boolean hasExamples() {
		return !examples.isEmpty();
	}

	public boolean hasDefaultValue() {
		return defaultValue != null;
	}

	/**
	 * Get a snippet value for completion. Returns the first example if available,
	 * otherwise a placeholder based on type.
	 */
	public String getSnippetValue() {
		if (!examples.isEmpty()) {
			return examples.get(0);
		}
		if (!enumValues.isEmpty()) {
			return enumValues.get(0);
		}
		if (defaultValue != null) {
			return defaultValue;
		}

		// Generate placeholder based on type
		switch (type) {
		case "string":
			return "";
		case "boolean":
			return "false";
		case "array":
			return "[]";
		case "object":
			return "{}";
		case "integer":
		case "number":
			return "0";
		default:
			return "";
		}
	}

	/**
	 * Check if this property needs quotes in YAML (strings).
	 */
	public boolean needsQuotes() {
		return "string".equals(type) && !isObjectType && !isUnionType;
	}

	static class Builder {
		private final String name;
		private String description = "";
		private String type = "string";
		private String defaultValue;
		private List<String> examples;
		private List<String> enumValues;
		private String pattern;
		private boolean isUnionType;
		private boolean isObjectType;
		private String itemType;

		public Builder(String name) {
			this.name = name;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder defaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder examples(List<String> examples) {
			this.examples = examples;
			return this;
		}

		public Builder enumValues(List<String> enumValues) {
			this.enumValues = enumValues;
			return this;
		}

		public Builder pattern(String pattern) {
			this.pattern = pattern;
			return this;
		}

		public Builder isUnionType(boolean isUnionType) {
			this.isUnionType = isUnionType;
			return this;
		}

		public Builder isObjectType(boolean isObjectType) {
			this.isObjectType = isObjectType;
			return this;
		}

		public Builder itemType(String itemType) {
			this.itemType = itemType;
			return this;
		}

		public FrontMatterProperty build() {
			return new FrontMatterProperty(this);
		}
	}

	public boolean isBoolean() {
		return "boolean".equals(getType());
	}

	public boolean isProperty(String propertyName) {
		return getName().equals(propertyName);
	}
}