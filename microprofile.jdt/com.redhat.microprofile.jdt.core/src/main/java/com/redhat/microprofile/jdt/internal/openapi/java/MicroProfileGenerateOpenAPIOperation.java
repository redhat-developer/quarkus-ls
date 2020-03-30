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
package com.redhat.microprofile.jdt.internal.openapi.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.microprofile.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import com.redhat.microprofile.jdt.core.java.codeaction.JavaCodeActionContext;
import com.redhat.microprofile.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import com.redhat.microprofile.jdt.core.utils.JDTTypeUtils;
import com.redhat.microprofile.jdt.internal.openapi.MicroProfileOpenAPIConstants;

/**
 * Generate OpenAPI annotations by the "Source" kind code action.
 * 
 * @author Benson Ning
 *
 */
public class MicroProfileGenerateOpenAPIOperation implements IJavaCodeActionParticipant {
	
	@Override
	public boolean isAdaptedForCodeAction(JavaCodeActionContext context, IProgressMonitor monitor)
			throws CoreException {
		IJavaProject javaProject = context.getJavaProject();
		return JDTTypeUtils.findType(javaProject, MicroProfileOpenAPIConstants.OPERATION_ANNOTATION) != null;
	}

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		List<CodeAction> codeActions = new ArrayList<>();
		CompilationUnit cu = context.getASTRoot();
		List<?> types = cu.types();
		for (Object type : types){
			if (type instanceof TypeDeclaration) {
				ChangeCorrectionProposal proposal = new OpenAPIAnnotationProposal(
						"Generate OpenAPI Annotations", context.getCompilationUnit(), context.getASTRoot(),
						(TypeDeclaration) type, MicroProfileOpenAPIConstants.OPERATION_ANNOTATION, 0);
				// Convert the proposal to LSP4J CodeAction
				CodeAction codeAction = context.convertToCodeAction(proposal);
				if (codeAction != null) { 
					codeActions.add(codeAction);
				}
			}
		}
		return codeActions;
	}
	
}
