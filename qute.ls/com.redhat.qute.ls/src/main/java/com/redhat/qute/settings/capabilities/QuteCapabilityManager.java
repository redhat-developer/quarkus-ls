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

import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.CODE_ACTION_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.CODE_LENS_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.COMPLETION_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_CODELENS_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_COMPLETION_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_DOCUMENT_LINK_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_DEFINITION_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_HIGHLIGHT_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_LINK_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_SYMBOL_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.HOVER_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.INLAY_HINT_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.LINKED_EDITING_RANGE_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.REFERENCES_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_ACTION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_LENS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_COMPLETION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DEFINITION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DOCUMENT_SYMBOL;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HIGHLIGHT;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HOVER;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_INLAY_HINT;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_LINK;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_LINKED_EDITING_RANGE;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_REFERENCES;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_EXECUTE_COMMAND;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_EXECUTE_COMMAND_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_WATCHED_FILES;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_WATCHED_FILES_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import com.redhat.qute.ls.commons.client.ExtendedClientCapabilities;

/**
 * Manages dynamic capabilities
 */
public class QuteCapabilityManager {

	private final Set<String> registeredCapabilities = new HashSet<>(3);
	private final LanguageClient languageClient;

	private ClientCapabilitiesWrapper clientWrapper;

	public QuteCapabilityManager(LanguageClient languageClient) {
		this.languageClient = languageClient;
	}

	/**
	 * Registers all dynamic capabilities that the server does not support client
	 * side preferences turning on/off
	 */
	public void initializeCapabilities() {
		if (this.getClientCapabilities().isCodeActionDynamicRegistered()) {
			registerCapability(CODE_ACTION_ID, TEXT_DOCUMENT_CODE_ACTION);
		}
		if (this.getClientCapabilities().isDocumentHighlightDynamicRegistered()) {
			registerCapability(DOCUMENT_HIGHLIGHT_ID, TEXT_DOCUMENT_HIGHLIGHT);
		}
		if (this.getClientCapabilities().isDefinitionDynamicRegistered()) {
			registerCapability(DOCUMENT_DEFINITION_ID, TEXT_DOCUMENT_DEFINITION);
		}
		if (this.getClientCapabilities().isDocumentLinkDynamicRegistered()) {
			registerCapability(DOCUMENT_DEFINITION_ID, TEXT_DOCUMENT_DEFINITION);
		}
		if (this.getClientCapabilities().isDocumentSymbolDynamicRegistrationSupported()) {
			registerCapability(DOCUMENT_SYMBOL_ID, TEXT_DOCUMENT_DOCUMENT_SYMBOL);
		}
		if (this.getClientCapabilities().isCompletionDynamicRegistrationSupported()) {
			registerCapability(COMPLETION_ID, TEXT_DOCUMENT_COMPLETION, DEFAULT_COMPLETION_OPTIONS);
		}
		if (this.getClientCapabilities().isCodeLensDynamicRegistered()) {
			registerCapability(CODE_LENS_ID, TEXT_DOCUMENT_CODE_LENS, DEFAULT_CODELENS_OPTIONS);
		}
		if (this.getClientCapabilities().isHoverDynamicRegistered()) {
			registerCapability(HOVER_ID, TEXT_DOCUMENT_HOVER);
		}
		if (this.getClientCapabilities().isDocumentLinkDynamicRegistered()) {
			registerCapability(DOCUMENT_LINK_ID, TEXT_DOCUMENT_LINK, DEFAULT_DOCUMENT_LINK_OPTIONS);
		}
		if (this.getClientCapabilities().isReferencesDynamicRegistrationSupported()) {
			registerCapability(REFERENCES_ID, TEXT_DOCUMENT_REFERENCES);
		}
		if (this.getClientCapabilities().isLinkedEditingRangeDynamicRegistered()) {
			registerCapability(LINKED_EDITING_RANGE_ID, TEXT_DOCUMENT_LINKED_EDITING_RANGE);
		}
		if (this.getClientCapabilities().isInlayHintDynamicRegistered()) {
			registerCapability(INLAY_HINT_ID, TEXT_DOCUMENT_INLAY_HINT);
		}
		if (this.getClientCapabilities().isDidChangeWatchedFilesRegistered()) {
			registerWatchedFiles();
		}
	}

	private void registerWatchedFiles() {
		List<FileSystemWatcher> watchers = new ArrayList<>(2);
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.html")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.qute.html")));
		DidChangeWatchedFilesRegistrationOptions options = new DidChangeWatchedFilesRegistrationOptions(watchers);
		registerCapability(WORKSPACE_WATCHED_FILES_ID, WORKSPACE_WATCHED_FILES, options);
	}

	public void setClientCapabilities(ClientCapabilities clientCapabilities,
			ExtendedClientCapabilities extendedClientCapabilities) {
		this.clientWrapper = new ClientCapabilitiesWrapper(clientCapabilities, extendedClientCapabilities);
	}

	public ClientCapabilitiesWrapper getClientCapabilities() {
		if (this.clientWrapper == null) {
			this.clientWrapper = new ClientCapabilitiesWrapper();
		}
		return this.clientWrapper;
	}

	public Set<String> getRegisteredCapabilities() {
		return registeredCapabilities;
	}

	private void registerCapability(String id, String method) {
		registerCapability(id, method, null);
	}

	private void registerCapability(String id, String method, Object options) {
		if (registeredCapabilities.add(id)) {
			Registration registration = new Registration(id, method, options);
			RegistrationParams registrationParams = new RegistrationParams(Collections.singletonList(registration));
			languageClient.registerCapability(registrationParams);
		}
	}

	public void registerExecuteCommand(List<String> commands) {
		registerCapability(WORKSPACE_EXECUTE_COMMAND_ID, WORKSPACE_EXECUTE_COMMAND,
				new ExecuteCommandOptions(commands));
	}

}