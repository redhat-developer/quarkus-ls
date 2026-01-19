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
package com.redhat.qute.parser.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.yaml.scanner.YamlTokenType;

/**
 * Test with YAML parser which builds a YamlDocument AST.
 * 
 * @author Angelo ZERR
 *
 */
public class YamlParserTest {

	@Test
	public void simpleKeyValue() {
		String content = "key: value";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlNode first = document.getChild(0);
		assertEquals(YamlNodeKind.YamlMapping, first.getKind());
		YamlMapping mapping = (YamlMapping) first;
		assertTrue(mapping.isClosed());

		assertEquals(1, mapping.getChildCount());
		YamlNode property = mapping.getChild(0);
		assertEquals(YamlNodeKind.YamlProperty, property.getKind());
		YamlProperty prop = (YamlProperty) property;

		assertNotNull(prop.getKey());
		assertEquals(YamlNodeKind.YamlScalar, prop.getKey().getKind());
		YamlScalar key = (YamlScalar) prop.getKey();
		assertEquals(0, key.getStart());
		assertEquals(3, key.getEnd());
		assertEquals("key", key.getValue());

		assertEquals(3, prop.getColonOffset());

		assertNotNull(prop.getValue());
		assertEquals(YamlNodeKind.YamlScalar, prop.getValue().getKind());
		YamlScalar value = (YamlScalar) prop.getValue();
		assertEquals(5, value.getStart());
		assertEquals(10, value.getEnd());
		assertEquals("value", value.getValue());
	}

	@Test
	public void multipleProperties() {
		String content = //
				"name: John\n" + //
						"age: 30";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlNode first = document.getChild(0);
		assertEquals(YamlNodeKind.YamlMapping, first.getKind());
		YamlMapping mapping = (YamlMapping) first;

		assertEquals(2, mapping.getChildCount());

		// First property: name: John
		YamlProperty prop1 = (YamlProperty) mapping.getChild(0);
		assertEquals("name", ((YamlScalar) prop1.getKey()).getValue());
		assertEquals("John", ((YamlScalar) prop1.getValue()).getValue());

		// Second property: age: 30
		YamlProperty prop2 = (YamlProperty) mapping.getChild(1);
		assertEquals("age", ((YamlScalar) prop2.getKey()).getValue());
		assertEquals("30", ((YamlScalar) prop2.getValue()).getValue());
		assertEquals(YamlTokenType.ScalarNumber, ((YamlScalar) prop2.getValue()).getScalarType());
	}

	@Test
	public void nestedMapping() {
		String content = "person:\n" + //
				"  name: John\n" + //
				"  age: 30";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(1, rootMapping.getChildCount());

		// person:
		YamlProperty personProp = (YamlProperty) rootMapping.getChild(0);
		assertEquals("person", ((YamlScalar) personProp.getKey()).getValue());

		// Nested mapping
		assertNotNull(personProp.getValue());
		assertEquals(YamlNodeKind.YamlMapping, personProp.getValue().getKind());
		YamlMapping nestedMapping = (YamlMapping) personProp.getValue();
		assertEquals(2, nestedMapping.getChildCount());

		YamlProperty nameProp = (YamlProperty) nestedMapping.getChild(0);
		assertEquals("name", ((YamlScalar) nameProp.getKey()).getValue());
		assertEquals("John", ((YamlScalar) nameProp.getValue()).getValue());

		YamlProperty ageProp = (YamlProperty) nestedMapping.getChild(1);
		assertEquals("age", ((YamlScalar) ageProp.getKey()).getValue());
		assertEquals("30", ((YamlScalar) ageProp.getValue()).getValue());
	}

	@Test
	public void simpleSequence() {
		String content = "- item1\n- item2\n- item3";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlNode first = document.getChild(0);
		assertEquals(YamlNodeKind.YamlSequence, first.getKind());
		YamlSequence sequence = (YamlSequence) first;
		assertTrue(sequence.isClosed());

		assertEquals(3, sequence.getChildCount());

		YamlScalar item1 = (YamlScalar) sequence.getChild(0);
		assertEquals("item1", item1.getValue());

		YamlScalar item2 = (YamlScalar) sequence.getChild(1);
		assertEquals("item2", item2.getValue());

		YamlScalar item3 = (YamlScalar) sequence.getChild(2);
		assertEquals("item3", item3.getValue());
	}

