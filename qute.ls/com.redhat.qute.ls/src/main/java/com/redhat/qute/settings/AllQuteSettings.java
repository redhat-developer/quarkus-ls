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

import org.eclipse.lsp4j.jsonrpc.json.adapters.JsonElementTypeAdapter;

import com.google.gson.annotations.JsonAdapter;
import com.redhat.qute.utils.JSONUtility;

/**
 * Represents all settings under the 'quarkus' key
 * 
 * { 'quarkus': {'tools' : {'qute'} :  {...}} }
 */
public class AllQuteSettings {

	private static class ToolsSettings {

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object tools;

		public Object getTools() {
			return tools;
		}

	}

	private static class QuteSettings {

		@JsonAdapter(JsonElementTypeAdapter.Factory.class)
		private Object qute;

		public Object getQute() {
			return qute;
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

	public static Object getQuteSettings(Object initializationOptionsSettings) {
		Object toolsSettings = getQuarkusToolsSettings(initializationOptionsSettings);
		if (toolsSettings == null) {
			return null;
		}
		QuteSettings quteSettings = JSONUtility.toModel(toolsSettings, QuteSettings.class);
		return quteSettings != null ? quteSettings.getQute() : null;
	}
	
	private static Object getQuarkusToolsSettings(Object initializationOptionsSettings) {
		AllQuteSettings rootSettings = JSONUtility.toModel(initializationOptionsSettings, AllQuteSettings.class);
		if (rootSettings == null) {
			return null;
		}
		ToolsSettings quarkusSettings = JSONUtility.toModel(rootSettings.getQuarkus(), ToolsSettings.class);
		return quarkusSettings != null ? quarkusSettings.getTools() : null;
	}
}