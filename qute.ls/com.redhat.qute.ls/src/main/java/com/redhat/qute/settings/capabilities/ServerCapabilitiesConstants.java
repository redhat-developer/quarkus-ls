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
package com.redhat.qute.settings.capabilities;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DocumentLinkOptions;

/**
 * Server Capabilities Constants
 */
public class ServerCapabilitiesConstants {

	private ServerCapabilitiesConstants() {
	}

	/* textDocument/... */
	public static final String TEXT_DOCUMENT_CODE_ACTION = "textDocument/codeAction";
	public static final String TEXT_DOCUMENT_CODE_LENS = "textDocument/codeLens";
	public static final String TEXT_DOCUMENT_COMPLETION = "textDocument/completion";
	public static final String TEXT_DOCUMENT_DEFINITION = "textDocument/definition";
	public static final String TEXT_DOCUMENT_DOCUMENT_LINK = "textDocument/documentLink";
	public static final String TEXT_DOCUMENT_DOCUMENT_SYMBOL = "textDocument/documentSymbol";
	public static final String TEXT_DOCUMENT_FORMATTING = "textDocument/formatting";
	public static final String TEXT_DOCUMENT_HIGHLIGHT = "textDocument/documentHighlight";
	public static final String TEXT_DOCUMENT_HOVER = "textDocument/hover";
	public static final String TEXT_DOCUMENT_LINKED_EDITING_RANGE = "textDocument/linkedEditingRange";
	public static final String TEXT_DOCUMENT_RANGE_FORMATTING = "textDocument/rangeFormatting";
	public static final String TEXT_DOCUMENT_REFERENCES = "textDocument/references";
	public static final String TEXT_DOCUMENT_RENAME = "textDocument/rename";
	public static final String TEXT_DOCUMENT_INLAY_HINT = "textDocument/inlayHint";
	/* workspace/... */
	public static final String WORKSPACE_EXECUTE_COMMAND = "workspace/executeCommand";
	public static final String WORKSPACE_WATCHED_FILES = "workspace/didChangeWatchedFiles";

	/* UUID */
	public static final String CODE_ACTION_ID = UUID.randomUUID().toString();
	public static final String CODE_LENS_ID = UUID.randomUUID().toString();
	public static final String COMPLETION_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_DEFINITION_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_HIGHLIGHT_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_LINK_ID = UUID.randomUUID().toString();
	public static final String DOCUMENT_SYMBOL_ID = UUID.randomUUID().toString();
	public static final String FORMATTING_ID = UUID.randomUUID().toString();
	public static final String HOVER_ID = UUID.randomUUID().toString();
	public static final String LINKED_EDITING_RANGE_ID = UUID.randomUUID().toString();
	public static final String RANGE_FORMATTING_ID = UUID.randomUUID().toString();
	public static final String REFERENCES_ID = UUID.randomUUID().toString();
	public static final String RENAME_ID = UUID.randomUUID().toString();
	public static final String WORKSPACE_EXECUTE_COMMAND_ID = UUID.randomUUID().toString();
	public static final String WORKSPACE_WATCHED_FILES_ID = UUID.randomUUID().toString();
	public static final String INLAY_HINT_ID = UUID.randomUUID().toString();

	/* Default Options */
	public static final CodeLensOptions DEFAULT_CODELENS_OPTIONS = new CodeLensOptions();
	public static final CompletionOptions DEFAULT_COMPLETION_OPTIONS = new CompletionOptions(false,
			Arrays.asList("{", "@", "#", ".", ":", "$", "!"));
	public static final DocumentLinkOptions DEFAULT_DOCUMENT_LINK_OPTIONS = new DocumentLinkOptions(true);
	public static final CodeActionOptions DEFAULT_CODE_ACTION_OPTIONS = new CodeActionOptions();
	static {
		DEFAULT_CODE_ACTION_OPTIONS.setCodeActionKinds(Arrays.asList(CodeActionKind.QuickFix, CodeActionKind.Empty));
		DEFAULT_CODE_ACTION_OPTIONS.setResolveProvider(true);
	}

}