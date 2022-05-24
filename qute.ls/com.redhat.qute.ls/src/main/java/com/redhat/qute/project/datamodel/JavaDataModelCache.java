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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.JavaMemberResult;
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache implements DataModelTemplateProvider {

	private static final CompletableFuture<ResolvedJavaTypeInfo> RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	private final QuteProjectRegistry projectRegistry;

	public JavaDataModelCache(QuteProjectRegistry projectRegistry) {
		this.projectRegistry = projectRegistry;
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return projectRegistry.getJavaTypes(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return projectRegistry.getJavaDefinition(params);
	}

	public List<MethodValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType, String projectUri) {
		return projectRegistry.getResolversFor(javaType, projectUri);
	}

	public List<ValueResolver> getGlobalVariables(String projectUri) {
		return projectRegistry.getGlobalVariables(projectUri);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className, String projectUri) {
		return projectRegistry.resolveJavaType(className, projectUri);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parameter parameter, String projectUri) {
		Expression expression = parameter.getJavaTypeExpression();
		if (expression != null) {
			Part lastPart = expression.getLastPart();
			if (lastPart != null) {
				return resolveJavaType(lastPart, projectUri);
			}
		}
		return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parts parts, int partIndex, String projectUri,
			boolean nullIfDontMatchWithIterable) {
		CompletableFuture<ResolvedJavaTypeInfo> future = null;
		for (int i = 0; i < partIndex + 1; i++) {
			Part current = ((Part) parts.getChild(i));
			switch (current.getPartKind()) {
			case Object:
				ObjectPart objectPart = (ObjectPart) current;
				future = resolveJavaType(objectPart, projectUri, nullIfDontMatchWithIterable);
				break;
			case Property:
			case Method:
				if (future != null) {
					future = future //
							.thenCompose(resolvedType -> {
								if (resolvedType == null) {
									return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
								}
								return resolveJavaType(current, projectUri, resolvedType);
							});
				}
				break;
			default:
			}
		}
		return future != null ? future : RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
	}

	private CompletionStage<ResolvedJavaTypeInfo> resolveJavaType(Part part, String projectUri,
			ResolvedJavaTypeInfo resolvedType) {
		JavaMemberInfo member = findMember(part, resolvedType, projectUri);
		if (member == null) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}
		String memberType = member.resolveJavaElementType(resolvedType);
		return resolveJavaType(memberType, projectUri);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Part part, String projectUri) {
		return resolveJavaType(part, projectUri, true);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Part part, String projectUri,
			boolean nullIfDontMatchWithIterable) {
		Parts parts = part.getParent();
		int partIndex = parts.getPartIndex(part);
		return resolveJavaType(parts, partIndex, projectUri, nullIfDontMatchWithIterable);
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(ObjectPart objectPart, String projectUri,
			boolean nullIfDontMatchWithIterable) {
		CompletableFuture<ResolvedJavaTypeInfo> future = null;
		JavaTypeInfoProvider javaTypeInfo = objectPart.resolveJavaType();
		if (javaTypeInfo == null) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}
		String javaType = javaTypeInfo.getJavaType();
		if (StringUtils.isEmpty(javaType)) {
			Expression expression = javaTypeInfo.getJavaTypeExpression();
			if (expression != null) {
				String literalJavaType = expression.getLiteralJavaType();
				if (literalJavaType != null) {
					future = resolveJavaType(literalJavaType, projectUri);
				} else {
					Part lastPart = expression.getLastPart();
					if (lastPart == null) {
						return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
					}
					future = resolveJavaType(lastPart, projectUri);
				}
			}
		}

		if (future == null) {
			future = resolveJavaType(javaType, projectUri);
		}
		Node node = javaTypeInfo.getJavaTypeOwnerNode();
		Section section = getOwnerSection(node);
		if (section != null) {
			if (section.isIterable()) {
				future = future //
						.thenCompose(resolvedType -> {
							if (resolvedType == null) {
								return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
							}
							if (!resolvedType.isIterable()) {
								// case when iterable section is associated with a Java class which is not
								// iterable.

								if (resolvedType.isInteger()) {
									// Special case with int, Integer

									// The for statement also works with integers, starting from 1. In the example
									// below, considering that total = 3:

									// {#for i in total}
									// {i}:
									// {/for}
									return resolveJavaType(resolvedType.getName(), projectUri);
								}

								if (nullIfDontMatchWithIterable) {
									// -> the class is not valid
									// Ex:
									// {@org.acme.Item items}
									// {#for item in items}
									// {item.|}
									return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
								}
							}
							// valid case
							// Ex:
							// {@java.util.List<org.acme.Item> items}
							// {#for item in items}
							// {item.|}

							// Here
							// - resolvedType = java.util.List<org.acme.Item>
							// - iterTypeName = org.acme.Item

							// Resolve org.acme.Item
							String iterTypeName = resolvedType.getIterableOf();
							return resolveJavaType(iterTypeName, projectUri);
						});
			}
		}
		return future;
	}

	private Section getOwnerSection(Node node) {
		if (node == null) {
			return null;
		}
		if (node.getKind() == NodeKind.Parameter) {
			return ((Parameter) node).getOwnerSection();
		}
		return null;
	}

	@Override
	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		return projectRegistry.getDataModelTemplate(template);
	}

	public InvalidMethodReason getInvalidMethodReason(String property, ResolvedJavaTypeInfo resolvedType,
			String projectUri) {
		if (resolvedType == null) {
			return InvalidMethodReason.Unknown;
		}
		// Search in the java root type
		InvalidMethodReason reason = resolvedType.getInvalidMethodReason(property);
		if (reason != null) {
			return reason;
		}

		if (resolvedType.getExtendedTypes() != null) {
			// Search in extended types
			for (String extendedType : resolvedType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedExtendedType = resolveJavaType(extendedType, projectUri).getNow(null);
				if (resolvedExtendedType != null) {
					reason = resolvedExtendedType.getInvalidMethodReason(property);
					if (reason != null) {
						return reason;
					}
				}
			}
		}
		return null;
	}

	public JavaMemberInfo findMember(Part part, ResolvedJavaTypeInfo baseType, String projectUri) {
		return findMember(baseType, part.getPartName(), projectUri);
	}

	public JavaMemberInfo findMember(ResolvedJavaTypeInfo baseType, String property, String projectUri) {
		return projectRegistry.findMember(baseType, property, projectUri);
	}

	public JavaMemberResult findProperty(Part part, ResolvedJavaTypeInfo baseType, boolean nativeMode,
			String projectUri) {
		return projectRegistry.findProperty(baseType, part.getPartName(), nativeMode, projectUri);
	}

	public boolean hasNamespace(String namespace, String projectUri) {
		return projectRegistry.hasNamespace(namespace, projectUri);
	}

	public CompletableFuture<JavaElementInfo> findJavaElementWithNamespace(String namespace, String partName,
			String projectUri) {
		return projectRegistry.findJavaElementWithNamespace(namespace, partName, projectUri);
	}

	public CompletableFuture<JavaElementInfo> findGlobalVariableJavaElement(String partName, String projectUri) {
		return projectRegistry.findGlobalVariableJavaElement(partName, projectUri);
	}

	public List<ValueResolver> getNamespaceResolvers(String namespace, String projectUri) {
		return projectRegistry.getNamespaceResolvers(namespace, projectUri);
	}

	public CompletableFuture<NamespaceResolverInfo> getNamespaceResolverInfo(String namespace, String projectUri) {
		return projectRegistry.getNamespaceResolverInfo(namespace, projectUri);
	}

	public JavaMemberResult findMethod(ResolvedJavaTypeInfo baseType, String namespace, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, boolean nativeMode, String projectUri) {
		return projectRegistry.findMethod(baseType, namespace, methodName, parameterTypes, nativeMode, projectUri);
	}

}
