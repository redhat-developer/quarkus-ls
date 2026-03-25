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

import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.settings.SharedSettings;

public class DiagnosticsParameters extends BaseParameters {

	private boolean filter;

	private QuteValidationSettings validationSettings;

	private QuteNativeSettings nativeImagesSettings;

	public DiagnosticsParameters() {
		super();
		SharedSettings sharedSettings = new SharedSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		sharedSettings.getHoverSettings().setCapabilities(capabilities);
		setNativeImagesSettings(new QuteNativeSettings());
	}

	public DiagnosticsParameters(String fileUri, String templateId, String projectUri, String templateBaseDir,
			boolean filter, QuteValidationSettings validationSettings, QuteNativeSettings nativeImagesSettings) {
		setFileUri(fileUri);
		setTemplateId(templateId);
		setTemplateBaseDir(templateBaseDir);
		setProjectUri(projectUri);
		setFilter(filter);
		setValidationSettings(validationSettings);
		setNativeImagesSettings(nativeImagesSettings);
	}

	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public QuteValidationSettings getValidationSettings() {
		return validationSettings;
	}

	public void setValidationSettings(QuteValidationSettings validationSettings) {
		this.validationSettings = validationSettings;
	}

	public QuteNativeSettings getNativeImagesSettings() {
		return nativeImagesSettings;
	}

	public void setNativeImagesSettings(QuteNativeSettings nativeImagesSettings) {
		this.nativeImagesSettings = nativeImagesSettings;
	}
}
