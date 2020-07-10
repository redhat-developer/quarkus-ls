/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;
import static org.eclipse.lsp4mp.utils.VersionHelper.getVersion;

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
import org.eclipse.lsp4mp.commons.MicroProfileJavaProjectLabelsParams;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;
import org.eclipse.lsp4mp.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4mp.ls.api.MicroProfileJavaProjectLabelsProvider;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageClientAPI;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageServerAPI;
import org.eclipse.lsp4mp.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import org.eclipse.lsp4mp.ls.commons.client.ExtendedClientCapabilities;
import org.eclipse.lsp4mp.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import org.eclipse.lsp4mp.services.MicroProfileLanguageService;
import org.eclipse.lsp4mp.settings.AllMicroProfileSettings;
import org.eclipse.lsp4mp.settings.InitializationOptionsSettings;
import org.eclipse.lsp4mp.settings.MicroProfileCodeLensSettings;
import org.eclipse.lsp4mp.settings.MicroProfileFormattingSettings;
import org.eclipse.lsp4mp.settings.MicroProfileGeneralClientSettings;
import org.eclipse.lsp4mp.settings.MicroProfileSymbolSettings;
import org.eclipse.lsp4mp.settings.MicroProfileValidationSettings;
import org.eclipse.lsp4mp.settings.capabilities.MicroProfileCapabilityManager;
import org.eclipse.lsp4mp.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * MicroProfile language server.
 *
 */
public class MicroProfileLanguageServer implements LanguageServer, ProcessLanguageServer, MicroProfileLanguageServerAPI,
		MicroProfileJavaProjectLabelsProvider {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileLanguageServer.class.getName());

	private final MicroProfileLanguageService microProfileLanguageService;
	private final MicroProfileTextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private Integer parentProcessId;
	private MicroProfileLanguageClientAPI languageClient;
	private MicroProfileCapabilityManager capabilityManager;

	public MicroProfileLanguageServer() {
		microProfileLanguageService = new MicroProfileLanguageService();
		textDocumentService = new MicroProfileTextDocumentService(this);
		workspaceService = new MicroProfileWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing MicroProfile server " + getVersion() + " with " + System.getProperty("java.home"));

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
	 * Update MicroProfile settings configured from the client.
	 * 
	 * @param initializationOptionsSettings the MicroProfile settings
	 */
	public synchronized void updateSettings(Object initializationOptionsSettings) {
		if (initializationOptionsSettings == null) {
			return;
		}
		// Update client settings
		initializationOptionsSettings = AllMicroProfileSettings.getMicroProfileToolsSettings(initializationOptionsSettings);
		MicroProfileGeneralClientSettings clientSettings = MicroProfileGeneralClientSettings
				.getGeneralMicroProfileSettings(initializationOptionsSettings);
		if (clientSettings != null) {
			MicroProfileSymbolSettings newSymbols = clientSettings.getSymbols();
			if (newSymbols != null) {
				textDocumentService.updateSymbolSettings(newSymbols);
			}
			MicroProfileValidationSettings newValidation = clientSettings.getValidation();
			if (newValidation != null) {
				textDocumentService.updateValidationSettings(newValidation);
			}
			MicroProfileFormattingSettings newFormatting = clientSettings.getFormatting();
			if (newFormatting != null) {
				textDocumentService.updateFormattingSettings(newFormatting);
			}
			MicroProfileCodeLensSettings newCodeLens = clientSettings.getCodeLens();
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

	public MicroProfileLanguageService getMicroProfileLanguageService() {
		return microProfileLanguageService;
	}

	@Override
	public void propertiesChanged(MicroProfilePropertiesChangeEvent event) {
		textDocumentService.propertiesChanged(event);
	}

	@Override
	public CompletableFuture<JsonSchemaForProjectInfo> getJsonSchemaForProjectInfo(
			MicroProfileProjectInfoParams params) {
		return textDocumentService.getJsonSchemaForProjectInfo(params);
	}

	@Override
	public CompletableFuture<ProjectLabelInfoEntry> getJavaProjectlabels(
			MicroProfileJavaProjectLabelsParams javaParams) {
		return getLanguageClient().getJavaProjectlabels(javaParams);
	}
}
