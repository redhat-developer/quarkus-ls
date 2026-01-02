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
package com.redhat.qute.project.datamodel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.redhat.qute.commons.JavaElementKind;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.resolvers.MessageResolverData;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.parser.expression.NamespacePart;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.resolvers.FieldValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MessageMethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.TypeValueResolver;
import com.redhat.qute.utils.JSONUtility;
import com.redhat.qute.utils.StringUtils;

public class ExtendedDataModelProject extends DataModelProject<ExtendedDataModelTemplate> {

	private final QuteProject project;

	private final Set<String> allNamespaces;

	private final Set<String> allTemplateExtensionsClasses;

	private final List<TypeValueResolver> typeValueResolvers;

	private final List<FieldValueResolver> fieldValueResolvers;

	private final List<MethodValueResolver> methodValueResolvers;

	private final Map<String, String> similarNamespaces;

	private Set<String> javaTypesSupportedInNativeMode;

	public ExtendedDataModelProject(DataModelProject<DataModelTemplate<DataModelParameter>> dataModelProject,
			QuteProject project) {
		this.project = project;
		super.setTemplates(createTemplates(dataModelProject.getTemplates()));
		super.setNamespaceResolverInfos(dataModelProject.getNamespaceResolverInfos());

		typeValueResolvers = new ArrayList<>();
		fieldValueResolvers = new ArrayList<>();
		methodValueResolvers = new ArrayList<MethodValueResolver>();
		updateValueResolvers(typeValueResolvers, fieldValueResolvers, methodValueResolvers, dataModelProject);
		Collections.sort(methodValueResolvers, (r1, r2) -> {
			if (r1.isMatchNameAny()) {
				return 1;
			}
			if (r2.isMatchNameAny()) {
				return -1;
			}
			return 0;
		});
		allNamespaces = getAllNamespaces(dataModelProject);
		allTemplateExtensionsClasses = getAllTemplateExtensionsClasses(dataModelProject);
		similarNamespaces = getSimilarNamespaces(dataModelProject);
	}

	private static void updateValueResolvers(List<TypeValueResolver> typeValueResolvers,
			List<FieldValueResolver> fieldValueResolvers, List<MethodValueResolver> methodValueResolvers,
			DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		project.getValueResolvers().forEach(resolver -> {
			JavaElementKind kind = resolver.getJavaElementKind();
			switch (kind) {
			case TYPE:
				TypeValueResolver typeValueResolver = new TypeValueResolver();
				typeValueResolver.setNamed(resolver.getNamed());
				typeValueResolver.setNamespace(resolver.getNamespace());
				typeValueResolver.setSignature(resolver.getSignature());
				typeValueResolver.setSourceType(resolver.getSourceType());
				typeValueResolver.setGlobalVariable(resolver.isGlobalVariable());
				typeValueResolver.setKind(resolver.getKind());
				typeValueResolvers.add(typeValueResolver);
				break;
			case FIELD:
				FieldValueResolver fieldValueResolver = new FieldValueResolver();
				fieldValueResolver.setNamed(resolver.getNamed());
				fieldValueResolver.setNamespace(resolver.getNamespace());
				fieldValueResolver.setSignature(resolver.getSignature());
				fieldValueResolver.setSourceType(resolver.getSourceType());
				fieldValueResolver.setGlobalVariable(resolver.isGlobalVariable());
				fieldValueResolver.setKind(resolver.getKind());
				fieldValueResolvers.add(fieldValueResolver);
				break;
			case METHOD:
				MethodValueResolver methodValueResolver = resolver.getKind() == ValueResolverKind.Message
						? new MessageMethodValueResolver()
						: new MethodValueResolver();
				methodValueResolver.setNamed(resolver.getNamed());
				methodValueResolver.setNamespace(resolver.getNamespace());
				methodValueResolver.setMatchNames(resolver.getMatchNames());
				methodValueResolver.setSignature(resolver.getSignature());
				methodValueResolver.setSourceType(resolver.getSourceType());
				methodValueResolver.setGlobalVariable(resolver.isGlobalVariable());
				methodValueResolver.setKind(resolver.getKind());
				if (resolver.getKind() == ValueResolverKind.Message && resolver.getData() != null) {
					MessageResolverData data = JSONUtility.toModel(resolver.getData(), MessageResolverData.class);
					((MessageMethodValueResolver) methodValueResolver).setLocale(data.getLocale());
					((MessageMethodValueResolver) methodValueResolver).setMessage(data.getMessage());
				}
				methodValueResolvers.add(methodValueResolver);
				break;
			default:
				break;

			}
		});
	}

