/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.ls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.api.MicroProfileProjectInfoProvider;

/**
 * MicroProfile project information cache.
 * 
 * @author Angelo ZERR
 *
 */
class MicroProfileProjectInfoCache {

	private final Map<String /* application.properties URI */, MicroProfileProjectInfoWrapper> cache;

	private final MicroProfileProjectInfoProvider provider;

	private static class MicroProfileProjectInfoWrapper extends MicroProfileProjectInfo {

		private boolean reloadFromSource;

		public MicroProfileProjectInfoWrapper(MicroProfileProjectInfo delegate) {
			super.setProjectURI(delegate.getProjectURI());
			super.setProperties(new CopyOnWriteArrayList<>(
					delegate.getProperties() != null ? delegate.getProperties() : new ArrayList<>()));
			super.setHints(
					new CopyOnWriteArrayList<>(delegate.getHints() != null ? delegate.getHints() : new ArrayList<>()));
			this.reloadFromSource = false;
		}

		/**
		 * Clear the cache only for Quarkus properties coming from java sources.
		 */
		public void clearPropertiesFromSource() {
			setReloadFromSource(true);
		}

		/**
		 * Add the new quarkus properties in the cache coming java sources.
		 * 
		 * @param propertiesFromJavaSource properties to add in the cache.
		 */
		synchronized void update(List<ItemMetadata> propertiesFromJavaSource, List<ItemHint> hintsFromJavaSource) {
			// remove old properties from Java sources
			if (propertiesFromJavaSource != null) {
				List<ItemMetadata> oldPropertiesFromJavaSource = getProperties().stream().filter(p -> {
					return p == null || !p.isBinary();
				}).collect(Collectors.toList());
				getProperties().removeAll(oldPropertiesFromJavaSource);
				// add new properties from Java sources
				getProperties().addAll(propertiesFromJavaSource);
			}
			// remove old hints from Java sources
			if (hintsFromJavaSource != null) {
				List<ItemHint> oldHintsFromJavaSource = getHints().stream().filter(h -> {
					return h == null || !h.isBinary();
				}).collect(Collectors.toList());
				getHints().removeAll(oldHintsFromJavaSource);
				// add new properties from Java sources
				getHints().addAll(hintsFromJavaSource);
			}
			setReloadFromSource(false);
		}

		private boolean isReloadFromSource() {
			return reloadFromSource;
		}

		private void setReloadFromSource(boolean reloadFromSource) {
			this.reloadFromSource = reloadFromSource;
		}
	}

	public MicroProfileProjectInfoCache(MicroProfileProjectInfoProvider provider) {
		this.provider = provider;
		this.cache = new ConcurrentHashMap<>();
	}

	/**
	 * Returns as promise the MicroProfile project information for the given
	 * application.properties URI.
	 * 
	 * @param params the URI of the application.properties.
	 * @return as promise the MicroProfile project information for the given
	 *         application.properties URI.
	 */
	public CompletableFuture<MicroProfileProjectInfo> getMicroProfileProjectInfo(MicroProfileProjectInfoParams params) {
		// Search project info in cache
		MicroProfileProjectInfoWrapper projectInfo = cache.get(params.getUri());
		if (projectInfo == null) {
			// not found in cache, load the project info from the JDT LS Extension
			params.setScopes(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
			return provider.getProjectInfo(params).thenApply(info ->
			// information was loaded, update the cache
			{
				cache.put(params.getUri(), new MicroProfileProjectInfoWrapper(info));
				return info;
			});
		}
		if (projectInfo.isReloadFromSource()) {
			// There are some java sources changed, get the Quarkus properties from java
			// sources.
			params.setScopes(MicroProfilePropertiesScope.ONLY_SOURCES);
			return provider.getProjectInfo(params).thenApply(info ->
			// then update the cache with the new properties
			{
				projectInfo.update(info.getProperties(), info.getHints());
				return projectInfo;
			});
		}
		// Returns the cached project info
		return CompletableFuture.completedFuture(projectInfo);
	}

	public Collection<String> microprofilePropertiesChanged(MicroProfilePropertiesChangeEvent event) {
		List<MicroProfilePropertiesScope> scopes = event.getType();
		boolean changedOnlyInSources = scopes.size() == 1 && scopes.get(0) == MicroProfilePropertiesScope.sources;
		if (changedOnlyInSources) {
			return javaSourceChanged(event.getProjectURIs());
		}
		return classpathChanged(event.getProjectURIs());
	}

	private Collection<String> classpathChanged(Set<String> projectURIs) {
		List<String> applicationPropertiesURIs = getApplicationPropertiesURIs(projectURIs);
		applicationPropertiesURIs.forEach(cache::remove);
		return applicationPropertiesURIs;
	}

	private Collection<String> javaSourceChanged(Set<String> projectURIs) {
		List<String> applicationPropertiesURIs = getApplicationPropertiesURIs(projectURIs);
		for (String uri : applicationPropertiesURIs) {
			MicroProfileProjectInfoWrapper info = cache.get(uri);
			info.clearPropertiesFromSource();
		}
		return applicationPropertiesURIs;
	}

	/**
	 * Returns the application.propeties URIs which belongs to the given project
	 * URIs.
	 * 
	 * @param projectURIs project URIs
	 * 
	 * @return the application.propeties URIs which belongs to the given project
	 *         URIs.
	 */
	private List<String> getApplicationPropertiesURIs(Set<String> projectURIs) {
		return cache.entrySet().stream().filter(entry -> projectURIs.contains(entry.getValue().getProjectURI()))
				.map(Map.Entry::getKey).collect(Collectors.toList());
	}

}
