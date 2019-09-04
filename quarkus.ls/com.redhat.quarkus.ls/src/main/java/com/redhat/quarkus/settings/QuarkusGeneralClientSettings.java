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
 * eg: {"symbols" : {...}}
 * 
 */
public class QuarkusGeneralClientSettings {

	private QuarkusSymbolSettings symbols;

	public QuarkusSymbolSettings getSymbols() {
		return symbols;
	}

	public void setSymbols(QuarkusSymbolSettings symbols) {
		this.symbols = symbols;
	}

	public static QuarkusGeneralClientSettings getGeneralQuarkusSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, QuarkusGeneralClientSettings.class);
	}
}