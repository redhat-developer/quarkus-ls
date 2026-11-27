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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.redhat.lsp4j.mcp.annotations.Tool;
import com.redhat.lsp4j.mcp.annotations.ToolArg;
import com.redhat.lsp4j.mcp.cache.McpCache;
import com.redhat.lsp4j.mcp.tools.McpTool;
import com.redhat.lsp4j.mcp.tools.Position;
import com.redhat.lsp4j.mcp.tools.TextDocumentIdentifier;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for extractMethodArgs - validates that MCP arguments (Map<String, Object>)
 * are correctly deserialized into Java method parameters via Gson.
 */
class McpMethodArgsDeserializationTest {

	private McpToolRegistry registry;
	private McpCache cache;

	@BeforeEach
	void setUp() {
		registry = new McpToolRegistry();
		cache = new McpCache();
		registry.register(McpCache.class, cache);
	}

	@Test
	void testSimpleStringParameter() {
		class SimpleTool implements McpTool {
			String receivedValue = null;

			@Tool(description = "Test")
			public void execute(@ToolArg(name = "input", description = "Input") String input) {
				this.receivedValue = input;
			}
		}

		SimpleTool tool = new SimpleTool();
		registry.scanAndRegister(tool);

		// Simulate MCP client sending arguments
		Map<String, Object> args = new HashMap<>();
		args.put("input", "hello world");

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		assertEquals("hello world", tool.receivedValue);
	}

	@Test
	void testTextDocumentIdentifier_nestedMap() {
		class TestTool implements McpTool {
			TextDocumentIdentifier receivedDoc = null;

			@Tool(description = "Test")
			public void execute(
					@ToolArg(name = "textDocument", description = "Doc") TextDocumentIdentifier textDocument) {
				this.receivedDoc = textDocument;
			}
		}

		TestTool tool = new TestTool();
		registry.scanAndRegister(tool);

		// Simulate MCP client sending: {textDocument={uri=file:///path/to/file}}
		Map<String, Object> textDocMap = new HashMap<>();
		textDocMap.put("uri", "file:///C:/Users/Test/file.html");

		Map<String, Object> args = new HashMap<>();
		args.put("textDocument", textDocMap);

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		// CRITICAL TEST: Verify TextDocumentIdentifier.uri was correctly deserialized
		assertNotNull(tool.receivedDoc, "TextDocumentIdentifier should not be null");
		assertEquals("file:///C:/Users/Test/file.html", tool.receivedDoc.getUri(),
				"URI should be correctly deserialized from nested map");
	}

	static class NestedDTO {
		private Position position;

		@ToolArg(description = "Position in file")
		public Position getPosition() {
			return position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}
	}

	@Test
	void testNestedObject_withPosition() {
		class TestTool implements McpTool {
			NestedDTO receivedData = null;

			@Tool(description = "Test")
			public void execute(@ToolArg(name = "data", description = "Data") NestedDTO data) {
				this.receivedData = data;
			}
		}

		TestTool tool = new TestTool();
		registry.scanAndRegister(tool);

		// Nested structure: {data={position={line=5, character=10}}}
		Map<String, Object> posMap = new HashMap<>();
		posMap.put("line", 5);
		posMap.put("character", 10);

		Map<String, Object> dataMap = new HashMap<>();
		dataMap.put("position", posMap);

		Map<String, Object> args = new HashMap<>();
		args.put("data", dataMap);

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		assertNotNull(tool.receivedData);
		assertNotNull(tool.receivedData.getPosition());
		assertEquals(5, tool.receivedData.getPosition().getLine());
		assertEquals(10, tool.receivedData.getPosition().getCharacter());
	}

	@Test
	void testParameterWithoutName_arg0() {
		class TestTool implements McpTool {
			String receivedValue = null;

			@Tool(description = "Test")
			public void execute(String unnamed) {
				this.receivedValue = unnamed;
			}
		}

		TestTool tool = new TestTool();
		registry.scanAndRegister(tool);

		// Without @ToolArg(name=...), parameter is named "arg0"
		Map<String, Object> args = new HashMap<>();
		args.put("arg0", "test value");

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		assertEquals("test value", tool.receivedValue);
	}

	@Test
	void testMultipleParameters() {
		class TestTool implements McpTool {
			String receivedStr = null;
			int receivedInt = 0;
			TextDocumentIdentifier receivedDoc = null;

			@Tool(description = "Test")
			public void execute(
					@ToolArg(name = "str", description = "") String str,
					@ToolArg(name = "num", description = "") int num,
					@ToolArg(name = "doc", description = "") TextDocumentIdentifier doc) {
				this.receivedStr = str;
				this.receivedInt = num;
				this.receivedDoc = doc;
			}
		}

		TestTool tool = new TestTool();
		registry.scanAndRegister(tool);

		Map<String, Object> docMap = new HashMap<>();
		docMap.put("uri", "file:///multi.html");

		Map<String, Object> args = new HashMap<>();
		args.put("str", "hello");
		args.put("num", 42);
		args.put("doc", docMap);

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		assertEquals("hello", tool.receivedStr);
		assertEquals(42, tool.receivedInt);
		assertNotNull(tool.receivedDoc);
		assertEquals("file:///multi.html", tool.receivedDoc.getUri());
	}

	@Test
	void testPrimitiveTypes() {
		class TestTool implements McpTool {
			int receivedInt = 0;
			double receivedDouble = 0.0;
			boolean receivedBool = false;

			@Tool(description = "Test")
			public void execute(
					@ToolArg(name = "i", description = "") int i,
					@ToolArg(name = "d", description = "") double d,
					@ToolArg(name = "b", description = "") boolean b) {
				this.receivedInt = i;
				this.receivedDouble = d;
				this.receivedBool = b;
			}
		}

		TestTool tool = new TestTool();
		registry.scanAndRegister(tool);

		Map<String, Object> args = new HashMap<>();
		args.put("i", 123);
		args.put("d", 3.14);
		args.put("b", true);

		McpSchema.CallToolRequest request = McpSchema.CallToolRequest.builder()
				.name("execute")
				.arguments(args)
				.build();

		LspMcpServer.McpToolRegistration toolReg = registry.getToolRegistrations().get(0);
		toolReg.getHandler().execute(null, request);

		assertEquals(123, tool.receivedInt);
		assertEquals(3.14, tool.receivedDouble, 0.001);
		assertTrue(tool.receivedBool);
	}
}
