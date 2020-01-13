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

import static com.redhat.microprofile.utils.VersionHelper.getVersion;
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

import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.ls.api.MicroProfileLanguageClientAPI;
import com.redhat.microprofile.ls.api.MicroProfileLanguageServerAPI;
import com.redhat.microprofile.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.redhat.microprofile.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.microprofile.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import com.redhat.microprofile.services.MicroProfileLanguageService;
import com.redhat.microprofile.settings.AllMicroProfileSettings;
import com.redhat.microprofile.settings.InitializationOptionsSettings;
import com.redhat.microprofile.settings.MicroProfileCodeLensSettings;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.settings.MicroProfileGeneralClientSettings;
import com.redhat.microprofile.settings.MicroProfileSymbolSettings;
import com.redhat.microprofile.settings.MicroProfileValidationSettings;
import com.redhat.microprofile.settings.capabilities.MicroProfileCapabilityManager;
import com.redhat.microprofile.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Quarkus language server.
 *
 */
public class MicroProfileLanguageServer implements LanguageServer, ProcessLanguageServer, MicroProfileLanguageServerAPI {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileLanguageServer.class.getName());

	private final MicroProfileLanguageService quarkusLanguageService;
	private final MicroProfileTextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private Integer parentProcessId;
	private MicroProfileLanguageClientAPI languageClient;
	private MicroProfileCapabilityManager capabilityManager;

	public MicroProfileLanguageServer() {
		quarkusLanguageService = new MicroProfileLanguageService();
		textDocumentService = new MicroProfileTextDocumentService(this);
		workspaceService = new MicroProfileWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing Quarkus server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
		updateSettings(InitializationOptionsSettings.getSettings(params));

		textDocumentService.updateClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
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
		initializationOptionsSettings = AllMicroProfileSettings.getQuarkusToolsSettings(initializationOptionsSettings);
		MicroProfileGeneralClientSettings quarkusClientSettings = MicroProfileGeneralClientSettings
				.getGeneralQuarkusSettings(initializationOptionsSettings);
		if (quarkusClientSettings != null) {
			MicroProfileSymbolSettings newSymbols = quarkusClientSettings.getSymbols();
			if (newSymbols != null) {
				textDocumentService.updateSymbolSettings(newSymbols);
			}
			MicroProfileValidationSettings newValidation = quarkusClientSettings.getValidation();
			if (newValidation != null) {
				textDocumentService.updateValidationSettings(newValidation);
			}
			MicroProfileFormattingSettings newFormatting = quarkusClientSettings.getFormatting();
			if (newFormatting != null) {
				textDocumentService.updateFormattingSettings(newFormatting);
			}
			MicroProfileCodeLensSettings newCodeLens = quarkusClientSettings.getCodeLens();
			if (newCodeLens != null) {
				textDocumentService.updateCodeLensSettings(newCodeLens);
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

	public MicroProfileLanguageClientAPI getLanguageClient() {
		return languageClient;
	}

	public MicroProfileCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (MicroProfileLanguageClientAPI) languageClient;
		this.capabilityManager = new MicroProfileCapabilityManager(languageClient);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	public MicroProfileLanguageService getQuarkusLanguageService() {
		return quarkusLanguageService;
	}

	@Override
	public void propertiesChanged(MicroProfilePropertiesChangeEvent event) {
		textDocumentService.propertiesChanged(event);
	}

	@Override
	public CompletableFuture<JsonSchemaForProjectInfo> getJsonSchemaForProjectInfo(MicroProfileProjectInfoParams params) {
		return textDocumentService.getJsonSchemaForProjectInfo(params);
	}

}
