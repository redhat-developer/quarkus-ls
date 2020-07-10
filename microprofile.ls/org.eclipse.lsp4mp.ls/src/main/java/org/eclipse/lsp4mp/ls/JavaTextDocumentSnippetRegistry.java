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
package org.eclipse.lsp4mp.ls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4mp.ls.commons.snippets.Snippet;
import org.eclipse.lsp4mp.ls.commons.snippets.TextDocumentSnippetRegistry;
import org.eclipse.lsp4mp.snippets.LanguageId;
import org.eclipse.lsp4mp.snippets.SnippetContextForJava;

/**
 * Java snippet registry.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaTextDocumentSnippetRegistry extends TextDocumentSnippetRegistry {

	private List<String> types;

	public JavaTextDocumentSnippetRegistry() {
		super(LanguageId.java.name());
	}

	/**
	 * Returns the all distinct types declared in context/type of each snippet.
	 * 
	 * @return the all distinct types declared in context/type of each snippet.
	 */
	public List<String> getTypes() {
		if (types != null) {
			return types;
		}
		types = collectTypes();
		return types;
	}

	private synchronized List<String> collectTypes() {
		if (types != null) {
			return types;
		}
		List<String> types = new ArrayList<>();
		for (Snippet snippet : getSnippets()) {
			if (snippet.getContext() != null && snippet.getContext() instanceof SnippetContextForJava) {
				List<String> snippetTypes = ((SnippetContextForJava) snippet.getContext()).getTypes();
				if (snippetTypes != null) {
					for (String snippetType : snippetTypes) {
						if (!types.contains(snippetType)) {
							types.add(snippetType);
						}
					}
				}
			}
		}
		return types;
	}
}
