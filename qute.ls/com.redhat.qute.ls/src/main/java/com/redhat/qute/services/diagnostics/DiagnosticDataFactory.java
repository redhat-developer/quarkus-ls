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
package com.redhat.qute.services.diagnostics;

import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.QUTE_SOURCE;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.parser.validator.IQuteErrorCode;
import com.redhat.qute.utils.JSONUtility;

/**
 * Diagnostic factory.
 *
 * @author Angelo ZERR
 *
 */
public class DiagnosticDataFactory {

	/**
	 * Returns the Java base type (signature) of the covered node by the diagnostic
	 * error range and null otherwise.
	 * 
	 * @param diagnostic the diagnostic.
	 * 
	 * @return the Java base type (signature) of the covered node by the diagnostic
	 *         error range and null otherwise.
	 */
	public static JavaBaseTypeOfPartData getJavaBaseTypeOfPartData(Diagnostic diagnostic) {
		if (diagnostic.getData() == null) {
			// TODO: the client may not support diagnostic data; resolve the needed
			// information some other way, such as with the Java cache
			return null;
		}
		return JSONUtility.toModel(diagnostic.getData(), JavaBaseTypeOfPartData.class);
	}

	public static Diagnostic createDiagnostic(Range range, DiagnosticSeverity severity, IQuteErrorCode errorCode,
			Object... arguments) {
		String message = errorCode.getMessage(arguments);
		return createDiagnostic(range, message, severity, errorCode);
	}

	public static Diagnostic createDiagnostic(Range range, String message, DiagnosticSeverity severity,
			IQuteErrorCode errorCode) {
		Diagnostic diagnostic = new Diagnostic(range, message, severity, QUTE_SOURCE,
				errorCode != null ? errorCode.getCode() : null);
		return diagnostic;
	}
}
