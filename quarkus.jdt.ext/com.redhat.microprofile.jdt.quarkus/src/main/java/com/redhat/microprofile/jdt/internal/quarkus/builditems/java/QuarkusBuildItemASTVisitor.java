/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.builditems.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.commons.DocumentFormat;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;
import com.redhat.microprofile.jdt.internal.quarkus.builditems.QuarkusBuildItemErrorCode;

/**
 * Validates <code>io.quarkus.builder.item.BuildItem</code> subclasses.
 * <ul>
 * <li>checks if the BuildItem is final or abstract</li>
 * </ul>
 */
public class QuarkusBuildItemASTVisitor extends JavaASTValidator {

	private static final String INVALID_MODIFIER = "BuildItem class %2$s%1$s%2$s must either be declared final or abstract";

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		// Collection of diagnostics for Quarkus Build Items is done only if
		// io.quarkus.builder.item.BuildItem is on the classpath
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, QuarkusConstants.QUARKUS_BUILD_ITEM_CLASS_NAME) != null;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		ITypeBinding typeBinding = node.resolveBinding();
		if (isBuildItem(typeBinding)) {
			validateBuildItem(node, typeBinding, super.getContext());
		}
		return super.visit(node);
	}

	/**
	 * Returns true if the given type declaration extends
	 * 'io.quarkus.builder.item.BuildItem' class and false otherwise.
	 * 
	 * @param typeBinding the type declaration.
	 * @return true if the given type declaration extends
	 *         'io.quarkus.builder.item.BuildItem' class and false otherwise.
	 */
	private static boolean isBuildItem(ITypeBinding typeBinding) {
		return typeBinding != null && extendsClass(typeBinding, QuarkusConstants.QUARKUS_BUILD_ITEM_CLASS_NAME);
	}

	private void validateBuildItem(TypeDeclaration typeDeclaration, ITypeBinding typeBinding,
			JavaDiagnosticsContext context) {
		if (isValidBuildItem(typeDeclaration)) {
			return;
		}
		super.addDiagnostic(createDiagnosticMessage(typeBinding, context.getDocumentFormat()),
				QuarkusConstants.QUARKUS_DIAGNOSTIC_SOURCE, typeDeclaration.getName(),
				QuarkusBuildItemErrorCode.InvalidModifierBuildItem, DiagnosticSeverity.Error);
	}

	private static boolean isValidBuildItem(TypeDeclaration typeDeclaration) {
		int flags = typeDeclaration.getModifiers();
		return Modifier.isAbstract(flags) || Modifier.isFinal(flags);
	}

	private static String createDiagnosticMessage(ITypeBinding typeBinding, DocumentFormat documentFormat) {
		String quote = DocumentFormat.Markdown.equals(documentFormat) ? "`" : "'";
		return String.format(INVALID_MODIFIER, typeBinding.getQualifiedName(), quote);
	}

	private static boolean extendsClass(ITypeBinding typeBinding, String classNameToCheck) {
		ITypeBinding superClass = typeBinding.getSuperclass();
		while (superClass != null) {
			if (superClass.getQualifiedName().equals(classNameToCheck)) {
				return true;
			}
			superClass = superClass.getSuperclass();
		}
		return false;
	}
}