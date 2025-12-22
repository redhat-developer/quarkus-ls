/*******************************************************************************
* Copyright (c) 2024 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.user_tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.project.BaseQuteProject;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.tags.UserTag;

/**
 * Qute Web project.
 */
public class QuteUserTagsProject extends BaseQuteProject {

	public static final String PROJECT_URI = "qute-user-tags";

	private final Map<String, UserTag> userTags;

	public QuteUserTagsProject(ProjectInfo projectInfo, QuteProjectRegistry projectRegistry) {
		super(projectInfo, projectRegistry);
		this.userTags = new HashMap<>();

	}

	@Override
	protected void fillTemplates(List<DataModelTemplate<DataModelParameter>> templates) {

	}

	@Override
	protected void fillValueResolvers(List<ValueResolverInfo> valueResolvers) {
	}

	@Override
	protected void fillNamespaceResolverInfos(Map<String, NamespaceResolverInfo> namespaces) {

	}

	@Override
	public UserTag findUserTag(String tagName) {
		return userTags.get(tagName);
	}

	public void registerUserTag(String tagName, String content) {
		UserTag userTag = new UserTag(tagName, this) {

			@Override
			public String getUri() {
				return tagName + ".html";
			}

			@Override
			public String getContent() {
				return content;
			}
		};
		userTags.put(tagName, userTag);
	}
}
