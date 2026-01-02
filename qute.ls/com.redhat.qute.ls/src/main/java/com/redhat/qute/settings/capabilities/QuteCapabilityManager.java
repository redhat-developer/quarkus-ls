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
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DEFAULT_DOCUMENT_LINK_OPTIONS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_DEFINITION_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_HIGHLIGHT_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_LINK_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.DOCUMENT_SYMBOL_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.HOVER_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.INLAY_HINT_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.LINKED_EDITING_RANGE_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.REFERENCES_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.RENAME_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_ACTION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_CODE_LENS;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_COMPLETION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DEFINITION;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DOCUMENT_LINK;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_DOCUMENT_SYMBOL;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HIGHLIGHT;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_HOVER;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_INLAY_HINT;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_LINKED_EDITING_RANGE;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_REFERENCES;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.TEXT_DOCUMENT_RENAME;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_EXECUTE_COMMAND;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_EXECUTE_COMMAND_ID;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_WATCHED_FILES;
import static com.redhat.qute.settings.capabilities.ServerCapabilitiesConstants.WORKSPACE_WATCHED_FILES_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionRegistrationOptions;
import org.eclipse.lsp4j.CompletionRegistrationOptions;
import org.eclipse.lsp4j.DefinitionRegistrationOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.DocumentHighlightRegistrationOptions;
import org.eclipse.lsp4j.DocumentSymbolRegistrationOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.HoverRegistrationOptions;
import org.eclipse.lsp4j.InlayHintRegistrationOptions;
import org.eclipse.lsp4j.LinkedEditingRangeRegistrationOptions;
import org.eclipse.lsp4j.ReferenceRegistrationOptions;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.TextDocumentRegistrationOptions;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import com.redhat.qute.QuteLanguageIds;
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
			// Code action is only available for Qute templates
			CodeActionRegistrationOptions options = new CodeActionRegistrationOptions(
					Arrays.asList(CodeActionKind.QuickFix, CodeActionKind.Empty));
			options.setResolveProvider(true);
			registerCapability(CODE_ACTION_ID, TEXT_DOCUMENT_CODE_ACTION, options, QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isCodeLensDynamicRegistered()) {
			// Code Lens is available for Qute templates and Java both
			registerCapability(CODE_LENS_ID, TEXT_DOCUMENT_CODE_LENS, DEFAULT_CODELENS_OPTIONS);
		}
		if (this.getClientCapabilities().isCompletionDynamicRegistrationSupported()) {
			// Completion is only available for Qute templates
			CompletionRegistrationOptions options = new CompletionRegistrationOptions(
					Arrays.asList("{", "@", "#", ".", ":", "$", "!"), false);
			registerCapability(COMPLETION_ID, TEXT_DOCUMENT_COMPLETION, options, QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isDefinitionDynamicRegistered()) {
			// Definition is only available for Qute templates
			registerCapability(DOCUMENT_DEFINITION_ID, TEXT_DOCUMENT_DEFINITION, new DefinitionRegistrationOptions(),
					QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isDocumentHighlightDynamicRegistered()) {
			// Document highlight is only available for Qute templates
			registerCapability(DOCUMENT_HIGHLIGHT_ID, TEXT_DOCUMENT_HIGHLIGHT,
					new DocumentHighlightRegistrationOptions(), QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isDocumentLinkDynamicRegistered()) {
			// Document link is available for Qute templates and Java both
			registerCapability(DOCUMENT_LINK_ID, TEXT_DOCUMENT_DOCUMENT_LINK, DEFAULT_DOCUMENT_LINK_OPTIONS);
		}
		if (this.getClientCapabilities().isDocumentSymbolDynamicRegistrationSupported()) {
			// Document symbol is only available for Qute templates
			registerCapability(DOCUMENT_SYMBOL_ID, TEXT_DOCUMENT_DOCUMENT_SYMBOL,
					new DocumentSymbolRegistrationOptions(), QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isHoverDynamicRegistered()) {
			// Hover is only available for Qute templates
			registerCapability(HOVER_ID, TEXT_DOCUMENT_HOVER, new HoverRegistrationOptions(), QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isLinkedEditingRangeDynamicRegistered()) {
			// Linked editing range is only available for Qute templates
			registerCapability(LINKED_EDITING_RANGE_ID, TEXT_DOCUMENT_LINKED_EDITING_RANGE,
					new LinkedEditingRangeRegistrationOptions(), QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isReferencesDynamicRegistrationSupported()) {
			// References is only available for Qute templates
			registerCapability(REFERENCES_ID, TEXT_DOCUMENT_REFERENCES, new ReferenceRegistrationOptions(),
					QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isRenameDynamicRegistered()) {
			// Rename is only available for Qute templates
			registerCapability(RENAME_ID, TEXT_DOCUMENT_RENAME, new RenameOptions(), QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isInlayHintDynamicRegistered()) {
			// Inlay Hint is only available for Qute templates
			registerCapability(INLAY_HINT_ID, TEXT_DOCUMENT_INLAY_HINT, new InlayHintRegistrationOptions(),
					QuteLanguageIds.QUTE_ALL);
		}
		if (this.getClientCapabilities().isDidChangeWatchedFilesRegistered()) {
			registerWatchedFiles();
		}
	}

	private void registerWatchedFiles() {
		List<FileSystemWatcher> watchers = new ArrayList<>(5);
		// Qute templates
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.html")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.json")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.yaml")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.yml")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/*.txt")));
		// Renarde
		watchers.add(new FileSystemWatcher(Either.forLeft("**/messages.properties")));
		watchers.add(new FileSystemWatcher(Either.forLeft("**/messages*.properties")));
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

	private void registerCapability(String id, String method, Object options, String... languageIds) {
		if (registeredCapabilities.add(id)) {
			if (languageIds != null && languageIds.length > 0) {
				List<DocumentFilter> documentSelector = new ArrayList<>();
				((TextDocumentRegistrationOptions) options).setDocumentSelector(documentSelector);
				for (String languageId : languageIds) {
					documentSelector.add(new DocumentFilter(languageId, null, null));
				}
			}
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