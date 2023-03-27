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
package com.redhat.qute.ls;

import static com.redhat.qute.utils.VersionHelper.getVersion;
import static org.eclipse.lsp4j.jsonrpc.CompletableFutures.computeAsync;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SetTraceParams;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressNotification;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteJavadocProvider;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;
import com.redhat.qute.ls.api.QuteLanguageServerAPI;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.ls.api.QuteTemplateProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.ls.commons.ParentProcessWatcher.ProcessLanguageServer;
import com.redhat.qute.ls.commons.client.ExtendedClientCapabilities;
import com.redhat.qute.ls.commons.client.InitializationOptionsExtendedClientCapabilities;
import com.redhat.qute.ls.template.TemplateFileTextDocumentService;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.ProgressSupport;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.settings.AllQuteSettings;
import com.redhat.qute.settings.InitializationOptionsSettings;
import com.redhat.qute.settings.QuteGeneralClientSettings;
import com.redhat.qute.settings.QuteGeneralClientSettings.SettingsUpdateState;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.settings.capabilities.QuteCapabilityManager;
import com.redhat.qute.settings.capabilities.ServerCapabilitiesInitializer;

/**
 * Qute language server.
 *
 */
public class QuteLanguageServer implements LanguageServer, ProcessLanguageServer, QuteLanguageServerAPI,
		QuteProjectInfoProvider, QuteJavaTypesProvider, QuteResolvedJavaTypeProvider, QuteJavaDefinitionProvider,
		QuteDataModelProjectProvider, QuteUserTagProvider, QuteTemplateJavaTextEditProvider, QuteJavadocProvider,
		QuteTemplateProvider, TemplateValidator, ProgressSupport {

	private static final Logger LOGGER = Logger.getLogger(QuteLanguageServer.class.getName());

	private final SharedSettings sharedSettings;

	private final QuteProjectRegistry projectRegistry;

	private final QuteLanguageService quteLanguageService;

	private final QuteTextDocumentService textDocumentService;

	private final QuteWorkspaceService workspaceService;

	private Integer parentProcessId;

	private QuteLanguageClientAPI languageClient;

	private QuteCapabilityManager capabilityManager;
	private List<WorkspaceFolder> workspaceFolders;

	public QuteLanguageServer() {
		this.sharedSettings = new SharedSettings();
		this.projectRegistry = new QuteProjectRegistry(this, this, this, this, this, this, this, this);
		this.quteLanguageService = new QuteLanguageService(projectRegistry);
		this.textDocumentService = new QuteTextDocumentService(this);
		this.workspaceService = new QuteWorkspaceService(this);
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		LOGGER.info("Initializing Qute server " + getVersion() + " with " + System.getProperty("java.home"));

		this.parentProcessId = params.getProcessId();

		ExtendedClientCapabilities extendedClientCapabilities = InitializationOptionsExtendedClientCapabilities
				.getExtendedClientCapabilities(params);
		capabilityManager.setClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
		updateSettings(InitializationOptionsSettings.getSettings(params));

		textDocumentService.updateClientCapabilities(params.getCapabilities(), extendedClientCapabilities);
		ServerCapabilities serverCapabilities = ServerCapabilitiesInitializer
				.getNonDynamicServerCapabilities(capabilityManager.getClientCapabilities());

		projectRegistry.setDidChangeWatchedFilesSupported(
				capabilityManager.getClientCapabilities().isDidChangeWatchedFilesRegistered());
		workspaceFolders = params.getWorkspaceFolders();

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
		getCapabilityManager().registerExecuteCommand(getWorkspaceService().getCommandIds());
		// The Qute language server is initialized, try to load a Qute project per
		// workspace folder.
		loadQuteProjects();
	}

	/**
	 * Try to load the Qute project for each workspace folder.
	 */
	private void loadQuteProjects() {
		if (workspaceFolders == null || workspaceFolders.isEmpty()) {
			// No workspace folders.
			return;
		}
		CompletableFuture.runAsync(() -> {
			for (WorkspaceFolder workspaceFolder : workspaceFolders) {
				// Get the LSP client progress support
				ProgressSupport progressSupport = capabilityManager.getClientCapabilities()
						.isWorkDoneProgressSupported() ? this : null;
				// Try to load the Qute project of the current workspace folder
				projectRegistry.tryToLoadQuteProject(workspaceFolder, progressSupport);
			}
		});
	}

	/**
	 * Update Qute settings configured from the client.
	 *
	 * @param initializationOptionsSettings the Qute settings
	 */
	public synchronized void updateSettings(Object initializationOptionsSettings) {
		if (initializationOptionsSettings == null) {
			return;
		}
		// Update client settings
		initializationOptionsSettings = AllQuteSettings.getQuteSettings(initializationOptionsSettings);
		QuteGeneralClientSettings clientSettings = QuteGeneralClientSettings
				.getGeneralQuteSettings(initializationOptionsSettings);
		if (clientSettings != null) {

			// Update shared settings from the new client settings
			SettingsUpdateState result = QuteGeneralClientSettings.update(getSharedSettings(), clientSettings);
			if (result.isCodeLensSettingsChanged()) {
				// Some codelens settings changed, ask the client to refresh all code lenses.
				getLanguageClient().refreshCodeLenses();
			}
			if (result.isInlayHintSettingsChanged()) {
				// Some inlay hint settings changed, ask the client to refresh all inlay hints.
				getLanguageClient().refreshInlayHints();
			}
			if (result.isValidationSettingsChanged() || result.isNativeImagesSettingsChanged()) {
				// Some validation settings changed
				textDocumentService.validationSettingsChanged();
			}
		}
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		if (capabilityManager.getClientCapabilities().shouldLanguageServerExitOnShutdown()) {
			ScheduledExecutorService delayer = Executors.newScheduledThreadPool(1);
			delayer.schedule(() -> exit(0), 1, TimeUnit.SECONDS);
		}
		textDocumentService.dispose();
		projectRegistry.dispose();
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

	@Override
	public TextDocumentService getTextDocumentService() {
		return this.textDocumentService;
	}

	@Override
	public QuteWorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public QuteLanguageClientAPI getLanguageClient() {
		return languageClient;
	}

	public QuteCapabilityManager getCapabilityManager() {
		return capabilityManager;
	}

	public void setClient(LanguageClient languageClient) {
		this.languageClient = (QuteLanguageClientAPI) languageClient;
		this.capabilityManager = new QuteCapabilityManager(languageClient);
	}

	@Override
	public long getParentProcessId() {
		return parentProcessId != null ? parentProcessId : 0;
	}

	public QuteLanguageService getQuteLanguageService() {
		return quteLanguageService;
	}

	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}

	@Override
	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return getLanguageClient().getJavaTypes(params);
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return getLanguageClient().getJavaDefinition(params);
	}

	@Override
	public CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return getLanguageClient().getResolvedJavaType(params);
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return getLanguageClient().getProjectInfo(params);
	}

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return getLanguageClient().getJavadoc(params);
	}

	@Override
	public void dataModelChanged(JavaDataModelChangeEvent event) {
		projectRegistry.dataModelChanged(event);
		textDocumentService.dataModelChanged(event);
	}

	public QuteProjectRegistry getProjectRegistry() {
		return projectRegistry;
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return getLanguageClient().getDataModelProject(params);
	}

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return getLanguageClient().getUserTags(params);
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		textDocumentService.didChangeWatchedFiles(params);
	}

	@Override
	public void setTrace(SetTraceParams params) {
		// to avoid having error in vscode, the method is implemented
		// FIXME : implement the behavior of this method.
	}

	@Override
	public Template getTemplate(String uri) {
		return ((TemplateFileTextDocumentService) textDocumentService.getTextDocumentService(uri)).getTemplate(uri);
	}

	@Override
	public void triggerValidationFor(QuteTextDocument document) {
		textDocumentService.triggerValidationFor(document);
	}

	@Override
	public void clearDiagnosticsFor(String fileUri) {
		textDocumentService.clearDiagnosticsFor(fileUri);
	}

	@Override
	public void triggerValidationFor(Collection<QuteProject> projects) {
		textDocumentService.triggerValidationFor(projects);
	}

	@Override
	public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
		return getLanguageClient().createProgress(params);
	}

	@Override
	public void notifyProgress(String progressId, WorkDoneProgressNotification notification) {
		ProgressParams params = new ProgressParams(Either.forLeft(progressId), Either.forRight(notification));
		getLanguageClient().notifyProgress(params);
	}

}
