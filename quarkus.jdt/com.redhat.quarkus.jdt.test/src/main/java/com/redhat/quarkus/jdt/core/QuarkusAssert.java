/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.core;

import java.util.List;
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
	}

	/**
	 * Returns an instance of Quarkus property.
	 * 
	 * @param propertyName the property name
	 * @param type         the property class type
	 * @param docs         the Javadoc
	 * @param location     the location (JAR, sources)
	 * @param source       the source (class + field)
	 * @param phase        the ConfigPhase.
	 * @return
	 */
	public static ExtendedConfigDescriptionBuildItem p(String propertyName, String type, String docs, String location,
			String source, int phase) {
		ExtendedConfigDescriptionBuildItem item = new ExtendedConfigDescriptionBuildItem();
		item.setPropertyName(propertyName);
		item.setType(type);
		item.setDocs(docs);
		item.setLocation(location);
		item.setSource(source);
		item.setPhase(phase);
		return item;
	}

}
