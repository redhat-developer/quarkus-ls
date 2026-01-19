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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.manipulation.dom.ASTResolving;

import com.redhat.qute.commons.ProjectFeature;
import com.redhat.qute.commons.ProjectInfo;
import com.redhat.qute.commons.TemplateRootPath;
import com.redhat.qute.jdt.internal.QuteJavaConstants;
import com.redhat.qute.jdt.internal.template.rootpath.TemplateRootPathProviderRegistry;
import com.redhat.qute.jdt.template.project.ProjectFeatureProviderRegistry;
import com.redhat.qute.jdt.template.rootpath.DefaultTemplateRootPathProvider;

import io.quarkus.runtime.util.StringUtil;

/**
 * JDT Qute utilities.
 *
 * @author Angelo ZERR
 *
 */
public class JDTQuteProjectUtils {

	private static final Logger LOGGER = Logger.getLogger(JDTQuteProjectUtils.class.getName());

	/**
	 * Value for Qute annotations indicating behaviour should be using the default
	 */
	public static final String DEFAULTED = "<<defaulted>>";

	private JDTQuteProjectUtils() {

	}

	public static ProjectInfo getProjectInfo(IJavaProject javaProject, IProgressMonitor monitor) {
		IProject project = javaProject.getProject();
		String projectUri = getProjectURI(project);
		// Project dependencies
		List<String> projectDependencies = Collections.emptyList();
		try {
			String[] requiredProjectNames = javaProject.getRequiredProjectNames();
			if (requiredProjectNames != null) {
				projectDependencies = Arrays.asList(requiredProjectNames);
			}
		} catch (JavaModelException e) {
			// Should never occurs
			LOGGER.log(Level.SEVERE,
					"Error while getting project dependencies for '" + javaProject.getElementName() + "' Java project.",
					e);
		}
		// Project folder
		String projectFolder = toUri(javaProject.getProject().getLocation());
		// Template root paths
		List<TemplateRootPath> templateRootPaths = TemplateRootPathProviderRegistry.getInstance()
				.getTemplateRootPaths(javaProject, monitor);
		// Source folders
		Set<String> sourceFolders = getSourceFolders(javaProject);
		// Project Features
		Set<ProjectFeature> projectFeatures = ProjectFeatureProviderRegistry.getInstance()
				.getProjectFeatures(javaProject, monitor);
		return new ProjectInfo(projectUri, projectFolder, projectDependencies, templateRootPaths, sourceFolders,
				projectFeatures);
	}

	private static Set<String> getSourceFolders(IJavaProject javaProject) {
		try {
			Set<String> sourceFolders = new HashSet<>();
			for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
				if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
					continue;
				}

				IResource resource = root.getCorrespondingResource();
				if (resource instanceof IFolder) {
					String uri = toUri(resource.getLocation());
					if (uri != null) {
						sourceFolders.add(uri);
					}
				}
			}
			return sourceFolders;
		} catch (JavaModelException e) {
			// Should never occurs
			LOGGER.log(Level.SEVERE, "Error while getting project source follders for '" + javaProject.getElementName()
					+ "' Java project.", e);
			return Collections.emptySet();
		}
	}

	private static String toUri(IPath path) {
		if (path == null) {
			return null;
		}
		return path.toFile().toURI().toASCIIString();
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
		return project.getName();
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

	public static TemplatePathInfo getTemplatePath(String basePath, String className, String methodOrFieldName,
			boolean ignoreFragments, TemplateNameStrategy templateNameStrategy) {
		String fragmentId = null;
		StringBuilder templateUri = new StringBuilder(DefaultTemplateRootPathProvider.TEMPLATES_BASE_DIR);
		if (basePath != null && !DEFAULTED.equals(basePath)) {
			appendAndSlash(templateUri, basePath);
		} else if (className != null) {
			appendAndSlash(templateUri, className);
		}
		if (!ignoreFragments) {
			int fragmentIndex = methodOrFieldName != null ? methodOrFieldName.lastIndexOf('$') : -1;
			if (fragmentIndex != -1) {
				fragmentId = methodOrFieldName.substring(fragmentIndex + 1, methodOrFieldName.length());
				methodOrFieldName = methodOrFieldName.substring(0, fragmentIndex);
			}
		}
		templateUri.append(defaultedName(templateNameStrategy, methodOrFieldName));
		return new TemplatePathInfo(templateUri.toString(), fragmentId);
	}

	/**
	 * 
	 * @param defaultNameStrategy
	 * @param value
	 * @return
	 * @see <a href=
	 *      "https://github.com/quarkusio/quarkus/blob/32392afcd5cbbed86fe119ed90d4c679d4d52123/extensions/qute/deployment/src/main/java/io/quarkus/qute/deployment/QuteProcessor.java#L562C5-L578C6">QuteProcessor#defaultName</a>
	 */
	private static String defaultedName(TemplateNameStrategy defaultNameStrategy, String value) {
		switch (defaultNameStrategy) {
		case ELEMENT_NAME:
			return value;
		case HYPHENATED_ELEMENT_NAME:
			return StringUtil.hyphenate(value);
		case UNDERSCORED_ELEMENT_NAME:
			return String.join("_", new Iterable<String>() {
				@Override
				public Iterator<String> iterator() {
					return StringUtil.lowerCase(StringUtil.camelHumpsIterator(value));
				}
			});
		default:
			return value;
		// throw new IllegalArgumentException("Unsupported
		// @CheckedTemplate#defaultName() value: " + defaultNameStrategy);
		}
	}

	/**
	 * Appends a segment to a path, add trailing "/" if necessary
	 * 
	 * @param path    the path to append to
	 * @param segment the segment to append to the path
	 */
	public static void appendAndSlash(StringBuilder path, String segment) {
		path.append(segment);
		if (!segment.endsWith("/")) {
			path.append('/');
		}
	}

	public static CompilationUnit getASTRoot(ITypeRoot typeRoot) {
		return ASTResolving.createQuickFixAST((ICompilationUnit) typeRoot, null);
	}
}
