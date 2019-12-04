/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.settings;

import com.redhat.microprofile.utils.JSONUtility;

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
public class MicroProfileGeneralClientSettings {

	private MicroProfileSymbolSettings symbols;

	private MicroProfileValidationSettings validation;

	private MicroProfileFormattingSettings formatting;

	private MicroProfileCodeLensSettings codeLens;

	/**
	 * Returns the symbols settings.
	 * 
	 * @return the symbols settings.
	 */
	public MicroProfileSymbolSettings getSymbols() {
		return symbols;
	}

	/**
	 * Set the symbols settings.
	 * 
	 * @param symbols the symbols settings.
	 */
	public void setSymbols(MicroProfileSymbolSettings symbols) {
		this.symbols = symbols;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public MicroProfileValidationSettings getValidation() {
		return validation;
	}

	/**
	 * Set the validation settings.
	 * 
	 * @param validation the validation settings.
	 */
	public void setValidation(MicroProfileValidationSettings validation) {
		this.validation = validation;
	}

	/**
	 * Returns the formatting settings
	 * 
	 * @return the formatting settings
	 */
	public MicroProfileFormattingSettings getFormatting() {
		return formatting;
	}

	/**
	 * Sets the formatting settings
	 * 
	 * @param formatting the formatting settings
	 */
	public void setFormatting(MicroProfileFormattingSettings formatting) {
		this.formatting = formatting;
	}

	/**
	 * Returns the code lens settings.
	 * 
	 * @return the code lens settings.
	 */
	public MicroProfileCodeLensSettings getCodeLens() {
		return codeLens;
	}

	/**
	 * Sets the code lens settings.
	 * 
	 * @param codeLens the code lens settings.
	 */
	public void setCodeLens(MicroProfileCodeLensSettings codeLens) {
		this.codeLens = codeLens;
	}

	/**
	 * Returns the general settings from the given initialization options
	 * 
	 * @param initializationOptionsSettings the initialization options
	 * @return the general settings from the given initialization options
	 */
	public static MicroProfileGeneralClientSettings getGeneralQuarkusSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, MicroProfileGeneralClientSettings.class);
	}
}