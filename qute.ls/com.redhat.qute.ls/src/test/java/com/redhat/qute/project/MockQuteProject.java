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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaTypeKind;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;

public abstract class MockQuteProject extends QuteProject {

	private static final Logger LOGGER = Logger.getLogger(MockQuteProject.class.getName());

	private final List<JavaTypeInfo> typesCache;

	private final List<ResolvedJavaTypeInfo> resolvedTypesCache;

	private List<DataModelTemplate<DataModelParameter>> templates;

	private final List<ValueResolverInfo> valueResolvers;

	private final Map<String, NamespaceResolverInfo> namespaceResolverInfos;

	public MockQuteProject(ProjectInfo projectInfo, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider tagProvider) {
		super(projectInfo, dataModelProvider, tagProvider);
		this.typesCache = createTypes();
		this.resolvedTypesCache = createResolvedTypes();
		this.templates = createTemplates();
		this.valueResolvers = createValueResolvers();
		this.namespaceResolverInfos = createNamespaceResolverInfos();
	}

	public ResolvedJavaTypeInfo getResolvedJavaTypeSync(String typeName) {
		for (ResolvedJavaTypeInfo resolvedJavaType : resolvedTypesCache) {
			if (typeName.equals(resolvedJavaType.getSignature())) {
				return resolvedJavaType;
			} else if (typeName.equals(resolvedJavaType.getIterableType())
					&& resolvedJavaType.getIterableOf().indexOf('.') == -1) {
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

	protected static ResolvedJavaTypeInfo createResolvedJavaTypeInfo(String typeName, List<ResolvedJavaTypeInfo> cache, boolean isSource,
			ResolvedJavaTypeInfo... extended) {
		return createResolvedJavaTypeInfo(typeName, null, null, cache, isSource, extended);
	}

	protected static ResolvedJavaTypeInfo createResolvedJavaTypeInfo(String signature, String iterableType,
			String iterableOf, List<ResolvedJavaTypeInfo> cache, boolean isSource, ResolvedJavaTypeInfo... extended) {
		ResolvedJavaTypeInfo resolvedType = new ResolvedJavaTypeInfo();
		resolvedType.setKind(JavaTypeKind.Class);
		resolvedType.setSource(isSource);
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
		project.setValueResolvers(valueResolvers);
		project.setNamespaceResolverInfos(namespaceResolverInfos);
		return CompletableFuture.completedFuture(new ExtendedDataModelProject(project));
	}

	protected static ValueResolverInfo createValueResolver(String namespace, String named, String matchName,
			String sourceType, String signature) {
		return createValueResolver(namespace, named, matchName, sourceType, signature, false);
	}

	protected static ValueResolverInfo createValueResolver(String namespace, String named, String matchName,
			String sourceType, String signature, boolean globalVariable) {
		ValueResolverInfo resolver = new ValueResolverInfo();
		resolver.setNamespace(namespace);
		resolver.setNamed(named);
		resolver.setMatchName(matchName);
		resolver.setSourceType(sourceType);
		resolver.setSignature(signature);
		resolver.setGlobalVariable(globalVariable);
		return resolver;
	}

	protected abstract List<JavaTypeInfo> createTypes();

	protected abstract List<ResolvedJavaTypeInfo> createResolvedTypes();

	protected abstract List<DataModelTemplate<DataModelParameter>> createTemplates();

	protected abstract List<ValueResolverInfo> createValueResolvers();

	protected abstract Map<String, NamespaceResolverInfo> createNamespaceResolverInfos();

	public JavaMethodInfo getMethodValueResolver(String typeName, String methodName) {
		try {
			List<MethodValueResolver> resolvers = super.getDataModelProject().get().getMethodValueResolvers();
			for (MethodValueResolver resolver : resolvers) {
				if (typeName.equals(resolver.getSourceType()) && methodName.equals(resolver.getMethodName())) {
					return resolver;
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error while gettings method value resolvers.");
		}
		return null;
	}

}
