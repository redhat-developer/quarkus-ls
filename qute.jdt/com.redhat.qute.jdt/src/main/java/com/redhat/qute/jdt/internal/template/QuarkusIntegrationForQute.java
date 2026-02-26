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
package com.redhat.qute.jdt.internal.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

import com.redhat.qute.commons.QuteProjectScope;
import com.redhat.qute.commons.binary.BinaryTemplate;
import com.redhat.qute.commons.binary.BinaryTemplateInfo;
import com.redhat.qute.commons.datamodel.DataModelParameter;
import com.redhat.qute.commons.datamodel.DataModelProject;
import com.redhat.qute.commons.datamodel.DataModelTemplate;
import com.redhat.qute.jdt.internal.template.datamodel.DataModelProviderRegistry;

/**
 * Provides Quarkus integration for Qute by collecting parameter information
 * (name and Java type) for Qute templates. Several strategies are used:
 *
 * <ul>
 * <li>@CheckedTemplate support: collects parameters by searching for the
 * {@code @CheckedTemplate} annotation.</li>
 * <li>Template field support: collects parameters by searching for
 * {@code Template} instances declared as fields in Java classes.</li>
 * <li>Template extension support: see
 * https://quarkus.io/guides/qute-reference#template_extension_methods</li>
 * </ul>
 *
 * @author Angelo ZERR
 *
 * @see https://quarkus.io/guides/qute-reference#quarkus_integration
 * @see https://quarkus.io/guides/qute-reference#typesafe_templates
 * @see https://quarkus.io/guides/qute-reference#template_extension_methods
 */
public class QuarkusIntegrationForQute {

	/** The JAR entry name for template files. */
	private static final String TEMPLATES_ENTRY = "templates";

	/** The JAR entry name for the application properties file. */
	private static final String APPLICATION_PROPERTIES_ENTRY = "application.properties";

	private static final Logger LOGGER = Logger.getLogger(QuarkusIntegrationForQute.class.getName());

	/**
	 * Returns the data model project for the given Java project, collecting
	 * template parameters from both sources and dependencies.
	 *
	 * @param javaProject the Java project.
	 * @param monitor     the progress monitor.
	 * @return the data model project.
	 * @throws CoreException if an error occurs.
	 */
	public static DataModelProject<DataModelTemplate<DataModelParameter>> getDataModelProject(IJavaProject javaProject,
			IProgressMonitor monitor) throws CoreException {
		return DataModelProviderRegistry.getInstance().getDataModelProject(javaProject,
				QuteProjectScope.SOURCES_AND_DEPENDENCIES, monitor);
	}

