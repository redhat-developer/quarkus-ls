/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Quarkus properties scope. Quarkus properties can be changed when:
 * 
 * <ul>
 * <li>{@link #dependencies}: a dependency changed (ex : add, remove a new JAR
 * to a given project)</li>
 * <li>{@link #sources}: a Java source changed.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public enum MicroProfilePropertiesScope {

	sources(1), dependencies(2);

	private final int value;

	MicroProfilePropertiesScope(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static MicroProfilePropertiesScope forValue(int value) {
		MicroProfilePropertiesScope[] allValues = MicroProfilePropertiesScope.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

	public static final List<MicroProfilePropertiesScope> ONLY_SOURCES = Collections
			.singletonList(MicroProfilePropertiesScope.sources);

	public static final List<MicroProfilePropertiesScope> SOURCES_AND_DEPENDENCIES = Arrays
			.asList(MicroProfilePropertiesScope.sources, MicroProfilePropertiesScope.dependencies);
}
