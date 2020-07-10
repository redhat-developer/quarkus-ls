/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.api;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;

/**
 * MicroProfile property definition provider.
 * 
 * @author Angelo ZERR
 *
 */
public interface MicroProfilePropertyDefinitionProvider {

	@JsonRequest("microprofile/propertyDefinition")
	default CompletableFuture<Location> getPropertyDefinition(MicroProfilePropertyDefinitionParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
