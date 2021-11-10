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
package com.redhat.qute.services.commands;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.qute.settings.SharedSettings;

/**
 * Command handler to register with the workspace service
 */
@FunctionalInterface
public interface IDelegateCommandHandler {

	/**
	 * Executes a command
	 * 
	 * @param params         command execution parameters
	 * @param sharedSettings the shared settings.
	 * @param cancelChecker  check if cancel has been requested
	 * @return the result of the command
	 * @throws Exception the unhandled exception will be wrapped in
	 *                   <code>org.eclipse.lsp4j.jsonrpc.ResponseErrorException</code>
	 *                   and be wired back to the JSON-RPC protocol caller
	 */
	CompletableFuture<Object> executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings,
			CancelChecker cancelChecker) throws Exception;
}