/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.utils;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

/**
 * JSON Schema utilities.
 * 
 * @author Angelo ZERR
 *
 */
public class JSONSchemaUtils {

	private static final String TILDE_PROP = "~";
	// JSON Schema properties
	private static final String SCHEMA_PROP = "$schema";
	private static final String SCHEMA_URL = "http://json-schema.org/draft-07/schema#";
	private static final String ROOT_PROP = "root";
	private static final String DEFINITIONS_PROP = "definitions";
	private static final String PROFILE_PATTERN = "^[%][a-zA-Z0-9]*$";
	private static final String DEFINITIONS_ROOT = "#/definitions/root";
	private static final String PATTERN_PROPERTIES_PROP = "patternProperties";
	private static final String $REF_PROP = "$ref";
	private static final String DESCRIPTION_PROP = "description";
	private static final String PROPERTIES_PROP = "properties";
	private static final String ADDITIONAL_PROPERTIES_PROP = "additionalProperties";
	private static final String TYPE_PROP = "type";
	private static final String ENUM_PROP = "enum";
	private static final String ITEMS_PROP = "items";

	// private static final String DEFAULT_PROP = "default";

	private static enum JSONSchemaType {

		string, //
		integer, //
		number, //
		object, //
		array, //
		booleanType("boolean"), //
		nullType("null"); //

		private final String name;

		private JSONSchemaType() {
			this(null);
		}

		private JSONSchemaType(String name) {
			this.name = name;
		}

		public String getName() {
			return name != null ? name : name();
		}
	}

	/**
	 * Returns as JSON string the JSON Schema of the given <code>info</code>.
	 * 
	 * <p>
	 * The generated JSON Schema is composed by a "definitions" which is referenced
	 * by $ref:
	 * <ul>
	 * <li>on the JSON Schema root</li>
	 * <li>on a patternProperties to manage profile (%dev)</li>
	 * <li></li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Here a sample
	 * </p>
	 * : <code>
	 * {
		  "$schema": "http://json-schema.org/draft-07/schema#",
		  "definitions": {
		    "root": {
		      "type": "object",
		      "additionalProperties": false,
		      "properties": {
		        "quarkus": {
		          "type": "object",
		          "additionalProperties": false,
		          "properties": {
		            "http": {
		              "type": "object",
		              "additionalProperties": false,
		              "properties": {
		                "cors": {
		                  "type": "object",
		                  "additionalProperties": false,
		                  "properties": {
		                    "~": {
		                      "type": "boolean",
		                      "description": "Enable the CORS filter.",
		                      "additionalProperties": false
		                    },
		                    "methods": {
		                      "type": "string",
		                      "description": "HTTP methods allowed for CORS\n\n Comma separated list of valid methods. ex: GET,PUT,POST\n The filter allows any method if this is not set.\n\n default: returns any requested method as valid",
		                      "additionalProperties": false
		                    }
		                  }
		                }
		              }
		            }
		          }
		        }
		      }
		    }
		  },
		  "type": "object",
		  "patternProperties": {
		    "^[%][a-zA-Z0-9]*$": {
		      "type": "object",
		      "$ref": "#/definitions/root"
		    }
		  },
		  "$ref": "#/definitions/root"
		}
	 * 
	 * </code>
	 * 
	 * @param info    the MicroProfile project information to convert as JSON
	 *                Schema.
	 * @param lenient true if 'additionalProperties' must be set to false and false
	 *                otherwise.
	 * 
	 * @return as JSON string the JSON Schema of the given <code>info</code>.
	 */
	public static String toJSONSchema(MicroProfileProjectInfo info, boolean lenient) {
		final JsonObject schema = new JsonObject();
		schema.addProperty(SCHEMA_PROP, SCHEMA_URL);
		// Generate the JSON Schema definitions for MicroProfile properties
		generateDefinitions(info, schema, lenient);
		// Reference the JSON Schema #/definitions/root on the root
		schema.addProperty($REF_PROP, DEFINITIONS_ROOT);
		// For accepting profile ("%dev"), we use patternProperties bound to
		// #/definitions/root
		generateProfile(schema);
		return new Gson().toJson(schema);
	}

