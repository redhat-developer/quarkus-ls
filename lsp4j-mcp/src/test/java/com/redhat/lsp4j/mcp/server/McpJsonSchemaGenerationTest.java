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
package com.redhat.lsp4j.mcp.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.lsp4j.mcp.annotations.Tool;
import com.redhat.lsp4j.mcp.annotations.ToolArg;
import com.redhat.lsp4j.mcp.tools.McpTool;

/**
 * Test JSON Schema generation from @Tool and @ToolArg annotations.
 * Compares generated schemas with expected JSON to see exactly what MCP clients receive.
 */
class McpJsonSchemaGenerationTest {

	private McpToolRegistry registry;
	private Gson gson;

	@BeforeEach
	void setUp() {
		registry = new McpToolRegistry();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	@Test
	void testSimpleStringParameter_withName() throws Exception {
		class SimpleTool implements McpTool {
			@Tool(description = "A simple tool")
			public String execute(@ToolArg(name = "input", description = "Input string") String input) {
				return input;
			}
		}

		String actualJson = generateSchemaJson(SimpleTool.class, "execute", String.class);

		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "input": {
		      "type": "string",
		      "description": "Input string"
		    }
		  },
		  "required": [
		    "input"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	@Test
	void testSimpleParameter_withoutName_generatesArg0() throws Exception {
		class SimpleTool implements McpTool {
			@Tool(description = "Test tool")
			public String execute(String unnamed) {
				return unnamed;
			}
		}

		String actualJson = generateSchemaJson(SimpleTool.class, "execute", String.class);

		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "arg0": {
		      "type": "string",
		      "description": ""
		    }
		  },
		  "required": [
		    "arg0"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	@Test
	void testNestedObject_withToolArgOnGetters() throws Exception {
		class NestedDTO {
			@ToolArg(description = "The URI")
			public String getUri() {
				return null;
			}

			@ToolArg(description = "The line number")
			public int getLine() {
				return 0;
			}
		}

		class ToolWithNested implements McpTool {
			@Tool(description = "Tool with nested object")
			public void execute(@ToolArg(name = "request", description = "The request") NestedDTO request) {
			}
		}

		String actualJson = generateSchemaJson(ToolWithNested.class, "execute", NestedDTO.class);

		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "request": {
		      "type": "object",
		      "properties": {
		        "uri": {
		          "type": "string",
		          "description": "The URI"
		        },
		        "line": {
		          "type": "integer",
		          "description": "The line number"
		        }
		      },
		      "required": [
		        "uri",
		        "line"
		      ]
		    }
		  },
		  "required": [
		    "request"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	@Test
	void testNestedObject_separateRequiredArrays_bugFix() throws Exception {
		// CRITICAL: Verifies the bug fix where nested required arrays
		// were incorrectly sharing the same list as the root required array
		class NestedDTO {
			@ToolArg(description = "Required field 1")
			public String getField1() {
				return null;
			}

			@ToolArg(description = "Required field 2")
			public String getField2() {
				return null;
			}
		}

		class ToolWithNested implements McpTool {
			@Tool(description = "Test")
			public void execute(@ToolArg(name = "request", description = "Request") NestedDTO request) {
			}
		}

		String actualJson = generateSchemaJson(ToolWithNested.class, "execute", NestedDTO.class);

		// The bug was: nested required contained ["request", "field1", "field2"]
		// The fix ensures: root required = ["request"], nested required = ["field1", "field2"]
		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "request": {
		      "type": "object",
		      "properties": {
		        "field1": {
		          "type": "string",
		          "description": "Required field 1"
		        },
		        "field2": {
		          "type": "string",
		          "description": "Required field 2"
		        }
		      },
		      "required": [
		        "field1",
		        "field2"
		      ]
		    }
		  },
		  "required": [
		    "request"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	@Test
	void testMultipleParameters() throws Exception {
		class MultiParamTool implements McpTool {
			@Tool(description = "Test")
			public void execute(
					@ToolArg(name = "param1", description = "First") String p1,
					@ToolArg(name = "param2", description = "Second") int p2) {
			}
		}

		String actualJson = generateSchemaJson(MultiParamTool.class, "execute", String.class, int.class);

		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "param1": {
		      "type": "string",
		      "description": "First"
		    },
		    "param2": {
		      "type": "integer",
		      "description": "Second"
		    }
		  },
		  "required": [
		    "param1",
		    "param2"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	@Test
	void testTypeMapping_allPrimitives() throws Exception {
		class TypeTool implements McpTool {
			@Tool(description = "Test")
			public void execute(
					@ToolArg(name = "str", description = "String param") String s,
					@ToolArg(name = "i", description = "Int param") int i,
					@ToolArg(name = "l", description = "Long param") long l,
					@ToolArg(name = "d", description = "Double param") double d,
					@ToolArg(name = "f", description = "Float param") float f,
					@ToolArg(name = "b", description = "Boolean param") boolean b) {
			}
		}

		String actualJson = generateSchemaJson(TypeTool.class, "execute",
				String.class, int.class, long.class, double.class, float.class, boolean.class);

		String expectedJson = """
		{
		  "type": "object",
		  "properties": {
		    "str": {
		      "type": "string",
		      "description": "String param"
		    },
		    "i": {
		      "type": "integer",
		      "description": "Int param"
		    },
		    "l": {
		      "type": "integer",
		      "description": "Long param"
		    },
		    "d": {
		      "type": "number",
		      "description": "Double param"
		    },
		    "f": {
		      "type": "number",
		      "description": "Float param"
		    },
		    "b": {
		      "type": "boolean",
		      "description": "Boolean param"
		    }
		  },
		  "required": [
		    "str",
		    "i",
		    "l",
		    "d",
		    "f",
		    "b"
		  ]
		}
		""";

		assertJsonEquals(expectedJson, actualJson);
	}

	// Helper methods

	private String generateSchemaJson(Class<?> toolClass, String methodName, Class<?>... paramTypes)
			throws Exception {
		Method method = toolClass.getMethod(methodName, paramTypes);
		Map<String, Object> schema = generateInputSchema(method);
		return gson.toJson(schema);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> generateInputSchema(Method method) throws Exception {
		Method generateMethod = McpToolRegistry.class.getDeclaredMethod("generateInputSchema", Method.class);
		generateMethod.setAccessible(true);
		return (Map<String, Object>) generateMethod.invoke(registry, method);
	}

	@SuppressWarnings("unchecked")
	private void assertJsonEquals(String expectedJson, String actualJson) {
		// Parse both JSON strings
		Object expected = gson.fromJson(expectedJson, Object.class);
		Object actual = gson.fromJson(actualJson, Object.class);

		// Normalize "required" arrays (sort them) since reflection order is not guaranteed
		normalizeRequiredArrays(expected);
		normalizeRequiredArrays(actual);

		// Compare objects directly (Map.equals() ignores key order)
		assertEquals(expected, actual,
				"\nExpected JSON:\n" + gson.toJson(expected) + "\n\nActual JSON:\n" + gson.toJson(actual));
	}

	@SuppressWarnings("unchecked")
	private void normalizeRequiredArrays(Object obj) {
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			// Sort "required" array if present
			if (map.containsKey("required") && map.get("required") instanceof List) {
				List<String> required = (List<String>) map.get("required");
				required.sort(String::compareTo);
			}
			// Recursively normalize nested objects
			for (Object value : map.values()) {
				normalizeRequiredArrays(value);
			}
		} else if (obj instanceof List) {
			for (Object item : (List<?>) obj) {
				normalizeRequiredArrays(item);
			}
		}
	}
}
