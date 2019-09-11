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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;

import com.google.gson.Gson;
import com.redhat.quarkus.commons.QuarkusProjectInfo;
import com.redhat.quarkus.ls.commons.BadLocationException;
import com.redhat.quarkus.ls.commons.TextDocument;
import com.redhat.quarkus.model.PropertiesModel;
import com.redhat.quarkus.settings.QuarkusCompletionSettings;
import com.redhat.quarkus.settings.QuarkusHoverSettings;
import com.redhat.quarkus.settings.QuarkusValidationSettings;

/**
 * Quarkus assert
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusAssert {

	private static QuarkusProjectInfo DEFAULT_PROJECT;

	private static final String QUARKUS_DIAGNOSTIC_SOURCE = "quarkus";

	public static QuarkusProjectInfo getDefaultQuarkusProjectInfo() {
		if (DEFAULT_PROJECT == null) {
			DEFAULT_PROJECT = new Gson().fromJson(
					new InputStreamReader(QuarkusAssert.class.getResourceAsStream("all-quarkus-properties.json")),
					QuarkusProjectInfo.class);
		}
		return DEFAULT_PROJECT;
	}

	// ------------------- Completion assert

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, snippetSupport, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, null, expectedCount, getDefaultQuarkusProjectInfo(), expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileURI, Integer expectedCount,
			QuarkusProjectInfo projectInfo, CompletionItem... expectedItems) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		PropertiesModel model = parse(value, fileURI);
		Position position = model.positionAt(offset);

		// Add snippet support for completion
		QuarkusCompletionSettings completionSettings = new QuarkusCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(snippetSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionSettings.setCapabilities(completionCapabilities);

		QuarkusLanguageService languageService = new QuarkusLanguageService();
		CompletionList list = languageService.doComplete(model, position, projectInfo, completionSettings, () -> {
		});

		assertCompletions(list, expectedCount, expectedItems);
	}

	public static void assertCompletions(CompletionList actual, Integer expectedCount,
			CompletionItem... expectedItems) {
		// no duplicate labels
		List<String> labels = actual.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			Assert.assertTrue(
					"Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}",
					previous != label);
			previous = label;
		}
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), actual.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(actual, item);
			}
		}
	}

	private static void assertCompletion(CompletionList completions, CompletionItem expected) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		Assert.assertEquals(
				expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(",")),
				1, matches.size());

		CompletionItem match = matches.get(0);
		/*
		 * if (expected.documentation != null) {
		 * Assert.assertEquals(match.getDocumentation().getRight().getValue(),
		 * expected.getd); } if (expected.kind) { Assert.assertEquals(match.kind,
		 * expected.kind); }
		 */
		// if (expected.getTextEdit() != null && match.getTextEdit() != null) {
		if (expected.getTextEdit().getNewText() != null) {
			Assert.assertEquals(expected.getTextEdit().getNewText(), match.getTextEdit().getNewText());
		}
		Range r = expected.getTextEdit().getRange();
		if (r != null && r.getStart() != null && r.getEnd() != null) {
			Assert.assertEquals(expected.getTextEdit().getRange(), match.getTextEdit().getRange());
		}
		// }
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			Assert.assertEquals(expected.getFilterText(), match.getFilterText());
		}

		if (expected.getDocumentation() != null) {
			Assert.assertEquals(expected.getDocumentation(), match.getDocumentation());
		}

	}

	public static CompletionItem c(String label, String newText, Range range) {
		return c(label, new TextEdit(range, newText), null);
	}

	private static CompletionItem c(String label, TextEdit textEdit, String filterText) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(textEdit);
		return item;
	}

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = new Position(startLine, startChar);
		Position end = new Position(endLine, endChar);
		return new Range(start, end);
	}
	// ------------------- Hover assert

	public static void assertHoverMarkdown(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		QuarkusHoverSettings hoverSettings = new QuarkusHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultQuarkusProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHoverPlaintext(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		QuarkusHoverSettings hoverSettings = new QuarkusHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.PLAINTEXT), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultQuarkusProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHover(String value, String fileURI, QuarkusProjectInfo projectInfo,
			QuarkusHoverSettings hoverSettings, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		PropertiesModel model = parse(value, fileURI);
		Position position = model.positionAt(offset);

		QuarkusLanguageService languageService = new QuarkusLanguageService();

		Hover hover = languageService.doHover(model, position, projectInfo, hoverSettings);
		if (expectedHoverLabel == null) {
			Assert.assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			Assert.assertEquals(expectedHoverLabel, actualHoverLabel);
			if (expectedHoverOffset != null) {
				Assert.assertNotNull(hover.getRange());
				Assert.assertNotNull(hover.getRange().getStart());
				Assert.assertEquals(expectedHoverOffset.intValue(), hover.getRange().getStart().getCharacter());
			}
		}
	}

	private static String getHoverLabel(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> contents = hover != null ? hover.getContents() : null;
		if (contents == null) {
			return null;
		}
		return contents.getRight().getValue();
	}

	// ------------------- SymbolInformation assert

	public static void testSymbolInformationsFor(String value, SymbolInformation... expected) {
		testSymbolInformationsFor(value, null, expected);
	}

	public static void testSymbolInformationsFor(String value, String fileURI, SymbolInformation... expected) {

		PropertiesModel model = parse(value, fileURI);

		QuarkusLanguageService languageService = new QuarkusLanguageService();

		List<SymbolInformation> actual = languageService.findSymbolInformations(model, () -> {
		});
		assertSymbolInformations(actual, expected);

	}

	public static SymbolInformation s(final String name, final SymbolKind kind, final String uri, final Range range) {
		return new SymbolInformation(name, kind, new Location(uri, range));
	}

	public static void assertSymbolInformations(List<SymbolInformation> actual, SymbolInformation... expected) {
		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- DocumentSymbol assert

	public static void testDocumentSymbolsFor(String value, DocumentSymbol... expected) {
		testDocumentSymbolsFor(value, null, expected);
	}

	public static void testDocumentSymbolsFor(String value, String fileURI, DocumentSymbol... expected) {
		PropertiesModel model = parse(value, fileURI);
		QuarkusLanguageService languageService = new QuarkusLanguageService();
		List<DocumentSymbol> actual = languageService.findDocumentSymbols(model, () -> {
		});
		assertDocumentSymbols(actual, expected);
	}

	public static DocumentSymbol ds(final String name, final SymbolKind kind, final Range range, final String detail) {
		return ds(name, kind, range, detail, new ArrayList<>());
	}

	public static DocumentSymbol ds(final String name, final SymbolKind kind, final Range range, final String detail,
			final List<DocumentSymbol> children) {
		return new DocumentSymbol(name, kind, range, range, detail, children);
	}

	public static void assertDocumentSymbols(List<DocumentSymbol> actual, DocumentSymbol... expected) {
		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- Diagnostics assert

	public static void testDiagnosticsFor(String value, QuarkusProjectInfo projectInfo, Diagnostic... expected) {
		QuarkusValidationSettings validationSettings = new QuarkusValidationSettings();
		testDiagnosticsFor(value, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, QuarkusProjectInfo projectInfo,
			QuarkusValidationSettings validationSettings, Diagnostic... expected) {
		testDiagnosticsFor(value, null, null, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, Integer expectedCount, QuarkusProjectInfo projectInfo,
			QuarkusValidationSettings validationSettings, Diagnostic... expected) {
		testDiagnosticsFor(value, null, expectedCount, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, String fileURI, Integer expectedCount,
			QuarkusProjectInfo projectInfo, QuarkusValidationSettings validationSettings, Diagnostic... expected) {
		PropertiesModel model = parse(value, fileURI);
		QuarkusLanguageService languageService = new QuarkusLanguageService();
		List<Diagnostic> actual = languageService.doDiagnostics(model, projectInfo, validationSettings, () -> {
		});
		if (expectedCount != null) {
			Assert.assertEquals(expectedCount.intValue(), actual.size());
		}
		assertDiagnostics(actual, expected);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		assertDiagnostics(actual, Arrays.asList(expected));
	}

	public static void assertDiagnostics(List<Diagnostic> actual, List<Diagnostic> expected) {
		List<Diagnostic> received = actual;
		Assert.assertEquals("Unexpected diagnostics:\n" + actual, expected, received);
	}

	public static Diagnostic d(int line, int startCharacter, int endCharacter, String message,
			DiagnosticSeverity severity, ValidationType code) {
		return d(line, startCharacter, line, endCharacter, message, severity, code);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, String message,
			DiagnosticSeverity severity, ValidationType code) {
		return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), message, severity,
				QUARKUS_DIAGNOSTIC_SOURCE, code.name());
	}

	private static PropertiesModel parse(String text, String uri) {
		TextDocument document = new TextDocument(text, uri != null ? uri : "application.properties");
		return PropertiesModel.parse(document);
	}

}