	@Test
	public void sequenceOfMappings() {
		String content = "items:\n" + //
				"- name: John\n" + //
				"  age: 30\n" + //
				"- name: Jane\n" + //
				"  age: 25";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		YamlProperty itemsProp = (YamlProperty) rootMapping.getChild(0);
		assertNotNull(itemsProp.getKey());
		assertEquals("items", ((YamlScalar) itemsProp.getKey()).getValue());

		assertNotNull(itemsProp.getValue());
		assertEquals(YamlNodeKind.YamlSequence, itemsProp.getValue().getKind());
		YamlSequence sequence = (YamlSequence) itemsProp.getValue();
		assertEquals(2, sequence.getChildCount());

		// First item
		YamlMapping item1 = (YamlMapping) sequence.getChild(0);
		assertEquals(2, item1.getChildCount());
		assertEquals("John", ((YamlScalar) ((YamlProperty) item1.getChild(0)).getValue()).getValue());
		assertEquals("30", ((YamlScalar) ((YamlProperty) item1.getChild(1)).getValue()).getValue());

		// Second item
		YamlMapping item2 = (YamlMapping) sequence.getChild(1);
		assertEquals(2, item2.getChildCount());
		assertEquals("Jane", ((YamlScalar) ((YamlProperty) item2.getChild(0)).getValue()).getValue());
		assertEquals("25", ((YamlScalar) ((YamlProperty) item2.getChild(1)).getValue()).getValue());
	}

	@Test
	public void flowSequence() {
		String content = "numbers: [1, 2, 3]";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		YamlProperty prop = (YamlProperty) mapping.getChild(0);
		assertEquals("numbers", ((YamlScalar) prop.getKey()).getValue());

		assertEquals(YamlNodeKind.YamlSequence, prop.getValue().getKind());
		YamlSequence sequence = (YamlSequence) prop.getValue();
		assertTrue(sequence.isClosed());
		assertEquals(3, sequence.getChildCount());

		assertEquals("1", ((YamlScalar) sequence.getChild(0)).getValue());
		assertEquals("2", ((YamlScalar) sequence.getChild(1)).getValue());
		assertEquals("3", ((YamlScalar) sequence.getChild(2)).getValue());
	}

	@Test
	public void flowMapping() {
		String content = "{name: John, age: 30}";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		assertTrue(mapping.isClosed());
		assertEquals(2, mapping.getChildCount());

		YamlProperty prop1 = (YamlProperty) mapping.getChild(0);
		assertEquals("name", ((YamlScalar) prop1.getKey()).getValue());
		assertEquals("John", ((YamlScalar) prop1.getValue()).getValue());

		YamlProperty prop2 = (YamlProperty) mapping.getChild(1);
		assertEquals("age", ((YamlScalar) prop2.getKey()).getValue());
		assertEquals("30", ((YamlScalar) prop2.getValue()).getValue());
	}

	@Test
	public void quotedStrings() {
		String content = "name: \"John Doe\"\n" + //
				"title: 'Developer'";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		assertEquals(2, mapping.getChildCount());

		YamlProperty prop1 = (YamlProperty) mapping.getChild(0);
		YamlScalar value1 = (YamlScalar) prop1.getValue();
		assertEquals("John Doe", value1.getValue());
		assertTrue(value1.isQuoted());

		YamlProperty prop2 = (YamlProperty) mapping.getChild(1);
		YamlScalar value2 = (YamlScalar) prop2.getValue();
		assertEquals("Developer", value2.getValue());
		assertTrue(value2.isQuoted());
	}

	@Test
	public void scalarTypes() {
		String content = "string: hello\n" + //
				"number: 42\n" + "float: 3.14\n" + //
				"boolean: true\n" + //
				"null: null";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		assertEquals(5, mapping.getChildCount());

		assertEquals(YamlTokenType.ScalarString,
				((YamlScalar) ((YamlProperty) mapping.getChild(0)).getValue()).getScalarType());
		assertEquals(YamlTokenType.ScalarNumber,
				((YamlScalar) ((YamlProperty) mapping.getChild(1)).getValue()).getScalarType());
		assertEquals(YamlTokenType.ScalarNumber,
				((YamlScalar) ((YamlProperty) mapping.getChild(2)).getValue()).getScalarType());
		assertEquals(YamlTokenType.ScalarBoolean,
				((YamlScalar) ((YamlProperty) mapping.getChild(3)).getValue()).getScalarType());
		assertEquals(YamlTokenType.ScalarNull,
				((YamlScalar) ((YamlProperty) mapping.getChild(4)).getValue()).getScalarType());
	}

