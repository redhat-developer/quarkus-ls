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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.UnregistrationParams;

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
}