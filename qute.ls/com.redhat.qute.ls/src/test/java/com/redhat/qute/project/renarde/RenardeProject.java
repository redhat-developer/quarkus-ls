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
package com.redhat.qute.project.renarde;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.project.BaseQuteProject;
import com.redhat.qute.project.QuteProjectRegistry;

public class RenardeProject extends BaseQuteProject {

	public final static String PROJECT_URI = "renarde";

	public RenardeProject(QuteProjectRegistry projectRegistry) {
		super(new ProjectInfo(PROJECT_URI, //
				getProjectPath(PROJECT_URI), //
				Collections.emptyList(), //
				List.of(new TemplateRootPath(getProjectPath(PROJECT_URI) + "/src/main/resources/templates")), //
				Set.of(getProjectPath(PROJECT_URI) + "/src/main/resources"), //
				Set.of(ProjectFeature.Renarde)), projectRegistry);
	}

	@Override
	protected void fillNamespaceResolverInfos(Map<String, NamespaceResolverInfo> namespaces) {
		NamespaceResolverInfo namespace = new NamespaceResolverInfo();
		namespace.setNamespaces(List.of("m"));
		namespaces.put("m", namespace);
	}

	@Override
	protected void fillTemplates(List<DataModelTemplate<DataModelParameter>> templates) {

	}

	@Override
	protected void fillValueResolvers(List<ValueResolverInfo> valueResolvers) {

	}

}
