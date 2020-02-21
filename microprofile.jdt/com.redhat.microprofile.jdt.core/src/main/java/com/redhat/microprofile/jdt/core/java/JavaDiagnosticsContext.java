/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core.java;

import java.util.List;

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

	private final List<Diagnostic> diagnostics;

	private final DocumentFormat documentFormat;

	public JavaDiagnosticsContext(String uri, ITypeRoot typeRoot, IJDTUtils utils, DocumentFormat documentFormat,
			List<Diagnostic> diagnostics) {
		super(uri, typeRoot, utils);
		this.diagnostics = diagnostics;
		this.documentFormat = documentFormat;
	}

	public List<Diagnostic> getDiagnostics() {
		return diagnostics;
	}

	public DocumentFormat getDocumentFormat() {
		return documentFormat;
	}

	public Diagnostic addDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code) {
		Diagnostic diagnostic = createDiagnostic(uri, message, range, source, code);
		getDiagnostics().add(diagnostic);
		return diagnostic;
	}

	private Diagnostic createDiagnostic(String uri, String message, Range range, String source, IJavaErrorCode code) {
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
