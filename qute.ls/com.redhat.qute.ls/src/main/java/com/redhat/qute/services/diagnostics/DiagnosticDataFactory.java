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

import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_ITERABLE;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_NAME;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_TAG;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.QUTE_SOURCE;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.google.gson.JsonObject;

/**
 * Diagnostic factory.
 * 
 * @author Angelo ZERR
 *
 */
public class DiagnosticDataFactory {

	public static JsonObject createUndefinedObjectData(String partName, boolean iterable) {
		JsonObject data = new JsonObject();
		data.addProperty(DIAGNOSTIC_DATA_NAME, partName);
		data.addProperty(DIAGNOSTIC_DATA_ITERABLE, iterable);
		return data;
	}

	public static JsonObject createUndefinedSectionTagData(String tagName) {
		JsonObject data = new JsonObject();
		data.addProperty(DIAGNOSTIC_DATA_TAG, tagName);
		return data;
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
