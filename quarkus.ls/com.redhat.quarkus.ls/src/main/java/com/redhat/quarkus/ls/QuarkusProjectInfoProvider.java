/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.ls;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

import com.redhat.quarkus.commons.QuarkusProjectInfo;

/**
 * Quarkus project information provider..
 * 
 * @author Angelo ZERR
 *
 */
public interface QuarkusProjectInfoProvider {

	/**
	 * Returns the Quarkus project information for the given
	 * <code>documentURI</code>.
	 * 
	 * @param documentURI the document URI
	 * @return the Quarkus project information for the given
	 *         <code>documentURI</code>.
	 */
	@JsonRequest("quarkus/projectInfo")
	CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(String documentURI);
}
