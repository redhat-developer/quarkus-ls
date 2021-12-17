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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;

public abstract class MockQuteProject extends QuteProject {

	private final List<JavaTypeInfo> typesCache;

	private final List<ResolvedJavaTypeInfo> resolvedTypesCache;

	private List<DataModelTemplate<DataModelParameter>> templates;

	private final List<ValueResolver> resolvers;

	public MockQuteProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider) {
		super(projectInfo, dataModelProvider);
		this.typesCache = createTypes();
		this.resolvedTypesCache = createResolvedTypes();
		this.templates = createTemplates();
		this.resolvers = createValueResolvers();
	}

	public ResolvedJavaTypeInfo getResolvedJavaTypeSync(String typeName) {
		for (ResolvedJavaTypeInfo resolvedJavaType : resolvedTypesCache) {
			if (typeName.equals(resolvedJavaType.getSignature())) {
				return resolvedJavaType;
			} else if (typeName.equals(resolvedJavaType.getIterableType()) && resolvedJavaType.getIterableOf().indexOf('.') == -1) {
				// ex : java.util.List<T>
				return resolvedJavaType;
			}
		}
		return null;
	}

	public List<JavaTypeInfo> getJavaTypes() {
		List<JavaTypeInfo> fromResolved = resolvedTypesCache //
				.stream() //
				.filter(t -> t.getTypeParameters().isEmpty() && !t.isIterable()) //
				.collect(Collectors.toList());
		fromResolved.addAll(typesCache);
		return new ArrayList<>(fromResolved);
	}

	protected static JavaMemberInfo registerField(String fieldSignature, ResolvedJavaTypeInfo resolvedType) {
		JavaFieldInfo member = new JavaFieldInfo();
		member.setSignature(fieldSignature);
		resolvedType.getFields().add(member);
		return member;
	}

	protected static JavaMemberInfo registerMethod(String methodSignature, ResolvedJavaTypeInfo resolvedType) {
		JavaMethodInfo member = new JavaMethodInfo();
		member.setSignature(methodSignature);
		resolvedType.getMethods().add(member);
		return member;
	}

	protected static JavaTypeInfo createJavaTypeInfo(String typeName, JavaTypeKind kind, List<JavaTypeInfo> cache) {
		JavaTypeInfo typeInfo = new JavaTypeInfo();
		typeInfo.setSignature(typeName);
		typeInfo.setKind(kind);
		cache.add(typeInfo);
		return typeInfo;
	}

	protected static ResolvedJavaTypeInfo createResolvedJavaTypeInfo(String typeName, List<ResolvedJavaTypeInfo> cache,
			ResolvedJavaTypeInfo... extended) {
		return createResolvedJavaTypeInfo(typeName, null, null, cache, extended);
	}

	protected static ResolvedJavaTypeInfo createResolvedJavaTypeInfo(String signature, String iterableType,
			String iterableOf, List<ResolvedJavaTypeInfo> cache, ResolvedJavaTypeInfo... extended) {
		ResolvedJavaTypeInfo resolvedType = new ResolvedJavaTypeInfo();
		resolvedType.setKind(JavaTypeKind.Class);
		resolvedType.setSignature(signature);
		resolvedType.setIterableType(iterableType);
		resolvedType.setIterableOf(iterableOf);
		resolvedType.setFields(new ArrayList<>());
		resolvedType.setMethods(new ArrayList<>());
		if (extended != null) {
			List<String> extendedTypes = Stream.of(extended).map(type -> type.getSignature())
					.collect(Collectors.toList());
			resolvedType.setExtendedTypes(extendedTypes);
		}
		resolvedType.setInvalidMethods(new HashMap<>());
		cache.add(resolvedType);
		return resolvedType;
	}

	@Override
	protected synchronized CompletableFuture<ExtendedDataModelProject> loadDataModelProject() {
		DataModelProject<DataModelTemplate<DataModelParameter>> project = new DataModelProject<DataModelTemplate<DataModelParameter>>();
		project.setTemplates(templates);
		project.setValueResolvers(resolvers);
		return CompletableFuture.completedFuture(new ExtendedDataModelProject(project));
	}

	protected static ValueResolver createValueResolver(String signature, String sourceType) {
		ValueResolver resolver = new ValueResolver();
		resolver.setSignature(signature);
		resolver.setSourceType(sourceType);
		return resolver;
	}

	protected abstract List<JavaTypeInfo> createTypes();

	protected abstract List<ResolvedJavaTypeInfo> createResolvedTypes();

	protected abstract List<DataModelTemplate<DataModelParameter>> createTemplates();

	protected abstract List<ValueResolver> createValueResolvers();

}
