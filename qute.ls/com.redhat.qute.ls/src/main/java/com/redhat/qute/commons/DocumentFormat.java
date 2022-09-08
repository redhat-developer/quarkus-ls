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
