/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.services;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.Command;
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
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.microprofile.commons.MicroProfileProjectInfo;
import com.redhat.microprofile.ls.MockMicroProfilePropertyDefinitionProvider;
import com.redhat.microprofile.ls.api.MicroProfilePropertyDefinitionProvider;
import com.redhat.microprofile.ls.commons.BadLocationException;
import com.redhat.microprofile.ls.commons.TextDocument;
import com.redhat.microprofile.ls.commons.client.CommandCapabilities;
import com.redhat.microprofile.ls.commons.client.CommandKind;
import com.redhat.microprofile.ls.commons.client.CommandKindCapabilities;
import com.redhat.microprofile.ls.commons.snippets.TextDocumentSnippetRegistry;
import com.redhat.microprofile.model.PropertiesModel;
import com.redhat.microprofile.settings.MicroProfileCommandCapabilities;
import com.redhat.microprofile.settings.MicroProfileCompletionSettings;
import com.redhat.microprofile.settings.MicroProfileFormattingSettings;
import com.redhat.microprofile.settings.MicroProfileHoverSettings;
import com.redhat.microprofile.settings.MicroProfileValidationSettings;
import com.redhat.microprofile.snippets.LanguageId;
import com.redhat.microprofile.snippets.SnippetContextForProperties;
import com.redhat.microprofile.utils.DocumentationUtils;
import com.redhat.microprofile.utils.PositionUtils;

/**
 * MicroProfile assert
 * 
 * @author Angelo ZERR
 *
 */
public class MicroProfileAssert {

	private static MicroProfileProjectInfo DEFAULT_PROJECT;

	private static MicroProfilePropertyDefinitionProvider DEFAULT_DEFINITION_PROVIDER;

	private static final String MICROPROFILE_DIAGNOSTIC_SOURCE = "microprofile";

	public static MicroProfileProjectInfo getDefaultMicroProfileProjectInfo() {
		if (DEFAULT_PROJECT == null) {
			DEFAULT_PROJECT = createGson().fromJson(
					new InputStreamReader(MicroProfileAssert.class.getResourceAsStream("all-quarkus-properties.json")),
					MicroProfileProjectInfo.class);
		}
		return DEFAULT_PROJECT;
	}

	private static Gson createGson() {
		return new GsonBuilder().registerTypeAdapterFactory(new EnumTypeAdapter.Factory()).create();
	}

	public static MicroProfilePropertyDefinitionProvider getDefaultMicroProfilePropertyDefinitionProvider() {
		if (DEFAULT_DEFINITION_PROVIDER == null) {
			DEFAULT_DEFINITION_PROVIDER = new MockMicroProfilePropertyDefinitionProvider();
		}
		return DEFAULT_DEFINITION_PROVIDER;
	}

