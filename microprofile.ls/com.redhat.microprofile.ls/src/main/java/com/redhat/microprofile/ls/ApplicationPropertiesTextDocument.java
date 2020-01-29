package com.redhat.microprofile.ls;

import java.util.function.BiFunction;

import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.commons.MicroProfilePropertiesChangeEvent;
import com.redhat.microprofile.ls.commons.ModelTextDocument;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.model.PropertiesModel;

public class ApplicationPropertiesTextDocument extends ModelTextDocument<PropertiesModel> {

	private String initialText;

	private MicroProfileProjectInfo currentProjectInfo;

	public ApplicationPropertiesTextDocument(TextDocumentItem document,
			BiFunction<TextDocument, CancelChecker, PropertiesModel> parse) {
		super(document, parse);
		this.setInitialText(document.getText());
	}

	public void setInitialText(String initialText) {
		this.initialText = initialText;
	}

	public MicroProfilePropertiesChangeEvent didSave() {
		try {
			return ApplicationPropertiesComparator.compare(initialText, getText(), currentProjectInfo);
		} finally {
			setInitialText(super.getText());
		}
	}

	public void update(MicroProfileProjectInfo projectInfo) {
		if (projectInfo == null) {
			return;
		}
		currentProjectInfo = projectInfo;
	}

}
