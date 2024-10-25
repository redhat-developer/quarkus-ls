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

import static com.redhat.quarkus.extensions.ollama.OllamaConstants.OLLAMA_MODEL_ID_KEYS;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4mp.services.properties.ValidationValueContext;
import org.eclipse.lsp4mp.services.properties.extensions.participants.IPropertyValidatorParticipant;

/**
 * Custom validation for 'quarkus.langchain4j.ollama.chat-model.model-id'.
 */
public class OllamaPropertyValidatorParticipant implements IPropertyValidatorParticipant {

	@Override
	public boolean validatePropertyValue(ValidationValueContext context, CancelChecker cancelChecker) {
		if (OLLAMA_MODEL_ID_KEYS.contains(context.getPropertyName())) {
			// No validation for Ollama model.
			return true;
		}
		return false;
	}
}
