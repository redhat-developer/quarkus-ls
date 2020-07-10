/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ItemHint.ValueHint;
import org.eclipse.lsp4mp.utils.JSONSchemaUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for converting {@link MicroProfileProjectInfo} to JSON Schema for YAML
 * support.
 * 
 * @author Angelo ZERR
 *
 */
public class JSONSchemaUtilsTest {

	@Test
	public void simple() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		addItem("quarkus.application.name", "java.lang.String",
				"The name of the application.\n If not set, defaults to the name of the project.", info);
		addItem("quarkus.application.version", "java.lang.String",
				"The version of the application.\n If not set, defaults to the version of the project", info);
		String jsonSchema = JSONSchemaUtils.toJSONSchema(info, true);
		Assert.assertEquals(
				"{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"root\":{\"type\":\"object\",\"properties\":{\"quarkus\":{\"type\":\"object\",\"properties\":{\"application\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"description\":\"The name of the application.\\n If not set, defaults to the name of the project.\"},\"version\":{\"type\":\"string\",\"description\":\"The version of the application.\\n If not set, defaults to the version of the project\"}}}}}}}},\"$ref\":\"#/definitions/root\",\"patternProperties\":{\"^[%][a-zA-Z0-9]*$\":{\"type\":\"object\",\"$ref\":\"#/definitions/root\"}}}",
				jsonSchema);
		// Not lenient
		jsonSchema = JSONSchemaUtils.toJSONSchema(info, false);
		Assert.assertEquals(
				"{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"root\":{\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"quarkus\":{\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"application\":{\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"name\":{\"type\":\"string\",\"description\":\"The name of the application.\\n If not set, defaults to the name of the project.\"},\"version\":{\"type\":\"string\",\"description\":\"The version of the application.\\n If not set, defaults to the version of the project\"}}}}}}}},\"$ref\":\"#/definitions/root\",\"patternProperties\":{\"^[%][a-zA-Z0-9]*$\":{\"type\":\"object\",\"$ref\":\"#/definitions/root\"}}}",
				jsonSchema);
	}

	/**
	 * See https://quarkus.io/guides/config#configuration-key-conflicts
	 */
	@Test
	public void tilde() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		addItem("quarkus.http.cors", "boolean", "Enable the CORS filter.", info);
		addItem("quarkus.http.cors.methods", "java.util.Optional<java.lang.String>",
				"HTTP methods allowed for CORS\n\n Comma separated list of valid methods. ex: GET,PUT,POST\n The filter allows any method if this is not set.\n\n default: returns any requested method as valid",
				info);
		String jsonSchema = JSONSchemaUtils.toJSONSchema(info, true);
		Assert.assertEquals(
				"{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"root\":{\"type\":\"object\",\"properties\":{\"quarkus\":{\"type\":\"object\",\"properties\":{\"http\":{\"type\":\"object\",\"properties\":{\"cors\":{\"type\":\"object\",\"properties\":{\"~\":{\"type\":\"boolean\",\"description\":\"Enable the CORS filter.\"},\"methods\":{\"type\":\"string\",\"description\":\"HTTP methods allowed for CORS\\n\\n Comma separated list of valid methods. ex: GET,PUT,POST\\n The filter allows any method if this is not set.\\n\\n default: returns any requested method as valid\"}}}}}}}}}},\"$ref\":\"#/definitions/root\",\"patternProperties\":{\"^[%][a-zA-Z0-9]*$\":{\"type\":\"object\",\"$ref\":\"#/definitions/root\"}}}",
				jsonSchema);
	}

	@Test
	public void enumType() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		addItem("quarkus.log.console.async.overflow", "org.jboss.logmanager.handlers.AsyncHandler$OverflowAction", null,
				info);
		// Create hints
		List<ValueHint> values = new ArrayList<>();
		ItemHint hint = new ItemHint();
		hint.setName("org.jboss.logmanager.handlers.AsyncHandler$OverflowAction");
		hint.setValues(values);
		info.setHints(Arrays.asList(hint));
		ValueHint value = new ValueHint();
		value.setValue("BLOCK");
		values.add(value);
		value = new ValueHint();
		value.setValue("DISCARD");
		values.add(value);
		String jsonSchema = JSONSchemaUtils.toJSONSchema(info, true);
		Assert.assertEquals(
				"{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"root\":{\"type\":\"object\",\"properties\":{\"quarkus\":{\"type\":\"object\",\"properties\":{\"log\":{\"type\":\"object\",\"properties\":{\"console\":{\"type\":\"object\",\"properties\":{\"async\":{\"type\":\"object\",\"properties\":{\"overflow\":{\"type\":\"string\",\"enum\":[\"BLOCK\",\"DISCARD\"]}}}}}}}}}}}},\"$ref\":\"#/definitions/root\",\"patternProperties\":{\"^[%][a-zA-Z0-9]*$\":{\"type\":\"object\",\"$ref\":\"#/definitions/root\"}}}",
				jsonSchema);
	}

	@Test
	public void array() {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		addItem("kubernetes.init-containers[*].image", "java.lang.String", null, info);
		addItem("kubernetes.init-containers[*].env-vars[*].name", "java.lang.String", null, info);
		addItem("kubernetes.init-containers[*].env-vars[*].value", "java.lang.String", null, info);
		addItem("kubernetes.image-pull-secrets[*]", "java.lang.String", null, info);
		String jsonSchema = JSONSchemaUtils.toJSONSchema(info, true);
		Assert.assertEquals(
				"{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"root\":{\"type\":\"object\",\"properties\":{\"kubernetes\":{\"type\":\"object\",\"properties\":{\"init-containers\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"image\":{\"type\":\"string\"},\"env-vars\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}}}}},\"image-pull-secrets\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}}}}}},\"$ref\":\"#/definitions/root\",\"patternProperties\":{\"^[%][a-zA-Z0-9]*$\":{\"type\":\"object\",\"$ref\":\"#/definitions/root\"}}}",
				jsonSchema);
	}

	private static void addItem(String name, String type, String description, MicroProfileProjectInfo info) {
		addItem(name, type, description, null, info);
	}

	private static void addItem(String name, String type, String description, String defaultValue,
			MicroProfileProjectInfo info) {
		if (info.getProperties() == null) {
			info.setProperties(new ArrayList<>());
		}
		ItemMetadata item = new ItemMetadata();
		item.setName(name);
		item.setType(type);
		item.setDescription(description);
		info.getProperties().add(item);
	}

}
