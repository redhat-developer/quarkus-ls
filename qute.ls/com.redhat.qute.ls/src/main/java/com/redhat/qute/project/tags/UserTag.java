/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;

import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteTextDocument;
import com.redhat.qute.services.snippets.QuteSnippetContext;
import com.redhat.qute.settings.QuteFormattingSettings;
import com.redhat.qute.settings.QuteFormattingSettings.SplitSectionParameters;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Abstract class for User tag section.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public class UserTag extends Snippet {

	private Map<String, UserTagParameter> parameters;
	private boolean hasArgs;
	private final QuteTextDocument document;
	private boolean hasContent;
	private QuteFormattingSettings formattingSettings;
	private int currentTabSize;
	private boolean currentInsertSpaces;
	private SplitSectionParameters currentSplitSectionParameters;
	private int currentSplitSectionParametersIndentSize;

	public UserTag(QuteTextDocument document, QuteFormattingSettings formattingSettings) {
		this.document = document;
		this.formattingSettings = formattingSettings;
		String name = document.getUserTagName();
		super.setLabel(name);
		super.setPrefixes(Arrays.asList(name));
		super.setContext(QuteSnippetContext.IN_TEXT);
		String relativePath = document.getRelativePath();
		if (relativePath != null) {
			CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
			labelDetails.setDescription(relativePath);
			super.setLabelDetails(labelDetails);
		}
		super.setKind(document.isBinary() ? CompletionItemKind.Function : CompletionItemKind.Method);
	}

	@Override
	public List<String> getBody() {
		if (super.getBody() == null || isFormattingSettingsChanged()) {
			currentTabSize = formattingSettings.getTabSize();
			currentInsertSpaces = formattingSettings.isInsertSpaces();
			currentSplitSectionParameters = formattingSettings.getSplitSectionParameters();
			currentSplitSectionParametersIndentSize = formattingSettings.getSplitSectionParametersIndentSize();
			super.setBody(createBody(currentTabSize, currentInsertSpaces, currentSplitSectionParameters,
					currentSplitSectionParametersIndentSize));
		}
		return super.getBody();
	}

	private boolean isFormattingSettingsChanged() {
		return currentTabSize != formattingSettings.getTabSize()
				|| currentInsertSpaces != formattingSettings.isInsertSpaces()
				|| currentSplitSectionParameters != formattingSettings.getSplitSectionParameters()
				|| currentSplitSectionParametersIndentSize != formattingSettings.getSplitSectionParametersIndentSize();
	}

	/**
	 * Creates the snippet body for the user tag section.
	 *
	 * <p>
	 * When {@code multiline} is {@code false} (default), all required parameters
	 * are placed on a single line:
	 * 
	 * <pre>
	 *   {#my-tag [param1] [param2] /}$0
	 * </pre>
	 *
	 * <p>
	 * When {@code multiline} is {@code true}, each required parameter is placed on
	 * its own line, continuation lines being aligned to the first parameter column
	 * using as many {@code \t} characters as possible (assuming a tab size of 4)
	 * followed by the remaining spaces:
	 * 
	 * <pre>
	 *   {#my-tag [param1]
	 *            [param2]
	 *            [param3]
	 *    /}$0
	 * </pre>
	 * 
	 * @param splitSectionParametersIndentSize
	 * @param splitSectionParameters
	 * @param insertSpaces
	 * @param tabSize
	 *
	 * @return the list of lines that form the snippet body.
	 */
	private List<String> createBody(int tabSize, boolean insertSpaces, SplitSectionParameters splitSectionParameters,
			int splitSectionParametersIndentSize) {
		String name = getLabel();
		List<String> body = new ArrayList<>();
		Collection<UserTagParameter> parameters = getParameters();
		int index = 1;

		StringBuilder startSection = new StringBuilder("{#");
		startSection.append(name);

		// Collect required parameter fragments first so we can decide
		// how to lay them out (inline vs. multiline) after the loop.
		List<String> paramFragments = new ArrayList<>();
		for (UserTagParameter parameter : parameters) {
			if (parameter.isRequired()) {
				// index++;
				switch (parameter.getName()) {
				case UserTagUtils.IT_OBJECT_PART_NAME:
					StringBuilder itFrag = new StringBuilder();
					SnippetsBuilder.placeholders(index++, parameter.getName(), itFrag);
					paramFragments.add(itFrag.toString());
					break;
				case UserTagUtils.NESTED_CONTENT_OBJECT_PART_NAME:
					break;
				default:
					// Generate parameter name
					String param = generateUserTagParameter(parameter, Collections.emptySet(), false, true, index++);
					paramFragments.add(param);
					break;
				}
			}
		}

		if ((splitSectionParameters == SplitSectionParameters.preserve) || paramFragments.size() <= 1) {
			// ── Inline mode───────────────────────
			for (String frag : paramFragments) {
				startSection.append(" ").append(frag);
			}
			if (hasContent) {
				if (!parameters.isEmpty()) {
					startSection.append(" ");
				}
				startSection.append("}");
			} else {
				startSection.append(" /}");
				SnippetsBuilder.tabstops(0, startSection);
			}
			body.add(startSection.toString());
		} else {
			// ── Multiline mode ─────────────────────────────────────────────────
			String continuationIndent = getContinuationIndent(splitSectionParameters, name, tabSize, insertSpaces,
					splitSectionParametersIndentSize);

			if (splitSectionParameters == SplitSectionParameters.splitNewLine) {
				// splitNewLine: all parameters on their own line, opening line has no param.
				body.add(startSection.toString());

				// All parameters except the last: one per line.
				for (int i = 0; i < paramFragments.size() - 1; i++) {
					body.add(continuationIndent + paramFragments.get(i));
				}
			} else {
				// alignWithFirstParam: first parameter stays on the opening line.
				startSection.append(" ").append(paramFragments.get(0));

				// Flush the opening line as-is.
				body.add(startSection.toString());

				// Middle parameters: one per line, aligned to param1.
				for (int i = 1; i < paramFragments.size() - 1; i++) {
					body.add(continuationIndent + paramFragments.get(i));
				}
			}

			// Last parameter: closing token appended on the same line.
			StringBuilder lastLine = new StringBuilder(continuationIndent);
			lastLine.append(paramFragments.get(paramFragments.size() - 1));
			if (hasContent) {
				if (!parameters.isEmpty()) {
					lastLine.append(" ");
				}
				lastLine.append("}");
			} else {
				lastLine.append(" /}");
				SnippetsBuilder.tabstops(0, lastLine);
			}
			body.add(lastLine.toString());
		}

		// Body for tags that wrap nested content.
		if (hasContent) {
			body.add("\t" + SnippetsBuilder.tabstops(index++));
			body.add("{/" + name + "}" + SnippetsBuilder.tabstops(0));
		}

		return body;
	}

	/**
	 * Computes the indentation string used to align continuation parameters in
	 * multiline mode, according to the {@code splitSectionParameters} strategy.
	 *
	 * <p>
	 * When {@code splitSectionParameters} is
	 * {@link SplitSectionParameters#alignWithFirstAttr}, continuation lines are
	 * aligned to the column of the first parameter (i.e. right after {@code {#name
	 * }):
	 *
	 * <pre>
	 *   {#my-tag param1
	 *            param2
	 *            param3 /}$0
	 * </pre>
	 *
	 * <p>
	 * When {@code splitSectionParameters} is {@link
	 * SplitSectionParameters#splitNewLine}, continuation lines are indented by
	 * {@code splitSectionParametersIndentSize} levels, where one level is either
	 * {@code tabSize} spaces (if {@code insertSpaces} is {@code true}) or one tab
	 * character:
	 *
	 * <pre>
	 *   {#my-tag param1
	 *       param2
	 *       param3 /}$0
	 * </pre>
	 *
	 * @param splitSectionParameters the split strategy
	 * ({@link SplitSectionParameters#alignWithFirstAttr} or
	 * {@link SplitSectionParameters#splitNewLine})
	 * 
	 * @param name                             the user tag name, used to compute
	 *                                         the alignment width in
	 *                                         {@code alignWithFirstAttr} mode
	 * @param tabSize                          the number of spaces per indentation
	 *                                         level
	 * @param insertSpaces                     {@code true} to use spaces,
	 *                                         {@code false} to use tab characters
	 * @param splitSectionParametersIndentSize the number of indentation levels to
	 *                                         apply in {@code splitNewLine} mode
	 * @return the indentation string to prepend to each continuation parameter line
	 */
	private static String getContinuationIndent(SplitSectionParameters splitSectionParameters, String name, int tabSize,
			boolean insertSpaces, int splitSectionParametersIndentSize) {
		if (splitSectionParameters == SplitSectionParameters.alignWithFirstParam) {
			// Align to the column right after "{#name ": 2 ("{#") + name length + 1 (space)
			int alignWidth = 2 + name.length() + 1;
			if (insertSpaces) {
				// Fill the alignment width with spaces
				return " ".repeat(alignWidth);
			} else {
				// Maximise tabs then fill the remainder with spaces
				int tabs = alignWidth / tabSize;
				int spaces = alignWidth % tabSize;
				return "\t".repeat(tabs) + " ".repeat(spaces);
			}
		} else {
			// splitNewLine: indent by N levels where 1 level = tabSize spaces or 1 tab
			if (insertSpaces) {
				return " ".repeat(tabSize * splitSectionParametersIndentSize);
			} else {
				return "\t".repeat(splitSectionParametersIndentSize);
			}
		}
	}

	/**
	 * Insert the user tag parameter snippet in the given snippet content.
	 * 
	 * @param parameter         the user tag parameter.
	 * @param snippetsSupported true if snippets is supported and false otherwise.
	 * @param index             the index
	 * @param snippet           the snippet content.
	 */
	public static void generateUserTagParameter(UserTagParameter parameter, Collection<String> names,
			boolean parameterExists, boolean snippetsSupported, int index, StringBuilder snippet) {
		if (parameterExists) {
			if (snippetsSupported) {
				SnippetsBuilder.placeholders(index, parameter.getName(), snippet);
			} else {
				snippet.append(parameter.getName());
			}
			return;
		}
		// Generate parameter name
		snippet.append(parameter.getName());
		snippet.append("=");

		// Generate parameter value
		if (names != null && !names.isEmpty()) {
			SnippetsBuilder.choice(index, names, snippet);
		} else {
			String value = parameter.getName();
			Character quote = null;
			String defaultValue = parameter.getDefaultValue();
			if (defaultValue != null && !defaultValue.isEmpty()) {
				value = defaultValue;
				// there is a default value, remove the quote if needed
				char start = defaultValue.charAt(0);
				if (start == '"' || start == '\'') {
					quote = start;
					value = value.substring(1, value.length() - (value.endsWith(start + "") ? 1 : 0));
				} else {
					quote = null;
				}
			}

			if (quote != null) {
				snippet.append(quote);
			}

			if (snippetsSupported) {
				SnippetsBuilder.placeholders(index, value, snippet);
			} else {
				snippet.append(value);
			}
			if (quote != null) {
				snippet.append(quote);
			}
		}
	}

	public static String generateUserTagParameter(UserTagParameter parameter, Collection<String> names,
			boolean parameterExists, boolean snippetsSupported, int index) {
		StringBuilder snippet = new StringBuilder();
		generateUserTagParameter(parameter, names, parameterExists, snippetsSupported, index, snippet);
		return snippet.toString();
	}

	/**
	 * Returns the user tag name.
	 *
	 * @return the user tag name.
	 */
	public String getName() {
		return getLabel();
	}

	/**
	 * Returns the user tag name.
	 *
	 * @return the the user tag name.
	 */
	public String getFileName() {
		String uri = document.getUri();
		int index = uri.lastIndexOf('/');
		return uri.substring(index + 1);
	}

	/**
	 * Returns the template id.
	 *
	 * @return the template id.
	 */
	public String getTemplateId() {
		return document.getTemplateId();
	}

	/**
	 * 
	 * @return
	 */
	public Collection<UserTagParameter> getParameters() {
		if (parameters == null) {
			UserTagInfoCollector collector = getUserTagCollector();
			if (collector != null) {
				parameters = collector.getParameters();
				hasArgs = collector.hasArgs();
				hasContent = collector.hasContent();
			} else {
				parameters = Collections.emptyMap();
				hasArgs = false;
				hasContent = false;
			}
		}
		return parameters.values();
	}

	public boolean hasArgs() {
		getParameters();
		return hasArgs;
	}

	/**
	 * Returns all required parameters names.
	 *
	 * @return all required parameter names.
	 */
	public List<String> getRequiredParameterNames() {
		return getParameters().stream().filter(UserTagParameter::isRequired).map(UserTagParameter::getName)
				.filter(paramName -> !paramName.equals(UserTagUtils.NESTED_CONTENT_OBJECT_PART_NAME))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the parameter which have the given name <code>parameterName</code>
	 * and null otherwise.
	 * 
	 * @param parameterName the parameter name.
	 * 
	 * @return the parameter which have the given name <code>parameterName</code>
	 *         and null otherwise.
	 */
	public UserTagParameter findParameter(String parameterName) {
		if (parameterName == null) {
			return null;
		}
		getParameters();
		return parameters.get(parameterName);
	}

	public boolean hasParameter(String parameterName) {
		getParameters();
		return parameters.containsKey(parameterName);
	}

	/**
	 * Returns parameters of the user tag.
	 * 
	 * @return parameters of the user tag.
	 */
	private UserTagInfoCollector getUserTagCollector() {
		Template template = getTemplate();
		if (template == null) {
			return null;
		}
		UserTagInfoCollector collector = new UserTagInfoCollector(document.getProject());
		template.accept(collector);
		return collector;
	}

	/**
	 * Evict the parameters cache.
	 */
	public void clear() {
		parameters = null;
		setBody(null);
	}

	public Template getTemplate() {
		return document.getTemplate();
	}

	public String getUri() {
		return document.getUri();
	}

	public QuteTextDocument getDocument() {
		return document;
	}
}
