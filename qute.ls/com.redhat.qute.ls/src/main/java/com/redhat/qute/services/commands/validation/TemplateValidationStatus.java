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
package com.redhat.qute.services.commands.validation;

import java.util.List;

/**
 * The template validation status.
 * 
 * @author Angelo ZERR
 *
 */
public class TemplateValidationStatus {

	private boolean validationEnabled;

	private List<String> excluded;

	public boolean isValidationEnabled() {
		return validationEnabled;
	}

	public void setValidationEnabled(boolean validationEnabled) {
		this.validationEnabled = validationEnabled;
	}

	public List<String> getExcluded() {
		return excluded;
	}

	public void setExcluded(List<String> excluded) {
		this.excluded = excluded;
	}

}