	/**
	 * Generate the JSON Schema definitions for MicroProfile properties:
	 * 
	 * <code>
	 * "definitions": {
		    "root": {
		      "type": "object",
		      "additionalProperties": false,
		      "properties": {
		        "quarkus": {
		        ...
	 * </code>
	 * 
	 * @param info   the MicroProfile project information.
	 * @param schema the JSON Schema
	 */
	private static void generateDefinitions(MicroProfileProjectInfo info, final JsonObject schema, boolean lenient) {
		JsonObject definitions = new JsonObject();
		schema.add(DEFINITIONS_PROP, definitions);

		JsonObject root = new JsonObject();
		definitions.add(ROOT_PROP, root);
		root.addProperty(TYPE_PROP, JSONSchemaType.object.getName());
		if (!lenient) {
			root.addProperty(ADDITIONAL_PROPERTIES_PROP, false);
		}
		JsonObject properties = new JsonObject();
		root.add(PROPERTIES_PROP, properties);

		List<ItemMetadata> items = info.getProperties();
		if (items != null && !items.isEmpty()) {
			items.forEach(item -> generateProperty(info, item, properties, lenient));
		}
	}

	private static void generateProperty(ConfigurationMetadata configuration, ItemMetadata item, JsonObject properties,
			boolean lenient) {
		JsonObject parent = properties;
		// property name contains '.' (ex: quarkus.application.name)
		// split it to generates the proper JSON Schema object
		String[] paths = item.getPaths();
		if (paths == null) {
			return;
		}
		for (int i = 0; i < paths.length - 1; i++) {
			String path = paths[i];
			boolean isArray = false;
			if (path.endsWith("[*]")) {
				// It's an array
				path = path.substring(0, path.length() - 3);
				isArray = true;
			}
			parent = getParentProperties(parent, path, isArray, lenient);
		}
		addProperty(paths[paths.length - 1], configuration, item, parent, lenient);
	}

	private static JsonObject getParentProperties(JsonObject parent, String path, boolean isArray, boolean lenient) {
		if (!parent.has(path)) {
			// parent has no the current path, create the JSON object
			JsonObject object = addProperty(path, isArray ? JSONSchemaType.array : JSONSchemaType.object, null, null,
					parent, lenient);
			return getOrCreateProperties(object, isArray);
		}
		JsonObject object = parent.getAsJsonObject(path);
		if (!isArray && object.has(TYPE_PROP)
				&& !JSONSchemaType.object.getName().equals(object.get(TYPE_PROP).getAsString())) {
			// Generate tilde -> see
			// https://quarkus.io/guides/config#configuration-key-conflicts
			parent.remove(path);
			JsonObject newObject = addProperty(path, JSONSchemaType.object, null, null, parent, lenient);
			JsonObject properties = new JsonObject();
			newObject.add(PROPERTIES_PROP, properties);
			properties.add(TILDE_PROP, object);
			return properties;
		}
		return getOrCreateProperties(object, isArray);
	}

	private static JsonObject getOrCreateProperties(JsonObject parent, boolean isArray) {
		if (isArray) {
			// should have items/properties
			JsonObject items = null;
			if (parent.has(ITEMS_PROP)) {
				items = parent.getAsJsonObject(ITEMS_PROP);
			} else {
				items = new JsonObject();
				parent.add(ITEMS_PROP, items);
				items.addProperty(TYPE_PROP, JSONSchemaType.object.getName());
			}
			return getOrCreateProperties(items, false);
		}
		// parent is a object : it should have properties
		if (parent.has(PROPERTIES_PROP)) {
			return parent.getAsJsonObject(PROPERTIES_PROP);
		}
		JsonObject properties = new JsonObject();
		parent.add(PROPERTIES_PROP, properties);
		return properties;
	}

