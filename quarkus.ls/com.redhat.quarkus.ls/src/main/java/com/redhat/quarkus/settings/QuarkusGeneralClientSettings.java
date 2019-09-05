/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.settings;

import com.redhat.quarkus.utils.JSONUtility;

/**
 * Class to hold all settings from the client side.
 * 
 * 
 * This class is created through the deserialization of a JSON object. Each
 * internal setting must be represented by a class and have:
 * 
 * 1) A constructor with no parameters
 * 
 * 2) The JSON key/parent for the settings must have the same name as a
 * variable.
 * 
 * eg: {"symbols" : {...}, "validation" : {...}}
 * 
 */
public class QuarkusGeneralClientSettings {

	private QuarkusSymbolSettings symbols;

	private QuarkusValidationSettings validation;

	/**
	 * Returns the symbols settings.
	 * 
	 * @return the symbols settings.
	 */
	public QuarkusSymbolSettings getSymbols() {
		return symbols;
	}

	/**
	 * Set the symbols settings.
	 * 
	 * @param symbols the symbols settings.
	 */
	public void setSymbols(QuarkusSymbolSettings symbols) {
		this.symbols = symbols;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public QuarkusValidationSettings getValidation() {
		return validation;
	}

	/**
	 * Set the validation settings.
	 * 
	 * @param validation the validation settings.
	 */
	public void setValidation(QuarkusValidationSettings validation) {
		this.validation = validation;
	}

	/**
	 * Returns the general settings from the given initialization options
	 * 
	 * @param initializationOptionsSettings the initialization options
	 * @return the general settings from the given initialization options
	 */
	public static QuarkusGeneralClientSettings getGeneralQuarkusSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, QuarkusGeneralClientSettings.class);
	}
}