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
package com.redhat.microprofile.snippets;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * GSON snippet context deserializer to create instance of
 * {@link SnippetContextProperties} from the JSON declaration:
 * 
 * <code>
 *  "context": {
			"extension": "quarkus-agroal"
	}
 *  </code>
 * 
 * @author Angeko ZERR
 *
 */
public class SnippetContextPropertiesDeserializer implements JsonDeserializer<SnippetContextProperties> {

	private static final String EXTENSION_ELT = "extension";

	@Override
	public SnippetContextProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		json = json.getAsJsonObject().get(EXTENSION_ELT);
		if (json != null && json.isJsonPrimitive()) {
			String extension = json.getAsString();
			return new SnippetContextProperties(extension);
		}
		return null;
	}

}
