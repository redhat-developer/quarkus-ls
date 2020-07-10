/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.values.ValuesRulesManager;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;

/**
 * MicroProfile diagnostics support.
 *
 */
class MicroProfileDiagnostics {

	/**
	 * Validate the given application.properties <code>document</code> by using the
	 * given MicroProfile properties metadata <code>projectInfo</code>.
	 * 
	 * @param document           the properties model.
	 * @param projectInfo        the MicroProfile properties
	 * @param valuesRulesManager manager for values rules
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(PropertiesModel document, MicroProfileProjectInfo projectInfo,
			ValuesRulesManager valuesRulesManager, MicroProfileValidationSettings validationSettings,
			CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = MicroProfileValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		if (validationSettings.isEnabled()) {
			MicroProfileValidator validator = new MicroProfileValidator(projectInfo, valuesRulesManager, diagnostics,
					validationSettings);
			validator.validate(document, cancelChecker);
		}
		return diagnostics;
	}

}
