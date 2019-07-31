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

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Quarkus workspace service.
 *
 */
public class QuarkusWorkspaceService implements WorkspaceService {

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {

	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {

	}

}
