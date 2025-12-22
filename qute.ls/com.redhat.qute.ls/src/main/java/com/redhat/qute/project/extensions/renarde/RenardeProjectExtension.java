/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.extensions.renarde;

import static com.redhat.qute.services.diagnostics.DiagnosticDataFactory.createDiagnostic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.expression.Parts.PartKind;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.extensions.ProjectExtension;
import com.redhat.qute.project.extensions.renarde.MessagesFileInfo.MessagesFileName;
import com.redhat.qute.services.ResolvingJavaTypeContext;
import com.redhat.qute.services.completions.CompletionRequest;
import com.redhat.qute.settings.QuteCompletionSettings;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteValidationSettings;
import com.redhat.qute.utils.DocumentationUtils;
import com.redhat.qute.utils.QutePositionUtility;

/**
 * Qute language server extension for Renarde framework support.
 * 
 * <p>
 * Provides language features for the Renarde {@code m:} namespace, which is
 * used for accessing internationalized messages from properties files.
 * </p>
 * 
 * <h2>Features</h2>
 * <ul>
 * <li><b>Completion:</b> Suggests message keys from messages.properties
 * files</li>
 * <li><b>Validation:</b> Reports errors for unknown message keys</li>
 * <li><b>Navigation:</b> Go to definition in messages.properties files</li>
 * <li><b>Hover:</b> Shows message values when hovering over keys</li>
 * <li><b>Inlay Hints:</b> Displays message values inline in the editor</li>
 * <li><b>File Watching:</b> Updates when messages.properties files change</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>
 * {m:main.login}           → "Login/Register"
 * {m:error.notFound('id')} → "Item 'id' not found"
 * </pre>
 * 
 * @author Angelo ZERR
 */
public class RenardeProjectExtension implements ProjectExtension {

	private static final String M_NAMESPACE_NAME = "m";
	private static final String M_NAMESPACE = "m:";

	private static final Logger LOGGER = Logger.getLogger(RenardeProjectExtension.class.getName());

	private boolean enabled;
	private final List<MessagesFileInfo> messagesFileInfos;
	private Set<Path> sourcePaths;

	public RenardeProjectExtension() {
		this.messagesFileInfos = new ArrayList<>();
	}

	@Override
	public void init(ExtendedDataModelProject dataModelProject) {
		// Check if the m: namespace resolver exists in the project
		enabled = dataModelProject.getNamespaceResolver(M_NAMESPACE_NAME) != null;
		sourcePaths = dataModelProject.getSourcePaths();

		if (!enabled) {
			messagesFileInfos.clear();
			return;
		}

		// Scan project source folders for messages.properties files
		if (messagesFileInfos.isEmpty()) {
			scanMessagesFiles(dataModelProject.getSourcePaths());
		}
	}

	/**
	 * Scans source folders for messages.properties files.
	 * 
	 * @param sourcePaths the source paths to scan
	 */
	private void scanMessagesFiles(Set<Path> sourcePaths) {
		for (Path sourcePath : sourcePaths) {
			try (Stream<Path> stream = Files.list(sourcePath)) {
				stream.forEach(this::loadMessagesFile);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error scanning source folder: " + sourcePath, e);
			}
		}
	}

