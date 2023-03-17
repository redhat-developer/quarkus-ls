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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.redhat.qute.commons.JavaElementInfo;
import com.redhat.qute.commons.JavaFieldInfo;
import com.redhat.qute.commons.JavaMemberInfo;
import com.redhat.qute.commons.JavaMethodInfo;
import com.redhat.qute.commons.JavaParameterInfo;
import com.redhat.qute.commons.JavaTypeInfo;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.QuteJavaDefinitionParams;
import com.redhat.qute.commons.QuteJavaTypesParams;
import com.redhat.qute.commons.QuteJavadocParams;
import com.redhat.qute.commons.QuteProjectParams;
import com.redhat.qute.commons.QuteResolvedJavaTypeParams;
import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.commons.datamodel.JavaDataModelChangeEvent;
import com.redhat.qute.commons.datamodel.QuteDataModelProjectParams;
import com.redhat.qute.commons.datamodel.resolvers.NamespaceResolverInfo;
import com.redhat.qute.commons.datamodel.resolvers.ValueResolverKind;
import com.redhat.qute.commons.jaxrs.JaxRsParamKind;
import com.redhat.qute.commons.jaxrs.RestParam;
import com.redhat.qute.commons.usertags.QuteUserTagParams;
import com.redhat.qute.commons.usertags.UserTagInfo;
import com.redhat.qute.ls.api.QuteDataModelProjectProvider;
import com.redhat.qute.ls.api.QuteJavaDefinitionProvider;
import com.redhat.qute.ls.api.QuteJavaTypesProvider;
import com.redhat.qute.ls.api.QuteJavadocProvider;
import com.redhat.qute.ls.api.QuteProjectInfoProvider;
import com.redhat.qute.ls.api.QuteResolvedJavaTypeProvider;
import com.redhat.qute.ls.api.QuteUserTagProvider;
import com.redhat.qute.parser.template.JavaTypeInfoProvider;
import com.redhat.qute.parser.template.LiteralSupport;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelProject;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.project.datamodel.resolvers.FieldValueResolver;
import com.redhat.qute.project.datamodel.resolvers.MethodValueResolver;
import com.redhat.qute.project.datamodel.resolvers.TypeValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolver;
import com.redhat.qute.project.datamodel.resolvers.ValueResolversRegistry;
import com.redhat.qute.project.documents.QuteOpenedTextDocument;
import com.redhat.qute.project.documents.TemplateValidator;
import com.redhat.qute.services.nativemode.JavaTypeFilter;
import com.redhat.qute.services.nativemode.ReflectionJavaTypeFilter;
import com.redhat.qute.settings.QuteNativeSettings;
import com.redhat.qute.utils.FileUtils;
import com.redhat.qute.utils.StringUtils;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 *
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry
		implements QuteDataModelProjectProvider, QuteUserTagProvider, QuteJavadocProvider {

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

	private final QuteProjectInfoProvider projectInfoProvider;

	private final QuteResolvedJavaTypeProvider resolvedTypeProvider;

	private final QuteDataModelProjectProvider dataModelProvider;

	private final QuteUserTagProvider userTagProvider;

	private final QuteJavaTypesProvider javaTypeProvider;

	private final QuteJavaDefinitionProvider definitionProvider;

	private final QuteJavadocProvider javadocProvider;

	private final TemplateValidator validator;

	private boolean didChangeWatchedFilesSupported;

	public QuteProjectRegistry(QuteProjectInfoProvider projectInfoProvider, QuteJavaTypesProvider javaTypeProvider,
			QuteJavaDefinitionProvider definitionProvider,
			QuteResolvedJavaTypeProvider resolvedClassProvider, QuteDataModelProjectProvider dataModelProvider,
			QuteUserTagProvider userTagsProvider, QuteJavadocProvider javadocProvider,
			TemplateValidator validator) {
		this.projectInfoProvider = projectInfoProvider;
		this.javaTypeProvider = javaTypeProvider;
		this.definitionProvider = definitionProvider;
		this.projects = new HashMap<>();
		this.resolvedTypeProvider = resolvedClassProvider;
		this.dataModelProvider = dataModelProvider;
		this.userTagProvider = userTagsProvider;
		this.javadocProvider = javadocProvider;
		this.valueResolversRegistry = new ValueResolversRegistry();
		this.validator = validator;
	}

	/**
	 * Enable/disable did change watched file support.
	 *
	 * @param didChangeWatchedFilesSupported true if did changed file is supported
	 *                                       by the LSP client and false otherwise.
	 */
	public void setDidChangeWatchedFilesSupported(boolean didChangeWatchedFilesSupported) {
		this.didChangeWatchedFilesSupported = didChangeWatchedFilesSupported;
	}

	/**
	 * Returns true if did changed file is supported by the LSP client and false
	 * otherwise.
	 *
	 * @return true if did changed file is supported by the LSP client and false
	 *         otherwise.
	 */
	public boolean isDidChangeWatchedFilesSupported() {
		return didChangeWatchedFilesSupported;
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
		return getProject(projectInfo, true);
	}

	/**
	 * Returns the Qute project by the given info <code>projectInfo</code>.
	 *
	 * @param projectInfo    the project information.
	 * @param validateOnLoad true if validation of templates must be validated if
	 *                       project is created and
	 *                       false otherwise.
	 *
	 * @return the Qute project by the given info <code>projectInfo</code>.
	 */
	private QuteProject getProject(ProjectInfo projectInfo, boolean validateOnLoad) {
		String projectUri = projectInfo.getUri();
		QuteProject project = getProject(projectUri);
		if (project == null) {
			project = registerProjectSync(projectInfo);
			if (validator != null && validateOnLoad) {
				QuteProject newProject = project;
				// Validate closed Qute template on project load.
				CompletableFuture.runAsync(() -> newProject.validateClosedTemplates());
			}
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
		return new QuteProject(projectInfo, this, validator);
	}

	protected void registerProject(QuteProject project) {
		projects.put(project.getUri(), project);
	}

	/**
	 * Open a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidOpenTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidOpenTextDocument(document);
		}
	}

	/**
	 * Close a Qute template.
	 *
	 * @param document the Qute template.
	 */
	public void onDidCloseTextDocument(QuteTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidCloseTextDocument(document);
		}
	}

	public void onDidSaveTextDocument(QuteOpenedTextDocument document) {
		QuteProject project = document.getProject();
		if (project != null) {
			project.onDidSaveTextDocument(document);
		}
	}

	public CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String javaTypeName,
			JavaTypeInfoProvider javaTypeInfo, String projectUri) {
		QuteProject project = StringUtils.isEmpty(projectUri) ? null : getProject(projectUri);
		if (project == null) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(javaTypeName, project);
		if (future != null) {
			return future;
		}
		return resolveJavaType(javaTypeName, javaTypeInfo, project, new HashSet<>());
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaType(String javaTypeName,
			JavaTypeInfoProvider javaTypeInfo, QuteProject project,
			Set<String> visited) {

		if (StringUtils.isEmpty(javaTypeName)) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		if (visited.contains(javaTypeName)) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}
		visited.add(javaTypeName);

		CompletableFuture<ResolvedJavaTypeInfo> primitiveType = javaPrimitiveTypes.get(javaTypeName);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}

		String projectUri = project.getUri();
		// Try to get the Java type from the cache
		CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(javaTypeName, project);
		if (future == null) {
			// The Java type needs to be loaded.
			if (javaTypeName.endsWith("[]")) {
				// Array case (ex : org.acme.Item[]), try to to get the, get the item type (ex :
				// org.acme.Item)
				future = resolveJavaType(javaTypeName.substring(0, javaTypeName.length() - 2), javaTypeInfo, projectUri) //
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
						javaTypeName, javaTypeInfo, project);
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
												.add(resolveJavaType(extendedType, javaTypeInfo, project, visited));
									}
									if (!resolvingExtendedFutures.isEmpty()) {
										CompletableFuture<Void> allFutures = CompletableFuture
												.allOf(resolvingExtendedFutures.toArray(
														new CompletableFuture[resolvingExtendedFutures.size()]));
										return allFutures //
												.thenApply(all -> {
													updateIterable(resolvedJavaTypeWithLoadedDeps,
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

	public void updateIterable(final ResolvedJavaTypeInfo resolvedJavaType,
			Set<CompletableFuture<ResolvedJavaTypeInfo>> resolvingExtendedFutures) {
		String iterableOf = null;
		for (CompletableFuture<ResolvedJavaTypeInfo> g : resolvingExtendedFutures) {
			// Update the iterable of the loaded Java type
			ResolvedJavaTypeInfo extendedType = g.getNow(null);
			if (extendedType != null) {
				if ("java.lang.Iterable".equals(extendedType.getName())) {
					extendedType.isIterable();
					iterableOf = extendedType.getIterableOf();
					break;
				} else if (extendedType.getIterableOf() != null) {
					iterableOf = extendedType.getIterableOf();
					break;
				}
			}
		}

		if (iterableOf != null) {
			resolvedJavaType.setIterableOf(iterableOf);
		}
	}

	private CompletableFuture<ResolvedJavaTypeInfo> resolveJavaTypeWithoutGeneric(String javaTypeName,
			JavaTypeInfoProvider javaTypeInfo,
			QuteProject project) {
		String javaTypeWithoutGeneric = javaTypeName;
		int genericIndex = javaTypeName.indexOf('<');
		if (genericIndex != -1) {
			// Get the resolved Java type without generic
			// ex : for javaTypeName=java.util.List<E>, we search from the cache the Java
			// type java.util.List.
			javaTypeWithoutGeneric = javaTypeName.substring(0, genericIndex);
			CompletableFuture<ResolvedJavaTypeInfo> future = getValidResolvedJavaTypeInCache(javaTypeWithoutGeneric,
					project);
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
		return getResolvedJavaType(params);
	}

	private CompletableFuture<ResolvedJavaTypeInfo> getValidResolvedJavaTypeInCache(String javaTypeName,
			QuteProject project) {
		CompletableFuture<ResolvedJavaTypeInfo> primitiveType = javaPrimitiveTypes.get(javaTypeName);
		if (primitiveType != null) {
			// It's a primitive type like boolean, double, float, etc
			return primitiveType;
		}
		if (StringUtils.isEmpty(javaTypeName)) {
			return RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
		}

		CompletableFuture<ResolvedJavaTypeInfo> future = project.getResolvedJavaType(javaTypeName);
		if (future == null || future.isCancelled() || future.isCompletedExceptionally()) {
			return null;
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

	private JavaMemberInfo findPropertyWithJavaReflection(ResolvedJavaTypeInfo baseType, String property,
			String projectUri) {
		return findPropertyWithJavaReflection(baseType, property, projectUri, new HashSet<>());
	}

	/**
	 * Returns the Java member from the given base type wich matches the given
	 * property by using Java reflection and null otherwise.
	 *
	 * @param baseType   the Java type.
	 * @param property   the property member.
	 * @param projectUri the project Uri.
	 * @param visited    the java types that have already been visited
	 *
	 * @return the Java member from the given base type wich matches the given
	 *         property by using Java reflection and null otherwise.
	 */
	private JavaMemberInfo findPropertyWithJavaReflection(ResolvedJavaTypeInfo baseType, String property,
			String projectUri, Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(baseType)) {
			return null;
		}
		visited.add(baseType);

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
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, null, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					JavaMemberInfo superMemberInfo = findPropertyWithJavaReflection(resolvedSuperType, property,
							projectUri, visited);
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
		return findMethod(baseType, methodName, parameterTypes, result, projectUri, new HashSet<>());
	}

	private boolean findMethod(ResolvedJavaTypeInfo baseType, String methodName,
			List<ResolvedJavaTypeInfo> parameterTypes, JavaMemberResult result, String projectUri,
			Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(baseType)) {
			return false;
		}
		visited.add(baseType);

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
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, null, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					if (findMethod(resolvedSuperType, methodName, parameterTypes, result, projectUri, visited)) {
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
					if (baseType == null) {
						return false;
					}
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
		JavaParameterInfo lastParameter = method.hasParameters()
				? method.getParameterAt(method.getParameters().size() - 1)
				: null;
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
			boolean valid = false;
			if (method.getJaxRsMethodKind() != null) {
				// Renarde parameters validation rules:
				// 1) all parameters which are annotated with @RestPath (or @PathParam) are
				// considered as required
				// 2) all parameters which are annotated with @RestQuery (or @QueryParam) are
				// optional
				// 3) all parameters which are annotated with @RestForm (or @FormParam) cannot
				// appear in the method of uri.
				int nbRequiredParameters = 0;
				int nbOptionalParameters = 0;
				for (int i = 0; i < nbParameters - (varargs ? 1 : 0); i++) {
					JavaParameterInfo parameterInfo = method.getParameters().get(i);
					RestParam restParam = method.getRestParameter(parameterInfo.getName());
					if (restParam != null) {
						if (restParam.getParameterKind() == JaxRsParamKind.PATH) {
							nbRequiredParameters++;
						} else if (restParam.getParameterKind() == JaxRsParamKind.QUERY) {
							nbOptionalParameters++;
						}
					}
				}
				if (declaredNbParameters < nbRequiredParameters) {
					return false;
				}
				if (declaredNbParameters > nbRequiredParameters + nbOptionalParameters) {
					return false;
				}
				nbParameters = declaredNbParameters;
				valid = true;
			}
			if (!valid) {
				return false;
			}
		}

		// Validate all mandatory parameters (without varargs)
		for (int i = 0; i < nbParameters - (varargs ? 1 : 0); i++) {
			JavaParameterInfo parameterInfo = method.getParameters().get(i + (virtualMethod ? 1 : 0));
			ResolvedJavaTypeInfo result = parameterTypes.get(i);

			// If the type info isn't available, assume the type matches.
			// This is helpful eg. when getting the docs for a method whose
			// parameters haven't been input correctly yet.
			if (result != null) {
				String parameterType = parameterInfo.getType();
				if (!isMatchType(result, parameterType, projectUri)) {
					return false;
				}
			}
		}

		if (varargs) {
			// Validate varargs parameters
			for (int i = nbParameters - 1; i < declaredNbParameters; i++) {
				String parameterType = lastParameter.getVarArgType();
				ResolvedJavaTypeInfo result = parameterTypes.get(i);
				// If the type info isn't available, assume the type matches
				// (see note above)
				if (result != null) {
					if (!isMatchType(result, parameterType, projectUri)) {
						return false;
					}
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

	/**
	 * Return all namespaces from the given Qute project Uri.
	 *
	 * @param projectUri the Qute project Uri
	 *
	 * @return all namespace from the given Qute project Uri.
	 */
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

	/**
	 * Return all template extensions classes from the given Qute project Uri.
	 *
	 * @param projectUri the Qute project Uri
	 *
	 * @return all template extensions classes from the given Qute project Uri.
	 */
	public Set<String> getAllTemplateExtensionsClasses(String projectUri) {
		if (StringUtils.isEmpty(projectUri)) {
			return Collections.emptySet();
		}
		QuteProject project = getProject(projectUri);
		if (project == null) {
			return Collections.emptySet();
		}
		ExtendedDataModelProject dataModel = project.getDataModelProject().getNow(null);
		return dataModel != null ? dataModel.getAllTemplateExtensionsClasses() : Collections.emptySet();
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
		return isMatchType(javaType, parameterType, projectUri, new HashSet<>());
	}

	private boolean isMatchType(ResolvedJavaTypeInfo javaType, String parameterType, String projectUri,
			Set<ResolvedJavaTypeInfo> visited) {
		if (visited.contains(javaType)) {
			return false;
		}
		visited.add(javaType);

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
				ResolvedJavaTypeInfo resolvedSuperType = resolveJavaType(superType, null, projectUri).getNow(null);
				if (resolvedSuperType != null) {
					if (isMatchType(resolvedSuperType, parameterType, projectUri, visited)) {
						return true;
					}
				}
			}
		}
		if (!javaType.getTypeParameters().isEmpty()) {
			ResolvedJavaTypeInfo result = resolveJavaType(resolvedTypeName, null, projectUri).getNow(null);
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
		return getGlobalVariables(project);
	}

	public static CompletableFuture<List<ValueResolver>> getGlobalVariables(QuteProject project) {
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

	@Override
	public CompletableFuture<String> getJavadoc(QuteJavadocParams params) {
		return javadocProvider.getJavadoc(params);
	}

	private QuteProject findProjectFor(Path path) {
		for (QuteProject project : projects.values()) {
			if (isBelongToProject(path, project)) {
				return project;
			}
		}
		return null;
	}

	private static boolean isBelongToProject(Path path, QuteProject project) {
		return path.startsWith(project.getTemplateBaseDir());
	}

	public Collection<QuteProject> getProjects() {
		return projects.values();
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		Set<QuteProject> projects = new HashSet<>();
		List<FileEvent> changes = params.getChanges();
		// Some qute templates are deleted, created, or changed
		// Collect impacted Qute projects
		for (FileEvent fileEvent : changes) {
			String fileUri = fileEvent.getUri();
			Path templatePath = FileUtils.createPath(fileUri);
			QuteProject project = findProjectFor(templatePath);
			if (project != null) {
				String templateId = project.getTemplateId(templatePath);
				if (project.isTemplateOpened(templateId)) {
					projects.add(project);
				} else {
					// In case of closed document, we collect the project and update the cache
					switch (fileEvent.getType()) {
						case Changed:
						case Created: {
							// The template is created, update the cache and collect the project
							QuteTextDocument closedTemplate = project
									.onDidCreateTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
							}
							break;
						}
						case Deleted: {
							// The template is deleted, update the cache, collect the project and publish
							// empty diagnostics for this file
							QuteTextDocument closedTemplate = project.onDidDeleteTemplate(templatePath);
							if (closedTemplate != null) {
								projects.add(closedTemplate.getProject());
								if (validator != null) {
									validator.clearDiagnosticsFor(fileUri);
								}
							}
							break;
						}
					}
				}
			}
		}

		if (projects.isEmpty()) {
			return;
		}

		// trigger validation for all opened and closed Qute template files which belong
		// to the project list.
		if (validator != null) {
			validator.triggerValidationFor(projects);
		}
	}

	public void dispose() {
		for (QuteProject project : projects.values()) {
			project.dispose();
		}
	}

	public CompletableFuture<ProjectInfo> getProjectInfo(QuteProjectParams params) {
		return projectInfoProvider.getProjectInfo(params);
	}

	/**
	 * Try to load the Qute project of the given workspace folder.
	 * 
	 * @param workspaceFolder the workspace folder.
	 * @param progressSupport the LSP client progress support and null otherwise.
	 */
	public void tryToLoadQuteProject(WorkspaceFolder workspaceFolder, ProgressSupport progressSupport) {
		String projectName = workspaceFolder.getName();

		String progressId = createAndStartProgress(projectName, progressSupport);

		// Load Qute project from the Java component (collect Java data model)
		String projectUri = workspaceFolder.getUri();
		QuteProjectParams params = new QuteProjectParams(projectUri);
		getProjectInfo(params)
				.thenAccept(projectInfo -> {
					if (projectInfo == null) {
						// The workspace folder is not a Qute project, end the process
						endProgress(progressId, progressSupport);
						return;
					}

					// The workspace folder is a Qute project, load the data model from Java
					// component
					QuteProject project = getProject(projectInfo, false);
					if (progressSupport != null) {
						WorkDoneProgressReport report = new WorkDoneProgressReport();
						report.setMessage("Loading data model for '" + projectName + "' Qute project.");
						report.setPercentage(10);
						progressSupport.notifyProgress(progressId, report);
					}
					project.getDataModelProject()
							.thenAccept(dataModel -> {
								// The Java data model is collected for the project, validate all templates of
								// the project
								if (progressSupport != null) {
									WorkDoneProgressReport report = new WorkDoneProgressReport();
									report.setMessage(
											"Validating Qute templates for '" + projectName + "' Qute project.");
									report.setPercentage(80);
									progressSupport.notifyProgress(progressId, report);
								}
								// Validate Qute templates
								project.validateClosedTemplates();

								// End progress
								endProgress(progressId, progressSupport);

							}).exceptionally((a) -> {
								endProgress(progressId, progressSupport);
								return null;
							});

				}).exceptionally((a) -> {
					endProgress(progressId, progressSupport);
					return null;
				});

	}

	private static String createAndStartProgress(String projectName, ProgressSupport progressSupport) {
		if (progressSupport == null) {
			return null;
		}
		String progressId = UUID.randomUUID().toString();
		// Initialize progress
		WorkDoneProgressCreateParams create = new WorkDoneProgressCreateParams(Either.forLeft(progressId));
		progressSupport.createProgress(create);

		// Start progress
		WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
		begin.setMessage("Trying to load '" + projectName + "' as Qute project.");
		begin.setPercentage(100);
		progressSupport.notifyProgress(progressId, begin);
		return progressId;
	}

	private static void endProgress(String progressId, ProgressSupport progressSupport) {
		if (progressSupport != null) {
			WorkDoneProgressEnd end = new WorkDoneProgressEnd();
			progressSupport.notifyProgress(progressId, end);
		}
	}
}
