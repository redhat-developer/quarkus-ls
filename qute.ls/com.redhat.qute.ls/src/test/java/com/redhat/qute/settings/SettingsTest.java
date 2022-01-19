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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.lsp4j.InitializeParams;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.redhat.qute.settings.QuteGeneralClientSettings.SettingsUpdateState;

/**
 * Tests for settings.
 */
public class SettingsTest {

	private static final String globalSettings = "{\r\n" + //
			"    \"settings\": {\r\n" + //
			"        \"qute\": {\r\n" + //
			"            \"validation\": {\r\n" + //
			"                \"enabled\": \"true\"\r\n" + //
			"            }\r\n" + //
			"        }\r\n" + //
			"    }\r\n" + //
			"}";

	private static final String workspaceFoldersSettings = "{\r\n" + //
			"	\"settings\": {\r\n" + //
			"		\"qute\": {\r\n" + //
			"			\"workspaceFolders\": {\r\n" + //
			"				\"file:///c%3A/qute-1\": {\r\n" + //
			"					\"validation\": {\r\n" + //
			"						\"enabled\": false\r\n" + //
			"					}\r\n" + //
			"				},\r\n" + //
			"				\"file:///c%3A/qute-2\": {\r\n" + //
			"					\"validation\": {\r\n" + //
			"						\"enabled\": true\r\n" + //
			"					}\r\n" + //
			"				}\r\n" + //
			"			}\r\n" + //
			"		}\r\n" + //
			"	}\r\n" + //
			"}";

	private static final String oneWorkspaceFoldersSettings = "{\r\n" + //
			"	\"settings\": {\r\n" + //
			"		\"qute\": {\r\n" + //
			"			\"workspaceFolders\": {\r\n" + //
			"				\"file:///c%3A/qute-1\": {\r\n" + //
			"					\"validation\": {\r\n" + //
			"						\"enabled\": false\r\n" + //
			"					}\r\n" + //
			"				}\r\n" + //
			"			}\r\n" + //
			"		}\r\n" + //
			"	}\r\n" + //
			"}";

	@Test
	public void globalClientSettings() {

		// Emulate InitializeParams#getInitializationOptions() object created as
		// JSONObject when QuteLanguageServer#initialize(InitializeParams
		// params) is
		// called
		QuteGeneralClientSettings settings = createGlobalSettings();
		assertNotNull(settings);

		// No workspace folders settings
		assertNull(settings.getWorkspaceFolders());

		// Validation
		QuteValidationSettings validation = settings.getValidation();
		assertNotNull(validation);
		assertTrue(validation.isEnabled(), "Validation enabled");
	}

	private static QuteGeneralClientSettings createGlobalSettings() {
		InitializeParams params = createInitializeParams(globalSettings);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllQuteSettings.getQuteSettings(initializationOptionsSettings);
		QuteGeneralClientSettings settings = QuteGeneralClientSettings
				.getGeneralQuteSettings(initializationOptionsSettings);
		return settings;
	}

	@Test
	public void workspaceFoldersClientSettings() {

		QuteGeneralClientSettings settings = createWorkspaceFoldersSettings();
		assertNotNull(settings);

		// No global settings
		assertNull(settings.getValidation());

		Map<String, QuteGeneralClientSettings> workspaceFolders = settings.getWorkspaceFolders();
		assertNotNull(workspaceFolders);

		assertEquals(2, workspaceFolders.keySet().size());

		// file:///c%3A/qute-1 workspace folder settings
		QuteGeneralClientSettings settings1 = workspaceFolders.get("file:///c%3A/qute-1");
		assertNotNull(settings1);
		QuteValidationSettings validation1 = settings1.getValidation();
		assertNotNull(validation1);
		assertFalse(validation1.isEnabled(), "Validation disabled");

		// file:///c%3A/qute-2 workspace folder settings
		QuteGeneralClientSettings settings2 = workspaceFolders.get("file:///c%3A/qute-2");
		assertNotNull(settings2);
		QuteValidationSettings validation2 = settings2.getValidation();
		assertNotNull(validation2);
		assertTrue(validation2.isEnabled(), "Validation enabled");

	}

