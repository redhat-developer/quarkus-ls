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
package com.redhat.quarkus.extensions.ollama.client;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.GsonBuilder;

/**
 * Ollama client.
 */
public class OllamaClient {

	private static final Logger LOGGER = Logger.getLogger(OllamaClient.class.getName());

	private static final long REFRESH_OLLAMA_CACHE_TIMEOUT = 60000L;

	private String currentBaseUrl;

	private List<OllamaModel> currentModels;

	private long lastDate = -1;

	/**
	 * Update the base Url of the Ollama server.
	 * 
	 * @param baseUrl the the base Url of the Ollama server
	 * @return true if base Url is different from the current base Url.
	 */
	public boolean update(String baseUrl) {
		if (!Objects.equals(baseUrl, currentBaseUrl)) {
			currentModels = null;
		}
		this.currentBaseUrl = baseUrl;
		return currentModels == null;
	}

	/**
	 * Returns true if the Ollama model cache must be evicted and false otherwise.
	 * 
	 * @return true if the Ollama model cache must be evicted and false otherwise.
	 */
	public boolean needToRefreshModels() {
		return (System.currentTimeMillis() - lastDate) > REFRESH_OLLAMA_CACHE_TIMEOUT;
	}

	/**
	 * Returns the list of the Ollama models retrieves with the /api/tags.
	 * 
	 * @return the list of the Ollama models retrieves with the /api/tags.
	 */
	public List<OllamaModel> getModels() {
		if (currentModels == null) {
			String apiTagsUrl = currentBaseUrl + "/api/tags";
			currentModels = collectOllamaModels(apiTagsUrl);
			lastDate = System.currentTimeMillis();
		}
		return currentModels;
	}

	private static List<OllamaModel> collectOllamaModels(String searchUrl) {
		try {
			// Call Http search Url (ex : http://localhost:11434/api/tags)
			HttpRequest request = HttpRequest.newBuilder() //
					.uri(new URI(searchUrl)) //
					.GET() //
					.build();

			HttpResponse<String> response = HttpClient.newBuilder() //
					.followRedirects(HttpClient.Redirect.NORMAL) // follow redirects
					.build() //
					.send(request, BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				return Collections.emptyList();
			}

			// ex :
			/*
			 * { models: [ { name: "qwen2:latest", model: "qwen2:latest", modified_at:
			 * "2024-08-20T18:56:28.9319459+02:00", size: 4431400262, digest:
			 * "e0d4e1163c585612b5419eee0a0d87fe0cac5fa6844001392e78d0cdf57c8138", details:
			 * { parent_model: "", format: "gguf", family: "qwen2", families: [ "qwen2" ],
			 * parameter_size: "7.6B", quantization_level: "Q4_0" } } ] }
			 */
			String result = response.body();
			OllamaModelsResult r = new GsonBuilder() //
					.create()//
					.fromJson(new StringReader(result), OllamaModelsResult.class);
			if (r != null && r.getModels() != null) {
				return r.getModels();
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not connect to ollama '" + searchUrl + "': " + e.getMessage());
		}
		return Collections.emptyList();
	}
}