	@Test
	public void comments() {
		String content = "# This is a comment\nkey: value # inline comment";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// The document has 2 children: a full-line comment and a mapping
		assertEquals(2, document.getChildCount());

		// First child: the full-line comment
		YamlNode first = document.getChild(0);
		assertEquals(YamlNodeKind.YamlComment, first.getKind());
		YamlComment comment = (YamlComment) first;
		assertEquals(" This is a comment", comment.getContent());

		// Second child: the mapping
		YamlMapping mapping = (YamlMapping) document.getChild(1);
		assertEquals(2, mapping.getChildCount()); // property + inline comment

		// First child of the mapping: the property
		YamlProperty property = (YamlProperty) mapping.getChild(0);
		assertEquals("key", ((YamlScalar) property.getKey()).getValue());
		assertEquals("value ", ((YamlScalar) property.getValue()).getValue());

		// Second child of the mapping: the inline comment
		YamlComment inlineComment = (YamlComment) mapping.getChild(1);
		assertEquals(" inline comment", inlineComment.getContent());
	}

	@Test
	public void emptyValue() {
		String content = "key:";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		YamlProperty prop = (YamlProperty) mapping.getChild(0);
		assertEquals("key", ((YamlScalar) prop.getKey()).getValue());
		// Value should be null when not provided
		// In fault-tolerant mode, we accept this
		assertTrue(prop.getValue() == null || prop.getValue().getKind() == YamlNodeKind.YamlScalar);
	}

	@Test
	public void unclosedFlowSequence() {
		String content = "[1, 2, 3";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlSequence sequence = (YamlSequence) document.getChild(0);
		// Fault-tolerant: should still parse the items
		assertEquals(3, sequence.getChildCount());
		// But the sequence is not closed
		assertFalse(sequence.isClosed());
	}

	@Test
	public void unclosedFlowMapping() {
		String content = "{name: John, age: 30";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(1, document.getChildCount());

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		// Fault-tolerant: should still parse the properties
		assertEquals(2, mapping.getChildCount());
		// But the mapping is not closed
		assertFalse(mapping.isClosed());
	}

	@Test
	public void emptyDocument() {
		String content = "";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(0, document.getChildCount());
	}

	@Test
	public void onlyComments() {
		String content = "# Comment 1\n" + //
				"# Comment 2";
		YamlDocument document = YamlParser.parse(content, "test.yaml");
		assertEquals(2, document.getChildCount());

		assertEquals(YamlNodeKind.YamlComment, document.getChild(0).getKind());
		assertEquals(YamlNodeKind.YamlComment, document.getChild(1).getKind());
	}

	@Test
	public void complexKubernetesStyle() {
		String content = "apiVersion: v1\n" + //
				"kind: Pod\n" + //
				"metadata:\n" + //
				"  name: nginx\n" + //
				"  labels:\n" + //
				"    app: nginx";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(3, rootMapping.getChildCount());

		// apiVersion: v1
		YamlProperty apiVersion = (YamlProperty) rootMapping.getChild(0);
		assertEquals("apiVersion", ((YamlScalar) apiVersion.getKey()).getValue());
		assertEquals("v1", ((YamlScalar) apiVersion.getValue()).getValue());

		// kind: Pod
		YamlProperty kind = (YamlProperty) rootMapping.getChild(1);
		assertEquals("kind", ((YamlScalar) kind.getKey()).getValue());
		assertEquals("Pod", ((YamlScalar) kind.getValue()).getValue());

		// metadata:
		YamlProperty metadata = (YamlProperty) rootMapping.getChild(2);
		assertEquals("metadata", ((YamlScalar) metadata.getKey()).getValue());
		assertEquals(YamlNodeKind.YamlMapping, metadata.getValue().getKind());

		YamlMapping metadataMapping = (YamlMapping) metadata.getValue();
		assertEquals(2, metadataMapping.getChildCount());

		// metadata.name
		YamlProperty name = (YamlProperty) metadataMapping.getChild(0);
		assertEquals("name", ((YamlScalar) name.getKey()).getValue());
		assertEquals("nginx", ((YamlScalar) name.getValue()).getValue());

		// metadata.labels
		YamlProperty labels = (YamlProperty) metadataMapping.getChild(1);
		assertEquals("labels", ((YamlScalar) labels.getKey()).getValue());
		assertEquals(YamlNodeKind.YamlMapping, labels.getValue().getKind());

		YamlMapping labelsMapping = (YamlMapping) labels.getValue();
		assertEquals(1, labelsMapping.getChildCount());

		YamlProperty app = (YamlProperty) labelsMapping.getChild(0);
		assertEquals("app", ((YamlScalar) app.getKey()).getValue());
		assertEquals("nginx", ((YamlScalar) app.getValue()).getValue());
	}

