/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4mp.ls.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Factory for simple {@link CodeAction}
 *
 */
public class CodeActionFactory {

	/**
	 * Create a CodeAction to remove the content from the given range.
	 * 
	 * @param title
	 * @param range
	 * @param document
	 * @param diagnostic
	 * @return
	 */
	public static CodeAction remove(String title, Range range, TextDocumentItem document, Diagnostic diagnostic) {
		return replace(title, range, "", document, diagnostic);
	}

	/**
	 * Create a CodeAction to insert a new content at the end of the given range.
	 * 
	 * @param title
	 * @param range
	 * @param insertText
	 * @param document
	 * @param diagnostic
	 * @return
	 */
	public static CodeAction insert(String title, Position position, String insertText, TextDocumentItem document,
			Diagnostic diagnostic) {
		return insert(title, position, insertText, document, Arrays.asList(diagnostic));
	}

	/**
	 * Create a CodeAction to insert a new content at the end of the given range.
	 * 
	 * @param title
	 * @param range
	 * @param insertText
	 * @param document
	 * @param diagnostics
	 * @return
	 */
	public static CodeAction insert(String title, Position position, String insertText, TextDocumentItem document,
			List<Diagnostic> diagnostics) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(diagnostics);
		TextEdit edit = new TextEdit(new Range(position, position), insertText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Collections.singletonList(edit));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}

	public static CodeAction replace(String title, Range range, String replaceText, TextDocumentItem document,
			Diagnostic diagnostic) {
		CodeAction insertContentAction = new CodeAction(title);
		insertContentAction.setKind(CodeActionKind.QuickFix);
		insertContentAction.setDiagnostics(Arrays.asList(diagnostic));
		TextEdit edit = new TextEdit(range, replaceText);
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
				document.getUri(), document.getVersion());

		TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier, Collections.singletonList(edit));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));

		insertContentAction.setEdit(workspaceEdit);
		return insertContentAction;
	}
	
	public static boolean isDiagnosticCode(Either<String, Number> diagnosticCode, String code) {
		if (diagnosticCode == null || diagnosticCode.isRight()) {
			return false;
		}
		return code.equals(diagnosticCode.getLeft());
	}
}
