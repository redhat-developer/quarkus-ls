/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
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

import java.util.UUID;

import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Progress context.
 */
public class ProgressContext {

	private final ProgressSupport progressSupport;

	private String progressId;
	private int currentPercentage;

	public ProgressContext(ProgressSupport progressSupport) {
		this.progressSupport = progressSupport;
		this.currentPercentage = 0;
	}

	public String getProgressId() {
		return progressId;
	}

	public ProgressSupport getProgressSupport() {
		return progressSupport;
	}

	public void startProgress(String title, String message) {
		this.progressId = UUID.randomUUID().toString();

		// Initialize progress
		WorkDoneProgressCreateParams create = new WorkDoneProgressCreateParams(Either.forLeft(progressId));
		progressSupport.createProgress(create);

		// Start progress
		WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
		begin.setTitle(title);
		begin.setMessage(message);
		begin.setPercentage(100);
		this.progressSupport.notifyProgress(progressId, begin);
	}

	public void report(String message, int increment) {
		currentPercentage+=increment;
		WorkDoneProgressReport report = new WorkDoneProgressReport();
		report.setMessage(message);
		report.setPercentage(currentPercentage);
		progressSupport.notifyProgress(progressId, report);
	}
	
	public void endProgress() {
		WorkDoneProgressEnd end = new WorkDoneProgressEnd();
		progressSupport.notifyProgress(progressId, end);
	}

}
