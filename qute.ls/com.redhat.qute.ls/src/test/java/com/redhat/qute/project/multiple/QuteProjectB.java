/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project.multiple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.project.MockQuteProject;
import com.redhat.qute.project.QuteProjectRegistry;

public class QuteProjectB extends MockQuteProject {

	public final static String PROJECT_URI = "project-b";

	public QuteProjectB(QuteProjectRegistry projectRegistry) {
		super(new ProjectInfo(PROJECT_URI, Collections.emptyList(),
				Arrays.asList(new TemplateRootPath(getProjectPath(PROJECT_URI) + "/src/main/resources/templates")), //
				Collections.emptySet()), projectRegistry);
		// project-b dependends from project-a
		super.getProjectDependencies().add(projectRegistry.getProject(new ProjectInfo(QuteProjectA.PROJECT_URI,
				Collections.emptyList(), Arrays.asList(new TemplateRootPath("")), Collections.emptySet())));
	}

	@Override
	protected void fillJavaTypes(List<JavaTypeInfo> types) {

	}

	@Override
	protected void fillResolvedJavaTypes(List<ResolvedJavaTypeInfo> resolvedJavaTypes) {

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

}
