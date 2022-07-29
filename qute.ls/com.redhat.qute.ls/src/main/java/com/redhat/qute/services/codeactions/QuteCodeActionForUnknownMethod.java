/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.codeactions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.MethodPart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Code actions for {@link QuteErrorCode#UnknownMethod}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUnknownMethod extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUnknownMethod.class.getName());

	public QuteCodeActionForUnknownMethod(JavaDataModelCache javaCache) {
		super(javaCache);
	}

	@Override
	public void doCodeActions(CodeActionRequest request, List<CompletableFuture<Void>> codeActionResolveFutures,
			List<CodeAction> codeActions) {
		try {
			Node node = request.getCoveredNode();
			if (node == null) {
				return;
			}
			
			ResolvedJavaTypeInfo baseResolvedType = request.getJavaTypeOfCoveredNode(javaCache);
			if (baseResolvedType == null) {
				return;
			}
			
			MethodPart part = (MethodPart) node;
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();

			
			// TODO : implement code actions

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of unknown method code action failed", e);
		}

	}

}
