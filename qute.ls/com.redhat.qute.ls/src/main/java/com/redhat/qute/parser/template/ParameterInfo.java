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
package com.redhat.qute.parser.template;

public class ParameterInfo {

	public static final String EMPTY = "$empty$";

	private final String name;

	private final String defaultValue;

	private final boolean optional;;

	public ParameterInfo(String name, String defaultValue, boolean optional) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.optional = optional;
	}

	public String getName() {
		return name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isOptional() {
		return optional;
	}

	public boolean hasDefaultValue() {
		return defaultValue != null;
	}
}
