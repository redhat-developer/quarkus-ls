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

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Qute language client API.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuteLanguageClientAPI extends LanguageClient, QuteJavaTypesProvider, QuteResolvedJavaTypeProvider,
		QuteJavaDefinitionProvider, QuteProjectInfoProvider, QuteDataModelProjectProvider, QuteUserTagProvider,
		QuteJavaCodeLensProvider, QuteJavaDiagnosticsProvider, QuteJavaDocumentLinkProvider {

	// TODO : remove this method when LSP4J will provide InlayHint support. See
	// https://github.com/eclipse/lsp4j/issues/570
	@JsonRequest("workspace/inlayHint/refresh")
	default CompletableFuture<Void> refreshInlayHints() {
		throw new UnsupportedOperationException();
	}
}