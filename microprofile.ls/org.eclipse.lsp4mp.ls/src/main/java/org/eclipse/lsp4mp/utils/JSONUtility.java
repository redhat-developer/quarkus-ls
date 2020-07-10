/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.utils;

import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * JSONUtility
 */
public class JSONUtility {

	private JSONUtility() {
	}

	public static <T> T toModel(Object object, Class<T> clazz) {
		if (object == null) {
			return null;
		}
		if (clazz == null) {
			throw new IllegalArgumentException("Class can not be null");
		}
		if (object instanceof JsonElement) {
			Gson gson = getDefaultGsonBuilder().create();
			return gson.fromJson((JsonElement) object, clazz);
		}
		if (clazz.isInstance(object)) {
			return clazz.cast(object);
		}
		return null;
	}

	private static GsonBuilder getDefaultGsonBuilder() {
		return new GsonBuilder() //
				// required to deserialize XMLFormattingOptions which extends FormattingOptions
				// which uses Either
				.registerTypeAdapterFactory(new EitherTypeAdapter.Factory());
	}
}