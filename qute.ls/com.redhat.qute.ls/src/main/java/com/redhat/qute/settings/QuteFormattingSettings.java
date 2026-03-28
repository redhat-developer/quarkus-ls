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

/**
 * Qute formatting settings
 */
public class QuteFormattingSettings extends LSPFormattingOptions {

	public static final int DEFAULT_TAB_SIZE = 2;

	public static final boolean DEFAULT_INSERT_SPACES = true;

	public static enum SplitSectionParameters {
		preserve, splitNewLine, alignWithFirstParam;
	}

	private String splitSectionParameters;

	private int splitSectionParametersIndentSize;

	public QuteFormattingSettings() {
		this(false);
	}

	/**
	 * Create an XMLFormattingOptions instance with the option to initialize default
	 * values for all supported settings.
	 */
	public QuteFormattingSettings(boolean initializeDefaults) {
		if (initializeDefaults) {
			initializeDefaultSettings();
		}
	}

	/**
	 * Necessary: Initialize default values in case client does not provide one
	 */
	private void initializeDefaultSettings() {
		super.setTabSize(DEFAULT_TAB_SIZE);
		super.setInsertSpaces(DEFAULT_INSERT_SPACES);
		this.setSplitSectionParameters(SplitSectionParameters.preserve);
	}

	public SplitSectionParameters getSplitSectionParameters() {
		String value = splitSectionParameters;
		if ((value != null)) {
			try {
				return SplitSectionParameters.valueOf(value);
			} catch (Exception e) {
			}
		}
		return SplitSectionParameters.preserve;
	}

	public void setSplitSectionParameters(SplitSectionParameters splitSectionParameters) {
		this.splitSectionParameters = splitSectionParameters.name();
	}

	public void setSplitSectionParametersIndentSize(int splitSectionParametersIndentSize) {
		this.splitSectionParametersIndentSize = splitSectionParametersIndentSize;
	}

	public int getSplitSectionParametersIndentSize() {
		int splitSectionParametersIndentSize = this.splitSectionParametersIndentSize;
		return splitSectionParametersIndentSize < 0 ? 0 : splitSectionParametersIndentSize;
	}

	public void update(QuteFormattingSettings formattingSettings) {
		setTabSize(formattingSettings.getTabSize());
		setInsertSpaces(formattingSettings.isInsertSpaces());
		setSplitSectionParameters(formattingSettings.getSplitSectionParameters());
		setSplitSectionParametersIndentSize(formattingSettings.getSplitSectionParametersIndentSize());
	}
}
