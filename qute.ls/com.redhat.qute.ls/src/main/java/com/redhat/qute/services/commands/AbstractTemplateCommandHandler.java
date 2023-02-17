/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.commands;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.ls.api.QuteTemplateProvider;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.settings.SharedSettings;

/**
 * Abstract command to work with a given {@link Template} filled in the first
 * argument of the command as {@link TextDocumentIdentifier}.
 * 
 * @author Angelo ZERR - copied from Lemminx
 *
 */
public abstract class AbstractTemplateCommandHandler implements IDelegateCommandHandler {

	private final QuteTemplateProvider templateProvider;

	public AbstractTemplateCommandHandler(QuteTemplateProvider templateProvider) {
		this.templateProvider = templateProvider;
	}

	@Override
	public final CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception {
		TextDocumentIdentifier identifier = ArgumentsUtils.getArgAt(params, 0, TextDocumentIdentifier.class);
		String uri = identifier.getUri();
		Template template = templateProvider.getTemplate(uri);
		if (template == null) {
			throw new UnsupportedOperationException(String
					.format("Command '%s' cannot find the Qute template with the URI '%s'.", params.getCommand(), uri));
		}
		return executeCommand(template, params, sharedSettings, cancelChecker);
	}

	/**
	 * Executes a command
	 * 
	 * @param template       the Qute template retrieve by the
	 *                       {@link TextDocumentIdentifier} argument.
	 * 
	 * @param params         command execution parameters
	 * @param sharedSettings the shared settings
	 * @param cancelChecker  check if cancel has been requested
	 * @return the result of the command
	 * @throws Exception the unhandled exception will be wrapped in
	 *                   <code>org.eclipse.lsp4j.jsonrpc.ResponseErrorException</code>
	 *                   and be wired back to the JSON-RPC protocol caller
	 */
	protected abstract CompletableFuture<Object> executeCommand(Template document, ExecuteCommandParams params,
			SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception;

}