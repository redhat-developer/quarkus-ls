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
import org.eclipse.jdt.core.IClasspathEntry;
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
		String projectFolder = getProjectFolder(javaProject);
		// Source folders
		Set<String> sourceFolders = getSourceFolders(javaProject);
		// Template root paths
		List<TemplateRootPath> templateRootPaths = TemplateRootPathProviderRegistry.getInstance()
				.getTemplateRootPaths(javaProject, projectFolder, sourceFolders, monitor);
		// Project Features
		Set<ProjectFeature> projectFeatures = ProjectFeatureProviderRegistry.getInstance()
				.getProjectFeatures(javaProject, monitor);
		return new ProjectInfo(projectUri, projectFolder, projectDependencies, templateRootPaths, sourceFolders,
				projectFeatures);
	}

	public static String getProjectFolder(IJavaProject javaProject) {
		return toUri(javaProject.getProject().getLocation());
	}

	/**
	 * Returns the set of source folders that are not Java source folders and not
	 * output locations (e.g. {@code src/main/resources}) for the given Java
	 * project.
	 *
	 * <p>
	 * The following folders are excluded:
	 * <ul>
	 * <li>Folders whose last segment is {@code java} (e.g.
	 * {@code src/main/java})</li>
	 * <li>Folders that are under an output location (e.g.
	 * {@code target/generated-sources/annotations})</li>
	 * </ul>
	 *
	 * <p>
	 * If no resource source folders are found, falls back to all source folders
	 * that are not output locations (regardless of their name).
	 *
	 * @param javaProject the Java project to retrieve source folders from
	 * @return a set of URI strings representing the resource source folders, or an
	 *         empty set if an error occurs
	 */
	public static Set<String> getSourceFolders(IJavaProject javaProject) {
		try {
			// Collect all output locations (default + per-entry) to exclude generated
			// sources
			Set<IPath> outputLocations = new HashSet<>();
			outputLocations.add(javaProject.getOutputLocation());
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getOutputLocation() != null) {
					outputLocations.add(entry.getOutputLocation());
				}
			}

			Set<String> sourceFolders = new HashSet<>();
			for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
				// Only consider source folders
				if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
					continue;
				}

				IPath path = root.getPath();

				// Exclude folders that are under an output location (e.g.
				// target/generated-sources/annotations)
				// Exclude folders that are under an output location (e.g.
				// target/generated-sources/annotations)
				if (outputLocations.stream().anyMatch(output -> output.isPrefixOf(path))) {
					continue;
				}

				// Exclude Java source folders (e.g. src/main/java)
				if ("java".equals(path.lastSegment())) {
					continue;
				}

				// Only consider IFolder resources (excludes IProject root)
				IResource resource = root.getCorrespondingResource();
				if (resource instanceof IFolder) {
					String uri = toUri(resource.getLocation());
					if (uri != null) {
						sourceFolders.add(uri);
					}
				}
			}

			// If no resource source folders were found, fall back to all non-output source
			// folders
			// (regardless of their name, e.g. src/main/resources in a non-standard project
			// layout)
			if (sourceFolders.isEmpty()) {
				for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
					if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
						continue;
					}

					IPath path = root.getPath();

					// Still exclude output locations
					if (outputLocations.stream().anyMatch(path::isPrefixOf)) {
						continue;
					}

					// Only consider IFolder resources (excludes IProject root)
					IResource resource = root.getCorrespondingResource();
					if (resource instanceof IFolder) {
						String uri = toUri(resource.getLocation());
						if (uri != null) {
							sourceFolders.add(uri);
						}
					}
				}
			}

			return sourceFolders;
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting project source folders for '" + javaProject.getElementName()
					+ "' Java project.", e);
			return Collections.emptySet();
		}
	}

	/**
	 * Returns the relative path of the first resource source folder found for the
	 * given Java project (e.g. {@code src/main/resources}).
	 *
	 * @param javaProject the Java project to retrieve the relative source folder
	 *                    from
	 * @return the relative path of the first resource source folder, or
	 *         {@code null} if none is found
	 */
	public static String getRelativeResourcesFolder(IJavaProject javaProject) {
		try {
			// Collect all output locations to exclude generated sources
			Set<IPath> outputLocations = new HashSet<>();
			outputLocations.add(javaProject.getOutputLocation());
			for (IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getOutputLocation() != null) {
					outputLocations.add(entry.getOutputLocation());
				}
			}

			IPath projectPath = javaProject.getProject().getFullPath();

			for (IPackageFragmentRoot root : javaProject.getPackageFragmentRoots()) {
				if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
					continue;
				}

				IPath path = root.getPath();

				if (outputLocations.stream().anyMatch(output -> output.isPrefixOf(path))) {
					continue;
				}

				if ("java".equals(path.lastSegment())) {
					continue;
				}

				if (root.getCorrespondingResource() instanceof IFolder) {
					// Make the path relative to the project (e.g. /myproject/src/main/resources ->
					// src/main/resources)
					return path.makeRelativeTo(projectPath).toString();
				}
			}
			return null;
		} catch (JavaModelException e) {
			LOGGER.log(Level.SEVERE, "Error while getting relative source folder for '" + javaProject.getElementName()
					+ "' Java project.", e);
			return null;
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
			boolean ignoreFragments, TemplateNameStrategy templateNameStrategy, String relativeSourceFolder) {
		String fragmentId = null;
		StringBuilder templateUri = new StringBuilder(TemplateRootPath.resolveSinglePath("", relativeSourceFolder,
				DefaultTemplateRootPathProvider.TEMPLATES_BASE_DIR));
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
