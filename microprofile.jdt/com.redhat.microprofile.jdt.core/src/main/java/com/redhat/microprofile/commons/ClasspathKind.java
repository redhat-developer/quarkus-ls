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

/**
 * Classpath kind where application.properties is stored:
 * 
 * <ul>
 * <li>not in classpath</li>
 * <li>in /java/main/src classpath</li>
 * <li>in /java/main/test classpath</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public enum ClasspathKind {

	NONE(1), SRC(2), TEST(3);

	private final int value;

	ClasspathKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ClasspathKind forValue(int value) {
		ClasspathKind[] allValues = ClasspathKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
