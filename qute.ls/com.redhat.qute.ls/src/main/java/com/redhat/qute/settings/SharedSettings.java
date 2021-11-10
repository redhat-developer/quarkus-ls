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
 * Qute shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {
	private final QuteCompletionSettings completionSettings;
	private final QuteCodeLensSettings codeLensSettings;
	private final QuteFormattingSettings formattingSettings;
	private final QuteValidationSettings validationSettings;
	private final QuteHoverSettings hoverSettings;

	public SharedSettings() {
		this.completionSettings = new QuteCompletionSettings();
		this.codeLensSettings = new QuteCodeLensSettings();
		this.formattingSettings = new QuteFormattingSettings();
		this.validationSettings = new QuteValidationSettings();
		this.hoverSettings = new QuteHoverSettings();
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
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public QuteValidationSettings getValidationSettings() {
		return validationSettings;
	}

	/**
	 * Returns the hover settings.
	 * 
	 * @return the hover settings.
	 */
	public QuteHoverSettings getHoverSettings() {
		return hoverSettings;
	}
}