	/**
	 * Loads a messages file if it matches the naming pattern.
	 * 
	 * @param filePath the file path to check and load
	 */
	private void loadMessagesFile(Path filePath) {
		MessagesFileName messagesFileName = MessagesFileInfo.getMessagesFileName(filePath);
		if (messagesFileName != null) {
			try {
				MessagesFileInfo messagesFile = new MessagesFileInfo(filePath, messagesFileName);
				// TODO: improve default by reading 'quarkus.default-locale=en' from
				// application.properties
				messagesFile.setDefaultFile(messagesFile.getLocale() == null);
				messagesFileInfos.add(messagesFile);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error loading messages file: " + filePath, e);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Checks if a message key exists in any messages.properties file.
	 * 
	 * @param messageKey the message key to check (e.g., "main.login")
	 * @return true if the key exists
	 */
	public boolean hasMessage(String messageKey) {
		return messagesFileInfos.stream().anyMatch(info -> info.hasMessage(messageKey));
	}

	/**
	 * Returns all messages.properties files in the project.
	 * 
	 * @return the list of messages file info
	 */
	public List<MessagesFileInfo> getMessagesFileInfos() {
		return messagesFileInfos;
	}

	/**
	 * Returns the default messages file (without locale suffix).
	 * 
	 * @return the default messages file info, or null if not found
	 */
	private MessagesFileInfo getDefaultMessagesFileInfo() {
		return messagesFileInfos.stream().filter(MessagesFileInfo::isDefaultFile).findFirst().orElse(null);
	}

	/**
	 * Finds a messages file info by filename.
	 * 
	 * @param name the filename to search for
	 * @return the messages file info, or null if not found
	 */
	private MessagesFileInfo find(String name) {
		return messagesFileInfos.stream().filter(info -> {
			Path path = info.getMessagesFile();
			String fileName = path.getName(path.getNameCount() - 1).toString();
			return name.equals(fileName);
		}).findFirst().orElse(null);
	}

	/**
	 * Extracts the message key from Qute expression parts.
	 * 
	 * <p>
	 * Converts {@code {m:main.login}} into the key {@code "main.login"}.
	 * </p>
	 * 
	 * @param parts the expression parts
	 * @return the message key, or null if not a valid m: expression
	 */
	private static String getMessageKey(Parts parts) {
		if (parts == null || !M_NAMESPACE_NAME.equals(parts.getNamespace())) {
			return null;
		}

		StringBuilder messageKey = new StringBuilder();
		for (Node child : parts.getChildren()) {
			if (child.getKind() == NodeKind.ExpressionPart) {
				Part part = (Part) child;
				PartKind partKind = part.getPartKind();

				// Build the key from object, property, and method parts
				if (partKind == PartKind.Object || partKind == PartKind.Property || partKind == PartKind.Method) {
					if (messageKey.length() > 0) {
						messageKey.append('.');
					}
					messageKey.append(part.getPartName());
				}
			}
		}
		return messageKey.toString();
	}

	// ======================== DidChangeWatchedFilesParticipant
	// ========================

	@Override
	public boolean didChangeWatchedFile(Path filePath, FileEvent fileEvent) {
		MessagesFileName messagesFileName = MessagesFileInfo.getMessagesFileName(filePath, sourcePaths);
		if (messagesFileName == null) {
			return false;
		}

		MessagesFileInfo file = find(messagesFileName.getFileName());
		boolean fileDeleted = fileEvent.getType() == FileChangeType.Deleted;

		try {
			if (file != null) {
				if (fileDeleted) {
					messagesFileInfos.remove(file);
				} else {
					file.reload();
				}
			} else if (!fileDeleted) {
				messagesFileInfos.add(new MessagesFileInfo(filePath, messagesFileName));
			}
			return true;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error handling file change: " + filePath, e);
			return false;
		}
	}

	// ======================== DefinitionParticipant ========================

	@Override
	public void definition(Part part, List<LocationLink> locationLinks, CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		String messageKey = getMessageKey(parts);

		if (messageKey != null && hasMessage(messageKey)) {
			for (MessagesFileInfo info : messagesFileInfos) {
				String messagesFileUri = info.getMessagesFile().toUri().toASCIIString();
				Range originRange = QutePositionUtility.createRange(parts);

				// TODO: Parse .properties file to find exact line number of the key
				// Currently navigates to beginning of file (0,0)
				Range targetRange = new Range(new Position(0, 0), new Position(0, 0));

				locationLinks.add(new LocationLink(messagesFileUri, targetRange, targetRange, originRange));
			}
		}
	}

	// ======================== DiagnosticsParticipant ========================

	@Override
	public boolean validateExpression(Parts parts, QuteValidationSettings validationSettings,
			ResolvingJavaTypeContext resolvingJavaTypeContext, List<Diagnostic> diagnostics) {
		String messageKey = getMessageKey(parts);

		if (messageKey == null) {
			return false;
		}

		// Report unknown message key
		if (!hasMessage(messageKey)) {
			Range range = QutePositionUtility.createRange(parts);
			Diagnostic diagnostic = createDiagnostic(range, DiagnosticSeverity.Warning,
					RenardeErrorCode.RenardeMessages, messageKey);
			diagnostics.add(diagnostic);
		}
		return true;
	}

	// ======================== HoverParticipant ========================

	@Override
	public void doHover(Part part, List<Hover> hovers, CancelChecker cancelChecker) {
		Parts parts = part.getParent();
		String messageKey = getMessageKey(parts);

		if (messageKey == null || !hasMessage(messageKey)) {
			return;
		}

		Range range = QutePositionUtility.createRange(parts);
		StringBuilder doc = new StringBuilder();
		boolean hasValue = false;

		// Collect message values from all messages files (different locales)
		for (MessagesFileInfo messagesFileInfo : messagesFileInfos) {
			String value = (String) messagesFileInfo.getProperties().get(messageKey);
			if (value != null) {
				if (hasValue) {
					doc.append("\n");
				}
				hasValue = true;

				doc.append(" * ");
				appendLocaleInfo(doc, messagesFileInfo);
				doc.append(": ");

				if (messagesFileInfo.isDefaultFile()) {
					doc.append("**");
				}
				doc.append(value);
				if (messagesFileInfo.isDefaultFile()) {
					doc.append("**");
				}
			}
		}

		MarkupContent contents = DocumentationUtils.createMarkupContent(doc, true);
		hovers.add(new Hover(contents, range));
	}

	/**
	 * Appends locale information to the documentation string.
	 * 
	 * @param doc              the string builder to append to
	 * @param messagesFileInfo the messages file info
	 */
	private void appendLocaleInfo(StringBuilder doc, MessagesFileInfo messagesFileInfo) {
		if (messagesFileInfo.isDefaultFile()) {
			doc.append("default");
		} else {
			String locale = messagesFileInfo.getLocale();
			doc.append(locale != null ? locale : "unknown");
		}
	}

	// ======================== InlayHintParticipant ========================

	@Override
	public void inlayHint(Expression node, List<InlayHint> inlayHints, CancelChecker cancelChecker) {
		Parts parts = node.getParts();
		String messageKey = getMessageKey(parts);

		if (messageKey == null || !hasMessage(messageKey)) {
			return;
		}

		MessagesFileInfo defaultMessagesFileInfo = getDefaultMessagesFileInfo();
		if (defaultMessagesFileInfo == null) {
			return;
		}

		String value = (String) defaultMessagesFileInfo.getProperties().get(messageKey);
		if (value == null || value.isEmpty()) {
			return;
		}

		try {
			// Display the message as inlay hint: {m:main.login [=Login/Register]}
			Template template = parts.getOwnerTemplate();
			Position position = template.positionAt(node.getEnd() - 1);

			InlayHint hint = new InlayHint();
			hint.setKind(InlayHintKind.Type);
			hint.setLabel(Either.forLeft(" =" + value));
			hint.setPosition(position);
			inlayHints.add(hint);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error creating inlay hint for message: " + messageKey, e);
		}
	}

	// ======================== CompletionParticipant ========================

	@Override
	public void doComplete(CompletionRequest completionRequest, Part part, Parts parts,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings,
			Set<CompletionItem> completionItems, CancelChecker cancelChecker) {
		if (!isRenardeMessageExpression(part, parts)) {
			return;
		}

		Range range = createRange(completionRequest, part, parts);

		for (MessagesFileInfo messagesFileInfo : messagesFileInfos) {
			messagesFileInfo.getProperties().forEach((k, v) -> {
				String key = k.toString();
				String value = v.toString();

				CompletionItem item = createMessageCompletionItem(key, value, range, completionSettings,
						formattingSettings);
				completionItems.add(item);
			});
		}
	}

	private static boolean isRenardeMessageExpression(Part part, Parts parts) {
		if (part == null) {
			if (parts == null || parts.getChildCount() == 0) {
				// - {|
				return true;
			}
			// - {m:|
			// - {m:name.|
			// - {name.|
			return M_NAMESPACE_NAME.equals(parts.getNamespace());
		}
		if (parts.getChildCount() == 1) {
			// - {m|
			return true;
		}
		// - {m:na|me
		// - {name.na|me
		return M_NAMESPACE_NAME.equals(parts.getNamespace());
	}

	/**
	 * Creates a completion item for a message key.
	 * 
	 * @param key                the message key
	 * @param value              the message value
	 * @param range              the text range to replace
	 * @param completionSettings completion preferences
	 * @param formattingSettings formatting preferences
	 * @return the completion item
	 */
	private CompletionItem createMessageCompletionItem(String key, String value, Range range,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings) {
		String messageKey = M_NAMESPACE + key;
		String label = messageKey + " = " + value;

		CompletionItem item = new CompletionItem();
		item.setLabel(label);
		item.setFilterText(messageKey);
		item.setKind(CompletionItemKind.Property);

		TextEdit textEdit = new TextEdit();
		textEdit.setRange(range);

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		item.setInsertTextFormat(snippetsSupported ? InsertTextFormat.Snippet : InsertTextFormat.PlainText);

		int nbArgs = countPercentS(value);
		textEdit.setNewText(createMMessageSnippet(messageKey, nbArgs, completionSettings, formattingSettings));

		item.setTextEdit(Either.forLeft(textEdit));
		return item;
	}

	/**
	 * Creates the text range for completion based on context.
	 * 
	 * @param completionRequest the completion request
	 * @param part              the current part (may be null)
	 * @param parts             the expression parts (may be null)
	 * @return the text range
	 */
	private Range createRange(CompletionRequest completionRequest, Part part, Parts parts) {
		if (parts != null) {
			return QutePositionUtility.createRange(parts);
		}
		if (part != null) {
			return QutePositionUtility.createRange(part);
		}
		return QutePositionUtility.createRange(completionRequest.getOffset(), completionRequest.getOffset(),
				completionRequest.getTemplate());
	}

	/**
	 * Creates a snippet for a message with parameters.
	 * 
	 * <p>
	 * For a message with placeholders like "Hello %s, you have %s messages",
	 * generates a snippet like: m:greeting.welcome(${1:arg0}, ${2:arg1})$0
	 * </p>
	 * 
	 * @param messageKey         the message key
	 * @param nbArgs             the number of arguments (%s placeholders)
	 * @param completionSettings completion preferences
	 * @param formattingSettings formatting preferences
	 * @return the snippet text
	 */
	private static String createMMessageSnippet(String messageKey, int nbArgs,
			QuteCompletionSettings completionSettings, QuteFormattingSettings formattingSettings) {
		if (nbArgs == 0) {
			return messageKey;
		}

		boolean snippetsSupported = completionSettings.isCompletionSnippetsSupported();
		StringBuilder snippet = new StringBuilder(messageKey);
		snippet.append('(');

		for (int i = 0; i < nbArgs; i++) {
			if (i > 0) {
				snippet.append(", ");
			}
			String paramName = "arg" + i;
			if (snippetsSupported) {
				SnippetsBuilder.placeholders(i + 1, paramName, snippet);
			} else {
				snippet.append(paramName);
			}
		}

		snippet.append(')');
		if (snippetsSupported) {
			SnippetsBuilder.tabstops(0, snippet);
		}
		return snippet.toString();
	}

	/**
	 * Counts the number of "%s" placeholders in a message string.
	 * 
	 * <p>
	 * Escaped percent sequences ("%%") are ignored. For example:
	 * <ul>
	 * <li>"Hello %s" → 1</li>
	 * <li>"Hello %s, you have %s messages" → 2</li>
	 * <li>"Use %% to escape" → 0</li>
	 * <li>"Hello %s, use %% and %s" → 2</li>
	 * </ul>
	 * </p>
	 *
	 * @param s the input string
	 * @return the number of "%s" occurrences
	 */
	static int countPercentS(String s) {
		int count = 0;

		for (int i = 0; i < s.length() - 1; i++) {
			if (s.charAt(i) == '%') {
				char next = s.charAt(i + 1);

				if (next == '%') {
					// Escaped percent (%%) - skip both characters
					i++;
				} else if (next == 's') {
					// Found a placeholder
					count++;
					i++;
				}
			}
		}

		return count;
	}
}