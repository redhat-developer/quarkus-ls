/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.settings;

/**
 * Quarkus shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {

	private final MicroProfileCompletionSettings completionSettings;
	private final MicroProfileHoverSettings hoverSettings;
	private final MicroProfileSymbolSettings symbolSettings;
	private final MicroProfileValidationSettings validationSettings;
	private final MicroProfileFormattingSettings formattingSettings;
	private final MicroProfileCommandCapabilities commandCapabilities;
	private final MicroProfileCodeLensSettings codeLensSettings;

	public SharedSettings() {
		this.completionSettings = new MicroProfileCompletionSettings();
		this.hoverSettings = new MicroProfileHoverSettings();
		this.symbolSettings = new MicroProfileSymbolSettings();
		this.validationSettings = new MicroProfileValidationSettings();
		this.formattingSettings = new MicroProfileFormattingSettings();
		this.commandCapabilities = new MicroProfileCommandCapabilities();
		this.codeLensSettings = new MicroProfileCodeLensSettings();
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public MicroProfileCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the hover settings.
	 * 
	 * @return the hover settings.
	 */
	public MicroProfileHoverSettings getHoverSettings() {
		return hoverSettings;
	}

	/**
	 * Returns the symbol settings.
	 * 
	 * @return the symbol settings.
	 */
	public MicroProfileSymbolSettings getSymbolSettings() {
		return symbolSettings;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public MicroProfileValidationSettings getValidationSettings() {
		return validationSettings;
	}

	/**
	 * Returns the formatting settings.
	 * 
	 * @return the formatting settings.
	 */
	public MicroProfileFormattingSettings getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the command capabilities.
	 * 
	 * @return the command capabilities.
	 */
	public MicroProfileCommandCapabilities getCommandCapabilities() {
		return commandCapabilities;
	}

	/**
	 * Returns the code lens settings.
	 * 
	 * @return the code lens settings.
	 */
	public MicroProfileCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}
}
