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

/**
 * Java method parameter information.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaMethodParameterInfo {

	private final String name;

	private final String type;

	public JavaMethodParameterInfo(String name, String type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Returns the parameter name.
	 * 
	 * @return the parameter name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameter Java type.
	 * 
	 * @return the parameter Java type.
	 */
	public String getType() {
		return type;
	}

}
