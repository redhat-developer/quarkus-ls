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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.jdt.utils.IJDTUtils;

/**
 * Report diagnostics error for non existing Qute template for:
 * 
 * <ul>
 * <li>declared method which have class annotated with @CheckedTemplate.</li>
 * <li>declared field which have Template as type.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class QuteJavaDiagnosticsCollector extends AbstractQuteTemplateLinkCollector {

	public static final String QUTE_SOURCE = "qute";

	private final List<Diagnostic> diagnostics;

	public QuteJavaDiagnosticsCollector(ITypeRoot typeRoot, List<Diagnostic> diagnostics, IJDTUtils utils,
			IProgressMonitor monitor) {
		super(typeRoot, utils, monitor);
		this.diagnostics = diagnostics;
	}

	@Override
	protected void collectTemplateLink(ASTNode fieldOrMethod, ASTNode locationAnnotation, TypeDeclaration type,
			String className, String fieldOrMethodName, String location, IFile templateFile, String templateFilePath)
			throws JavaModelException {
		if (!templateFile.exists()) {
			// No template matching the path HelloResource/index could be found for:
			// org.acme.HelloResource$Templates.index
			String path = createPath(className, fieldOrMethodName, location);
			ITypeBinding binding = type.resolveBinding();
			String fullQualifiedName = ((IType) binding.getJavaElement()).getFullyQualifiedName();
			Range range = createRange(locationAnnotation != null ? locationAnnotation : fieldOrMethod);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Error, QuteErrorCode.NoMatchingTemplate,
					path, fullQualifiedName);
			this.diagnostics.add(diagnostic);
		}
	}

	private String createPath(String className, String fieldOrMethodName, String location) {
		if (location != null) {
			return location;
		}
		if (className == null) {
			return fieldOrMethodName;
		}
		return className + '/' + fieldOrMethodName;
	}

	private static Diagnostic createDiagnostic(Range range, DiagnosticSeverity severity, IQuteErrorCode errorCode,
			Object... arguments) {
		String message = errorCode.getMessage(arguments);
		return new Diagnostic(range, message, severity, QUTE_SOURCE, errorCode.getCode());
	}
}
