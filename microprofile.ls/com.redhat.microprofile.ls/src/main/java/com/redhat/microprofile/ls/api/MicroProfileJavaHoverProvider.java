/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.microprofile.commons.MicroProfileJavaHoverParams;

/**
 * MicroProfile Java hover provider.
 *
 */
public interface MicroProfileJavaHoverProvider {

	@JsonRequest("microprofile/java/hover")
	default CompletableFuture<Hover> getJavaHover(MicroProfileJavaHoverParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}