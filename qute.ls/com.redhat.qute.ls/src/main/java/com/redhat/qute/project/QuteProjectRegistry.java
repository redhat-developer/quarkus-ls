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
import static com.redhat.qute.services.QuteCompletableFutures.JAVA_ELEMENT_INFO_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.METHOD_VALUE_RESOLVERS_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.NAMESPACE_RESOLVER_INFO_NULL_FUTURE;
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

import com.redhat.qute.commons.JavaElementInfo;
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
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.parser.template.LiteralSupport;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.FieldValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.TypeValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolversRegistry;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.ReflectionJavaTypeFilter;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.utils.StringUtils;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry implements QuteProjectInfoProvider, QuteDataModelProjectProvider, QuteUserTagProvider {

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

	private final QuteUserTagProvider userTagProvider;

	private final QuteJavaTypesProvider javaTypeProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	public QuteProjectRegistry(QuteJavaTypesProvider classProvider, QuteJavaDefinitionProvider definitionProvider,
			QuteResolvedJavaTypeProvider resolvedClassProvider, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider userTagsProvider) {
		this.javaTypeProvider = classProvider;
		this.definitionProvider = definitionProvider;
		this.projects = new HashMap<>();
		this.resolvedTypeProvider = resolvedClassProvider;
		this.dataModelProvider = dataModelProvider;
		this.userTagProvider = userTagsProvider;
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
		return new QuteProject(projectInfo, this, this);
	}

	protected void registerProject(QuteProject project) {
		projects.put(project.getUri(), project);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(TemplateInfoProvider document) {
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
	public void onDidCloseTextDocument(TemplateInfoProvider document) {
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

	private CompletableFuture<List<MethodValueResolver>> getMethodValueResolvers(String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return METHOD_VALUE_RESOLVERS_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return METHOD_VALUE_RESOLVERS_NULL_FUTURE;
		}
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.getMethodValueResolvers();
				});
	}

	public CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template) {
		String projectUri = template.getProjectUri();
		if (StringUtils.isEmpty(projectUri)) {
			// The project uri is not already get (it occurs when Qute template is opened
			// and the project information takes some times).
			// Load the project information and call the data model.
			return template.getProjectFuture() //
					.thenCompose(project -> {
						if (project == null) {
							return EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
						}
						return getDataModelTemplate(template, project.getUri());
					});
		}
		return getDataModelTemplate(template, projectUri);
	}

	private CompletableFuture<ExtendedDataModelTemplate> getDataModelTemplate(Template template, String projectUri) {
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

	@Override
	public CompletableFuture<List<UserTagInfo>> getUserTags(QuteUserTagParams params) {
		return userTagProvider.getUserTags(params);
	}

	/**
	 * Returns the Java member (field or method) from the given Java base type with
	 * the given property and null otherwise.
	 * 
	 * @param baseType   the Java base type.
	 * @param property   the member property (field name or getter method name).
	 * @param projectUri the project Uri used to search in the extended Java type.
	 * @return the Java member (field or method) from the given Java base type with
	 *         the given property and null otherwise.
	 */
	public JavaMemberInfo findMember(ResolvedJavaTypeInfo baseType, String property, String projectUri) {
		if (baseType == null) {
			return null;
		}
		JavaMemberInfo member = findPropertyWithJavaReflection(baseType, property, projectUri);
		if (member != null) {
			return member;
		}
		return findValueResolver(baseType, property, projectUri);
	}

	/**
	 * Returns the Java method / field from the given Java type
	 * <code>baseType</code> which matches the given property name
	 * <code>property</code>.
	 *
	 * @param baseType   the Java base object type.
	 * @param property   the property name to search.
	 * @param nativeMode the native image mode.
	 * @param projectUri the project Uri.
	 *
	 * @return the Java method / field from the given Java type
	 *         <code>baseType</code> which matches the given property name
	 *         <code>property</code>.
	 */
	public JavaMemberResult findProperty(ResolvedJavaTypeInfo baseType, String property, boolean nativeMode,
			String projectUri) {
		JavaMemberResult result = new JavaMemberResult();

		if (!nativeMode) {

			// Find member with Java reflection.
			JavaMemberInfo member = findPropertyWithJavaReflection(baseType, property, projectUri);
			if (member != null) {
				result.setMember(member);
				return result;
			}

			// Find member with value resolvers.
			member = findValueResolver(baseType, property, projectUri);
			if (member != null) {
				result.setMember(member);
			}
			return result;
		}

		// Find member with value resolvers.
		JavaMemberInfo member = findValueResolver(baseType, property, projectUri);
		if (member != null) {
			result.setMember(member);
			return result;
		}

		// Find member with Java reflection.
		member = findPropertyWithJavaReflection(baseType, property, projectUri);
		if (member != null) {
			result.setMember(member);
			return result;
		}
		return result;
	}

	/**
	 * Returns the Java member from the given base type wich matches the given
	 * property by using Java reflection and null otherwise.
	 * 
	 * @param baseType   the Java type.
	 * @param property   the property member.
	 * @param projectUri the project Uri.
	 * 
	 * @return the Java member from the given base type wich matches the given
	 *         property by using Java reflection and null otherwise.
	 */
	private JavaMemberInfo findPropertyWithJavaReflection(ResolvedJavaTypeInfo baseType, String property,
			String projectUri) {
		if (baseType.isIterable() && !baseType.isArray()) {
			// Expression uses iterable type
			// {@java.util.List<org.acme.Item items>
			// {items.size()}
			// Property, method to validate must be done for iterable type (ex :
			// java.util.List
			String iterableType = baseType.getIterableType();
			baseType = resolveJavaType(iterableType, projectUri).getNow(null);
			if (baseType == null) {
				// The java type doesn't exists or it is resolving, stop the validation
				return null;
			}
		}
		// Search in the java root type
		String getterMethodName = computeGetterName(property);
		String booleanGetterName = computeBooleanGetterName(property);
		JavaMemberInfo memberInfo = findMember(baseType, property, getterMethodName, booleanGetterName);
		if (memberInfo != null) {
			return memberInfo;
		}
		if (baseType.getExtendedTypes() != null) {
			// Search in extended types
			for (String superType : baseType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					JavaMemberInfo superMemberInfo = findPropertyWithJavaReflection(resolvedSuperType, property,
							projectUri);
					if (superMemberInfo != null) {
						return superMemberInfo;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the member retrieved by the given property and null otherwise.
	 *
	 * @param property the property
	 * @return the member retrieved by the given property and null otherwise.
	 */
	private JavaMemberInfo findMember(ResolvedJavaTypeInfo resolvedType, String propertyOrMethodName,
			String getterMethodName, String booleanGetterName) {
		JavaFieldInfo fieldInfo = findField(resolvedType, propertyOrMethodName);
		if (fieldInfo != null) {
			return fieldInfo;
		}
		return findMethod(resolvedType, propertyOrMethodName, getterMethodName, booleanGetterName);
	}

	/**
	 * Returns the member field retrieved by the given name and null otherwise.
	 *
	 * @param baseType  the Java base type.
	 * @param fieldName the field name
	 *
	 * @return the member field retrieved by the given property and null otherwise.
	 */
	protected static JavaFieldInfo findField(ResolvedJavaTypeInfo baseType, String fieldName) {
		List<JavaFieldInfo> fields = baseType.getFields();
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
	 * @param baseType          the Java base type.
	 * @param methodName        property or method name.
	 * @param getterMethodName  the getter method name.
	 * @param booleanGetterName the boolean getter method name.
	 *
	 * @return the member field retrieved by the given property or method name and
	 *         null otherwise.
	 */
	protected static JavaMethodInfo findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			String getterMethodName, String booleanGetterName) {
		List<JavaMethodInfo> methods = baseType.getMethods();
		if (methods == null || methods.isEmpty() || isEmpty(methodName)) {
			return null;
		}
		for (JavaMethodInfo method : methods) {
			if (isMatchMethod(method, methodName, getterMethodName, booleanGetterName)) {
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
	 * @param namespace      the namespace part and null otherwise.
	 * @param methodName     the method name to search.
	 * @param parameterTypes the parameter types of the method to search.
	 * @param nativeMode     the native image mode.
	 * @param projectUri     the project Uri.
	 *
	 * @return the Java method from the given Java type <code>baseType</code> which
	 *         matches the given method name <code>methodName</code> with the given
	 *         parameter types <code>parameterTypes</code>.
	 */
	public JavaMemberResult findMethod(ResolvedJavaTypeInfo baseType, String namespace, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, boolean nativeMode, String projectUri) {
		// Search in the java root type
		JavaMemberResult result = new JavaMemberResult();

		if (!nativeMode && baseType != null) {
			// In NO native image mode, search with Java reflection at the begin
			if (findMethod(baseType, methodName, parameterTypes, result, projectUri)) {
				return result;
			}
		}

		// Search in template extension value resolvers retrieved by @TemplateExtension
		List<MethodValueResolver> dynamicResolvers = getMethodValueResolvers(projectUri).getNow(null);
		if (findMethodResolver(baseType, namespace, methodName, parameterTypes, dynamicResolvers, result, projectUri)) {
			return result;
		}

		if (baseType != null) {
			// Search in static value resolvers (ex : orEmpty, take, etc)
			List<MethodValueResolver> staticResolvers = valueResolversRegistry.getResolvers();
			if (findMethodResolver(baseType, null, methodName, parameterTypes, staticResolvers, result, projectUri)) {
				return result;
			}

			if (nativeMode) {
				// In native image mode, search with Java reflection at the end
				if (findMethod(baseType, methodName, parameterTypes, result, projectUri)) {
					return result;
				}
			}
		}

		return result;
	}

	private boolean findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, JavaMemberResult result, String projectUri) {
		if (isEmpty(methodName)) {
			return false;
		}
		List<JavaMethodInfo> methods = baseType.getMethods();
		boolean hasMethods = methods != null && !methods.isEmpty();
		if (hasMethods) {
			for (JavaMethodInfo method : methods) {
				if (isMatchMethod(method, methodName, null, null)) {
					// The current method matches the method name.

					// Check if the current method matches the parameters.
					boolean matchParameters = isMatchParameters(method, parameterTypes, projectUri);
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
		}
		if (baseType.getExtendedTypes() != null) {
			// Search in extended types
			for (String superType : baseType.getExtendedTypes()) {
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					if (findMethod(resolvedSuperType, methodName, parameterTypes, result, projectUri)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean findMethodResolver(ResolvedJavaTypeInfo baseType, String namespace, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, List<MethodValueResolver> resolvers, JavaMemberResult result,
			String projectUri) {
		if (resolvers == null) {
			return false;
		}
		for (MethodValueResolver resolver : resolvers) {
			if (isMatchMethod(resolver, methodName, null, null)) {
				// The current resolver matches the method name.
				if (namespace != null) {
					if (namespace.equals(resolver.getNamespace())) {
						result.setMember(resolver);
						result.setMatchVirtualMethod(true);
						// Check if the current resolver matches the parameters.
						boolean matchParameters = isMatchParameters(resolver, parameterTypes, projectUri);
						result.setMatchParameters(matchParameters);
						return true;
					}
				} else {
					// Check if the baseType matches the type of the first parameter of the current
					// resolver.
					boolean matchVirtualMethod = matchResolver(baseType, resolver, methodName);
					boolean matchParameters = false;
					if (matchVirtualMethod) {
						// Check if the current resolver matches the parameters.
						matchParameters = isMatchParameters(resolver, parameterTypes, projectUri);
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
		}
		return false;
	}

	private boolean isMatchParameters(JavaMethodInfo method, List<ResolvedJavaTypeInfo> parameterTypes,
			String projectUri) {
		boolean virtualMethod = method.isVirtual();
		int nbParameters = method.getParameterslength();
		int declaredNbParameters = parameterTypes.size();
		JavaParameterInfo lastParameter = method.hasParameters() ? method.getParameterAt(method.getParameters().size() - 1) : null;
		boolean varargs = lastParameter != null && lastParameter.isVarargs();
		if (varargs) {
			if (declaredNbParameters == 0) {
				// Method defines just a varargs parameter
				if (parameterTypes.isEmpty()) {
					// with varargs, the parameter is optional
					return true;
				}
			}
			if (declaredNbParameters < nbParameters) {
				return false;
			}
		} else if (declaredNbParameters != nbParameters) {
			return false;
		}

		// Validate all mandatory parameters (without varargs)
		for (int i = 0; i < nbParameters - (varargs ? 1 : 0); i++) {
			JavaParameterInfo parameterInfo = method.getParameters().get(i + (virtualMethod ? 1 : 0));
			ResolvedJavaTypeInfo result = parameterTypes.get(i);

			String parameterType = parameterInfo.getType();
			if (!isMatchType(result, parameterType, projectUri)) {
				return false;
			}
		}

		if (varargs) {
			// Validate varargs parameters
			for (int i = nbParameters - 1; i < declaredNbParameters; i++) {
				ResolvedJavaTypeInfo result = parameterTypes.get(i);

				String parameterType = lastParameter.getVarArgType();
				if (!isMatchType(result, parameterType, projectUri)) {
					return false;
				}
			}
		}

		return true;
	}

	protected static String computeGetterName(String propertyOrMethodName) {
		return "get" + (propertyOrMethodName.charAt(0) + "").toUpperCase()
				+ propertyOrMethodName.substring(1, propertyOrMethodName.length());
	}

	protected static String computeBooleanGetterName(String propertyOrMethodName) {
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
		String methodName = method.getMethodName();
		if (propertyOrMethodName.equals(methodName) || (getterMethodName != null && getterMethodName.equals(methodName))
				|| (booleanGetterName != null && booleanGetterName.equals(methodName))) {
			return true;
		}
		return false;
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public MethodValueResolver findValueResolver(ResolvedJavaTypeInfo baseType, String property, String projectUri) {
		// Search in value resolver
		String literalType = LiteralSupport.getLiteralJavaType(property);
		if (literalType != null) {
			// ex : @java.lang.Integer(base : T[]) : T (see qute-resolvers.jsonc)
			property = "@" + literalType;
		}
		List<MethodValueResolver> resolvers = getResolversFor(baseType, projectUri);
		for (MethodValueResolver resolver : resolvers) {
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
	public List<ValueResolver> getNamespaceResolvers(String namespace, String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return Collections.emptyList();
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return Collections.emptyList();
		}

		ExtendedDataModelProject dataModel = project.getDataModelProject().getNow(null);
		if (dataModel == null) {
			return Collections.emptyList();
		}

		List<ValueResolver> namespaceResolvers = new ArrayList<>();

		List<TypeValueResolver> allTypeResolvers = dataModel.getTypeValueResolvers();
		if (allTypeResolvers != null) {
			for (ValueResolver resolver : allTypeResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}

		List<MethodValueResolver> allMethodResolvers = dataModel.getMethodValueResolvers();
		if (allMethodResolvers != null) {
			for (ValueResolver resolver : allMethodResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}

		List<FieldValueResolver> allFieldResolvers = dataModel.getFieldValueResolvers();
		if (allFieldResolvers != null) {
			for (ValueResolver resolver : allFieldResolvers) {
				if (isMatchNamespace(resolver, namespace, dataModel)) {
					namespaceResolvers.add(resolver);
				}
			}
		}
		return namespaceResolvers;
	}

	private static boolean isMatchNamespace(ValueResolver resolver, String namespace,
			ExtendedDataModelProject dataModel) {
		if (resolver.getNamespace() == null) {
			return false;
		}
		return namespace == null || dataModel.getSimilarNamespace(namespace).equals(resolver.getNamespace());
	}

	public boolean hasNamespace(String namespace, String projectUri) {
		return getAllNamespaces(projectUri).contains(namespace);
	}

	public Set<String> getAllNamespaces(String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return Collections.emptySet();
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return Collections.emptySet();
		}
		ExtendedDataModelProject dataModel = project.getDataModelProject().getNow(null);
		return dataModel != null ? dataModel.getAllNamespaces() : Collections.emptySet();
	}

	public CompletableFuture<JavaElementInfo> findJavaElementWithNamespace(String namespace, String partName,
			String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return JAVA_ELEMENT_INFO_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return JAVA_ELEMENT_INFO_NULL_FUTURE;
		}
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					// Search in types resolvers
					List<TypeValueResolver> typeResolvers = dataModel.getTypeValueResolvers();
					for (TypeValueResolver resolver : typeResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in methods resolvers
					List<MethodValueResolver> methodResolvers = dataModel.getMethodValueResolvers();
					for (MethodValueResolver resolver : methodResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in field resolvers
					List<FieldValueResolver> fieldResolvers = dataModel.getFieldValueResolvers();
					for (FieldValueResolver resolver : fieldResolvers) {
						if (isMatchNamespaceResolver(namespace, partName, resolver, dataModel)) {
							return resolver;
						}
					}
					return null;
				});
	}

	public CompletableFuture<JavaElementInfo> findGlobalVariableJavaElement(String partName, String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return JAVA_ELEMENT_INFO_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return JAVA_ELEMENT_INFO_NULL_FUTURE;
		}
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					// Search in types resolvers
					List<TypeValueResolver> typeResolvers = dataModel.getTypeValueResolvers();
					for (TypeValueResolver resolver : typeResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in methods resolvers
					List<MethodValueResolver> methodResolvers = dataModel.getMethodValueResolvers();
					for (MethodValueResolver resolver : methodResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					// Search in field resolvers
					List<FieldValueResolver> fieldResolvers = dataModel.getFieldValueResolvers();
					for (FieldValueResolver resolver : fieldResolvers) {
						if (isMatchGlobalVariableResolver(partName, resolver, dataModel)) {
							return resolver;
						}
					}
					return null;
				});
	}

	private static boolean isMatchNamespaceResolver(String namespace, String partName, ValueResolver resolver,
			ExtendedDataModelProject dataModel) {
		String name = getResolverName(resolver);
		if (dataModel.getSimilarNamespace(namespace).equals(resolver.getNamespace())
				&& ("*".equals(name) || partName.equals(name))) {
			return true;
		}
		return false;
	}

	private static boolean isMatchGlobalVariableResolver(String partName, ValueResolver resolver,
			ExtendedDataModelProject dataModel) {
		String name = getResolverName(resolver);
		if (resolver.isGlobalVariable() && partName.equals(name)) {
			return true;
		}
		return false;
	}

	private static String getResolverName(ValueResolver resolver) {
		if (resolver.getNamed() != null) {
			return resolver.getNamed();
		}
		if (resolver.getMatchName() != null) {
			return resolver.getMatchName();
		}
		return resolver.getName();
	}

	public CompletableFuture<NamespaceResolverInfo> getNamespaceResolverInfo(String namespace, String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return NAMESPACE_RESOLVER_INFO_NULL_FUTURE;
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return NAMESPACE_RESOLVER_INFO_NULL_FUTURE;
		}
		return project.getDataModelProject() //
				.thenApply(dataModel -> {
					if (dataModel == null) {
						return null;
					}
					return dataModel.getNamespaceResolver(namespace);
				});
	}

	public List<MethodValueResolver> getResolversFor(ResolvedJavaTypeInfo javaType, String projectUri) {
		// Search in static value resolvers (ex : orEmpty, take, etc)
		List<MethodValueResolver> matches = new ArrayList<>();
		for (MethodValueResolver resolver : valueResolversRegistry.getResolvers()) {
			if (matchResolver(javaType, resolver, projectUri)) {
				matches.add(resolver);
			}
		}
		// Search in template extension value resolvers retrieved by @TemplateExtension
		List<MethodValueResolver> allResolvers = getMethodValueResolvers(projectUri).getNow(null);
		if (allResolvers != null) {
			for (MethodValueResolver resolver : allResolvers) {
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
	private boolean matchResolver(ResolvedJavaTypeInfo javaType, MethodValueResolver resolver, String projectUri) {
		// Example with following signature:
		// "orEmpty(arg : java.util.List<T>) : java.lang.Iterable<T>"
		JavaParameterInfo parameter = resolver.getParameterAt(0); // arg : java.util.List<T>
		if (parameter == null) {
			return false;
		}
		if (parameter.getJavaType().isSingleGenericType()) {
			// - <T>
			// - <T[]>
			return javaType.isArray() == parameter.getJavaType().isArray();
		}
		String parameterType = parameter.getJavaType().getName();
		return isMatchType(javaType, parameterType, projectUri);
	}

	private boolean isMatchType(ResolvedJavaTypeInfo javaType, String parameterType, String projectUri) {
		String resolvedTypeName = javaType.getName();
		if ("java.lang.Object".equals(parameterType)) {
			return true;
		}
		if (isSameType(parameterType, resolvedTypeName)) {
			return true;
		}
		// class BigItem <- Item <- SmallItem
		// javaType = BigItem => javaType.getExtendedTypes() = [Item]
		if (javaType.getExtendedTypes() != null) {
			// Loop for first level of super types (ex Item)
			for (String superType : javaType.getExtendedTypes()) {
				if (isSameType(parameterType, superType)) {
					return true;
				}

				// Loop for other levels of super types (ex SmallItem)
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					if (isMatchType(resolvedSuperType, parameterType, projectUri)) {
						return true;
					}
				}
			}
		}
		if (!javaType.getTypeParameters().isEmpty()) {
			ResolvedJavaTypeInfo result = resolveJavaType(resolvedTypeName, projectUri).getNow(null);
			if (result != null && result.getExtendedTypes() != null) {
				for (String superType : result.getExtendedTypes()) {
					if (isSameType(parameterType, superType)) {
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

	public List<ValueResolver> getGlobalVariables(String projectUri) {
		return getGlobalVariablesValueResolvers(projectUri).getNow(null);
	}

	private CompletableFuture<List<ValueResolver>> getGlobalVariablesValueResolvers(String projectUri) {
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
					List<ValueResolver> globalVariables = new ArrayList<>();
					for (ValueResolver valueResolver : dataModel.getTypeValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					for (ValueResolver valueResolver : dataModel.getMethodValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					for (ValueResolver valueResolver : dataModel.getFieldValueResolvers()) {
						if (valueResolver.isGlobalVariable()) {
							globalVariables.add(valueResolver);
						}
					}
					return globalVariables;
				});
	}

	/**
	 * Returns the java type filter according the given root java type and the
	 * native mode.
	 * 
	 * @param rootJavaType         the Java root type.
	 * @param nativeImagesSettings the native images settings.
	 * 
	 * @return the java type filter according the given root java type and the
	 *         native mode.
	 */
	public JavaTypeFilter getJavaTypeFilter(String projectUri, QuteNativeSettings nativeImagesSettings) {
		if (nativeImagesSettings != null && nativeImagesSettings.isEnabled()) {
			if (projectUri != null) {
				QuteProject project = getProject(projectUri);
				if (project != null) {
					return project.getJavaTypeFilterInNativeMode();
				}
			}
		}
		return ReflectionJavaTypeFilter.INSTANCE;
	}
}