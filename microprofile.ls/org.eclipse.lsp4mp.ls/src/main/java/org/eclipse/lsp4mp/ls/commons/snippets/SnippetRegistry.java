/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
* 
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4mp.ls.commons.snippets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

/**
 * A registry for snippets which uses the same format than vscode snippet.
 * 
 * @author Angelo ZERR
 *
 */
public class SnippetRegistry {

	private static final Logger LOGGER = Logger.getLogger(SnippetRegistry.class.getName());

	private final List<Snippet> snippets;

	public SnippetRegistry() {
		this(null);
	}

	/**
	 * Snippet registry for a given language id.
	 * 
	 * @param languageId the language id and null otherwise.
	 */
	public SnippetRegistry(String languageId) {
		snippets = new ArrayList<>();
		// Load snippets from SPI
		ServiceLoader<ISnippetRegistryLoader> loaders = ServiceLoader.load(ISnippetRegistryLoader.class);
		loaders.forEach(loader -> {
			if (Objects.equals(languageId, loader.getLanguageId())) {
				try {
					loader.load(this);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error while consumming snippet loader " + loader.getClass().getName(), e);
				}
			}
		});
	}

	/**
	 * Register the given snippet.
	 * 
	 * @param snippet the snippet to register.
	 */
	public void registerSnippet(Snippet snippet) {
		snippets.add(snippet);
	}

	/**
	 * Register the snippets from the given JSON input stream.
	 * 
	 * @param in the JSON input stream which declares snippets with vscode snippet
	 *           format.
	 * @throws IOException
	 */
	public void registerSnippets(InputStream in) throws IOException {
		registerSnippets(in, null);
	}

	/**
	 * Register the snippets from the given JSON stream with a context.
	 * 
	 * @param in                  the JSON input stream which declares snippets with
	 *                            vscode snippet format.
	 * @param contextDeserializer the GSON context deserializer used to create Java
	 *                            context.
	 * @throws IOException
	 */
	public void registerSnippets(InputStream in, TypeAdapter<? extends ISnippetContext<?>> contextDeserializer)
			throws IOException {
		registerSnippets(new InputStreamReader(in, StandardCharsets.UTF_8.name()), contextDeserializer);
	}

	/**
	 * Register the snippets from the given JSON reader.
	 * 
	 * @param in the JSON reader which declares snippets with vscode snippet format.
	 * @throws IOException
	 */
	public void registerSnippets(Reader in) throws IOException {
		registerSnippets(in, null);
	}

