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

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfileProjectInfoParams;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.commons.MicroProfilePropertiesScope;
import com.redhat.microprofile.commons.MicroProfilePropertyDefinitionParams;
import com.redhat.microprofile.commons.metadata.ItemMetadata;
import com.redhat.microprofile.ls.api.MicroProfileLanguageClientAPI;
import com.redhat.microprofile.ls.api.MicroProfilePropertyDefinitionProvider;

public class MockMicroProfileLanguageClient implements MicroProfileLanguageClientAPI {

	private final MicroProfileLanguageServer languageServer;

	private final Map<String, List<ItemMetadata>> jarProperties;
	private final Map<String, List<ItemMetadata>> sourcesProperties;

	private MicroProfilePropertyDefinitionProvider provider;

	public MockMicroProfileLanguageClient(MicroProfileLanguageServer languageServer) {
		this.languageServer = languageServer;
		this.jarProperties = new HashMap<>();
		this.sourcesProperties = new HashMap<>();
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
		List<ItemMetadata> properties = new ArrayList<>();
		info.setProperties(properties);

		List<ItemMetadata> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			properties.addAll(fromSources);
		}
		if (params.getScopes().contains(MicroProfilePropertiesScope.dependencies)) {
			List<ItemMetadata> fromJars = jarProperties.get(projectURI);
			if (fromJars != null) {
				properties.addAll(fromJars);
			}

		}
		return CompletableFuture.completedFuture(info);
	}

	public void changedClasspath(String projectURI, ItemMetadata... properties) {
		// Update properties
		List<ItemMetadata> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			fromSources.clear();
		} else {
			fromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, fromSources);
		}
		List<ItemMetadata> fromJars = jarProperties.get(projectURI);
		if (fromJars != null) {
			fromJars.clear();
		} else {
			fromJars = new ArrayList<>();
			jarProperties.put(projectURI, fromJars);
		}
		for (ItemMetadata property : properties) {
			if (!property.isBinary()) {
				fromJars.add(property);
			} else {
				fromSources.add(property);
			}
		}
		// Throw Quarkus event
		MicroProfilePropertiesChangeEvent event = new MicroProfilePropertiesChangeEvent();
		event.setType(MicroProfilePropertiesScope.SOURCES_AND_DEPENDENCIES);
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		languageServer.propertiesChanged(event);
	}

	public void changedJavaSources(String projectURI, ItemMetadata... properties) {
		// Update properties
		List<ItemMetadata> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			fromSources.clear();
		} else {
			fromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, fromSources);
		}

		for (ItemMetadata property : properties) {
			fromSources.add(property);
		}
		// Throw Quarkus event
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
