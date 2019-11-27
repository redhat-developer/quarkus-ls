/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls.api;

import java.util.concurrent.CompletableFuture;

import com.redhat.quarkus.commons.QuarkusJavaHoverInfo;
import com.redhat.quarkus.commons.QuarkusJavaHoverParams;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

/**
 * Quarkus Java hover provider.
 *
 */
public interface QuarkusJavaHoverProvider {

	@JsonRequest("quarkus/java/hover")
	default CompletableFuture<QuarkusJavaHoverInfo> quarkusJavaHover(QuarkusJavaHoverParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}