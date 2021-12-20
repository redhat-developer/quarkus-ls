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

import static com.redhat.qute.services.QuteCompletableFutures.EXTENDED_TEMPLATE_DATAMODEL_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.RESOLVED_JAVA_CLASSINFO_NULL_FUTURE;
import static com.redhat.qute.services.QuteCompletableFutures.VALUE_RESOLVERS_NULL_FUTURE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Location;

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
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.datamodel.ExtendedDataModelTemplate;
import com.redhat.qute.utils.StringUtils;

/**
 * Registry which hosts Qute project {@link QuteProject}.
 * 
 * @author Angelo ZERR
 *
 */
public class QuteProjectRegistry implements QuteProjectInfoProvider, QuteDataModelProjectProvider {

	private static final Map<String, CompletableFuture<ResolvedJavaTypeInfo>> javaPrimitiveTypes;

	static {
		javaPrimitiveTypes = new HashMap<>();
		registerPrimitiveType("boolean");
		registerPrimitiveType("byte");
		registerPrimitiveType("double");
		registerPrimitiveType("float");
		registerPrimitiveType("int");
		registerPrimitiveType("long");
	}

	private static void registerPrimitiveType(String type) {
		ResolvedJavaTypeInfo classInfo = new ResolvedJavaTypeInfo();
		classInfo.setSignature(type);
		javaPrimitiveTypes.put(type, CompletableFuture.completedFuture(classInfo));

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
								f.setResolvedType(c);
							});
							c.getMethods().forEach(m -> {
								m.setResolvedType(c);
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
}
