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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.lsp4j.InitializeParams;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for settings.
 */
public class SettingsTest {

	private final String json = "{\r\n" + 
			"    \"settings\": {\r\n" + 
			"        \"quarkus\": {\r\n" + 
			"            \"tools\": {\r\n" + 
			"                \"trace\": {\r\n" + 
			"                    \"server\": \"verbose\"\r\n" + 
			"                },\r\n" + 
			"                \"starter\": {\r\n" + 
			"                    \"api\": \"http://code.quarkus.io/api\",\r\n" + 
			"                    \"defaults\": {}\r\n" + 
			"                },\r\n" + 
			"                \"symbols\": {\r\n" + 
			"                    \"showAsTree\": true\r\n" + 
			"                },\r\n" + 
			"                \"validation\": {\r\n" + 
			"                    \"enabled\": \"true\",\r\n" + 
			"                    \"unknown\": {\r\n" + 
			"                        \"severity\": \"error\",\r\n" + 
			"                        \"excluded\": [\r\n" + 
			"                            \"abcd\"\r\n" + 
			"                        ]\r\n" + 
			"                    }\r\n" + 
			"                }\r\n" + 
			"            }\r\n" + 
			"        }\r\n" + 
			"    }\r\n" + 
			"}";

	@Test
	public void initializationOptionsSettings() {

		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when QuarkusLanguageServer#initialize(InitializeParams params) is
		// called
		InitializeParams params = createInitializeParams(json);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllQuarkusSettings.getQuarkusToolsSettings(initializationOptionsSettings);
		QuarkusGeneralClientSettings settings = QuarkusGeneralClientSettings
				.getGeneralQuarkusSettings(initializationOptionsSettings);
		assertNotNull(settings);

		// Symbols
		assertNotNull(settings.getSymbols());
		assertEquals(true, settings.getSymbols().isShowAsTree());

		// Validation
		assertNotNull(settings.getValidation());
		assertEquals("error", settings.getValidation().getUnknown().getSeverity());
		assertEquals("error", settings.getValidation().getSyntax().getSeverity());
		assertEquals("warning", settings.getValidation().getDuplicate().getSeverity());
	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}

}
