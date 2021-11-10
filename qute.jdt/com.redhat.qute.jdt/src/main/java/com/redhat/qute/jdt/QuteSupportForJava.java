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
package com.redhat.qute.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.commons.QuteJavaDiagnosticsParams;
import com.redhat.qute.commons.QuteJavaDocumentLinkParams;
import com.redhat.qute.jdt.internal.java.QuarkusIntegrationForQute;
import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Qute support for Java file.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteSupportForJava {

	private static final QuteSupportForJava INSTANCE = new QuteSupportForJava();

	public static QuteSupportForJava getInstance() {
		return INSTANCE;
	}

	public List<? extends CodeLens> codeLens(QuteJavaCodeLensParams params, IJDTUtils utils, IProgressMonitor monitor) {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return QuarkusIntegrationForQute.codeLens(typeRoot, utils, monitor);
	}

	public List<PublishDiagnosticsParams> diagnostics(QuteJavaDiagnosticsParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		List<String> uris = params.getUris();
		if (uris == null) {
			return Collections.emptyList();
		}
		List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
		for (String uri : uris) {
			if (monitor.isCanceled()) {
				return Collections.emptyList();
			}
			List<Diagnostic> diagnostics = new ArrayList<>();
			PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
			publishDiagnostics.add(publishDiagnostic);
			ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
			QuarkusIntegrationForQute.diagnostics(typeRoot, diagnostics, utils, monitor);
		}
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return publishDiagnostics;
	}

	public List<DocumentLink> documentLink(QuteJavaDocumentLinkParams params, IJDTUtils utils,
			IProgressMonitor monitor) {
		String uri = params.getUri();
		ITypeRoot typeRoot = resolveTypeRoot(uri, utils, monitor);
		if (monitor.isCanceled()) {
			return Collections.emptyList();
		}
		return QuarkusIntegrationForQute.documentLink(typeRoot, utils, monitor);
	}

	/**
	 * Given the uri returns a {@link ITypeRoot}. May return null if it can not
	 * associate the uri with a Java file ot class file.
	 *
	 * @param uri
	 * @param utils   JDT LS utilities
	 * @param monitor the progress monitor
	 * @return compilation unit
	 */
	private static ITypeRoot resolveTypeRoot(String uri, IJDTUtils utils, IProgressMonitor monitor) {
		utils.waitForLifecycleJobs(monitor);
		final ICompilationUnit unit = utils.resolveCompilationUnit(uri);
		IClassFile classFile = null;
		if (unit == null) {
			classFile = utils.resolveClassFile(uri);
			if (classFile == null) {
				return null;
			}
		} else {
			if (!unit.getResource().exists() || monitor.isCanceled()) {
				return null;
			}
		}
		return unit != null ? unit : classFile;
	}

}
