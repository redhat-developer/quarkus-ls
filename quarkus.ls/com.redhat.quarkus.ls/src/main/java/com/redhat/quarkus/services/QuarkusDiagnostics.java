/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.model.values.ValuesRulesManager;
import com.redhat.quarkus.settings.QuarkusValidationSettings;

/**
 * Quarkus diagnostics support.
 *
 */
class QuarkusDiagnostics {

	/**
	 * Validate the given application.properties <code>document</code> by using the
	 * given Quarkus properties metadata <code>projectInfo</code>.
	 * 
	 * @param document           the properties model.
	 * @param projectInfo        the Quarkus properties
	 * @param valuesRulesManager manager for values rules
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(PropertiesModel document, QuarkusProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, QuarkusValidationSettings validationSettings,
			CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = QuarkusValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		if (validationSettings.isEnabled()) {
			QuarkusValidator validator = new QuarkusValidator(projectInfo, valuesRulesManager, diagnostics,
					validationSettings);
			validator.validate(document, cancelChecker);
		}
		return diagnostics;
	}

}
