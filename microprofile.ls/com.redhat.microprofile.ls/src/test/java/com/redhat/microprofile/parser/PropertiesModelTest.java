/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.parser;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.model.Node;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.model.Property;
import com.redhat.microprofile.model.Node.NodeType;

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
		assertProperty(firstPropertyNode, 12, 13, "a", 14, 16, 17, "b");

		Node secondPropertyNode = model.getChildren().get(2);
		assertProperty(secondPropertyNode, 19, 20, "c", 20, 21, 22, "d");

	}

	@Test
	public void parsePropertyWithoutAssign() {
		String text = " a";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 1, 2, "a", -1, -1, -1, null);

	}

	@Test
	public void parsePropertyValueWithPoundSign() {
		String text = "a = value # value";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 0, 1, "a", 2, 4, 17, "value # value");
	}

	@Test
	public void parsePropertyWithoutValue() {
		String text = " a=";
		PropertiesModel model = PropertiesModel.parse(text, "application.properties");
		assertModel(model, text.length(), 1);

		Node firstPropertyNode = model.getChildren().get(0);
		assertProperty(firstPropertyNode, 1, 2, "a", 2, -1, -1, null);

	}

	private static void assertComments(Node comments, int expectedStart, int expectedEnd, String expectedText) {
		Assert.assertEquals(comments.getNodeType(), NodeType.COMMENTS);
		Assert.assertEquals(expectedText, comments.getText());
		Assert.assertEquals(expectedStart, comments.getStart());
		Assert.assertEquals(expectedEnd, comments.getEnd());
	}

	private static void assertProperty(Node propertyNode, int expectedStartKey, int expectedEndKey,
			String expectedTextKey, int expectedDelemiterAssign, int expectedStartValue, int expectedEndValue,
			String expectedTextValue) {
		Assert.assertEquals(propertyNode.getNodeType(), NodeType.PROPERTY);
		Property property = (Property) propertyNode;

		Node propertyKey = property.getKey();
		if (expectedStartKey == -1) {
			Assert.assertNull(propertyKey);
		} else {
			Assert.assertNotNull(propertyKey);
			Assert.assertEquals(expectedStartKey, propertyKey.getStart());
			Assert.assertEquals(expectedEndKey, propertyKey.getEnd());
			Assert.assertEquals(expectedTextKey, propertyKey.getText());
		}

		Node delemiterAssign = property.getDelimiterAssign();
		if (expectedDelemiterAssign == -1) {
			Assert.assertNull(delemiterAssign);
		} else {
			Assert.assertNotNull(delemiterAssign);
			Assert.assertEquals(expectedDelemiterAssign, delemiterAssign.getStart());
		}

		Node propertyValue = property.getValue();
		if (expectedStartValue == -1) {
			Assert.assertNull(propertyValue);
		} else {
			Assert.assertNotNull(propertyValue);
			Assert.assertEquals(expectedStartValue, propertyValue.getStart());
			Assert.assertEquals(expectedEndValue, propertyValue.getEnd());
			Assert.assertEquals(expectedTextValue, propertyValue.getText());
		}

	}

	private static void assertModel(Node model, int expectedEnd, int expectedChildren) {
		Assert.assertEquals(model.getNodeType(), NodeType.DOCUMENT);
		Assert.assertEquals(model.getStart(), 0);
		Assert.assertEquals(model.getEnd(), expectedEnd);
		Assert.assertEquals(model.getChildren().size(), expectedChildren);
	}

}
