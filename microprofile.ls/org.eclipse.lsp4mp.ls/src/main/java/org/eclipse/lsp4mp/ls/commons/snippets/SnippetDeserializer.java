/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.commons.snippets;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;

/**
 * GSON deserializer to build Snippet from vscode JSON snippet.
 * 
 * @author Angelo ZERR
 *
 */
class SnippetDeserializer implements JsonDeserializer<Snippet> {

	private static final String PREFIX_ELT = "prefix";
	private static final String DESCRIPTION_ELT = "description";
	private static final String SCOPE_ELT = "scope";
	private static final String BODY_ELT = "body";
	private static final String CONTEXT_ELT = "context";

	private final TypeAdapter<? extends ISnippetContext<?>> contextDeserializer;

	public SnippetDeserializer(TypeAdapter<? extends ISnippetContext<?>> contextDeserializer) {
		this.contextDeserializer = contextDeserializer;
	}

	@Override
	public Snippet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		Snippet snippet = new Snippet();
		JsonObject snippetObj = json.getAsJsonObject();

		// prefix
		List<String> prefixes = new ArrayList<>();
		JsonElement prefixElt = snippetObj.get(PREFIX_ELT);
		if (prefixElt != null) {
			if (prefixElt.isJsonArray()) {
				JsonArray prefixArray = (JsonArray) prefixElt;
				prefixArray.forEach(elt -> {
					prefixes.add(elt.getAsString());
				});
			} else if (prefixElt.isJsonPrimitive()) {
				prefixes.add(prefixElt.getAsString());
			}
		}
		snippet.setPrefixes(prefixes);

		// body
		List<String> body = new ArrayList<>();
		JsonElement bodyElt = snippetObj.get(BODY_ELT);
		if (bodyElt != null) {
			if (bodyElt.isJsonArray()) {
				JsonArray bodyArray = (JsonArray) bodyElt;
				bodyArray.forEach(elt -> {
					body.add(elt.getAsString());
				});
			} else if (bodyElt.isJsonPrimitive()) {
				body.add(bodyElt.getAsString());
			}
		}
		snippet.setBody(body);

		// description
		JsonElement descriptionElt = snippetObj.get(DESCRIPTION_ELT);
		if (descriptionElt != null) {
			String description = descriptionElt.getAsString();
			snippet.setDescription(description);
		}

		// scope
		JsonElement scopeElt = snippetObj.get(SCOPE_ELT);
		if (scopeElt != null) {
			String scope = scopeElt.getAsString();
			snippet.setScope(scope);
		}

		// context
		if (contextDeserializer != null) {
			JsonElement contextElt = snippetObj.get(CONTEXT_ELT);
			if (contextElt != null) {
				ISnippetContext<?> snippetContext = contextDeserializer.fromJsonTree(contextElt);
				snippet.setContext(snippetContext);
			}
		}

		return snippet;
	}

}