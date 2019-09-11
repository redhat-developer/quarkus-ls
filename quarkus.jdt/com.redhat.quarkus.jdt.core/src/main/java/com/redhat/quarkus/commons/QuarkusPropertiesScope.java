/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.commons;

/**
 * Quarkus properties scope. Quarkus properties can be changed when:
 * 
 * <ul>
 * <li>{@link #classpath}: a classpath changed (ex : add, remove a new JAR to a
 * given project)</li>
 * <li>{@link #sources}: a Java source changed.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public enum QuarkusPropertiesScope {

	sources(1), classpath(2);

	private final int value;

	QuarkusPropertiesScope(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static QuarkusPropertiesScope forValue(int value) {
		QuarkusPropertiesScope[] allValues = QuarkusPropertiesScope.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