	@Test
	public void workspaceFoldersSharedSettings() {

		// Client settings load 1
		QuteGeneralClientSettings clientSettings = createWorkspaceFoldersSettings();
		assertNotNull(clientSettings);

		SharedSettings sharedSettings = new SharedSettings();
		SettingsUpdateState result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertTrue(result.isValidationSettingsChanged());

		QuteValidationSettings defaultValidation = sharedSettings
				.getValidationSettings("file:///c%3A/XXXX/src/main/resources/items.qute.html");
		assertNotNull(defaultValidation);
		assertTrue(defaultValidation.isEnabled(), "Validation enabled");

		QuteValidationSettings validation1 = sharedSettings
				.getValidationSettings("file:///c%3A/qute-1/src/main/resources/items.qute.html");
		assertNotNull(validation1);
		assertFalse(validation1.isEnabled(), "Validation disabled");

		QuteValidationSettings validation2 = sharedSettings
				.getValidationSettings("file:///c%3A/qute-2/src/main/resources/items.qute.html");
		assertNotNull(validation2);
		assertTrue(validation2.isEnabled(), "Validation enabled");

		// Client settings load 2
		clientSettings = createWorkspaceFoldersSettings();
		result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertFalse(result.isValidationSettingsChanged());

		// Client settings load 3
		clientSettings = createWorkspaceFoldersSettings();
		QuteGeneralClientSettings settings = clientSettings.getWorkspaceFolders().get("file:///c%3A/qute-1");
		assertNotNull(settings);
		// Update the validation enabled for the first workspace folder settings.
		settings.getValidation().setEnabled(true);
		result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertTrue(result.isValidationSettingsChanged());

		validation1 = sharedSettings.getValidationSettings("file:///c%3A/qute-1/src/main/resources/items.qute.html");
		assertNotNull(validation1);
		assertTrue(validation1.isEnabled(), "Validation enabled");

	}

	@Test
	public void workspaceFoldersSharedSettingsChanged() {

		// 2 workspace settings
		QuteGeneralClientSettings clientSettings = createWorkspaceFoldersSettings();
		assertNotNull(clientSettings);

		SharedSettings sharedSettings = new SharedSettings();
		SettingsUpdateState result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertTrue(result.isValidationSettingsChanged());
		assertEquals(2, sharedSettings.getWorkspaceFolderSettingsUris().size());

		// 1 workspace settings
		clientSettings = createOneWorkspaceFoldersSettings();
		assertNotNull(clientSettings);

		result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertTrue(result.isValidationSettingsChanged());
		assertEquals(1, sharedSettings.getWorkspaceFolderSettingsUris().size());

		// 0 workspace settings
		clientSettings = createGlobalSettings();
		assertNotNull(clientSettings);

		result = QuteGeneralClientSettings.update(sharedSettings, clientSettings);
		assertTrue(result.isValidationSettingsChanged());
		assertEquals(0, sharedSettings.getWorkspaceFolderSettingsUris().size());
	}

	private static QuteGeneralClientSettings createWorkspaceFoldersSettings() {
		InitializeParams params = createInitializeParams(workspaceFoldersSettings);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllQuteSettings.getQuteSettings(initializationOptionsSettings);
		QuteGeneralClientSettings settings = QuteGeneralClientSettings
				.getGeneralQuteSettings(initializationOptionsSettings);
		return settings;
	}

	private static QuteGeneralClientSettings createOneWorkspaceFoldersSettings() {
		InitializeParams params = createInitializeParams(oneWorkspaceFoldersSettings);
		Object initializationOptionsSettings = InitializationOptionsSettings.getSettings(params);

		// Test client commons settings
		initializationOptionsSettings = AllQuteSettings.getQuteSettings(initializationOptionsSettings);
		QuteGeneralClientSettings settings = QuteGeneralClientSettings
				.getGeneralQuteSettings(initializationOptionsSettings);
		return settings;
	}

	private static InitializeParams createInitializeParams(String json) {
		InitializeParams initializeParams = new InitializeParams();
		Object initializationOptions = new Gson().fromJson(json, JsonObject.class);
		initializeParams.setInitializationOptions(initializationOptions);
		return initializeParams;
	}

}
