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
package com.redhat.qute.services;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qute.commons.ResolvedJavaTypeInfo;
import com.redhat.qute.parser.template.Parameter;
import com.redhat.qute.parser.template.Template;
import com.redhat.qute.project.QuteProject;
import com.redhat.qute.project.datamodel.JavaDataModelCache;
import com.redhat.qute.services.diagnostics.QuteErrorCode;

/**
 * Resolving java type context host completable future of Java type to resolved.
 * 
 * @author Angelo ZERR
 *
 */
public class ResolvingJavaTypeContext extends ArrayList<CompletableFuture<?>> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(ResolvingJavaTypeContext.class.getName());

	public static final ResolvedJavaTypeInfo RESOLVING_JAVA_TYPE = new ResolvedJavaTypeInfo();

	private final JavaDataModelCache javaCache;
	private boolean projectResolved;

	private boolean dataModelTemplateResolved;

	private boolean binaryUserTagResolved;

	private Set<String> javaTypesSupportedInNativeMode;

	private final Template template;

	public ResolvingJavaTypeContext(Template template, JavaDataModelCache javaCache) {
		this.javaCache = javaCache;
		this.template = template;
		projectResolved = template.getProjectUri() != null;
		if (projectResolved) {
			// Get or load the data model template, data model defined
			//
			// 1) with @CheckedTemplate --> see
			// https://quarkus.io/guides/qute-reference#typesafe_templates
			//
			// 2) or of Template field --> see
			// https://quarkus.io/guides/qute-reference#resteasy_integration
			CompletableFuture<?> f = template.getDataModelTemplate();
			dataModelTemplateResolved = f.isDone();
			if (!dataModelTemplateResolved) {
				super.add(f);
			}
			binaryUserTagResolved = false;
			QuteProject project = template.getProject();
			if (project != null) {
				CompletableFuture<?> tags = project.getBinaryUserTags();
				binaryUserTagResolved = tags.isDone();
				if (!binaryUserTagResolved) {
					super.add(tags);
				}
			}
		}
	}

	/**
	 * Returns true if the Qute project has been resolved and false otherwise.
	 * 
	 * @return true if the Qute project has been resolved and false otherwise.
	 */
	public boolean isProjectResolved() {
		return projectResolved;
	}

	/**
	 * Returns true if the data model template is resolved and false otherwise.
	 * 
	 * @return true if the data model template is resolved and false otherwise.
	 */
	public boolean isDataModelTemplateResolved() {
		return dataModelTemplateResolved;
	}

	/**
	 * Returns true if the binary user tag is resolved and false otherwise.
	 * 
	 * @return true if the binary user tag is resolved and false otherwise.
	 */
	public boolean isBinaryUserTagResolved() {
		return binaryUserTagResolved;
	}

	@Override
	public boolean add(CompletableFuture<?> e) {
		if (super.contains(e)) {
			return true;
		}
		return super.add(e);
	}

	public ResolvedJavaTypeInfo resolveJavaType(String javaType, String projectUri) {
		CompletableFuture<ResolvedJavaTypeInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(javaType,
				projectUri);
		ResolvedJavaTypeInfo resolvedJavaType = resolvingJavaTypeFuture.getNow(RESOLVING_JAVA_TYPE);
		if (isResolvingJavaType(resolvedJavaType)) {
			LOGGER.log(Level.INFO, QuteErrorCode.ResolvingJavaType.getMessage(javaType));
			this.add(resolvingJavaTypeFuture);
			return RESOLVING_JAVA_TYPE;
		}
		return resolvedJavaType;
	}

	public static boolean isResolvingJavaType(ResolvedJavaTypeInfo resolvedJavaType) {
		return RESOLVING_JAVA_TYPE.equals(resolvedJavaType);
	}

	public ResolvedJavaTypeInfo resolveJavaType(Parameter parameter, String projectUri) {
		CompletableFuture<ResolvedJavaTypeInfo> resolvingJavaTypeFuture = javaCache.resolveJavaType(parameter,
				projectUri);
		ResolvedJavaTypeInfo resolvedJavaType = resolvingJavaTypeFuture.getNow(RESOLVING_JAVA_TYPE);
		if (isResolvingJavaType(resolvedJavaType)) {
			this.add(resolvingJavaTypeFuture);
			return RESOLVING_JAVA_TYPE;
		}
		return resolvedJavaType;
	}

	public Set<String> getJavaTypesSupportedInNativeMode() {
		if (javaTypesSupportedInNativeMode == null) {
			javaTypesSupportedInNativeMode = template.getJavaTypesSupportedInNativeMode();
		}
		return javaTypesSupportedInNativeMode;
	}

}