	// ------------------- Completion assert

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount)
			throws BadLocationException {
		testCompletionFor(value, snippetSupport, false, expectedCount);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws BadLocationException {
		testCompletionFor(value, snippetSupport, false, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, boolean insertSpacing,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, insertSpacing, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, false, null, expectedCount, getDefaultMicroProfileProjectInfo(),
				expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, boolean insertSpacing,
			Integer expectedCount, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, insertSpacing, null, expectedCount,
				getDefaultMicroProfileProjectInfo(), expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileURI, Integer expectedCount,
			MicroProfileProjectInfo projectInfo, CompletionItem... expectedItems) throws BadLocationException {
		testCompletionFor(value, snippetSupport, false, null, expectedCount, projectInfo, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, boolean insertSpacing, String fileURI,
			Integer expectedCount, MicroProfileProjectInfo projectInfo, CompletionItem... expectedItems)
			throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		PropertiesModel model = parse(value, fileURI);
		Position position = model.positionAt(offset);

		// Add snippet support for completion
		MicroProfileCompletionSettings completionSettings = new MicroProfileCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(snippetSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionSettings.setCapabilities(completionCapabilities);

		MicroProfileFormattingSettings formattingSettings = new MicroProfileFormattingSettings();
		formattingSettings.setSurroundEqualsWithSpaces(insertSpacing);

		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
		CompletionList list = languageService.doComplete(model, position, projectInfo, completionSettings,
				formattingSettings, () -> {
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
			Assert.assertEquals(DocumentationUtils.getDocumentationTextFromEither(expected.getDocumentation()),
					DocumentationUtils.getDocumentationTextFromEither(match.getDocumentation()));
		}

	}

	public static CompletionItem c(String label, String newText, Range range) {
		return c(label, newText, range, null);
	}

	public static CompletionItem c(String label, String newText, Range range, String documentation) {
		return c(label, new TextEdit(range, newText), null,
				documentation != null ? Either.forLeft(documentation) : null);
	}

	private static CompletionItem c(String label, TextEdit textEdit, String filterText,
			Either<String, MarkupContent> documentation) {
		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(filterText);
		item.setTextEdit(textEdit);
		item.setDocumentation(documentation);
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

	// ------------------- Snippet completion assert

	public static void assertCompletion(String value, TextDocumentSnippetRegistry registry, CompletionItem... expectedItems) {
		assertCompletion(value, null, registry, expectedItems);
	}

	public static void assertCompletion(String value, Integer expectedCount, TextDocumentSnippetRegistry registry,
			CompletionItem... expectedItems) {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);
		TextDocument document = new TextDocument(value, "application.properties");
		List<CompletionItem> items = registry.getCompletionItems(document, offset, true, context -> {
			return true;
		});
		CompletionList actual = new CompletionList(items);
		assertCompletions(actual, expectedCount, expectedItems);
	}
	
	public static void assertCompletionWithDependencies(String value, Integer expectedCount, String[] dependencies, CompletionItem... expectedItems) {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);
		TextDocumentSnippetRegistry registry = new TextDocumentSnippetRegistry(LanguageId.properties.name());
		TextDocument document = new TextDocument(value, "application.properties");
		List<CompletionItem> items = registry.getCompletionItems(document, offset, true, context -> {
			if (context instanceof SnippetContextForProperties) {
				SnippetContextForProperties contextProperties = (SnippetContextForProperties) context;
				return contextProperties.isMatch(new HashSet<>(Arrays.asList(dependencies)));
			}
			return false;
		});
		CompletionList actual = new CompletionList(items);
		assertCompletions(actual, expectedCount, expectedItems);
	}

	// ------------------- Hover assert

	public static void assertHoverMarkdown(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		MicroProfileHoverSettings hoverSettings = new MicroProfileHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultMicroProfileProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHoverPlaintext(String value, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		MicroProfileHoverSettings hoverSettings = new MicroProfileHoverSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.PLAINTEXT), false);
		hoverSettings.setCapabilities(capabilities);

		assertHover(value, null, getDefaultMicroProfileProjectInfo(), hoverSettings, expectedHoverLabel,
				expectedHoverOffset);
	}

	public static void assertHover(String value, String fileURI, MicroProfileProjectInfo projectInfo,
			MicroProfileHoverSettings hoverSettings, String expectedHoverLabel, Integer expectedHoverOffset)
			throws BadLocationException {

		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		PropertiesModel model = parse(value, fileURI);
		Position position = model.positionAt(offset);

		MicroProfileLanguageService languageService = new MicroProfileLanguageService();

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

		MicroProfileLanguageService languageService = new MicroProfileLanguageService();

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
		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
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

	// ------------------- Definition assert

	public static void testDefinitionFor(String value, LocationLink... expected)
			throws BadLocationException, InterruptedException, ExecutionException {
		testDefinitionFor(value, getDefaultMicroProfileProjectInfo(),
				getDefaultMicroProfilePropertyDefinitionProvider(), expected);
	}

	public static void testDefinitionFor(String value, MicroProfileProjectInfo projectInfo,
			MicroProfilePropertyDefinitionProvider definitionProvider, LocationLink... expected)
			throws BadLocationException, InterruptedException, ExecutionException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
		PropertiesModel document = parse(value, null);
		Position position = document.positionAt(offset);

		Either<List<? extends Location>, List<? extends LocationLink>> actual = languageService
				.findDefinition(document, position, projectInfo, definitionProvider, true).get();
		assertLocationLink(actual.getRight(), expected);

	}

	public static LocationLink ll(final String uri, final Range originRange, Range targetRange) {
		return new LocationLink(uri, targetRange, targetRange, originRange);
	}

	public static void assertLocationLink(List<? extends LocationLink> actual, LocationLink... expected) {
		Assert.assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			actual.get(i).setTargetUri(actual.get(i).getTargetUri().replaceAll("file:///", "file:/"));
			expected[i].setTargetUri(expected[i].getTargetUri().replaceAll("file:///", "file:/"));
		}
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- Diagnostics assert

	public static void testDiagnosticsFor(String value, Diagnostic... expected) {
		testDiagnosticsFor(value, getDefaultMicroProfileProjectInfo(), expected);
	}

	public static void testDiagnosticsFor(String value, MicroProfileProjectInfo projectInfo, Diagnostic... expected) {
		MicroProfileValidationSettings validationSettings = new MicroProfileValidationSettings();
		testDiagnosticsFor(value, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, MicroProfileProjectInfo projectInfo,
			MicroProfileValidationSettings validationSettings, Diagnostic... expected) {
		testDiagnosticsFor(value, null, null, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, Integer expectedCount, MicroProfileProjectInfo projectInfo,
			MicroProfileValidationSettings validationSettings, Diagnostic... expected) {
		testDiagnosticsFor(value, null, expectedCount, projectInfo, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, String fileURI, Integer expectedCount,
			MicroProfileProjectInfo projectInfo, MicroProfileValidationSettings validationSettings,
			Diagnostic... expected) {
		PropertiesModel model = parse(value, fileURI);
		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
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
				MICROPROFILE_DIAGNOSTIC_SOURCE, code.name());
	}

	// ------------------- CodeAction assert

	public static void testCodeActionsFor(String value, Diagnostic diagnostic, CodeAction... expected) {
		testCodeActionsFor(value, diagnostic, getDefaultMicroProfileProjectInfo(), new MicroProfileFormattingSettings(),
				expected);
	}

	public static void testCodeActionsFor(String value, Diagnostic diagnostic, MicroProfileProjectInfo projectInfo,
			CodeAction... expected) {
		testCodeActionsFor(value, Collections.singletonList(diagnostic), diagnostic.getRange(), projectInfo,
				new MicroProfileFormattingSettings(), expected);
	}

	public static void testCodeActionsFor(String value, Diagnostic diagnostic, MicroProfileProjectInfo projectInfo,
			MicroProfileFormattingSettings formattingSettings, CodeAction... expected) {
		testCodeActionsFor(value, Collections.singletonList(diagnostic), diagnostic.getRange(), projectInfo,
				formattingSettings, expected);
	}

	public static void testCodeActionsFor(String value, List<Diagnostic> diagnostics, Range range,
			MicroProfileProjectInfo projectInfo, CodeAction... expected) {
		testCodeActionsFor(value, diagnostics, range, projectInfo, new MicroProfileFormattingSettings(), expected);
	}

	public static void testCodeActionsFor(String value, List<Diagnostic> diagnostics, Range range,
			MicroProfileProjectInfo projectInfo, MicroProfileFormattingSettings formattingSettings,
			CodeAction... expected) {
		PropertiesModel model = parse(value, null);
		MicroProfileLanguageService languageService = new MicroProfileLanguageService();

		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(diagnostics);

		MicroProfileCommandCapabilities mpCommandCapabilities = new MicroProfileCommandCapabilities();

		List<String> valueSet = Arrays.asList(CommandKind.COMMAND_CONFIGURATION_UPDATE);
		CommandKindCapabilities commandKindCapabilities = new CommandKindCapabilities(valueSet);
		CommandCapabilities commandCapabilities = new CommandCapabilities(commandKindCapabilities);

		mpCommandCapabilities.setCapabilities(commandCapabilities);

		List<CodeAction> actual = languageService.doCodeActions(context, range, model, projectInfo, formattingSettings,
				mpCommandCapabilities);
		assertCodeActions(actual, expected);
	}

	public static void assertCodeActions(List<CodeAction> actual, CodeAction... expected) {
		actual.stream().forEach(ca -> {
			// we don't want to compare title, etc
			ca.setKind(null);
			if (ca.getDiagnostics() != null) {
				ca.getDiagnostics().forEach(d -> {
					d.setSeverity(null);
					d.setMessage("");
					d.setSource(null);
				});
			}
		});

		Assert.assertEquals(expected.length, actual.size());
		Assert.assertArrayEquals(expected, actual.toArray());
	}

	public static CodeAction ca(String title, TextEdit te, Diagnostic... d) {
		return ca(title, te, null, d);
	}

	public static CodeAction ca(String title, Command command, Diagnostic... d) {
		return ca(title, null, command, d);
	}

	public static CodeAction ca(String title, TextEdit te, Command command, Diagnostic... d) {
		List<Diagnostic> diagnostics = new ArrayList<>();
		for (int i = 0; i < d.length; i++) {
			diagnostics.add(d[i]);
		}
		return ca(title, te, command, diagnostics);
	}

	public static CodeAction ca(String title, TextEdit te, Command command, List<Diagnostic> diagnostics) {
		CodeAction codeAction = new CodeAction();
		codeAction.setTitle(title);
		codeAction.setDiagnostics(diagnostics);
		codeAction.setCommand(command);

		if (te != null) {
			VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(
					"application.properties", 0);
			TextDocumentEdit textDocumentEdit = new TextDocumentEdit(versionedTextDocumentIdentifier,
					Collections.singletonList(te));
			WorkspaceEdit workspaceEdit = new WorkspaceEdit(
					Collections.singletonList(Either.forLeft(textDocumentEdit)));
			codeAction.setEdit(workspaceEdit);
		}

		return codeAction;
	}

	public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(newText);
		textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
		return textEdit;
	}

	// ------------------- Formatting assert

	public static void assertFormat(String value, String expected, boolean insertSpaces) {
		MicroProfileFormattingSettings formattingSettings = new MicroProfileFormattingSettings();
		formattingSettings.setSurroundEqualsWithSpaces(insertSpaces);
		assertFormat(value, expected, formattingSettings);
	}

	public static void assertFormat(String value, String expected, MicroProfileFormattingSettings formattingSettings) {

		PropertiesModel model = parse(value, null);
		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
		List<? extends TextEdit> edits = languageService.doFormat(model, formattingSettings);

		String formatted = edits.stream().map(edit -> edit.getNewText()).collect(Collectors.joining(""));
		Assert.assertEquals(expected, formatted);
	}

	public static void assertRangeFormat(String value, String expected, boolean insertSpaces)
			throws BadLocationException {
		MicroProfileFormattingSettings formattingSettings = new MicroProfileFormattingSettings();
		formattingSettings.setSurroundEqualsWithSpaces(insertSpaces);
		assertRangeFormat(value, expected, formattingSettings);
	}

	public static void assertRangeFormat(String value, String expected,
			MicroProfileFormattingSettings formattingSettings) throws BadLocationException {

		int startOffset = value.indexOf("|");
		value = value.substring(0, startOffset) + value.substring(startOffset + 1);
		int endOffset = value.indexOf("|");
		value = value.substring(0, endOffset) + value.substring(endOffset + 1);
		TextDocument document = new TextDocument(value, "application.properties");
		Range range = PositionUtils.createRange(startOffset, endOffset, document);

		PropertiesModel model = parse(value, null);
		MicroProfileLanguageService languageService = new MicroProfileLanguageService();
		List<? extends TextEdit> edits = languageService.doRangeFormat(model, range, formattingSettings);

		Range formatRange = edits.get(0).getRange();
		int formatStart = document.offsetAt(formatRange.getStart());
		int formatEnd = document.offsetAt(formatRange.getEnd());

		String formatted = value.substring(0, formatStart)
				+ edits.stream().map(edit -> edit.getNewText()).collect(Collectors.joining(""))
				+ value.substring(formatEnd);
		Assert.assertEquals(expected, formatted);
	}

	private static PropertiesModel parse(String text, String uri) {
		TextDocument document = new TextDocument(text, uri != null ? uri : "application.properties");
		return PropertiesModel.parse(document);
	}

}