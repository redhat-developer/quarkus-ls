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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;

/**
 * Quarkus project information cache.
 * 
 * @author Angelo ZERR
 *
 */
class QuarkusProjectInfoCache {

	private final Map<String /* application.properties URI */, QuarkusProjectInfoWrapper> cache;

	private final QuarkusProjectInfoProvider provider;

	private static class QuarkusProjectInfoWrapper extends QuarkusProjectInfo {

		private static final String JAR_EXTENSION = ".jar";

		private boolean reloadFromSource;

		public QuarkusProjectInfoWrapper(QuarkusProjectInfo delegate) {
			super.setProjectURI(delegate.getProjectURI());
			super.setProperties(new CopyOnWriteArrayList<>(delegate.getProperties()));
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
		synchronized void update(List<ExtendedConfigDescriptionBuildItem> propertiesFromJavaSource) {
			// remove old properties from Java sources
			List<ExtendedConfigDescriptionBuildItem> oldPropertiesFromJavaSource = getProperties().stream()
					.filter(p -> {
						return p == null || !p.getLocation().endsWith(JAR_EXTENSION);
					}).collect(Collectors.toList());
			getProperties().removeAll(oldPropertiesFromJavaSource);
			// add new properties from Java sources
			getProperties().addAll(propertiesFromJavaSource);
			setReloadFromSource(false);
		}

		private boolean isReloadFromSource() {
			return reloadFromSource;
		}

		private void setReloadFromSource(boolean reloadFromSource) {
			this.reloadFromSource = reloadFromSource;
		}
	}

	public QuarkusProjectInfoCache(QuarkusProjectInfoProvider provider) {
		this.provider = provider;
		this.cache = new ConcurrentHashMap<>();
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
		QuarkusProjectInfoWrapper projectInfo = cache.get(params.getUri());
		if (projectInfo == null) {
			// not found in cache, load the project info from the JDT LS Extension
			params.setScope(QuarkusPropertiesScope.classpath);
			return provider.getQuarkusProjectInfo(params).thenApply(info ->
			// information was loaded, update the cache
			{
				cache.put(params.getUri(), new QuarkusProjectInfoWrapper(info));
				return info;
			});
		}
		if (projectInfo.isReloadFromSource()) {
			// There are some java sources changed, get the Quarkus properties from java
			// sources.
			params.setScope(QuarkusPropertiesScope.sources);
			return provider.getQuarkusProjectInfo(params).thenApply(info ->
			// then update the cache with the new properties
			{
				projectInfo.update(info.getProperties());
				return projectInfo;
			});
		}
		// Returns the cached project info
		return CompletableFuture.completedFuture(projectInfo);
	}

	public Collection<String> quarkusPropertiesChanged(QuarkusPropertiesChangeEvent event) {
		if (QuarkusPropertiesScope.sources == event.getType()) {
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
			QuarkusProjectInfoWrapper info = cache.get(uri);
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
