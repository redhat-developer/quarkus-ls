package com.redhat.qute.ls.commons;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;

public abstract class TextDocumentItems<T extends TextDocumentItem> {

	private final Map<String, T> documents;
	
	public TextDocumentItems() {
		documents = new HashMap<>();
	}

	public T onDidOpenTextDocument(DidOpenTextDocumentParams params) {
		TextDocumentItem item = params.getTextDocument();
		synchronized (documents) {
			T document = createDocument(item);
			documents.put(document.getUri(), document);
			return document;
		}
	}

	public T onDidCloseTextDocument(DidCloseTextDocumentParams params) {
		synchronized (documents) {
			T document = getDocument(params.getTextDocument());
			if (document != null) {
				documents.remove(params.getTextDocument().getUri());
			}
			return document;
		}
	}

	protected T getDocument(TextDocumentIdentifier identifier) {
		return documents.get(identifier.getUri());
	}

	/**
	 * Returns the all opened documents.
	 *
	 * @return the all opened documents.
	 */
	public Collection<T> all() {
		synchronized (documents) {
			return documents.values();
		}
	}

	protected abstract T createDocument(TextDocumentItem item);
}