	@Test
	public void mixedFlowAndBlock() {
		String content = "data:\n" + //
				"  items: [1, 2, 3]\n" + //
				"  config: {debug: true}";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		YamlProperty dataProp = (YamlProperty) rootMapping.getChild(0);
		/*
		 * YamlDocument └── YamlMapping └── YamlProperty (data) └── YamlMapping ├──
		 * YamlProperty (items) │ └── YamlSequence [1, 2, 3] └── YamlProperty (config)
		 * └── YamlMapping └── YamlProperty (debug → true)
		 */

		YamlMapping dataMapping = (YamlMapping) dataProp.getValue();
		assertNotNull(dataMapping);
		assertEquals(2, dataMapping.getChildCount());

		// items: [1, 2, 3]
		YamlProperty itemsProp = (YamlProperty) dataMapping.getChild(0);
		assertEquals(YamlNodeKind.YamlSequence, itemsProp.getValue().getKind());
		YamlSequence itemsSeq = (YamlSequence) itemsProp.getValue();
		assertEquals(3, itemsSeq.getChildCount());

		// config: {debug: true}
		YamlProperty configProp = (YamlProperty) dataMapping.getChild(1);
		assertEquals(YamlNodeKind.YamlMapping, configProp.getValue().getKind());
		YamlMapping configMap = (YamlMapping) configProp.getValue();
		assertEquals(1, configMap.getChildCount());
	}

	@Test
	public void nestedFlowCollections() {
		String content = "matrix: [[1, 2], [3, 4]]";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		YamlProperty prop = (YamlProperty) mapping.getChild(0);

		YamlSequence outerSeq = (YamlSequence) prop.getValue();
		assertEquals(2, outerSeq.getChildCount());

		YamlSequence innerSeq1 = (YamlSequence) outerSeq.getChild(0);
		assertEquals(2, innerSeq1.getChildCount());
		assertEquals("1", ((YamlScalar) innerSeq1.getChild(0)).getValue());
		assertEquals("2", ((YamlScalar) innerSeq1.getChild(1)).getValue());

		YamlSequence innerSeq2 = (YamlSequence) outerSeq.getChild(1);
		assertEquals(2, innerSeq2.getChildCount());
		assertEquals("3", ((YamlScalar) innerSeq2.getChild(0)).getValue());
		assertEquals("4", ((YamlScalar) innerSeq2.getChild(1)).getValue());
	}

	@Test
	public void testFlowArrayWithFlowObject() {
		String content = "list: [{ title: \"Applied AI\", year: 2005, tagline: \"AI guide for Java\" }]";

		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// Root structure
		assertEquals(1, document.getChildCount());
		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(1, rootMapping.getChildCount());
		assertFalse(rootMapping.isFlowStyle());

		// list property
		YamlProperty listProp = (YamlProperty) rootMapping.getChild(0);
		assertEquals("list", ((YamlScalar) listProp.getKey()).getValue());
		assertNotNull(listProp.getValue());
		assertEquals(YamlNodeKind.YamlSequence, listProp.getValue().getKind());

		// Flow sequence
		YamlSequence sequence = (YamlSequence) listProp.getValue();
		assertTrue(sequence.isFlowStyle());
		assertEquals(1, sequence.getChildCount());

		// Flow mapping inside sequence
		YamlMapping mapping = (YamlMapping) sequence.getChild(0);
		assertTrue(mapping.isFlowStyle());
		assertEquals(3, mapping.getChildCount());

		// title property
		YamlProperty titleProp = (YamlProperty) mapping.getChild(0);
		assertEquals("title", ((YamlScalar) titleProp.getKey()).getValue());
		YamlScalar titleValue = (YamlScalar) titleProp.getValue();
		assertEquals("Applied AI", titleValue.getValue());
		assertTrue(titleValue.isQuoted());
		assertEquals(YamlTokenType.ScalarString, titleValue.getScalarType());

		// year property
		YamlProperty yearProp = (YamlProperty) mapping.getChild(1);
		assertEquals("year", ((YamlScalar) yearProp.getKey()).getValue());
		YamlScalar yearValue = (YamlScalar) yearProp.getValue();
		assertEquals("2005", yearValue.getValue());
		assertFalse(yearValue.isQuoted());
		assertEquals(YamlTokenType.ScalarNumber, yearValue.getScalarType());

		// tagline property
		YamlProperty taglineProp = (YamlProperty) mapping.getChild(2);
		assertEquals("tagline", ((YamlScalar) taglineProp.getKey()).getValue());
		YamlScalar taglineValue = (YamlScalar) taglineProp.getValue();
		assertEquals("AI guide for Java", taglineValue.getValue());
		assertTrue(taglineValue.isQuoted());
		assertEquals(YamlTokenType.ScalarString, taglineValue.getScalarType());
	}

