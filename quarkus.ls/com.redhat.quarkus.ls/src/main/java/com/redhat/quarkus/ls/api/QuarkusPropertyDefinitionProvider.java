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

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.quarkus.commons.QuarkusPropertyDefinitionParams;

/**
 * Quarkus property definition provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface QuarkusPropertyDefinitionProvider {

	@JsonRequest("quarkus/propertyDefinition")
	default CompletableFuture<Location> getPropertyDefinition(QuarkusPropertyDefinitionParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
