/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.services.snippets;

import java.io.IOException;
import java.util.Map;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.services.completions.CompletionRequest;

/**
 * Snippet context for Qute sections that have parent sections.
 *
 * e.g.
 *
 * "context": { "parent": "if" }
 *
 */

public class QuteParentSnippetContext implements IQuteSnippetContext {

	public static final TypeAdapter<QuteParentSnippetContext> IN_PARENT = new SnippetContextForQuteParentAdapter();

	private final String parent;

	public QuteParentSnippetContext(String parent) {
		this.parent = parent;
	}

	@Override
	public boolean isMatch(CompletionRequest request, Map<String, String> model) {
		Node node = request.getNode();
		// Completion is triggered inside a section tag which matches the parent
		// e.g.
		// {#if ...}
		// | --> here {#else} completion is shown, the cursor is nested in #if parent.
		// {/if}
		if (isMatchParent(node)) {
			if ((node.getKind() == NodeKind.Template || node.getKind() == NodeKind.Text)) {
				return true;
			} else if (node.getKind() == NodeKind.Section) {
				Section section = (Section) node;
				return section.isInStartTagName(request.getOffset());
			}
		}
		return false;
	}

	private static class SnippetContextForQuteParentAdapter extends TypeAdapter<QuteParentSnippetContext> {

		@Override
		public QuteParentSnippetContext read(final JsonReader in) throws IOException {
			JsonToken nextToken = in.peek();
			if (nextToken == JsonToken.NULL) {
				return null;
			}

			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "parent":
					if (in.peek() == JsonToken.STRING) {
						return new QuteParentSnippetContext(in.nextString());
					}
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			return null;
		}

		@Override
		public void write(JsonWriter out, QuteParentSnippetContext value) throws IOException {
			// Do nothing
		}
	}

	/**
	 * Return true if parent section matches context parent, false otherwise.
	 *
	 * @param node the completion request node
	 * @return true if parent section matches context parent, false otherwise.
	 */
	private boolean isMatchParent(Node node) {
		return (node.getParentSection() != null && parent.equals(node.getParentSection().getTag()));
	}

}
