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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Qute shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings extends BaseSettings {
	private final QuteCompletionSettings completionSettings;
	private final QuteCodeLensSettings codeLensSettings;
	private final QuteFormattingSettings formattingSettings;
	private final QuteHoverSettings hoverSettings;
	private final QuteCommandCapabilities commandCapabilities;

	private Map<String /* workspace folder Uri */, BaseSettings> workspaceFolders;

	public SharedSettings() {
		this.completionSettings = new QuteCompletionSettings();
		this.codeLensSettings = new QuteCodeLensSettings();
		this.formattingSettings = new QuteFormattingSettings();
		this.hoverSettings = new QuteHoverSettings();
		this.commandCapabilities = new QuteCommandCapabilities();
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public QuteCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the codeLens settings.
	 * 
	 * @return the codeLens settings.
	 */
	public QuteCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}

	/**
	 * Returns the formatting settings.
	 * 
	 * @return the formatting settings.
	 */
	public QuteFormattingSettings getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the validation settings for the given Qute template file Uri.
	 * 
	 * @param templateFileUri the Qute template file Uri.
	 * 
	 * @return the validation settings for the given Qute template file Uri.
	 */
	public QuteValidationSettings getValidationSettings(String templateFileUri) {
		return getSettings(templateFileUri).getValidationSettings();
	}

	/**
	 * Returns the hover settings.
	 * 
	 * @return the hover settings.
	 */
	public QuteHoverSettings getHoverSettings() {
		return hoverSettings;
	}

	/**
	 * Returns the command capabilities.
	 *
	 * @return the command capabilities.
	 */
	public QuteCommandCapabilities getCommandCapabilities() {
		return commandCapabilities;
	}

	/**
	 * Returns the settings for the given Qute template file Uri.
	 * 
	 * @param templateFileUri the Qute template file Uri.
	 * 
	 * @return the settings for the given Qute template file Uri.
	 */
	private BaseSettings getSettings(String templateFileUri) {
		if (workspaceFolders != null) {
			for (Map.Entry<String /* workspace folder Uri */, BaseSettings> entry : workspaceFolders.entrySet()) {
				String workspaceFolderUri = entry.getKey();
				if (templateFileUri.startsWith(workspaceFolderUri)) {
					return entry.getValue();
				}
			}
		}
		return this;
	}

	/**
	 * Returns the settings for the given workspace folder Uri.
	 * 
	 * @param workspaceFolderUri the workspace folder Uri.
	 * 
	 * @return the settings for the given workspace folder Uri.
	 */
	public BaseSettings getWorkspaceFolderSettings(String workspaceFolderUri) {
		if (workspaceFolders == null) {
			workspaceFolders = new HashMap<String, BaseSettings>();
		}
		if (!workspaceFolders.containsKey(workspaceFolderUri)) {
			workspaceFolders.put(workspaceFolderUri, new BaseSettings());
		}
		return workspaceFolders.get(workspaceFolderUri);
	}

	/**
	 * Clean unused workspace folder settings.
	 * 
	 * @param existingWorkspaceFolderUris the existing workspace folder uris.
	 * 
	 * @return true if a workspace folder settings is clear and false otherwise.
	 */
	public boolean cleanWorkspaceFolderSettings(Set<String> existingWorkspaceFolderUris) {
		if (workspaceFolders == null) {
			return false;
		}
		if (existingWorkspaceFolderUris == null) {
			workspaceFolders.clear();
			return true;
		} else {
			boolean changed = false;
			Set<String> uris = new HashSet<String>(getWorkspaceFolderSettingsUris());
			for (String uri : uris) {
				if (!existingWorkspaceFolderUris.contains(uri)) {
					workspaceFolders.remove(uri);
					changed = true;
				}
			}
			return changed;
		}
	}

	/**
	 * Returns the workspace folder settings Uris.
	 * 
	 * @return the workspace folder settings Uris.
	 */
	public Set<String> getWorkspaceFolderSettingsUris() {
		if (workspaceFolders == null) {
			return Collections.emptySet();
		}
		return workspaceFolders.keySet();
	}
}
