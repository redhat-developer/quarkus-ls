/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.qute.commons.GenerateMissingJavaMemberParams;

/**
 * Provides information from the associated java language server in order to
 * resolve code actions.
 *
 * @author datho7561
 */
public interface QuteTemplateJavaTextEditProvider {

	/**
	 * Returns the workspace edit to create a field, getter, or template extension
	 * for a given type.
	 * 
	 * @param params the parameters indicating which field, getter, or template
	 *               extension to create
	 * @return the workspace edit to create a field, getter, or template extension
	 *         for a given type
	 */
	@JsonRequest("qute/template/generateMissingJavaMember")
	CompletableFuture<WorkspaceEdit> generateMissingJavaMember(GenerateMissingJavaMemberParams params);
}
