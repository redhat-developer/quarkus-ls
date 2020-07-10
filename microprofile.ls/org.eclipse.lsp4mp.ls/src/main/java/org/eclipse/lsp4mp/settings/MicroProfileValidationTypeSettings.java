/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.settings;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.utils.AntPathMatcher;

/**
 * MicroProfile validation type settings.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileValidationTypeSettings {

	private String severity;

	private String[] excluded;

	private transient List<ExcludedProperty> excludedProperties;

	private static class ExcludedProperty {

		private final String pattern;
		private final AntPathMatcher matcher;

		public ExcludedProperty(String pattern, AntPathMatcher matcher) {
			this.pattern = pattern;
			this.matcher = matcher.isPattern(pattern) ? matcher : null;
		}

		/**
		 * Returns true if the given property name matches the pattern and false
		 * otherwise.
		 * 
		 * @param propertyName the property name.
		 * @return true if the given property name matches the pattern and false
		 *         otherwise.
		 */
		public boolean match(String propertyName) {
			if (matcher != null) {
				// the excluded property is a pattern, use pattern matcher to check the match
				return matcher.match(pattern, propertyName);
			}
			// the excluded property is not a pattern, check if the property name is equal
			// to the pattern
			return pattern.equals(propertyName);
		}

	}

	/**
	 * Returns the severity of the validation type.
	 * 
	 * @return the severity of the validation type.
	 */
	public String getSeverity() {
		return severity;
	}

	/**
	 * Set the severity of the validation type.
	 * 
	 * @param severity the severity of the validation type.
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

	/**
	 * Returns the array of properties to ignore for this validation type.
	 * 
	 * @return the array of properties to ignore for this validation type.
	 */
	public String[] getExcluded() {
		return excluded;
	}

	/**
	 * Set the array of properties to ignore for this validation type.
	 * 
	 * @param excluded the array of properties to ignore for this validation type.
	 */
	public void setExcluded(String[] excluded) {
		this.excluded = excluded;
	}

	/**
	 * Returns the diagnostic severity according the given property name and null
	 * otherwise.
	 * 
	 * @param propertyName the property name.
	 * @return the diagnostic severity according the given property name and null
	 *         otherwise.
	 */
	public DiagnosticSeverity getDiagnosticSeverity(String propertyName) {
		DiagnosticSeverity severity = getDiagnosticSeverity();
		if (severity == null) {
			return null;
		}
		return isExcluded(propertyName) ? null : severity;
	}

	private DiagnosticSeverity getDiagnosticSeverity() {
		DiagnosticSeverity[] severities = DiagnosticSeverity.values();
		for (DiagnosticSeverity severity : severities) {
			if (severity.name().toUpperCase().equals(this.severity.toUpperCase())) {
				return severity;
			}
		}
		return null;
	}

	/**
	 * Returns true if the given property name must be excluded and false otherwise.
	 * 
	 * @param propertyName the property name
	 * @return true if the given property name must be excluded and false otherwise.
	 */
	private boolean isExcluded(String propertyName) {
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
		return Stream.of(excluded) //
				.map(p -> new ExcludedProperty(p, matcher)) //
				.collect(Collectors.toList());
	}

}
