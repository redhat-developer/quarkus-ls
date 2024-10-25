/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.extensions.ollama;

import static com.redhat.quarkus.extensions.ollama.OllamaConstants.DEFAULT_OLLAMA_BASE_URL;
import static com.redhat.quarkus.extensions.ollama.OllamaConstants.OLLAMA_MODEL_ID_KEYS;
import static com.redhat.quarkus.extensions.ollama.OllamaConstants.QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL_KEY;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4mp.commons.metadata.ItemHint;
import org.eclipse.lsp4mp.commons.metadata.ItemMetadata;
import org.eclipse.lsp4mp.commons.metadata.ValueHint;
import org.eclipse.lsp4mp.commons.utils.StringUtils;
import org.eclipse.lsp4mp.extensions.ExtendedMicroProfileProjectInfo;
import org.eclipse.lsp4mp.extensions.ItemMetadataProvider;
import org.eclipse.lsp4mp.model.Node;
import org.eclipse.lsp4mp.model.Node.NodeType;
import org.eclipse.lsp4mp.model.PropertiesModel;
import org.eclipse.lsp4mp.model.Property;

import com.redhat.quarkus.extensions.ollama.client.OllamaClient;

/**
 * Properties provider to collect available Ollama model values for the property
 * 'quarkus.langchain4j.ollama.chat-model.model-id'.
 * 
 * The Ollama models are collected by using Ollama Search Api by using the
 * following base url:
 * 
 * <ul>
 * <li>defined with 'quarkus.langchain4j.ollama.base-url' property.
 * <li>otherwise with 'http://localhost:11434'</li>
 * </ul>
 */
public class OllamaItemMetadataProvider implements ItemMetadataProvider {

	private ExtendedMicroProfileProjectInfo projectInfo;
	private OllamaClient ollamaClient;

	public OllamaItemMetadataProvider(ExtendedMicroProfileProjectInfo projectInfo) {
		this.projectInfo = projectInfo;
		// the Ollama models are collected only if
		// 'quarkus.langchain4j.ollama.chat-model.model-id' property exists.
		boolean available = this.projectInfo.getProperties().stream()
				.anyMatch(p -> OLLAMA_MODEL_ID_KEYS.contains(p.getName()));
		if (available) {
			OLLAMA_MODEL_ID_KEYS.forEach(modelIdKey -> {
				ItemHint hint = new ItemHint();
				hint.setName(modelIdKey);
				hint.setValues(Collections.emptyList());
				projectInfo.getHints().add(hint);
			});
			this.ollamaClient = new OllamaClient();
		}
	}

	@Override
	public boolean isAvailable() {
		return ollamaClient != null;
	}

	@Override
	public void update(PropertiesModel document) {
		if (document == null) {
			// Java sources changes, do nothing
			return;
		}
		// Called when application.properties file is opened or updated (when user type
		// something in the file).

		if (ollamaClient != null && hasModelProperty(document)) {
			// The application.properties declare the
			// 'quarkus.langchain4j.ollama.chat-model.model-id' property,
			// we need to collect available Ollama models by using Ollama Search Api
			String baseUrl = getSearchBaseUrl(document);
			if (ollamaClient.update(baseUrl) || ollamaClient.needToRefreshModels()) {
				// The current search url is different with the new, collect Ollama models.
				// Update the available models values for the property
				// 'quarkus.langchain4j.ollama.chat-model.model-id'
				List<ValueHint> models = ollamaClient.getModels() //
						.stream() //
						.map(m -> {
							ValueHint model = new ValueHint();
							String modelId = m.getName();
							if (modelId.endsWith(":latest")) {
								modelId = modelId.substring(0, modelId.length() - 7);
							}
							model.setValue(modelId);
							return model;
						}) //
						.collect(Collectors.toList());
				OLLAMA_MODEL_ID_KEYS.forEach(modelIdKey -> {
					ItemHint hint = projectInfo.getHint(modelIdKey);
					hint.setValues(models);
				});
			}
		}
	}

	private boolean hasModelProperty(PropertiesModel document) {
		for (String modelIdKey : OLLAMA_MODEL_ID_KEYS) {
			if (getProperty(document, modelIdKey) != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<ItemMetadata> getProperties() {
		// This provider doesn't contribute to add/remove properties
		return null;
	}

	private static Property getProperty(PropertiesModel document, String propertyName) {
		for (Node node : document.getChildren()) {
			if (node.getNodeType() == NodeType.PROPERTY) {
				Property property = (Property) node;
				if (propertyName.equals(property.getPropertyName())) {
					return property;
				}
			}
		}
		return null;
	}

	private static String getSearchBaseUrl(PropertiesModel document) {
		Property baseUrlProperty = getProperty(document, QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL_KEY);
		if (baseUrlProperty != null) {
			String propertyValue = baseUrlProperty.getPropertyValue();
			if (StringUtils.hasText(propertyValue)) {
				return propertyValue;
			}
		}
		return DEFAULT_OLLAMA_BASE_URL;
	}

}
