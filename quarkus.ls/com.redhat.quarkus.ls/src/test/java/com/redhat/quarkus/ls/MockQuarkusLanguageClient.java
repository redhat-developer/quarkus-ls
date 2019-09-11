package com.redhat.quarkus.ls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;

import com.redhat.quarkus.commons.ExtendedConfigDescriptionBuildItem;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.commons.QuarkusProjectInfoParams;
import com.redhat.quarkus.commons.QuarkusPropertiesChangeEvent;
import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.ls.api.QuarkusLanguageClientAPI;

public class MockQuarkusLanguageClient implements QuarkusLanguageClientAPI {

	private final QuarkusLanguageServer languageServer;

	private final Map<String, List<ExtendedConfigDescriptionBuildItem>> jarProperties;
	private final Map<String, List<ExtendedConfigDescriptionBuildItem>> sourcesProperties;

	public MockQuarkusLanguageClient(QuarkusLanguageServer languageServer) {
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
	public CompletableFuture<QuarkusProjectInfo> getQuarkusProjectInfo(QuarkusProjectInfoParams params) {
		QuarkusProjectInfo info = new QuarkusProjectInfo();
		String applicationPropertiesURI = params.getUri();
		String projectURI = applicationPropertiesURI.substring(0, applicationPropertiesURI.indexOf('/'));
		info.setProjectURI(projectURI);
		List<ExtendedConfigDescriptionBuildItem> properties = new ArrayList<>();
		info.setProperties(properties);

		List<ExtendedConfigDescriptionBuildItem> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			properties.addAll(fromSources);
		}
		if (params.getScope() == QuarkusPropertiesScope.classpath) {
			List<ExtendedConfigDescriptionBuildItem> fromJars = jarProperties.get(projectURI);
			if (fromJars != null) {
				properties.addAll(fromJars);
			}

		}
		return CompletableFuture.completedFuture(info);
	}

	public void changedClasspath(String projectURI, ExtendedConfigDescriptionBuildItem... properties) {
		// Update properties
		List<ExtendedConfigDescriptionBuildItem> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			fromSources.clear();
		} else {
			fromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, fromSources);
		}
		List<ExtendedConfigDescriptionBuildItem> fromJars = jarProperties.get(projectURI);
		if (fromJars != null) {
			fromJars.clear();
		} else {
			fromJars = new ArrayList<>();
			jarProperties.put(projectURI, fromJars);
		}
		for (ExtendedConfigDescriptionBuildItem property : properties) {
			if (property.getLocation().endsWith(".jar")) {
				fromJars.add(property);
			} else {
				fromSources.add(property);
			}
		}
		// Throw Quarkus event
		QuarkusPropertiesChangeEvent event = new QuarkusPropertiesChangeEvent();
		event.setType(QuarkusPropertiesScope.classpath);
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		languageServer.quarkusPropertiesChanged(event);
	}

	public void changedJavaSources(String projectURI, ExtendedConfigDescriptionBuildItem... properties) {
		// Update properties
		List<ExtendedConfigDescriptionBuildItem> fromSources = sourcesProperties.get(projectURI);
		if (fromSources != null) {
			fromSources.clear();
		} else {
			fromSources = new ArrayList<>();
			sourcesProperties.put(projectURI, fromSources);
		}

		for (ExtendedConfigDescriptionBuildItem property : properties) {
			fromSources.add(property);
		}
		// Throw Quarkus event
		QuarkusPropertiesChangeEvent event = new QuarkusPropertiesChangeEvent();
		event.setType(QuarkusPropertiesScope.sources);
		event.setProjectURIs(new HashSet<String>(Arrays.asList(projectURI)));
		languageServer.quarkusPropertiesChanged(event);
	}

}