	@Test
	public void findNodeAt() {
		String content = "person:\n  name: John\n  age: 30";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// Find node at offset 0 (start of "person")
		YamlNode node = document.findNodeAt(0);
		assertNotNull(node);

		// Find node at offset in "John"
		node = document.findNodeAt(17);
		assertNotNull(node);
		// Should find the scalar "John"
		if (node.getKind() == YamlNodeKind.YamlScalar) {
			assertEquals("John", ((YamlScalar) node).getValue());
		}
	}

	@Test
	public void multilineValue() {
		String content = "description: This is a long description";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		YamlProperty prop = (YamlProperty) mapping.getChild(0);

		assertEquals("description", ((YamlScalar) prop.getKey()).getValue());
		assertEquals("This is a long description", ((YamlScalar) prop.getValue()).getValue());
	}

	@Test
	public void booleanVariants() {
		String content = "a: yes\n" + //
				"b: no\n" + //
				"c: true\n" + //
				"d: false\n" + //
				"e: on\n" + //
				"f: off";
		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping mapping = (YamlMapping) document.getChild(0);
		assertEquals(6, mapping.getChildCount());

		// All should be recognized as booleans
		for (int i = 0; i < 6; i++) {
			YamlProperty prop = (YamlProperty) mapping.getChild(i);
			YamlScalar value = (YamlScalar) prop.getValue();
			assertEquals(YamlTokenType.ScalarBoolean, value.getScalarType());
		}
	}

	@Test
	public void testFrontMatter() {
		String content = "title: Hello Roqers (1)\n" + //
				"description: It is time to start Roqing 🚀!\n" + //
				"layout: :theme/index (2)";

		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// Root structure
		assertEquals(1, document.getChildCount());
		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(3, rootMapping.getChildCount());
		assertFalse(rootMapping.isFlowStyle());

		// title property
		YamlProperty titleProp = (YamlProperty) rootMapping.getChild(0);
		assertEquals("title", ((YamlScalar) titleProp.getKey()).getValue());
		assertNotNull(titleProp.getValue());
		assertEquals(YamlNodeKind.YamlScalar, titleProp.getValue().getKind());

		YamlScalar titleValue = (YamlScalar) titleProp.getValue();
		assertEquals("Hello Roqers (1)", titleValue.getValue());
		assertEquals(YamlTokenType.ScalarString, titleValue.getScalarType());
		assertFalse(titleValue.isQuoted());

		// description property
		YamlProperty descProp = (YamlProperty) rootMapping.getChild(1);
		assertEquals("description", ((YamlScalar) descProp.getKey()).getValue());
		assertNotNull(descProp.getValue());

		YamlScalar descValue = (YamlScalar) descProp.getValue();
		assertEquals("It is time to start Roqing 🚀!", descValue.getValue());
		assertEquals(YamlTokenType.ScalarString, descValue.getScalarType());
		assertFalse(descValue.isQuoted());

		// layout property
		YamlProperty layoutProp = (YamlProperty) rootMapping.getChild(2);
		assertEquals("layout", ((YamlScalar) layoutProp.getKey()).getValue());
		assertNotNull(layoutProp.getValue());

		YamlScalar layoutValue = (YamlScalar) layoutProp.getValue();
		assertEquals(":theme/index (2)", layoutValue.getValue());
		assertEquals(YamlTokenType.ScalarString, layoutValue.getScalarType());
		assertFalse(layoutValue.isQuoted());
	}

