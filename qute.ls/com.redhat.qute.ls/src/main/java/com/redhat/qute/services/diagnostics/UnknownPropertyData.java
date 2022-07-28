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

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Represents the data needed to create a "create missing member" CodeAction
 *
 * @author datho7561
 */
public class UnknownPropertyData {

	private String signature;
	private String property;
	private Boolean source;

	public UnknownPropertyData(String signature, String property, Boolean source) {
		this.signature = signature;
		this.property = property;
		this.source = source;
	}

	public String getSignature() {
		return this.signature;
	}

	public String getProperty() {
		return this.property;
	}

	public Boolean isSource() {
		return source != null && this.source.booleanValue();
	}

	@Override
	public UnknownPropertyData clone() {
		return new UnknownPropertyData(signature, property, source);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof UnknownPropertyData)) {
			return false;
		}
		UnknownPropertyData otherCast = (UnknownPropertyData) other;
		return ((this.property == null && otherCast.property == null) || (this.property.equals(otherCast.property)))
				&& ((this.signature == null && otherCast.signature == null)
						|| (this.signature.equals(otherCast.signature)))
				&& ((this.source == null && otherCast.source == null)
						|| (this.source.booleanValue() == otherCast.source.booleanValue()));
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.add("signature", signature);
		builder.add("property", property);
		builder.add("source", source);
		return builder.toString();
	}

}
