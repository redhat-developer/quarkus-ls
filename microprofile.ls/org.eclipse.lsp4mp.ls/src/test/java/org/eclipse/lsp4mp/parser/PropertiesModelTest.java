/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.parser;

import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;
import org.eclipse.lsp4mp.model.PropertyValue;
import org.eclipse.lsp4mp.model.PropertyValueLiteral;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link PropertiesModel} parser.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesModelTest {

	@Test
	public void parseCommentsAndTwoProperties() {
		String text = " # comment \na = b\n c=d";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 3);

		Node comments = model.getChildren().get(0);
		assertComments(comments, 1, 11, "# comment ");

		Node firstPropertyNode = model.getChildren().get(1);
		assertProperty(firstPropertyNode, 12, 17, 12, 13, "a", 14, 16, 17, "b");

		Node secondPropertyNode = model.getChildren().get(2);
		assertProperty(secondPropertyNode, 19, 22, 19, 20, "c", 20, 21, 22, "d");

	}

	@Test
	public void parsePropertyWithoutAssign() {
		String text = " a";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 1, 2, 1, 2, "a", -1, -1, -1, null);
	}

	@Test
	public void parsePropertyValueWithPoundSign() {
		String text = "a = value # value";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 0, 17, 0, 1, "a", 2, 4, 17, "value # value");
	}

	@Test
	public void parsePropertyWithoutValue() {
		String text = " a=";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 1, 3, 1, 2, "a", 2, -1, -1, null);
	}

	@Test
	public void parseMultiPropertyKey() {
		String text = "quarkus\\" + "\n" + ".application\\" + "\n" + ".name=name";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);
		Node property = model.getChildren().get(0);
		assertProperty(property, 0, 33, 0, 28, "quarkus.application.name", 28, 29, 33, "name");
	}

	@Test
	public void parseMultiPropertyValue() {
		String text = "mp.openapi.schema.java.util.Date = { \\" + "\n" + "  \"name\": \"EpochMillis\" \\" + "\n"
				+ "  \"type\": \"number\", \\" + "\n" + "  \"format\": \"int64\", \\" + "\n"
				+ "  \"description\": \"Milliseconds since January 1, 1970, 00:00:00 GMT\" \\" + "\n" + "}" + "\n"
				+ "quarkus.http.port=9090";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 2);

		Node firstProperty = model.getChildren().get(0);
		assertProperty(firstProperty, 0, 181, 0, 32, "mp.openapi.schema.java.util.Date", 33, 35, 181,
				"{ " + "\"name\": \"EpochMillis\" " + "\"type\": \"number\", " + "\"format\": \"int64\", "
						+ "\"description\": \"Milliseconds since January 1, 1970, 00:00:00 GMT\" " + "}");
		Node secondProperty = model.getChildren().get(1);
		assertProperty(secondProperty, 182, 204, 182, 199, "quarkus.http.port", 199, 200, 204, "9090");
	}

	@Test
	public void parseMultiPropertyValueLeadingSpaces() {
		String text = "a = hello \\" + "\n" + "how \\" + "\n" + "are \\" + "\n" + "you?";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Node property = model.getChildren().get(0);
		assertProperty(property, 0, 28, 0, 1, "a", 2, 4, 28, "hello how are you?");

		text = "a = hello \\" + "\n" + "      how \\" + "\n" + "   are \\" + "\n" + "      you?";
		model = PropertiesModel.parse(text, "application.properties");
		property = model.getChildren().get(0);
		assertProperty(property, 0, 43, 0, 1, "a", 2, 4, 43, "hello how are you?");
	}

	@Test
	public void parseMultiPropertyKeyAndValue() {
		String text = "mp\\" + "\n" + ".openapi\\" + "\n" + ".schema.java.util.Date = { \\" + "\n"
				+ "  \"name\": \"EpochMillis\" \\" + "\n" + "  \"type\": \"number\", \\" + "\n"
				+ "  \"format\": \"int64\", \\" + "\n"
				+ "  \"description\": \"Milliseconds since January 1, 1970, 00:00:00 GMT\" \\" + "\n" + "}" + "\n"
				+ "quarkus.http.port=9090";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 2);

		Node firstProperty = model.getChildren().get(0);
		assertProperty(firstProperty, 0, 185, 0, 36, "mp.openapi.schema.java.util.Date", 37, 39, 185,
				"{ " + "\"name\": \"EpochMillis\" " + "\"type\": \"number\", " + "\"format\": \"int64\", "
						+ "\"description\": \"Milliseconds since January 1, 1970, 00:00:00 GMT\" " + "}");
		Node secondProperty = model.getChildren().get(1);
		assertProperty(secondProperty, 186, 208, 186, 203, "quarkus.http.port", 203, 204, 208, "9090");
	}

	@Test
	public void parseSpaceAfterBackSlash() {
		// in the case where spaces follow the backslash,
		// the next line is treated as a new property key
		String text = "greeting.\\ \r\n" + "             message = hello\r\n" + "greeting.name = quarkus";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 3);

		Node property = model.getChildren().get(0);
		assertProperty(property, 0, 11, 0, 10, "greeting.\\", -1, -1, -1, null);

		property = model.getChildren().get(1);
		assertProperty(property, 26, 41, 26, 33, "message", 34, 36, 41, "hello");

		property = model.getChildren().get(2);
		assertProperty(property, 43, 66, 43, 56, "greeting.name", 57, 59, 66, "quarkus");
	}

	@Test
	public void parseSpaceAfterBackSlash2() {
		// in the case where spaces follow the backslash,
		// the next line is treated as a new property key
		String text = "quarkus\\  " + "\n" + ".application\\ " + "\n" + ".name=name";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 3);

		Node property = model.getChildren().get(0);
		assertProperty(property, 0, 10, 0, 8, "quarkus\\", -1, -1, -1, null);

		property = model.getChildren().get(1);
		assertProperty(property, 11, 25, 11, 24, ".application\\", -1, -1, -1, null);

		property = model.getChildren().get(2);
		assertProperty(property, 26, 36, 26, 31, ".name", 31, 32, 36, "name");
	}

	@Test
	public void parseEndWithBackSlash() {

		// end property key with backslash
		String text = "mp.opentracing.server.skip\\";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);
		Node property = model.getChildren().get(0);
		assertProperty(property, 0, 27, 0, 27, "mp.opentracing.server.skip\\", -1, -1, -1, null);

		// end property value with backslash
		text = "mp.opentracing.server.skip-pattern=\\";
		model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);
		property = model.getChildren().get(0);
		assertProperty(property, 0, 36, 0, 34, "mp.opentracing.server.skip-pattern", 34, 35, 36, "\\");
	}

	@Test
	public void parsePropertyExpression() {
		String text = //
				"mp.openstracking.server.skip = http://${ip.address}:${port}";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Property property = (Property) model.getChildren().get(0);
		assertPropertyValue(property, new MockNode(31, 38, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(38, 51, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(51, 52, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(52, 59, NodeType.PROPERTY_VALUE_EXPRESSION));
	}

	@Test
	public void parseMultiplePropertyExpression() {
		String text = //
				"mp.openstracking.server.skip = http://${ip.address}:${port}\n" + //
						"quarkus.made.up.property=${port}${port}${port}\n";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Property property0 = (Property) model.getChildren().get(0);
		Property property1 = (Property) model.getChildren().get(1);
		assertPropertyValue(property0, new MockNode(31, 38, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(38, 51, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(51, 52, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(52, 59, NodeType.PROPERTY_VALUE_EXPRESSION));
		assertPropertyValue(property1, new MockNode(85, 92, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(92, 99, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(99, 106, NodeType.PROPERTY_VALUE_EXPRESSION));
	}

	@Test
	public void parseContinuedLinePropertyExpression() {
		String text = //
				"mp.openstacktracking.server.skip = \\\n" + //
						"  http://\\\n" + //
						"  ${ip.address}:${port}\n\n\n";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Property property = (Property) model.getChildren().get(0);
		assertPropertyValue(property, new MockNode(35, 50, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(50, 63, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(63, 64, NodeType.PROPERTY_VALUE_LITERAL),
				new MockNode(64, 71, NodeType.PROPERTY_VALUE_EXPRESSION));
	}

	@Test
	public void crossLinePropertyExpression() {
		String text = //
				"mp.openstacktracking.server.skip = ${http.\\\n" + //
				"  port}";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Property property = (Property) model.getChildren().get(0);
		assertPropertyValue(property,
				new MockNode(35, 51, NodeType.PROPERTY_VALUE_EXPRESSION));
	}

	@Test
	public void parseTrailingPropertyLiteral() {
		String text = "mp.openstracking.server.skip = ${test}:9090\n" + //
				"quarkus.made.up.property=${test}:90\\\n" + //
				"00";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		Property property0 = (Property) model.getChildren().get(0);
		Property property1 = (Property) model.getChildren().get(1);
		assertPropertyValue(property0, new MockNode(31, 38, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(38, 43, NodeType.PROPERTY_VALUE_LITERAL));
		assertPropertyValue(property1, new MockNode(69, 76, NodeType.PROPERTY_VALUE_EXPRESSION),
				new MockNode(76, 83, NodeType.PROPERTY_VALUE_LITERAL));
	}

	private static void assertComments(Node comments, int expectedStart, int expectedEnd, String expectedText) {
		Assert.assertEquals(comments.getNodeType(), NodeType.COMMENTS);
		Assert.assertEquals(expectedText, comments.getText());
		Assert.assertEquals(expectedStart, comments.getStart());
		Assert.assertEquals(expectedEnd, comments.getEnd());
	}

	/**
	 * NOTE: <code>expectedDelimiterAssign</code> is the start offset of the
	 * delimiter. The assumption is that the delimiter is only one character long.
	 */
	private static void assertProperty(Node propertyNode, int expectedStart, int expectedEnd, int expectedStartKey,
			int expectedEndKey, String expectedTextKey, int expectedDelimiterAssign, int expectedStartValue,
			int expectedEndValue, String expectedTextValue) {
		Assert.assertEquals(propertyNode.getNodeType(), NodeType.PROPERTY);
		Property property = (Property) propertyNode;

		// assert Property offsets
		Assert.assertEquals(expectedStart, property.getStart());
		Assert.assertEquals(expectedEnd, property.getEnd());

		// assert PropertyKey offsets
		Node propertyKey = property.getKey();
		if (expectedStartKey == -1) {
			Assert.assertNull(propertyKey);
		} else {
			Assert.assertNotNull(propertyKey);
			Assert.assertEquals(expectedStartKey, propertyKey.getStart());
			Assert.assertEquals(expectedEndKey, propertyKey.getEnd());
			Assert.assertEquals(expectedTextKey, propertyKey.getText(true));
		}

		// assert delimiter offsets
		Node delemiterAssign = property.getDelimiterAssign();
		if (expectedDelimiterAssign == -1) {
			Assert.assertNull(delemiterAssign);
		} else {
			Assert.assertNotNull(delemiterAssign);
			Assert.assertEquals(expectedDelimiterAssign, delemiterAssign.getStart());
		}

		// assert PropertyValue offsets
		Node propertyValue = property.getValue();
		if (expectedStartValue == -1) {
			Assert.assertNull(propertyValue);
		} else {
			Assert.assertNotNull(propertyValue);
			Assert.assertEquals(expectedStartValue, propertyValue.getStart());
			Assert.assertEquals(expectedEndValue, propertyValue.getEnd());
			Assert.assertEquals(expectedTextValue, propertyValue.getText(true));
		}

	}

	private static void assertPropertyValue(Property property, MockNode... propertyValueParts) {
		Assert.assertArrayEquals(propertyValueParts, property.getValue().getChildren().toArray());
	}

	private static void assertModel(Node model, int expectedEnd, int expectedChildren) {
		NodeType expectedNodeType = NodeType.DOCUMENT;
		int expectedStart = 0;

		Assert.assertEquals(expectedNodeType, model.getNodeType());
		Assert.assertEquals(expectedStart, model.getStart());
		Assert.assertEquals(expectedEnd, model.getEnd());
		Assert.assertEquals(expectedChildren, model.getChildren().size());
	}

	private class MockNode {

		private int start, end;
		private NodeType type;

		public MockNode(int start, int end, NodeType type) {
			this.start = start;
			this.end = end;
			this.type = type;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Node))
				return false;
			Node otherNode = (Node) other;
			return this.type == otherNode.getNodeType() && this.start == otherNode.getStart()
					&& this.end == otherNode.getEnd();
		}
	}

}
