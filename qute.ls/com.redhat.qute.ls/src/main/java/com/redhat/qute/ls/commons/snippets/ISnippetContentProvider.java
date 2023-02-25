package com.redhat.qute.ls.commons.snippets;

import java.util.Map;

import org.eclipse.lsp4j.CompletionItem;

public interface ISnippetContentProvider {

	String getInsertText(Snippet snippet, Map<String, String> model, boolean replace,
			String lineDelimiter, String whitespacesIndent, CompletionItem item);
}
