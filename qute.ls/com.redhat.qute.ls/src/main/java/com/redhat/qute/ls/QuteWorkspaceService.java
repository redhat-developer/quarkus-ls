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
package com.redhat.qute.ls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures.FutureCancelChecker;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.redhat.qute.services.commands.IDelegateCommandHandler;
import com.redhat.qute.services.commands.QuteGenerateCommandHandler;
import com.redhat.qute.services.commands.QuteGenerateTemplateContentCommandHandler;

/**
 * Qute workspace service.
 *
 */
public class QuteWorkspaceService implements WorkspaceService {

	private final Map<String, IDelegateCommandHandler> commands;

	private final QuteLanguageServer quteLanguageServer;

	public QuteWorkspaceService(QuteLanguageServer quteLanguageServer) {
		this.quteLanguageServer = quteLanguageServer;
		this.commands = registerCommands();
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		quteLanguageServer.updateSettings(params.getSettings());
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		synchronized (commands) {
			IDelegateCommandHandler handler = commands.get(params.getCommand());
			if (handler == null) {
				throw new ResponseErrorException(new ResponseError(ResponseErrorCode.InternalError,
						"No command handler for the command: " + params.getCommand(), null));
			}
			return computeAsync(cancelChecker -> {
				try {
					return handler.executeCommand(params, quteLanguageServer.getSharedSettings(), cancelChecker);
				} catch (Exception e) {
					if (e instanceof ResponseErrorException) {
						throw (ResponseErrorException) e;
					} else if (e instanceof CancellationException) {
						throw (CancellationException) e;
					}
					throw new ResponseErrorException(
							new ResponseError(ResponseErrorCode.UnknownErrorCode, e.getMessage(), e));
				}
			});
		}
	}

	private Map<String, IDelegateCommandHandler> registerCommands() {
		Map<String, IDelegateCommandHandler> commands = new HashMap<>();
		commands.put(QuteGenerateCommandHandler.COMMAND_ID, new QuteGenerateCommandHandler());
		commands.put(QuteGenerateTemplateContentCommandHandler.COMMAND_ID,
				new QuteGenerateTemplateContentCommandHandler(quteLanguageServer.getDataModelCache()));
		return commands;
	}

	public List<String> getCommandIds() {
		return new ArrayList<>(commands.keySet());
	}

	public static <R> CompletableFuture<R> computeAsync(Function<CancelChecker, CompletableFuture<R>> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenComposeAsync(cancelChecker -> code.apply(cancelChecker));
		start.complete(new FutureCancelChecker(result));
		return result;
	}

}
