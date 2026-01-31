package com.redhat.qute.parser.yaml.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.redhat.qute.parser.scanner.Scanner;

/**
 * Tests for YAML scanner {@link YamlScanner}.
 * 
 * @author Your Name
 *
 */
public class YamlScannerTest {

	private Scanner<YamlTokenType, YamlScannerState> scanner;

	// ========== Basic Key-Value Tests ==========

	@Test
	public void testSimpleKeyValue() {
		scanner = YamlScanner.createScanner("key: value");
		assertOffsetAndToken(0, YamlTokenType.Key, "key");
		assertOffsetAndToken(3, YamlTokenType.Colon, ":");
		assertOffsetAndToken(4, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(5, YamlTokenType.ScalarString, "value");
		assertOffsetAndToken(10, YamlTokenType.EOS, "");
	}

	@Test
	public void testKeyValueWithNumber() {
		scanner = YamlScanner.createScanner("port: 8080");
		assertOffsetAndToken(0, YamlTokenType.Key, "port");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ScalarNumber, "8080");
		assertOffsetAndToken(10, YamlTokenType.EOS, "");
	}

	@Test
	public void testKeyValueWithBoolean() {
		scanner = YamlScanner.createScanner("enabled: true");
		assertOffsetAndToken(0, YamlTokenType.Key, "enabled");
		assertOffsetAndToken(7, YamlTokenType.Colon, ":");
		assertOffsetAndToken(8, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(9, YamlTokenType.ScalarBoolean, "true");
		assertOffsetAndToken(13, YamlTokenType.EOS, "");
	}

	@Test
	public void testKeyValueWithNull() {
		scanner = YamlScanner.createScanner("value: null");
		assertOffsetAndToken(0, YamlTokenType.Key, "value");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarNull, "null");
		assertOffsetAndToken(11, YamlTokenType.EOS, "");
	}

