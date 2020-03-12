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
package com.redhat.microprofile.jdt.internal.health.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.microprofile.jdt.core.java.codeaction.IJavaCodeActionParticipant;
import com.redhat.microprofile.jdt.core.java.codeaction.JavaCodeActionContext;
import com.redhat.microprofile.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import com.redhat.microprofile.jdt.core.java.corrections.proposal.NewAnnotationProposal;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;

/**
 * QuickFix for fixing
 * {@link MicroProfileHealthErrorCode#HealthAnnotationMissing} error by
 * providing several code actions:
 * 
 * <ul>
 * <li>Insert @Liveness annotation and the proper import.</li>
 * <li>Insert @Readiness annotation and the proper import.</li>
 * <li>Insert @Health annotation and the proper import.</li>
 * </ul>
 * 
 * @author Angelo ZERR
 *
 */
public class HealthAnnotationMissingQuickFix implements IJavaCodeActionParticipant {

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		ASTNode node = context.getCoveredNode();
		ITypeBinding parentType = Bindings.getBindingOfParentType(node);
		if (parentType != null) {
			List<CodeAction> codeActions = new ArrayList<>();
			insertAnnotation(diagnostic, context, parentType, MicroProfileHealthConstants.LIVENESS_ANNOTATION,
					codeActions);
			insertAnnotation(diagnostic, context, parentType, MicroProfileHealthConstants.READINESS_ANNOTATION,
					codeActions);
			insertAnnotation(diagnostic, context, parentType, MicroProfileHealthConstants.HEALTH_ANNOTATION,
					codeActions);
			return codeActions;
		}
		return null;
	}

	private static void insertAnnotation(Diagnostic diagnostic, JavaCodeActionContext context, ITypeBinding parentType,
			String annotation, List<CodeAction> codeActions) throws CoreException {
		// Insert the annotation and the proper import by using JDT Core Manipulation
		// API
		String name = annotation.substring(annotation.lastIndexOf('.') + 1, annotation.length());
		ChangeCorrectionProposal proposal = new NewAnnotationProposal("Insert @" + name, context.getCompilationUnit(),
				context.getASTRoot(), parentType, annotation, 0);
		// Convert the proposal to LSP4J CodeAction
		CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
		codeActions.add(codeAction);
	}

}