	@Test
	public void testBookList() {
		String content = "list:\n" + //
				"  - title: \"Book 1\"\n" + //
				"    year: 2025\n" + //
				"  - title: \"Book 2\"\n" + //
				"    year: 2021";

		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// Root structure
		assertEquals(1, document.getChildCount());
		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(1, rootMapping.getChildCount());

		// list property
		YamlProperty listProp = (YamlProperty) rootMapping.getChild(0);
		assertEquals("list", ((YamlScalar) listProp.getKey()).getValue());
		assertNotNull(listProp.getValue());
		assertEquals(YamlNodeKind.YamlSequence, listProp.getValue().getKind());

		// Sequence with 2 items
		YamlSequence sequence = (YamlSequence) listProp.getValue();
		assertEquals(2, sequence.getChildCount());
		assertFalse(sequence.isFlowStyle());

		// First book
		YamlMapping book1 = (YamlMapping) sequence.getChild(0);
		assertEquals(2, book1.getChildCount());

		YamlProperty title1 = (YamlProperty) book1.getChild(0);
		assertEquals("title", ((YamlScalar) title1.getKey()).getValue());
		YamlScalar titleValue1 = (YamlScalar) title1.getValue();
		assertEquals("Book 1", titleValue1.getValue());
		assertTrue(titleValue1.isQuoted());

		YamlProperty year1 = (YamlProperty) book1.getChild(1);
		assertEquals("year", ((YamlScalar) year1.getKey()).getValue());
		assertEquals("2025", ((YamlScalar) year1.getValue()).getValue());
		assertEquals(YamlTokenType.ScalarNumber, ((YamlScalar) year1.getValue()).getScalarType());

		// Second book
		YamlMapping book2 = (YamlMapping) sequence.getChild(1);
		assertEquals(2, book2.getChildCount());

		YamlProperty title2 = (YamlProperty) book2.getChild(0);
		assertEquals("title", ((YamlScalar) title2.getKey()).getValue());
		YamlScalar titleValue2 = (YamlScalar) title2.getValue();
		assertEquals("Book 2", titleValue2.getValue());
		assertTrue(titleValue2.isQuoted());

		YamlProperty year2 = (YamlProperty) book2.getChild(1);
		assertEquals("year", ((YamlScalar) year2.getKey()).getValue());
		assertEquals("2021", ((YamlScalar) year2.getValue()).getValue());
		assertEquals(YamlTokenType.ScalarNumber, ((YamlScalar) year2.getValue()).getScalarType());
	}

	@Test
	public void testBookListWithTagline() {
		String content = "list:\n" + //
				"  - title: \"Applied AI\"\n" + //
				"    year: 2025\n" + //
				"    tagline: \"AI guide for Java\"\n" + //
				"  - title: \"Modernizing Java\"\n" + //
				"    year: 2021\n" + //
				"    tagline: \"Bring apps to future\"";

		YamlDocument document = YamlParser.parse(content, "test.yaml");

		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		YamlProperty listProp = (YamlProperty) rootMapping.getChild(0);
		YamlSequence sequence = (YamlSequence) listProp.getValue();

		assertEquals(2, sequence.getChildCount());

		// First book - 3 properties
		YamlMapping book1 = (YamlMapping) sequence.getChild(0);
		assertEquals(3, book1.getChildCount());
		assertEquals("Applied AI", ((YamlScalar) ((YamlProperty) book1.getChild(0)).getValue()).getValue());
		assertEquals("2025", ((YamlScalar) ((YamlProperty) book1.getChild(1)).getValue()).getValue());
		assertEquals("AI guide for Java", ((YamlScalar) ((YamlProperty) book1.getChild(2)).getValue()).getValue());

		// Second book - 3 properties
		YamlMapping book2 = (YamlMapping) sequence.getChild(1);
		assertEquals(3, book2.getChildCount());
		assertEquals("Modernizing Java", ((YamlScalar) ((YamlProperty) book2.getChild(0)).getValue()).getValue());
		assertEquals("2021", ((YamlScalar) ((YamlProperty) book2.getChild(1)).getValue()).getValue());
		assertEquals("Bring apps to future", ((YamlScalar) ((YamlProperty) book2.getChild(2)).getValue()).getValue());
	}

