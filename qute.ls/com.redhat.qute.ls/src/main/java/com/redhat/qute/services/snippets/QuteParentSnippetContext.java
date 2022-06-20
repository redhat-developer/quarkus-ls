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
 * "context": { "parent" : "for", "unique" : true }
 *
 */

public class QuteParentSnippetContext extends AbstractQuteSnippetContext {

	public static final TypeAdapter<QuteParentSnippetContext> IN_PARENT = new SnippetContextForQuteParentAdapter();

	private final String parent;
	private final boolean unique;

	public QuteParentSnippetContext(String parent, boolean unique) {
		this.parent = parent;
		this.unique = unique;
	}

	@Override
	public boolean isMatch(CompletionRequest request, Map<String, String> model) {
		Node node = request.getNode();
		// Completion is triggered inside a section tag which matches the parent
		// e.g.
		// {#if ...}
		// | --> here {#else} completion is shown, the cursor is nested in #if parent.
		// {/if}
		if (isMatchParent(node) && isMatchUnique(node)) {
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
			String parent = null;
			boolean unique = false;
			in.beginObject();
			while (in.hasNext()) {
				String name = in.nextName();
				switch (name) {
				case "parent":
					if (in.peek() == JsonToken.STRING) {
						parent = in.nextString();
					}
					break;
				case "unique":
					if (in.peek() == JsonToken.BOOLEAN) {
						unique = in.nextBoolean();
					}
					break;
				default:
					in.skipValue();
				}
			}
			in.endObject();
			if (parent != null) {
				return new QuteParentSnippetContext(parent, unique);
			}
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

	/**
	 * Return true if unqiue context is false, or if parent node doesn't contain the
	 * nested snippet, false otherwise.
	 *
	 * @param node the completion request node
	 * @return true if unqiue context is true, and if parent node doesn't contain
	 *         the nested snippet, false otherwise.
	 */
	private boolean isMatchUnique(Node node) {
		if (!unique) {
			return true;
		}
		Section parentSection = node.getParentSection();
		if (parentSection != null) {
			for (Node child : parentSection.getChildren()) {
				if (child instanceof Section && ((Section) child).getTag() != null
						&& ((Section) child).getTag().equals(getTag())) {
					return false;
				}
			}
		}
		return true;
	}

	private String getTag() {
		return super.getPrefixes().get(0);
	}

}
