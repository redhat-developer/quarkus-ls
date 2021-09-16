/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.quarkus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Test;

import com.redhat.microprofile.jdt.internal.quarkus.utils.YamlUtils;

public class YamlUtilsTest {

	@Test
	public void oneLevel() {
		Properties properties = loadYamlAsProperties("a: 50");
		assertEquals(1, properties.size());
		assertTrue(properties.containsKey("a"));
		assertEquals("50", properties.getProperty("a"));

		properties = loadYamlAsProperties("a: 50\n" + //
				"b: '100'");
		assertEquals(2, properties.size());
		assertTrue(properties.containsKey("a"));
		assertEquals("50", properties.getProperty("a"));
		assertTrue(properties.containsKey("b"));
		assertEquals("100", properties.getProperty("b"));
	}

	@Test
	public void threeLevels() {
		Properties properties = loadYamlAsProperties("a:\n" + //
				" b:\n" + //
				"  c: 50\n");
		assertEquals(1, properties.size());
		assertTrue(properties.containsKey("a.b.c"));
		assertEquals("50", properties.getProperty("a.b.c"));
	}
	
	@Test
	public void quarkus() {
		Properties properties = loadYamlAsProperties("quarkus:\r\n" + //
				"  application:\r\n" + //
				"    name: name\r\n" + //
				"    version: 2.2\r\n" + //
				"  http:\r\n" + //
				"    port:\r\n" + //
				"      ~: 8083\r\n" + //
				"      unknown_property: 123");
		assertEquals(4, properties.size());
		assertTrue(properties.containsKey("quarkus.application.name"));
		assertEquals("name", properties.getProperty("quarkus.application.name"));
		assertTrue(properties.containsKey("quarkus.application.version"));
		assertEquals("2.2", properties.getProperty("quarkus.application.version"));
		assertTrue(properties.containsKey("quarkus.http.port"));
		assertEquals("8083", properties.getProperty("quarkus.http.port"));
		assertTrue(properties.containsKey("quarkus.http.port.unknown_property"));
		assertEquals("123", properties.getProperty("quarkus.http.port.unknown_property"));
	}
	
	
	
	private static Properties loadYamlAsProperties(String yamlContent) {
		InputStream input = new ByteArrayInputStream(yamlContent.getBytes());
		return YamlUtils.loadYamlAsProperties(input);
	}

}
