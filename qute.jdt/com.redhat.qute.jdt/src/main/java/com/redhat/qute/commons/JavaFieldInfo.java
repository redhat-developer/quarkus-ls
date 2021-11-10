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

	private String type;

	/**
	 * Returns the Java type field.
	 * 
	 * @return the Java type field.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Set the Java type field.
	 * 
	 * @param type the Java type field.
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public JavaMemberKind getKind() {
		return JavaMemberKind.FIELD;
	}

	@Override
	public String getMemberType() {
		return getType();
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.getName());
		b.add("type", this.type);
		return b.toString();
	}
}