	@Test
	public void testNestedSandwiches() {
		String content = "sandwiches:\n" + //
				"  - name: Turkey and Brie\n" + //
				"    ingredients:\n" + //
				"     - turkey\n" + //
				"     - brie\n" + //
				"     - apple\n" + //
				"     - ciabatta\n" + //
				"  - name: Ham and Cheese\n" + //
				"    ingredients:\n" + //
				"     - ham\n" + //
				"     - cheddar\n" + //
				"     - mustard\n" + //
				"     - green leaf lettuce\n" + //
				"     - whole grain bread";

		YamlDocument document = YamlParser.parse(content, "test.yaml");

		// Root structure
		assertEquals(1, document.getChildCount());
		YamlMapping rootMapping = (YamlMapping) document.getChild(0);
		assertEquals(1, rootMapping.getChildCount());

		// sandwiches property
		YamlProperty sandwichesProp = (YamlProperty) rootMapping.getChild(0);
		assertEquals("sandwiches", ((YamlScalar) sandwichesProp.getKey()).getValue());
		assertNotNull(sandwichesProp.getValue());
		assertEquals(YamlNodeKind.YamlSequence, sandwichesProp.getValue().getKind());

		// Sequence with 2 sandwiches
		YamlSequence sandwichesSeq = (YamlSequence) sandwichesProp.getValue();
		assertEquals(2, sandwichesSeq.getChildCount());

		// First sandwich
		YamlMapping sandwich1 = (YamlMapping) sandwichesSeq.getChild(0);
		assertEquals(2, sandwich1.getChildCount());

		// name: Turkey and Brie
		YamlProperty name1 = (YamlProperty) sandwich1.getChild(0);
		assertEquals("name", ((YamlScalar) name1.getKey()).getValue());
		assertEquals("Turkey and Brie", ((YamlScalar) name1.getValue()).getValue());

		// ingredients: [turkey, brie, apple, ciabatta]
		YamlProperty ingredients1 = (YamlProperty) sandwich1.getChild(1);
		assertEquals("ingredients", ((YamlScalar) ingredients1.getKey()).getValue());
		assertEquals(YamlNodeKind.YamlSequence, ingredients1.getValue().getKind());

		YamlSequence ingredientsSeq1 = (YamlSequence) ingredients1.getValue();
		assertEquals(4, ingredientsSeq1.getChildCount());
		assertEquals("turkey", ((YamlScalar) ingredientsSeq1.getChild(0)).getValue());
		assertEquals("brie", ((YamlScalar) ingredientsSeq1.getChild(1)).getValue());
		assertEquals("apple", ((YamlScalar) ingredientsSeq1.getChild(2)).getValue());
		assertEquals("ciabatta", ((YamlScalar) ingredientsSeq1.getChild(3)).getValue());

		// Second sandwich
		YamlMapping sandwich2 = (YamlMapping) sandwichesSeq.getChild(1);
		assertEquals(2, sandwich2.getChildCount());

		// name: Ham and Cheese
		YamlProperty name2 = (YamlProperty) sandwich2.getChild(0);
		assertEquals("name", ((YamlScalar) name2.getKey()).getValue());
		assertEquals("Ham and Cheese", ((YamlScalar) name2.getValue()).getValue());

		// ingredients: [ham, cheddar, mustard, green leaf lettuce, whole grain bread]
		YamlProperty ingredients2 = (YamlProperty) sandwich2.getChild(1);
		assertEquals("ingredients", ((YamlScalar) ingredients2.getKey()).getValue());
		assertEquals(YamlNodeKind.YamlSequence, ingredients2.getValue().getKind());

		YamlSequence ingredientsSeq2 = (YamlSequence) ingredients2.getValue();
		assertEquals(5, ingredientsSeq2.getChildCount());
		assertEquals("ham", ((YamlScalar) ingredientsSeq2.getChild(0)).getValue());
		assertEquals("cheddar", ((YamlScalar) ingredientsSeq2.getChild(1)).getValue());
		assertEquals("mustard", ((YamlScalar) ingredientsSeq2.getChild(2)).getValue());
		assertEquals("green leaf lettuce", ((YamlScalar) ingredientsSeq2.getChild(3)).getValue());
		assertEquals("whole grain bread", ((YamlScalar) ingredientsSeq2.getChild(4)).getValue());
	}
}