	@Test
	public void testMultipleKeyValues() {
		scanner = YamlScanner.createScanner("name: John\n" + //
				"age: 30");
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ScalarString, "John");
		assertOffsetAndToken(10, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(11, YamlTokenType.Key, "age");
		assertOffsetAndToken(14, YamlTokenType.Colon, ":");
		assertOffsetAndToken(15, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(16, YamlTokenType.ScalarNumber, "30");
		assertOffsetAndToken(18, YamlTokenType.EOS, "");
	}

	@Test
	public void testMissingValue() {
		scanner = YamlScanner.createScanner("name:     \n" + //
				"age: 30");
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, "     ");
		// assertOffsetAndToken(6, YamlTokenType.ScalarString, "John");
		assertOffsetAndToken(10, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(11, YamlTokenType.Key, "age");
		assertOffsetAndToken(14, YamlTokenType.Colon, ":");
		assertOffsetAndToken(15, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(16, YamlTokenType.ScalarNumber, "30");
		assertOffsetAndToken(18, YamlTokenType.EOS, "");
	}

	// ========== Quoted String Tests ==========

	@Test
	public void testDoubleQuotedString() {
		scanner = YamlScanner.createScanner("name: \"John Doe\"");
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(7, YamlTokenType.String, "John Doe");
		assertOffsetAndToken(15, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(16, YamlTokenType.EOS, "");
	}

	@Test
	public void testSingleQuotedString() {
		scanner = YamlScanner.createScanner("name: 'Jane Doe'");
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.StartString, "'");
		assertOffsetAndToken(7, YamlTokenType.String, "Jane Doe");
		assertOffsetAndToken(15, YamlTokenType.EndString, "'");
		assertOffsetAndToken(16, YamlTokenType.EOS, "");
	}

	@Test
	public void testQuotedStringWithEscape() {
		scanner = YamlScanner.createScanner("text: \"Hello\\n" + //
				"World\"");
		assertOffsetAndToken(0, YamlTokenType.Key, "text");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(7, YamlTokenType.String, "Hello\\nWorld");
		assertOffsetAndToken(19, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(20, YamlTokenType.EOS, "");
	}

	@Test
	public void testUnclosedQuotedString() {
		scanner = YamlScanner.createScanner("name: \"unclosed");
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(7, YamlTokenType.String, "unclosed");
		assertOffsetAndToken(15, YamlTokenType.EOS, "");
	}

	// ========== List/Array Tests ==========

	@Test
	public void testSimpleList() {
		scanner = YamlScanner.createScanner("- item1\n" + //
				"- item2");
		assertOffsetAndToken(0, YamlTokenType.Dash, "-");
		assertOffsetAndToken(1, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(2, YamlTokenType.ScalarString, "item1");
		assertOffsetAndToken(7, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(8, YamlTokenType.Dash, "-");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(10, YamlTokenType.ScalarString, "item2");
		assertOffsetAndToken(15, YamlTokenType.EOS, "");
	}

	@Test
	public void testListWithNumbers() {
		scanner = YamlScanner.createScanner("- 1\n" + //
				"- 2\n" + //
				"- 3");
		assertOffsetAndToken(0, YamlTokenType.Dash, "-");
		assertOffsetAndToken(1, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(2, YamlTokenType.ScalarNumber, "1");
		assertOffsetAndToken(3, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(4, YamlTokenType.Dash, "-");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ScalarNumber, "2");
		assertOffsetAndToken(7, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(8, YamlTokenType.Dash, "-");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(10, YamlTokenType.ScalarNumber, "3");
		assertOffsetAndToken(11, YamlTokenType.EOS, "");
	}

	@Test
	public void testNestedList() {
		scanner = YamlScanner.createScanner("items:\n" + //
				"- first\n" + //
				"- second");
		assertOffsetAndToken(0, YamlTokenType.Key, "items");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(7, YamlTokenType.Dash, "-");
		assertOffsetAndToken(8, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(9, YamlTokenType.ScalarString, "first");
		assertOffsetAndToken(14, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(15, YamlTokenType.Dash, "-");
		assertOffsetAndToken(16, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(17, YamlTokenType.ScalarString, "second");
		assertOffsetAndToken(23, YamlTokenType.EOS, "");
	}

	// ========== Flow Collection Tests (JSON-style) ==========

	@Test
	public void testFlowSequence() {
		scanner = YamlScanner.createScanner("items: [1, 2, 3]");
		assertOffsetAndToken(0, YamlTokenType.Key, "items");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(8, YamlTokenType.ScalarNumber, "1");
		assertOffsetAndToken(9, YamlTokenType.Comma, ",");
		assertOffsetAndToken(10, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(11, YamlTokenType.ScalarNumber, "2");
		assertOffsetAndToken(12, YamlTokenType.Comma, ",");
		assertOffsetAndToken(13, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(14, YamlTokenType.ScalarNumber, "3");
		assertOffsetAndToken(15, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(16, YamlTokenType.EOS, "");
	}

	@Test
	public void testFlowSequenceWithStrings() {
		scanner = YamlScanner.createScanner("[\"a\", \"b\", \"c\"]");
		assertOffsetAndToken(0, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(1, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(2, YamlTokenType.String, "a");
		assertOffsetAndToken(3, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(4, YamlTokenType.Comma, ",");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(7, YamlTokenType.String, "b");
		assertOffsetAndToken(8, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(9, YamlTokenType.Comma, ",");
		assertOffsetAndToken(10, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(11, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(12, YamlTokenType.String, "c");
		assertOffsetAndToken(13, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(14, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(15, YamlTokenType.EOS, "");
	}

	@Test
	public void testFlowMapping() {
		scanner = YamlScanner.createScanner("{name: John, age: 30}");
		assertOffsetAndToken(0, YamlTokenType.ObjectOpen, "{");
		assertOffsetAndToken(1, YamlTokenType.Key, "name");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarString, "John");
		assertOffsetAndToken(11, YamlTokenType.Comma, ",");
		assertOffsetAndToken(12, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(13, YamlTokenType.Key, "age");
		assertOffsetAndToken(16, YamlTokenType.Colon, ":");
		assertOffsetAndToken(17, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(18, YamlTokenType.ScalarNumber, "30");
		assertOffsetAndToken(20, YamlTokenType.ObjectClose, "}");
		assertOffsetAndToken(21, YamlTokenType.EOS, "");
	}

	@Test
	public void testNestedFlowCollections() {
		scanner = YamlScanner.createScanner("data: [1, [2, 3]]");
		assertOffsetAndToken(0, YamlTokenType.Key, "data");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(7, YamlTokenType.ScalarNumber, "1");
		assertOffsetAndToken(8, YamlTokenType.Comma, ",");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(10, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(11, YamlTokenType.ScalarNumber, "2");
		assertOffsetAndToken(12, YamlTokenType.Comma, ",");
		assertOffsetAndToken(13, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(14, YamlTokenType.ScalarNumber, "3");
		assertOffsetAndToken(15, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(16, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(17, YamlTokenType.EOS, "");
	}

	@Test
	public void testFlowArrayWithFlowObject() {
		scanner = YamlScanner
				.createScanner("list: [{ title: \"Applied AI\", year: 2005, tagline: \"AI guide for Java\" }]");

		// list:
		assertOffsetAndToken(0, YamlTokenType.Key, "list");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");

		// [
		assertOffsetAndToken(6, YamlTokenType.ArrayOpen, "[");

		// {
		assertOffsetAndToken(7, YamlTokenType.ObjectOpen, "{");
		assertOffsetAndToken(8, YamlTokenType.Whitespace, " ");

		// title: "Applied AI"
		assertOffsetAndToken(9, YamlTokenType.Key, "title");
		assertOffsetAndToken(14, YamlTokenType.Colon, ":");
		assertOffsetAndToken(15, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(16, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(17, YamlTokenType.String, "Applied AI");
		assertOffsetAndToken(27, YamlTokenType.EndString, "\"");

		// ,
		assertOffsetAndToken(28, YamlTokenType.Comma, ",");
		assertOffsetAndToken(29, YamlTokenType.Whitespace, " ");

		// year: 2005
		assertOffsetAndToken(30, YamlTokenType.Key, "year");
		assertOffsetAndToken(34, YamlTokenType.Colon, ":");
		assertOffsetAndToken(35, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(36, YamlTokenType.ScalarNumber, "2005");

		// ,
		assertOffsetAndToken(40, YamlTokenType.Comma, ",");
		assertOffsetAndToken(41, YamlTokenType.Whitespace, " ");

		// tagline: "AI guide for Java"
		assertOffsetAndToken(42, YamlTokenType.Key, "tagline");
		assertOffsetAndToken(49, YamlTokenType.Colon, ":");
		assertOffsetAndToken(50, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(51, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(52, YamlTokenType.String, "AI guide for Java");
		assertOffsetAndToken(69, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(70, YamlTokenType.Whitespace, " ");

		// }
		assertOffsetAndToken(71, YamlTokenType.ObjectClose, "}");

		// ]
		assertOffsetAndToken(72, YamlTokenType.ArrayClose, "]");

		assertOffsetAndToken(73, YamlTokenType.EOS, "");
	}

	// ========== Comment Tests ==========

	@Test
	public void testSimpleComment() {
		scanner = YamlScanner.createScanner("# This is a comment");
		assertOffsetAndToken(0, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(1, YamlTokenType.Comment, " This is a comment");
		assertOffsetAndToken(19, YamlTokenType.EOS, "");
	}

	@Test
	public void testCommentAfterValue() {
		scanner = YamlScanner.createScanner("key: value # comment");
		assertOffsetAndToken(0, YamlTokenType.Key, "key");
		assertOffsetAndToken(3, YamlTokenType.Colon, ":");
		assertOffsetAndToken(4, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(5, YamlTokenType.ScalarString, "value ");
		assertOffsetAndToken(11, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(12, YamlTokenType.Comment, " comment");
		assertOffsetAndToken(20, YamlTokenType.EOS, "");
	}

	@Test
	public void testMultilineWithComments() {
		scanner = YamlScanner.createScanner("# Header comment\n" + //
				"key: value\n" + //
				"# Footer comment");
		assertOffsetAndToken(0, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(1, YamlTokenType.Comment, " Header comment");
		assertOffsetAndToken(16, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(17, YamlTokenType.Key, "key");
		assertOffsetAndToken(20, YamlTokenType.Colon, ":");
		assertOffsetAndToken(21, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(22, YamlTokenType.ScalarString, "value");
		assertOffsetAndToken(27, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(28, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(29, YamlTokenType.Comment, " Footer comment");
		assertOffsetAndToken(44, YamlTokenType.EOS, "");
	}

	// ========== Whitespace and Newline Tests ==========

	@Test
	public void testIndentation() {
		scanner = YamlScanner.createScanner("  key: value");
		assertOffsetAndToken(0, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(2, YamlTokenType.Key, "key");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarString, "value");
		assertOffsetAndToken(12, YamlTokenType.EOS, "");
	}

	@Test
	public void testCRLF() {
		scanner = YamlScanner.createScanner("key1: value1\r\n" + //
				"key2: value2");
		assertOffsetAndToken(0, YamlTokenType.Key, "key1");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ScalarString, "value1");
		assertOffsetAndToken(12, YamlTokenType.Newline, "\r\n");
		assertOffsetAndToken(14, YamlTokenType.Key, "key2");
		assertOffsetAndToken(18, YamlTokenType.Colon, ":");
		assertOffsetAndToken(19, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(20, YamlTokenType.ScalarString, "value2");
		assertOffsetAndToken(26, YamlTokenType.EOS, "");
	}

	// ========== Complex/Real-World Tests ==========

	@Test
	public void testComplexYamlDocument() {
		scanner = YamlScanner.createScanner("name: MyApp\n" + //
				"version: 1.0\n" + //
				"enabled: true\n" + //
				"ports:\n" + //
				"  - 8080\n" + //
				"  - 8443");

		// name: MyApp
		assertOffsetAndToken(0, YamlTokenType.Key, "name");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ScalarString, "MyApp");
		assertOffsetAndToken(11, YamlTokenType.Newline, "\n");

		// version: 1.0
		assertOffsetAndToken(12, YamlTokenType.Key, "version");
		assertOffsetAndToken(19, YamlTokenType.Colon, ":");
		assertOffsetAndToken(20, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(21, YamlTokenType.ScalarNumber, "1.0");
		assertOffsetAndToken(24, YamlTokenType.Newline, "\n");

		// enabled: true
		assertOffsetAndToken(25, YamlTokenType.Key, "enabled");
		assertOffsetAndToken(32, YamlTokenType.Colon, ":");
		assertOffsetAndToken(33, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(34, YamlTokenType.ScalarBoolean, "true");
		assertOffsetAndToken(38, YamlTokenType.Newline, "\n");

		// ports:
		assertOffsetAndToken(39, YamlTokenType.Key, "ports");
		assertOffsetAndToken(44, YamlTokenType.Colon, ":");
		assertOffsetAndToken(45, YamlTokenType.Newline, "\n");

		// - 8080
		assertOffsetAndToken(46, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(48, YamlTokenType.Dash, "-");
		assertOffsetAndToken(49, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(50, YamlTokenType.ScalarNumber, "8080");
		assertOffsetAndToken(54, YamlTokenType.Newline, "\n");

		// - 8443
		assertOffsetAndToken(55, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(57, YamlTokenType.Dash, "-");
		assertOffsetAndToken(58, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(59, YamlTokenType.ScalarNumber, "8443");
		assertOffsetAndToken(63, YamlTokenType.EOS, "");
	}

	@Test
	public void testKubernetesStyleYaml() {
		scanner = YamlScanner.createScanner("apiVersion: v1\n" + //
				"kind: Pod\n" + //
				"metadata:\n" + //
				"  name: nginx\n" + //
				"  labels:\n" + //
				"    app: nginx");

		assertOffsetAndToken(0, YamlTokenType.Key, "apiVersion");
		assertOffsetAndToken(10, YamlTokenType.Colon, ":");
		assertOffsetAndToken(11, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(12, YamlTokenType.ScalarString, "v1");
		assertOffsetAndToken(14, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(15, YamlTokenType.Key, "kind");
		assertOffsetAndToken(19, YamlTokenType.Colon, ":");
		assertOffsetAndToken(20, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(21, YamlTokenType.ScalarString, "Pod");
		assertOffsetAndToken(24, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(25, YamlTokenType.Key, "metadata");
		assertOffsetAndToken(33, YamlTokenType.Colon, ":");
		assertOffsetAndToken(34, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(35, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(37, YamlTokenType.Key, "name");
		assertOffsetAndToken(41, YamlTokenType.Colon, ":");
		assertOffsetAndToken(42, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(43, YamlTokenType.ScalarString, "nginx");
		assertOffsetAndToken(48, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(49, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(51, YamlTokenType.Key, "labels");
		assertOffsetAndToken(57, YamlTokenType.Colon, ":");
		assertOffsetAndToken(58, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(59, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(63, YamlTokenType.Key, "app");
		assertOffsetAndToken(66, YamlTokenType.Colon, ":");
		assertOffsetAndToken(67, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(68, YamlTokenType.ScalarString, "nginx");
		assertOffsetAndToken(73, YamlTokenType.EOS, "");
	}

	@Test
	public void testSequenceOfMappings() {
		scanner = YamlScanner.createScanner("items:\n" + //
				"- name: John\n" + //
				"  age: 30\n" + //
				"- name: Jane\n" + //
				"  age: 25");

		// items:
		assertOffsetAndToken(0, YamlTokenType.Key, "items");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Newline, "\n");

		// - name: John
		assertOffsetAndToken(7, YamlTokenType.Dash, "-");
		assertOffsetAndToken(8, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(9, YamlTokenType.Key, "name");
		assertOffsetAndToken(13, YamlTokenType.Colon, ":");
		assertOffsetAndToken(14, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(15, YamlTokenType.ScalarString, "John");
		assertOffsetAndToken(19, YamlTokenType.Newline, "\n");

		// age: 30
		assertOffsetAndToken(20, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(22, YamlTokenType.Key, "age");
		assertOffsetAndToken(25, YamlTokenType.Colon, ":");
		assertOffsetAndToken(26, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(27, YamlTokenType.ScalarNumber, "30");
		assertOffsetAndToken(29, YamlTokenType.Newline, "\n");

		// - name: Jane
		assertOffsetAndToken(30, YamlTokenType.Dash, "-");
		assertOffsetAndToken(31, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(32, YamlTokenType.Key, "name");
		assertOffsetAndToken(36, YamlTokenType.Colon, ":");
		assertOffsetAndToken(37, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(38, YamlTokenType.ScalarString, "Jane");
		assertOffsetAndToken(42, YamlTokenType.Newline, "\n");

		// age: 25
		assertOffsetAndToken(43, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(45, YamlTokenType.Key, "age");
		assertOffsetAndToken(48, YamlTokenType.Colon, ":");
		assertOffsetAndToken(49, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(50, YamlTokenType.ScalarNumber, "25");

		assertOffsetAndToken(52, YamlTokenType.EOS, "");
	}

	@Test
	public void testMixedFlowAndBlock() {
		scanner = YamlScanner.createScanner("data:\n" + //
				"  items: [1, 2, 3]\n" + //
				"  config: {debug: true}");

		// data:
		assertOffsetAndToken(0, YamlTokenType.Key, "data");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Newline, "\n");

		// First line indentation
		assertOffsetAndToken(6, YamlTokenType.Whitespace, "  ");

		// items:
		assertOffsetAndToken(8, YamlTokenType.Key, "items");
		assertOffsetAndToken(13, YamlTokenType.Colon, ":");
		assertOffsetAndToken(14, YamlTokenType.Whitespace, " ");

		// [1, 2, 3]
		assertOffsetAndToken(15, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(16, YamlTokenType.ScalarNumber, "1");
		assertOffsetAndToken(17, YamlTokenType.Comma, ",");
		assertOffsetAndToken(18, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(19, YamlTokenType.ScalarNumber, "2");
		assertOffsetAndToken(20, YamlTokenType.Comma, ",");
		assertOffsetAndToken(21, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(22, YamlTokenType.ScalarNumber, "3");
		assertOffsetAndToken(23, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(24, YamlTokenType.Newline, "\n");

		// Second line indentation
		assertOffsetAndToken(25, YamlTokenType.Whitespace, "  ");

		// config:
		assertOffsetAndToken(27, YamlTokenType.Key, "config");
		assertOffsetAndToken(33, YamlTokenType.Colon, ":");
		assertOffsetAndToken(34, YamlTokenType.Whitespace, " ");

		// {debug: true}
		assertOffsetAndToken(35, YamlTokenType.ObjectOpen, "{");
		assertOffsetAndToken(36, YamlTokenType.Key, "debug");
		assertOffsetAndToken(41, YamlTokenType.Colon, ":");
		assertOffsetAndToken(42, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(43, YamlTokenType.ScalarBoolean, "true");
		assertOffsetAndToken(47, YamlTokenType.ObjectClose, "}");

		assertOffsetAndToken(48, YamlTokenType.EOS, "");
	}

	@Test
	public void testFrontMatter() {
		scanner = YamlScanner.createScanner("title: Hello Roqers (1)\n" + //
				"description: It is time to start Roqing!\n" + //
				"layout: :theme/index (2)");

		// title: Hello Roqers (1)
		assertOffsetAndToken(0, YamlTokenType.Key, "title");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarString, "Hello Roqers (1)");
		assertOffsetAndToken(23, YamlTokenType.Newline, "\n");

		// description: It is time to start Roqing 🚀!
		assertOffsetAndToken(24, YamlTokenType.Key, "description");
		assertOffsetAndToken(35, YamlTokenType.Colon, ":");
		assertOffsetAndToken(36, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(37, YamlTokenType.ScalarString, "It is time to start Roqing!");
		assertOffsetAndToken(64, YamlTokenType.Newline, "\n");

		// layout: :theme/index (2)
		assertOffsetAndToken(65, YamlTokenType.Key, "layout");
		assertOffsetAndToken(71, YamlTokenType.Colon, ":");
		assertOffsetAndToken(72, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(73, YamlTokenType.ScalarString, ":theme/index (2)");
		assertOffsetAndToken(89, YamlTokenType.EOS, "");
	}

	@Test
	public void testBookList() {
		scanner = YamlScanner.createScanner("list:\n" + //
				"  - title: \"Book 1\"\n" + //
				"    year: 2025\n" + //
				"  - title: \"Book 2\"\n" + //
				"    year: 2021");

		// list:
		assertOffsetAndToken(0, YamlTokenType.Key, "list");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Newline, "\n");

		// First item - indentation
		assertOffsetAndToken(6, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(8, YamlTokenType.Dash, "-");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");

		// title: "Book 1"
		assertOffsetAndToken(10, YamlTokenType.Key, "title");
		assertOffsetAndToken(15, YamlTokenType.Colon, ":");
		assertOffsetAndToken(16, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(17, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(18, YamlTokenType.String, "Book 1");
		assertOffsetAndToken(24, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(25, YamlTokenType.Newline, "\n");

		// year: 2025
		assertOffsetAndToken(26, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(30, YamlTokenType.Key, "year");
		assertOffsetAndToken(34, YamlTokenType.Colon, ":");
		assertOffsetAndToken(35, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(36, YamlTokenType.ScalarNumber, "2025");
		assertOffsetAndToken(40, YamlTokenType.Newline, "\n");

		// Second item - indentation
		assertOffsetAndToken(41, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(43, YamlTokenType.Dash, "-");
		assertOffsetAndToken(44, YamlTokenType.Whitespace, " ");

		// title: "Book 2"
		assertOffsetAndToken(45, YamlTokenType.Key, "title");
		assertOffsetAndToken(50, YamlTokenType.Colon, ":");
		assertOffsetAndToken(51, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(52, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(53, YamlTokenType.String, "Book 2");
		assertOffsetAndToken(59, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(60, YamlTokenType.Newline, "\n");

		// year: 2021
		assertOffsetAndToken(61, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(65, YamlTokenType.Key, "year");
		assertOffsetAndToken(69, YamlTokenType.Colon, ":");
		assertOffsetAndToken(70, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(71, YamlTokenType.ScalarNumber, "2021");

		assertOffsetAndToken(75, YamlTokenType.EOS, "");
	}

	@Test
	public void testBookListWithTagline() {
		scanner = YamlScanner.createScanner("list:\n" + //
				"  - title: \"Applied AI\"\n" + //
				"    year: 2025\n" + //
				"    tagline: \"AI guide for Java\"\n" + //
				"  - title: \"Modernizing Java\"\n" + //
				"    year: 2021\n" + //
				"    tagline: \"Bring apps to future\"");

		// list:
		assertOffsetAndToken(0, YamlTokenType.Key, "list");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Newline, "\n");

		// First item
		assertOffsetAndToken(6, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(8, YamlTokenType.Dash, "-");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");

		// title: "Applied AI"
		assertOffsetAndToken(10, YamlTokenType.Key, "title");
		assertOffsetAndToken(15, YamlTokenType.Colon, ":");
		assertOffsetAndToken(16, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(17, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(18, YamlTokenType.String, "Applied AI");
		assertOffsetAndToken(28, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(29, YamlTokenType.Newline, "\n");

		// year: 2025
		assertOffsetAndToken(30, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(34, YamlTokenType.Key, "year");
		assertOffsetAndToken(38, YamlTokenType.Colon, ":");
		assertOffsetAndToken(39, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(40, YamlTokenType.ScalarNumber, "2025");
		assertOffsetAndToken(44, YamlTokenType.Newline, "\n");

		// tagline: "AI guide for Java"
		assertOffsetAndToken(45, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(49, YamlTokenType.Key, "tagline");
		assertOffsetAndToken(56, YamlTokenType.Colon, ":");
		assertOffsetAndToken(57, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(58, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(59, YamlTokenType.String, "AI guide for Java");
		assertOffsetAndToken(76, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(77, YamlTokenType.Newline, "\n");

		// Second item
		assertOffsetAndToken(78, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(80, YamlTokenType.Dash, "-");
		assertOffsetAndToken(81, YamlTokenType.Whitespace, " ");

		// title: "Modernizing Java"
		assertOffsetAndToken(82, YamlTokenType.Key, "title");
		assertOffsetAndToken(87, YamlTokenType.Colon, ":");
		assertOffsetAndToken(88, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(89, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(90, YamlTokenType.String, "Modernizing Java");
		assertOffsetAndToken(106, YamlTokenType.EndString, "\"");
		assertOffsetAndToken(107, YamlTokenType.Newline, "\n");

		// year: 2021
		assertOffsetAndToken(108, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(112, YamlTokenType.Key, "year");
		assertOffsetAndToken(116, YamlTokenType.Colon, ":");
		assertOffsetAndToken(117, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(118, YamlTokenType.ScalarNumber, "2021");
		assertOffsetAndToken(122, YamlTokenType.Newline, "\n");

		// tagline: "Bring apps to future"
		assertOffsetAndToken(123, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(127, YamlTokenType.Key, "tagline");
		assertOffsetAndToken(134, YamlTokenType.Colon, ":");
		assertOffsetAndToken(135, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(136, YamlTokenType.StartString, "\"");
		assertOffsetAndToken(137, YamlTokenType.String, "Bring apps to future");
		assertOffsetAndToken(157, YamlTokenType.EndString, "\"");

		assertOffsetAndToken(158, YamlTokenType.EOS, "");
	}

	@Test
	public void testNestedSandwiches() {
		scanner = YamlScanner.createScanner("sandwiches:\n" + //
				"  - name: Turkey and Brie\n" + //
				"    ingredients:\n" + //
				"     - turkey\n" + //
				"     - brie\n" + //
				"  - name: Ham and Cheese\n" + //
				"    ingredients:\n" + //
				"     - ham\n" + //
				"     - cheddar");

		// sandwiches:
		assertOffsetAndToken(0, YamlTokenType.Key, "sandwiches");
		assertOffsetAndToken(10, YamlTokenType.Colon, ":");
		assertOffsetAndToken(11, YamlTokenType.Newline, "\n");

		// First sandwich
		assertOffsetAndToken(12, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(14, YamlTokenType.Dash, "-");
		assertOffsetAndToken(15, YamlTokenType.Whitespace, " ");

		// name: Turkey and Brie
		assertOffsetAndToken(16, YamlTokenType.Key, "name");
		assertOffsetAndToken(20, YamlTokenType.Colon, ":");
		assertOffsetAndToken(21, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(22, YamlTokenType.ScalarString, "Turkey and Brie");
		assertOffsetAndToken(37, YamlTokenType.Newline, "\n");

		// ingredients:
		assertOffsetAndToken(38, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(42, YamlTokenType.Key, "ingredients");
		assertOffsetAndToken(53, YamlTokenType.Colon, ":");
		assertOffsetAndToken(54, YamlTokenType.Newline, "\n");

		// - turkey
		assertOffsetAndToken(55, YamlTokenType.Whitespace, "     ");
		assertOffsetAndToken(60, YamlTokenType.Dash, "-");
		assertOffsetAndToken(61, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(62, YamlTokenType.ScalarString, "turkey");
		assertOffsetAndToken(68, YamlTokenType.Newline, "\n");

		// - brie
		assertOffsetAndToken(69, YamlTokenType.Whitespace, "     ");
		assertOffsetAndToken(74, YamlTokenType.Dash, "-");
		assertOffsetAndToken(75, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(76, YamlTokenType.ScalarString, "brie");
		assertOffsetAndToken(80, YamlTokenType.Newline, "\n");

		// Second sandwich
		assertOffsetAndToken(81, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(83, YamlTokenType.Dash, "-");
		assertOffsetAndToken(84, YamlTokenType.Whitespace, " ");

		// name: Ham and Cheese
		assertOffsetAndToken(85, YamlTokenType.Key, "name");
		assertOffsetAndToken(89, YamlTokenType.Colon, ":");
		assertOffsetAndToken(90, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(91, YamlTokenType.ScalarString, "Ham and Cheese");
		assertOffsetAndToken(105, YamlTokenType.Newline, "\n");

		// ingredients:
		assertOffsetAndToken(106, YamlTokenType.Whitespace, "    ");
		assertOffsetAndToken(110, YamlTokenType.Key, "ingredients");
		assertOffsetAndToken(121, YamlTokenType.Colon, ":");
		assertOffsetAndToken(122, YamlTokenType.Newline, "\n");

		// - ham
		assertOffsetAndToken(123, YamlTokenType.Whitespace, "     ");
		assertOffsetAndToken(128, YamlTokenType.Dash, "-");
		assertOffsetAndToken(129, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(130, YamlTokenType.ScalarString, "ham");
		assertOffsetAndToken(133, YamlTokenType.Newline, "\n");

		// - cheddar
		assertOffsetAndToken(134, YamlTokenType.Whitespace, "     ");
		assertOffsetAndToken(139, YamlTokenType.Dash, "-");
		assertOffsetAndToken(140, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(141, YamlTokenType.ScalarString, "cheddar");

		assertOffsetAndToken(148, YamlTokenType.EOS, "");
	}

	// ========== Edge Cases and Fault Tolerance ==========

	@Test
	public void testEmptyDocument() {
		scanner = YamlScanner.createScanner("");
		assertOffsetAndToken(0, YamlTokenType.EOS, "");
	}

	@Test
	public void testOnlyWhitespace() {
		scanner = YamlScanner.createScanner("   \t  \n  ");
		assertOffsetAndToken(0, YamlTokenType.Whitespace, "   \t  ");
		assertOffsetAndToken(6, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(7, YamlTokenType.Whitespace, "  ");
		assertOffsetAndToken(9, YamlTokenType.EOS, "");
	}

	@Test
	public void testOnlyComments() {
		scanner = YamlScanner.createScanner("# Comment 1\n# Comment 2");
		assertOffsetAndToken(0, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(1, YamlTokenType.Comment, " Comment 1");
		assertOffsetAndToken(11, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(12, YamlTokenType.StartComment, "#");
		assertOffsetAndToken(13, YamlTokenType.Comment, " Comment 2");
		assertOffsetAndToken(23, YamlTokenType.EOS, "");
	}

	@Test
	public void testMissingColon() {
		scanner = YamlScanner.createScanner("key value");
		assertOffsetAndToken(0, YamlTokenType.ScalarString, "key value");
		assertOffsetAndToken(9, YamlTokenType.EOS, "");
	}

	@Test
	public void testSpecialNumberFormats() {
		scanner = YamlScanner.createScanner("hex: 0xFF\n" + //
				"octal: 0o77\n" + //
				"float: 3.14e-10");

		assertOffsetAndToken(0, YamlTokenType.Key, "hex");
		assertOffsetAndToken(3, YamlTokenType.Colon, ":");
		assertOffsetAndToken(4, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(5, YamlTokenType.ScalarNumber, "0xFF");
		assertOffsetAndToken(9, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(10, YamlTokenType.Key, "octal");
		assertOffsetAndToken(15, YamlTokenType.Colon, ":");
		assertOffsetAndToken(16, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(17, YamlTokenType.ScalarNumber, "0o77");
		assertOffsetAndToken(21, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(22, YamlTokenType.Key, "float");
		assertOffsetAndToken(27, YamlTokenType.Colon, ":");
		assertOffsetAndToken(28, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(29, YamlTokenType.ScalarNumber, "3.14e-10");
		assertOffsetAndToken(37, YamlTokenType.EOS, "");
	}

	@Test
	public void testBooleanVariants() {
		scanner = YamlScanner.createScanner("a: yes\n" + //
				"b: no\n" + //
				"c: on\n" + //
				"d: off");

		assertOffsetAndToken(0, YamlTokenType.Key, "a");
		assertOffsetAndToken(1, YamlTokenType.Colon, ":");
		assertOffsetAndToken(2, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(3, YamlTokenType.ScalarBoolean, "yes");
		assertOffsetAndToken(6, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(7, YamlTokenType.Key, "b");
		assertOffsetAndToken(8, YamlTokenType.Colon, ":");
		assertOffsetAndToken(9, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(10, YamlTokenType.ScalarBoolean, "no");
		assertOffsetAndToken(12, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(13, YamlTokenType.Key, "c");
		assertOffsetAndToken(14, YamlTokenType.Colon, ":");
		assertOffsetAndToken(15, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(16, YamlTokenType.ScalarBoolean, "on");
		assertOffsetAndToken(18, YamlTokenType.Newline, "\n");

		assertOffsetAndToken(19, YamlTokenType.Key, "d");
		assertOffsetAndToken(20, YamlTokenType.Colon, ":");
		assertOffsetAndToken(21, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(22, YamlTokenType.ScalarBoolean, "off");
		assertOffsetAndToken(25, YamlTokenType.EOS, "");
	}

	@Test
	public void testUnclosedFlowSequence() {
		scanner = YamlScanner.createScanner("[1, 2, 3");
		assertOffsetAndToken(0, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(1, YamlTokenType.ScalarNumber, "1");
		assertOffsetAndToken(2, YamlTokenType.Comma, ",");
		assertOffsetAndToken(3, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(4, YamlTokenType.ScalarNumber, "2");
		assertOffsetAndToken(5, YamlTokenType.Comma, ",");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarNumber, "3");
		assertOffsetAndToken(8, YamlTokenType.EOS, "");
	}

	@Test
	public void testUnclosedFlowMapping() {
		scanner = YamlScanner.createScanner("{name: John");
		assertOffsetAndToken(0, YamlTokenType.ObjectOpen, "{");
		assertOffsetAndToken(1, YamlTokenType.Key, "name");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ScalarString, "John");
		assertOffsetAndToken(11, YamlTokenType.EOS, "");
	}

	@Test
	public void testEmptyValue() {
		scanner = YamlScanner.createScanner("key:");
		assertOffsetAndToken(0, YamlTokenType.Key, "key");
		assertOffsetAndToken(3, YamlTokenType.Colon, ":");
		assertOffsetAndToken(4, YamlTokenType.EOS, "");
	}

	@Test
	public void testEmptyList() {
		scanner = YamlScanner.createScanner("items: []");
		assertOffsetAndToken(0, YamlTokenType.Key, "items");
		assertOffsetAndToken(5, YamlTokenType.Colon, ":");
		assertOffsetAndToken(6, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(7, YamlTokenType.ArrayOpen, "[");
		assertOffsetAndToken(8, YamlTokenType.ArrayClose, "]");
		assertOffsetAndToken(9, YamlTokenType.EOS, "");
	}

	@Test
	public void testEmptyObject() {
		scanner = YamlScanner.createScanner("data: {}");
		assertOffsetAndToken(0, YamlTokenType.Key, "data");
		assertOffsetAndToken(4, YamlTokenType.Colon, ":");
		assertOffsetAndToken(5, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(6, YamlTokenType.ObjectOpen, "{");
		assertOffsetAndToken(7, YamlTokenType.ObjectClose, "}");
		assertOffsetAndToken(8, YamlTokenType.EOS, "");
	}

	@Test
	public void testLayoutT() {
		scanner = YamlScanner.createScanner("layout: \nt");
		assertOffsetAndToken(0, YamlTokenType.Key, "layout");
		assertOffsetAndToken(6, YamlTokenType.Colon, ":");
		assertOffsetAndToken(7, YamlTokenType.Whitespace, " ");
		assertOffsetAndToken(8, YamlTokenType.Newline, "\n");
		assertOffsetAndToken(9, YamlTokenType.Key, "t");
		assertOffsetAndToken(10, YamlTokenType.EOS, "");
	}

	public void assertOffsetAndToken(int tokenOffset, YamlTokenType tokenType) {
		YamlTokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
	}

	public void assertOffsetAndToken(int tokenOffset, YamlTokenType tokenType, String tokenText) {
		YamlTokenType token = scanner.scan();
		assertEquals(tokenOffset, scanner.getTokenOffset());
		assertEquals(tokenType, token);
		assertEquals(tokenText, scanner.getTokenText());
	}
}
