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

import static com.redhat.quarkus.utils.VersionHelper.getVersion;
import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;
import com.redhat.quarkus.ls.api.QuarkusLanguageClientAPI;
import com.redhat.quarkus.ls.api.QuarkusLanguageServerAPI;
import com.redhat.quarkus.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.redhat.quarkus.services.QuarkusLanguageService;
import com.redhat.quarkus.settings.AllQuarkusSettings;
import com.redhat.quarkus.settings.InitializationOptionsSettings;
import com.redhat.quarkus.settings.QuarkusGeneralClientSettings;
import com.redhat.quarkus.settings.QuarkusSymbolSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.settings.capabilities.QuarkusCapabilityManager;
import com.redhat.quarkus.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Quarkus language server.
 *
 */
public class QuarkusLanguageServer implements LanguageServer, ProcessLanguageServer, QuarkusLanguageServerAPI {

	private static final Logger LOGGER = Logger.getLogger(QuarkusLanguageServer.class.getName());

	private final QuarkusLanguageService quarkusLanguageService;
	private final QuarkusTextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private Integer parentProcessId;
	private QuarkusLanguageClientAPI languageClient;
	private QuarkusCapabilityManager capabilityManager;

	public QuarkusLanguageServer() {
		quarkusLanguageService = new QuarkusLanguageService();
		textDocumentService = new QuarkusTextDocumentService(this);
		workspaceService = new QuarkusWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing Quarkus server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		capabilityManager.setClientCapabilities(params.getCapabilities());
		updateSettings(InitializationOptionsSettings.getSettings(params));

		textDocumentService.updateClientCapabilities(params.getCapabilities());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(capabilityManager.getClientCapabilities());

		InitializeResult initializeResult = new InitializeResult(serverCapabilities);
		return CompletableFuture.completedFuture(initializeResult);
	}

	/*
	 * Registers all capabilities that do not support client side preferences to
	 * turn on/off
	 *
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.lsp4j.services.LanguageServer#initialized(org.eclipse.lsp4j.
	 * InitializedParams)
	 */
	@Override
	public void initialized(InitializedParams params) {
		capabilityManager.initializeCapabilities();
	}

	/**
	 * Update Quarkus settings configured from the client.
	 * 
	 * @param initializationOptionsSettings the Quarkus settings
	 */
	public synchronized void updateSettings(Object initializationOptionsSettings) {
		if (initializationOptionsSettings == null) {
			return;
		}
		// Update client settings
		initializationOptionsSettings = AllQuarkusSettings.getQuarkusToolsSettings(initializationOptionsSettings);
		QuarkusGeneralClientSettings quarkusClientSettings = QuarkusGeneralClientSettings
				.getGeneralQuarkusSettings(initializationOptionsSettings);
		if (quarkusClientSettings != null) {
			QuarkusSymbolSettings newSymbols = quarkusClientSettings.getSymbols();
			if (newSymbols != null) {
				textDocumentService.updateSymbolSettings(newSymbols);
			}
			QuarkusValidationSettings newValidation = quarkusClientSettings.getValidation();
			if (newValidation != null) {
				textDocumentService.updateValidationSettings(newValidation);
			}
		}
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return computeAsync(cc -> new Object());
	}

	@Override
	public void exit() {
		exit(0);
	}

	@Override
	public void exit(int exitCode) {
		System.exit(exitCode);
	}

	public TextDocumentService getTextDocumentService() {
		return this.textDocumentService;
	}

	public WorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public QuarkusLanguageClientAPI getLanguageClient() {
		return languageClient;
	}

	public QuarkusCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (QuarkusLanguageClientAPI) languageClient;
		this.capabilityManager = new QuarkusCapabilityManager(languageClient);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	public QuarkusLanguageService getQuarkusLanguageService() {
		return quarkusLanguageService;
	}

	@Override
	public void quarkusPropertiesChanged(QuarkusPropertiesChangeEvent event) {
		textDocumentService.quarkusPropertiesChanged(event);
	}

}
