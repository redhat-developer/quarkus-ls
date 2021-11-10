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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.launch.LSPLauncher.Builder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.ls.commons.ParentProcessWatcher;

/**
 * Qute server launcher
 *
 */
public class QuteServerLauncher {

	/**
	 * Main entry point for the server. System properties may influence the
	 * behavior:
	 * <ul>
	 * <i>watchParentProcess</i>: if defined and value is false then do not watch
	 * for the parent process otherwise if parent process is dead then stop this
	 * server.
	 * </ul>
	 * <ul>
	 * <i>runAsync</i>: if defined and value is true then received message are
	 * processed in a separate thread than the LSP4J thread.
	 * </ul>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		QuteLanguageServer server = new QuteLanguageServer();
		Function<MessageConsumer, MessageConsumer> wrapper;
		wrapper = it -> it;
		if ("true".equals(System.getProperty("runAsync"))) {
			wrapper = it -> msg -> CompletableFuture.runAsync(() -> it.consume(msg));
		}
		if (!"false".equals(System.getProperty("watchParentProcess"))) {
			wrapper = new ParentProcessWatcher(server, wrapper);
		}
		Launcher<LanguageClient> launcher = createServerLauncher(server, System.in, System.out,
				Executors.newCachedThreadPool(), wrapper);

		server.setClient(launcher.getRemoteProxy());
		launcher.startListening();
	}

	/**
	 * Create a new Launcher for a language server and an input and output stream.
	 * Threads are started with the given executor service. The wrapper function is
	 * applied to the incoming and outgoing message streams so additional message
	 * handling such as validation and tracing can be included.
	 * 
	 * @param server          - the server that receives method calls from the
	 *                        remote client
	 * @param in              - input stream to listen for incoming messages
	 * @param out             - output stream to send outgoing messages
	 * @param executorService - the executor service used to start threads
	 * @param wrapper         - a function for plugging in additional message
	 *                        consumers
	 */
	public static Launcher<LanguageClient> createServerLauncher(LanguageServer server, InputStream in, OutputStream out,
			ExecutorService executorService, Function<MessageConsumer, MessageConsumer> wrapper) {
		return new Builder<LanguageClient>().setLocalService(server).setRemoteInterface(QuteLanguageClientAPI.class) // Set
																														// client
																														// as
																														// Quarkus
																														// language
																														// client
				.setInput(in).setOutput(out).setExecutorService(executorService).wrapMessages(wrapper).create();
	}
}
