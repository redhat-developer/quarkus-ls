/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.commons.jaxrs;

/**
 * JaxRs parameter kind.
 *
 * @author Angelo ZERR
 */
public enum JaxRsParamKind {

	NONE(0), //
	QUERY(1), //
	FORM(2), //s
	PATH(3);

	private final int value;

	JaxRsParamKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JaxRsParamKind forValue(int value) {
		JaxRsParamKind[] allValues = JaxRsParamKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
