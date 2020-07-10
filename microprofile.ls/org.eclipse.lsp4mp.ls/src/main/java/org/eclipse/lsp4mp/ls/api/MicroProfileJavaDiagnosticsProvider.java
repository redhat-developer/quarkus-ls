/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.MicroProfileJavaDiagnosticsParams;

/**
 * MicroProfile Java diagnostics provider.
 *
 */
public interface MicroProfileJavaDiagnosticsProvider {

	@JsonRequest("microprofile/java/diagnostics")
	default CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(MicroProfileJavaDiagnosticsParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}