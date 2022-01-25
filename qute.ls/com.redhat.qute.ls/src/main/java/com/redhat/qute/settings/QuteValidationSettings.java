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
 * Qute validation settings.
 *
 * @author Angelo ZERR
 *
 */
public class QuteValidationSettings {

	private static enum Severity {
		none, error, warning;
	}

	public static final QuteValidationSettings DEFAULT;

	private static final QuteValidationTypeSettings UNDEFINED_VARIABLE;

	static {
		UNDEFINED_VARIABLE = new QuteValidationTypeSettings();
		UNDEFINED_VARIABLE.setSeverity(Severity.warning.name());
		DEFAULT = new QuteValidationSettings();
		DEFAULT.updateDefault();
	}

	private transient boolean updated;

	private boolean enabled;

	private QuteValidationTypeSettings undefinedVariable;

	public QuteValidationSettings() {
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
	 * Update each kind of validation settings with default value if not defined.
	 */
	private void updateDefault() {
		if (updated) {
			return;
		}
		setUndefinedVariable(undefinedVariable != null ? undefinedVariable : UNDEFINED_VARIABLE);
		updated = true;
	}

	/**
	 * Update the the validation settings with the given new validation settings.
	 *
	 * @param newValidation the new validation settings.
	 */
	public void update(QuteValidationSettings newValidation) {
		this.setEnabled(newValidation.isEnabled());
		this.setUndefinedVariable(newValidation.getUndefinedVariable());
	}

	/**
	 * Returns the settings for Qute undefined variable validation.
	 *
	 * @return the settings for Qute undefined variable validation
	 */
	public QuteValidationTypeSettings getUndefinedVariable() {
		updateDefault();
		return this.undefinedVariable;
	}

	/**
	 * Set the settings for Qute undefined variable validation.
	 *
	 * @param undefined the settings for Qute undefined variable validation.
	 */
	public void setUndefinedVariable(QuteValidationTypeSettings undefined) {
		this.undefinedVariable = undefined;
		this.updated = false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((undefinedVariable == null) ? 0 : undefinedVariable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuteValidationSettings other = (QuteValidationSettings) obj;
		if (enabled != other.enabled)
			return false;
		if (undefinedVariable == null) {
			if (other.undefinedVariable != null)
				return false;
		} else if (!undefinedVariable.equals(other.undefinedVariable))
			return false;
		return true;
	}

}