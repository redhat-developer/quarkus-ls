/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import static com.redhat.microprofile.services.MicroProfileAssert.assertCompletions;
import static com.redhat.microprofile.services.MicroProfileAssert.c;
import static com.redhat.microprofile.services.MicroProfileAssert.r;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.Assert;
import org.junit.Test;

import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.api.MicroProfileLanguageClientAPI;

/**
 * Test with change usecase of classpath and java sources changed.
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileLanguageServerScopeChangedTest {

	private static final String PROJECT1 = "project1";
	private static final String PROJECT1_APPLICATION_PROPERTIES = PROJECT1 + "/application.properties";

	private static final ItemMetadata property1FromJar;
	private static final ItemMetadata property2FromJar;
	private static final ItemMetadata property1FromSources;
	private static final ItemMetadata property2FromSources;
	private static final ItemMetadata dynamicProperty1FromSources;

	private static final ItemMetadata dynamicProperty2FromSources;
	private static final ItemHint itemHintFromSources;

	static {
		property1FromJar = new ItemMetadata();
		property1FromJar.setName("quarkus.application.name");

		property2FromJar = new ItemMetadata();
		property2FromJar.setName("quarkus.application.version");

		property1FromSources = new ItemMetadata();
		property1FromSources.setName("greeting.message");
		property1FromSources.setSource(Boolean.TRUE);

		property2FromSources = new ItemMetadata();
		property2FromSources.setName("greeting.suffix");
		property2FromSources.setSource(Boolean.TRUE);

		dynamicProperty1FromSources = new ItemMetadata();
		dynamicProperty1FromSources.setName("${mp.register.rest.client.class}/mp-rest/url");
		dynamicProperty1FromSources.setSource(Boolean.TRUE);

		dynamicProperty2FromSources = new ItemMetadata();
		dynamicProperty2FromSources.setName("${mp.register.rest.client.class}/mp-rest/scope");
		dynamicProperty2FromSources.setSource(Boolean.TRUE);

		itemHintFromSources = new ItemHint();
		itemHintFromSources.setName("${mp.register.rest.client.class}");
		itemHintFromSources.setValues(new ArrayList<>());
		ValueHint value = new ValueHint();
		value.setValue("org.acme.restclient.CountriesService");
		itemHintFromSources.getValues().add(value);
		value = new ValueHint();
		value.setValue("configKey");
		itemHintFromSources.getValues().add(value);
	}

	@Test
	public void classpathChanged() throws InterruptedException, ExecutionException {
		MicroProfileLanguageServer server = createServer();
		MockMicroProfileLanguageClient client = (MockMicroProfileLanguageClient) server.getLanguageClient();
		// Initialize quarkus properties
		client.changedClasspath(PROJECT1, property1FromJar, property2FromJar, property1FromSources);

		didOpen(PROJECT1_APPLICATION_PROPERTIES, server);
		CompletionList list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 3, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("quarkus.application.version", "quarkus.application.version=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of classpath (Jar and Java sources)
		client.changedClasspath(PROJECT1, property1FromJar, property1FromSources);
		list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of Java sources (add)
		client.changedJavaSources(PROJECT1, property2FromSources);
		list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.suffix", "greeting.suffix=", r(0, 0, 0)));

		// Emulate change of Java sources with dynamic properties
		client.changedJavaSources(PROJECT1, dynamicProperty1FromSources, dynamicProperty2FromSources,
				itemHintFromSources);
		list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 1 /* (from JAR ) */ + 4 /* from sources */,
				c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("org.acme.restclient.CountriesService/mp-rest/url",
						"org.acme.restclient.CountriesService/mp-rest/url=", r(0, 0, 0)),
				c("org.acme.restclient.CountriesService/mp-rest/scope",
						"org.acme.restclient.CountriesService/mp-rest/scope=", r(0, 0, 0)),
				c("configKey/mp-rest/url", "configKey/mp-rest/url=", r(0, 0, 0)),
				c("configKey/mp-rest/scope", "configKey/mp-rest/scope=", r(0, 0, 0)));
	}

	@Test
	public void javaSourcesChangedInThreadContext() throws InterruptedException, ExecutionException {
		MicroProfileLanguageServer server = createServer();
		MockMicroProfileLanguageClient client = (MockMicroProfileLanguageClient) server.getLanguageClient();
		// Initialize quarkus properties
		client.changedClasspath(PROJECT1, property1FromJar, property2FromJar, property1FromSources);

		didOpen(PROJECT1_APPLICATION_PROPERTIES, server);
		CompletionList list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 3, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("quarkus.application.version", "quarkus.application.version=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// Emulate change of classpath (Jar and Java sources)
		client.changedClasspath(PROJECT1, property1FromJar, property1FromSources);
		list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
		assertCompletions(list, 2, c("quarkus.application.name", "quarkus.application.name=", r(0, 0, 0)), //
				c("greeting.message", "greeting.message=", r(0, 0, 0)));

		// create a lot of thread which change java sources (update Quarkus properties)
		// and execute completion (to recompute Quarkus properties)
		List<Thread> threads = new ArrayList<>();
		List<Integer> count = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			Thread t1 = createChangedJavaSourcesThread(server, client);
			threads.add(t1);
			Thread t2 = createCompletionThread(server, client, count);
			threads.add(t2);
			t1.start();
			t2.start();
		}

		for (Thread thread : threads) {
			thread.join();
		}
		Integer max = count.stream().max(Math::max).get();
		Assert.assertTrue(max <= 2);
	}

	private Thread createCompletionThread(MicroProfileLanguageServer server, MockMicroProfileLanguageClient client,
			List<Integer> count) {
		return new Thread(() -> {
			try {
				CompletionList list = completion(PROJECT1_APPLICATION_PROPERTIES, server);
				synchronized (count) {
					count.add(list.getItems().size());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private Thread createChangedJavaSourcesThread(MicroProfileLanguageServer server,
			MockMicroProfileLanguageClient client) {
		return new Thread(() -> {
			try {
				// Emulate change of Java sources (add)
				client.changedJavaSources(PROJECT1, property2FromSources);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static MicroProfileLanguageServer createServer() {
		MicroProfileLanguageServer languageServer = new MicroProfileLanguageServer();
		MicroProfileLanguageClientAPI languageClient = new MockMicroProfileLanguageClient(languageServer);
		languageServer.setClient(languageClient);
		return languageServer;
	}

	private void didOpen(String uri, MicroProfileLanguageServer server) {
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
		params.setTextDocument(new TextDocumentItem(uri, "", 1, ""));
		server.getTextDocumentService().didOpen(params);
	}

	private static CompletionList completion(String uri, MicroProfileLanguageServer server)
			throws InterruptedException, ExecutionException {
		CompletionParams params = new CompletionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setPosition(new Position(0, 0));
		return server.getTextDocumentService().completion(params).get().getRight();
	}

}
