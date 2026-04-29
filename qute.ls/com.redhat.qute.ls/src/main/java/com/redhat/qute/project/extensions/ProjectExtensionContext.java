package com.redhat.qute.project.extensions;

import java.util.ArrayList;
import java.util.List;

import com.redhat.qute.project.QuteTextDocument;

public class ProjectExtensionContext {

	private final List<QuteTextDocument> documentsToReparse;

	public ProjectExtensionContext() {
		this.documentsToReparse = new ArrayList<>();
	}

	public void reparseTemplate(QuteTextDocument document) {
		documentsToReparse.add(document);
	}

	public void reparseTemplates() {
		for (QuteTextDocument document : documentsToReparse) {
			document.reparseTemplate();
		}
	}
}
