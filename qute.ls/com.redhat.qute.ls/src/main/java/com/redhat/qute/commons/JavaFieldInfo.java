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

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Java field information.
 *
 * @author Angelo ZERR
 *
 */
public class JavaFieldInfo extends JavaMemberInfo {

	private transient String name;

	private transient String type;

	/**
	 * Returns the Java field signature.
	 *
	 * Example:
	 *
	 * <code>
	 * price : java.math.BigInteger
	 * </code>
	 *
	 * @return the Java field signature.
	 */
	@Override
	public String getSignature() {
		return super.getSignature();
	}

	/**
	 * Returns the simple Java field signature.
	 *
	 * Example:
	 *
	 * <code>
	 * price : BigInteger
	 * </code>
	 *
	 * @return the simple Java field signature.
	 */
	public String getSimpleSignature() {
		return name + " : " + getJavaElementSimpleType();
	}

	/**
	 * Returns the field name.
	 *
	 * Example:
	 *
	 * <code>
	 *  price
	 *  </code>
	 *
	 * from the given signature:
	 *
	 * <code>
	 * price : java.math.BigInteger
	 * </code>
	 *
	 * @return the field name.
	 */
	@Override
	public String getName() {
		if (name != null) {
			return name;
		}
		// The field name is not computed, compute it from signature
		String signature = getSignature();
		int index = signature != null ? signature.indexOf(':') : -1;
		if (index != -1) {
			name = signature.substring(0, index).trim();
		}
		return name;
	}

	/**
	 * Returns the field Java type and null otherwise.
	 *
	 * Example:
	 *
	 * <code>
	 *  java.math.BigInteger
	 *  </code>
	 *
	 * from the given signature:
	 *
	 * <code>
	 * price : java.math.BigInteger
	 * </code>
	 *
	 * @return the field Java type and null otherwise.
	 */
	public String getType() {
		if (type == null) {
			// Compute field type from the signature
			String signature = getSignature();
			int index = signature.lastIndexOf(':');
			type = index != -1 ? signature.substring(index + 1, signature.length()).trim() : NO_VALUE;
		}
		return NO_VALUE.equals(type) ? null : type;
	}

	@Override
	public JavaElementKind getJavaElementKind() {
		return JavaElementKind.FIELD;
	}

	@Override
	public String getJavaElementType() {
		return getType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("type", this.getType());
		b.add("signature", this.getSignature());
		return b.toString();
	}
}
