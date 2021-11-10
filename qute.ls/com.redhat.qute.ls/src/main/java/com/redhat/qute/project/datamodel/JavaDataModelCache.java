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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
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
import com.redhat.qute.project.QuteProjectRegistry;
import com.redhat.qute.utils.StringUtils;

public class JavaDataModelCache implements DataModelTemplateProvider {

	private static final CompletableFuture<ResolvedJavaTypeInfo> RESOLVED_JAVA_CLASSINFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	private final ValueResolversRegistry valueResolversRegistry;

	private final QuteProjectRegistry projectRegistry;

	public JavaDataModelCache(QuteProjectRegistry projectRegistry) {
		this.valueResolversRegistry = new ValueResolversRegistry();
		this.projectRegistry = projectRegistry;
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaClasses(QuteJavaTypesParams params) {
		return projectRegistry.getJavaClasses(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return projectRegistry.getJavaDefinition(params);
	}

	public List<ValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType) {
		return valueResolversRegistry.getResolversFor(javaType);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className, String projectUri) {
		return projectRegistry.resolveJavaType(className, projectUri);
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parameter object, String projectUri) {
		Expression expression = object.getJavaTypeExpression();
		if (expression != null) {
			Part lastPart = expression.getLastPart();
			if (lastPart != null) {
				return resolveJavaType(lastPart, projectUri);
			}
		}
		return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
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
							.thenCompose(resolvedClass -> {
								if (resolvedClass == null) {
									return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
								}
								return resolveJavaType(current, projectUri, resolvedClass);
							});
				}
				break;
			default:
			}
		}
		return future != null ? future : RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
	}

	private CompletionStage<ResolvedJavaTypeInfo> resolveJavaType(Part current, String projectUri,
			ResolvedJavaTypeInfo resolvedClass) {
		String property = current.getPartName();
		JavaMemberInfo member = findMember(property, resolvedClass, projectUri);
		if (member == null) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		String memberType = member.getMemberType();
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
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		String javaType = javaTypeInfo.getJavaType();
		if (StringUtils.isEmpty(javaType)) {
			Expression expression = javaTypeInfo.getJavaTypeExpression();
			if (expression != null) {
				String literalJavaType = expression.getLiteralJavaType();
				if (literalJavaType != null) {
					return resolveJavaType(literalJavaType, projectUri);
				}

				Part lastPart = expression.getLastPart();
				if (lastPart == null) {
					return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
				}
				future = resolveJavaType(lastPart, projectUri);
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
						.thenCompose(resolvedClass -> {
							if (resolvedClass == null) {
								return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
							}
							if (!resolvedClass.isIterable() && nullIfDontMatchWithIterable) {
								// case when iterable section is associated with a Java class which is not
								// iterable, the class is not valid
								// Ex:
								// {@org.acme.Item items}
								// {#for item in items}
								// {item.|}
								return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
							}
							// valid case
							// Ex:
							// {@java.util.List<org.acme.Item> items}
							// {#for item in items}
							// {item.|}

							// Here
							// - resolvedClass = java.util.List<org.acme.Item>
							// - iterClassName = org.acme.Item

							// Resolve org.acme.Item
							String iterClassName = resolvedClass.getIterableOf();
							return resolveJavaType(iterClassName, projectUri);
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

	public JavaMemberInfo findMember(String property, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		// Search in he java root type
		JavaMemberInfo memberInfo = resolvedType.findMember(property);
		if (memberInfo != null) {
			return memberInfo;
		}
		if (resolvedType.getExtendedTypes() != null) {
			// Search in extended types
			for (String extendedType : resolvedType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedExtendedType = resolveJavaType(extendedType, projectUri).getNow(null);
				if (resolvedExtendedType != null) {
					memberInfo = resolvedExtendedType.findMember(property);
					if (memberInfo != null) {
						return memberInfo;
					}
				}
			}
		}
		return findValueResolver(property, resolvedType, projectUri);
	}

	public ValueResolver findValueResolver(String property, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		// Search in value resolvers (ex : orEmpty, take, etc)
		List<ValueResolver> resolvers = valueResolversRegistry.getResolversFor(resolvedType);
		for (ValueResolver resolver : resolvers) {
			if (resolver.match(property)) {
				return resolver;
			}
		}
		resolvers = getValueResolvers(projectUri).getNow(null);
		if (resolvers != null) {
			for (ValueResolver resolver : resolvers) {
				if (resolver.match(property)) {
					return resolver;
				}
			}
		}
		return null;
	}

	private CompletableFuture<List<ValueResolver>> getValueResolvers(String projectUri) {
		return projectRegistry.getValueResolvers(projectUri);
	}

	public List<ValueResolver> getNamespaceResolvers(String projectUri) {
		List<ValueResolver> resolvers = getValueResolvers(projectUri).getNow(null);
		if (resolvers != null) {
			List<ValueResolver> namespaceResolvers = new ArrayList<>();
			for (ValueResolver resolver : resolvers) {
				if (resolver.getNamespace() != null) {
					namespaceResolvers.add(resolver);
				}
			}
			return namespaceResolvers;
		}
		return Collections.emptyList();
	}

	public boolean hasNamespace(String namespace, String projectUri) {
		List<ValueResolver> resolvers = getValueResolvers(projectUri).getNow(null);
		if (resolvers != null) {
			for (ValueResolver resolver : resolvers) {
				if (namespace.equals(resolver.getNamespace())) {
					return true;
				}
			}
		}
		return false;
	}

}
