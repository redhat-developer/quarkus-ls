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
package com.redhat.qute.jdt.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;

import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.jdt.internal.QuteJavaConstants;

/**
 * JDT Qute utilities.
 *
 * @author Angelo ZERR
 *
 */
public class JDTQuteProjectUtils {

	private static final String TEMPLATES_BASE_DIR = "src/main/resources/templates/";

	private JDTQuteProjectUtils() {

	}

	public static ProjectInfo getProjectInfo(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		String projectUri = getProjectURI(project);
		String templateBaseDir = project.getFile(TEMPLATES_BASE_DIR).getLocationURI().toString();
		return new ProjectInfo(projectUri, templateBaseDir);
	}

	/**
	 * Returns the project URI of the given project.
	 *
	 * @param project the java project
	 * @return the project URI of the given project.
	 */
	public static String getProjectUri(IJavaProject project) {
		return getProjectURI(project.getProject());
	}

	/**
	 * returns the project URI of the given project.
	 *
	 * @param project the project
	 * @return the project URI of the given project.
	 */
	public static String getProjectURI(IProject project) {
		return project.getName(); // .getLocation().toOSString();
	}

	/**
	 * Returns true if the given <code>project</code> has a nature specified by
	 * <code>natureId</code> and false otherwise.
	 *
	 * @param project  the project
	 * @param natureId the nature id
	 * @return true if the given <code>project</code> has a nature specified by
	 *         <code>natureId</code> and false otherwise.
	 */
	public static boolean hasNature(IProject project, String natureId) {
		try {
			return project != null && project.hasNature(natureId);
		} catch (CoreException e) {
			return false;
		}
	}

	public static boolean hasQuteSupport(IJavaProject javaProject) {
		return JDTTypeUtils.findType(javaProject, QuteJavaConstants.ENGINE_BUILDER_CLASS) != null;
	}

	public static String getTemplatePath(String className, String methodOrFieldName) {
		StringBuilder path = new StringBuilder(TEMPLATES_BASE_DIR);
		if (className != null) {
			path.append(className);
			path.append('/');
		}
		return path.append(methodOrFieldName).toString();
	}

	public static CompilationUnit getASTRoot(ITypeRoot typeRoot) {
		return ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, null);
	}
}
