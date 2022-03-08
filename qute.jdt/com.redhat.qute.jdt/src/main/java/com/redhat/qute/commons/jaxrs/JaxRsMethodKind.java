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
 * JaxRs method kind.
 *
 * @author Angelo ZERR
 */
public enum JaxRsMethodKind {

	POST(1), //
	GET(2); // TODO : manage another kind like @DELETE, @PUT. it currently represents any jax ws-rs other than @POST

	private final int value;

	JaxRsMethodKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static JaxRsMethodKind forValue(int value) {
		JaxRsMethodKind[] allValues = JaxRsMethodKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
