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
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.utils.JSONUtility;

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


	public static UnknownPropertyData getMissingMemberData(Diagnostic diagnostic) {
		if (diagnostic.getData() == null) {
			// TODO: the client may not support diagnostic data; resolve the needed
			// information some other way, such as with the Java cache
			return null;
		}
		return JSONUtility.toModel(diagnostic.getData(), UnknownPropertyData.class);
	}

	public static UnknownPropertyData getMissingMemberData(JsonObject obj) {
		return JSONUtility.toModel(obj, UnknownPropertyData.class);
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
