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

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import com.redhat.quarkus.commons.QuarkusProjectInfo;

/**
 * Quarkus project information cache.
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusProjectInfoCache extends HashMap<String /* Document URI */, QuarkusProjectInfo> {

	private static final long serialVersionUID = 1L;

	private final QuarkusProjectInfoProvider provider;

	public QuarkusProjectInfoCache(QuarkusProjectInfoProvider provider) {
		this.provider = provider;
	}

	/**
	 * Returns as promise the Quarkus project information for the given
	 * application.properties URI.
	 * 
	 * @param documentURI the URI of the application.properties.
	 * @return as promise the Quarkus project information for the given
	 *         application.properties URI.
	 */
	public CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(String documentURI) {
		// Search project info in cache
		QuarkusProjectInfo projectInfo = super.get(documentURI);
		if (projectInfo == null) {
			// not found in cache, load the project info from the JDT LS Extension
			return provider.getQuarkusProjectInfo(documentURI).thenApplyAsync(info ->
			// information was loaded, update the cache
			QuarkusProjectInfoCache.this.put(documentURI, info));
		}
		// Returns the cached project info
		return CompletableFuture.completedFuture(projectInfo);
	}

}
