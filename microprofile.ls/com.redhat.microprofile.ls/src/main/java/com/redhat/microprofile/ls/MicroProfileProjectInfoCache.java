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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.metadata.ItemBase;
import com.redhat.microprofile.commons.metadata.ItemHint;
import com.redhat.microprofile.commons.metadata.ItemHint.ValueHint;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.api.MicroProfileProjectInfoProvider;

/**
 * MicroProfile project information cache.
 * 
 * @author Angelo ZERR
 *
 */
class MicroProfileProjectInfoCache {

	private static final Logger LOGGER = Logger.getLogger(MicroProfileProjectInfoCache.class.getName());

	private final Map<String /* application.properties URI */, CompletableFuture<MicroProfileProjectInfo>> cache;

	private final MicroProfileProjectInfoProvider provider;

	private static final MicroProfileProjectInfo EMPTY_PROJECT_INFO;
	
	static {
		EMPTY_PROJECT_INFO = new MicroProfileProjectInfo();
		EMPTY_PROJECT_INFO.setProperties(Collections.emptyList());
		EMPTY_PROJECT_INFO.setHints(Collections.emptyList());
		EMPTY_PROJECT_INFO.setProjectURI("");
	}

	/**
	 * Computed metadata build from dynamic properties and a given hint value.
	 *
	 */
	private static class ComputedItemMetadata extends ItemMetadata {

		/**
		 * Computed metadata constructor
		 * 
		 * @param metadata dynamic metadata name (ex : name =
		 *                 '${mp.register.rest.client.class}/mp-rest/url)').
		 * @param itemHint item hint which matches the dynamic metadata (ex : name =
		 *                 '${mp.register.rest.client.class}').
		 * @param value    the item value (ex : value =
		 *                 'org.acme.restclient.CountriesService').
		 */
		public ComputedItemMetadata(ItemMetadata metadata, ItemHint itemHint, ValueHint value) {
			// replace dynamic part from metedata name (ex:
			// '${mp.register.rest.client.class}/mp-rest/url'))
			// with hint value (ex: 'org.acme.restclient.CountriesService') to obtain
			// the new name 'org.acme.restclient.CountriesService/mp-rest/url'
			String name = metadata.getName().replace(itemHint.getName(), value.getValue());
			super.setName(name);
			super.setSource(Boolean.TRUE);
			super.setDescription(metadata.getDescription());
			super.setSourceType(value.getSourceType());
		}
	}

	private static class MicroProfileProjectInfoWrapper extends MicroProfileProjectInfo {

		private boolean reloadFromSource;

		private List<ItemMetadata> dynamicProperties;

		private final Function<String, ItemHint> getHint = hint -> getHint(hint);

		public MicroProfileProjectInfoWrapper(MicroProfileProjectInfo delegate) {
			super.setProjectURI(delegate.getProjectURI());
			// Update hints
			super.setHints(
					new CopyOnWriteArrayList<>(delegate.getHints() != null ? delegate.getHints() : new ArrayList<>()));
			// Get dynamic and static properties from delegate project info
			List<ItemMetadata> staticProperties = delegate.getProperties() != null ? delegate.getProperties()
					: new ArrayList<>();
			List<ItemMetadata> dynamicProperties = computeDynamicProperties(staticProperties);
			staticProperties.removeAll(dynamicProperties);
			expandProperties(staticProperties, dynamicProperties, getHint);

			// Update dynamic and static properties
			this.setDynamicProperties(new CopyOnWriteArrayList<ItemMetadata>(dynamicProperties));
			super.setProperties(new CopyOnWriteArrayList<>(staticProperties));
			this.reloadFromSource = false;
		}

		/**
		 * Clear the cache only for Quarkus properties coming from java sources.
		 */
		public void clearPropertiesFromSource() {
			setReloadFromSource(true);
		}

		private static List<ItemMetadata> computeDynamicProperties(List<ItemMetadata> properties) {
			return properties.stream().filter(p -> p != null && p.getName().contains("${"))
					.collect(Collectors.toList());
		}

