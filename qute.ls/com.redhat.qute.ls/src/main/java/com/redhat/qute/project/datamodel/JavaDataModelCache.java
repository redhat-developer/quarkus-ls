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

import com.redhat.qute.commons.InvalidMethodReason;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
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

	private static final CompletableFuture<ResolvedJavaTypeInfo> RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE = CompletableFuture
			.completedFuture(null);

	private final ValueResolversRegistry valueResolversRegistry;

	private final QuteProjectRegistry projectRegistry;

	public JavaDataModelCache(QuteProjectRegistry projectRegistry) {
		this.valueResolversRegistry = new ValueResolversRegistry();
		this.projectRegistry = projectRegistry;
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return projectRegistry.getJavaTypes(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return projectRegistry.getJavaDefinition(params);
	}

	public List<ValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType, String projectUri) {
		List<ValueResolver> matches = new ArrayList<>();
		for (ValueResolver resolver : valueResolversRegistry.getResolvers()) {
			if (matchResolver(javaType, resolver, projectUri)) {
				matches.add(resolver);
			}
		}

		List<ValueResolver> allResolvers = getValueResolvers(projectUri).getNow(null);
		if (allResolvers != null) {
			for (ValueResolver resolver : allResolvers) {
				if (resolver.getNamespace() == null && matchResolver(javaType, resolver, projectUri)) {
					matches.add(resolver);
				}
			}
		}
		return matches;
	}

	/**
	 * Returns true if the given java type match the value resolver (if the type
	 * matches the first parameter of the value resolver method) and false
	 * otherwise.
	 * 
	 * @param javaType   the java type.
	 * @param resolver   the value resolver.
	 * @param projectUri the project Uri.
	 * 
	 * @return true if the given java type match the value resolver (if the type
	 *         matches the first parameter of the value resolver method) and false
	 *         otherwise.
	 */
	private boolean matchResolver(ResolvedJavaTypeInfo javaType, ValueResolver resolver, String projectUri) {
		// Example with following signature:
		// "orEmpty(arg : java.util.List<T>) : java.lang.Iterable<T>"
		JavaParameterInfo parameter = resolver.getParameterAt(0); // arg : java.util.List<T>
		if (parameter == null) {
			return false;
		}
		if (parameter.getJavaType().isSingleGenericType()) {
			// <T>
			return true;
		}
		String parameterType = parameter.getJavaType().getName();
		String resolvedTypeName = javaType.getName();
		if (parameterType.equals(resolvedTypeName)) {
			return true;
		}
		if (javaType.getExtendedTypes() != null) {
			for (String extendedType : javaType.getExtendedTypes()) {
				if (parameterType.equals(extendedType)) {
					return true;
				}
			}
		}
		if (!javaType.getTypeParameters().isEmpty()) {
			ResolvedJavaTypeInfo result = resolveJavaType(resolvedTypeName, projectUri).getNow(null);
			if (result != null && result.getExtendedTypes() != null) {
				for (String extendedType : result.getExtendedTypes()) {
					if (parameterType.equals(extendedType)) {
						return true;
					}
				}
			}
		}
		return false;
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

	private CompletionStage<ResolvedJavaTypeInfo> resolveJavaType(Part current, String projectUri,
			ResolvedJavaTypeInfo resolvedType) {
		String property = current.getPartName();
		JavaMemberInfo member = findMember(property, resolvedType, projectUri);
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
					return resolveJavaType(literalJavaType, projectUri);
				}

				Part lastPart = expression.getLastPart();
				if (lastPart == null) {
					return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
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
						.thenCompose(resolvedType -> {
							if (resolvedType == null) {
								return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
							}
							if (!resolvedType.isIterable() && nullIfDontMatchWithIterable) {
								// case when iterable section is associated with a Java class which is not
								// iterable, the class is not valid
								// Ex:
								// {@org.acme.Item items}
								// {#for item in items}
								// {item.|}
								return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
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

	public JavaMemberInfo findMember(String property, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		if (resolvedType == null) {
			return null;
		}
		// Search in the java root type
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
		// Search in value resolver
		return findValueResolver(property, resolvedType, projectUri);
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

	public ValueResolver findValueResolver(String property, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		// Search in static value resolvers (ex : orEmpty, take, etc)
		List<ValueResolver> resolvers = getResolversFor(resolvedType, projectUri);
		for (ValueResolver resolver : resolvers) {
			if (resolver.match(property)) {
				return resolver;
			}
		}
		// Search in template extension value resolvers retrieved by @TemplateExtension
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

	/**
	 * Returns namespace resolvers from the given Qute project Uri.
	 * 
	 * @param projectUri the Qute project Uri
	 * 
	 * @return namespace resolvers from the given Qute project Uri.
	 */
	public List<ValueResolver> getNamespaceResolvers(String projectUri) {
		List<ValueResolver> allResolvers = getValueResolvers(projectUri).getNow(null);
		if (allResolvers != null) {
			List<ValueResolver> namespaceResolvers = new ArrayList<>();
			for (ValueResolver resolver : allResolvers) {
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
