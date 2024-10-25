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

import java.util.Set;

/**
 * Quarkus Ollama constants.
 */
public class OllamaConstants {

	private static final String QUARKUS_LANGCHAIN4J_OLLAMA_CHAT_MODEL_MODEL_ID_KEY = "quarkus.langchain4j.ollama.chat-model.model-id";

	private static final String QUARKUS_LANGCHAIN4J_OLLAMA_EMBEDDING_MODEL_MODEL_ID_KEY = "quarkus.langchain4j.ollama.embedding-model.model-id";

	public static final Set<String> OLLAMA_MODEL_ID_KEYS = Set.of(QUARKUS_LANGCHAIN4J_OLLAMA_CHAT_MODEL_MODEL_ID_KEY,
			QUARKUS_LANGCHAIN4J_OLLAMA_EMBEDDING_MODEL_MODEL_ID_KEY);

	public static final String QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL_KEY = "quarkus.langchain4j.ollama.base-url";

	public static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";

}
