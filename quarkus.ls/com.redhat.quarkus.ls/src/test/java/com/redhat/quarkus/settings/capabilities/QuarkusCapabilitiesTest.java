/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.settings.capabilities;

import static com.redhat.quarkus.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static com.redhat.quarkus.settings.capabilities.ServerCapabilitiesConstants.COMPLETION_ID;
import static com.redhat.quarkus.settings.capabilities.ServerCapabilitiesConstants.HOVER_ID;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.Before;
import org.junit.Test;

/**
 * XMLCapabilityManagerTest
 */
public class QuarkusCapabilitiesTest {

	private LanguageClient languageClient = new LanguageClientMock();
	private QuarkusCapabilityManager manager;
	private ClientCapabilities clientCapabilities;
	private TextDocumentClientCapabilities textDocument;
	private WorkspaceClientCapabilities workspace;
	private Set<String> capabilityIDs;

	@Before
	public void startup() {

		textDocument = new TextDocumentClientCapabilities();
		workspace = new WorkspaceClientCapabilities();
		manager = new QuarkusCapabilityManager(languageClient);
		clientCapabilities = new ClientCapabilities();
		capabilityIDs = null;

	}

	@Test
	public void testAllDynamicCapabilities() {
		setAllCapabilities(true);
		setAndInitializeCapabilities();

		assertEquals(2, capabilityIDs.size());
		assertEquals(true, capabilityIDs.contains(COMPLETION_ID));
		assertEquals(true, capabilityIDs.contains(HOVER_ID));

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities());

		assertEquals(null, serverCapabilities.getCompletionProvider());
		assertEquals(false, serverCapabilities.getHoverProvider());
	}

	@Test
	public void testNoDynamicCapabilities() {
		setAllCapabilities(false);
		setAndInitializeCapabilities();

		assertEquals(0, capabilityIDs.size());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities());

		assertEquals(DEFAULT_COMPLETION_OPTIONS, serverCapabilities.getCompletionProvider());
		assertEquals(true, serverCapabilities.getHoverProvider());
	}

	@Test
	public void testBothCapabilityTypes() {

		// Dynamic capabilities
		CompletionCapabilities completion = new CompletionCapabilities();
		completion.setDynamicRegistration(true);
		textDocument.setCompletion(completion);

		// Non dynamic capabilities
		textDocument.setHover(new HoverCapabilities(false));

		setAndInitializeCapabilities();

		assertEquals(1, capabilityIDs.size());
		assertEquals(true, capabilityIDs.contains(COMPLETION_ID));
		assertEquals(false, capabilityIDs.contains(HOVER_ID));

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(manager.getClientCapabilities());

		assertEquals(true, serverCapabilities.getHoverProvider());
		assertEquals(null, serverCapabilities.getCompletionProvider());
	}

	private void setAllCapabilities(boolean areAllDynamic) {
		CompletionCapabilities completion = new CompletionCapabilities();
		completion.setDynamicRegistration(areAllDynamic);
		textDocument.setCompletion(completion);
		textDocument.setHover(new HoverCapabilities(areAllDynamic));
	}

	private void setAndInitializeCapabilities() {
		clientCapabilities.setTextDocument(textDocument);
		clientCapabilities.setWorkspace(workspace);
		manager.setClientCapabilities(clientCapabilities);
		manager.initializeCapabilities();
		capabilityIDs = manager.getRegisteredCapabilities();
	}

	class LanguageClientMock implements LanguageClient {
		@Override
		public void telemetryEvent(Object object) {
		}

		@Override
		public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
		}

		@Override
		public void showMessage(MessageParams messageParams) {
		}

		@Override
		public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
			return null;
		}

		@Override
		public void logMessage(MessageParams message) {
		}

		@Override
		public CompletableFuture<Void> registerCapability(RegistrationParams params) {
			return null;
		}
	}
}