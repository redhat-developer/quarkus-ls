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

import com.google.gson.JsonObject;

public class DiagnosticDataFactory {

	public static JsonObject createUndefinedVariableData(String partName, boolean iterable) {
		JsonObject data = new JsonObject();
		data.addProperty(DIAGNOSTIC_DATA_NAME, partName);
		data.addProperty(DIAGNOSTIC_DATA_ITERABLE, iterable);
		return data;
	}
}
