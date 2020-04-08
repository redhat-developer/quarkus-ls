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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.redhat.microprofile.commons.ProjectLabelInfoEntry;
import com.redhat.microprofile.ls.commons.snippets.ISnippetContext;

/**
 * A snippet context for Java files which matches java scope and dependency.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetContextForJava implements ISnippetContext<ProjectLabelInfoEntry> {

	public static final TypeAdapter<SnippetContextForJava> TYPE_ADAPTER = new SnippetContextForJavaAdapter();
	private List<String> types;

	public SnippetContextForJava(List<String> types) {
		this.types = types;
	}

	public List<String> getTypes() {
		return types;
	}

	@Override
	public boolean isMatch(ProjectLabelInfoEntry context) {
		if (context == null) {
			return true;
		}
		if (types == null || types.isEmpty()) {
			return true;
		}
		for (String type : types) {
			if (context.hasLabel(type)) {
				return true;
			}
		}
		return false;
	}

	private static class SnippetContextForJavaAdapter extends TypeAdapter<SnippetContextForJava> {

		@Override
		public SnippetContextForJava read(final JsonReader in) throws IOException {
			JsonToken nextToken = in.peek();
			if (nextToken == JsonToken.NULL) {
				return null;
			}

			List<String> types = new ArrayList<>();
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "type":
					if (in.peek() == JsonToken.BEGIN_ARRAY) {
						in.beginArray();
						while (in.peek() != JsonToken.END_ARRAY) {
							types.add(in.nextString());
						}
						in.endArray();
					} else {
						types.add(in.nextString());
					}
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return new SnippetContextForJava(types);
		}

		@Override
		public void write(JsonWriter out, SnippetContextForJava value) throws IOException {
			// Do nothing
		}
	}

}