	private static Set<String> getAllNamespaces(DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		Set<String> allNamespaces = project.getValueResolvers() //
				.stream() //
				.filter(resolver -> resolver.getNamespace() != null) //
				.map(resolver -> resolver.getNamespace()) //
				.distinct() //
				.collect(Collectors.toSet());
		allNamespaces.add(NamespacePart.DATA_NAMESPACE);
		for (NamespaceResolverInfo info : project.getNamespaceResolverInfos().values()) {
			info.getNamespaces().forEach(namespace -> {
				allNamespaces.add(namespace);
			});
		}
		return allNamespaces;
	}

	private static Set<String> getAllTemplateExtensionsClasses(
			DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		return project.getValueResolvers().stream() //
				.filter(resolver -> ValueResolverKind.TemplateExtensionOnClass.equals(resolver.getKind())
						&& !resolver.isBinary()) //
				.map(resolver -> resolver.getSourceType()) //
				.distinct() //
				.collect(Collectors.toSet());
	}

	private static Map<String, String> getSimilarNamespaces(
			DataModelProject<DataModelTemplate<DataModelParameter>> project) {
		Map<String, String> similar = new HashMap<>();
		for (Entry<String, NamespaceResolverInfo> entry : project.getNamespaceResolverInfos().entrySet()) {
			String mainNamespace = entry.getKey();
			List<String> namespaces = entry.getValue().getNamespaces();
			for (String namespace : namespaces) {
				similar.put(namespace, mainNamespace);
			}
		}
		return similar;
	}

	private List<ExtendedDataModelTemplate> createTemplates(List<DataModelTemplate<DataModelParameter>> templates) {
		if (templates == null || templates.isEmpty()) {
			return Collections.emptyList();
		}
		return templates.stream() //
				.map(template -> {
					return new ExtendedDataModelTemplate(template);
				}) //
				.collect(Collectors.toList());
	}

	public Set<String> getAllNamespaces() {
		return allNamespaces;
	}

	public Set<String> getAllTemplateExtensionsClasses() {
		return allTemplateExtensionsClasses;
	}

	public String getSimilarNamespace(String namespace) {
		String similar = similarNamespaces.get(namespace);
		return similar != null ? similar : namespace;
	}

	public List<TypeValueResolver> getTypeValueResolvers() {
		return typeValueResolvers;
	}

	/**
	 * Return the list of value resolvers which belong to this project.
	 * 
	 * @return the list of value resolvers which belong to this project.
	 */
	public List<MethodValueResolver> getMethodValueResolvers() {
		return methodValueResolvers;
	}

	/**
	 * Return the list of value resolvers which belong to this project.
	 * 
	 * @return the list of value resolvers which belong to this project.
	 */
	public List<FieldValueResolver> getFieldValueResolvers() {
		return fieldValueResolvers;
	}

	public NamespaceResolverInfo getNamespaceResolver(String namespace) {
		String mainNamespace = getSimilarNamespace(namespace);
		return super.getNamespaceResolverInfos().get(mainNamespace);
	}

	public Set<String> getJavaTypesSupportedInNativeMode() {
		if (javaTypesSupportedInNativeMode == null) {
			javaTypesSupportedInNativeMode = loadJavaTypesSupportedInNativeMode();
		}
		return javaTypesSupportedInNativeMode;
	}

	private synchronized Set<String> loadJavaTypesSupportedInNativeMode() {
		if (javaTypesSupportedInNativeMode != null) {
			return javaTypesSupportedInNativeMode;
		}
		Set<String> javaTypesSupportedInNativeMode = new HashSet<String>();
		getTemplates().forEach(t -> {
			List<ExtendedDataModelParameter> parameters = t.getParameters();
			for (ExtendedDataModelParameter parameter : parameters) {
				if (!parameter.isDataMethodInvocation()) {
					String sourceType = parameter.getJavaType();
					if (StringUtils.isEmpty(sourceType)) {
						return;
					}
					if (sourceType.contains("<")) {
						JavaTypeInfo javaType = new JavaTypeInfo();
						javaType.setSignature(sourceType);
						List<JavaParameterInfo> javaTypeParameters = javaType.getTypeParameters();
						for (JavaParameterInfo parameterType : javaTypeParameters) {
							javaTypesSupportedInNativeMode.add(parameterType.getType());
						}
					} else {
						javaTypesSupportedInNativeMode.add(sourceType);
					}
				}
			}
		});
		return javaTypesSupportedInNativeMode;
	}

	public Set<Path> getSourcePaths() {
		return project.getSourcePaths();
	}

}