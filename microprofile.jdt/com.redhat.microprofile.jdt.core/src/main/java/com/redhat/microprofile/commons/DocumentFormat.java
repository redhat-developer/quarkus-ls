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
package com.redhat.microprofile.commons;

/**
 * Document format.
 *
 * @author Angelo ZERR
 */
public enum DocumentFormat {

	PlainText(1), Markdown(2);

	private final int value;

	DocumentFormat(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static DocumentFormat forValue(int value) {
		DocumentFormat[] allValues = DocumentFormat.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
