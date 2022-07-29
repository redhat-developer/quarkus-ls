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
 * The Java base type (signature) data for a given part.
 *
 * @author datho7561
 */
public class JavaBaseTypeOfPartData {

	private String signature;

	public JavaBaseTypeOfPartData(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return this.signature;
	}

	@Override
	public JavaBaseTypeOfPartData clone() {
		return new JavaBaseTypeOfPartData(signature);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((signature == null) ? 0 : signature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaBaseTypeOfPartData other = (JavaBaseTypeOfPartData) obj;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.add("signature", signature);
		return builder.toString();
	}

}
