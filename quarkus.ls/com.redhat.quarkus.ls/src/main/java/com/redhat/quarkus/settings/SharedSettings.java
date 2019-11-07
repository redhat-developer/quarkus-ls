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

/**
 * Quarkus shared settings.
 * 
 * @author Angelo ZERR
 *
 */
public class SharedSettings {

	private final QuarkusCompletionSettings completionSettings;
	private final QuarkusHoverSettings hoverSettings;
	private final QuarkusSymbolSettings symbolSettings;
	private final QuarkusValidationSettings validationSettings;
	private final QuarkusFormattingSettings formattingSettings;
	private final QuarkusCommandCapabilities commandCapabilities;
	private final QuarkusCodeLensSettings codeLensSettings;

	public SharedSettings() {
		this.completionSettings = new QuarkusCompletionSettings();
		this.hoverSettings = new QuarkusHoverSettings();
		this.symbolSettings = new QuarkusSymbolSettings();
		this.validationSettings = new QuarkusValidationSettings();
		this.formattingSettings = new QuarkusFormattingSettings();
		this.commandCapabilities = new QuarkusCommandCapabilities();
		this.codeLensSettings = new QuarkusCodeLensSettings();
	}

	/**
	 * Returns the completion settings.
	 * 
	 * @return the completion settings.
	 */
	public QuarkusCompletionSettings getCompletionSettings() {
		return completionSettings;
	}

	/**
	 * Returns the hover settings.
	 * 
	 * @return the hover settings.
	 */
	public QuarkusHoverSettings getHoverSettings() {
		return hoverSettings;
	}

	/**
	 * Returns the symbol settings.
	 * 
	 * @return the symbol settings.
	 */
	public QuarkusSymbolSettings getSymbolSettings() {
		return symbolSettings;
	}

	/**
	 * Returns the validation settings.
	 * 
	 * @return the validation settings.
	 */
	public QuarkusValidationSettings getValidationSettings() {
		return validationSettings;
	}

	/**
	 * Returns the formatting settings.
	 * 
	 * @return the formatting settings.
	 */
	public QuarkusFormattingSettings getFormattingSettings() {
		return formattingSettings;
	}

	/**
	 * Returns the command capabilities.
	 * 
	 * @return the command capabilities.
	 */
	public QuarkusCommandCapabilities getCommandCapabilities() {
		return commandCapabilities;
	}

	/**
	 * Returns the code lens settings.
	 * 
	 * @return the code lens settings.
	 */
	public QuarkusCodeLensSettings getCodeLensSettings() {
		return codeLensSettings;
	}
}
