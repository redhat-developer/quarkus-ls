/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.launch.LSPLauncher.Builder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import com.redhat.quarkus.ls.api.QuarkusLanguageClientAPI;
import com.redhat.quarkus.ls.commons.ParentProcessWatcher;

/**
 * Quarkus server launcher
 *
 */
public class QuarkusServerLauncher {
	public static void main(String[] args) {
		QuarkusLanguageServer server = new QuarkusLanguageServer();
		Function<MessageConsumer, MessageConsumer> wrapper;
		wrapper = it -> it;
		if ("false".equals(System.getProperty("watchParentProcess"))) {
			wrapper = it -> it;
		} else {
			wrapper = new ParentProcessWatcher(server);
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
		return new Builder<LanguageClient>().setLocalService(server).setRemoteInterface(QuarkusLanguageClientAPI.class) // Set
																														// client
																														// as
																														// Quarkus
																														// language
																														// client
				.setInput(in).setOutput(out).setExecutorService(executorService).wrapMessages(wrapper).create();
	}
}
