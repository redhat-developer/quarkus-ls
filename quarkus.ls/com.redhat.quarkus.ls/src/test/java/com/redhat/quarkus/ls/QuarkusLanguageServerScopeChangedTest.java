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

import static com.redhat.quarkus.services.QuarkusAssert.assertCompletions;
import static com.redhat.quarkus.services.QuarkusAssert.c;
import static com.redhat.quarkus.services.QuarkusAssert.r;

import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.Test;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.ls.api.QuarkusLanguageClientAPI;

/**
 * Test with change usecase of classpath and java sources changed.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusLanguageServerScopeChangedTest {

	private static final String PROJECT1 = "project1";
	private static final String PROJECT1_APPLICATION_PROPERTIES = PROJECT1 + "/application.properties";

	private static final ExtendedConfigDescriptionBuildItem property1FromJar;
	private static final ExtendedConfigDescriptionBuildItem property2FromJar;
	private static final ExtendedConfigDescriptionBuildItem property1FromSources;
	private static final ExtendedConfigDescriptionBuildItem property2FromSources;

	static {
		property1FromJar = new ExtendedConfigDescriptionBuildItem();
		property1FromJar.setPropertyName("quarkus.application.name");
		property1FromJar.setLocation("quarkus-core-deployment-0.21.1.jar");

		property2FromJar = new ExtendedConfigDescriptionBuildItem();
		property2FromJar.setPropertyName("quarkus.application.version");
		property2FromJar.setLocation("quarkus-core-deployment-0.21.1.jar");

		property1FromSources = new ExtendedConfigDescriptionBuildItem();
		property1FromSources.setPropertyName("greeting.message");
		property1FromSources.setLocation("src/main/java");

		property2FromSources = new ExtendedConfigDescriptionBuildItem();
		property2FromSources.setPropertyName("greeting.suffix");
		property2FromSources.setLocation("src/main/java");
	}

	@Test
	public void classpathChanged() throws InterruptedException, ExecutionException {
		QuarkusLanguageServer server = createServer();
		MockQuarkusLanguageClient client = (MockQuarkusLanguageClient) server.getLanguageClient();
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
	}

	private static QuarkusLanguageServer createServer() {
		QuarkusLanguageServer languageServer = new QuarkusLanguageServer();
		QuarkusLanguageClientAPI languageClient = new MockQuarkusLanguageClient(languageServer);
		languageServer.setClient(languageClient);
		return languageServer;
	}

	private void didOpen(String uri, QuarkusLanguageServer server) {
		DidOpenTextDocumentParams params = new DidOpenTextDocumentParams();
		params.setTextDocument(new TextDocumentItem(uri, "", 1, ""));
		server.getTextDocumentService().didOpen(params);
	}

	private static CompletionList completion(String uri, QuarkusLanguageServer server)
			throws InterruptedException, ExecutionException {
		CompletionParams params = new CompletionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setPosition(new Position(0, 0));
		return server.getTextDocumentService().completion(params).get().getRight();
	}

}
