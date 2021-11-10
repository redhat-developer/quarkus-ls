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

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.redhat.qute.utils.JSONUtility;

/**
 * Represents all settings sent from the server
 * 
 * { 'settings': { 'quarkus': { 'tools' : {'qute'} : 'validation':{...}} } }
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
