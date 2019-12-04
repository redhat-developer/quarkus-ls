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

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.redhat.microprofile.utils.JSONUtility;

/**
 * Represents all settings sent from the server
 * 
 * { 'settings': { 'quarkus': {...}, 'http': {...} } }
 */
public class InitializationOptionsSettings {

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object settings;

	public Object getSettings() {
		return settings;
	}

	public void setSettings(Object settings) {
		this.settings = settings;
	}

	/**
	 * Returns the "settings" section of
	 * {@link InitializeParams#getInitializationOptions()}.
	 * 
	 * Here a sample of initializationOptions
	 * 
	 * <pre>
	 * "initializationOptions": {
			"settings": {
				"xml": {
					"catalogs": [
						"catalog.xml",
						"catalog2.xml"
					],
					"logs": {
						"client": true
					},
					"format": {
						"joinCommentLines": false,
						"formatComments": true
					},
					...
				}
			}
		}
	 * </pre>
	 * 
	 * @param initializeParams
	 * @return the "settings" section of
	 *         {@link InitializeParams#getInitializationOptions()}.
	 */
	public static Object getSettings(InitializeParams initializeParams) {
		InitializationOptionsSettings root = JSONUtility.toModel(initializeParams.getInitializationOptions(),
				InitializationOptionsSettings.class);
		return root != null ? root.getSettings() : null;
	}
}
