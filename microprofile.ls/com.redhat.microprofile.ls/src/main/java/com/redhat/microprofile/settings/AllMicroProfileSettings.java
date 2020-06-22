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

import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.redhat.microprofile.utils.JSONUtility;

/**
 * Represents all settings under the 'microprofile' key
 * 
 * { 'microprofile': {...} }
 */
public class AllMicroProfileSettings {

	private static class ToolsSettings {

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object tools;

		public Object getTools() {
			return tools;
		}

	}

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object microprofile;

	/**
	 * @return the microprofile
	 */
	public Object getMicroProfile() {
		return microprofile;
	}

	/**
	 * @param microprofile the microprofile to set
	 */
	public void setQuarkus(Object microprofile) {
		this.microprofile = microprofile;
	}

	public static Object getMicroProfileToolsSettings(Object initializationOptionsSettings) {
		AllMicroProfileSettings rootSettings = JSONUtility.toModel(initializationOptionsSettings, AllMicroProfileSettings.class);
		if (rootSettings == null) {
			return null;
		}
		ToolsSettings microprofileSettings = JSONUtility.toModel(rootSettings.getMicroProfile(), ToolsSettings.class);
		return microprofileSettings != null ? microprofileSettings.getTools() : null;
	}
}