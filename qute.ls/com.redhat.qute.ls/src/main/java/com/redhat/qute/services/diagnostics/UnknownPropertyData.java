/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.diagnostics;

/**
 * Represents the data needed to create a "create missing member" CodeAction
 *
 * @author datho7561
 */
public class UnknownPropertyData {

	private String signature;
	private String property;

	public UnknownPropertyData(String signature, String property) {
		this.signature = signature;
		this.property = property;
	}

	public String getSignature() {
		return this.signature;
	}

	public String getProperty() {
		return this.property;
	}

	@Override
	public UnknownPropertyData clone() {
		return new UnknownPropertyData(signature, property);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof UnknownPropertyData)) {
			return false;
		}
		UnknownPropertyData otherCast = (UnknownPropertyData) other;
		return (this.property.equals(otherCast.property))
				&& (this.signature.equals(otherCast.signature));
	}

}
