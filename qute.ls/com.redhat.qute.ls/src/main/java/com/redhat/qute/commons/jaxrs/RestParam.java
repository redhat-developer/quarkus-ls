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
package com.redhat.qute.commons.jaxrs;

/**
 * Rest parameters informations.
 * 
 * @author Angelo ZERR
 *
 */
public class RestParam {

	private String name;

	private JaxRsParamKind parameterKind;

	private Boolean required;

	public RestParam(String name, JaxRsParamKind parameterKind, boolean required) {
		this.name = name;
		this.parameterKind = parameterKind;
		this.required = required ? true : null;
	}

	public String getName() {
		return name;
	}

	public JaxRsParamKind getParameterKind() {
		return parameterKind;
	}

	public boolean isRequired() {
		return required != null ? required.booleanValue() : false;
	}

}
