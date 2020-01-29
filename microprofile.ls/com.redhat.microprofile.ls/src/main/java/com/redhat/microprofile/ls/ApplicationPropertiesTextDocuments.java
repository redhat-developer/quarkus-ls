package com.redhat.microprofile.ls;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.ls.commons.TextDocuments;
import com.redhat.microprofile.model.PropertiesModel;

public class ApplicationPropertiesTextDocuments extends TextDocuments<ApplicationPropertiesTextDocument> {

	private final BiFunction<TextDocument, CancelChecker, PropertiesModel> parse;

	public ApplicationPropertiesTextDocuments() {
		this.parse = ((document, cancelChecker) -> {
			return PropertiesModel.parse(document);
		});
	}

	@Override
	public ApplicationPropertiesTextDocument createDocument(TextDocumentItem document) {
		ApplicationPropertiesTextDocument doc = new ApplicationPropertiesTextDocument(document, parse);
		doc.setIncremental(isIncremental());
		return doc;
	}

	public MicroProfilePropertiesChangeEvent onDidSaveTextDocument(DidSaveTextDocumentParams params) {
		ApplicationPropertiesTextDocument document = super.get(params.getTextDocument().getUri());
		if (document != null) {
			return document.didSave();
		}
		return null;
	}

}
