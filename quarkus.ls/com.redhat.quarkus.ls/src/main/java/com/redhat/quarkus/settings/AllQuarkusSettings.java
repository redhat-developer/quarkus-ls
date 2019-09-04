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

import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.redhat.quarkus.utils.JSONUtility;

/**
 * Represents all settings under the 'quarkus' key
 * 
 * { 'quarkus': {...} }
 */
public class AllQuarkusSettings {

	private static class ToolsSettings {

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object tools;

		public Object getTools() {
			return tools;
		}

	}

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object quarkus;

	/**
	 * @return the quarkus
	 */
	public Object getQuarkus() {
		return quarkus;
	}

	/**
	 * @param quarkus the quarkus to set
	 */
	public void setQuarkus(Object quarkus) {
		this.quarkus = quarkus;
	}

	public static Object getQuarkusToolsSettings(Object initializationOptionsSettings) {
		AllQuarkusSettings rootSettings = JSONUtility.toModel(initializationOptionsSettings, AllQuarkusSettings.class);
		if (rootSettings == null) {
			return null;
		}
		ToolsSettings quarkusSettings = JSONUtility.toModel(rootSettings.getQuarkus(), ToolsSettings.class);
		return quarkusSettings != null ? quarkusSettings.getTools() : null;
	}
}