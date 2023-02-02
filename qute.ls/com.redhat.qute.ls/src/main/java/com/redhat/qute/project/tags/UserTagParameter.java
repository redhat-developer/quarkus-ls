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
package com.redhat.qute.project.tags;

import com.redhat.qute.parser.expression.ObjectPart;

/**
 * User tag parameter information.
 * 
 * @author Angelo ZERR
 *
 */
public class UserTagParameter {

	private final ObjectPart part;

	private boolean required;

	public UserTagParameter(ObjectPart part) {
		this.part = part;
	}

	/**
	 * Returns the object part where the user tag parameter is declared.
	 * 
	 * @return the object part where the user tag parameter is declared.
	 */
	public ObjectPart getPart() {
		return part;
	}

	/**
	 * Set the required flag.
	 * 
	 * @param required the required flag.
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

	/**
	 * Returns true if the user tag parameter is required and false otherwise.
	 * 
	 * @return true if the user tag parameter is required and false otherwise.
	 */
	public Boolean isRequired() {
		return required;
	}

	/**
	 * Returns the user tag parameter name.
	 * 
	 * @return the user tag parameter name.
	 */
	public String getPartName() {
		return part.getPartName();
	}
}
