/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute;

import static com.redhat.qute.project.QuteQuickStartProject.PROJECT_URI;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemInsertTextModeSupportCapabilities;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverCapabilities;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintLabelPart;
import org.eclipse.lsp4j.InsertTextMode;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.ls.api.QuteTemplateJavaTextEditProvider;
import com.redhat.qute.ls.commons.BadLocationException;
import com.redhat.qute.ls.commons.TextDocument;
import com.redhat.qute.ls.commons.client.CommandCapabilities;
import com.redhat.qute.ls.commons.client.CommandKindCapabilities;
import com.redhat.qute.ls.commons.client.ConfigurationItemEdit;
import com.redhat.qute.ls.commons.client.ConfigurationItemEditType;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.MockQuteLanguageServer;
import com.redhat.qute.project.MockQuteProjectRegistry;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.QuteLanguageService;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.services.commands.QuteClientCommandConstants;
import com.redhat.qute.services.commands.QuteSurroundWithCommandHandler;
import com.redhat.qute.services.commands.QuteSurroundWithCommandHandler.SurroundWithKind;
import com.redhat.qute.services.commands.QuteSurroundWithCommandHandler.SurroundWithResponse;
import com.redhat.qute.services.diagnostics.IQuteErrorCode;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteInlayHintSettings;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.settings.SharedSettings;
import com.redhat.qute.utils.StringUtils;

/**
 * Qute Assert
 *
 * @author Angelo ZERR
 *
 */
public class QuteAssert {

	private static final String QUTE_SOURCE = "qute";

	public static final String TEMPLATE_BASE_DIR = "src/test/resources/templates";

	private static final String FILE_URI = "test.qute";

	public static final int USER_TAG_SIZE = 7 /*
												 * #input, #form, #title, #simpleTitle, #user, #formElement,
												 * #inputRequired
												 */;

	public static final int SECTION_SNIPPET_SIZE = 15 /* #each, #for, ... #fragment ... */ + USER_TAG_SIZE;

	public static String getFileUri(String templateFile) {
		return Paths.get(TEMPLATE_BASE_DIR + templateFile).toUri().toString();
	}
	// ------------------- Completion assert

