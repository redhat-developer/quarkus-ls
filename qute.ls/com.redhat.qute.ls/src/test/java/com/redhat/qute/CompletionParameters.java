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

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemInsertTextModeSupportCapabilities;
import org.eclipse.lsp4j.InsertTextMode;

import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteNativeSettings;

public class CompletionParameters extends BaseParameters {

	private Integer expectedCount;
	private QuteNativeSettings nativeImagesSettings;
	private QuteCompletionSettings completionSettings;
	private boolean itemDefaultsSupport;

	public CompletionParameters() {
		super(); // Add snippet support for completion
		QuteCompletionSettings completionSettings = new QuteCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		CompletionItemInsertTextModeSupportCapabilities insertTextModeSupport = new CompletionItemInsertTextModeSupportCapabilities();
		insertTextModeSupport.setValueSet(Arrays.asList(InsertTextMode.AsIs, InsertTextMode.AdjustIndentation));
		completionItemCapabilities.setInsertTextModeSupport(insertTextModeSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionCapabilities.setInsertTextMode(InsertTextMode.AdjustIndentation);
		completionSettings.setCapabilities(completionCapabilities);
		setCompletionSettings(completionSettings);
	}

	public Integer getExpectedCount() {
		return expectedCount;
	}

	public void setExpectedCount(Integer expectedCount) {
		this.expectedCount = expectedCount;
	}

	public QuteNativeSettings getNativeImagesSettings() {
		return nativeImagesSettings;
	}

	public void setNativeImagesSettings(QuteNativeSettings nativeImagesSettings) {
		this.nativeImagesSettings = nativeImagesSettings;
	}

	public QuteCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	public void setCompletionSettings(QuteCompletionSettings completionSettings) {
		this.completionSettings = completionSettings;
	}

	public boolean isItemDefaultsSupport() {
		return itemDefaultsSupport;
	}

	public void setItemDefaultsSupport(boolean itemDefaultsSupport) {
		this.itemDefaultsSupport = itemDefaultsSupport;
	}

}
