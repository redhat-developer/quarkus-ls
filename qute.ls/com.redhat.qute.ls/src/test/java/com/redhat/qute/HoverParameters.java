/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute;

import java.util.Arrays;

import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.MarkupKind;

import com.redhat.qute.settings.SharedSettings;

public class HoverParameters extends BaseParameters {
	private SharedSettings sharedSettings;

	public HoverParameters() {
		super();
		SharedSettings sharedSettings = new SharedSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		sharedSettings.getHoverSettings().setCapabilities(capabilities);
		setSharedSettings(sharedSettings);
	}

	public HoverParameters(String fileUri, String templateId, String projectUri, String templateBaseDir,
			SharedSettings sharedSettings) {
		setFileUri(fileUri);
		setTemplateId(templateId);
		setTemplateBaseDir(templateBaseDir);
		setProjectUri(projectUri);
		this.sharedSettings = sharedSettings;
	}

	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}

	public void setSharedSettings(SharedSettings sharedSettings) {
		this.sharedSettings = sharedSettings;
	}
}
