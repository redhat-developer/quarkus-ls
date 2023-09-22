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
	 * Returns the Qute project from the given template file uri parameter.
	 * 
	 * @param params the template file uri parameter.
	 * @return
	 */
	@JsonRequest("qute/template/project")
	CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params);

}
