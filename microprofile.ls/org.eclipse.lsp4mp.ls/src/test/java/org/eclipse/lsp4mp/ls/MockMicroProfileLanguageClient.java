/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfo;
import org.eclipse.lsp4mp.commons.MicroProfileProjectInfoParams;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesChangeEvent;
import org.eclipse.lsp4mp.commons.MicroProfilePropertiesScope;
import org.eclipse.lsp4mp.commons.MicroProfilePropertyDefinitionParams;
import org.eclipse.lsp4mp.commons.metadata.ItemBase;
import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.ls.MicroProfileLanguageServer;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageClientAPI;
import org.eclipse.lsp4mp.ls.api.MicroProfilePropertyDefinitionProvider;

public class MockMicroProfileLanguageClient implements MicroProfileLanguageClientAPI {

	private final MicroProfileLanguageServer languageServer;

	private final Map<String, List<ItemMetadata>> jarProperties;
	private final Map<String, List<ItemHint>> jarHints;
	private final Map<String, List<ItemMetadata>> sourcesProperties;

	private final Map<String, List<ItemHint>> sourcesHints;

	private MicroProfilePropertyDefinitionProvider provider;

	public MockMicroProfileLanguageClient(MicroProfileLanguageServer languageServer) {
		this.languageServer = languageServer;
		this.jarProperties = new HashMap<>();
		this.jarHints = new HashMap<>();
		this.sourcesProperties = new HashMap<>();
		this.sourcesHints = new HashMap<>();
	}

	@Override
	public void telemetryEvent(Object object) {

	}

	@Override
	public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {

	}

	@Override
	public void showMessage(MessageParams messageParams) {

	}

	@Override
	public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
		return null;
	}

	@Override
	public void logMessage(MessageParams message) {

	}

	@Override
	public CompletableFuture<MicroProfileProjectInfo> getProjectInfo(MicroProfileProjectInfoParams params) {
		MicroProfileProjectInfo info = new MicroProfileProjectInfo();
		String applicationPropertiesURI = params.getUri();
		String projectURI = applicationPropertiesURI.substring(0, applicationPropertiesURI.indexOf('/'));
		info.setProjectURI(projectURI);

		// Update properties
		List<ItemMetadata> properties = new ArrayList<>();
		info.setProperties(properties);

		List<ItemMetadata> propertiesFromSources = sourcesProperties.get(projectURI);
		if (propertiesFromSources != null) {
			properties.addAll(propertiesFromSources);
		}
		if (params.getScopes().contains(MicroProfilePropertiesScope.dependencies)) {
			List<ItemMetadata> fromJars = jarProperties.get(projectURI);
			if (fromJars != null) {
				properties.addAll(fromJars);
			}
		}
		// Update hints
		List<ItemHint> hints = new ArrayList<>();
		info.setHints(hints);

		List<ItemHint> hintsFromSources = sourcesHints.get(projectURI);
		if (hintsFromSources != null) {
			hints.addAll(hintsFromSources);
		}
		if (params.getScopes().contains(MicroProfilePropertiesScope.dependencies)) {
			List<ItemHint> fromJars = jarHints.get(projectURI);
			if (fromJars != null) {
				hints.addAll(fromJars);
			}
		}

		return CompletableFuture.completedFuture(info);
	}

	public void changedClasspath(String projectURI, ItemBase... items) {
		// Update properties
		List<ItemMetadata> propertiesFromSources = sourcesProperties.get(projectURI);
		if (propertiesFromSources != null) {
			propertiesFromSources.clear();
		} else {
			propertiesFromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, propertiesFromSources);
		}
		// Update hints
		List<ItemHint> hintsFromSources = sourcesHints.get(projectURI);
		if (hintsFromSources != null) {
			hintsFromSources.clear();
		} else {
			hintsFromSources = new ArrayList<>();
			sourcesHints.put(projectURI, hintsFromSources);
		}

		List<ItemMetadata> propertiesFromJars = jarProperties.get(projectURI);
		if (propertiesFromJars != null) {
			propertiesFromJars.clear();
		} else {
			propertiesFromJars = new ArrayList<>();
			jarProperties.put(projectURI, propertiesFromJars);
		}
		List<ItemHint> hintsFromJars = jarHints.get(projectURI);
		if (hintsFromJars != null) {
			hintsFromJars.clear();
		} else {
			hintsFromJars = new ArrayList<>();
			jarHints.put(projectURI, hintsFromJars);
		}

		for (ItemBase item : items) {
			if (item instanceof ItemMetadata) {
				if (!item.isBinary()) {
					propertiesFromJars.add((ItemMetadata) item);
				} else {
					propertiesFromSources.add((ItemMetadata) item);
				}
			} else if (item instanceof ItemHint) {
				if (!item.isBinary()) {
					hintsFromJars.add((ItemHint) item);
				} else {
					hintsFromSources.add((ItemHint) item);
				}
			}

		}
		// Throw properties change event
		MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
		event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		languageServer.propertiesChanged(event);
	}

	public void changedJavaSources(String projectURI, ItemBase... items) {
		// Update properties
		List<ItemMetadata> propertiesFromSources = sourcesProperties.get(projectURI);
		if (propertiesFromSources != null) {
			propertiesFromSources.clear();
		} else {
			propertiesFromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, propertiesFromSources);
		}
		// Update hints
		List<ItemHint> hintsFromSources = sourcesHints.get(projectURI);
		if (hintsFromSources != null) {
			hintsFromSources.clear();
		} else {
			hintsFromSources = new ArrayList<>();
			sourcesHints.put(projectURI, hintsFromSources);
		}

		for (ItemBase item : items) {
			if (item instanceof ItemMetadata) {
				propertiesFromSources.add((ItemMetadata) item);
			} else if (item instanceof ItemHint) {
				hintsFromSources.add((ItemHint) item);
			}
		}
		// Throw properties change event
		MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
		event.setType(MicroProfilePropertiesScope.ONLY_SOURCES);
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		languageServer.propertiesChanged(event);
	}

	@Override
	public CompletableFuture<Location> getPropertyDefinition(MicroProfilePropertyDefinitionParams params) {
		if (provider == null) {
			provider = new MockMicroProfilePropertyDefinitionProvider();
		}
		return provider.getPropertyDefinition(params);
	}

}
