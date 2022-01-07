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

import static com.redhat.qute.parser.template.LiteralSupport.getPrimitiveObjectType;
import static com.redhat.qute.services.QuteCompletableFutures.EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.VALUE_RESOLVERS_NULL_FUTURE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.ValueResolver;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.parser.expression.Part;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.ValueResolversRegistry;
import com.redhat.qute.utils.StringUtils;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry implements QuteProjectInfoProvider, QuteDataModelProjectProvider {

	private final ValueResolversRegistry valueResolversRegistry;

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

	private final Map<String /* project uri */, QuteProject> projects;

	private final QuteResolvedJavaTypeProvider resolvedTypeProvider;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final QuteJavaTypesProvider javaTypeProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	public QuteProjectRegistry(QuteJavaTypesProvider classProvider, QuteJavaDefinitionProvider definitionProvider,
			QuteResolvedJavaTypeProvider resolvedClassProvider, QuteDataModelProjectProvider dataModelProvider) {
		this.javaTypeProvider = classProvider;
		this.definitionProvider = definitionProvider;
		this.projects = new HashMap<>();
		this.resolvedTypeProvider = resolvedClassProvider;
		this.dataModelProvider = dataModelProvider;
		this.valueResolversRegistry = new ValueResolversRegistry();
	}

	/**
	 * Returns the Qute project by the given uri <code>projectUri</code> and null
	 * otherwise.
	 *
	 * @param projectUri the project Uri.
	 *
	 * @return the Qute project by the given uri <code>projectUri</code> and null
	 *         otherwise.
	 */
	public QuteProject getProject(String projectUri) {
		return projects.get(projectUri);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo the project information.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	public QuteProject getProject(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			project = registerProjectSync(projectInfo);
		}
		return project;
	}

	private synchronized QuteProject registerProjectSync(ProjectInfo projectInfo) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project != null) {
			return project;
		}
		project = createProject(projectInfo);
		registerProject(project);
		return project;
	}

	protected QuteProject createProject(ProjectInfo projectInfo) {
		return new QuteProject(projectInfo, this);
	}

	protected void registerProject(QuteProject project) {
		projects.put(project.getUri(), project);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(TemplateProvider document) {
		String projectUri = document.getProjectUri();
		if (projectUri != null) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.onDidOpenTextDocument(document);
			}
		}
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(TemplateProvider document) {
		String projectUri = document.getProjectUri();
		if (projectUri != null) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.onDidCloseTextDocument(document);
			}
		}
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String javaTypeName, String projectUri) {
		CompletableFuture<ResolvedJavaTypeInfo> primitiveType = javaPrimitiveTypes.get(javaTypeName);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}
		if (StringUtils.isEmpty(javaTypeName) || StringUtils.isEmpty(projectUri)) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		CompletableFuture<ResolvedJavaTypeInfo> future = project.getResolvedJavaType(javaTypeName);
		if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
			QuteResolvedJavaTypeParams params = new QuteResolvedJavaTypeParams(javaTypeName, projectUri);
			future = getResolvedJavaType(params) //
					.thenCompose(c -> {
						if (c != null) {
							// Update members with the resolved class
							c.getFields().forEach(f -> {
								f.setJavaType(c);
							});
							c.getMethods().forEach(m -> {
								m.setJavaType(c);
							});
							// Load extended Java types
							if (c.getExtendedTypes() != null) {
								List<CompletableFuture<ResolvedJavaTypeInfo>> resolvingExtendedFutures = new ArrayList<>();
								for (String extendedType : c.getExtendedTypes()) {
									CompletableFuture<ResolvedJavaTypeInfo> extendedFuture = resolveJavaType(
											extendedType, projectUri);
									if (!extendedFuture.isDone()) {
										resolvingExtendedFutures.add(extendedFuture);
									}
								}
								if (!resolvingExtendedFutures.isEmpty()) {
									CompletableFuture<Void> allFutures = CompletableFuture
											.allOf(resolvingExtendedFutures
													.toArray(new CompletableFuture[resolvingExtendedFutures.size()]));
									return allFutures //
											.thenApply(a -> c);
								}
							}
						}
						return CompletableFuture.completedFuture(c);
					});
			project.registerResolvedJavaType(javaTypeName, future);
		}
		return future;
	}

	protected CompletableFuture<ResolvedJavaTypeInfo> getResolvedJavaType(QuteResolvedJavaTypeParams params) {
		return resolvedTypeProvider.getResolvedJavaType(params);
	}

	public void dataModelChanged(JavaDataModelChangeEvent event) {
		Set<String> projectUris = event.getProjectURIs();
		for (String projectUri : projectUris) {
			QuteProject project = getProject(projectUri);
			if (project != null) {
				project.resetJavaTypes();
			}
		}
	}

	public CompletableFuture<List<ValueResolver>> getValueResolvers(String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return VALUE_RESOLVERS_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return VALUE_RESOLVERS_NULL_FUTURE;
		}
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.getValueResolvers();
				});
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		String projectUri = template.getProjectUri();
		if (StringUtils.isEmpty(projectUri)) {
			return EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
		}
		String templateUri = template.getUri();
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.findDataModelTemplate(templateUri);
				});
	}

	public CompletableFuture<List<JavaTypeInfo>> getJavaTypes(QuteJavaTypesParams params) {
		return javaTypeProvider.getJavaTypes(params);
	}

	public CompletableFuture<Location> getJavaDefinition(QuteJavaDefinitionParams params) {
		return definitionProvider.getJavaDefinition(params);
	}

	@Override
	public CompletableFuture<DataModelProject<DataModelTemplate<DataModelParameter>>> getDataModelProject(
			QuteDataModelProjectParams params) {
		return dataModelProvider.getDataModelProject(params);
	}

	public JavaMemberInfo findMember(Part part, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		if (resolvedType == null) {
			return null;
		}
		if (resolvedType.isIterable()) {
			// Expression uses iterable type
			// {@java.util.List<org.acme.Item items>
			// {items.size()}
			// Property, method to validate must be done for iterable type (ex :
			// java.util.List
			String iterableType = resolvedType.getIterableType();
			resolvedType = resolveJavaType(iterableType, projectUri).getNow(null);
			if (resolvedType == null) {
				// The java type doesn't exists or it is resolving, stop the validation
				return null;
			}
		}
		String property = part.getPartName();
		// Search in the java root type
		JavaMemberInfo memberInfo = findMember(resolvedType, property);
		if (memberInfo != null) {
			return memberInfo;
		}
		if (resolvedType.getExtendedTypes() != null) {
			// Search in extended types
			for (String extendedType : resolvedType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedExtendedType = resolveJavaType(extendedType, projectUri).getNow(null);
				if (resolvedExtendedType != null) {
					memberInfo = findMember(resolvedExtendedType, property);
					if (memberInfo != null) {
						return memberInfo;
					}
				}
			}
		}
		// Search in value resolver
		return findValueResolver(property, resolvedType, projectUri);
	}

	/**
	 * Returns the member retrieved by the given property and null otherwise.
	 *
	 * @param property the property
	 * @return the member retrieved by the given property and null otherwise.
	 */
	public JavaMemberInfo findMember(ResolvedJavaTypeInfo resolvedType, String property) {
		JavaFieldInfo fieldInfo = findField(resolvedType, property);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(resolvedType, property);
	}

	/**
	 * Returns the member field retrieved by the given name and null otherwise.
	 *
	 * @param fieldName the field name
	 *
	 * @return the member field retrieved by the given property and null otherwise.
	 */
	protected static JavaFieldInfo findField(ResolvedJavaTypeInfo resolvedType, String fieldName) {
		List<JavaFieldInfo> fields = resolvedType.getFields();
		if (fields == null || fields.isEmpty() || isEmpty(fieldName)) {
			return null;
		}
		for (JavaFieldInfo field : fields) {
			if (fieldName.equals(field.getName())) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the member method retrieved by the given property or method name and
	 * null otherwise.
	 *
	 * @param propertyOrMethodName property or method name
	 *
	 * @return the member field retrieved by the given property or method name and
	 *         null otherwise.
	 */
	protected static JavaMethodInfo findMethod(ResolvedJavaTypeInfo resolvedType, String propertyOrMethodName) {
		List<JavaMethodInfo> methods = resolvedType.getMethods();
		if (methods == null || methods.isEmpty() || isEmpty(propertyOrMethodName)) {
			return null;
		}
		String getterMethodName = computeGetterName(propertyOrMethodName);
		String booleanGetterName = computeBooleanGetterName(propertyOrMethodName);
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, propertyOrMethodName, getterMethodName, booleanGetterName)) {
				return method;
			}
		}
		return null;
	}

	/**
	 * Returns the Java method from the given Java type <code>baseType</code> which
	 * matches the given method name <code>methodName</code> with the given
	 * parameter types <code>parameterTypes</code>.
	 *
	 * @param baseType       the Java base object type.
	 * @param methodName     the method name to search.
	 * @param parameterTypes the parameter types of the method to search.
	 * @param projectUri     the project Uri.
	 *
	 * @return the Java method from the given Java type <code>baseType</code> which
	 *         matches the given method name <code>methodName</code> with the given
	 *         parameter types <code>parameterTypes</code>.
	 */
	public JavaMemberResult findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, String projectUri) {
		// Search in the java root type
		JavaMemberResult result = new JavaMemberResult();
		if (findMethod(baseType, methodName, parameterTypes, result, projectUri)) {
			return result;
		}
		if (baseType.getExtendedTypes() != null) {
			// Search in extended types
			for (String extendedType : baseType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedExtendedType = resolveJavaType(extendedType, projectUri).getNow(null);
				if (resolvedExtendedType != null) {
					if (findMethod(resolvedExtendedType, methodName, parameterTypes, result, projectUri)) {
						return result;
					}
				}
			}
		}

		// Search in value resolvers
		// Search in static value resolvers (ex : orEmpty, take, etc)
		List<ValueResolver> staticResolvers = valueResolversRegistry.getResolvers();
		if (findResolver(baseType, methodName, parameterTypes, staticResolvers, result, projectUri)) {
			return result;
		}
		// Search in template extension value resolvers retrieved by @TemplateExtension
		List<ValueResolver> dynamicResolvers = getValueResolvers(projectUri).getNow(null);
		if (findResolver(baseType, methodName, parameterTypes, dynamicResolvers, result, projectUri)) {
			return result;
		}
		return result;
	}

	private boolean findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, JavaMemberResult result, String projectUri) {
		List<JavaMethodInfo> methods = baseType.getMethods();
		if (methods == null || methods.isEmpty() || isEmpty(methodName)) {
			return false;
		}
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, methodName, methodName, methodName)) {
				// The current method matches the method name.

				// Check if the current method matches the parameters.
				boolean matchParameters = isMatchParameters(method, baseType, parameterTypes, projectUri);
				if (result.getMember() == null || matchParameters) {
					result.setMember(method);
					result.setMatchParameters(matchParameters);
					result.setMatchVirtualMethod(true);
				}
				if (matchParameters) {
					// The current method matches the method name and and parameters types,stop the
					// search
					return true;
				}
			}
		}
		return false;
	}

	private boolean findResolver(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, List<ValueResolver> allResolvers, JavaMemberResult result,
			String projectUri) {
		for (ValueResolver resolver : allResolvers) {
			if (isMatchMethod(resolver, methodName, methodName, methodName)) {
				// The current resolver matches the method name.

				// Check if the baseType matches the type of the first parameter of the current
				// resolver.
				boolean matchVirtualMethod = matchResolver(baseType, resolver, methodName);
				boolean matchParameters = false;
				if (matchVirtualMethod) {
					// Check if the current resolver matches the parameters.
					matchParameters = isMatchParameters(resolver, baseType, parameterTypes, projectUri);
				}
				if (result.getMember() == null || (matchParameters && matchVirtualMethod)) {
					result.setMember(resolver);
					result.setMatchParameters(matchParameters);
					result.setMatchVirtualMethod(matchVirtualMethod);
				}
				if (matchParameters && matchVirtualMethod) {
					// The current resolver matches the method name, the parameters types and the
					// virtual method,stop the search
					return true;
				}
			}
		}
		return false;
	}

	private boolean isMatchParameters(JavaMethodInfo method, ResolvedJavaTypeInfo baseType,
			List<ResolvedJavaTypeInfo> parameterTypes, String projectUri) {
		boolean virtualMethod = method.isVirtual();
		if (parameterTypes.size() != (method.getParameters().size() - (virtualMethod ? 1 : 0))) {
			return false;
		}

		for (int i = 0; i < parameterTypes.size(); i++) {
			JavaParameterInfo parameterInfo = method.getParameters().get(i + (virtualMethod ? 1 : 0));
			ResolvedJavaTypeInfo result = parameterTypes.get(i);

			String parameterType = parameterInfo.getType();
			if (!isMatchType(result, parameterType, projectUri)) {
				return false;
			}
		}
		return true;
	}

	private static String computeGetterName(String propertyOrMethodName) {
		return "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	private static String computeBooleanGetterName(String propertyOrMethodName) {
		return "is" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName) {
		String getterMethodName = computeGetterName(propertyOrMethodName);
		String booleanGetterName = computeBooleanGetterName(propertyOrMethodName);
		return isMatchMethod(method, propertyOrMethodName, getterMethodName, booleanGetterName);
	}

	private static boolean isMatchMethod(JavaMethodInfo method, String propertyOrMethodName, String getterMethodName,
			String booleanGetterName) {
		if (propertyOrMethodName.equals(method.getName()) || getterMethodName.equals(method.getName())
				|| booleanGetterName.equals(method.getName())) {
			return true;
		}
		return false;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public ValueResolver findValueResolver(String property, ResolvedJavaTypeInfo resolvedType, String projectUri) {
		List<ValueResolver> resolvers = getResolversFor(resolvedType, projectUri);
		for (ValueResolver resolver : resolvers) {
			if (isMatchMethod(resolver, property)) {
				return resolver;
			}
		}
		return null;
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

	public List<ValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType, String projectUri) {
		// Search in static value resolvers (ex : orEmpty, take, etc)
		List<ValueResolver> matches = new ArrayList<>();
		for (ValueResolver resolver : valueResolversRegistry.getResolvers()) {
			if (matchResolver(javaType, resolver, projectUri)) {
				matches.add(resolver);
			}
		}
		// Search in template extension value resolvers retrieved by @TemplateExtension
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
		return isMatchType(javaType, parameterType, projectUri);
	}

	private boolean isMatchType(ResolvedJavaTypeInfo javaType, String parameterType, String projectUri) {
		String resolvedTypeName = javaType.getName();
		if ("java.lang.Object".equals(parameterType) && !JavaTypeInfo.PRIMITIVE_TYPES.contains(resolvedTypeName)) {
			return true;
		}
		if (isSameType(parameterType, resolvedTypeName)) {
			return true;
		}
		if (javaType.getExtendedTypes() != null) {
			for (String extendedType : javaType.getExtendedTypes()) {
				if (isSameType(parameterType, extendedType)) {
					return true;
				}
			}
		}
		if (!javaType.getTypeParameters().isEmpty()) {
			ResolvedJavaTypeInfo result = resolveJavaType(resolvedTypeName, projectUri).getNow(null);
			if (result != null && result.getExtendedTypes() != null) {
				for (String extendedType : result.getExtendedTypes()) {
					if (isSameType(parameterType, extendedType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if the given type1 and type2 are the same and false otherwise.
	 *
	 * @param type1 the first Java type to compare.
	 * @param type2 the second Java type to compare.
	 *
	 * @return true if the given type1 and type2 are the same and false otherwise.
	 */
	private boolean isSameType(String type1, String type2) {
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
