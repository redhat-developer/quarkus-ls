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

import java.util.Arrays;

import com.redhat.qute.ls.commons.snippets.Snippet;
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

	public UserTag(String fileName) {
		String name = UserTagUtils.getUserTagName(fileName);
		super.setLabel(name);
		super.setPrefixes(Arrays.asList(name));
		super.setBody(Arrays.asList("{#" + name + " /}$0"));
		super.setContext(QuteSnippetContext.IN_TEXT);
		this.fileName = fileName;
		this.templateId = UserTagUtils.getTemplateId(name);
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
	 * Returns the Qute template file Uri.
	 * 
	 * @return the Qute template file Uri.
	 */
	public abstract String getUri();
}
