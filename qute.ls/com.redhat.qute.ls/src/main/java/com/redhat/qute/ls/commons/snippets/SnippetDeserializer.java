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
package com.redhat.qute.ls.commons.snippets;

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
import com.redhat.qute.utils.StringUtils;

/**
 * GSON deserializer to build Snippet from vscode JSON snippet.
 * 
 * @author Angelo ZERR
 *
 */
class SnippetDeserializer implements JsonDeserializer<Snippet> {

	private static final String PREFIX_ELT = "prefix";
	private static final String SUFFIX_ELT = "suffix";
	private static final String DESCRIPTION_ELT = "description";
	private static final String LABEL_ELT = "label";
	private static final String SCOPE_ELT = "scope";
	private static final String SORTTEXT_ELT = "sortText";
	private static final String BODY_ELT = "body";
	private static final String CONTEXT_ELT = "context";
	private static final String LINK_ELT = "link";
	private static final String LINK_URL_ELT = "url";
	private static final String LINK_LABEL_ELT = "label";

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
		// by default label is the first prefix
		if (!prefixes.isEmpty()) {
			snippet.setLabel(prefixes.get(0));
		}

		// suffix
		JsonElement suffixElt = snippetObj.get(SUFFIX_ELT);
		if (suffixElt != null) {
			String suffix = suffixElt.getAsString();
			snippet.setSuffix(suffix);
		}

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

		// label
		JsonElement labelElt = snippetObj.get(LABEL_ELT);
		if (labelElt != null) {
			String label = labelElt.getAsString();
			if (label.contains("$")) {
				if (!StringUtils.isEmpty(snippet.getDescription())) {
					label = label.replace("$description", snippet.getDescription());
				}
				if (!snippet.getPrefixes().isEmpty()) {
					label = label.replace("$prefix", snippet.getPrefixes().get(0));
				}
			}
			snippet.setLabel(label);
		}

		// scope
		JsonElement scopeElt = snippetObj.get(SCOPE_ELT);
		if (scopeElt != null) {
			String scope = scopeElt.getAsString();
			snippet.setScope(scope);
		}

		// sortText
		JsonElement sortTextElt = snippetObj.get(SORTTEXT_ELT);
		if (sortTextElt != null) {
			String sortText = sortTextElt.getAsString();
			snippet.setSortText(sortText);
		}

		// context
		if (contextDeserializer != null) {
			JsonElement contextElt = snippetObj.get(CONTEXT_ELT);
			if (contextElt != null) {
				ISnippetContext<?> snippetContext = contextDeserializer.fromJsonTree(contextElt);
				snippet.setContext(snippetContext);
			}
		}

		// link
		List<Link> links = new ArrayList<>();
		JsonElement linkElt = snippetObj.get(LINK_ELT);
		if (linkElt != null) {
			if (linkElt.isJsonArray()) {
				JsonArray urlArray = (JsonArray) linkElt;
				urlArray.forEach(elt -> {
					Link link = createLink(elt);
					if (link != null) {
						links.add(link);
					}
				});
			} else if (linkElt.isJsonObject()) {
				Link link = createLink(linkElt);
				if (link != null) {
					links.add(link);
				}
			}
		}
		snippet.setLinks(links);

		return snippet;
	}

	private static Link createLink(JsonElement elt) {
		if (!elt.isJsonObject()) {
			return null;
		}
		JsonObject linkObj = elt.getAsJsonObject();
		JsonElement urlElt = linkObj.get(LINK_URL_ELT);
		if (urlElt == null) {
			return null;
		}
		String url = urlElt.getAsString();
		JsonElement labelElt = linkObj.get(LINK_LABEL_ELT);
		String label = labelElt != null ? labelElt.getAsString() : null;
		return new Link(url, label);
	}

}