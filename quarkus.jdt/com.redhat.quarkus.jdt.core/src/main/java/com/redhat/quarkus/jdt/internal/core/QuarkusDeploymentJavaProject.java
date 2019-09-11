/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.quarkus.jdt.internal.core;

import static com.redhat.quarkus.jdt.internal.core.QuarkusConstants.QUARKUS_EXTENSION_PROPERTIES;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ExternalJavaProject;

import com.redhat.quarkus.commons.QuarkusPropertiesScope;
import com.redhat.quarkus.jdt.internal.core.utils.DependencyUtil;
import com.redhat.quarkus.jdt.internal.core.utils.JDTQuarkusSearchUtils;

/**
 * A Java project which collect all Quarkus deployment JARs.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusDeploymentJavaProject extends ExternalJavaProject {

	/**
	 * Artifact resolver API
	 *
	 */
	@FunctionalInterface
	public static interface ArtifactResolver {

		String resolve(String groupId, String artifactId, String version);
	}

	public static final ArtifactResolver MAVEN_ARTIFACT_RESOLVER = (groupId, artifactId, version) -> {
		File jarFile = null;
		try {
			jarFile = DependencyUtil.getArtifact(groupId, artifactId, version, null);
		} catch (Exception e) {
			return null;
		}
		return jarFile != null ? jarFile.toString() : null;
	};

	private final IJavaProject rootProject;

	public QuarkusDeploymentJavaProject(IJavaProject rootProject, ArtifactResolver artifactResolver)
			throws JavaModelException {
		super(createDeploymentClasspath(rootProject, artifactResolver));
		this.rootProject = rootProject;
	}

	/**
	 * Returns the classpath of deployment JARs.
	 * 
	 * @param project          the quarkus project
	 * @param artifactResolver the artifact resolver to use to download deployment
	 *                         JARs.
	 * @return the classpath of deployment JARs.
	 * @throws JavaModelException
	 */
	private static IClasspathEntry[] createDeploymentClasspath(IJavaProject project, ArtifactResolver artifactResolver)
			throws JavaModelException {
		List<IClasspathEntry> externalJarEntries = new ArrayList<>();

		IClasspathEntry[] entries = project.getResolvedClasspath(true);
		for (IClasspathEntry entry : entries) {
			switch (entry.getEntryKind()) {

			case IClasspathEntry.CPE_LIBRARY:

				try {
					String jarPath = entry.getPath().toOSString();
					IPackageFragmentRoot root = project.getPackageFragmentRoot(jarPath);
					if (root != null) {
						IJarEntryResource resource = JDTQuarkusSearchUtils.findPropertiesResource(root,
								QUARKUS_EXTENSION_PROPERTIES);
						if (resource != null) {
							Properties properties = new Properties();
							properties.load(resource.getContents());
							// deployment-artifact=io.quarkus\:quarkus-undertow-deployment\:0.21.1
							String deploymentArtifact = properties.getProperty("deployment-artifact");
							String[] result = deploymentArtifact.split(":");
							String groupId = result[0];
							String artifactId = result[1];
							String version = result[2];
							String jarFile = artifactResolver.resolve(groupId, artifactId, version);
							if (jarFile != null) {
								externalJarEntries.add(JavaCore.newLibraryEntry(new Path(jarFile), null, null));
							}
						}
					}
				} catch (Exception e) {
					// do nothing
				}

				break;
			}
		}
		// Add the Quarkus project in classpath to resolve dependencies of deployment
		// Quarkus JARs.
		externalJarEntries.add(JavaCore.newProjectEntry(project.getProject().getLocation()));
		return externalJarEntries.toArray(new IClasspathEntry[externalJarEntries.size()]);
	}

	/**
	 * Returns the java elements to search according the scope:
	 * 
	 * <ul>
	 * <li>sources scope: only Quarkus Java project</li>
	 * <li>classpatch scope:
	 * <ul>
	 * <li>the Quarkus project</li>
	 * <li>all deployment JARs</li>
	 * </ul>
	 * </li>
	 * </ul>
	 *
	 * @param propertiesScope
	 * 
	 * @return the java elements to search
	 * @throws JavaModelException
	 */
	public IJavaElement[] getElementsToSearch(QuarkusPropertiesScope propertiesScope) throws JavaModelException {
		if (propertiesScope == QuarkusPropertiesScope.sources) {
			return new IJavaElement[] { rootProject };
		}
		IPackageFragmentRoot[] roots = super.getPackageFragmentRoots();
		IJavaElement[] elements = new IJavaElement[1 + roots.length];
		elements[0] = rootProject;
		for (int i = 0; i < roots.length; i++) {
			elements[i + 1] = roots[i];
		}
		return elements;
	}

}
