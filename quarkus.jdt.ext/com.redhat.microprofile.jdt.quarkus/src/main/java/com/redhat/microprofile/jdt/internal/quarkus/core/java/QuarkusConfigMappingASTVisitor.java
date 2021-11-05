/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.internal.quarkus.core.java;

import static org.eclipse.lsp4mp.jdt.core.utils.AnnotationUtils.isMatchAnnotation;

import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4mp.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4mp.jdt.core.java.validators.JavaASTValidator;
import org.eclipse.lsp4mp.jdt.core.utils.JDTTypeUtils;

import com.redhat.microprofile.jdt.internal.quarkus.QuarkusConstants;

/**
 * Quarkus @ConfigMapping validator.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusConfigMappingASTVisitor extends JavaASTValidator {

	private static final Logger LOGGER = Logger.getLogger(QuarkusConfigMappingASTVisitor.class.getName());

	private static final String EXPECTED_INTERFACE_ERROR = "The @ConfigMapping annotation can only be placed in interfaces, class `{0}` is a class";

	@Override
	public boolean isAdaptedForDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, QuarkusConstants.CONFIG_MAPPING_ANNOTATION) != null;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		try {
			@SuppressWarnings("rawtypes")
			List modifiers = node.modifiers();
			for (Object modifier : modifiers) {
				if (modifier instanceof Annotation) {
					Annotation annotation = (Annotation) modifier;
					if (isMatchAnnotation(annotation, QuarkusConstants.CONFIG_MAPPING_ANNOTATION)) {
						validateConfigMappingAnnotation(node, annotation);
					}
				}
			}
		} catch (JavaModelException e) {
			LOGGER.log(Level.WARNING, "An exception occurred when attempting to validate the annotation marked type");
		}
		super.visit(node);
		return true;
	}

	/**
	 * Checks if the given type declaration annotated with @ConfigMapping annotation
	 * is an interface.
	 *
	 * @param node       The type declaration to validate
	 * @param annotation The @ConfigMapping annotation
	 * @throws JavaModelException
	 */
	private void validateConfigMappingAnnotation(TypeDeclaration node, Annotation annotation)
			throws JavaModelException {
		if (!node.isInterface()) {
			super.addDiagnostic(MessageFormat.format(EXPECTED_INTERFACE_ERROR, node.getName()),
					QuarkusConstants.QUARKUS_PREFIX, annotation, null, DiagnosticSeverity.Error);
		}
	}

}