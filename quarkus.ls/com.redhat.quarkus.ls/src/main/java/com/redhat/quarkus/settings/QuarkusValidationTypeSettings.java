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

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * Quarkus validation type settings.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusValidationTypeSettings {

	private String severity;

	private String[] excluded;

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
		for (String pattern : excluded) {
			if (pattern.equals(propertyName)) {
				return true;
			}
		}
		return false;
	}

}
