/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.ArrayList;
import java.util.List;

import com.redhat.quarkus.ls.commons.CodeActionFactory;
import com.redhat.quarkus.ls.commons.TextDocument;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

public class QuarkusCodeActions {

	public List<CodeAction> doCodeActions(CodeActionContext context, Range range, TextDocument document) {
		List<CodeAction> codeActions = new ArrayList<>();

		for (Diagnostic diagnostic: context.getDiagnostics()) {
			switch (diagnostic.getCode()) {
				case "missing_equals_after_key": {
					codeActions.add(codeAction_missing_equals_after_key(diagnostic, range, document));
					break;
				}
			}
		}
		return codeActions;
	}

	private CodeAction codeAction_missing_equals_after_key(Diagnostic diagnostic, Range range, TextDocument textDocument) {
		// TODO equals sign should be placed with respect to the user's formatting settings
		return CodeActionFactory.insert("Add '=' after key", range.getEnd(), " = ", textDocument, diagnostic);
	}
}