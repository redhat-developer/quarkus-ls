/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

/**
 * MicroProfile assert for JUnit tests.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileAssert {

	/**
	 * Assert MicroProfile properties.
	 * 
	 * @param info     the MicroProfile project information
	 * @param expected the expected MicroProfile properties.
	 */
	public static void assertProperties(MicroProfileProjectInfo info, ItemMetadata... expected) {
		assertProperties(info, null, expected);
	}

	/**
	 * Assert MicroProfile properties.
	 * 
	 * @param info          the MicroProfile project information
	 * @param expectedCount MicroProfile properties expected count.
	 * @param expected      the expected MicroProfile properties.
	 */
	public static void assertProperties(MicroProfileProjectInfo info, Integer expectedCount, ItemMetadata... expected) {
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), info.getProperties().size());
		}
		for (ItemMetadata item : expected) {
			assertProperty(info, item);
		}
	}

	/**
	 * Assert MicroProfile metadata property
	 * 
	 * @param info     the MicroProfile project information
	 * @param expected the MicroProfile property.
	 */
	private static void assertProperty(MicroProfileProjectInfo info, ItemMetadata expected) {
		List<ItemMetadata> matches = info.getProperties().stream().filter(completion -> {
			return expected.getName().equals(completion.getName());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getName() + " should only exist once: Actual: "
						+ info.getProperties().stream().map(c -> c.getName()).collect(Collectors.joining(",")),
				1, matches.size());

		ItemMetadata actual = matches.get(0);
		Assert.assertEquals("Test 'extension name' for '" + expected.getName() + "'", expected.getExtensionName(),
				actual.getExtensionName());
		Assert.assertEquals("Test 'type' for '" + expected.getName() + "'", expected.getType(), actual.getType());
		Assert.assertEquals("Test 'description' for '" + expected.getName() + "'", expected.getDescription(),
				actual.getDescription());
		Assert.assertEquals("Test 'binary' for '" + expected.getName() + "'", expected.isBinary(), actual.isBinary());
		Assert.assertEquals("Test 'source type' for '" + expected.getName() + "'", expected.getSourceType(),
				actual.getSourceType());
		Assert.assertEquals("Test 'source field' for '" + expected.getName() + "'", expected.getSourceField(),
				actual.getSourceField());
		Assert.assertEquals("Test 'source method' for '" + expected.getName() + "'", expected.getSourceMethod(),
				actual.getSourceMethod());
		Assert.assertEquals("Test 'phase' for '" + expected.getName() + "'", expected.getPhase(), actual.getPhase());
		Assert.assertEquals("Test 'default value' for '" + expected.getName() + "'", expected.getDefaultValue(),
				actual.getDefaultValue());
	}

	/**
	 * Returns an instance of MicroProfile property.
	 * 
	 * @param extensionName Quarkus extension name
	 * @param name          the property name
	 * @param type          the property class type
	 * @param description   the Javadoc
	 * @param binary        true if it comes from a binary field/method and false
	 *                      otherwise.
	 * @param sourceType    the source type (class or interface)
	 * @param sourceField   the source field name and null otherwise
	 * @param sourceMethod  the source method signature and null otherwise
	 * @param phase         the ConfigPhase.
	 * @param defaultValue  the default value
	 * @return
	 */
	public static ItemMetadata p(String extensionName, String name, String type, String description, boolean binary,
			String sourceType, String sourceField, String sourceMethod, int phase, String defaultValue) {
		ItemMetadata item = new ItemMetadata();
		item.setExtensionName(extensionName);
		item.setName(name);
		item.setType(type);
		item.setDescription(description);
		item.setSource(!binary);
		item.setSourceType(sourceType);
		item.setSourceMethod(sourceMethod);
		item.setSourceField(sourceField);
		item.setPhase(phase);
		item.setDefaultValue(defaultValue);
		return item;
	}

	/**
	 * Assert duplicate properties from the given the MicroProfile project
	 * information
	 * 
	 * @param info the MicroProfile project information
	 */
	public static void assertPropertiesDuplicate(MicroProfileProjectInfo info) {
		Map<String, Long> propertiesCount = info.getProperties().stream()
				.collect(Collectors.groupingBy(ItemMetadata::getName, Collectors.counting()));
		List<Entry<String, Long>> result = propertiesCount.entrySet().stream().filter(entry -> entry.getValue() > 1)
				.collect(Collectors.toList());
		Assert.assertEquals(
				result.stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(",")),
				0, result.size());
	}
}
