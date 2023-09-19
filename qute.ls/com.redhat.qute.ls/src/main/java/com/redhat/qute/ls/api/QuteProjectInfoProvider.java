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
package com.redhat.qute.ls.api;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteProjectParams;

/**
 * Qute project information provider.
 *
 * @author Angelo ZERR
 *
 */
public interface QuteProjectInfoProvider {

	/**
	 * Returns all Qute projects from the workspace.
	 * 
	 * @return all Qute projects from the workspace.
	 */
	@JsonRequest("qute/template/projects")
	CompletableFuture<Collection<ProjectInfo>> getProjects();

	/**
	 * Notification received when a Qute project is added in the workspace.
	 * 
	 * @param project the Qute project which is added in the workspace.
	 */
	@JsonNotification("qute/template/project/added")
	void projectAdded(ProjectInfo project);

	/**
	 * Notification received when a Qute project is closed / removed from the
	 * workspace.
	 * 
	 * @param project the Qute project which is closed / removed from the workspace.
	 */
	@JsonNotification("qute/template/project/removed")
	void projectRemoved(ProjectInfo project);

	/**
	 * Returns the Qute project from the given template file uri parameter.
	 * 
	 * @param params the template file uri parameter.
	 * @return
	 */
	@JsonRequest("qute/template/project")
	CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params);

}
