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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.microprofile.commons.MicroProfileJavaCodeLensParams;

/**
 * MicroProfile Java code lens provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfileJavaCodeLensProvider {

	@JsonRequest("microprofile/java/codeLens")
	default CompletableFuture<List<? extends CodeLens>> getJavaCodelens(MicroProfileJavaCodeLensParams javaParams) {
		return CompletableFuture.completedFuture(null);
	}

}
