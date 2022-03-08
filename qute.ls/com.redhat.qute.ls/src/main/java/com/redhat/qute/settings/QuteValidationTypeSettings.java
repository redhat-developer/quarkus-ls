/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.settings;

import org.eclipse.lsp4j.DiagnosticSeverity;

/**
 * Qute validation type settings.
 *
 */
public class QuteValidationTypeSettings {

	private String severity;

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
	 * Returns the diagnostic severity according the given property name and null
	 * otherwise.
	 *
	 * @return the diagnostic severity according the given property name and null
	 *         otherwise.
	 */

	public DiagnosticSeverity getDiagnosticSeverity() {
		DiagnosticSeverity[] severities = DiagnosticSeverity.values();
		for (DiagnosticSeverity severity : severities) {
			if (severity.name().toUpperCase().equals(this.severity.toUpperCase())) {
				return severity;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((severity == null) ? 0 : severity.hashCode());
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
		QuteValidationTypeSettings other = (QuteValidationTypeSettings) obj;
		if (severity == null) {
			if (other.severity != null)
				return false;
		} else if (!severity.equals(other.severity))
			return false;
		return true;
	}

}