	/**
	 * Register the snippets from the given JSON reader with a context.
	 * 
	 * @param in                  the JSON reader which declares snippets with
	 *                            vscode snippet format.
	 * @param contextDeserializer the GSON context deserializer used to create Java
	 *                            context.
	 * @throws IOException
	 */
	public void registerSnippets(Reader in, TypeAdapter<? extends ISnippetContext<?>> contextDeserializer)
			throws IOException {
		JsonReader reader = new JsonReader(in);
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			Snippet snippet = createSnippet(reader, contextDeserializer);
			if (snippet.getDescription() == null) {
				snippet.setDescription(name);
			}
			registerSnippet(snippet);
		}
		reader.endObject();
	}

	private static Snippet createSnippet(JsonReader reader,
			TypeAdapter<? extends ISnippetContext<?>> contextDeserializer) throws JsonIOException, JsonSyntaxException {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Snippet.class, new SnippetDeserializer(contextDeserializer));
		return builder.create().fromJson(reader, Snippet.class);
	}

	/**
	 * Returns all snippets.
	 * 
	 * @return all snippets.
	 */
	public List<Snippet> getSnippets() {
		return snippets;
	}

	/**
	 * Returns the snippet completion items according to the context filter.
	 * 
	 * @param replaceRange       the replace range.
	 * @param lineDelimiter      the line delimiter.
	 * @param canSupportMarkdown true if markdown is supported to generate
	 *                           documentation and false otherwise.
	 * @param contextFilter      the context filter.
	 * @return the snippet completion items according to the context filter.
	 */
	public List<CompletionItem> getCompletionItem(final Range replaceRange, final String lineDelimiter,
			boolean canSupportMarkdown, Predicate<ISnippetContext<?>> contextFilter) {
		if (replaceRange == null) {
			return Collections.emptyList();
		}
		return getSnippets().stream().filter(snippet -> {
			return snippet.match(contextFilter);
		}).map(snippet -> {
			String prefix = snippet.getPrefixes().get(0);
			String label = prefix;
			CompletionItem item = new CompletionItem();
			item.setLabel(label);
			item.setDetail(snippet.getDescription());
			String insertText = getInsertText(snippet, false, lineDelimiter);
			item.setKind(CompletionItemKind.Snippet);
			item.setDocumentation(Either.forRight(createDocumentation(snippet, canSupportMarkdown, lineDelimiter)));
			item.setFilterText(prefix);
			item.setTextEdit(new TextEdit(replaceRange, insertText));
			item.setInsertTextFormat(InsertTextFormat.Snippet);
			return item;

		}).filter(item -> item != null).collect(Collectors.toList());
	}

	private static MarkupContent createDocumentation(Snippet snippet, boolean canSupportMarkdown,
			String lineDelimiter) {
		StringBuilder doc = new StringBuilder();
		if (canSupportMarkdown) {
			doc.append(System.lineSeparator());
			doc.append("```");
			String scope = snippet.getScope();
			if (scope != null) {
				doc.append(scope);
			}
			doc.append(System.lineSeparator());
		}
		String insertText = getInsertText(snippet, true, lineDelimiter);
		doc.append(insertText);
		if (canSupportMarkdown) {
			doc.append(System.lineSeparator());
			doc.append("```");
			doc.append(System.lineSeparator());
		}
		return new MarkupContent(canSupportMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, doc.toString());
	}

	private static String getInsertText(Snippet snippet, boolean replace, String lineDelimiter) {
		StringBuilder text = new StringBuilder();
		int i = 0;
		List<String> body = snippet.getBody();
		if (body != null) {
			for (String bodyLine : body) {
				if (i > 0) {
					text.append(lineDelimiter);
				}
				if (replace) {
					bodyLine = replace(bodyLine);
				}
				text.append(bodyLine);
				i++;
			}
		}
		return text.toString();
	}

	private static String replace(String line) {
		return replace(line, 0, null);
	}

	private static String replace(String line, int offset, StringBuilder newLine) {
		int startExpr = line.indexOf("${", offset);
		if (startExpr == -1) {
			if (newLine == null) {
				return line;
			}
			newLine.append(line.substring(offset, line.length()));
			return newLine.toString();
		}
		int endExpr = line.indexOf("}", startExpr);
		if (endExpr == -1) {
			// Should never occur
			return line;
		}
		if (newLine == null) {
			newLine = new StringBuilder();
		}
		newLine.append(line.substring(offset, startExpr));
		// Parameter
		int startParam = startExpr + 2;
		int endParam = endExpr;
		boolean startsWithNumber = true;
		for (int i = startParam; i < endParam; i++) {
			char ch = line.charAt(i);
			if (Character.isDigit(ch)) {
				startsWithNumber = true;
			} else if (ch == ':') {
				if (startsWithNumber) {
					startParam = i + 1;
				}
				break;
			} else if (ch == '|') {
				if (startsWithNumber) {
					startParam = i + 1;
					int index = line.indexOf(',', startExpr);
					if (index != -1) {
						endParam = index;
					}
				}
				break;
			} else {
				break;
			}
		}
		newLine.append(line.substring(startParam, endParam));
		return replace(line, endExpr + 1, newLine);
	}

	protected static String findExprBeforeAt(String text, int offset) {
		if (offset < 0 || offset > text.length()) {
			return null;
		}
		if (offset == 0) {
			return "";
		}
		StringBuilder expr = new StringBuilder();
		int i = offset - 1;
		for (; i >= 0; i--) {
			char ch = text.charAt(i);
			if (Character.isWhitespace(ch)) {
				break;
			} else {
				expr.insert(0, ch);
			}
		}
		return expr.toString();
	}

}