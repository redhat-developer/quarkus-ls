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
package com.redhat.qute.project.roq;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.project.BaseQuteProject;
import com.redhat.qute.project.QuteProjectRegistry;

/**
 * Roq project.
 */
public class RoqProject extends BaseQuteProject {

	public static final String PROJECT_URI = "roq";
	private DataModelProject<DataModelTemplate<?>> dataModel;

	public RoqProject(QuteProjectRegistry projectRegistry) {
		super(new ProjectInfo(PROJECT_URI, //
				getProjectPath(PROJECT_URI), //
				Collections.emptyList(), //
				List.of(new TemplateRootPath(getProjectPath(PROJECT_URI) + "/src/main/resources/templates")), //
				Set.of(getProjectPath(PROJECT_URI) + "/src/main/resources"), //
				Set.of(ProjectFeature.Roq)), projectRegistry);
	}

	@Override
	protected void fillResolvedJavaTypes(List<ResolvedJavaTypeInfo> resolvedJavaTypes) {
		super.fillResolvedJavaTypes(resolvedJavaTypes);
		loadResolvedJavaType("Site.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("RoqCollection.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("RoqCollections.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("Paginator.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("DocumentPage.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("NormalPage.json", resolvedJavaTypes, RoqProject.class);
		loadResolvedJavaType("Page.json", resolvedJavaTypes, RoqProject.class);
	}

	@Override
	protected void fillTemplates(List<DataModelTemplate<DataModelParameter>> templates) {

	}

	@Override
	protected void fillValueResolvers(List<ValueResolverInfo> valueResolvers) {
		valueResolvers.addAll(getDataModel().getValueResolvers());
	}

	private DataModelProject<DataModelTemplate<?>> getDataModel() {
		if (dataModel == null) {
			dataModel = loadDataModel("RoqDataModel.json", RoqProject.class);
		}
		return dataModel;
	}

	@Override
	protected void fillNamespaceResolverInfos(Map<String, NamespaceResolverInfo> namespaces) {

	}

	public static String getFileUri(String fileName) {
		return Paths.get(getRoqProjectPath() + fileName).toAbsolutePath().toUri().toASCIIString();
	}

	public static String getContentFileUri(String fileName) {
		return getFileUri("/content/" + fileName);
	}

	public static String getDataFileUri(String fileName) {
		return getFileUri("/data/" + fileName);
	}

	private static String getRoqProjectPath() {
		return getProjectPath(RoqProject.PROJECT_URI);
	}
}
