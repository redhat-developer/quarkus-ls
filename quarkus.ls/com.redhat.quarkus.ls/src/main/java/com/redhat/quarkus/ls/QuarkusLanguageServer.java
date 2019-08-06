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

import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;
import static com.redhat.quarkus.utils.VersionHelper.getVersion;

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

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.redhat.quarkus.services.QuarkusLanguageService;
import com.redhat.quarkus.settings.capabilities.QuarkusCapabilityManager;
import com.redhat.quarkus.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Quarkus language server.
 *
 */
public class QuarkusLanguageServer implements LanguageServer, ProcessLanguageServer, QuarkusProjectInfoProvider {

	private static final Logger LOGGER = Logger.getLogger(QuarkusLanguageServer.class.getName());

	private final QuarkusLanguageService quarkusLanguageService;
	private final QuarkusTextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	private Integer parentProcessId;
	private QuarkusLanguageClient languageClient;
	private QuarkusCapabilityManager capabilityManager;

	public QuarkusLanguageServer() {
		quarkusLanguageService = new QuarkusLanguageService();
		textDocumentService = new QuarkusTextDocumentService(this);
		workspaceService = new QuarkusWorkspaceService();
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing Quarkus server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		capabilityManager.setClientCapabilities(params.getCapabilities());

		textDocumentService.updateClientCapabilities(params.getCapabilities());

		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer.getNonDynamicServerCapabilities(
			capabilityManager.getClientCapabilities());
		
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

	public CompletableFuture<Object> shutdown() {
		return computeAsync(cc -> new Object());
	}

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

	public QuarkusLanguageClient getLanguageClient() {
		return languageClient;
	}

	public QuarkusCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (QuarkusLanguageClient) languageClient;
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
	public CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(QuarkusProjectInfoParams params) {
		return languageClient.getQuarkusProjectInfo(params);
	}

}
