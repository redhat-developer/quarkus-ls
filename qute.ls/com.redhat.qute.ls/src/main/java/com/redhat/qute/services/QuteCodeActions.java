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
package com.redhat.qute.services;

import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_ITERABLE;
import static com.redhat.qute.services.diagnostics.QuteDiagnosticContants.DIAGNOSTIC_DATA_NAME;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.google.gson.JsonObject;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.CodeActionFactory;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.services.diagnostics.QuteErrorCode;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute code actions support.
 * 
 * @author Angelo ZERR
 *
 */
class QuteCodeActions {

	private static final String DECLARE_UNDEFINED_VARIABLE_MESSAGE = "Declare `{0}` with parameter declaration.";

	public CompletableFuture<List<CodeAction>> doCodeActions(Template template, CodeActionContext context, Range range,
			SharedSettings sharedSettings) {
		List<CodeAction> codeActions = new ArrayList<>();
		if (context.getDiagnostics() != null) {
			for (Diagnostic diagnostic : context.getDiagnostics()) {
				if (QuteErrorCode.UndefinedVariable.isQuteErrorCode(diagnostic.getCode())) {
					// Manage code action for undefined variable
					doCodeActionsForUndefinedVariable(template, diagnostic, codeActions);
				}
			}
		}
		return CompletableFuture.completedFuture(codeActions);
	}

	private void doCodeActionsForUndefinedVariable(Template template, Diagnostic diagnostic,
			List<CodeAction> codeActions) {
		try {
			String varName = null;
			boolean isIterable = false;
			JsonObject data = (JsonObject) diagnostic.getData();
			if (data != null) {
				varName = data.get(DIAGNOSTIC_DATA_NAME).getAsString();
				isIterable = data.get(DIAGNOSTIC_DATA_ITERABLE).getAsBoolean();
			} else {
				int offset = template.offsetAt(diagnostic.getRange().getStart());
				Node node = template.findNodeAt(offset);
				node = QutePositionUtility.findBestNode(offset, node);
				if (node.getKind() == NodeKind.Expression) {
					Expression expression = (Expression) node;
					ObjectPart part = expression.getObjectPart();
					if (part != null) {
						varName = part.getPartName();
					}
				}
			}

			if (varName != null) {
				TextDocument document = template.getTextDocument();
				String lineDelimiter = document.lineDelimiter(0);

				String title = MessageFormat.format(DECLARE_UNDEFINED_VARIABLE_MESSAGE, varName);

				Position position = new Position(0, 0);

				StringBuilder insertText = new StringBuilder("{@");
				if (isIterable) {
					insertText.append("java.util.List");
				} else {
					insertText.append("java.lang.String");
				}
				insertText.append(" ");
				insertText.append(varName);
				insertText.append("}");
				insertText.append(lineDelimiter);

				CodeAction insertParameterDeclaration = CodeActionFactory.insert(title, position, insertText.toString(),
						document, diagnostic);
				codeActions.add(insertParameterDeclaration);
			}

		} catch (BadLocationException e) {

		}

	}

}
