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

/**
 * Quarkus Ollama constants.
 */
public class OllamaConstants {

	public static final String QUARKUS_LANGCHAIN4J_OLLAMA_CHAT_MODEL_MODEL_ID_KEY = "quarkus.langchain4j.ollama.chat-model.model-id";

	public static final String QUARKUS_LANGCHAIN4J_OLLAMA_BASE_URL_KEY = "quarkus.langchain4j.ollama.base-url";

	public static final String DEFAULT_OLLAMA_BASE_URL = "http://localhost:11434";

}
