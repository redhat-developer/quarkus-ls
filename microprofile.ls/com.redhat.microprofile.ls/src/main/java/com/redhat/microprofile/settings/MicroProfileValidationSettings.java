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
 * Quarkus validation settings.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileValidationSettings {

	private static enum Severity {
		none, error, warning;
	}

	public static final MicroProfileValidationSettings DEFAULT;

	private static final MicroProfileValidationTypeSettings DEFAULT_SYNTAX;
	private static final MicroProfileValidationTypeSettings DEFAULT_UNKNOWN;
	private static final MicroProfileValidationTypeSettings DEFAULT_DUPLICATE;
	private static final MicroProfileValidationTypeSettings DEFAULT_VALUE;
	private static final MicroProfileValidationTypeSettings DEFAULT_REQUIRED;

	static {
		DEFAULT_SYNTAX = new MicroProfileValidationTypeSettings();
		DEFAULT_SYNTAX.setSeverity(Severity.error.name());
		DEFAULT_UNKNOWN = new MicroProfileValidationTypeSettings();
		DEFAULT_UNKNOWN.setSeverity(Severity.warning.name());
		DEFAULT_DUPLICATE = new MicroProfileValidationTypeSettings();
		DEFAULT_DUPLICATE.setSeverity(Severity.warning.name());
		DEFAULT_VALUE = new MicroProfileValidationTypeSettings();
		DEFAULT_VALUE.setSeverity(Severity.error.name());
		DEFAULT_REQUIRED = new MicroProfileValidationTypeSettings();
		DEFAULT_REQUIRED.setSeverity(Severity.none.name());
		DEFAULT = new MicroProfileValidationSettings();
		DEFAULT.updateDefault();
	}

	private transient boolean updated;

	private boolean enabled;

	private MicroProfileValidationTypeSettings syntax;
	private MicroProfileValidationTypeSettings unknown;
	private MicroProfileValidationTypeSettings duplicate;
	private MicroProfileValidationTypeSettings value;
	private MicroProfileValidationTypeSettings required;

	public MicroProfileValidationSettings() {
		setEnabled(true);
	}

	/**
	 * Returns true if the validation is enabled and false otherwise.
	 * 
	 * @return true if the validation is enabled and false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set true if the validation is enabled and false otherwise.
	 * 
	 * @param enabled true if the validation is enabled and false otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the settings for syntax validation.
	 * 
	 * @return the settings for syntax validation.
	 */
	public MicroProfileValidationTypeSettings getSyntax() {
		updateDefault();
		return syntax;
	}

	/**
	 * Set the settings for syntax validation.
	 * 
	 * @param syntax the settings for syntax validation.
	 */
	public void setSyntax(MicroProfileValidationTypeSettings syntax) {
		this.syntax = syntax;
		this.updated = false;
	}

	/**
	 * Returns the settings for unknown Quarkus properties validation.
	 * 
	 * @return the settings for unknown Quarkus properties validation.
	 */
	public MicroProfileValidationTypeSettings getUnknown() {
		updateDefault();
		return unknown;
	}

	/**
	 * Set the settings for unknown Quarkus properties validation.
	 * 
	 * @param unknown the settings for unknown Quarkus properties validation.
	 */
	public void setUnknown(MicroProfileValidationTypeSettings unknown) {
		this.unknown = unknown;
		this.updated = false;
	}

	/**
	 * Returns the settings for duplicate Quarkus properties validation.
	 * 
	 * @return the settings for duplicate Quarkus properties validation.
	 */
	public MicroProfileValidationTypeSettings getDuplicate() {
		updateDefault();
		return duplicate;
	}

	/**
	 * Set the settings for duplicate Quarkus properties validation.
	 * 
	 * @param duplicate the settings for duplicate Quarkus properties validation.
	 */
	public void setDuplicate(MicroProfileValidationTypeSettings duplicate) {
		this.duplicate = duplicate;
		this.updated = false;
	}

	public MicroProfileValidationTypeSettings getRequired() {
		updateDefault();
		return required;
	}

	public void setRequired(MicroProfileValidationTypeSettings required) {
		this.required = required;
		this.updated = false;
	}

	/**
	 * Returns the settings for value of Quarkus properties validation.
	 * 
	 * @return the settings for value of Quarkus properties validation.
	 */
	public MicroProfileValidationTypeSettings getValue() {
		updateDefault();
		return value;
	}

	/**
	 * Set the settings for value of Quarkus properties validation.
	 * 
	 * @param value the settings for value of Quarkus properties validation.
	 */
	public void setValue(MicroProfileValidationTypeSettings value) {
		this.value = value;
		this.updated = false;
	}

	/**
	 * Update each kind of validation settings with default value if not defined.
	 */
	private void updateDefault() {
		if (updated) {
			return;
		}
		setSyntax(syntax != null ? syntax : DEFAULT_SYNTAX);
		setUnknown(unknown != null ? unknown : DEFAULT_UNKNOWN);
		setDuplicate(duplicate != null ? duplicate : DEFAULT_DUPLICATE);
		setValue(value != null ? value : DEFAULT_VALUE);
		setRequired(required != null ? required : DEFAULT_REQUIRED);
		updated = true;
	}

	/**
	 * Update the the validation settings with the given new validation settings.
	 * 
	 * @param newValidation the new validation settings.
	 */
	public void update(MicroProfileValidationSettings newValidation) {
		this.setEnabled(newValidation.isEnabled());
		this.setSyntax(newValidation.getSyntax());
		this.setUnknown(newValidation.getUnknown());
		this.setDuplicate(newValidation.getDuplicate());
		this.setRequired(newValidation.getRequired());
		this.setValue(newValidation.getValue());
	}
}