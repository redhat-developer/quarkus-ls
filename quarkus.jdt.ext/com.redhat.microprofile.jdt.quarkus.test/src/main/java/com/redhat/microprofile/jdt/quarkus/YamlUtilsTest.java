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
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import com.redhat.microprofile.jdt.internal.quarkus.utils.YamlUtils;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class YamlUtilsTest {

	@Test
	public void testGetValueNull() {
		assertNull(YamlUtils.getValueRecursively(Arrays.asList("a", "b", "c"), null));
	}

	@Test
	public void testGetValueOneLevel() {
		Object map = makeYamlMap( //
				"a: aaa\n");
		String value = YamlUtils.getValueRecursively(Arrays.asList("a"), map);
		assertEquals("aaa", value);
	}

	@Test
	public void testGetValueMultiLevel() {
		Object map = makeYamlMap( //
				"a:\n" + //
				"  b:\n" + //
				"    c: hello\n");
		String value = YamlUtils.getValueRecursively(Arrays.asList("a", "b", "c"), map);
		assertEquals("hello", value);
	}

	@Test
	public void testGetValueMultiLevelFailure() {
		Object map = makeYamlMap( //
				"a:\n" + //
				"  b:\n" + //
				"    d: hello\n");
		String value = YamlUtils.getValueRecursively(Arrays.asList("a", "b", "c"), map);
		assertNull(value);
	}

	@Test
	public void testGetValueMultiEntries() {
		Object map = makeYamlMap( //
				"a:\n" + //
				"  b:\n" + //
				"    c: hello\n" + //
				"  d:\n" + //
				"    e: hi\n" + //
				"    f: salu\n");
		String value = YamlUtils.getValueRecursively(Arrays.asList("a", "d", "f"), map);
		assertEquals("salu", value);
		value = YamlUtils.getValueRecursively(Arrays.asList("a", "d", "e"), map);
		assertEquals("hi", value);
	}

	@Test
	public void testGetValueHandlesNonStrings() {
		Object map = makeYamlMap( //
				"a:\n" + //
				"  b:\n" + //
				"    c: 50\n");
		String value = YamlUtils.getValueRecursively(Arrays.asList("a", "b", "c"), map);
		assertEquals("50", value);
	}

	private Object makeYamlMap(String yaml) {
		return new Yaml().load(yaml);
	}

}
