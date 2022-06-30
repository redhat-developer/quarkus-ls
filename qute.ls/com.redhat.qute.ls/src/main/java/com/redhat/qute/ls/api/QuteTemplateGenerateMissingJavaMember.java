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
 * Request additional information from the associated java language server in
 * order to resolve the workspace edit to create a field, getter, or template
 * extension for a given type.
 *
 * @author datho7561
 */
public interface QuteTemplateGenerateMissingJavaMember {
	@JsonRequest("qute/template/generateMissingJavaMember")
	default CompletableFuture<WorkspaceEdit> generateMissingJavaMember(GenerateMissingJavaMemberParams params) {
		return CompletableFuture.completedFuture(null);
	}
}
