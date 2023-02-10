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
		snippet.append(parameter.getName());
		snippet.append("=");
		snippet.append("\"");
		if (snippetsSupported) {
			SnippetsBuilder.placeholders(index, parameter.getName(), snippet);
		} else {
			snippet.append(parameter.getName());
		}
		snippet.append("\"");

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
			parameters = collectParameters();
		}
		return parameters.values();
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
	private Map<String, UserTagParameter> collectParameters() {
		Template template = getTemplate();
		if (template == null) {
			return Collections.emptyMap();
		}
		UserTagParameterCollector collector = new UserTagParameterCollector(project);
		template.accept(collector);
		return collector.getParameters();
	}

	/**
	 * Returns the template.
	 * 
	 * @return the template.
	 */
	private Template getTemplate() {
		String content = getContent();
		if (content == null) {
			return null;
		}
		return TemplateParser.parse(content, getFileName());
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
