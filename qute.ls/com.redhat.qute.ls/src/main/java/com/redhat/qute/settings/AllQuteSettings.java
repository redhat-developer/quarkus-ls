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
 * Represents all settings under the 'qute' key
 * 
 * { {'qute'} : {...}} }
 */
public class AllQuteSettings {

	@JsonAdapter(JsonElementTypeAdapter.Factory.class)
	private Object qute;

	/**
	 * @return the qute
	 */
	public Object getQute() {
		return qute;
	}

	/**
	 * @param qute the qute to set
	 */
	public void setQute(Object qute) {
		this.qute = qute;
	}

	public static Object getQuteSettings(Object initializationOptionsSettings) {
		AllQuteSettings settings = JSONUtility.toModel(initializationOptionsSettings, AllQuteSettings.class);
		return settings != null ? settings.getQute() : null;
	}
}