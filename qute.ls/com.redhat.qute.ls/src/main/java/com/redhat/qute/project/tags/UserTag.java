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

import com.redhat.qute.ls.commons.snippets.Snippet;
import com.redhat.qute.ls.commons.snippets.SnippetsBuilder;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.parser.template.TemplateParser;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.services.snippets.QuteSnippetContext;
import com.redhat.qute.utils.UserTagUtils;

/**
 * Abstract class for User tag section.
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#user_tags
 *
 */
public abstract class UserTag extends Snippet {

	private final String fileName;
	private final String templateId;
	private Map<String, UserTagParameter> parameters;
	private final QuteProject project;
	private boolean hasArgs;

	public UserTag(String fileName, QuteProject project) {
		String name = UserTagUtils.getUserTagName(fileName);
		super.setLabel(name);
		super.setPrefixes(Arrays.asList(name));
		super.setContext(QuteSnippetContext.IN_TEXT);
		this.fileName = fileName;
		this.templateId = UserTagUtils.getTemplateId(name);
		this.project = project;
	}

	@Override
	public List<String> getBody() {
		if (super.getBody() == null) {
			super.setBody(createBody());
		}
		return super.getBody();
	}

	private List<String> createBody() {
		String name = getLabel();
		List<String> body = new ArrayList<>();
		Collection<UserTagParameter> parameters = getParameters();

		boolean hasNestedContent = false;
		int index = 1;

		StringBuilder startSection = new StringBuilder("{#");
		startSection.append(name);
		for (UserTagParameter parameter : parameters) {
			if (parameter.isRequired()) {
				switch (parameter.getName()) {
				case UserTagUtils.IT_OBJECT_PART_NAME:
					startSection.append(" ");
					SnippetsBuilder.placeholders(index++, parameter.getName(), startSection);
					break;
				case UserTagUtils.NESTED_CONTENT_OBJECT_PART_NAME:
					hasNestedContent = true;
					break;
				default:
					startSection.append(" ");
					generateUserTagParameter(parameter, true, index++, startSection);
					break;
				}
			}
		}
		if (hasNestedContent) {
			if (!parameters.isEmpty()) {
				startSection.append(" ");
			}
			startSection.append("}");
		} else {
			startSection.append(" /}");
			SnippetsBuilder.tabstops(0, startSection);
		}
		body.add(startSection.toString());

		if (hasNestedContent) {
			body.add("\t" + SnippetsBuilder.tabstops(index++));
			body.add("{/" + name + "}" + SnippetsBuilder.tabstops(0));
		}
		return body;
	}

	/**
	 * Insert the user tag parameter snippet in the given snippet content.
	 * 
	 * @param parameter         the user tag parameter.
	 * @param snippetsSupported true if snippets is supported and false otherwise.
	 * @param index             the index
	 * @param snippet           the snippet content.
	 */
	public static void generateUserTagParameter(UserTagParameter parameter, boolean snippetsSupported, int index,
			StringBuilder snippet) {
		// Generate parameter name
		snippet.append(parameter.getName());
		snippet.append("=");

		// Generate parameter value
		String value = parameter.getName();
		Character quote = '"';
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

	/**
	 * Returns the user tag name.
	 *
	 * @return the user tag name.
	 */
	public String getName() {
		return getLabel();
	}

	/**
	 * Returns the file name of the user tag.
	 *
	 * @return the file name of the user tag.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns the template id.
	 *
	 * @return the template id.
	 */
	public String getTemplateId() {
		return templateId;
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
			} else {
				parameters = Collections.emptyMap();
				hasArgs = false;
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
		for (UserTagParameter parameter : getParameters()) {
			if (parameterName.equals(parameter.getName())) {
				return parameter;
			}
		}
		return null;
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
		UserTagInfoCollector collector = new UserTagInfoCollector(project);
		template.accept(collector);
		return collector;
	}

	/**
	 * Returns the template.
	 * 
	 * @return the template.
	 */
	public Template getTemplate() {
		String content = getContent();
		if (content == null) {
			return null;
		}
		return TemplateParser.parse(content, getUri(), Collections.emptyList());
	}

	/**
	 * Evict the parameters cache.
	 */
	public void clear() {
		parameters = null;
		setBody(null);
	}

	/**
	 * Returns the Qute template file Uri.
	 *
	 * @return the Qute template file Uri.
	 */
	public abstract String getUri();

	/**
	 * Returns the content of the user tag.
	 *
	 * @return the content of the user tag.
	 */
	public abstract String getContent();

}