	public static void testCompletionFor(String value, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, false, expectedItems);
	}

	public static void testCompletionFor(String value, String fileUri, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, false, fileUri, PROJECT_URI, TEMPLATE_BASE_DIR, null, expectedItems);
	}

	public static void testCompletionFor(String value, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, true, expectedCount, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, snippetSupport, null, expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, snippetSupport, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expectedCount,
				expectedItems);
	}

	public static void testCompletionFor(String value, String fileUri, boolean snippetSupport, Integer expectedCount,
			CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, snippetSupport, fileUri, PROJECT_URI, TEMPLATE_BASE_DIR, expectedCount,
				expectedItems);
	}

	public static void testCompletionFor(String value, String fileUri, String templateId, Integer expectedCount,
			CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, false, fileUri, templateId, PROJECT_URI, TEMPLATE_BASE_DIR, expectedCount,
				expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileUri, String projectUri,
			String templateBaseDir, Integer expectedCount, CompletionItem... expectedItems) throws Exception {
		testCompletionFor(value, snippetSupport, fileUri, null, projectUri, templateBaseDir, expectedCount,
				expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileUri, String templateId,
			String projectUri, String templateBaseDir, Integer expectedCount, CompletionItem... expectedItems)
			throws Exception {
		testCompletionFor(value, snippetSupport, fileUri, templateId, projectUri, templateBaseDir, expectedCount,
				new QuteNativeSettings(), expectedItems);
	}

	public static void testCompletionFor(String value, boolean snippetSupport, String fileUri, String templateId,
			String projectUri, String templateBaseDir, Integer expectedCount, QuteNativeSettings nativeImagesSettings,
			CompletionItem... expectedItems) throws Exception {

		// Add snippet support for completion
		QuteCompletionSettings completionSettings = new QuteCompletionSettings();
		CompletionItemCapabilities completionItemCapabilities = new CompletionItemCapabilities();
		completionItemCapabilities.setSnippetSupport(snippetSupport);
		CompletionItemInsertTextModeSupportCapabilities insertTextModeSupport = new CompletionItemInsertTextModeSupportCapabilities();
		insertTextModeSupport.setValueSet(Arrays.asList(InsertTextMode.AsIs, InsertTextMode.AdjustIndentation));
		completionItemCapabilities.setInsertTextModeSupport(insertTextModeSupport);
		CompletionCapabilities completionCapabilities = new CompletionCapabilities(completionItemCapabilities);
		completionCapabilities.setInsertTextMode(InsertTextMode.AdjustIndentation);
		completionSettings.setCapabilities(completionCapabilities);

		testCompletionFor(value, fileUri, templateId, projectUri, templateBaseDir, expectedCount, nativeImagesSettings,
				completionSettings, expectedItems);
	}

	public static void testCompletionFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, Integer expectedCount, QuteNativeSettings nativeImagesSettings,
			QuteCompletionSettings completionSettings, CompletionItem... expectedItems) throws Exception {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		template.setTemplateId(templateId);

		Position position = template.positionAt(offset);

		QuteFormattingSettings formattingSettings = new QuteFormattingSettings();

		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));
		CompletionList list = languageService
				.doComplete(template, position, completionSettings, formattingSettings, nativeImagesSettings, () -> {
				}).get();

		// no duplicate labels
		List<String> labels = list.getItems().stream().map(i -> i.getLabel()).sorted().collect(Collectors.toList());
		String previous = null;
		for (String label : labels) {
			if (expectedCount != null) {
				continue;
			}
			assertNotEquals(previous, label, () -> {
				return "Duplicate label " + label + " in " + labels.stream().collect(Collectors.joining(",")) + "}";
			});
			previous = label;
		}
		if (expectedCount != null) {
			assertEquals(expectedCount.intValue(), list.getItems().size());
		}
		if (expectedItems != null) {
			for (CompletionItem item : expectedItems) {
				assertCompletion(list, item, expectedCount);
			}
		}
	}

	public static void assertCompletion(CompletionList completions, CompletionItem expected, Integer expectedCount) {
		List<CompletionItem> matches = completions.getItems().stream().filter(completion -> {
			return expected.getLabel().equals(completion.getLabel());
		}).collect(Collectors.toList());

		if (expectedCount != null) {
			assertTrue(matches.size() >= 1, () -> {
				return expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(","));
			});
		} else {
			assertEquals(1, matches.size(), () -> {
				return expected.getLabel() + " should only exist once: Actual: "
						+ completions.getItems().stream().map(c -> c.getLabel()).collect(Collectors.joining(","));
			});
		}

		CompletionItem match = getCompletionMatch(matches, expected);
		if (expected.getTextEdit() != null && match.getTextEdit() != null) {
			if (expected.getTextEdit().getLeft().getNewText() != null) {
				assertEquals(expected.getTextEdit().getLeft().getNewText(), match.getTextEdit().getLeft().getNewText());
			}
			Range r = expected.getTextEdit().getLeft().getRange();
			if (r != null && r.getStart() != null && r.getEnd() != null) {
				assertEquals(expected.getTextEdit().getLeft().getRange(), match.getTextEdit().getLeft().getRange());
			}
		}
		if (expected.getFilterText() != null && match.getFilterText() != null) {
			assertEquals(expected.getFilterText(), match.getFilterText());
		}

		if (expected.getDocumentation() != null) {
			assertEquals(expected.getDocumentation(), match.getDocumentation());
		}
		assertEquals(expected.getInsertTextMode(), match.getInsertTextMode());

	}

	private static CompletionItem getCompletionMatch(List<CompletionItem> matches, CompletionItem expected) {
		for (CompletionItem item : matches) {
			if (expected.getTextEdit().getLeft().getNewText().equals(item.getTextEdit().getLeft().getNewText())) {
				return item;
			}
		}
		return matches.get(0);
	}

	public static CompletionItem c(String newText, Range range) {
		return c(newText, newText, range);
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
		item.setTextEdit(Either.forLeft(textEdit));
		item.setDocumentation(documentation);
		return item;
	}

	// ------------------- Diagnostics assert

	public static void testDiagnosticsFor(String value, Diagnostic... expected) {
		testDiagnosticsFor(value, FILE_URI, expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, Diagnostic... expected) {
		testDiagnosticsFor(value, fileUri, null, expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, String templateId, Diagnostic... expected) {
		testDiagnosticsFor(value, fileUri, templateId, PROJECT_URI, TEMPLATE_BASE_DIR, false, expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, boolean filter, Diagnostic... expected) {
		testDiagnosticsFor(value, fileUri, templateId, projectUri, templateBaseDir, filter, null, expected);
	}

	public static void testDiagnosticsFor(String value, QuteValidationSettings validationSettings,
			Diagnostic... expected) {
		testDiagnosticsFor(value, FILE_URI, null, PROJECT_URI, TEMPLATE_BASE_DIR, false, validationSettings, expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, boolean filter, QuteValidationSettings validationSettings, Diagnostic... expected) {
		testDiagnosticsFor(value, fileUri, templateId, projectUri, templateBaseDir, filter, validationSettings,
				new QuteNativeSettings(), expected);
	}

	public static void testDiagnosticsFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, boolean filter, QuteValidationSettings validationSettings,
			QuteNativeSettings nativeImagesSettings, Diagnostic... expected) {
		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		template.setTemplateId(templateId);

		JavaDataModelCache javaCache = new JavaDataModelCache(projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		List<Diagnostic> actual = languageService.doDiagnostics(template, validationSettings, nativeImagesSettings,
				new ResolvingJavaTypeContext(template, javaCache), () -> {
				});
		if (expected == null) {
			assertTrue(actual.isEmpty());
			return;
		}
		assertDiagnostics(actual, Arrays.asList(expected), filter);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, Diagnostic... expected) {
		assertDiagnostics(actual, Arrays.asList(expected), false);
	}

	public static void assertDiagnostics(List<Diagnostic> actual, List<Diagnostic> expected, boolean filter) {
		List<Diagnostic> received = actual;
		final boolean filterMessage;
		if (expected != null && !expected.isEmpty() && !StringUtils.isEmpty(expected.get(0).getMessage())) {
			filterMessage = true;
		} else {
			filterMessage = false;
		}
		if (filter) {
			received = actual.stream().map(d -> {
				Diagnostic simpler = new Diagnostic(d.getRange(), "");
				if (d.getCode() != null && !StringUtils.isEmpty(d.getCode().getLeft())) {
					simpler.setCode(d.getCode());
				}
				if (filterMessage) {
					simpler.setMessage(d.getMessage());
				}
				return simpler;
			}).collect(Collectors.toList());
		}
		// Don't compare message of diagnosticRelatedInformation
		for (Diagnostic diagnostic : received) {
			List<DiagnosticRelatedInformation> diagnosticRelatedInformations = diagnostic.getRelatedInformation();
			if (diagnosticRelatedInformations != null) {
				for (DiagnosticRelatedInformation diagnosticRelatedInformation : diagnosticRelatedInformations) {
					diagnosticRelatedInformation.setMessage("");
				}
			}
		}
		assertIterableEquals(expected, received, "Unexpected diagnostics:\n" + actual);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code) {
		return d(startLine, startCharacter, endLine, endCharacter, code, "");
	}

	public static Diagnostic d(int startLine, int startCharacter, int endCharacter, IQuteErrorCode code) {
		// Diagnostic on 1 line
		return d(startLine, startCharacter, startLine, endCharacter, code);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message) {
		return d(startLine, startCharacter, endLine, endCharacter, code, message, null);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message, DiagnosticSeverity severity) {
		return d(startLine, startCharacter, endLine, endCharacter, code, message, QUTE_SOURCE, severity);
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message, Object data, DiagnosticSeverity severity) {
		Diagnostic diagnostic = d(startLine, startCharacter, endLine, endCharacter, code, message, QUTE_SOURCE,
				severity);
		diagnostic.setData(data);
		return diagnostic;
	}

	public static Diagnostic d(int startLine, int startCharacter, int endLine, int endCharacter, IQuteErrorCode code,
			String message, String source, DiagnosticSeverity severity) {
		// Diagnostic on 1 line
		return new Diagnostic(r(startLine, startCharacter, endLine, endCharacter), message, severity, source,
				code != null ? code.getCode() : null);
	}

	// ------------------- Hover assert

	public static void assertHover(String value) throws Exception {
		assertHover(value, null, null);
	}

	public static void assertHover(String value, String expectedHoverLabel, Range expectedHoverRange) throws Exception {
		assertHover(value, null, expectedHoverLabel, expectedHoverRange);
	}

	public static void assertHover(String value, String fileURI, String expectedHoverLabel, Range expectedHoverRange)
			throws Exception {
		assertHover(value, fileURI, null, expectedHoverLabel, expectedHoverRange);
	}

	public static void assertHover(String value, String fileURI, String templateId, String expectedHoverLabel,
			Range expectedHoverRange) throws Exception {
		SharedSettings sharedSettings = new SharedSettings();
		HoverCapabilities capabilities = new HoverCapabilities(Arrays.asList(MarkupKind.MARKDOWN), false);
		sharedSettings.getHoverSettings().setCapabilities(capabilities);
		assertHover(value, fileURI, templateId, PROJECT_URI, TEMPLATE_BASE_DIR, expectedHoverLabel, expectedHoverRange,
				sharedSettings);
	}

	private static void assertHover(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, String expectedHoverLabel, Range expectedHoverRange, SharedSettings sharedSettings)
			throws Exception {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		template.setTemplateId(templateId);

		Position position = template.positionAt(offset);

		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));
		Hover hover = languageService.doHover(template, position, sharedSettings, () -> {
		}).get();
		if (expectedHoverLabel == null) {
			assertNull(hover);
		} else {
			String actualHoverLabel = getHoverLabel(hover);
			assertEquals(expectedHoverLabel, actualHoverLabel);
			if (expectedHoverRange != null) {
				assertEquals(expectedHoverRange, hover.getRange());
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

	// ------------------- Definition assert

	public static void testDefinitionFor(String value, LocationLink... expected) throws Exception {
		testDefinitionFor(value, null, expected);
	}

	public static void testDefinitionFor(String value, String fileURI, LocationLink... expected) throws Exception {
		testDefinitionFor(value, fileURI, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testDefinitionFor(String value, String fileUri, String projectUri, String templateBaseDir,
			LocationLink... expected) throws Exception {
		int offset = value.indexOf("|");
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);

		Position position = template.positionAt(offset);

		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		List<? extends LocationLink> actual = languageService.findDefinition(template, position, () -> {
		}).get();
		assertLocationLink(actual, expected);

	}

	public static LocationLink ll(final String uri, final Range originRange, Range targetRange) {
		return new LocationLink(uri, targetRange, targetRange, originRange);
	}

	public static void assertLocationLink(List<? extends LocationLink> actual, LocationLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			actual.get(i).setTargetUri(actual.get(i).getTargetUri().replace("file:///", "file:/"));
			expected[i].setTargetUri(expected[i].getTargetUri().replace("file:///", "file:/"));
		}
		assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- Links assert

	public static void testDocumentLinkFor(String value, String fileUri, DocumentLink... expected) throws Exception {
		testDocumentLinkFor(value, fileUri, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testDocumentLinkFor(String value, String fileUri, String projectUri, String templateBaseDir,
			DocumentLink... expected) throws Exception {

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		List<DocumentLink> actual = languageService.findDocumentLinks(template, () -> {
		}).get();
		assertDocumentLinks(actual, expected);
	}

	public static DocumentLink dl(Range range, String target) {
		return new DocumentLink(range, target);
	}

	public static void assertDocumentLinks(List<DocumentLink> actual, DocumentLink... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange(), " Range test '" + i + "' link");
			assertEquals(Paths.get(expected[i].getTarget()).toUri().toString().replace("file:///", "file:/"),
					actual.get(i).getTarget().replace("file:///", "file:/"), " Target test '" + i + "' link");
		}
	}

	// ------------------- Highlights assert

	public static void testHighlightsFor(String value, DocumentHighlight... expected) throws BadLocationException {
		testHighlightsFor(value, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testHighlightsFor(String value, String fileUri, String projectUri, String templateBaseDir,
			DocumentHighlight... expected) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		Position position = template.positionAt(offset);
		List<? extends DocumentHighlight> actual = languageService.findDocumentHighlights(template, position, () -> {
		});
		assertDocumentHighlight(actual, expected);
	}

	public static void assertDocumentHighlight(List<? extends DocumentHighlight> actual,
			DocumentHighlight... expected) {
		assertEquals(expected.length, actual.size());
		assertArrayEquals(expected, actual.toArray());
	}

	public static DocumentHighlight hl(Range range) {
		return hl(range, DocumentHighlightKind.Read);
	}

	public static DocumentHighlight hl(Range range, DocumentHighlightKind kind) {
		return new DocumentHighlight(range, kind);
	}

	// ------------------- CodeLens assert

	public static void testCodeLensFor(String value, String fileUri, CodeLens... expected) throws Exception {
		testCodeLensFor(value, fileUri, null, expected);
	}

	public static void testCodeLensFor(String value, String fileUri, String templateId, CodeLens... expected)
			throws Exception {
		testCodeLensFor(value, fileUri, templateId, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	private static void testCodeLensFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, CodeLens... expected) throws Exception {
		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		template.setTemplateId(templateId);

		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));
		SharedSettings sharedSettings = createSharedSettings();
		List<? extends CodeLens> actual = languageService.getCodeLens(template, sharedSettings, () -> {
		}).get();
		assertCodeLens(actual, expected);
	}

	public static CodeLens cl(Range range, String title, String command) {
		return new CodeLens(range, new Command(title, command), null);
	}

	public static void assertCodeLens(List<? extends CodeLens> actual, CodeLens... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getRange(), actual.get(i).getRange());
			Command expectedCommand = expected[i].getCommand();
			Command actualCommand = actual.get(i).getCommand();
			if (expectedCommand != null && actualCommand != null) {
				assertEquals(expectedCommand.getTitle(), actualCommand.getTitle());
				assertEquals(expectedCommand.getCommand(), actualCommand.getCommand());
			}
			assertEquals(expected[i].getData(), actual.get(i).getData());
		}
	}

	// ------------------- InlayHint assert

	public static void testInlayHintFor(String value, InlayHint... expected) throws Exception {
		testInlayHintFor(value, null, expected);
	}

	public static void testInlayHintFor(String value, QuteInlayHintSettings inlayHintSettings, InlayHint... expected)
			throws Exception {
		testInlayHintFor(value, FILE_URI, null, PROJECT_URI, TEMPLATE_BASE_DIR, inlayHintSettings, expected);
	}

	private static void testInlayHintFor(String value, String fileUri, String templateId, String projectUri,
			String templateBaseDir, QuteInlayHintSettings inlayHintSettings, InlayHint... expected) throws Exception {
		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		template.setTemplateId(templateId);

		SharedSettings settings = createSharedSettings();
		if (inlayHintSettings != null) {
			settings.getInlayHintSettings().update(inlayHintSettings);
		}

		JavaDataModelCache javaCache = new JavaDataModelCache(projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(javaCache);
		Range range = null;
		List<InlayHint> actual = languageService
				.getInlayHint(template, range, settings, new ResolvingJavaTypeContext(template, javaCache), () -> {
				}).get();
		assertInlayHint(actual, expected);
	}

	public static InlayHint ih(Position position, String label) {
		return new InlayHint(position, Either.forLeft(label));
	}

	public static InlayHint ih(Position position, InlayHintLabelPart... parts) {
		return new InlayHint(position, Either.forRight(Arrays.asList(parts)));
	}

	public static InlayHintLabelPart ihLabel(String label) {
		return new InlayHintLabelPart(label);
	}

	public static InlayHintLabelPart ihLabel(String label, String tooltip, Command command) {
		InlayHintLabelPart part = ihLabel(label);
		part.setCommand(command);
		part.setTooltip(tooltip);
		return part;
	}

	public static void assertInlayHint(List<? extends InlayHint> actual, InlayHint... expected) {
		assertEquals(expected.length, actual.size());
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].getPosition(), actual.get(i).getPosition(), "position at " + i);
			assertEquals(expected[i].getLabel(), actual.get(i).getLabel(), "label at " + i);
		}
	}

	// ------------------- CodeAction assert

	public static void testCodeActionsFor(String value, Diagnostic diagnostic, CodeAction... expected)
			throws Exception {
		testCodeActionsFor(value, diagnostic, new SharedSettings(), expected);
	}

	public static void testCodeActionsFor(String value, Diagnostic diagnostic, SharedSettings settings,
			CodeAction... expected) throws Exception {
		testCodeActionsFor(value, diagnostic, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, settings, expected);
	}

	private static void testCodeActionsFor(String value, Diagnostic diagnostic, String fileUri, String projectUri,
			String templateBaseDir, SharedSettings settings, CodeAction... expected) throws Exception {
		int offset = value.indexOf('|');
		Range range = null;

		if (offset != -1) {
			value = value.substring(0, offset) + value.substring(offset + 1);
		}
		TextDocument document = new TextDocument(value.toString(), FILE_URI);

		if (offset != -1) {
			Position position = document.positionAt(offset);
			range = new Range(position, position);
		} else {
			range = diagnostic.getRange();
		}

		CodeActionContext context = new CodeActionContext();
		context.setDiagnostics(Arrays.asList(diagnostic));

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		List<CodeAction> actual = languageService
				.doCodeActions(template, context, new QuteTemplateJavaTextEditProvider() {
					// do not attempt to resolve "generate missing java member" code actions in
					// qute-ls unit tests
				}, range, settings).get();
		assertCodeActions(actual, expected);
	}

	public static void assertCodeActions(List<CodeAction> actual, CodeAction... expected) {
		actual.stream().forEach(ca -> {
			// we don't want to compare title, etc
			ca.setKind(null);
			ca.setTitle("");
			if (ca.getDiagnostics() != null) {
				ca.getDiagnostics().forEach(d -> {
					d.setSeverity(null);
					d.setMessage("");
					d.setSource(null);
				});
			}
		});

		assertEquals(expected.length, actual.size());
		assertArrayEquals(expected, actual.toArray());
	}

	public static CodeAction ca(Diagnostic d, TextEdit... te) {
		CodeAction codeAction = new CodeAction();
		codeAction.setTitle("");
		codeAction.setDiagnostics(Arrays.asList(d));

		TextDocumentEdit textDocumentEdit = tde(FILE_URI, 0, te);
		WorkspaceEdit workspaceEdit = new WorkspaceEdit(Collections.singletonList(Either.forLeft(textDocumentEdit)));
		codeAction.setEdit(workspaceEdit);
		return codeAction;
	}

	/**
	 * Mock code action for creating a command code action
	 */
	public static CodeAction ca(Diagnostic d, Command c) {
		CodeAction codeAction = new CodeAction();
		codeAction.setTitle("");
		codeAction.setDiagnostics(Arrays.asList(d));
		codeAction.setCommand(c);
		return codeAction;
	}

	public static CodeAction cad(Diagnostic d, Object data) {
		CodeAction codeAction = new CodeAction("");
		codeAction.setDiagnostics(Arrays.asList(d));
		codeAction.setData(data);
		return codeAction;
	}

	public static Command c(String title, String commandId, String section, String scopeUri,
			ConfigurationItemEditType edit, Object value, Diagnostic d) {
		Command command = new Command();
		command.setTitle(title);
		command.setCommand(commandId);
		ConfigurationItemEdit itemEdit = new ConfigurationItemEdit(section, edit, value);
		itemEdit.setScopeUri(scopeUri);
		command.setArguments(Collections.singletonList(itemEdit));
		return command;
	}

	public static TextDocumentEdit tde(String uri, int version, TextEdit... te) {
		VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier(uri,
				version);
		return new TextDocumentEdit(versionedTextDocumentIdentifier, Arrays.asList(te));
	}

	public static CodeAction ca(Diagnostic d, Either<TextDocumentEdit, ResourceOperation>... ops) {
		CodeAction codeAction = new CodeAction();
		codeAction.setDiagnostics(Collections.singletonList(d));
		codeAction.setEdit(new WorkspaceEdit(Arrays.asList(ops)));
		codeAction.setTitle("");
		return codeAction;
	}

	public static TextEdit te(int startLine, int startCharacter, int endLine, int endCharacter, String newText) {
		TextEdit textEdit = new TextEdit();
		textEdit.setNewText(newText);
		textEdit.setRange(r(startLine, startCharacter, endLine, endCharacter));
		return textEdit;
	}

	public static Either<TextDocumentEdit, ResourceOperation> createFile(String uri, boolean overwrite) {
		CreateFileOptions options = new CreateFileOptions();
		options.setIgnoreIfExists(!overwrite);
		options.setOverwrite(overwrite);
		return Either.forRight(new CreateFile(uri, options));
	}

	public static Either<TextDocumentEdit, ResourceOperation> teOp(String uri, int startLine, int startChar,
			int endLine, int endChar, String newText) {
		return Either.forLeft(new TextDocumentEdit(new VersionedTextDocumentIdentifier(uri, 0),
				Collections.singletonList(te(startLine, startChar, endLine, endChar, newText))));
	}

	// ------------------- Reference assert

	public static void testReferencesFor(String value, Location... expected) throws BadLocationException {
		testReferencesFor(value, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testReferencesFor(String value, String fileUri, String projectUri, String templateBaseDir,
			Location... expected) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		Position position = template.positionAt(offset);
		List<? extends Location> actual = languageService.findReferences(template, position, new ReferenceContext(),
				() -> {
				});
		assertLocation(actual, expected);

	}

	public static Location l(final String uri, final Range range) {
		return new Location(uri, range);
	}

	public static void assertLocation(List<? extends Location> actual, Location... expected) {
		assertEquals(expected.length, actual.size());
		assertArrayEquals(expected, actual.toArray());
	}

	// ------------------- Rename assert

	public static void assertRename(String value, String newText) throws BadLocationException {
		assertRename(value, newText, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, Collections.emptyList());
	}

	public static void assertRename(String value, String newText, List<TextEdit> expectedEdits)
			throws BadLocationException {
		assertRename(value, newText, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expectedEdits);
	}

	public static void assertRename(String value, String newText, String fileUri, String projectUri,
			String templateBaseDir, List<TextEdit> expectedEdits) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		Position position = template.positionAt(offset);

		WorkspaceEdit workspaceEdit = languageService.doRename(template, position, newText, () -> {
		});
		if (workspaceEdit == null) {
			assertArrayEquals(expectedEdits.toArray(), Collections.emptyList().toArray());
		} else {
			List<TextEdit> actualEdits = workspaceEdit.getChanges().get(FILE_URI);
			assertArrayEquals(expectedEdits.toArray(), actualEdits.toArray());
		}
	}

	public static List<TextEdit> edits(String newText, Range... ranges) {
		return Stream.of(ranges) //
				.map(r -> new TextEdit(r, newText)) //
				.collect(Collectors.toList());
	}

	// ------------------- Linked Editing assert

	public static void testLinkedEditingFor(String value, LinkedEditingRanges expected) throws BadLocationException {
		testLinkedEditingFor(value, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testLinkedEditingFor(String value, String fileUri, String projectUri, String templateBaseDir,
			LinkedEditingRanges expected) throws BadLocationException {
		int offset = value.indexOf('|');
		value = value.substring(0, offset) + value.substring(offset + 1);

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		Position position = template.positionAt(offset);
		LinkedEditingRanges actual = languageService.findLinkedEditingRanges(template, position, () -> {
		});
		assertLinkedEditing(actual, expected);
	}

	public static void assertLinkedEditing(LinkedEditingRanges actual, LinkedEditingRanges expected) {
		if (expected == null) {
			assertNull(actual);
		} else {
			assertNotNull(actual);
			assertEquals(expected.getWordPattern(), actual.getWordPattern());
			assertEquals(expected.getRanges(), actual.getRanges());
		}
	}

	public static LinkedEditingRanges le(Range... ranges) {
		return new LinkedEditingRanges(Arrays.asList(ranges));
	}

	// ------------------- DocumentSymbol assert

	public static void testDocumentSymbolsFor(String value, DocumentSymbol expected) throws BadLocationException {
		testDocumentSymbolsFor(value, FILE_URI, PROJECT_URI, TEMPLATE_BASE_DIR, expected);
	}

	public static void testDocumentSymbolsFor(String value, String fileUri, String projectUri, String templateBaseDir,
			DocumentSymbol... expected) throws BadLocationException {

		QuteProjectRegistry projectRegistry = new MockQuteProjectRegistry();
		Template template = createTemplate(value, fileUri, projectUri, templateBaseDir, projectRegistry);
		QuteLanguageService languageService = new QuteLanguageService(new JavaDataModelCache(projectRegistry));

		List<DocumentSymbol> actual = languageService.findDocumentSymbols(template, () -> {
		});
		assertDocumentSymbols(actual, expected);

	}

	public static DocumentSymbol ds(final String name, final SymbolKind kind, final Range range,
			final Range selectionRange, final String detail, final List<DocumentSymbol> children) {
		return new DocumentSymbol(name, kind, range, selectionRange, detail, children);
	}

	public static void assertDocumentSymbols(List<DocumentSymbol> actual, DocumentSymbol... expected) {
		assertEquals(expected.length, actual.size());
		assertArrayEquals(expected, actual.toArray());
	}

	// Surround With Assert
	public static void assertSurroundWith(String template, SurroundWithKind kind, boolean snippetsSupported,
			TextEdit... te) throws Exception {
		assertSurroundWith(template, kind, snippetsSupported, TEMPLATE_BASE_DIR + "/" + FILE_URI, Arrays.asList(te));
	}

	public static void assertSurroundWith(String template, SurroundWithKind kind, boolean snippetsSupported,
			String uri, List<TextEdit> expected) throws Exception {
		MockQuteLanguageServer languageServer = new MockQuteLanguageServer();
		int rangeStart = template.indexOf('|');
		int rangeEnd = template.lastIndexOf('|');
		// remove '|'
		StringBuilder x = new StringBuilder(template.substring(0, rangeStart));
		if (rangeEnd > rangeStart) {
			x.append(template.substring(rangeStart + 1, rangeEnd));
		}
		x.append(template.substring(Math.min(rangeEnd + 1, template.length())));

		TextDocument document = new TextDocument(x.toString(), "");
		Position startPos = document.positionAt(rangeStart);
		Position endPos = rangeStart == rangeEnd ? startPos : document.positionAt(rangeEnd - 1);
		Range selection = new Range(startPos, endPos);

		TextDocumentIdentifier templateIdentifier = languageServer.didOpen(uri, template);

		QuteSurroundWithCommandHandler command = new QuteSurroundWithCommandHandler(languageServer);

		// Execute surround with tags command
		ExecuteCommandParams params = new ExecuteCommandParams(QuteSurroundWithCommandHandler.COMMAND_ID,
				Arrays.asList(templateIdentifier, selection, kind.name(),
						snippetsSupported));
		SurroundWithResponse response = (SurroundWithResponse) command.executeCommand(params, new SharedSettings(), //
				() -> {
				}).get();
		List<TextEdit> actual = Arrays.asList(response.getStart(), response.getEnd());
		assertEquals(expected.size(), actual.size());
		assertArrayEquals(expected.toArray(), actual.toArray());
	}

	// ------------------- Utilities

	public static Range r(int line, int startChar, int endChar) {
		return r(line, startChar, line, endChar);
	}

	public static Range r(int startLine, int startChar, int endLine, int endChar) {
		Position start = p(startLine, startChar);
		Position end = p(endLine, endChar);
		return new Range(start, end);
	}

	public static Position p(int line, int character) {
		return new Position(line, character);
	}

	private static Template createTemplate(String value, String fileUri, String projectUri, String templateBaseDir,
			QuteProjectRegistry projectRegistry) {
		Template template = TemplateParser.parse(value, fileUri != null ? fileUri : FILE_URI);
		template.setProjectUri(projectUri);
		projectRegistry.getProject(new ProjectInfo(projectUri, templateBaseDir));
		template.setProjectRegistry(projectRegistry);
		return template;
	}

	private static SharedSettings createSharedSettings() {
		SharedSettings sharedSettings = new SharedSettings();
		CommandCapabilities commandCapabilities = new CommandCapabilities();
		CommandKindCapabilities kinds = new CommandKindCapabilities(
				Arrays.asList(QuteClientCommandConstants.COMMAND_JAVA_DEFINITION));
		commandCapabilities.setCommandKind(kinds);
		sharedSettings.getCommandCapabilities().setCapabilities(commandCapabilities);
		return sharedSettings;
	}

}
