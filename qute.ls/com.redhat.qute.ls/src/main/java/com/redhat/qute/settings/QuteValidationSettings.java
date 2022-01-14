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

import java.util.List;
import java.util.stream.Collectors;

import com.redhat.qute.utils.AntPathMatcher;

/**
 * Qute validation settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteValidationSettings {

	public static final QuteValidationSettings DEFAULT;

	static {
		DEFAULT = new QuteValidationSettings();
		DEFAULT.updateDefault();
	}

	private transient boolean updated;

	private boolean enabled;

	private List<String> excluded;

	private transient List<ExcludedProperty> excludedProperties;

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
	 * Returns the array of properties to ignore for this validation type.
	 *
	 * @return the array of properties to ignore for this validation type.
	 */
	public List<String> getExcluded() {
		return excluded;
	}

	/**
	 * Set the array of properties to ignore for this validation type.
	 *
	 * @param excluded the array of properties to ignore for this validation type.
	 */
	public void setExcluded(List<String> excluded) {
		this.excluded = excluded;
	}

	/**
	 * Update each kind of validation settings with default value if not defined.
	 */
	private void updateDefault() {
		if (updated) {
			return;
		}
		updated = true;
	}

	/**
	 * Update the the validation settings with the given new validation settings.
	 * 
	 * @param newValidation the new validation settings.
	 */
	public void update(QuteValidationSettings newValidation) {
		this.setEnabled(newValidation.isEnabled());
		this.setExcluded(newValidation.getExcluded());
	}

	/**
	 * Returns true if the given property name must be excluded and false otherwise.
	 *
	 * @param propertyName the property name
	 * @return true if the given property name must be excluded and false otherwise.
	 */
	public boolean isExcluded(String propertyName) {
		if (excluded == null) {
			return false;
		}
		// Get compiled excluded properties
		List<ExcludedProperty> excludedProperties = getExcludedProperties();
		for (ExcludedProperty excluded : excludedProperties) {
			// the property name matches an excluded pattern
			if (excluded.match(propertyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the compiled excluded properties.
	 *
	 * @return the compiled excluded properties.
	 */
	private List<ExcludedProperty> getExcludedProperties() {
		if (excludedProperties != null) {
			return excludedProperties;
		}
		return createExcludedProperties();
	}

	/**
	 * Create the compiled excluded properties.
	 *
	 * @return the compiled excluded properties.
	 */
	private synchronized List<ExcludedProperty> createExcludedProperties() {
		if (excludedProperties != null) {
			return excludedProperties;
		}
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setCachePatterns(true);
		return excluded.stream() //
				.map(p -> new ExcludedProperty(p, matcher)) //
				.collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((excluded == null) ? 0 : excluded.hashCode());
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
		if (excluded == null) {
			if (other.excluded != null)
				return false;
		} else if (!excluded.equals(other.excluded))
			return false;
		return true;
	}

}