package com.redhat.qute.project.extensions.roq.frontmatter.schema;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Provides frontmatter schema information loaded from JSON Schema. This
 * component loads the Roq frontmatter JSON Schema and exposes property metadata
 * for completion, validation, and hover support.
 */
public class YamlFrontMatterSchemaProvider {

	private static final Logger LOGGER = Logger.getLogger(YamlFrontMatterSchemaProvider.class.getName());

	private static final String SCHEMA_RESOURCE_PATH = "roq-frontmatter.schema.json";

	private static YamlFrontMatterSchemaProvider INSTANCE;

	private final Map<String, FrontMatterProperty> properties;
	private final boolean loaded;

	private YamlFrontMatterSchemaProvider() {
		this.properties = new HashMap<>();
		this.loaded = loadSchema();
	}

	/**
	 * Get the singleton instance of the schema provider.
	 * 
	 * @return the schema provider instance
	 */
	public static synchronized YamlFrontMatterSchemaProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new YamlFrontMatterSchemaProvider();
		}
		return INSTANCE;
	}

	/**
	 * Check if the schema was successfully loaded.
	 * 
	 * @return true if schema is loaded, false otherwise
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Get all frontmatter properties defined in the schema.
	 * 
	 * @return unmodifiable list of properties
	 */
	public List<FrontMatterProperty> getProperties() {
		return Collections.unmodifiableList(new ArrayList<>(properties.values()));
	}

	/**
	 * Get a specific property by name.
	 * 
	 * @param name the property name
	 * @return the property or null if not found
	 */
	public FrontMatterProperty getProperty(String name) {
		return properties.get(name);
	}

	/**
	 * Check if a property exists in the schema.
	 * 
	 * @param name the property name
	 * @return true if property exists
	 */
	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	/**
	 * Load the JSON Schema from resources.
	 * 
	 * @return true if loaded successfully
	 */
	private boolean loadSchema() {
		try (InputStream is = getClass().getResourceAsStream(SCHEMA_RESOURCE_PATH)) {
			if (is == null) {
				LOGGER.log(Level.WARNING, "Roq frontmatter schema not found at: " + SCHEMA_RESOURCE_PATH);
				return false;
			}

			JsonObject schema = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8))
					.getAsJsonObject();

			if (schema.has("properties")) {
				JsonObject propsObject = schema.getAsJsonObject("properties");

				for (String propertyName : propsObject.keySet()) {
					JsonObject propertyDef = propsObject.getAsJsonObject(propertyName);
					FrontMatterProperty property = parseProperty(propertyName, propertyDef);
					properties.put(propertyName, property);
				}
			}

			LOGGER.log(Level.INFO, "Loaded " + properties.size() + " frontmatter properties from schema");
			return true;

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to load Roq frontmatter schema", e);
			return false;
		}
	}

	/**
	 * Parse a property definition from the schema.
	 */
	private FrontMatterProperty parseProperty(String name, JsonObject definition) {
		FrontMatterProperty.Builder builder = new FrontMatterProperty.Builder(name);

		// Description
		if (definition.has("description")) {
			builder.description(definition.get("description").getAsString());
		}

		// Type
		String type = extractType(definition);
		builder.type(type);

		// Default value
		if (definition.has("default")) {
			JsonElement defaultValue = definition.get("default");
			builder.defaultValue(defaultValue.isJsonPrimitive() ? defaultValue.getAsString() : defaultValue.toString());
		}

		// Examples
		if (definition.has("examples")) {
			JsonArray examples = definition.getAsJsonArray("examples");
			List<String> examplesList = new ArrayList<>();
			for (JsonElement example : examples) {
				examplesList.add(example.isJsonPrimitive() ? example.getAsString() : example.toString());
			}
			builder.examples(examplesList);
		}

		// Enum values
		if (definition.has("enum")) {
			JsonArray enumValues = definition.getAsJsonArray("enum");
			List<String> enumList = new ArrayList<>();
			for (JsonElement value : enumValues) {
				enumList.add(value.getAsString());
			}
			builder.enumValues(enumList);
		}

		// Pattern (for validation)
		if (definition.has("pattern")) {
			builder.pattern(definition.get("pattern").getAsString());
		}

		// Handle oneOf for union types (like paginate)
		if (definition.has("oneOf")) {
			builder.isUnionType(true);
			// Could parse union type details here if needed
		}

		// Array items type
		if ("array".equals(type) && definition.has("items")) {
			JsonObject items = definition.getAsJsonObject("items");
			String itemType = extractType(items);
			builder.itemType(itemType);
		}

		// Object properties (for nested objects)
		if ("object".equals(type) && definition.has("properties")) {
			builder.isObjectType(true);
			// Could parse nested properties here if needed
		}

		return builder.build();
	}

	/**
	 * Extract the type from a property definition, handling various schema
	 * patterns.
	 */
	private String extractType(JsonObject definition) {
		if (definition.has("type")) {
			JsonElement typeElement = definition.get("type");
			if (typeElement.isJsonPrimitive()) {
				return typeElement.getAsString();
			} else if (typeElement.isJsonArray()) {
				// Handle array of types (e.g., ["string", "null"])
				JsonArray typeArray = typeElement.getAsJsonArray();
				if (typeArray.size() > 0) {
					return typeArray.get(0).getAsString();
				}
			}
		}

		// Handle oneOf/anyOf
		if (definition.has("oneOf") || definition.has("anyOf")) {
			return "mixed";
		}

		return "string"; // default fallback
	}

	/**
	 * Represents a frontmatter property from the schema.
	 */
	public static class FrontMatterProperty {
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
	}
}