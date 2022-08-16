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
 * Java type kind.
 *
 * @author Angelo ZERR
 */
public enum JavaTypeKind {

	Unknown(0), //
	Package(1), //
	Class(2), //
	Interface(3), //
	Enum(4);

	private final int value;

	JavaTypeKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JavaTypeKind forValue(int value) {
		JavaTypeKind[] allValues = JavaTypeKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