	/**
	 * Generate the JSON Schema profile type definition:
	 * 
	 * <code>
	 * "patternProperties": {
	    "^[%][a-zA-Z0-9]*$": {
	      "type": "object",
	      "$ref": "#/definitions/root"
	    }
	  }
	 * 
	 * </code>
	 * 
	 * @param schema the JSON Schema
	 */
	private static void generateProfile(final JsonObject schema) {
		// For accepting profile ("%dev", use patternProperties which is bound to the
		// MicroProfile definitions
		JsonObject patternProperties = new JsonObject();
		JsonObject profile = new JsonObject();
		profile.addProperty(TYPE_PROP, JSONSchemaType.object.getName());
		profile.addProperty($REF_PROP, DEFINITIONS_ROOT);
		patternProperties.add(PROFILE_PATTERN, profile);
		schema.add(PATTERN_PROPERTIES_PROP, patternProperties);
	}

	private static JsonObject addProperty(String name, ConfigurationMetadata configuration, ItemMetadata item,
			JsonObject parent, boolean lenient) {
		List<ValueHint> values = getValues(configuration, item);
		JSONSchemaType type = getType(item, values);
		JsonObject property = addProperty(name, type, item.getDescription(), item.getDefaultValue(), parent, lenient);
		// enum
		if (values != null) {
			JsonArray enumType = new JsonArray();
			property.add(ENUM_PROP, enumType);
			for (ValueHint value : values) {
				if (value.getValue() != null && !value.getValue().isEmpty()) {
					enumType.add(value.getValue());
				}
			}
		}
		return property;

	}

	private static List<ValueHint> getValues(ConfigurationMetadata configuration, ItemMetadata item) {
		ItemHint hint = configuration.getHint(item);
		if (hint != null) {
			List<ValueHint> values = hint.getValues();
			if (values != null && !values.isEmpty()) {
				return values;
			}
		}
		return null;
	}

	private static JSONSchemaType getType(ItemMetadata item, List<ValueHint> values) {
		if (item.isStringType()) {
			return JSONSchemaType.string;
		}
		if (item.isBooleanType()) {
			return JSONSchemaType.booleanType;
		}
		if (item.isIntegerType() || item.isBigIntegerType()) {
			return JSONSchemaType.integer;
		}
		if (item.isLongType() || item.isShortType() || item.isDoubleType() || item.isFloatType() || item.isBigDecimalType()) {
			return JSONSchemaType.number;
		}
		// In case of enum and no type has been found, we use string
		return values != null ? JSONSchemaType.string : null;
	}

	private static JsonObject addProperty(String name, JSONSchemaType type, String description, String defaultValue,
			JsonObject parent, boolean lenient) {
		JsonObject property = null;
		if (name.endsWith("[*]")) {
			// This case comes from with property which ends with an array
			// ex : kubernetes.image-pull-secrets[*]
			// here we must create a JSON object array kind
			name = name.substring(0, name.length() - 3);
			// should have items/properties
			JsonObject array = new JsonObject();
			array.addProperty(TYPE_PROP, JSONSchemaType.array.getName());
			parent.add(name, array);

			JsonObject items = null;
			if (parent.has(ITEMS_PROP)) {
				items = array.getAsJsonObject(ITEMS_PROP);
			} else {
				items = new JsonObject();
				array.add(ITEMS_PROP, items);
				items.addProperty(TYPE_PROP, JSONSchemaType.object.getName());
			}
			property = items;
		} else {
			property = new JsonObject();
			parent.add(name, property);
		}

		if (type != null) {
			property.addProperty(TYPE_PROP, type.getName());
			if (JSONSchemaType.object.equals(type) && !lenient) {
				property.addProperty(ADDITIONAL_PROPERTIES_PROP, false);
			}
		}
		if (description != null) {
			property.addProperty(DESCRIPTION_PROP, description);
		}
		if (defaultValue != null && !defaultValue.isEmpty()) {
			// don't generate default value since apply of completion generate all
			// properties
			// with default value.
			// property.addProperty(DEFAULT_PROP, defaultValue);
		}
		return property;
	}
}
