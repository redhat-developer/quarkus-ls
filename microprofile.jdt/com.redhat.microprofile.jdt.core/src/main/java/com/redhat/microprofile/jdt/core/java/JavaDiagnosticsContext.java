/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.java;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.redhat.microprofile.commons.DocumentFormat;
import com.redhat.microprofile.jdt.core.utils.IJDTUtils;

/**
 * Java diagnostics context for a given compilation unit.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDiagnosticsContext extends AbtractJavaContext {

	private final DocumentFormat documentFormat;

	public JavaDiagnosticsContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, DocumentFormat documentFormat) {
		super(uri, typeRoot, utils);
		this.documentFormat = documentFormat;
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	public Diagnostic createDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code) {
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setSource(source);
		diagnostic.setMessage(message);
		diagnostic.setSeverity(DiagnosticSeverity.Warning);
		diagnostic.setRange(range);
		if (code != null) {
			diagnostic.setCode(code.getCode());
		}
		return diagnostic;
	}

}
