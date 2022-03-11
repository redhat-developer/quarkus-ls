/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.lsp4j.proposed;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

public interface QuteInlayHintProvider {

	/**
	 * The inlay hints request is sent from the client to the server to compute
	 * inlay hints for a given [text document, range] tuple that may be rendered in
	 * the editor in place with other text.
	 * <p>
	 * Since 3.17.0
	 */
	@JsonRequest("textDocument/inlayHint")
	default CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		throw new UnsupportedOperationException();
	}

	/**
	 * The request is sent from the client to the server to resolve additional
	 * information for a given inlay hint. This is usually used to compute the
	 * {@code tooltip}, {@code location} or {@code command} properties of an inlay
	 * hint's label part to avoid its unnecessary computation during the
	 * {@code textDocument/inlayHint} request.
	 * <p>
	 * Since 3.17.0
	 */
	@JsonRequest(value = "inlayHint/resolve", useSegment = false)
	default CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved) {
		throw new UnsupportedOperationException();
	}

}