		/**
		 * Add the new quarkus properties in the cache coming java sources.
		 * 
		 * @param propertiesFromJavaSource properties to add in the cache.
		 */
		synchronized void update(List<ItemMetadata> propertiesFromJavaSource, List<ItemHint> hintsFromJavaSource) {
			// remove old hints from Java sources
			if (hintsFromJavaSource != null) {
				updateListFromPropertiesSources(getHints(), hintsFromJavaSource);
			}
			// remove old properties from Java sources
			if (propertiesFromJavaSource != null) {
				List<ItemMetadata> staticProperties = propertiesFromJavaSource;
				List<ItemMetadata> dynamicProperties = computeDynamicProperties(staticProperties);
				staticProperties.removeAll(dynamicProperties);

				expandProperties(staticProperties, dynamicProperties, getHint);
				updateListFromPropertiesSources(getProperties(), staticProperties);
				updateListFromPropertiesSources(getDynamicProperties(), dynamicProperties);
			}
			setReloadFromSource(false);
		}

		private static <T extends ItemBase> void updateListFromPropertiesSources(List<T> allProperties,
				List<T> propertiesFromJavaSources) {
			List<? extends ItemBase> oldPropertiesFromJavaSources = allProperties.stream().filter(h -> {
				return h == null || !h.isBinary();
			}).collect(Collectors.toList());
			allProperties.removeAll(oldPropertiesFromJavaSources);
			// add new properties from Java sources
			allProperties.addAll(propertiesFromJavaSources);
		}

		private static void expandProperties(List<ItemMetadata> allProperties, List<ItemMetadata> dynamicProperties,
				Function<String, ItemHint> getHint) {
			for (ItemMetadata metadata : dynamicProperties) {
				int start = metadata.getName().indexOf("${");
				int end = metadata.getName().indexOf("}", start);
				String hint = metadata.getName().substring(start, end + 1);
				ItemHint itemHint = getHint.apply(hint);
				if (itemHint != null) {
					for (ValueHint value : itemHint.getValues()) {
						allProperties.add(new ComputedItemMetadata(metadata, itemHint, value));
					}
				}
			}
		}

		private boolean isReloadFromSource() {
			return reloadFromSource;
		}

		private void setReloadFromSource(boolean reloadFromSource) {
			this.reloadFromSource = reloadFromSource;
		}

		private List<ItemMetadata> getDynamicProperties() {
			return dynamicProperties;
		}

		void setDynamicProperties(List<ItemMetadata> dynamicProperties) {
			this.dynamicProperties = dynamicProperties;
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
	public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
		// Search future which load project info in cache
		CompletableFuture<MicroProfileProjectInfo> projectInfo = cache.get(params.getUri());
		if (projectInfo == null || projectInfo.isCancelled() || projectInfo.isCompletedExceptionally()) {
			// not found in the cache, load the project info from the JDT LS Extension
			params.setScopes(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
			CompletableFuture<MicroProfileProjectInfo> future = provider.getProjectInfo(params). //
					exceptionally(ex-> {
						LOGGER.warning(String.format("Cannot find MicroProfileProjectInfo for '%s'", params.getUri()));
						return new MicroProfileProjectInfoWrapper(EMPTY_PROJECT_INFO);
					}).thenApply(info -> new MicroProfileProjectInfoWrapper(info));
			// cache the future.
			cache.put(params.getUri(), future);
			return future;
		}
		if (!projectInfo.isDone()) {
			return projectInfo;
		}

		MicroProfileProjectInfoWrapper wrapper = getProjectInfoWrapper(projectInfo);
		if (wrapper.isReloadFromSource()) {
			// There are some java sources changed, get the Quarkus properties from java
			// sources.
			params.setScopes(MicroProfilePropertiesScope.ONLY_SOURCES);
			return provider.getProjectInfo(params).thenApply(info ->
			// then update the cache with the new properties
			{
				wrapper.update(info.getProperties(), info.getHints());
				return wrapper;
			});
		}

		// Returns the cached project info
		return projectInfo;
	}

	private static MicroProfileProjectInfoWrapper getProjectInfoWrapper(
			CompletableFuture<MicroProfileProjectInfo> future) {
		return future != null ? (MicroProfileProjectInfoWrapper) future.getNow(null) : null;
	}

	public Collection<String> propertiesChanged(MicroProfilePropertiesChangeEvent event) {
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
			MicroProfileProjectInfoWrapper info = getProjectInfoWrapper(cache.get(uri));
			if (info != null) {
				info.clearPropertiesFromSource();
			}
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
		return cache.entrySet().stream().filter(entry -> {
			MicroProfileProjectInfo projectInfo = getProjectInfoWrapper(entry.getValue());
			if (projectInfo != null) {
				return projectURIs.contains(projectInfo.getProjectURI());
			}
			return false;
		}).map(Map.Entry::getKey).collect(Collectors.toList());
	}

}
