/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.core;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ConfigurationMetadata;
import com.redhat.microprofile.commons.metadata.ItemMetadata;

/**
 * Test for {@link PropertiesCollector}.
 * 
 * @author Angelo ZERR
 *
 */
public class PropertiesCollectorTest {

	@Test
	public void merge() {
		ConfigurationMetadata configuration = new ConfigurationMetadata();
		PropertiesCollector collector = new PropertiesCollector(configuration,
				MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);

		ConfigurationMetadata toMerge = createToMerge();
		collector.merge(toMerge);

		Assert.assertEquals(2, configuration.getProperties().size());
	}

	@Test
	public void mergeWithOnlySources() {
		ConfigurationMetadata configuration = new ConfigurationMetadata();
		PropertiesCollector collector = new PropertiesCollector(configuration,
				MicroProfilePropertiesScope.ONLY_SOURCES);

		ConfigurationMetadata toMerge = createToMerge();
		collector.merge(toMerge);

		Assert.assertEquals(1, configuration.getProperties().size());
	}

	private static ConfigurationMetadata createToMerge() {
		ConfigurationMetadata toMerge = new ConfigurationMetadata();
		toMerge.setProperties(new ArrayList<>());
		// Add a binary metadata
		ItemMetadata binary = new ItemMetadata();
		toMerge.getProperties().add(binary);

		// Add a source metadata
		ItemMetadata source = new ItemMetadata();
		source.setSource(true);
		toMerge.getProperties().add(source);
		return toMerge;
	}
}
