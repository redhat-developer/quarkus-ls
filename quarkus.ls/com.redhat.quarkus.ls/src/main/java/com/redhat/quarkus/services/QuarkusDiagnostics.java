/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.services;

import java.util.ArrayList;
import java.util.List;

import static com.redhat.quarkus.utils.PropertiesScannerUtils.PropertiesToken;
import static com.redhat.quarkus.utils.PropertiesScannerUtils.getTokenAt;

import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.settings.QuarkusValidationSettings;
import com.redhat.quarkus.utils.ApplicationPropertiesDocumentUtils;
import com.redhat.quarkus.utils.PropertiesScannerUtils.PropertiesTokenType;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

class QuarkusDiagnostics {

	public List<Diagnostic> doDiagnostics(TextDocument document, QuarkusValidationSettings validationSettings) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		if (validationSettings.isEnabled()) {
			scanForMissingEquals(document, diagnostics);
		}

		return diagnostics;
	}

	private void scanForMissingEquals(TextDocument document, List<Diagnostic> diagnostics) {
		try {
			int lines = ApplicationPropertiesDocumentUtils.getTotalLines(document);
			String lineText;

			for (int i = 0; i < lines; i++) {
				lineText = document.lineText(i);
				
				if (lineText.length() == 0) {
					continue;
				}

				if (lineRequiresEqualsToken(lineText)) {
					diagnostics.add(createMissingEqualsDiagnostic(i, lineText.length()));
				}
			}
		} catch (BadLocationException e) {
			// Do nothing
		}
	}

	private boolean lineRequiresEqualsToken(String lineText) {

		boolean lineContainsKey = false;
		int startLineOffset = 0;
		int offset = 0;
		PropertiesToken curr;

		while (offset < lineText.length()) {
			curr = getTokenAt(lineText, startLineOffset, offset);
			
			if (curr.getType() == PropertiesTokenType.KEY) {
				lineContainsKey = true;
			} else if (curr.getType() == PropertiesTokenType.EQUALS) {
				return false;
			}

			offset = curr.getEnd() + 1;
		}

		return lineContainsKey;
	}

	private Diagnostic createMissingEqualsDiagnostic(int lineIndex, int lineLength) {
		Position start = new Position(lineIndex, 0);
		Position end = new Position(lineIndex, lineLength);
		Range range = new Range(start, end);
		return new Diagnostic(range, "Missing '=' after key", DiagnosticSeverity.Error, "application.properties", "missing_equals_after_key");
	}
}