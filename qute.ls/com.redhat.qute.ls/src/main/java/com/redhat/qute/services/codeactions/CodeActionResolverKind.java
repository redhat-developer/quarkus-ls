/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

/**
 * Represents what resolver is needed to resolve a code action
 * 
 * @author datho7561
 */
public enum CodeActionResolverKind {

	GenerateMissingMember(1);

	private final int value;

	CodeActionResolverKind(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static CodeActionResolverKind forValue(int value) {
		CodeActionResolverKind[] allValues = CodeActionResolverKind.values();
		if (value < 1 || value > allValues.length)
			throw new IllegalArgumentException("Illegal enum value: " + value);
		return allValues[value - 1];
	}

}