	/**
	 * Collects binary templates from all JAR dependencies of the given Java
	 * project. Templates are read from the {@code templates/} entry and its
	 * sub-folders inside each JAR.
	 *
	 * @param javaProject the Java project.
	 * @param monitor     the progress monitor.
	 * @return the list of binary template infos found.
	 * @throws CoreException if an error occurs.
	 */
	public static List<BinaryTemplateInfo> getBinaryTemplates(IJavaProject javaProject, IProgressMonitor monitor)
			throws CoreException {
		List<BinaryTemplateInfo> binaryTemplates = new ArrayList<>();

		// Iterate over each classpath entry and collect binary templates from JAR
		// libraries
		IClasspathEntry[] resolvedClasspath = ((JavaProject) javaProject).getResolvedClasspath();
		for (IClasspathEntry entry : resolvedClasspath) {
			// Skip test-scoped entries
			if (entry.isTest()) {
				continue;
			}
			switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
				String jarPath = entry.getPath().toOSString();
				IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(jarPath);
				if (root != null && root.exists()) {
					// IPackageFragmentRoot may not exist if the Maven dependency is missing,
					// e.g. a dependency with an incorrect groupId/artifactId/version.
					BinaryTemplateInfo info = collectBinaryTemplates(root);
					if (info != null) {
						binaryTemplates.add(info);
					}
				}
			}
		}
		return binaryTemplates;
	}

	/**
	 * Collects binary templates for the given package fragment root (JAR).
	 *
	 * <p>
	 * This method looks for:
	 * <ul>
	 * <li>Template files inside the {@code templates/} JAR entry and all its
	 * sub-packages (e.g. {@code templates/tags/}, {@code templates/partials/}).
	 * </li>
	 * <li>{@code application.properties} at the root of the JAR (via
	 * {@link IPackageFragmentRoot#getNonJavaResources()}).</li>
	 * </ul>
	 * </p>
	 *
	 * @param root the package fragment root (JAR).
	 * @return a {@link BinaryTemplateInfo} if templates were found, {@code null}
	 *         otherwise.
	 * @throws JavaModelException if a JDT model error occurs.
	 * @throws CoreException      if another error occurs.
	 */
	private static BinaryTemplateInfo collectBinaryTemplates(IPackageFragmentRoot root)
			throws JavaModelException, CoreException {

		List<BinaryTemplate> templates = null;

		IJavaElement[] children = root.getChildren();
		for (IJavaElement child : children) {
			if (child instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment) child;
				String elementName = packageFragment.getElementName();

				// Include the 'templates' root package and all its sub-packages
				// (e.g. 'templates.tags', 'templates.partials', 'templates.sub.detail')
				if (elementName.equals(TEMPLATES_ENTRY) || elementName.startsWith(TEMPLATES_ENTRY + ".")) {

					Object[] resources = packageFragment.getNonJavaResources();
					if (resources != null) {

						if (templates == null) {
							templates = new ArrayList<>();
						}

						// Compute the relative folder path from 'templates/' for this package.
						// e.g. 'templates' -> '', 'templates.tags' -> 'tags'
						String relativeFolderPath = buildRelativeFolderPath(elementName);

						for (Object object : resources) {
							if (object instanceof IJarEntryResource) {
								// A resource can be either a file or a sub-directory.
								// Recurse into directories to collect nested files.
								collectJarEntry((IJarEntryResource) object, relativeFolderPath, templates);
							}
						}
					}
				}
			}
		}

		if (templates != null) {
			BinaryTemplateInfo info = new BinaryTemplateInfo();
			String jarName = root.getPath().lastSegment();
			info.setBinaryName(jarName);
			info.setTemplates(templates);

			// Look for application.properties at the root of the JAR.
			// Note: root.getNonJavaResources() returns root-level resources (not packages).
			Object[] rootResources = root.getNonJavaResources();
			if (rootResources != null) {
				for (Object resource : rootResources) {
					if (resource instanceof IJarEntryResource) {
						IJarEntryResource jarEntry = (IJarEntryResource) resource;
						if (APPLICATION_PROPERTIES_ENTRY.equals(jarEntry.getName())) {
							info.setProperties(parseProperties(jarEntry));
							break;
						}
					}
				}
			}
			return info;
		}

		return null;
	}

	/**
	 * Recursively collects template files from a JAR entry resource.
	 *
	 * <p>
	 * A {@link IJarEntryResource} can represent either a file or a directory. When
	 * it is a directory, its children are visited recursively, enriching the
	 * {@code currentPath} at each level so that the final relative path is always
	 * correct regardless of nesting depth.
	 * </p>
	 *
	 * <p>
	 * Examples of resulting paths:
	 * <ul>
	 * <li>{@code currentPath=""}, file {@code index.html} →
	 * {@code "index.html"}</li>
	 * <li>{@code currentPath="tags"}, file {@code search-button.html} →
	 * {@code "tags/search-button.html"}</li>
	 * <li>{@code currentPath="sub"}, directory {@code detail/}, file
	 * {@code page.html} → {@code "sub/detail/page.html"}</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource    the JAR entry resource (file or directory).
	 * @param currentPath the relative path of the parent folder from
	 *                    {@code templates/} (empty string for the root level).
	 * @param templates   the list to fill with collected binary templates.
	 * @throws CoreException if an error occurs while reading the resource.
	 */
	private static void collectJarEntry(IJarEntryResource resource, String currentPath, List<BinaryTemplate> templates)
			throws CoreException {

		if (resource.isFile()) {
			// Build the full relative path by appending the file name to the current path
			String fileName = resource.getName();
			String path = currentPath.isEmpty() ? fileName : currentPath + "/" + fileName;
			String uri = toUri(resource);
			String content = convertStreamToString(resource.getContents());

			BinaryTemplate template = new BinaryTemplate();
			template.setPath(path);
			template.setUri(uri);
			template.setContent(content);
			templates.add(template);
			return;
		}

		// It's a directory: descend into it, appending the directory name to the path.
		// This ensures the relative path grows correctly at each recursion level.
		String childPath = currentPath.isEmpty() ? resource.getName() : currentPath + "/" + resource.getName();
		for (IJarEntryResource child : resource.getChildren()) {
			collectJarEntry(child, childPath, templates);
		}
	}

	/**
	 * Computes the relative folder path from the {@code templates/} root for a
	 * given JDT package element name.
	 *
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code "templates"} → {@code ""} (root, no sub-folder)</li>
	 * <li>{@code "templates.tags"} → {@code "tags"}</li>
	 * <li>{@code "templates.sub.detail"} → {@code "sub/detail"}</li>
	 * </ul>
	 * </p>
	 *
	 * @param packageName the JDT package element name (e.g.
	 *                    {@code "templates.tags"}).
	 * @return the relative folder path from {@code templates/}, or an empty string
	 *         if the package is the root {@code templates} entry.
	 */
	private static String buildRelativeFolderPath(String packageName) {
		if (TEMPLATES_ENTRY.equals(packageName)) {
			// Package is 'templates' itself: no sub-folder
			return "";
		}
		// Strip the 'templates.' prefix and replace dots with slashes
		// e.g. 'templates.tags' -> 'tags', 'templates.sub.detail' -> 'sub/detail'
		return packageName.substring(TEMPLATES_ENTRY.length() + 1).replace('.', '/');
	}

	/**
	 * Parses the given {@code application.properties} JAR entry into a key/value
	 * {@link Map}.
	 *
	 * @param applicationPropertiesFile the JAR entry resource for
	 *                                  {@code application.properties}.
	 * @return a {@link Map} of property key/value pairs, or {@code null} if an
	 *         error occurred while reading the file.
	 * @throws CoreException if an error occurs while opening the stream.
	 */
	private static Map<String, String> parseProperties(IJarEntryResource applicationPropertiesFile)
			throws CoreException {
		Properties props = new Properties();
		try {
			props.load(applicationPropertiesFile.getContents());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error while loading application.properties from JAR entry", e);
			return null;
		}
		// Use a LinkedHashMap to preserve insertion order
		Map<String, String> map = new LinkedHashMap<>();
		for (String key : props.stringPropertyNames()) {
			map.put(key, props.getProperty(key));
		}
		return map;
	}

	/**
	 * Converts the given {@link InputStream} into a String. The stream is closed
	 * automatically after reading.
	 *
	 * @param is the input stream to read.
	 * @return the full content of the stream as a String, or an empty String if the
	 *         stream contains no data.
	 */
	private static String convertStreamToString(InputStream is) {
		try (Scanner s = new java.util.Scanner(is)) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	/** URI scheme used for JDT JAR entry resources. */
	private static final String JDT_SCHEME = "jdt";

	/** URI authority used for JAR entry resources. */
	private static final String CONTENTS_AUTHORITY = "jarentry";

	/**
	 * Builds a {@code jdt://jarentry/...} URI string for the given JAR entry
	 * resource. The URI encodes the full path and the JAR handle identifier so that
	 * the resource can be located unambiguously.
	 *
	 * @see <a href=
	 *      "https://github.com/microsoft/vscode-java-dependency/blob/27c306b770c23b1eba1f9a7c3e70d2793baced68/jdtls.ext/com.microsoft.jdtls.ext.core/src/com/microsoft/jdtls/ext/core/ExtUtils.java#L39">ExtUtils.java</a>
	 *
	 * @param jarEntryFile the JAR entry resource.
	 * @return the URI as an ASCII string, or {@code null} if the URI could not be
	 *         built.
	 */
	private static String toUri(IJarEntryResource jarEntryFile) {
		IPackageFragmentRoot fragmentRoot = jarEntryFile.getPackageFragmentRoot();
		try {
			return new URI(JDT_SCHEME, CONTENTS_AUTHORITY, jarEntryFile.getFullPath().toPortableString(),
					fragmentRoot.getHandleIdentifier(), null).toASCIIString();
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, "Error while generating URI for jar entry file", e);
			return null;
		}
	}

}