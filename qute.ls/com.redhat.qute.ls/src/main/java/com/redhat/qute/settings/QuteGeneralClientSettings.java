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

import java.util.Map;

import com.redhat.qute.utils.JSONUtility;

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
 * eg: {"symbols" : {...}, "validation" : {...}}
 *
 */
public class QuteGeneralClientSettings {

	private Map<String, QuteGeneralClientSettings> workspaceFolders;

	private QuteValidationSettings validation;

	private QuteCodeLensSettings codeLens;

	/**
	 * Returns the validation settings.
	 *
	 * @return the validation settings.
	 */
	public QuteValidationSettings getValidation() {
		return validation;
	}

	/**
	 * Set the validation settings.
	 *
	 * @param validation the validation settings.
	 */
	public void setValidation(QuteValidationSettings validation) {
		this.validation = validation;
	}

	/**
	 * Returns the Qute CodeLens settings.
	 *
	 * @return the Qute CodeLens settings.
	 */
	public QuteCodeLensSettings getCodeLens() {
		return codeLens;
	}

	/**
	 * Set the CodeLens setting.
	 *
	 * @param codeLens the CodeLens setting.
	 */
	public void setCodeLens(QuteCodeLensSettings codeLens) {
		this.codeLens = codeLens;
	}

	public Map<String, QuteGeneralClientSettings> getWorkspaceFolders() {
		return workspaceFolders;
	}

	public void setWorkspaceFolders(Map<String, QuteGeneralClientSettings> workspaceFolders) {
		this.workspaceFolders = workspaceFolders;
	}

	/**
	 * Returns the general settings from the given initialization options
	 *
	 * @param initializationOptionsSettings the initialization options
	 * @return the general settings from the given initialization options
	 */
	public static QuteGeneralClientSettings getGeneralQuteSettings(Object initializationOptionsSettings) {
		return JSONUtility.toModel(initializationOptionsSettings, QuteGeneralClientSettings.class);
	}

	/**
	 * State after an update of the shared settings.
	 *
	 */
	public static class SettingsUpdateState {

		private final boolean validationSettingsChanged;

		private final boolean codeLensSettingsChanged;

		public SettingsUpdateState(boolean validationChanged, boolean codeLensChanged) {
			this.validationSettingsChanged = validationChanged;
			this.codeLensSettingsChanged = codeLensChanged;
		}

		/**
		 * Returns true if validation settings changed and false otherwise.
		 *
		 * @return true if validation settings changed and false otherwise.
		 */
		public boolean isValidationSettingsChanged() {
			return validationSettingsChanged;
		}

		/**
		 * Returns true if CodeLens settings changed and false otherwise.
		 *
		 * @return true if CodeLens settings changed and false otherwise.
		 */
		public boolean isCodeLensSettingsChanged() {
			return codeLensSettingsChanged;
		}

	}

	/**
	 * Update the given shared settings with the given client settings.
	 *
	 * @param sharedSettings the shared settings to update.
	 * @param clientSettings the client settings used to update the shared settings.
	 *
	 * @return the state of the update of the shared settings.
	 */
	public static SettingsUpdateState update(SharedSettings sharedSettings, QuteGeneralClientSettings clientSettings) {
		Map<String, QuteGeneralClientSettings> workspaceFolders = clientSettings.getWorkspaceFolders();
		boolean workspaceChanged = sharedSettings
				.cleanWorkspaceFolderSettings(workspaceFolders != null ? workspaceFolders.keySet() : null);

		// Update validation settings
		boolean validationSettingsChanged = updateValidationSettings(sharedSettings, clientSettings);
		if (workspaceChanged) {
			validationSettingsChanged = true;
		}

		// Update CodeLens settings
		boolean codeLensSettingsChanged = updateCodeLensSettings(sharedSettings, clientSettings);
		if (workspaceChanged) {
			codeLensSettingsChanged = true;
		}

		return new SettingsUpdateState(validationSettingsChanged, codeLensSettingsChanged);
	}

	private static boolean updateValidationSettings(SharedSettings sharedSettings,
			QuteGeneralClientSettings clientSettings) {
		// Global validation settings
		boolean validationSettingsChanged = updateValidationSettings(sharedSettings, clientSettings.getValidation());
		// Workspace folder validation settings
		Map<String, QuteGeneralClientSettings> workspaceFolders = clientSettings.getWorkspaceFolders();
		if (workspaceFolders != null) {
			for (Map.Entry<String /* workspace folder Uri */, QuteGeneralClientSettings> entry : workspaceFolders
					.entrySet()) {
				String workspaceFolderUri = entry.getKey();
				BaseSettings settings = sharedSettings.getWorkspaceFolderSettings(workspaceFolderUri);
				validationSettingsChanged |= updateValidationSettings(settings, entry.getValue().getValidation());
			}
		}
		return validationSettingsChanged;
	}

	private static boolean updateValidationSettings(BaseSettings sharedSettings, QuteValidationSettings validation) {
		if (validation != null && !validation.equals(sharedSettings.getValidationSettings())) {
			sharedSettings.getValidationSettings().update(validation);
			return true;
		}
		return false;
	}

	private static boolean updateCodeLensSettings(SharedSettings sharedSettings,
			QuteGeneralClientSettings clientSettings) {
		// Global CodeLens settings
		boolean codeLensSettingsChanged = updateCodeLensSettings(sharedSettings, clientSettings.getCodeLens());
		// Workspace folder CodeLens settings
		Map<String, QuteGeneralClientSettings> workspaceFolders = clientSettings.getWorkspaceFolders();
		if (workspaceFolders != null) {
			for (Map.Entry<String /* workspace folder Uri */, QuteGeneralClientSettings> entry : workspaceFolders
					.entrySet()) {
				String workspaceFolderUri = entry.getKey();
				BaseSettings settings = sharedSettings.getWorkspaceFolderSettings(workspaceFolderUri);
				codeLensSettingsChanged |= updateCodeLensSettings(settings, entry.getValue().getCodeLens());
			}
		}
		return codeLensSettingsChanged;
	}

	private static boolean updateCodeLensSettings(BaseSettings sharedSettings, QuteCodeLensSettings codeLens) {
		if (codeLens != null && !codeLens.equals((sharedSettings.getCodeLensSettings()))) {
			sharedSettings.getCodeLensSettings().update(codeLens);
			return true;
		}
		return false;
	}
}