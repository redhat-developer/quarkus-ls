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
import com.redhat.microprofile.jdt.core.java.corrections.proposal.ImplementInterfaceProposal;
import com.redhat.microprofile.jdt.internal.health.MicroProfileHealthConstants;

/**
 * QuickFix for fixing {@link MicroProfileHealthErrorCode#ImplementHealthCheck}
 * error by providing the code actions which implements
 * 'org.eclipse.microprofile.health.HealthCheck'.
 * 
 * @author Angelo ZERR
 *
 */
public class ImplementHealthCheckQuickFix implements IJavaCodeActionParticipant {

	@Override
	public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
			IProgressMonitor monitor) throws CoreException {
		ASTNode node = context.getCoveredNode();
		ITypeBinding parentType = Bindings.getBindingOfParentType(node);
		if (parentType != null) {
			List<CodeAction> codeActions = new ArrayList<>();
			// Create code action to implement 'org.eclipse.microprofile.health.HealthCheck'
			// interface
			ChangeCorrectionProposal proposal = new ImplementInterfaceProposal(context.getCompilationUnit(), parentType,
					context.getASTRoot(), MicroProfileHealthConstants.HEALTH_CHECK_INTERFACE, 0);
			CodeAction codeAction = context.convertToCodeAction(proposal, diagnostic);
			codeActions.add(codeAction);
			return codeActions;
		}
		return null;
	}

}
