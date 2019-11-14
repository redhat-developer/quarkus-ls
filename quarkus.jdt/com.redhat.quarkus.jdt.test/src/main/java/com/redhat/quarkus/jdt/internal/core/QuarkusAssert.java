/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;

/**
 * Quarkus assert for JUnit tests.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusAssert {

	/**
	 * Assert Quarkus properties.
	 * 
	 * @param info     the Quarkus project information
	 * @param expected the expected Quarkus properties.
	 */
	public static void assertProperties(QuarkusProjectInfo info, ExtendedConfigDescriptionBuildItem... expected) {
		assertProperties(info, null, expected);
	}

	/**
	 * Assert Quarkus properties.
	 * 
	 * @param info          the Quarkus project information
	 * @param expectedCount Quarkus properties expected count.
	 * @param expected      the expected Quarkus properties.
	 */
	public static void assertProperties(QuarkusProjectInfo info, Integer expectedCount,
			ExtendedConfigDescriptionBuildItem... expected) {
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), info.getProperties().size());
		}
		for (ExtendedConfigDescriptionBuildItem item : expected) {
			assertProperty(info, item);
		}
	}

	/**
	 * Assert Quarkus property
	 * 
	 * @param info     the Quarkus project information
	 * @param expected the Quarkus property.
	 */
	private static void assertProperty(QuarkusProjectInfo info, ExtendedConfigDescriptionBuildItem expected) {
		List<ExtendedConfigDescriptionBuildItem> matches = info.getProperties().stream().filter(completion -> {
			return expected.getPropertyName().equals(completion.getPropertyName());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getPropertyName() + " should only exist once: Actual: "
						+ info.getProperties().stream().map(c -> c.getPropertyName()).collect(Collectors.joining(",")),
				1, matches.size());

		ExtendedConfigDescriptionBuildItem actual = matches.get(0);
		Assert.assertEquals("Test 'extension name' for '" + expected.getPropertyName() + "'",
				expected.getExtensionName(), actual.getExtensionName());
		Assert.assertEquals("Test 'type' for '" + expected.getPropertyName() + "'", expected.getType(),
				actual.getType());
		Assert.assertEquals("Test 'docs' for '" + expected.getPropertyName() + "'", expected.getDocs(),
				actual.getDocs());
		Assert.assertEquals("Test 'location' for '" + expected.getPropertyName() + "'",
				expected.getLocation().replace('\\', '/'), actual.getLocation().replace('\\', '/'));
		Assert.assertEquals("Test 'source' for '" + expected.getPropertyName() + "'", expected.getSource(),
				actual.getSource());
		Assert.assertEquals("Test 'phase' for '" + expected.getPropertyName() + "'", expected.getPhase(),
				actual.getPhase());
		Assert.assertEquals("Test 'default value' for '" + expected.getPropertyName() + "'", expected.getDefaultValue(),
				actual.getDefaultValue());
	}

	/**
	 * Returns an instance of Quarkus property.
	 * 
	 * @param extensionName Quarkus extension name
	 * @param propertyName  the property name
	 * @param type          the property class type
	 * @param docs          the Javadoc
	 * @param location      the location (JAR, sources)
	 * @param source        the source (class + field)
	 * @param phase         the ConfigPhase.
	 * @param defaultValue  the default value
	 * @return
	 */
	public static ExtendedConfigDescriptionBuildItem p(String extensionName, String propertyName, String type,
			String docs, String location, String source, int phase, String defaultValue) {
		ExtendedConfigDescriptionBuildItem item = new ExtendedConfigDescriptionBuildItem();
		item.setExtensionName(extensionName);
		item.setPropertyName(propertyName);
		item.setType(type);
		item.setDocs(docs);
		item.setLocation(location);
		item.setSource(source);
		item.setPhase(phase);
		item.setDefaultValue(defaultValue);
		return item;
	}

	/**
	 * Assert duplicate properties from the given the Quarkus project information
	 * 
	 * @param info the Quarkus project information
	 */
	public static void assertPropertiesDuplicate(QuarkusProjectInfo info) {
		Map<String, Long> propertiesCount = info.getProperties().stream().collect(
				Collectors.groupingBy(ExtendedConfigDescriptionBuildItem::getPropertyName, Collectors.counting()));
		List<Entry<String, Long>> result = propertiesCount.entrySet().stream().filter(entry -> entry.getValue() > 1)
				.collect(Collectors.toList());
		Assert.assertEquals(
				result.stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(",")),
				0, result.size());
	}
}
