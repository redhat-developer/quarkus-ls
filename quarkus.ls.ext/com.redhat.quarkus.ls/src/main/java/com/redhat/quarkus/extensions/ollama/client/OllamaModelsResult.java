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

import java.util.List;

/**
 * Bean class which maps the HTTP response of http://localhost:11434/v1/models
 * 
 * Example: <code>
 * {
		models: [
			{
				name: "qwen2:latest",
				model: "qwen2:latest",
				modified_at: "2024-08-20T18:56:28.9319459+02:00",
				size: 4431400262,
				digest: "e0d4e1163c585612b5419eee0a0d87fe0cac5fa6844001392e78d0cdf57c8138",
				details: {
					parent_model: "",
					format: "gguf",
					family: "qwen2",
					families: [
						"qwen2"
					],
					parameter_size: "7.6B",
					quantization_level: "Q4_0"
				}
			}
		]
}
 * </code>
 */
public class OllamaModelsResult {

	private List<OllamaModel> models;

	public List<OllamaModel> getModels() {
		return models;
	}

	public void setModels(List<OllamaModel> models) {
		this.models = models;
	}
}
