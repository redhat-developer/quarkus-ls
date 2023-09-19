/*******************************************************************************
* Copyright (c) 2021 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.qute.project;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.project.multiple.QuteProjectA;
import com.redhat.qute.project.multiple.QuteProjectB;

public class MockQuteProjectRegistry extends QuteProjectRegistry {

	public static final Range JAVA_CLASS_RANGE = new Range(new Position(0, 0), new Position(0, 0));

	public static final Range JAVA_FIELD_RANGE = new Range(new Position(1, 1), new Position(1, 1));

	public static final Range JAVA_METHOD_RANGE = new Range(new Position(2, 2), new Position(2, 2));

	public static final Range JAVA_STATIC_METHOD_RANGE = new Range(new Position(3, 3), new Position(3, 3));

	public MockQuteProjectRegistry() {
		super(null, null, null, null, null, null, null, null, () -> null);
		super.setDidChangeWatchedFilesSupported(true);
	}

	@Override
	protected QuteProject createProject(ProjectInfo projectInfo) {
		if (QuteQuickStartProject.PROJECT_URI.equals(projectInfo.getUri())) {
			return new QuteQuickStartProject(projectInfo, this);
		}
		if (QuteProjectA.PROJECT_URI.equals(projectInfo.getUri())) {
			return new QuteProjectA(this);
		}
		if (QuteProjectB.PROJECT_URI.equals(projectInfo.getUri())) {
			return new QuteProjectB(this);
		}
		return super.createProject(projectInfo);
	}

	@Override
	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		MockQuteProject project = (MockQuteProject) getProject(params.getProjectUri());
		if (project == null) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		List<JavaTypeInfo> result = project.getJavaTypes() //
				.stream() //
				.filter(type -> (type.getName().startsWith(params.getPattern())
						|| type.getJavaElementSimpleType().startsWith(params.getPattern()))) //
				.collect(Collectors.toList());
		return CompletableFuture.completedFuture(result);
	}

	@Override
	protected CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		MockQuteProject project = (MockQuteProject) getProject(params.getProjectUri());
		if (project == null) {
			return CompletableFuture.completedFuture(null);
		}
		return CompletableFuture.completedFuture(project.getResolvedJavaTypeSync(params.getClassName()));
	}

	@Override
	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		String projectUri = params.getProjectUri();
		MockQuteProject project = (MockQuteProject) getProject(projectUri);
		if (project == null) {
			return CompletableFuture.completedFuture(null);
		}
		Range definitionRange = null;
		String className = params.getSourceType();
		ResolvedJavaTypeInfo classInfo = project.getResolvedJavaTypeSync(className);
		if (classInfo != null) {
			// Find by field, method of Java type
			String fieldName = params.getSourceField();
			if (fieldName != null) {
				// Definition for field
				JavaFieldInfo fieldInfo = QuteProject.findField(classInfo, fieldName);
				if (fieldInfo != null) {
					definitionRange = JAVA_FIELD_RANGE;
				}
			} else {
				// Definition for method
				String methodName = params.getSourceMethod();
				if (methodName != null) {
					String getterMethodName = QuteProject.computeGetterName(methodName);
					String booleanGetterName = QuteProject.computeBooleanGetterName(methodName);
					JavaMethodInfo methodInfo = QuteProject.findMethod(classInfo, methodName, getterMethodName,
							booleanGetterName);
					if (methodInfo != null) {
						definitionRange = JAVA_METHOD_RANGE;
					}
				} else {
					// Definition for class
					definitionRange = JAVA_CLASS_RANGE;
				}
			}
		}

		if (definitionRange == null) {
			// Find by field, method of value resolver
			String methodName = params.getSourceMethod();
			if (methodName != null) {
				JavaMethodInfo methodInfo = project.getMethodValueResolver(className, methodName);
				if (methodInfo != null) {
					definitionRange = JAVA_STATIC_METHOD_RANGE;
				}
			}
		}

		if (definitionRange != null) {
			int index = className.indexOf('<');
			if (index != -1) {
				// ex : java.util.List<E>
				// Remove generic
				className = className.substring(0, index);
			}
			// ex : java/util/List.java
			String javeFileUri = className.replace('.', '/') + ".java";
			Location location = new Location(javeFileUri, definitionRange);
			return CompletableFuture.completedFuture(location);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
	}

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return CompletableFuture.completedFuture(null);
	}

}
