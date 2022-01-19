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
package com.redhat.qute.settings;

/**
 * Base class for settings.
 * 
 * @author Angelo ZERR
 *
 */
public class BaseSettings {

	private final QuteValidationSettings validationSettings;

	public BaseSettings() {
		this.validationSettings = new QuteValidationSettings();
	}

	/**
	 * Returns the Qute validation settings.
	 * 
	 * @return the Qute validation settings.
	 */
	public QuteValidationSettings getValidationSettings() {
		return validationSettings;
	}
}
