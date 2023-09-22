/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.WorkspaceEdit;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaCodeLensParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaDiagnosticsParams;
import com.redhat.qute.commons.QuteJavaDocumentLinkParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteLanguageClientAPI;

/**
 * Mock Qute Language client which helps to track show messages, actionable
 * notification and commands.
 *
 */
public class MockQuteLanguageClient implements QuteLanguageClientAPI {

	private final List<PublishDiagnosticsParams> publishDiagnostics;

	private final List<MessageParams> showMessages;

	private final List<MessageParams> logMessages;

	public MockQuteLanguageClient() {
		publishDiagnostics = new ArrayList<>();
		showMessages = new ArrayList<>();
		logMessages = new ArrayList<>();
	}

	@Override
	public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
		return null;
	}

	@Override
	public void showMessage(MessageParams messageParams) {
		showMessages.add(messageParams);
	}

	@Override
	public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
		publishDiagnostics.add(diagnostics);
	}

	@Override
	public void logMessage(MessageParams message) {
		logMessages.add(message);
	}

	@Override
	public void telemetryEvent(Object object) {

	}

	@Override
	public CompletableFuture<Void> registerCapability(RegistrationParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> unregisterCapability(UnregistrationParams params) {
		return CompletableFuture.completedFuture(null);
	}

	public List<PublishDiagnosticsParams> getPublishDiagnostics() {
		return publishDiagnostics;
	}

	public List<MessageParams> getLogMessages() {
		return logMessages;
	}

	public List<MessageParams> getShowMessages() {
		return showMessages;
	}

	@Override
	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> getJavaCodelens(QuteJavaCodeLensParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(QuteJavaDiagnosticsParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<DocumentLink>> getJavaDocumentLink(QuteJavaDocumentLinkParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> generateMissingJavaMember(GenerateMissingJavaMemberParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Collection<ProjectInfo>> getProjects() {
		return CompletableFuture.completedFuture(null);
	}

}