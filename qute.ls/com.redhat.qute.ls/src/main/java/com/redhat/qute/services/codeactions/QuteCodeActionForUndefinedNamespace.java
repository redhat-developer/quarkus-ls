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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Code actions for {@link QuteErrorCode#UndefinedNamespace}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteCodeActionForUndefinedNamespace extends AbstractQuteCodeAction {

	private static final Logger LOGGER = Logger.getLogger(QuteCodeActionForUndefinedNamespace.class.getName());

	private static final String UNDEFINED_NAMESPACE_SEVERITY_SETTING = "qute.validation.undefinedNamespace.severity";

	public QuteCodeActionForUndefinedNamespace(JavaDataModelCache javaCache) {
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
			NamespacePart part = (NamespacePart) node;
			Template template = request.getTemplate();
			Diagnostic diagnostic = request.getDiagnostic();

			// CodeAction(s) to replace text with similar suggestions
			doCodeActionsForSimilarValues(part, template, diagnostic, codeActions);

			// CodeAction to set validation severity to ignore
			doCodeActionToSetIgnoreSeverity(template, diagnostic,
					QuteErrorCode.UndefinedNamespace, codeActions, UNDEFINED_NAMESPACE_SEVERITY_SETTING);

		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "Creation of undefined namespace code action failed", e);
		}
	}
	
	private void doCodeActionsForSimilarValues(NamespacePart part, Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) throws BadLocationException {
		Collection<String> availableValues = collectAvailableValuesForNamespacePart(part, template);
		doCodeActionsForSimilarValues(part, availableValues, template, diagnostic, codeActions);
	}

	private Collection<String> collectAvailableValuesForNamespacePart(NamespacePart node, Template template) {
		String projectUri = template.getProjectUri();
		return javaCache.getAllNamespaces(projectUri);
	}

}
