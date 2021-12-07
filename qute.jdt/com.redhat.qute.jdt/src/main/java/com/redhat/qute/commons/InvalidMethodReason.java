/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons;

/**
 * Invalid method reason.
 *
 * @author Angelo ZERR
 * 
 * @see https://github.com/quarkusio/quarkus/blob/ce19ff75e9f732ff731bb30c2141b44b42c66050/independent-projects/qute/core/src/main/java/io/quarkus/qute/ReflectionValueResolver.java#L176
 */
public enum InvalidMethodReason {

	Unknown(0), // the method doesn't exist.
	VoidReturn(1), // the method exists, but it returns nothing (void) which is forbidden in Qute
					// template.
	FromObject(2), // the method exists, but it belongs to java.lang.Object which is forbidden in
					// Qute template.
	Static(3); // the method exists, but it's a static method which is forbidden in
	// Qute template.

	private final int value;

	InvalidMethodReason(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static InvalidMethodReason forValue(int value) {
		InvalidMethodReason[] allValues = InvalidMethodReason.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
