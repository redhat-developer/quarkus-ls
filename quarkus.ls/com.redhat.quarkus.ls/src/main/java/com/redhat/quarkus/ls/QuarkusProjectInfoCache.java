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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfoParams;

/**
 * Quarkus project information cache.
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusProjectInfoCache {

	private final Map<String /* Document URI */, QuarkusProjectInfo> cache;

	private final QuarkusProjectInfoProvider provider;

	public QuarkusProjectInfoCache(QuarkusProjectInfoProvider provider) {
		this.provider = provider;
		this.cache = new HashMap<>();
	}

	/**
	 * Returns as promise the Quarkus project information for the given
	 * application.properties URI.
	 * 
	 * @param params the URI of the application.properties.
	 * @return as promise the Quarkus project information for the given
	 *         application.properties URI.
	 */
	public CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(QuarkusProjectInfoParams params) {
		// Search project info in cache
		QuarkusProjectInfo projectInfo = cache.get(params.getUri());
		if (projectInfo == null) {
			// not found in cache, load the project info from the JDT LS Extension
			return provider.getQuarkusProjectInfo(params).thenApplyAsync(info ->
			// information was loaded, update the cache
			{
				cache.put(params.getUri(), info);
				return info;
			});
		}
		// Returns the cached project info
		return CompletableFuture.completedFuture(projectInfo);
	}

	public List<String> classpathChanged(Set<String> projects) {
		List<String> uris = cache.entrySet().stream()
				.filter(entry -> projects.contains(entry.getValue().getProjectURI())).map(Map.Entry::getKey)
				.collect(Collectors.toList());
		uris.forEach(cache::remove);
		return uris;
	}

}
