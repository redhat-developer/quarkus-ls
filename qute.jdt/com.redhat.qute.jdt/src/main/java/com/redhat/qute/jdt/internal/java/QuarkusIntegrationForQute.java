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
package com.redhat.qute.jdt.internal.java;

import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.getASTRoot;
import static com.redhat.qute.jdt.utils.JDTQuteProjectUtils.hasQuteSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentLink;

import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Quarkus integration for Qute.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusIntegrationForQute {

	public static List<? extends CodeLens> codeLens(ITypeRoot typeRoot, IJDTUtils utils, IProgressMonitor monitor) {
		if (typeRoot == null || !hasQuteSupport(typeRoot.getJavaProject())) {
			return Collections.emptyList();
		}
		List<CodeLens> lenses = new ArrayList<>();
		CompilationUnit cu = getASTRoot(typeRoot);
		cu.accept(new QuteJavaCodeLensCollector(typeRoot, lenses, utils, monitor));
		return lenses;
	}

	public static void diagnostics(ITypeRoot typeRoot, List<Diagnostic> diagnostics, IJDTUtils utils,
			IProgressMonitor monitor) {
		if (typeRoot == null || !hasQuteSupport(typeRoot.getJavaProject())) {
			return;
		}
		CompilationUnit cu = getASTRoot(typeRoot);
		cu.accept(new QuteJavaDiagnosticsCollector(typeRoot, diagnostics, utils, monitor));
	}

	public static List<DocumentLink> documentLink(ITypeRoot typeRoot, IJDTUtils utils,
			IProgressMonitor monitor) {
		if (typeRoot == null || !hasQuteSupport(typeRoot.getJavaProject())) {
			return Collections.emptyList();
		}
		List<DocumentLink> links = new ArrayList<>();
		CompilationUnit cu = getASTRoot(typeRoot);
		cu.accept(new QuteJavaDocumentLinkCollector(typeRoot, links, utils, monitor));
		return links;
	}
}
