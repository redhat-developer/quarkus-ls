/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Qute project scope. Qute data model can be changed when:
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
public enum QuteProjectScope {

	sources(1), dependencies(2);

	private final int value;

	QuteProjectScope(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static QuteProjectScope forValue(int value) {
		QuteProjectScope[] allValues = QuteProjectScope.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

	public static final List<QuteProjectScope> ONLY_SOURCES = Collections.singletonList(QuteProjectScope.sources);

	public static final List<QuteProjectScope> SOURCES_AND_DEPENDENCIES = Arrays.asList(QuteProjectScope.sources,
			QuteProjectScope.dependencies);

	/**
	 * Returns true if the given scopes is only sources and false otherwise.
	 *
	 * @param scopes
	 * @return true if the given scopes is only sources and false otherwise.
	 */
	public static boolean isOnlySources(List<QuteProjectScope> scopes) {
		return scopes != null && scopes.size() == 1 && scopes.get(0) == QuteProjectScope.sources;
	}

}