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
package com.redhat.qute.project;

import static com.redhat.qute.parser.template.LiteralSupport.getPrimitiveObjectType;
import static com.redhat.qute.services.QuteCompletableFutures.NOT_ITERABLE_JAVA_TYPE;
import static com.redhat.qute.services.QuteCompletableFutures.RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.RESOLVED_JAVA_TYPE_NOT_ITERABLE_FUTURE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.parser.expression.ObjectPart;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.expression.Parts;
import com.redhat.qute.parser.template.Expression;
import com.redhat.qute.parser.template.ExpressionParameter;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.Node;
import com.redhat.qute.parser.template.NodeKind;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Section;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.utils.StringUtils;

/**
 * Cache for Java data model for a given Qute project.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaDataModelCache {

	private static final Map<String, String> autoboxing;

	private static final Map<String, CompletableFuture<ResolvedJavaTypeInfo>> javaPrimitiveTypes;

	static {
		javaPrimitiveTypes = new HashMap<>();
		autoboxing = new HashMap<>();
		JavaTypeInfo.PRIMITIVE_TYPES.forEach(type -> registerPrimitiveType(type));
	}

	private static void registerPrimitiveType(String type) {
		// int
		ResolvedJavaTypeInfo primitiveType = new ResolvedJavaTypeInfo();
		primitiveType.setSignature(type);
		javaPrimitiveTypes.put(primitiveType.getSignature(), CompletableFuture.completedFuture(primitiveType));

		// int[]
		ResolvedJavaTypeInfo primitiveTypeArray = new ResolvedJavaTypeInfo();
		primitiveTypeArray.setSignature(type + "[]");
		primitiveTypeArray.setIterableOf(type);
		javaPrimitiveTypes.put(primitiveTypeArray.getSignature(),
				CompletableFuture.completedFuture(primitiveTypeArray));

		String objectType = getPrimitiveObjectType(type);
		if (objectType != null) {
			autoboxing.put(type, type);
			autoboxing.put(objectType, type);
		}
	}

	private final QuteProject project;

	public JavaDataModelCache(QuteProject project) {
		this.project = project;
	}

	/**
	 * Returns the Java type of the given parameter.
	 * 
	 * @param parameter the parameter
	 * 
	 * @return the Java type of the given parameter.
	 */
	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parameter parameter) {
		Expression expression = parameter.getJavaTypeExpression();
		if (expression instanceof ExpressionParameter) {
			ExpressionParameter expressionParameter = (ExpressionParameter) expression;
			if (!StringUtils.isEmpty(expressionParameter.getLiteralJavaType())) {
				return resolveJavaType(expressionParameter.getLiteralJavaType());
			}
		}
		if (expression != null) {
			Part lastPart = expression.getLastPart();
			if (lastPart != null) {
				return resolveJavaType(lastPart);
			}
		}
		return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
	}

	/**
	 * Returns the Java type of the given part.
	 * 
	 * @param part the object, property part.
	 * 
	 * @return the Java type of the given part.
	 */
	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Part part) {
		Parts parts = part.getParent();
		int partIndex = parts.getPartIndex(part);
		return resolveJavaType(parts, partIndex);
	}

	/**
	 * Returns the Java type of the given class name.
	 * 
	 * @param className the Java type name.
	 * 
	 * @return the Java type of the given class name.
	 */
	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className) {
		return resolveJavaType(className, false);
	}

	/**
	 * Returns the Java type of the given class name.
	 * 
	 * @param className the Java type name.
	 * 
	 * @return the Java type of the given class name.
	 */
	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className, boolean wrap) {
		CompletableFuture<ResolvedJavaTypeInfo> result = resolveJavaType(className, null);
		if (wrap) {
			return wrapObject(result);
		}
		return result;
	}

	/**
	 * Returns the Java type of the given class name.
	 * 
	 * @param className the Java type name.
	 * @aram javaTypeInfo the Java type information.
	 * 
	 * @return the Java type of the given class name.
	 */
	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String className,
			JavaTypeInfoProvider javaTypeInfo) {
		CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(className);
		if (future != null) {
			return future;
		}
		return resolveJavaType(className, javaTypeInfo, new HashSet<>());
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String javaTypeName,
			JavaTypeInfoProvider javaTypeInfo,
			Set<String> visited) {

		if (StringUtils.isEmpty(javaTypeName)) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}
		if (visited.contains(javaTypeName)) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}
		visited.add(javaTypeName);

		CompletableFuture<ResolvedJavaTypeInfo> primitiveType = javaPrimitiveTypes.get(javaTypeName);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}

		// Try to get the Java type from the cache
		CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(javaTypeName);
		if (future == null) {
			// The Java type needs to be loaded.
			if (javaTypeName.endsWith("[]")) {
				// Array case (ex : org.acme.Item[]), try to to get the, get the item type (ex :
				// org.acme.Item)
				future = resolveJavaType(javaTypeName.substring(0, javaTypeName.length() - 2), javaTypeInfo) //
						.thenApply(item -> {
							ResolvedJavaTypeInfo array = new ResolvedJavaTypeInfo();
							array.setSignature(javaTypeName);
							return array;
						});
			} else {
				// Resolve Java type without generic.
				// ex :
				// - java.util.List<java.lang.String> -> java.util.List
				// - java.lang.String -> java.lang.String
				CompletableFuture<ResolvedJavaTypeInfo> loadResolveJavaTypeFuture = resolveJavaTypeWithoutGeneric(
						javaTypeName, javaTypeInfo);
				future = loadResolveJavaTypeFuture //
						.thenCompose(resolvedJavaType -> {
							if (resolvedJavaType != null) {

								// Create generic Map if the given Java type name declares some generic.
								Map<String, String> generics = resolvedJavaType.createGenericMap(javaTypeName);
								// Update the Java type (apply generic + update references of this Java type for
								// fields / methods).
								resolvedJavaType = updateJavaType(resolvedJavaType, generics);
								visited.add(resolvedJavaType.getSignature());

								final ResolvedJavaTypeInfo resolvedJavaTypeWithLoadedDeps = resolvedJavaType;
								// Load extended Java types
								if (resolvedJavaType.getExtendedTypes() != null) {
									Set<CompletableFuture<ResolvedJavaTypeInfo>> resolvingExtendedFutures = new HashSet<>();
									for (String extendedType : resolvedJavaType.getExtendedTypes()) {
										resolvingExtendedFutures
												.add(resolveJavaType(extendedType, javaTypeInfo, visited));
									}
									if (!resolvingExtendedFutures.isEmpty()) {
										CompletableFuture<Void> allFutures = CompletableFuture
												.allOf(resolvingExtendedFutures.toArray(
														new CompletableFuture[resolvingExtendedFutures.size()]));
										return allFutures //
												.thenApply(all -> {
													updateIterableAndWrappedObject(
															resolvedJavaTypeWithLoadedDeps,
															resolvingExtendedFutures);
													return resolvedJavaTypeWithLoadedDeps;
												});
									}
								}
							}
							return CompletableFuture.completedFuture(resolvedJavaType);
						});
			}
			project.registerResolvedJavaType(javaTypeName, future);
		}
		return future;
	}

	public static ResolvedJavaTypeInfo updateJavaType(ResolvedJavaTypeInfo simpleOrGenericType,
			Map<String, String> genericMap) {
		boolean hasGeneric = genericMap != null;
		ResolvedJavaTypeInfo javaType = simpleOrGenericType;
		if (hasGeneric) {
			// Create a new instance of ResolvedJavaTypeInfo with apply of generic.
			javaType = new ResolvedGenericJavaTypeInfo(simpleOrGenericType, genericMap);
		} else {
			// Update Java fields
			for (JavaFieldInfo field : simpleOrGenericType.getFields()) {
				// Reference the Java type for the current field
				field.setJavaType(javaType);
			}

			// Update Java methods
			for (JavaMethodInfo method : simpleOrGenericType.getMethods()) {
				// Reference the Java type for the current method
				method.setJavaType(javaType);
			}
		}
		return javaType;
	}

	private void updateIterableAndWrappedObject(final ResolvedJavaTypeInfo resolvedJavaType,
			Set<CompletableFuture<ResolvedJavaTypeInfo>> resolvingExtendedFutures) {
		Set<ResolvedJavaTypeInfo> extendedTypes = resolvingExtendedFutures
				.stream()
				.map(r -> r.getNow(null))
				.filter(r -> r != null)
				.collect(Collectors.toSet());
		updateIterableAndWrappedObjectSync(resolvedJavaType, extendedTypes);
	}

	private void updateIterableAndWrappedObjectSync(final ResolvedJavaTypeInfo resolvedJavaType,
			Set<ResolvedJavaTypeInfo> extendedTypes) {
		String iterableOf = null;
		boolean wrappedObject = false;
		for (ResolvedJavaTypeInfo extendedType : extendedTypes) {
			// Update the iterable of the loaded Java type
			if (ResolvedJavaTypeInfo.isIterable(extendedType.getName())) {
				extendedType.isIterable();
				iterableOf = extendedType.getIterableOf();
				break;
			} else if (extendedType.getIterableOf() != null) {
				iterableOf = extendedType.getIterableOf();
				break;
			}
		}
		for (ResolvedJavaTypeInfo extendedType : extendedTypes) {
			if (ResolvedJavaTypeInfo.isWrapperType(extendedType.getName())) {
				wrappedObject = true;
				break;
			}
		}
		if (wrappedObject) {
			resolvedJavaType.setWrapperType(true);
		}
		if (iterableOf != null) {
			resolvedJavaType.setIterableOf(iterableOf);
		}
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaTypeWithoutGeneric(String javaTypeName,
			JavaTypeInfoProvider javaTypeInfo) {
		String javaTypeWithoutGeneric = javaTypeName;
		int genericIndex = javaTypeName.indexOf('<');
		if (genericIndex != -1) {
			// Get the resolved Java type without generic
			// ex : for javaTypeName=java.util.List<E>, we search from the cache the Java
			// type java.util.List.
			javaTypeWithoutGeneric = javaTypeName.substring(0, genericIndex);
			CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(javaTypeWithoutGeneric);
			if (future != null) {
				return future;
			}
		}
		// The Java type (without generic) is not loaded from JDT / IJ side, load it.
		String projectUri = project.getUri();
		ValueResolverKind kind = null;
		if (javaTypeInfo != null && javaTypeInfo instanceof ValueResolver) {
			kind = ((ValueResolver) javaTypeInfo).getKind();
		}
		QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams(javaTypeWithoutGeneric, kind, projectUri);
		return project.getProjectRegistry().getResolvedJavaType(params);
	}

	private CompletableFuture<ResolvedJavaTypeInfo> getValidResolvedJavaTypeInCache(String javaTypeName) {
		CompletableFuture<ResolvedJavaTypeInfo> primitiveType = javaPrimitiveTypes.get(javaTypeName);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}
		if (StringUtils.isEmpty(javaTypeName)) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}

		CompletableFuture<ResolvedJavaTypeInfo> future = project.getResolvedJavaType(javaTypeName);
		if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
			return null;
		}
		return future;
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Parts parts, int partIndex) {
		CompletableFuture<ResolvedJavaTypeInfo> future = null;
		for (int i = 0; i < partIndex + 1; i++) {
			Part current = (parts.getChild(i));
			switch (current.getPartKind()) {
				case Object:
					ObjectPart objectPart = (ObjectPart) current;
					future = resolveJavaType(objectPart);
					future = wrapGeneric(wrapObject(future));
					break;
				case Property:
				case Method:
					if (future != null) {
						ResolvedJavaTypeInfo actualResolvedType = future.getNow(null);
						if (actualResolvedType != null) {
							future = resolveJavaType(current, actualResolvedType);
						} else {
							future = future //
									.thenCompose(resolvedType -> {
										if (resolvedType == null) {
											return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
										}
										return resolveJavaType(current, resolvedType);
									});
						}
					}
					future = wrapGeneric(wrapObject(future));
					break;
				default:
			}
		}
		return future != null ? future : RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
	}

	private CompletableFuture<ResolvedJavaTypeInfo> wrapGeneric(CompletableFuture<ResolvedJavaTypeInfo> future) {
		if (future == null) {
			return null;
		}
		if (future.isDone()) {
			ResolvedJavaTypeInfo resolvedJavaType = future.getNow(null);
			return wrapGeneric(resolvedJavaType, future);
		}
		return future.thenCompose(resolvedJavaType -> wrapGeneric(resolvedJavaType, future));
	}

	private CompletableFuture<ResolvedJavaTypeInfo> wrapGeneric(ResolvedJavaTypeInfo resolvedJavaType,
			CompletableFuture<ResolvedJavaTypeInfo> defaultFuture) {
		if (resolvedJavaType != null
				&& resolvedJavaType != NOT_ITERABLE_JAVA_TYPE
				&& !(resolvedJavaType instanceof ResolvedGenericJavaTypeInfo)
				&& resolvedJavaType.isGenericType()) {
			// Here resolvedJavaType is java.util.List<E>
			// we need to return java.util.List<java.lang.Object>
			String key = resolvedJavaType.getName() + "@generic";
			CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(key);
			if (future != null) {
				return future;
			}

			Map<String, String> generics = new LinkedHashMap<>();
			List<JavaParameterInfo> typeParameters = resolvedJavaType.getTypeParameters();
			for (JavaParameterInfo typeParameter : typeParameters) {
				generics.put(typeParameter.getType(), "java.lang.Object");
			}
			// Update the Java type (apply generic + update references of this Java type for
			// fields / methods).
			resolvedJavaType = updateJavaType(resolvedJavaType, generics);
			future = CompletableFuture.completedFuture(resolvedJavaType);
			project.registerResolvedJavaType(key, future);
			return future;
		}
		return defaultFuture;
	}

	CompletableFuture<ResolvedJavaTypeInfo> wrapObject(CompletableFuture<ResolvedJavaTypeInfo> future) {
		if (future == null) {
			return null;
		}
		if (future.isDone()) {
			ResolvedJavaTypeInfo resolvedJavaType = future.getNow(null);
			return wrapObject(resolvedJavaType, future);
		}
		return future.thenCompose(resolvedJavaType -> wrapObject(resolvedJavaType, future));
	}

	CompletableFuture<ResolvedJavaTypeInfo> wrapObject(ResolvedJavaTypeInfo resolvedJavaType,
			CompletableFuture<ResolvedJavaTypeInfo> future) {
		if (resolvedJavaType != null && resolvedJavaType != NOT_ITERABLE_JAVA_TYPE
				&& resolvedJavaType.isWrapperType()) {
			List<JavaParameterInfo> types = resolvedJavaType.getTypeParameters();
			if (types != null && !types.isEmpty()) {
				// java.util.concurrent.CompletableFuture<java.util.List<org.acme.Item>>
				JavaParameterInfo type = types.get(0);
				String javaTypeToResolve = type.getType(); // java.util.List<org.acme.Item>
				// Here
				// - javaTypeToResolve = java.util.List<org.acme.Item>
				// - iterTypeName = org.acme.Item
				return resolveJavaType(javaTypeToResolve);
			}
		}
		return future;
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(Part part,
			ResolvedJavaTypeInfo resolvedType) {
		JavaMemberInfo member = project.findMember(resolvedType, part.getPartName());
		if (member == null) {
			return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
		}
		String memberType = member.resolveJavaElementType(resolvedType);
		return resolveJavaType(memberType);
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(ObjectPart objectPart) {
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
					future = resolveJavaType(literalJavaType);
				} else {
					Part lastPart = expression.getLastPart();
					if (lastPart == null) {
						return RESOLVED_JAVA_TYPE_INFO_NULL_FUTURE;
					}
					future = resolveJavaType(lastPart);
				}
			}
		}

		if (future == null) {
			future = resolveJavaType(javaType, javaTypeInfo);
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
									return resolveJavaType(resolvedType.getName());
								}
								/*
								 * // -> the class is not valid
								 * // Ex:
								 * // {@org.acme.Item items}
								 * // {#for item in items}
								 * // {item.|}
								 */
								return RESOLVED_JAVA_TYPE_NOT_ITERABLE_FUTURE;

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
							return resolveJavaType(iterTypeName);
						});
			}
		}
		return future;
	}

	private static Section getOwnerSection(Node node) {
		if (node == null) {
			return null;
		}
		if (node.getKind() == NodeKind.Parameter) {
			return ((Parameter) node).getOwnerSection();
		}
		return null;
	}

	/**
	 * Returns true if the given type1 and type2 are the same and false otherwise.
	 *
	 * @param type1 the first Java type to compare.
	 * @param type2 the second Java type to compare.
	 *
	 * @return true if the given type1 and type2 are the same and false otherwise.
	 */
	public static boolean isSameType(String type1, String type2) {
		if (type1.equals(type2)) {
			return true;
		}
		String primitiveType = autoboxing.get(type1);
		if (primitiveType != null) {
			// It's a Java primitive type
			return primitiveType.equals(autoboxing.get(type2));
		}
		return false;
	}
}
