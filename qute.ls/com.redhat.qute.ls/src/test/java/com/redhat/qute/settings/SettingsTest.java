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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lsp4j.InitializeParams;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for settings.
 */
public class SettingsTest {

	private final String json = "{\r\n" + //
			"    \"settings\": {\r\n" + //
			"        \"qute\": {\r\n" + //
			"            \"validation\": {\r\n" + //
			"                \"enabled\": \"true\"\r\n" + //
			"            }\r\n" + //
			"        }\r\n" + //
			"    }\r\n" + //
			"}";

	@Test
	public void initializationOptionsSettings() {

		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when QuteLanguageServer#initialize(InitializeParams
		// params) is
		// called
		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllQuteSettings.getQuteSettings(initializationOptionsSettings);
		QuteGeneralClientSettings settings = QuteGeneralClientSettings
				.getGeneralQuteSettings(initializationOptionsSettings);
		assertNotNull(settings);

		// Validation
		assertNotNull(settings.getValidation());
		assertTrue(settings.getValidation().isEnabled(), "Validation enabled");
	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}

}
