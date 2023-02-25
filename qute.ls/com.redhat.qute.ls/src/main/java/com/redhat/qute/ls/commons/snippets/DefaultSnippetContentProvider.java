package com.redhat.qute.ls.commons.snippets;

import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.CompletionItem;

public class DefaultSnippetContentProvider implements ISnippetContentProvider {

	public static final ISnippetContentProvider INSTANCE = new DefaultSnippetContentProvider();

	@Override
	public String getInsertText(Snippet snippet, Map<String, String> model, boolean replace, String lineDelimiter,
			String whitespacesIndent, CompletionItem item) {
		StringBuilder text = new StringBuilder();
		List<String> body = snippet.getBody();
		if (body != null) {
			for (int i = 0; i < body.size(); i++) {
				String bodyLine = body.get(i);
				if (i > 0) {
					text.append(lineDelimiter);
					if (whitespacesIndent != null) {
						text.append(whitespacesIndent);
					}
				}
				bodyLine = merge(bodyLine, model, replace);
				text.append(bodyLine);
			}
		}
		return text.toString();
	}

	public static String merge(String line, Map<String, String> model, boolean replace) {
		return replace(line, 0, model, replace, null);
	}

	private static String replace(String line, int offset, Map<String, String> model, boolean replace,
			StringBuilder newLine) {
		int dollarIndex = line.indexOf("$", offset);
		if (dollarIndex == -1 || dollarIndex == line.length() - 1) {
			if (newLine == null) {
				return line;
			}
			newLine.append(line, offset, line.length());
			return newLine.toString();
		}
		if (newLine == null) {
			newLine = new StringBuilder();
		}
		char next = line.charAt(dollarIndex + 1);
		if (Character.isDigit(next)) {
			if (replace) {
				newLine.append(line, offset, dollarIndex);
			}
			int lastDigitOffset = dollarIndex + 1;
			while (line.length() > lastDigitOffset && Character.isDigit(line.charAt(lastDigitOffset))) {
				lastDigitOffset++;
			}
			if (!replace) {
				newLine.append(line, offset, lastDigitOffset);
			}
			return replace(line, lastDigitOffset, model, replace, newLine);
		} else if (next == '{') {
			int startExpr = dollarIndex;
			int endExpr = line.indexOf("}", startExpr);
			if (endExpr == -1) {
				// Should never occur
				return line;
			}
			newLine.append(line, offset, startExpr);
			// Parameter
			int startParam = startExpr + 2;
			int endParam = endExpr;
			boolean startsWithNumber = true;
			boolean onlyNumber = true;
			for (int i = startParam; i < endParam; i++) {
				char ch = line.charAt(i);
				if (Character.isDigit(ch)) {
					startsWithNumber = true;
				} else {
					onlyNumber = false;
					if (ch == ':') {

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
			}
			String paramName = line.substring(startParam, endParam);
			if (model.containsKey(paramName)) {
				paramName = model.get(paramName);
			} else if (!replace) {
				paramName = line.substring(startExpr, endExpr + 1);
			}
			if (!(replace && onlyNumber)) {
				newLine.append(paramName);
			}
			return replace(line, endExpr + 1, model, replace, newLine);
		}
		return line;
	}
}
