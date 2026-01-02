/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.settings.QuteValidationSettings;

/**
 * Participant for providing custom validation diagnostics in Qute templates.
 * 
 * @author Angelo ZERR
 */
public interface DiagnosticsParticipant {

	/**
	 * Checks if this participant is enabled.
	 * 
	 * @return true if this participant should provide diagnostics
	 */
	boolean isEnabled();

	/**
	 * Validates the given expression and adds diagnostics if issues are found.
	 * 
	 * @param parts                    the expression parts to validate
	 * @param validationSettings       validation preferences
	 * @param resolvingJavaTypeContext context for resolving Java types
	 * @param diagnostics              accumulator for diagnostics (modified
	 *                                 in-place)
	 * @return true if this participant handled the expression, false otherwise
	 */
	boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics);
}